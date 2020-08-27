package no.nav.omsorgsdageroverforingsoknad

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.helse.dusseldorf.ktor.jackson.dusseldorfConfigured
import no.nav.omsorgsdageroverforingsoknad.meldingDeleOmsorgsdager.MeldingDeleOmsorgsdager
import no.nav.omsorgsdageroverforingsoknad.soknadOverforeDager.*
import java.time.LocalDate

class MeldingDeleOmsorgsdagerUtils {
    companion object {
        internal val objectMapper = jacksonObjectMapper().dusseldorfConfigured()
            .setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE)
            .configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false)

        val meldingDeleOmsorgsdager = MeldingDeleOmsorgsdager(
            språk = "nb",
            harForståttRettigheterOgPlikter = true,
            harBekreftetOpplysninger = true
        )


        fun fullBody(): String {
            return """
                {
                  "språk": "nb",
                  "harForståttRettigheterOgPlikter": true,
                  "harBekreftetOpplysninger": true
                }
            """.trimIndent()
        }

    }
}

internal fun MeldingDeleOmsorgsdager.somJson() = MeldingDeleOmsorgsdagerUtils.objectMapper.writeValueAsString(this)