package no.nav.omsorgsdageroverforingsoknad.soknadOverforeDager

import no.nav.omsorgsdageroverforingsoknad.soker.Søker
import java.time.ZonedDateTime

data class KomplettSøknadOverføreDager (
    val mottatt: ZonedDateTime,
    val søker: Søker,
    val språk: String,
    val antallDager: Int,
    val fnrMottaker: String,
    val navnMottaker: String,
    val medlemskap: Medlemskap,
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean,
    val arbeidssituasjon: List<Arbeidssituasjon>,
    val fosterbarn: List<Fosterbarn>? = listOf(),
    val stengingsperiode: Stengingsperiode? = null//TODO Fjerne optional etter frontend har prodsatt
)


