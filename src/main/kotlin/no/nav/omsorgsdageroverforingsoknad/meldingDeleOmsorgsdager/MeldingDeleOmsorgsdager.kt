package no.nav.omsorgsdageroverforingsoknad.meldingDeleOmsorgsdager

import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.omsorgsdageroverforingsoknad.barn.Barn
import no.nav.omsorgsdageroverforingsoknad.soknadOverforeDager.Arbeidssituasjon
import java.time.LocalDate

data class MeldingDeleOmsorgsdager(
    val språk: String,
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean,
    val andreBarn: List<AndreBarn>,
    val harAleneomsorg: Boolean,
    val harAleneomsorgFor: BarnOgAndreBarn,
    val harUtvidetRett: Boolean,
    val harUtvidetRettFor: BarnOgAndreBarn,
    val borINorge: Boolean,
    val arbeidINorge: Boolean,
    val arbeidssituasjon: List<Arbeidssituasjon>,
    val antallDagerBruktEtter1Juli: Int,
    val mottakerType: Mottaker,
    val mottakerFnr: String,
    val mottakerNavn: String,
    val antallDagerSomSkalOverføres: Int
)

data class BarnOgAndreBarn(
    val barn: List<Barn>,
    val andreBarn: List<AndreBarn>
) {
    fun erTom(): Boolean {
        return barn.isEmpty() && andreBarn.isEmpty()
    }
}

data class AndreBarn (
    val fnr: String,
    val fødselsdato: LocalDate,
    val navn: String
)

enum class Mottaker() {
    @JsonProperty("ektefelle") EKTEFELLE,
    @JsonProperty("samboer") SAMBOER
}