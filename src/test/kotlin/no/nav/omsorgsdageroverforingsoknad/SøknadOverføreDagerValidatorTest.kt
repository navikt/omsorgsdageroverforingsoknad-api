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

internal fun Medlemskap.valider(): MutableSet<Violation> {
    val violations = mutableSetOf<Violation>()

    // Booleans (For å forsikre at de er satt og ikke blir default false)
    fun booleanIkkeSatt(parameterName: String) {
        violations.add(
            Violation(
                parameterName = parameterName,
                parameterType = ParameterType.ENTITY,
                reason = "Må settes til true eller false.",
                invalidValue = null
            )
        )
    }

    when (harBoddIUtlandetSiste12Mnd) {
        null -> {
            booleanIkkeSatt("medlemskap.harBoddIUtlandetSiste12Mnd")
        }
        true -> {
            violations.addAll(
                validerUtenlandopphold(
                    "medlemskap.harBoddIUtlandetSiste12Mnd",
                    "medlemskap.utenlandsoppholdSiste12Mnd",
                    utenlandsoppholdSiste12Mnd
                )
            )
        }
        else -> {
        }
    }

    when (skalBoIUtlandetNeste12Mnd) {
        null -> {
            booleanIkkeSatt("medlemskap.skalBoIUtlandetNeste12Mnd")
        }
        true -> {
            violations.addAll(
                validerUtenlandopphold(
                    "medlemskap.skalBoIUtlandetNeste12Mnd",
                    "medlemskap.utenlandsoppholdNeste12Mnd",
                    utenlandsoppholdNeste12Mnd
                )
            )
        }
        else -> {
        }
    }

    return violations
}


private fun validerUtenlandopphold(
    relatertFelt: String,
    felt: String,
    utenlandsOpphold: List<Utenlandsopphold>
): MutableSet<Violation> {
    val violations = mutableSetOf<Violation>()

    if (utenlandsOpphold.isNullOrEmpty()) {
        violations.add(
            Violation(
                parameterName = "$felt",
                parameterType = ParameterType.ENTITY,
                reason = "$relatertFelt er satt til true, men $relatertFelt var tomt eller null.",
                invalidValue = utenlandsOpphold
            )
        )
    }

    utenlandsOpphold.mapIndexed { index, utenlandsopphold ->
        val fraDataErEtterTilDato = utenlandsopphold.fraOgMed.isAfter(utenlandsopphold.tilOgMed)
        if (fraDataErEtterTilDato) {
            violations.add(
                Violation(
                    parameterName = "Utenlandsopphold[$index].fraOgMed eller Utenlandsopphold[$index].tilOgMed",
                    parameterType = ParameterType.ENTITY,
                    reason = "Til dato kan ikke være før fra dato",
                    invalidValue = "fraOgMed eller tilOgMed"
                )
            )
        }
        if (utenlandsopphold.landkode.isEmpty()) {
            violations.add(
                Violation(
                    parameterName = "Utenlandsopphold[$index].landkode",
                    parameterType = ParameterType.ENTITY,
                    reason = "Landkode er ikke satt",
                    invalidValue = "landkode"
                )
            )
        }
        if (utenlandsopphold.landnavn.isEmpty()) {
            violations.add(
                Violation(
                    parameterName = "Utenlandsopphold[$index].landnavn",
                    parameterType = ParameterType.ENTITY,
                    reason = "Landnavn er ikke satt",
                    invalidValue = "landnavn"
                )
            )
        }
    }
    return violations
}

fun String.erKunSiffer() = matches(KUN_SIFFER)

fun String.starterMedFodselsdato(): Boolean {
    // Sjekker ikke hvilket århundre vi skal tolket yy som, kun at det er en gyldig dato.
    // F.eks blir 290990 parset til 2090-09-29, selv om 1990-09-29 var ønskelig.
    // Kunne sett på individsifre (Tre første av personnummer) for å tolke århundre,
    // men virker unødvendig komplekst og sårbart for ev. endringer i fødselsnummeret.
    return try {
        var substring = substring(0, 6)
        val førsteSiffer = (substring[0]).toString().toInt()
        if (førsteSiffer in 4..7) {
            substring = (førsteSiffer - 4).toString() + substring(1, 6)
        }
        fnrDateFormat.parse(substring)

        true
    } catch (cause: Throwable) {
        false
    }
}

fun String.erGyldigNorskIdentifikator(): Boolean {
    if (length != 11 || !erKunSiffer() || !starterMedFodselsdato()) return false

    val forventetKontrollsifferEn = get(9)

    val kalkulertKontrollsifferEn = Mod11.kontrollsiffer(
        number = substring(0, 9),
        vekttallProvider = vekttallProviderFnr1
    )

    if (kalkulertKontrollsifferEn != forventetKontrollsifferEn) return false

    val forventetKontrollsifferTo = get(10)

    val kalkulertKontrollsifferTo = Mod11.kontrollsiffer(
        number = substring(0, 10),
        vekttallProvider = vekttallProviderFnr2
    )

    return kalkulertKontrollsifferTo == forventetKontrollsifferTo
}

/**
 * https://github.com/navikt/helse-sparkel/blob/2e79217ae00632efdd0d4e68655ada3d7938c4b6/src/main/kotlin/no/nav/helse/ws/organisasjon/Mod11.kt
 * https://www.miles.no/blogg/tema/teknisk/validering-av-norske-data
 */
internal object Mod11 {
    private val defaultVekttallProvider: (Int) -> Int = { 2 + it % 6 }

    internal fun kontrollsiffer(
        number: String,
        vekttallProvider: (Int) -> Int = defaultVekttallProvider
    ): Char {
        return number.reversed().mapIndexed { i, char ->
            Character.getNumericValue(char) * vekttallProvider(i)
        }.sum().let(Mod11::kontrollsifferFraSum)
    }


    private fun kontrollsifferFraSum(sum: Int) = sum.rem(11).let { rest ->
        when (rest) {
            0 -> '0'
            1 -> '-'
            else -> "${11 - rest}"[0]
        }
    }
}