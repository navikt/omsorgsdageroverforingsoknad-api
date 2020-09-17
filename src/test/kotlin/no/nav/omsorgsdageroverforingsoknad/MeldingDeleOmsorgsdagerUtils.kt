package no.nav.omsorgsdageroverforingsoknad

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.helse.dusseldorf.ktor.jackson.dusseldorfConfigured
import no.nav.omsorgsdageroverforingsoknad.meldingDeleOmsorgsdager.BarnUtvidet
import no.nav.omsorgsdageroverforingsoknad.meldingDeleOmsorgsdager.MeldingDeleOmsorgsdager
import no.nav.omsorgsdageroverforingsoknad.meldingDeleOmsorgsdager.Mottaker
import no.nav.omsorgsdageroverforingsoknad.soknadOverforeDager.Arbeidssituasjon
import java.time.LocalDate

class MeldingDeleOmsorgsdagerUtils {
    companion object {
        internal val objectMapper = jacksonObjectMapper().dusseldorfConfigured()
            .setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE)
            .configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false)

        val gyldigfnr = "07068920285"

        val meldingDeleOmsorgsdager = MeldingDeleOmsorgsdager(
            språk = "nb",
            harForståttRettigheterOgPlikter = true,
            harBekreftetOpplysninger = true,
            barn = listOf(
                BarnUtvidet(
                    identitetsnummer = "07068920285",
                    aktørId = "1000000000001",
                    navn = "Barn Barnesen",
                    fødselsdato = LocalDate.parse("2020-01-01"),
                    aleneOmOmsorgen = true,
                    utvidetRett = false
                )
            ),
            borINorge = true,
            arbeiderINorge = true,
            arbeidssituasjon = listOf(
                Arbeidssituasjon.ARBEIDSTAKER
            ),
            antallDagerBruktIÅr = 10,
            mottakerType = Mottaker.EKTEFELLE,
            mottakerFnr = gyldigfnr,
            mottakerNavn = "Navn Mottaker",
            antallDagerSomSkalOverføres = 5
        )


        fun fullBody(): String {
            //language=json
            return """
            {
              "språk": "nb",
              "harForståttRettigheterOgPlikter": true,
              "harBekreftetOpplysninger": true,
              "barn": [
                {
                  "identitetsnummer": null,
                  "aktørId": "1000000000001",
                  "navn": "Barn Barnesen",
                  "fødselsdato": "2020-01-01",
                  "aleneOmOmsorgen": true,
                  "utvidetRett": true
                }
              ],
              "borINorge": true,
              "arbeidINorge": true,
              "arbeidssituasjon": [
                "arbeidstaker"
              ],
              "antallDagerBruktEtter1Juli": 10,
              "mottakerType": "ektefelle",
              "mottakerFnr": "07068920285",
              "mottakerNavn": "Navn Mottaker",
              "antallDagerSomSkalOverføres": 5
            }
            """.trimIndent()
        }

    }
}

internal fun MeldingDeleOmsorgsdager.somJson() = MeldingDeleOmsorgsdagerUtils.objectMapper.writeValueAsString(this)