package no.nav.omsorgsdageroverforingsoknad.meldingDeleOmsorgsdager

import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.helse.dusseldorf.ktor.core.ValidationProblemDetails
import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.omsorgsdageroverforingsoknad.soknadOverforeDager.erGyldigNorskIdentifikator

val MAX_ANTALL_DAGER_MAN_KAN_DELE = 10
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


    if(antallDagerBruktIÅr !in 0..MAX_ANTALL_DAGER_MAN_KAN_DELE){
        violations.add(
            Violation(
                parameterName = "antallDagerBruktIÅr",
                parameterType = ParameterType.ENTITY,
                reason = "antallDagerBruktIÅr må være mellom 0 og $MAX_ANTALL_DAGER_MAN_KAN_DELE",
                invalidValue = antallDagerBruktIÅr
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

    violations.addAll(barn.valider())

    if (violations.isNotEmpty()) {
        throw Throwblem(ValidationProblemDetails(violations))
    }
}

private fun List<BarnUtvidet>.valider(): MutableSet<Violation> {
    val violations: MutableSet<Violation> = mutableSetOf<Violation>()

    forEachIndexed { index, barnUtvidet ->
        if(barnUtvidet.identitetsnummer.isNullOrEmpty()){
            violations.add(
                Violation(
                    parameterName = "barn[$index].identitetsnummer",
                    parameterType = ParameterType.ENTITY,
                    reason = "identitetsnummer er tom eller null",
                    invalidValue = barnUtvidet.identitetsnummer
                )
            )
        }

        if(barnUtvidet.identitetsnummer != null){
            if(!barnUtvidet.identitetsnummer!!.erGyldigNorskIdentifikator()){
                violations.add(
                    Violation(
                        parameterName = "barn[$index].identitetsnummer",
                        parameterType = ParameterType.ENTITY,
                        reason = "identitetsnummer er ikke gyldig norsk identifikator",
                        invalidValue = barnUtvidet.identitetsnummer
                    )
                )
            }
        }
    }

    return violations
}
