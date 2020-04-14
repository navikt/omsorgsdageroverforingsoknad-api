package no.nav.omsorgsdageroverforingsoknad.soknadOverforeDager;

import no.nav.omsorgsdageroverforingsoknad.general.CallId
import no.nav.omsorgsdageroverforingsoknad.general.auth.IdToken
import no.nav.omsorgsdageroverforingsoknad.soker.Søker
import no.nav.omsorgsdageroverforingsoknad.soker.SøkerService
import no.nav.omsorgsdageroverforingsoknad.soker.validate
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.ZoneOffset
import java.time.ZonedDateTime

class SøknadOverføreDagerService(
    private val omsorgpengesøknadMottakGateway: OmsorgpengesøknadMottakGateway,
    private val søkerService: SøkerService
){
    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(SøknadOverføreDagerService::class.java)
    }

    suspend fun registrer(
        søknadOverføreDager: SøknadOverføreDager,
        idToken: IdToken,
        callId: CallId
    ){
        logger.info("Registrerer søknad for overføring av dager. Henter søker")
        val søker: Søker = søkerService.getSoker(idToken = idToken, callId = callId)

        logger.info("Søker hentet. Validerer søker.")
        søker.validate()

        logger.info("Legger søknad for overføring av dager til prosessering")

        val komplettSøknadOverføreDager = KomplettSøknadOverføreDager(
            språk = søknadOverføreDager.språk,
            mottatt = ZonedDateTime.now(ZoneOffset.UTC),
            søker = søker,
            antallDager = søknadOverføreDager.antallDager,
            fnrMottaker = søknadOverføreDager.fnrMottaker,
            medlemskap = søknadOverføreDager.medlemskap,
            harForståttRettigheterOgPlikter = søknadOverføreDager.harForståttRettigheterOgPlikter,
            harBekreftetOpplysninger = søknadOverføreDager.harBekreftetOpplysninger,
            arbeidssituasjon = søknadOverføreDager.arbeidssituasjon,
            fosterbarn = søknadOverføreDager.fosterbarn
        )

        omsorgpengesøknadMottakGateway.leggTilProsesseringOverføreDager(
            soknad = komplettSøknadOverføreDager,
            callId = callId
        )

        logger.trace("Søknad for overføring av dager lagt til prosessering.")
    }
}