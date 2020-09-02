package no.nav.omsorgsdageroverforingsoknad

import no.nav.helse.dusseldorf.ktor.core.Throwblem
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
        val melding = MeldingDeleOmsorgsdager(
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

        melding.valider()
    }

    @Test(expected = Throwblem::class)
    fun `Skal feile dersom harBekreftetOpplysninger er false`(){
        val melding = MeldingDeleOmsorgsdager(
            språk = "nb",
            harForståttRettigheterOgPlikter = true,
            harBekreftetOpplysninger = false,
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

        melding.valider()
    }

}