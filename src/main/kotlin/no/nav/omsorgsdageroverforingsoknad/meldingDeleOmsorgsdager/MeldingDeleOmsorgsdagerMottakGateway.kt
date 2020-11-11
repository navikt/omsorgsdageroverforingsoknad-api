package no.nav.omsorgsdageroverforingsoknad.meldingDeleOmsorgsdager

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult
import com.github.kittinunf.fuel.httpPost
import io.ktor.http.*
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

class MeldingDeleOmsorgsdagerMottakGateway(
    baseUrl: URI,
    private val accessTokenClient: AccessTokenClient,
    private val sendeSoknadTilProsesseringScopes: Set<String>,
    private val apiGatewayApiKey: ApiGatewayApiKey
) : HealthCheck {

    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(MeldingDeleOmsorgsdagerMottakGateway::class.java)
        private val objectMapper = jacksonObjectMapper().dusseldorfConfigured()
    }

    private val komplettUrlDeleDager = Url.buildURL(
    baseUrl = baseUrl,
    pathParts = listOf("v1", DELE_DAGER_MOTTAK_URL)
    ).toString()

    private val cachedAccessTokenClient = CachedAccessTokenClient(accessTokenClient)

    override suspend fun check(): Result {
        return try {
            accessTokenClient.getAccessToken(sendeSoknadTilProsesseringScopes)
            Healthy("MeldingDeleOmsorgsdagerMottakGateway", "Henting av access token for å legge melding til prosessering OK.")
        } catch (cause: Throwable) {
            logger.error("Feil ved henting av access token for å legge søknad til prosessering", cause)
            UnHealthy("MeldingDeleOmsorgsdagerMottakGateway", "Henting av access token for å legge melding til prosessering.")
        }
    }

    suspend fun leggTilProsesseringDeleOmsorgsdager(
        soknad: KomplettMeldingDeleOmsorgsdager,
        callId: CallId
    ) {
        val authorizationHeader =
            cachedAccessTokenClient.getAccessToken(sendeSoknadTilProsesseringScopes).asAuthoriationHeader()

        val body = objectMapper.writeValueAsBytes(soknad)
        val contentStream = { ByteArrayInputStream(body) }

        val httpRequet = komplettUrlDeleDager
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
            operation = "sende-melding-deling-omsorgsdager-til-prosessering",
            resultResolver = { 202 == it.second.statusCode }
        ) { httpRequet.awaitStringResponseResult() }

        result.fold(
            { },
            { error ->
                logger.error("Error response = '${error.response.body().asString("text/plain")}' fra '${request.url}'")
                logger.error(error.toString())
                throw IllegalStateException("Feil ved sending av melding om deling av omsorgsdager til prosessering.")
            }
        )
    }



}