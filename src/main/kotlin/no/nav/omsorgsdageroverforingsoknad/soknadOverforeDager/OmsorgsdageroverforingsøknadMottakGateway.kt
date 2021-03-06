package no.nav.omsorgsdageroverforingsoknad.soknadOverforeDager

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult
import com.github.kittinunf.fuel.httpPost
import io.ktor.http.HttpHeaders
import io.ktor.http.Url
import no.nav.helse.dusseldorf.ktor.client.buildURL
import no.nav.helse.dusseldorf.ktor.health.HealthCheck
import no.nav.helse.dusseldorf.ktor.health.Healthy
import no.nav.helse.dusseldorf.ktor.health.Result
import no.nav.helse.dusseldorf.ktor.health.UnHealthy
import no.nav.helse.dusseldorf.ktor.jackson.dusseldorfConfigured
import no.nav.helse.dusseldorf.ktor.metrics.Operation
import no.nav.helse.dusseldorf.oauth2.client.AccessTokenClient
import no.nav.helse.dusseldorf.oauth2.client.CachedAccessTokenClient
import no.nav.omsorgsdageroverforingsoknad.general.CallId
import no.nav.omsorgsdageroverforingsoknad.general.auth.ApiGatewayApiKey
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.net.URI

class OmsorgsdageroverforingsøknadMottakGateway(
    baseUrl: URI,
    private val accessTokenClient: AccessTokenClient,
    private val sendeSoknadTilProsesseringScopes: Set<String>,
    private val apiGatewayApiKey: ApiGatewayApiKey
) : HealthCheck {

    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(OmsorgsdageroverforingsøknadMottakGateway::class.java)
        private val objectMapper = jacksonObjectMapper().dusseldorfConfigured()
            .setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE)
            .configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false)
    }

    private val komplettUrlOverforeDager = Url.buildURL(
    baseUrl = baseUrl,
    pathParts = listOf("v1", "soknad/overfore-dager")
    ).toString()

    private val cachedAccessTokenClient = CachedAccessTokenClient(accessTokenClient)

    override suspend fun check(): Result {
        return try {
            accessTokenClient.getAccessToken(sendeSoknadTilProsesseringScopes)
            Healthy("OmsorgsdageroverforingsoknadMottakGateway", "Henting av access token for å legge søknad til prosessering OK.")
        } catch (cause: Throwable) {
            logger.error("Feil ved henting av access token for å legge søknad til prosessering", cause)
            UnHealthy("OmsorgsdageroverforingsoknadMottakGateway", "Henting av access token for å legge søknad til prosessering.")
        }
    }

    suspend fun leggTilProsesseringOverføreDager(
        soknad: KomplettSøknadOverføreDager,
        callId: CallId
    ) {
        val authorizationHeader =
            cachedAccessTokenClient.getAccessToken(sendeSoknadTilProsesseringScopes).asAuthoriationHeader()

        val body = objectMapper.writeValueAsBytes(soknad)
        val contentStream = { ByteArrayInputStream(body) }

        val httpRequet = komplettUrlOverforeDager
            .httpPost()
            .timeout(20_000)
            .timeoutRead(20_000)
            .body(contentStream)
            .header(
                HttpHeaders.ContentType to "application/json",
                HttpHeaders.XCorrelationId to callId.value,
                HttpHeaders.Authorization to authorizationHeader,
                apiGatewayApiKey.headerKey to apiGatewayApiKey.value
            )

        val (request, _, result) = Operation.monitored(
            app = "omsorgsdageroverforingsoknad-api",
            operation = "sende-overfore-soknad-til-prosessering",
            resultResolver = { 202 == it.second.statusCode }
        ) { httpRequet.awaitStringResponseResult() }

        result.fold(
            { },
            { error ->
                logger.error("Error response = '${error.response.body().asString("text/plain")}' fra '${request.url}'")
                logger.error(error.toString())
                throw IllegalStateException("Feil ved sending av søknad om overføring av dager til prosessering.")
            }
        )
    }



}