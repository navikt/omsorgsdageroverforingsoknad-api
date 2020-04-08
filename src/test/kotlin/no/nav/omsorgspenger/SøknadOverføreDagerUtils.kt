package no.nav.omsorgspenger

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.helse.dusseldorf.ktor.jackson.dusseldorfConfigured
import no.nav.omsorgspenger.soknadOverforeDager.Arbeidssituasjon

class SøknadOverføreDagerUtils {
    companion object {

        fun fullBody(arbeidssituasjon: List<Arbeidssituasjon> = listOf(Arbeidssituasjon.SELVSTENDIGNÆRINGSDRIVENDE),
                     fnrMottaker: String = "26104500284"
        ): String {
            //language=JSON
            val arbeidssituasjonSomJson = jacksonObjectMapper().dusseldorfConfigured().writerWithDefaultPrettyPrinter().writeValueAsString(arbeidssituasjon)

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

        fun fullBodyMedMedlemskap(arbeidssituasjon: List<Arbeidssituasjon> = listOf(Arbeidssituasjon.SELVSTENDIGNÆRINGSDRIVENDE),
                                  landkode: String = "DK",
                                  fnrMottaker: String = "26104500284",
                                  fnrFosterbarn: String = "30116404369"
        ): String {
            //language=JSON
            val arbeidssituasjonSomJson = jacksonObjectMapper().dusseldorfConfigured().writerWithDefaultPrettyPrinter().writeValueAsString(arbeidssituasjon)

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
