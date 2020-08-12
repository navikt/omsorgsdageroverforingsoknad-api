package no.nav.omsorgsdageroverforingsoknad.soknadOverforeDager

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

data class SøknadOverføreDager(
    val språk: String,
    val antallDager: Int,
    val fnrMottaker: String,
    val navnMottaker: String,
    val medlemskap: Medlemskap,
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean,
    val arbeidssituasjon: List<Arbeidssituasjon>,
    val fosterbarn: List<Fosterbarn>? = listOf(),
    val stengingsperiode: Stengingsperiode? = null //TODO Fjerne optional etter frontend har prodsatt
)

enum class Arbeidssituasjon() {
    @JsonProperty("arbeidstaker") ARBEIDSTAKER,
    @JsonProperty("selvstendigNæringsdrivende") SELVSTENDIGNÆRINGSDRIVENDE,
    @JsonProperty("frilanser") FRILANSER
}

class Medlemskap(
    val harBoddIUtlandetSiste12Mnd: Boolean,
    val utenlandsoppholdSiste12Mnd: List<Utenlandsopphold> = listOf(),
    val skalBoIUtlandetNeste12Mnd: Boolean,
    val utenlandsoppholdNeste12Mnd: List<Utenlandsopphold> = listOf()
)


data class Utenlandsopphold(
    @JsonFormat(pattern = "yyyy-MM-dd") val fraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd") val tilOgMed: LocalDate,
    val landkode: String,
    val landnavn: String
)

enum class Stengingsperiode(){
    @JsonProperty("mars13tilJuni30") MARS_13_TIL_JUNI_30,
    @JsonProperty("etterAugust10") ETTER_AUGUST_10
}