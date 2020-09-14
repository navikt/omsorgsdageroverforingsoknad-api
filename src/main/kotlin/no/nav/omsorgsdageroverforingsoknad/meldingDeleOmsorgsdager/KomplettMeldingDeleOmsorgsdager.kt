package no.nav.omsorgsdageroverforingsoknad.meldingDeleOmsorgsdager

import no.nav.omsorgsdageroverforingsoknad.barn.Barn
import no.nav.omsorgsdageroverforingsoknad.soker.Søker
import no.nav.omsorgsdageroverforingsoknad.soknadOverforeDager.Arbeidssituasjon
import java.time.ZonedDateTime

data class KomplettMeldingDeleOmsorgsdager(
    val mottatt: ZonedDateTime,
    val søker: Søker,
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