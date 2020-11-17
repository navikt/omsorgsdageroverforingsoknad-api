package no.nav.omsorgsdageroverforingsoknad.meldingDeleOmsorgsdager

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.omsorgsdageroverforingsoknad.barn.Barn
import no.nav.omsorgsdageroverforingsoknad.soker.Søker
import no.nav.omsorgsdageroverforingsoknad.soknadOverforeDager.Arbeidssituasjon
import java.time.ZoneOffset
import java.time.ZonedDateTime

data class MeldingDeleOmsorgsdager(
    val id: String,
    val språk: String,
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean,
    val arbeiderINorge: Boolean? = null, //Settes til null for å unngå default false
    val barn: List<BarnUtvidet>,
    val arbeidssituasjon: List<Arbeidssituasjon>,
    val erYrkesaktiv: Boolean? = null, //Settes til null for å unngå default false
    val mottakerType: Mottaker,
    val mottakerFnr: String,
    val mottakerNavn: String,
    val antallDagerSomSkalOverføres: Int,
    @JsonAlias("antallDagerBruktEtter1Juli")
    val antallDagerBruktIÅr: Int //Frontend burde oppdateres etter 2020 og alias fjernes.
    ){

    fun oppdaterBarnMedFnr(listeOverBarn: List<Barn>){
        barn.forEach { barn ->
            if(barn.manglerIdentitetsnummer()){
                barn oppdaterIdentitetsnummerMed listeOverBarn.hentIdentitetsnummerForBarn(barn.aktørId)
            }
        }
    }

    fun tilKomplettMelding(søker: Søker): KomplettMeldingDeleOmsorgsdager{
        return KomplettMeldingDeleOmsorgsdager(
            mottatt = ZonedDateTime.now(ZoneOffset.UTC),
            søker = søker,
            språk = språk,
            id = id,
            harForståttRettigheterOgPlikter = harForståttRettigheterOgPlikter,
            harBekreftetOpplysninger = harBekreftetOpplysninger,
            barn = barn,
            arbeiderINorge = arbeiderINorge!!,
            arbeidssituasjon = arbeidssituasjon,
            antallDagerBruktIÅr = antallDagerBruktIÅr,
            mottakerType = mottakerType,
            mottakerFnr = mottakerFnr,
            mottakerNavn = mottakerNavn,
            antallDagerSomSkalOverføres = antallDagerSomSkalOverføres,
            erYrkesaktiv = erYrkesaktiv!!
        )
    }

}

enum class Mottaker() {
    @JsonProperty("ektefelle") EKTEFELLE,
    @JsonProperty("samboer") SAMBOER
}

private fun List<Barn>.hentIdentitetsnummerForBarn(aktørId: String?): String?{
    this.forEach {
        if(it.aktørId == aktørId) return it.identitetsnummer
    }
    return null
}
