package no.nav.omsorgsdageroverforingsoknad.meldingDeleOmsorgsdager

import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.helse.dusseldorf.ktor.core.ValidationProblemDetails
import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.omsorgsdageroverforingsoknad.soknadOverforeDager.erGyldigNorskIdentifikator

val MAX_ANTALL_DAGER_MAN_KAN_DELE = 999
val MIN_ANTALL_DAGER_MAN_KAN_DELE = 1

internal fun MeldingDeleOmsorgsdager.valider() {
    val violations: MutableSet<Violation> = mutableSetOf<Violation>()

    //TODO: Utvide med mer validering.

    if (!harForståttRettigheterOgPlikter) {
        violations.add(
            Violation(
                parameterName = "harForståttRettigheterOgPlikter",
                parameterType = ParameterType.ENTITY,
                reason = "Må ha forstått rettigheter og plikter for å sende inn søknad.",
                invalidValue = harForståttRettigheterOgPlikter
            )
        )
    }

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

    violations.addAll(andreBarn.valider())

    if(harAleneomsorg && harAleneomsorgFor.erTom()){
        violations.add(
            Violation(
                parameterName = "harAleneomsorgFor && harAleneomsorg",
                parameterType = ParameterType.ENTITY,
                reason = "Dersom harAleneomsorg er true kan ikke harAleneomsorgFor være tom",
                invalidValue = harAleneomsorgFor
            )
        )
    }

    violations.addAll(harAleneomsorgFor.valider("harAleneomsorgFor."))

    if(harUtvidetRett && harUtvidetRettFor.erTom()){
        violations.add(
            Violation(
                parameterName = "harUtvidetRettFor && harUtvidetRett",
                parameterType = ParameterType.ENTITY,
                reason = "Dersom harUtvidetRett er true kan ikke harUtvidetRettFor være tom",
                invalidValue = harUtvidetRettFor
            )
        )
    }

    violations.addAll(harUtvidetRettFor.valider("harUtvidetRettFor."))

    if(antallDagerBruktEtter1Juli !in 0..MAX_ANTALL_DAGER_MAN_KAN_DELE){
        violations.add(
            Violation(
                parameterName = "antallDagerBruktEtter1Juli",
                parameterType = ParameterType.ENTITY,
                reason = "antallDagerBruktEtter1Juli må være mellom 0 og $MAX_ANTALL_DAGER_MAN_KAN_DELE",
                invalidValue = antallDagerBruktEtter1Juli
            )
        )
    }

    if(!mottakerFnr.erGyldigNorskIdentifikator()){
        violations.add(
            Violation(
                parameterName = "fnrMottaker",
                parameterType = ParameterType.ENTITY,
                reason = "fnrMottaker er ikke gyldig norsk identifikator",
                invalidValue = mottakerFnr
            )
        )
    }

    if(mottakerNavn.isBlank()){
        violations.add(
            Violation(
                parameterName = "mottakerNavn",
                parameterType = ParameterType.ENTITY,
                reason = "mottakerNavn er tomt eller bare whitespace",
                invalidValue = mottakerNavn
            )
        )
    }

    if(antallDagerSomSkalOverføres !in MIN_ANTALL_DAGER_MAN_KAN_DELE..MAX_ANTALL_DAGER_MAN_KAN_DELE){
        violations.add(
            Violation(
                parameterName = "antallDagerTilOverføre",
                parameterType = ParameterType.ENTITY,
                reason = "antallDagerTilOverføre må være mellom $MIN_ANTALL_DAGER_MAN_KAN_DELE og $MAX_ANTALL_DAGER_MAN_KAN_DELE",
                invalidValue = antallDagerSomSkalOverføres
            )
        )
    }

    if (violations.isNotEmpty()) {
        throw Throwblem(ValidationProblemDetails(violations))
    }
}

private fun List<AndreBarn>.valider(prefixSti: String = ""): MutableSet<Violation> {
    val violations = mutableSetOf<Violation>()

    mapIndexed { index, andreBarn ->
        violations.addAll(andreBarn.valider(prefixSti, index))
    }

    return violations
}

private fun AndreBarn.valider(prefixSti: String? = "", index: Int): MutableSet<Violation>{
    val violations = mutableSetOf<Violation>()

    if(!fnr.erGyldigNorskIdentifikator()){
        violations.add(
            Violation(
                parameterName = "${prefixSti}andreBarn[$index].fnr",
                parameterType = ParameterType.ENTITY,
                reason = "Fnr er ikke gyldig norsk identifikator",
                invalidValue = fnr
            )
        )
    }

    return violations
}

private fun BarnOgAndreBarn.valider(prefixSti: String): MutableSet<Violation>{
    val violations = mutableSetOf<Violation>()

    violations.addAll(andreBarn.valider(prefixSti))

    return violations
}