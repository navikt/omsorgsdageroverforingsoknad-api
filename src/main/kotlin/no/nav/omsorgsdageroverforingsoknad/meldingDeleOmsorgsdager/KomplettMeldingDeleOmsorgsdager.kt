package no.nav.omsorgsdageroverforingsoknad.meldingDeleOmsorgsdager

import no.nav.omsorgsdageroverforingsoknad.soker.Søker
import no.nav.omsorgsdageroverforingsoknad.soknadOverforeDager.Medlemskap
import java.time.ZonedDateTime

data class KomplettMeldingDeleOmsorgsdager(
    val mottatt: ZonedDateTime,
    val søker: Søker,
    val språk: String,
    val medlemskap: Medlemskap,
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean
)