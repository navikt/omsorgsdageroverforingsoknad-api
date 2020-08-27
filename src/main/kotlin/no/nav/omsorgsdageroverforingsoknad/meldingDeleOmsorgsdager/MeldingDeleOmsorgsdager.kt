package no.nav.omsorgsdageroverforingsoknad.meldingDeleOmsorgsdager

data class MeldingDeleOmsorgsdager(
    val språk: String,
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean
)