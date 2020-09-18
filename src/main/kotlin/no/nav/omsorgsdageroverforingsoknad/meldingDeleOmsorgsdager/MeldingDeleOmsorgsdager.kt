package no.nav.omsorgsdageroverforingsoknad.meldingDeleOmsorgsdager

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.omsorgsdageroverforingsoknad.barn.Barn
import no.nav.omsorgsdageroverforingsoknad.soknadOverforeDager.Arbeidssituasjon
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDate


data class MeldingDeleOmsorgsdager(
    val id: String,
    val språk: String,
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean,
    val barn: List<BarnUtvidet>,
    val borINorge: Boolean,
    val arbeiderINorge: Boolean,
    val arbeidssituasjon: List<Arbeidssituasjon>,
    val mottakerType: Mottaker,
    val mottakerFnr: String,
    val mottakerNavn: String,
    val antallDagerSomSkalOverføres: Int,
    @JsonAlias("antallDagerBruktEtter1Juli")val antallDagerBruktIÅr: Int
    ){

    companion object{
        private val logger: Logger = LoggerFactory.getLogger(MeldingDeleOmsorgsdager::class.java)
    }

    fun oppdaterBarnUtvidetMedFnr(listeOverBarn: List<Barn>){
        barn.forEach { barn ->
            if(barn.manglerIdentitetsnummer()){
                logger.info("Oppdaterer fnr på et barn") //TODO FJERN FØR PRODSETTING
                barn oppdaterIdentitetsnummerMed listeOverBarn.hentIdentitetsnummerForBarn(barn.aktørId)
            }
        }
    }

}

private fun List<Barn>.hentIdentitetsnummerForBarn(aktørId: String?): String?{
    this.forEach {
        if(it.aktørId == aktørId) return it.identitetsnummer
    }
    return null
}

enum class Mottaker() {
    @JsonProperty("ektefelle") EKTEFELLE,
    @JsonProperty("samboer") SAMBOER
}

data class BarnUtvidet(
    var identitetsnummer: String?,
    val aktørId: String?,
    val fødselsdato: LocalDate,
    val navn: String,
    val aleneOmOmsorgen: Boolean,
    val utvidetRett: Boolean
){
    fun manglerIdentitetsnummer(): Boolean = identitetsnummer.isNullOrEmpty()

    infix fun oppdaterIdentitetsnummerMed(identitetsnummer: String?){
        this.identitetsnummer = identitetsnummer
    }
}