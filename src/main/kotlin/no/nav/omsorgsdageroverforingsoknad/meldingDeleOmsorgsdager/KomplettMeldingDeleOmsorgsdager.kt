package no.nav.omsorgsdageroverforingsoknad.meldingDeleOmsorgsdager

import no.nav.omsorgsdageroverforingsoknad.soker.Søker
import no.nav.omsorgsdageroverforingsoknad.soknadOverforeDager.Arbeidssituasjon
import java.time.ZonedDateTime

data class KomplettMeldingDeleOmsorgsdager(
    val mottatt: ZonedDateTime,
    val søker: Søker,
    val språk: String,
    val id: String,
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean,
    val barn: List<BarnUtvidet>,
    val arbeiderINorge: Boolean,
    val arbeidssituasjon: List<Arbeidssituasjon>,
    val antallDagerBruktIÅr: Int,
    val mottakerType: Mottaker,
    val mottakerFnr: String,
    val mottakerNavn: String,
    val antallDagerSomSkalOverføres: Int
)