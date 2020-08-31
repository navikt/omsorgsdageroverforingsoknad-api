package no.nav.omsorgsdageroverforingsoknad.meldingDeleOmsorgsdager

import no.nav.omsorgsdageroverforingsoknad.soknadOverforeDager.Medlemskap

data class MeldingDeleOmsorgsdager(
    val språk: String,
    val medlemskap: Medlemskap,
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean
)