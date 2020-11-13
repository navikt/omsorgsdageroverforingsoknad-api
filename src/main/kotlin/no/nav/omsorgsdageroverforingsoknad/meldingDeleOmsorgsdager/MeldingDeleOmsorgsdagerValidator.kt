package no.nav.omsorgsdageroverforingsoknad.meldingDeleOmsorgsdager

import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.helse.dusseldorf.ktor.core.ValidationProblemDetails
import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.omsorgsdageroverforingsoknad.soknadOverforeDager.erGyldigNorskIdentifikator

val MAX_ANTALL_DAGER_MAN_KAN_DELE = 10
val MIN_ANTALL_DAGER_MAN_KAN_DELE = 1
val MAX_ANTALL_DAGER_MAN_KAN_HA_DELT_I_ÅR = 999

internal fun MeldingDeleOmsorgsdager.valider() {
    val mangler: MutableSet<Violation> = mutableSetOf<Violation>()

    if(harBekreftetOpplysninger er false) {
        mangler.add(
            Violation(
                parameterName = "harBekreftetOpplysninger",
                parameterType = ParameterType.ENTITY,
                reason = "Opplysningene må bekreftes for å sende inn søknad.",
                invalidValue = harBekreftetOpplysninger
            )
        )
    }

    if(harForståttRettigheterOgPlikter er false) {
        mangler.add(
            Violation(
                parameterName = "harForståttRettigheterOgPlikter",
                parameterType = ParameterType.ENTITY,
                reason = "Må ha forstått rettigheter og plikter for å sende inn søknad.",
                invalidValue = harForståttRettigheterOgPlikter
            )
        )
    }

    if(antallDagerBruktIÅr !in 0..MAX_ANTALL_DAGER_MAN_KAN_HA_DELT_I_ÅR){
        mangler.add(
            Violation(
                parameterName = "antallDagerBruktIÅr",
                parameterType = ParameterType.ENTITY,
                reason = "antallDagerBruktIÅr må være mellom 0 og $MAX_ANTALL_DAGER_MAN_KAN_HA_DELT_I_ÅR",
                invalidValue = antallDagerBruktIÅr
            )
        )
    }

    if(!mottakerFnr.erGyldigNorskIdentifikator()){
        mangler.add(
            Violation(
                parameterName = "fnrMottaker",
                parameterType = ParameterType.ENTITY,
                reason = "fnrMottaker er ikke gyldig norsk identifikator",
                invalidValue = mottakerFnr
            )
        )
    }

    if(mottakerNavn.isBlank()){
        mangler.add(
            Violation(
                parameterName = "mottakerNavn",
                parameterType = ParameterType.ENTITY,
                reason = "mottakerNavn er tomt eller bare whitespace",
                invalidValue = mottakerNavn
            )
        )
    }

    if(antallDagerSomSkalOverføres !in MIN_ANTALL_DAGER_MAN_KAN_DELE..MAX_ANTALL_DAGER_MAN_KAN_DELE){
        mangler.add(
            Violation(
                parameterName = "antallDagerTilOverføre",
                parameterType = ParameterType.ENTITY,
                reason = "antallDagerTilOverføre må være mellom $MIN_ANTALL_DAGER_MAN_KAN_DELE og $MAX_ANTALL_DAGER_MAN_KAN_DELE",
                invalidValue = antallDagerSomSkalOverføres
            )
        )
    }

    if(arbeiderINorge er false){
        mangler.add(
            Violation(
                parameterName = "arbeiderINorge",
                parameterType = ParameterType.ENTITY,
                reason = "arbeiderINorge må være true",
                invalidValue = arbeiderINorge
            )
        )
    }

    mangler.addAll(barn.valider())
    mangler.addAll(nullSjekk(arbeiderINorge, "arbeiderINorge"))

    if (mangler.isNotEmpty()) {
        throw Throwblem(ValidationProblemDetails(mangler))
    }
}

private fun List<BarnUtvidet>.valider(): MutableSet<Violation> {
    val mangler: MutableSet<Violation> = mutableSetOf<Violation>()

    forEachIndexed { index, barnUtvidet ->

        mangler.addAll(nullSjekk(barnUtvidet.aleneOmOmsorgen, "aleneOmOmsorgen"))
        mangler.addAll(nullSjekk(barnUtvidet.utvidetRett, "utvidetRett"))

        if(barnUtvidet.identitetsnummer.isNullOrEmpty()){
            mangler.add(
                Violation(
                    parameterName = "barn[$index].identitetsnummer",
                    parameterType = ParameterType.ENTITY,
                    reason = "identitetsnummer er tom eller null",
                    invalidValue = barnUtvidet.identitetsnummer
                )
            )
        }

        val identitetsnummer = barnUtvidet.identitetsnummer
        if(identitetsnummer != null && !identitetsnummer.erGyldigNorskIdentifikator()){
            mangler.add(
                Violation(
                    parameterName = "barn[$index].identitetsnummer",
                    parameterType = ParameterType.ENTITY,
                    reason = "identitetsnummer er ikke gyldig norsk identifikator",
                    invalidValue = barnUtvidet.identitetsnummer
                )
            )
        }
    }

    return mangler
}

private fun nullSjekk(verdi: Boolean?, navn: String): MutableSet<Violation>{
    val mangler: MutableSet<Violation> = mutableSetOf<Violation>()

    if(verdi er null){
        mangler.add(
            Violation(
                parameterName = navn,
                parameterType = ParameterType.ENTITY,
                reason = "$navn kan ikke være null",
                invalidValue = verdi
            )
        )
    }

    return mangler
}

private infix fun Boolean?.er(forventetVerdi: Boolean?): Boolean = this == forventetVerdi