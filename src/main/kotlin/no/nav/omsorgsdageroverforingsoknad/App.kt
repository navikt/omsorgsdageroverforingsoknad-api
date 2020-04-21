package no.nav.omsorgsdageroverforingsoknad

import com.auth0.jwk.JwkProviderBuilder
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.*
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.jwt.jwt
import io.ktor.features.*
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.jackson.jackson
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Locations
import io.ktor.metrics.micrometer.MicrometerMetrics
import io.ktor.routing.Routing
import io.ktor.util.KtorExperimentalAPI
import io.prometheus.client.hotspot.DefaultExports
import no.nav.helse.dusseldorf.ktor.auth.clients
import no.nav.helse.dusseldorf.ktor.client.HttpRequestHealthCheck
import no.nav.helse.dusseldorf.ktor.client.HttpRequestHealthConfig
import no.nav.helse.dusseldorf.ktor.client.buildURL
import no.nav.helse.dusseldorf.ktor.core.*
import no.nav.helse.dusseldorf.ktor.health.HealthReporter
import no.nav.helse.dusseldorf.ktor.health.HealthRoute
import no.nav.helse.dusseldorf.ktor.health.HealthService
import no.nav.helse.dusseldorf.ktor.jackson.JacksonStatusPages
import no.nav.helse.dusseldorf.ktor.jackson.dusseldorfConfigured
import no.nav.helse.dusseldorf.ktor.metrics.MetricsRoute
import no.nav.helse.dusseldorf.ktor.metrics.init
import no.nav.omsorgsdageroverforingsoknad.barn.BarnGateway
import no.nav.omsorgsdageroverforingsoknad.barn.BarnService
import no.nav.omsorgsdageroverforingsoknad.barn.barnApis
import no.nav.omsorgsdageroverforingsoknad.general.auth.IdTokenProvider
import no.nav.omsorgsdageroverforingsoknad.general.auth.authorizationStatusPages
import no.nav.omsorgsdageroverforingsoknad.general.systemauth.AccessTokenClientResolver
import no.nav.omsorgsdageroverforingsoknad.mellomlagring.MellomlagringService
import no.nav.omsorgsdageroverforingsoknad.mellomlagring.mellomlagringApis
import no.nav.omsorgsdageroverforingsoknad.redis.RedisConfig
import no.nav.omsorgsdageroverforingsoknad.redis.RedisConfigurationProperties
import no.nav.omsorgsdageroverforingsoknad.redis.RedisStore
import no.nav.omsorgsdageroverforingsoknad.soker.SøkerGateway
import no.nav.omsorgsdageroverforingsoknad.soker.SøkerService
import no.nav.omsorgsdageroverforingsoknad.soker.søkerApis
import no.nav.omsorgsdageroverforingsoknad.soknadOverforeDager.OmsorgsdageroverforingsøknadMottakGateway
import no.nav.omsorgsdageroverforingsoknad.soknadOverforeDager.søknadApis
import no.nav.omsorgsdageroverforingsoknad.soknadOverforeDager.SøknadOverføreDagerService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.concurrent.TimeUnit

fun main(args: Array<String>): Unit  = io.ktor.server.netty.EngineMain.main(args)

private val logger: Logger = LoggerFactory.getLogger("nav.omsorgsdageroverforingsoknadapi")

