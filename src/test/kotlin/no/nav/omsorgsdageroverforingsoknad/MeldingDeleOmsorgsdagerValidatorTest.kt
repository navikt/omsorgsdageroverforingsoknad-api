package no.nav.omsorgsdageroverforingsoknad

import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.omsorgsdageroverforingsoknad.MeldingDeleOmsorgsdagerUtils.Companion.meldingDeleOmsorgsdager
import no.nav.omsorgsdageroverforingsoknad.barn.Barn
import no.nav.omsorgsdageroverforingsoknad.meldingDeleOmsorgsdager.AndreBarn
import no.nav.omsorgsdageroverforingsoknad.meldingDeleOmsorgsdager.MeldingDeleOmsorgsdager
import no.nav.omsorgsdageroverforingsoknad.meldingDeleOmsorgsdager.OverføreTilType
import no.nav.omsorgsdageroverforingsoknad.meldingDeleOmsorgsdager.valider
import no.nav.omsorgsdageroverforingsoknad.soknadOverforeDager.Arbeidssituasjon
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
            fnrMottaker = dNummerA
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
    fun `Skal feile dersom harBekreftetMottakerOpplysninger er false`(){
        val melding = meldingDeleOmsorgsdager.copy(
            harBekreftetMottakerOpplysninger = false
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
    fun `Skal feile dersom fnrMottaker ikke er gyldig`(){
        val melding = meldingDeleOmsorgsdager.copy(
            fnrMottaker = "ugyldig"
        )
        melding.valider()
    }

    @Test(expected = Throwblem::class)
    fun `Skal feile dersom antallDagerHarDeltMedAndre er ugyldig og harDeltDagerMedAndre er true`(){
        val melding = meldingDeleOmsorgsdager.copy(
            harDeltDagerMedAndreTidligere = true,
            antallDagerHarDeltMedAndre = 0
        )
        melding.valider()
    }

    @Test(expected = Throwblem::class)
    fun `Skal feile dersom antallDagerTilOverføre er 0`(){
        val melding = meldingDeleOmsorgsdager.copy(
            antallDagerTilOverføre = 0
        )
        melding.valider()
    }

    @Test(expected = Throwblem::class)
    fun `Skal feile dersom andreBarn har ugyldig fnr og ingenFnr er false`(){
        val melding = meldingDeleOmsorgsdager.copy(
            andreBarn = listOf(
                AndreBarn(
                    fnr = "ikke gyldig",
                    navn = "Barn",
                    ingenFnr = false
                )
            )
        )
        melding.valider()
    }

}