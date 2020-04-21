package no.nav.omsorgsdageroverforingsoknad

import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.omsorgsdageroverforingsoknad.soknadOverforeDager.*
import org.junit.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val KUN_SIFFER = Regex("\\d+")
internal val vekttallProviderFnr1: (Int) -> Int = { arrayOf(3, 7, 6, 1, 8, 9, 4, 5, 2).reversedArray()[it] }
internal val vekttallProviderFnr2: (Int) -> Int = { arrayOf(5, 4, 3, 2, 7, 6, 5, 4, 3, 2).reversedArray()[it] }
private val fnrDateFormat = DateTimeFormatter.ofPattern("ddMMyy")

internal class SøknadOverføreDagerValideringsTest {

    companion object {
        private val gyldigFodselsnummerA = "26104500284"
        private val dNummerA = "55125314561"
    }

    @Test
    fun `Skal ikke feile på gyldig søknad`(){
        val søknadOverføreDager = SøknadOverføreDager(
            språk = "nb",
            antallDager = 5,
            fnrMottaker = gyldigFodselsnummerA,
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
            )
        )
        søknadOverføreDager.valider()
    }

    @Test
    fun `Skal ikke feile på gyldig søknad med dNummer som identifikator`(){
        val søknadOverføreDager = SøknadOverføreDager(
            språk = "nb",
            antallDager = 5,
            fnrMottaker = dNummerA,
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
            )
        )
        søknadOverføreDager.valider()
    }



    @Test(expected = Throwblem::class)
    fun `Skal feile dersom harBekreftetOpplysninger er false`(){
        val søknadOverføreDager = SøknadOverføreDager(
            språk = "nb",
            antallDager = 5,
            fnrMottaker = gyldigFodselsnummerA,
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
            harBekreftetOpplysninger = false,
            arbeidssituasjon = listOf(
                Arbeidssituasjon.ARBEIDSTAKER
            )
        )
        søknadOverføreDager.valider()
    }

    @Test(expected = Throwblem::class)
    fun `Skal feile dersom harForståttRettigheterOgPlikter er false`(){
        val søknadOverføreDager = SøknadOverføreDager(
            språk = "nb",
            antallDager = 5,
            fnrMottaker = gyldigFodselsnummerA,
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
            harForståttRettigheterOgPlikter = false,
            harBekreftetOpplysninger = true,
            arbeidssituasjon = listOf(
                Arbeidssituasjon.ARBEIDSTAKER
            )
        )
        søknadOverføreDager.valider()
    }

    @Test(expected = Throwblem::class)
    fun `Skal feile dersom arbeidssituasjon er tom`(){
        val søknadOverføreDager = SøknadOverføreDager(
            språk = "nb",
            antallDager = 5,
            fnrMottaker = gyldigFodselsnummerA,
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
            arbeidssituasjon = listOf()
        )
        søknadOverføreDager.valider()
    }

    @Test(expected = Throwblem::class)
    fun `Skal feile dersom mottakerAvDagerNorskIdentifikator er ugyldig nr`(){
        val søknadOverføreDager = SøknadOverføreDager(
            språk = "nb",
            antallDager = 5,
            fnrMottaker = "111111111",
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
            )
        )
        søknadOverføreDager.valider()
    }

    @Test(expected = Throwblem::class)
    fun `Skal feile dersom fnr til fosterbarn er ugyldig nr`(){
        val søknadOverføreDager = SøknadOverføreDager(
            språk = "nb",
            antallDager = 5,
            fnrMottaker = gyldigFodselsnummerA,
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
            fosterbarn = listOf(
                Fosterbarn(
                    fødselsnummer = "111"
                )
            )
        )
        søknadOverføreDager.valider()
    }

    @Test(expected = Throwblem::class)
    fun `Skal feile dersom navn til mottaker er bare space`(){
        val søknadOverføreDager = SøknadOverføreDager(
            språk = "nb",
            antallDager = 5,
            fnrMottaker = gyldigFodselsnummerA,
            navnMottaker = " ",
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
            fosterbarn = listOf(
                Fosterbarn(
                    fødselsnummer = "111"
                )
            )
        )
        søknadOverføreDager.valider()
    }
}