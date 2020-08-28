package no.nav.omsorgsdageroverforingsoknad.meldingDeleOmsorgsdager

import no.nav.omsorgsdageroverforingsoknad.soker.Søker
import java.time.ZonedDateTime

data class KomplettMeldingDeleOmsorgsdager(
    val mottatt: ZonedDateTime,
    val søker: Søker,
    val språk: String,
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean
)