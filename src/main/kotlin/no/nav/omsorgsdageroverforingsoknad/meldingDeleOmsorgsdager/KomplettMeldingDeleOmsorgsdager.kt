package no.nav.omsorgsdageroverforingsoknad.meldingDeleOmsorgsdager

import java.time.ZonedDateTime

data class KomplettMeldingDeleOmsorgsdager(
    val mottatt: ZonedDateTime,
    val språk: String,
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean
)