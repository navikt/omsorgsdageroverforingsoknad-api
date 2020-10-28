package no.nav.omsorgsdageroverforingsoknad

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import io.ktor.config.*
import io.ktor.util.*
import io.ktor.config.*
import io.ktor.util.*
import no.nav.helse.dusseldorf.ktor.auth.EnforceEqualsOrContains
import no.nav.helse.dusseldorf.ktor.auth.issuers
import no.nav.helse.dusseldorf.ktor.auth.withAdditionalClaimRules
import no.nav.helse.dusseldorf.ktor.core.getOptionalList
import no.nav.helse.dusseldorf.ktor.core.getOptionalString
import no.nav.helse.dusseldorf.ktor.core.getRequiredList
import no.nav.helse.dusseldorf.ktor.core.getRequiredString
import no.nav.omsorgsdageroverforingsoknad.general.auth.ApiGatewayApiKey
import java.net.URI
import java.time.Duration

@KtorExperimentalAPI
data class Configuration(val config : ApplicationConfig) {

    private val loginServiceClaimRules = setOf(
        EnforceEqualsOrContains("acr", "Level4")
    )

    internal fun issuers() = config.issuers().withAdditionalClaimRules(mapOf(
        "login-service-v1" to loginServiceClaimRules,
        "login-service-v2" to loginServiceClaimRules
    ))

    internal fun getCookieName() : String {
        return config.getRequiredString("nav.authorization.cookie_name", secret = false)
    }

    internal fun getWhitelistedCorsAddreses(): List<URI> {
        return config.getOptionalList(
            key = "nav.cors.addresses",
            builder = { value ->
                URI.create(value)
            },
            secret = false
        )
    }

    internal fun getK9OppslagUrl() = URI(config.getRequiredString("nav.gateways.k9_oppslag_url", secret = false))

    internal fun getK9DokumentUrl() = URI(config.getRequiredString("nav.gateways.k9_dokument_url", secret = false))

    internal fun getOmsorgsdageroverforingsoknadMottakBaseUrl() = URI(config.getRequiredString("nav.gateways.omsorgsdager_overforing_soknad_mottak_base_url", secret = false))

    internal fun getApiGatewayApiKey(): ApiGatewayApiKey {
        val apiKey = config.getRequiredString(key = "nav.authorization.api_gateway.api_key", secret = true)
        return ApiGatewayApiKey(value = apiKey)
    }

    private fun getScopesFor(operation: String) = config.getRequiredList("nav.auth.scopes.$operation", secret = false, builder = { it }).toSet()
    internal fun getSendSoknadTilProsesseringScopes() = getScopesFor("sende-soknad-til-prosessering")
    internal fun getRedisPort() = config.getOptionalString("nav.redis.port", secret = false)
    internal fun getRedisHost() = config.getOptionalString("nav.redis.host", secret = false)

    internal fun getStoragePassphrase(): String {
        return config.getRequiredString("nav.storage.passphrase", secret = true)
    }

    internal fun<K, V>cache(
        expiry: Duration = Duration.ofMinutes(config.getRequiredString("nav.cache.expiry_in_minutes", secret = false).toLong())
    ) : Cache<K, V> {
        val maxSize = config.getRequiredString("nav.cache.max_size", secret = false).toLong()
        return Caffeine.newBuilder()
            .expireAfterWrite(expiry)
            .maximumSize(maxSize)
            .build()
    }
}