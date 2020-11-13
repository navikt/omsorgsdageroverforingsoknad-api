package no.nav.omsorgsdageroverforingsoknad

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

        val gyldigfnr = "07068920285"

        val meldingDeleOmsorgsdager = MeldingDeleOmsorgsdager(
            språk = "nb",
            id = "1",
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
    }
}

internal fun MeldingDeleOmsorgsdager.somJson() = MeldingDeleOmsorgsdagerUtils.objectMapper.writeValueAsString(this)