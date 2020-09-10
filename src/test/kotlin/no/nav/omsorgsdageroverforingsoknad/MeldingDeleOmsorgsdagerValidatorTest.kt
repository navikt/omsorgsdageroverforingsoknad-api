package no.nav.omsorgsdageroverforingsoknad

import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.omsorgsdageroverforingsoknad.MeldingDeleOmsorgsdagerUtils.Companion.meldingDeleOmsorgsdager
import no.nav.omsorgsdageroverforingsoknad.meldingDeleOmsorgsdager.AndreBarn
import no.nav.omsorgsdageroverforingsoknad.meldingDeleOmsorgsdager.valider
import org.junit.Test
import java.time.LocalDate

internal class MeldingDeleOmsorgsdagerValidatorTest {

    companion object {
        private val gyldigFodselsnummerA = "26104500284"
        private val dNummerA = "55125314561"
    }

    @Test
    fun `Skal ikke feile på gyldig søknad`(){
        val melding = meldingDeleOmsorgsdager
        melding.valider()
    }

    @Test
    fun `Skal ikke feile på gyldig søknad med dnummer`(){
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
    fun `Skal feile dersom harAleneomsorg er true mens harAleneomsorgFor er tom`(){
        val melding = meldingDeleOmsorgsdager.copy(
            harAleneomsorg = true,
            harAleneomsorgFor = listOf()
        )
        melding.valider()
    }

    @Test(expected = Throwblem::class)
    fun `Skal feile dersom harUtvidetRett er true mens harUtvidetRettFor er tom`(){
        val melding = meldingDeleOmsorgsdager.copy(
            harUtvidetRett = true,
            harUtvidetRettFor= listOf()
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
    fun `Skal feile dersom antallDagerBruktEtter1Juli er negativt`(){
        val melding = meldingDeleOmsorgsdager.copy(
            antallDagerBruktEtter1Juli = -1
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
    fun `Skal feile dersom andreBarn har ugyldig fnr`(){
        val melding = meldingDeleOmsorgsdager.copy(
            andreBarn = listOf(
                AndreBarn(
                    fnr = "ikke gyldig",
                    navn = "Barn",
                    fødselsdato = LocalDate.parse("2020-01-01")
                )
            )
        )
        melding.valider()
    }

}