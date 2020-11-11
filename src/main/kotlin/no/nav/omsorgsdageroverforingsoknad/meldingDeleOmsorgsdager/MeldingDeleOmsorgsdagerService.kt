package no.nav.omsorgsdageroverforingsoknad.meldingDeleOmsorgsdager;

import no.nav.omsorgsdageroverforingsoknad.general.CallId
import no.nav.omsorgsdageroverforingsoknad.general.auth.IdToken
import no.nav.omsorgsdageroverforingsoknad.soker.Søker
import no.nav.omsorgsdageroverforingsoknad.soker.SøkerService
import no.nav.omsorgsdageroverforingsoknad.soker.validate
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MeldingDeleOmsorgsdagerService(
    private val meldingDeleOmsorgsdagerMottakGateway: MeldingDeleOmsorgsdagerMottakGateway,
    private val søkerService: SøkerService
){
    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(MeldingDeleOmsorgsdagerService::class.java)
    }

    suspend fun registrer(
        melding: MeldingDeleOmsorgsdager,
        idToken: IdToken,
        callId: CallId
    ){
        logger.trace("Registrerer melding for deling av omsorgsdager. Henter søker")
        val søker: Søker = søkerService.getSoker(idToken = idToken, callId = callId)

        logger.trace("Søker hentet. Validerer søker.")
        søker.validate()

        logger.info("Legger melding om deling av omsorgsdager til prosessering")

        val komplettMeldingDeleOmsorgsdager = melding.tilKomplettMelding(søker)

        meldingDeleOmsorgsdagerMottakGateway.leggTilProsesseringDeleOmsorgsdager(
            soknad = komplettMeldingDeleOmsorgsdager,
            callId = callId
        )

        logger.trace("Melding om deling av omsorgsdager lagt til prosessering.")
    }
}