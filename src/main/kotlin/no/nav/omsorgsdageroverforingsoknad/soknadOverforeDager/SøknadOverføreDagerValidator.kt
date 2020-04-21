package no.nav.omsorgsdageroverforingsoknad.soknadOverforeDager

import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.helse.dusseldorf.ktor.core.ValidationProblemDetails
import no.nav.helse.dusseldorf.ktor.core.Violation
import java.time.format.DateTimeFormatter

private val KUN_SIFFER = Regex("\\d+")
internal val vekttallProviderFnr1: (Int) -> Int = { arrayOf(3, 7, 6, 1, 8, 9, 4, 5, 2).reversedArray()[it] }
internal val vekttallProviderFnr2: (Int) -> Int = { arrayOf(5, 4, 3, 2, 7, 6, 5, 4, 3, 2).reversedArray()[it] }
private val fnrDateFormat = DateTimeFormatter.ofPattern("ddMMyy")

val MAX_ANTALL_DAGER_MAN_KAN_OVERFØRE = 999
val MIN_ANTALL_DAGER_MAN_KAN_OVERFØRE = 1

internal fun SøknadOverføreDager.valider() {
    val violations: MutableSet<Violation> = mutableSetOf<Violation>()

    if (arbeidssituasjon.isEmpty()) {
        violations.add(
            Violation(
                parameterName = "arbeidssituasjon",
                parameterType = ParameterType.ENTITY,
                reason = "List over arbeidssituasjon kan ikke være tomt. Må inneholde minst 1 verdi",
                invalidValue = arbeidssituasjon
            )
        )
    }

    if (antallDager !in MIN_ANTALL_DAGER_MAN_KAN_OVERFØRE..MAX_ANTALL_DAGER_MAN_KAN_OVERFØRE) {
        violations.add(
            Violation(
                parameterName = "antallDager",
                parameterType = ParameterType.ENTITY,
                reason = "Tillatt antall dager man kan overføre må ligge mellom $MIN_ANTALL_DAGER_MAN_KAN_OVERFØRE og $MAX_ANTALL_DAGER_MAN_KAN_OVERFØRE dager.",
                invalidValue = antallDager
            )
        )
    }

    if(navnMottaker.isNullOrBlank()){
        violations.add(
            Violation(
                parameterName = "navnMottaker",
                parameterType = ParameterType.ENTITY,
                reason = "Navn på mottaker må være satt, kan ikke være tomt eller blankt",
                invalidValue = navnMottaker
            )
        )
    }

    violations.addAll(medlemskap.valider())

    fosterbarn?.let { violations.addAll(validerFosterbarn(it)) }

    if (!harBekreftetOpplysninger) {
        violations.add(
            Violation(
                parameterName = "harBekreftetOpplysninger",
                parameterType = ParameterType.ENTITY,
                reason = "Opplysningene må bekreftes for å sende inn søknad.",
                invalidValue = harBekreftetOpplysninger
            )
        )
    }

    if (!harForståttRettigheterOgPlikter) {
        violations.add(
            Violation(
                parameterName = "harForståttRettigheterOgPlikter",
                parameterType = ParameterType.ENTITY,
                reason = "Må ha forstått rettigheter og plikter for å sende inn søknad.",
                invalidValue = harBekreftetOpplysninger
            )
        )
    }

    if (!fnrMottaker.erGyldigNorskIdentifikator()) {
        violations.add(
            Violation(
                parameterName = "fnrMottaker",
                parameterType = ParameterType.ENTITY,
                reason = "Ikke gyldig norskIdentifikator på mottaker av dager",
                invalidValue = fnrMottaker
            )
        )
    }

// Ser om det er noen valideringsfeil
    if (violations.isNotEmpty()) {
        throw Throwblem(ValidationProblemDetails(violations))
    }

}

private fun validerFosterbarn(fosterbarn: List<Fosterbarn>) = mutableSetOf<Violation>().apply {
    fosterbarn.mapIndexed { index, barn ->
        if (!barn.fødselsnummer.erGyldigNorskIdentifikator()) {
            add(
                Violation(
                    parameterName = "fosterbarn[$index].fødselsnummer",
                    parameterType = ParameterType.ENTITY,
                    reason = "Ikke gyldig fødselsnummer.",
                    invalidValue = barn.fødselsnummer
                )
            )
        }
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
