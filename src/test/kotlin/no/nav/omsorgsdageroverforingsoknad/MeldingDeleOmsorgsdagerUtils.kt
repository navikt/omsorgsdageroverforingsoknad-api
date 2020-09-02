package no.nav.omsorgsdageroverforingsoknad

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.helse.dusseldorf.ktor.jackson.dusseldorfConfigured
import no.nav.omsorgsdageroverforingsoknad.barn.Barn
import no.nav.omsorgsdageroverforingsoknad.meldingDeleOmsorgsdager.AndreBarn
import no.nav.omsorgsdageroverforingsoknad.meldingDeleOmsorgsdager.MeldingDeleOmsorgsdager
import no.nav.omsorgsdageroverforingsoknad.meldingDeleOmsorgsdager.OverføreTilType
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
            harBekreftetOpplysninger = true,
            andreBarn = listOf(
                AndreBarn(
                    fnr = "12345678900",
                    navn = "Barn Barnesen",
                    ingenFnr = false
                )
            ),
            harAleneomsorg = true,
            harAleneomsorgFor = listOf(
              Barn(
                  fødselsdato = LocalDate.parse("2010-01-01"),
                  aktørId = "12345",
                  fornavn = "Fornavn",
                  etternavn = "Etternavn",
                  mellomnavn = "Mellomnavn"
              )
            ),
            harUtvidetRett = true,
            harUtvidetRettFor = listOf(
                Barn(
                    fødselsdato = LocalDate.parse("2010-01-01"),
                    aktørId = "12345",
                    fornavn = "Fornavn",
                    etternavn = "Etternavn",
                    mellomnavn = "Mellomnavn"
                )
            ),
            borINorge = true,
            arbeidINorge = true,
            arbeidssituasjon = listOf(
                Arbeidssituasjon.ARBEIDSTAKER
            ),
            antallDagerHarBruktEtter1Juli = 10,
            harDeltDagerMedAndreTidligere = true,
            antallDagerHarDeltMedAndre = 10,
            overføreTilType = OverføreTilType.NY_EKTEFELLE,
            fnrMottaker = "12345678911",
            navnMottaker = "Navn Mottaker",
            antallDagerTilOverføre = 5,
            harBekreftetMottakerOpplysninger = true
        )


        fun fullBody(): String {
            //language=json
            return """
                {
                  "språk": "nb",
                  "harForståttRettigheterOgPlikter": true,
                  "harBekreftetOpplysninger": true,
                  "andreBarn": [
                    {
                      "fnr": "12345678900",
                      "ingenFnr": false,
                      "navn": "Barn Barnesen"
                    }
                  ],
                  "harAleneomsorg": true,
                  "harAleneomsorgFor": [
                    {
                      "fødselsdato": "2010-01-01",
                      "fornavn": "Fornavn",
                      "mellomnavn": "Mellomnavn",
                      "etternavn": "Etternavn",
                      "aktørId": "12345"
                    }
                  ],
                  "harUtvidetRett": true,
                  "harUtvidetRettFor": [
                    {
                      "fødselsdato": "2010-01-01",
                      "fornavn": "Fornavn",
                      "mellomnavn": "Mellomnavn",
                      "etternavn": "Etternavn",
                      "aktørId": "12345"
                    }
                  ],
                  "borINorge": true,
                  "arbeidINorge": true,
                  "arbeidssituasjon": [
                    "arbeidstaker"
                  ],
                  "antallDagerHarBruktEtter1Juli": 10,
                  "harDeltDagerMedAndreTidligere": true,
                  "antallDagerHarDeltMedAndre": 10,
                  "overføreTilType": "nyEktefelle",
                  "fnrMottaker": "12345678911",
                  "navnMottaker": "Navn Mottaker",
                  "antallDagerTilOverføre": 5,
                  "harBekreftetMottakerOpplysninger": true
                }
            """.trimIndent()
        }

    }
}

internal fun MeldingDeleOmsorgsdager.somJson() = MeldingDeleOmsorgsdagerUtils.objectMapper.writeValueAsString(this)