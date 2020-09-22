package no.nav.omsorgsdageroverforingsoknad

import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.omsorgsdageroverforingsoknad.MeldingDeleOmsorgsdagerUtils.Companion.meldingDeleOmsorgsdager
import no.nav.omsorgsdageroverforingsoknad.meldingDeleOmsorgsdager.BarnUtvidet
import no.nav.omsorgsdageroverforingsoknad.meldingDeleOmsorgsdager.valider
import org.junit.Test
import java.time.LocalDate

internal class MeldingDeleOmsorgsdagerValidatorTest {

    companion object {
        private val dNummerA = "55125314561"
    }

    @Test
    fun `Skal ikke feile på gyldig melding`(){
        val melding = meldingDeleOmsorgsdager
        melding.valider()
    }

    @Test
    fun `Skal ikke feile på gyldig melding med dnummer`(){
        val melding = meldingDeleOmsorgsdager.copy(
            mottakerFnr = dNummerA
        )
        melding.valider()
    }

    @Test(expected = Throwblem::class)
    fun `Skal feile dersom harBekreftetOpplysninger er false`(){
        val melding = meldingDeleOmsorgsdager.copy(
            harBekreftetOpplysninger = false
        )
        melding.valider()
    }

    @Test(expected = Throwblem::class)
    fun `Skal feile dersom harForståttRettigheterOgPlikter er false`(){
        val melding = meldingDeleOmsorgsdager.copy(
            harForståttRettigheterOgPlikter = false
        )
        melding.valider()
    }

    @Test(expected = Throwblem::class)
    fun `Skal feile dersom mottakerFnr ikke er gyldig`(){
        val melding = meldingDeleOmsorgsdager.copy(
            mottakerFnr = "ugyldig"
        )
        melding.valider()
    }

    @Test(expected = Throwblem::class)
    fun `Skal feile dersom antallDagerSomSkalOverføres er 0`(){
        val melding = meldingDeleOmsorgsdager.copy(
            antallDagerSomSkalOverføres = 0
        )
        melding.valider()
    }

    @Test(expected = Throwblem::class)
    fun `Skal feile dersom antallDagerBruktIÅr er negativt`(){
        val melding = meldingDeleOmsorgsdager.copy(
            antallDagerBruktIÅr = -1
        )
        melding.valider()
    }

    @Test(expected = Throwblem::class)
    fun `Skal feile dersom mottakerNavn er ugyldig`(){
        val melding = meldingDeleOmsorgsdager.copy(
            mottakerNavn = "    "
        )
        melding.valider()
    }

    @Test(expected = Throwblem::class)
    fun `Skal feile dersom barn har ugyldig identitetsnummer`(){
        val melding = meldingDeleOmsorgsdager.copy(
            barn = listOf(
                BarnUtvidet(
                    identitetsnummer = "ikke gyldig",
                    aktørId = "1000000000001",
                    navn = "Barn Barnesen",
                    fødselsdato = LocalDate.parse("2020-01-01"),
                    aleneOmOmsorgen = true,
                    utvidetRett = false
                )
            )
        )
        melding.valider()
    }

}