@KtorExperimentalAPI
@KtorExperimentalLocationsAPI
fun Application.omsorgsdageroverforingsoknadapi() {
    val appId = environment.config.id()
    logProxyProperties()
    DefaultExports.initialize()

    val configuration = Configuration(environment.config)
    val apiGatewayApiKey = configuration.getApiGatewayApiKey()
    val accessTokenClientResolver = AccessTokenClientResolver(environment.config.clients())

    install(ContentNegotiation) {
        jackson {
            dusseldorfConfigured()
                .setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE)
                .configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false)
        }
    }

    install(CORS) {
        method(HttpMethod.Options)
        method(HttpMethod.Get)
        method(HttpMethod.Post)
        method(HttpMethod.Delete)
        allowNonSimpleContentTypes = true
        allowCredentials = true
        log.info("Configuring CORS")
        configuration.getWhitelistedCorsAddreses().forEach {
            log.info("Adding host {} with scheme {}", it.host, it.scheme)
            host(host = it.authority, schemes = listOf(it.scheme))
        }
    }

    val idTokenProvider = IdTokenProvider(cookieName = configuration.getCookieName())
    val jwkProvider = JwkProviderBuilder(configuration.getJwksUrl().toURL())
        .cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()

    install(Authentication) {
        jwt {
            realm = appId
            verifier(jwkProvider, configuration.getIssuer()) {
                acceptNotBefore(10)
                acceptIssuedAt(10)
            }
            authHeader { call ->
                idTokenProvider
                    .getIdToken(call)
                    .medValidertLevel("Level4")
                    .somHttpAuthHeader()
            }
            validate { credentials ->
                return@validate JWTPrincipal(credentials.payload)
            }
        }
    }

    install(StatusPages) {
        DefaultStatusPages()
        JacksonStatusPages()
        authorizationStatusPages()
    }

    install(Locations)

    install(Routing) {

        val omsorgsdageroverforingsøknadMottakGateway =
            OmsorgsdageroverforingsøknadMottakGateway(
                baseUrl = configuration.getOmsorgpengesoknadMottakBaseUrl(),
                accessTokenClient = accessTokenClientResolver.accessTokenClient(),
                sendeSoknadTilProsesseringScopes = configuration.getSendSoknadTilProsesseringScopes(),
                apiGatewayApiKey = apiGatewayApiKey
            )

        val sokerGateway = SøkerGateway(
            baseUrl = configuration.getK9OppslagUrl(),
            apiGatewayApiKey = apiGatewayApiKey
        )

        val barnGateway = BarnGateway(
            baseUrl = configuration.getK9OppslagUrl(),
            apiGatewayApiKey = apiGatewayApiKey
        )

        val søkerService = SøkerService(
            søkerGateway = sokerGateway
        )

        authenticate {

            søkerApis(
                søkerService = søkerService,
                idTokenProvider = idTokenProvider
            )

            barnApis(
                barnService = BarnService(
                    barnGateway = barnGateway
                ),
                idTokenProvider = idTokenProvider
            )

            mellomlagringApis(
                mellomlagringService = MellomlagringService(
                    RedisStore(
                        RedisConfig(
                        RedisConfigurationProperties(
                            configuration.getRedisHost().equals("localhost"))
                        ).redisClient(configuration)), configuration.getStoragePassphrase()),
                idTokenProvider = idTokenProvider
            )

            søknadApis(
                idTokenProvider = idTokenProvider,
                søknadOverføreDagerService = SøknadOverføreDagerService(
                    omsorgsdageroverforingsøknadMottakGateway = omsorgsdageroverforingsøknadMottakGateway,
                    søkerService = søkerService
                )
            )
        }

        val healthService = HealthService(
            healthChecks = setOf(
                omsorgsdageroverforingsøknadMottakGateway,
                HttpRequestHealthCheck(mapOf(
                    configuration.getJwksUrl() to HttpRequestHealthConfig(expectedStatus = HttpStatusCode.OK, includeExpectedStatusEntity = false),
                    Url.buildURL(baseUrl = configuration.getK9DokumentUrl(), pathParts = listOf("health")) to HttpRequestHealthConfig(expectedStatus = HttpStatusCode.OK),
                    Url.buildURL(baseUrl = configuration.getOmsorgpengesoknadMottakBaseUrl(), pathParts = listOf("health")) to HttpRequestHealthConfig(expectedStatus = HttpStatusCode.OK, httpHeaders = mapOf(apiGatewayApiKey.headerKey to apiGatewayApiKey.value))
                ))
            )
        )

        HealthReporter(
            app = appId,
            healthService = healthService,
            frequency = Duration.ofMinutes(1)
        )

        DefaultProbeRoutes()
        MetricsRoute()
        HealthRoute(
            healthService = healthService
        )
    }

    install(MicrometerMetrics) {
        init(appId)
    }

    intercept(ApplicationCallPipeline.Monitoring) {
        call.request.log()
    }

    install(CallId) {
        generated()
    }

    install(CallLogging) {
        correlationIdAndRequestIdInMdc()
        logRequests()
        mdc("id_token_jti") { call ->
            try { idTokenProvider.getIdToken(call).getId() }
            catch (cause: Throwable) { null }
        }
    }
}