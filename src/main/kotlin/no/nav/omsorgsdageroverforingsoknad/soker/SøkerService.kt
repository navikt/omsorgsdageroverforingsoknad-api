package no.nav.omsorgsdageroverforingsoknad.soker

import com.auth0.jwt.JWT
import no.nav.omsorgsdageroverforingsoknad.general.CallId
import no.nav.omsorgsdageroverforingsoknad.general.auth.IdToken

class SøkerService (
    private val søkerGateway: SøkerGateway
) {
    suspend fun getSoker(
        idToken: IdToken,
        callId: CallId
    ): Søker {
        val ident: String = JWT.decode(idToken.value).subject ?: throw IllegalStateException("Token mangler 'sub' claim.")
        return søkerGateway.hentSoker(idToken, callId).tilSøker(ident)
    }

    private fun  SøkerGateway.SokerOppslagRespons.tilSøker(fodselsnummer: String) = Søker(
        aktørId = aktør_id,
        fødselsnummer = fodselsnummer,
        fødselsdato = fødselsdato,
        fornavn = fornavn,
        mellomnavn = mellomnavn,
        etternavn = etternavn
    )
}