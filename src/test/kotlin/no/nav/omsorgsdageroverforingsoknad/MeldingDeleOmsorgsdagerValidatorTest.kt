package no.nav.omsorgsdageroverforingsoknad

import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.omsorgsdageroverforingsoknad.meldingDeleOmsorgsdager.MeldingDeleOmsorgsdager
import no.nav.omsorgsdageroverforingsoknad.meldingDeleOmsorgsdager.valider
import no.nav.omsorgsdageroverforingsoknad.soknadOverforeDager.Medlemskap
import no.nav.omsorgsdageroverforingsoknad.soknadOverforeDager.Utenlandsopphold
import org.junit.Test
import java.time.LocalDate

internal class MeldingDeleOmsorgsdagerValidatorTest {

    companion object {
        private val gyldigFodselsnummerA = "26104500284"
        private val dNummerA = "55125314561"
    }

    @Test
    fun `Skal ikke feile på gyldig søknad`(){
        val melding = MeldingDeleOmsorgsdager(
            språk = "nb",
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
            harBekreftetOpplysninger = true
        )

        melding.valider()
    }

    @Test(expected = Throwblem::class)
    fun `Skal feile dersom harBekreftetOpplysninger er false`(){
        val melding = MeldingDeleOmsorgsdager(
            språk = "nb",
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
            harBekreftetOpplysninger = false
        )

        melding.valider()
    }

}