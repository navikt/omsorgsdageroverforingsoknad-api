package no.nav.omsorgspenger

import com.github.kittinunf.fuel.httpGet
import com.github.tomakehurst.wiremock.WireMockServer
import no.nav.helse.dusseldorf.testsupport.jws.ClientCredentials
import no.nav.helse.dusseldorf.testsupport.wiremock.getAzureV2WellKnownUrl
import no.nav.helse.dusseldorf.testsupport.wiremock.getLoginServiceV1WellKnownUrl
import no.nav.omsorgspenger.wiremock.getK9DokumentUrl
import no.nav.omsorgspenger.wiremock.getK9OppslagUrl
import no.nav.omsorgspenger.wiremock.getOmsorgpengesoknadMottakUrl
import org.json.JSONObject

object TestConfiguration {

    fun asMap(
        wireMockServer: WireMockServer? = null,
        port : Int = 8080,
        k9OppslagUrl: String? = wireMockServer?.getK9OppslagUrl(),
        omsorgpengesoknadMottakUrl : String? = wireMockServer?.getOmsorgpengesoknadMottakUrl(),
        k9DokumentUrl : String? = wireMockServer?.getK9DokumentUrl(),
        corsAdresses : String = "http://localhost:8080"
    ) : Map<String, String> {

        val loginServiceWellKnownJson = wireMockServer?.getLoginServiceV1WellKnownUrl()?.getAsJson()

        val map = mutableMapOf(
            Pair("ktor.deployment.port","$port"),
            Pair("nav.authorization.issuer", "${loginServiceWellKnownJson?.getString("issuer")}"),
            Pair("nav.authorization.cookie_name", "localhost-idtoken"),
            Pair("nav.authorization.jwks_uri","${loginServiceWellKnownJson?.getString("jwks_uri")}"),
            Pair("nav.gateways.k9_oppslag_url","$k9OppslagUrl"),
            Pair("nav.gateways.omsorgpengesoknad_mottak_base_url", "$omsorgpengesoknadMottakUrl"),
            Pair("nav.gateways.k9_dokument_url", "$k9DokumentUrl"),
            Pair("nav.cors.addresses", corsAdresses),
            Pair("nav.authorization.api_gateway.api_key", "verysecret")
        )

        // Clients
        if (wireMockServer != null) {
            map["nav.auth.clients.0.alias"] = "azure-v2"
            map["nav.auth.clients.0.client_id"] = "omsorgspengesoknad-api"
            map["nav.auth.clients.0.private_key_jwk"] = ClientCredentials.ClientC.privateKeyJwk
            map["nav.auth.clients.0.certificate_hex_thumbprint"] = "The keyId of Azure JWK"
            map["nav.auth.clients.0.discovery_endpoint"] = wireMockServer.getAzureV2WellKnownUrl()
            map["nav.auth.scopes.sende-soknad-til-prosessering"] = "omsorgspengesoknad-mottak/.default"
        }

        map["nav.redis.host"] = "localhost"
        map["nav.redis.port"] = "6379"
        map["nav.storage.passphrase"] = "verySecret"

        return map.toMap()
    }

    private fun String.getAsJson() = JSONObject(this.httpGet().responseString().third.component1())
}