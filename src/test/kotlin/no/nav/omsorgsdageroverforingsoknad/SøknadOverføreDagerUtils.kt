package no.nav.omsorgsdageroverforingsoknad

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.helse.dusseldorf.ktor.jackson.dusseldorfConfigured
import no.nav.omsorgsdageroverforingsoknad.soknadOverforeDager.*
import java.time.LocalDate

class SøknadOverføreDagerUtils {
    companion object {
        internal val objectMapper = jacksonObjectMapper().dusseldorfConfigured()
            .setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE)
            .configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false)

        val søknadOverføreDager = SøknadOverføreDager(
            språk = "nb",
            antallDager = 5,
            fnrMottaker = "26104500284",
            navnMottaker = "Navn Navnesen",
            medlemskap = Medlemskap(
                harBoddIUtlandetSiste12Mnd = false,
                skalBoIUtlandetNeste12Mnd = true,
                utenlandsoppholdNeste12Mnd = listOf(
                    Utenlandsopphold(
                        fraOgMed = LocalDate.now().minusDays(5),
                        tilOgMed = LocalDate.now(),
                        landkode = "NO",
                        landnavn = "Norge"
                    )
                )
            ),
            harForståttRettigheterOgPlikter = true,
            harBekreftetOpplysninger = true,
            arbeidssituasjon = listOf(
                Arbeidssituasjon.ARBEIDSTAKER
            ),
            stengingsperiode = Stengingsperiode.ETTER_AUGUST_9
        )


        fun fullBody(
            arbeidssituasjon: List<Arbeidssituasjon> = listOf(Arbeidssituasjon.SELVSTENDIGNÆRINGSDRIVENDE),
            fnrMottaker: String = "26104500284"
        ): String {
            //language=JSON
            val arbeidssituasjonSomJson = jacksonObjectMapper().dusseldorfConfigured().writerWithDefaultPrettyPrinter()
                .writeValueAsString(arbeidssituasjon)

            return """
                {
                  "språk": "nb",
                  "arbeidssituasjon": $arbeidssituasjonSomJson,
                  "medlemskap": {
                    "harBoddIUtlandetSiste12Mnd": false,
                    "utenlandsoppholdSiste12Mnd": [],
                    "skalBoIUtlandetNeste12Mnd": false,
                    "utenlandsoppholdNeste12Mnd": []
                  },
                  "harForståttRettigheterOgPlikter": true,
                  "harBekreftetOpplysninger": true,
                  "antallDager": 5,
                  "fnrMottaker": "$fnrMottaker",
                  "navnMottaker": "Navn Navnesen",
                  "fosterbarn": [
                    {
                      "fornavn": "fornavnsen",
                      "etternavn": "etternavnsen",
                      "fødselsnummer": "30116404369"
                    }
                  ]
                }
            """.trimIndent()
        }

        fun fullBodyMedMedlemskap(
            arbeidssituasjon: List<Arbeidssituasjon> = listOf(Arbeidssituasjon.SELVSTENDIGNÆRINGSDRIVENDE),
            landkode: String = "DK",
            fnrMottaker: String = "26104500284",
            navnMottaker: String = "Navn navnesen",
            fnrFosterbarn: String = "30116404369"
        ): String {
            //language=JSON
            val arbeidssituasjonSomJson = jacksonObjectMapper().dusseldorfConfigured().writerWithDefaultPrettyPrinter()
                .writeValueAsString(arbeidssituasjon)

            return """
                {
                  "språk": "nb",
                  "arbeidssituasjon": $arbeidssituasjonSomJson,
                  "medlemskap": {
                    "harBoddIUtlandetSiste12Mnd": true,
                    "utenlandsoppholdSiste12Mnd": [
                      {
                        "fraOgMed": "2020-01-31",
                        "tilOgMed": "2020-02-31",
                        "landkode": "$landkode",
                        "landnavn": "Danmark"
                      }
                    ],
                    "skalBoIUtlandetNeste12Mnd": true,
                    "utenlandsoppholdNeste12Mnd": [
                      {
                        "fraOgMed": "2020-01-31",
                        "tilOgMed": "2020-02-31",
                        "landkode": "DK",
                        "landnavn": "Danmark"
                      }
                    ]
                  },
                  "harForståttRettigheterOgPlikter": true,
                  "harBekreftetOpplysninger": true,
                  "antallDager": 5,
                  "fnrMottaker": "$fnrMottaker",
                  "navnMottaker": "$navnMottaker",
                  "fosterbarn": [
                    {
                      "fornavn": "fornavnsen",
                      "etternavn": "etternavnsen",
                      "fødselsnummer": "$fnrFosterbarn"
                    }
                  ]
                }
            """.trimIndent()
        }
    }
}

internal fun SøknadOverføreDager.somJson() = SøknadOverføreDagerUtils.objectMapper.writeValueAsString(this)