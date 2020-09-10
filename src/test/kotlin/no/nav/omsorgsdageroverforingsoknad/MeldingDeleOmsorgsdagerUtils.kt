package no.nav.omsorgsdageroverforingsoknad

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.helse.dusseldorf.ktor.jackson.dusseldorfConfigured
import no.nav.omsorgsdageroverforingsoknad.barn.Barn
import no.nav.omsorgsdageroverforingsoknad.meldingDeleOmsorgsdager.AndreBarn
import no.nav.omsorgsdageroverforingsoknad.meldingDeleOmsorgsdager.MeldingDeleOmsorgsdager
import no.nav.omsorgsdageroverforingsoknad.meldingDeleOmsorgsdager.Mottaker
import no.nav.omsorgsdageroverforingsoknad.soknadOverforeDager.*
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
            andreBarn = listOf(
                AndreBarn(
                    fnr = gyldigfnr,
                    fødselsdato = LocalDate.parse("2020-01-01"),
                    navn = "Barn Barnesen"
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
            antallDagerBruktEtter1Juli = 10,
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
                  "andreBarn": [
                    {
                      "fnr": "07068920285",
                      "fødselsdato": "2020-01-01",
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