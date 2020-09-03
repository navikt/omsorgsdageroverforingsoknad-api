package no.nav.omsorgsdageroverforingsoknad.meldingDeleOmsorgsdager

import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.helse.dusseldorf.ktor.core.ValidationProblemDetails
import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.omsorgsdageroverforingsoknad.soknadOverforeDager.erGyldigNorskIdentifikator

internal fun MeldingDeleOmsorgsdager.valider() {
    val violations: MutableSet<Violation> = mutableSetOf<Violation>()

    //TODO: Utvide med mer validering.

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
                reason = "Dersom harAleneomsorg er true kan ikke harAleneomsorgFor være tom"
            )
        )
    }

    if(harUtvidetRett && harUtvidetRettFor.isEmpty()){
        violations.add(
            Violation(
                parameterName = "harUtvidetRettFor && harUtvidetRett",
                parameterType = ParameterType.ENTITY,
                reason = "Dersom harUtvidetRett er true kan ikke harUtvidetRettFor være tom"
            )
        )
    }

    if(!fnrMottaker.erGyldigNorskIdentifikator()){
        violations.add(
            Violation(
                parameterName = "fnrMottaker",
                parameterType = ParameterType.ENTITY,
                reason = "fnrMottaker er ikke gyldig norsk identifikator"
            )
        )
    }

    if(harDeltDagerMedAndreTidligere && antallDagerHarDeltMedAndre <= 0){
        violations.add(
            Violation(
                parameterName = "harDeltDagerMedAndreTidligere && antallDagerHarDeltMedAndre",
                parameterType = ParameterType.ENTITY,
                reason = "Hvis harDeltDagerMedAndreTidligere er true så må antallDagerHarDeltMedAndre være større enn 0"
            )
        )
    }

    if(antallDagerTilOverføre <= 0){
        violations.add(
            Violation(
                parameterName = "antallDagerTilOverføre",
                parameterType = ParameterType.ENTITY,
                reason = "antallDagerTilOverføre kan ikke være 0 eller mindre"
            )
        )
    }

// Ser om det er noen valideringsfeil
    if (violations.isNotEmpty()) {
        throw Throwblem(ValidationProblemDetails(violations))
    }
}