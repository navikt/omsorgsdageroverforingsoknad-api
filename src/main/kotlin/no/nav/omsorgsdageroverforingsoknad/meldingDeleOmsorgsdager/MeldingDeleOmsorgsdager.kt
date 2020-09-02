package no.nav.omsorgsdageroverforingsoknad.meldingDeleOmsorgsdager

import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.omsorgsdageroverforingsoknad.barn.Barn
import no.nav.omsorgsdageroverforingsoknad.soknadOverforeDager.Arbeidssituasjon
import no.nav.omsorgsdageroverforingsoknad.soknadOverforeDager.Medlemskap

data class MeldingDeleOmsorgsdager(
    val språk: String,
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean,
    val andreBarn: List<AndreBarn>,
    val harAleneomsorg: Boolean,
    val harAleneomsorgFor: List<Barn>,
    val harUtvidetRett: Boolean,
    val harUtvidetRettFor: List<Barn>,
    val borINorge: Boolean,
    val arbeidINorge: Boolean,
    val arbeidssituasjon: List<Arbeidssituasjon>,
    val antallDagerHarBruktEtter1Juli: Int,
    val harDeltDagerMedAndreTidligere: Boolean,
    val antallDagerHarDeltMedAndre: Int,
    val overføreTilType: OverføreTilType,
    val fnrMottaker: String,
    val navnMottaker: String,
    val antallDagerTilOverføre: Int,
    val harBekreftetMottakerOpplysninger: Boolean

)

data class AndreBarn (
    val fnr: String,
    val ingenFnr: Boolean,
    val navn: String
)



enum class OverføreTilType() {
    @JsonProperty("nyEktefelle") NY_EKTEFELLE,
    @JsonProperty("nySamboerHarBoddSammenMinst12maneder") NY_SAMBOER_HAR_BODD_SAMMEN_MINST_12_MÅNEDER
}