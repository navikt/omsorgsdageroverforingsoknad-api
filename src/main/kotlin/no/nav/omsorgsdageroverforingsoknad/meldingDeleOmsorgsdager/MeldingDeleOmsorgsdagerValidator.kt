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

    violations.addAll(andreBarn.valider())

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
                invalidValue = harForståttRettigheterOgPlikter
            )
        )
    }

    if (!harBekreftetMottakerOpplysninger) {
        violations.add(
            Violation(
                parameterName = "harBekreftetMottakerOpplysninger",
                parameterType = ParameterType.ENTITY,
                reason = "Må ha bekreftet mottaker opplysninger.",
                invalidValue = harBekreftetMottakerOpplysninger
            )
        )
    }

    if(harAleneomsorg && harAleneomsorgFor.isEmpty()){
        violations.add(
            Violation(
                parameterName = "harAleneomsorgFor && harAleneomsorg",
                parameterType = ParameterType.ENTITY,
                reason = "Dersom harAleneomsorg er true kan ikke harAleneomsorgFor være tom",
                invalidValue = harAleneomsorgFor
            )
        )
    }

    if(harUtvidetRett && harUtvidetRettFor.isEmpty()){
        violations.add(
            Violation(
                parameterName = "harUtvidetRettFor && harUtvidetRett",
                parameterType = ParameterType.ENTITY,
                reason = "Dersom harUtvidetRett er true kan ikke harUtvidetRettFor være tom",
                invalidValue = harUtvidetRettFor
            )
        )
    }

    if(!fnrMottaker.erGyldigNorskIdentifikator()){
        violations.add(
            Violation(
                parameterName = "fnrMottaker",
                parameterType = ParameterType.ENTITY,
                reason = "fnrMottaker er ikke gyldig norsk identifikator",
                invalidValue = fnrMottaker
            )
        )
    }

    if(harDeltDagerMedAndreTidligere && antallDagerHarDeltMedAndre <= 0){
        violations.add(
            Violation(
                parameterName = "harDeltDagerMedAndreTidligere && antallDagerHarDeltMedAndre",
                parameterType = ParameterType.ENTITY,
                reason = "Hvis harDeltDagerMedAndreTidligere er true så må antallDagerHarDeltMedAndre være større enn 0",
                invalidValue = antallDagerHarDeltMedAndre
            )
        )
    }

    if(antallDagerTilOverføre !in MIN_ANTALL_DAGER_MAN_KAN_DELE..MAX_ANTALL_DAGER_MAN_KAN_DELE){
        violations.add(
            Violation(
                parameterName = "antallDagerTilOverføre",
                parameterType = ParameterType.ENTITY,
                reason = "antallDagerTilOverføre må være mellom $MIN_ANTALL_DAGER_MAN_KAN_DELE og $MAX_ANTALL_DAGER_MAN_KAN_DELE",
                invalidValue = antallDagerTilOverføre
            )
        )
    }

// Ser om det er noen valideringsfeil
    if (violations.isNotEmpty()) {
        throw Throwblem(ValidationProblemDetails(violations))
    }
}

private fun List<AndreBarn>.valider(): MutableSet<Violation> {
    val violations = mutableSetOf<Violation>()

    mapIndexed { index, andreBarn ->
        if (!andreBarn.ingenFnr && andreBarn.fnr.isNullOrBlank()) {
            violations.add(
                Violation(
                    parameterName = "andreBarn[$index].fnr",
                    parameterType = ParameterType.ENTITY,
                    reason = "Dersom ingenFnr er false så må fnr være satt",
                    invalidValue = andreBarn.fnr
                )
            )
        }

        if (!andreBarn.ingenFnr && andreBarn.fnr != null && !andreBarn.fnr.erGyldigNorskIdentifikator()) {
            violations.add(
                Violation(
                    parameterName = "andreBarn[$index].fnr",
                    parameterType = ParameterType.ENTITY,
                    reason = "Dersom ingenFnr er false så må fnr være gyldig norsk idenfifikator",
                    invalidValue = andreBarn.fnr
                )
            )
        }
    }

    return violations
}