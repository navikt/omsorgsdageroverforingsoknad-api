package no.nav.omsorgsdageroverforingsoknad.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.matching.AnythingPattern
import io.ktor.http.*
import no.nav.helse.dusseldorf.testsupport.wiremock.WireMockBuilder

internal const val k9OppslagPath = "/helse-reverse-proxy/k9-selvbetjening-oppslag-mock"
private const val omsorgsdageroverforingsoknadMottakPath = "/helse-reverse-proxy/omsorgsdagveroverforingsoknad-mottak-mock"
private const val k9DokumentPath = "/k9-dokument-mock"

internal fun WireMockBuilder.omsorgsdageroverforingsoknadApiConfig() = wireMockConfiguration {
    it
        .extensions(SokerResponseTransformer())
        .extensions(BarnResponseTransformer())
}


internal fun WireMockServer.stubK9OppslagSoker() : WireMockServer {
    WireMock.stubFor(
        WireMock.get(WireMock.urlPathMatching("$k9OppslagPath/.*"))
            .withHeader("x-nav-apiKey", AnythingPattern())
            .withHeader(HttpHeaders.Authorization, AnythingPattern())
            .withQueryParam("a", equalTo("aktør_id"))
            .withQueryParam("a", equalTo("fornavn"))
            .withQueryParam("a", equalTo("mellomnavn"))
            .withQueryParam("a", equalTo("etternavn"))
            .withQueryParam("a", equalTo("fødselsdato"))
            .willReturn(
                WireMock.aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(200)
                    .withTransformers("k9-oppslag-soker")
            )
    )
    return this
}

internal fun WireMockServer.stubK9OppslagBarn(simulerFeil: Boolean = false) : WireMockServer {
    WireMock.stubFor(
        WireMock.get(WireMock.urlPathMatching("$k9OppslagPath/.*"))
            .withHeader("x-nav-apiKey", AnythingPattern())
            .withHeader(HttpHeaders.Authorization, AnythingPattern())
            .withQueryParam("a", equalTo("barn[].aktør_id"))
            .withQueryParam("a", equalTo("barn[].fornavn"))
            .withQueryParam("a", equalTo("barn[].mellomnavn"))
            .withQueryParam("a", equalTo("barn[].etternavn"))
            .withQueryParam("a", equalTo("barn[].fødselsdato"))
            .withQueryParam("a", equalTo("barn[].identitetsnummer"))
            .willReturn(
                WireMock.aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(if (simulerFeil) 500 else 200)
                    .withTransformers("k9-oppslag-barn")
            )
    )
    return this
}

private fun WireMockServer.stubHealthEndpoint(
    path : String
) : WireMockServer{
    WireMock.stubFor(
        WireMock.get(WireMock.urlPathMatching(".*$path")).willReturn(
            WireMock.aResponse()
                .withStatus(200)
        )
    )
    return this
}

private fun WireMockServer.stubHealthEndpointThroughZones(
    path : String
) : WireMockServer{
    WireMock.stubFor(
        WireMock.get(WireMock.urlPathMatching(".*$path"))
            .withHeader("x-nav-apiKey", AnythingPattern())
            .willReturn(
            WireMock.aResponse()
                .withStatus(200)
        )
    )
    return this
}

internal fun WireMockServer.stubK9DokumentHealth() = stubHealthEndpoint("$k9DokumentPath/health")
internal fun WireMockServer.stubOmsorgsoknadMottakHealth() = stubHealthEndpointThroughZones("$omsorgsdageroverforingsoknadMottakPath/health")
internal fun WireMockServer.stubOppslagHealth() = stubHealthEndpointThroughZones("$k9OppslagPath/health")

internal fun WireMockServer.stubLeggSoknadTilProsessering(path: String) : WireMockServer{
    WireMock.stubFor(
        WireMock.post(WireMock.urlMatching(".*$omsorgsdageroverforingsoknadMottakPath/$path"))
            .withHeader("x-nav-apiKey", AnythingPattern())
            .willReturn(
                WireMock.aResponse()
                    .withStatus(202)
            )
    )
    return this
}

internal fun WireMockServer.stubK9Dokument() : WireMockServer{
    WireMock.stubFor(
        WireMock.any(WireMock.urlMatching(".*$k9DokumentPath/v1/dokument.*"))
            .willReturn(
                WireMock.aResponse()
                    .withTransformers("K9DokumentResponseTransformer")
            )
    )
    return this
}

internal fun WireMockServer.getK9OppslagUrl() = baseUrl() + k9OppslagPath
internal fun WireMockServer.getOmsorgsdageroverforingsoknadMottakUrl() = baseUrl() + omsorgsdageroverforingsoknadMottakPath
internal fun WireMockServer.getK9DokumentUrl() = baseUrl() + k9DokumentPath