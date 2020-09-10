package no.nav.omsorgsdageroverforingsoknad.meldingDeleOmsorgsdager;

import no.nav.omsorgsdageroverforingsoknad.general.CallId
import no.nav.omsorgsdageroverforingsoknad.general.auth.IdToken
import no.nav.omsorgsdageroverforingsoknad.soker.Søker
import no.nav.omsorgsdageroverforingsoknad.soker.SøkerService
import no.nav.omsorgsdageroverforingsoknad.soker.validate
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.ZoneOffset
import java.time.ZonedDateTime

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
        logger.info("Registrerer melding for deling av omsorgsdager. Henter søker")
        val søker: Søker = søkerService.getSoker(idToken = idToken, callId = callId)

        logger.info("Søker hentet. Validerer søker.")
        søker.validate()

        logger.info("Legger melding om deling av omsorgsdager til prosessering")

        val komplettMeldingDeleOmsorgsdager = KomplettMeldingDeleOmsorgsdager(
            mottatt = ZonedDateTime.now(ZoneOffset.UTC),
            søker = søker,
            språk = melding.språk,
            harForståttRettigheterOgPlikter = melding.harForståttRettigheterOgPlikter,
            harBekreftetOpplysninger = melding.harBekreftetOpplysninger,
            andreBarn = melding.andreBarn,
            harAleneomsorg = melding.harAleneomsorg,
            harAleneomsorgFor = melding.harAleneomsorgFor,
            harUtvidetRett = melding.harUtvidetRett,
            harUtvidetRettFor = melding.harUtvidetRettFor,
            borINorge = melding.borINorge,
            arbeidINorge = melding.arbeidINorge,
            arbeidssituasjon = melding.arbeidssituasjon,
            antallDagerBruktEtter1Juli = melding.antallDagerBruktEtter1Juli,
            mottakerType = melding.mottakerType,
            mottakerFnr = melding.mottakerFnr,
            mottakerNavn = melding.mottakerNavn,
            antallDagerSomSkalOverføres = melding.antallDagerSomSkalOverføres
        )

        meldingDeleOmsorgsdagerMottakGateway.leggTilProsesseringDeleOmsorgsdager(
            soknad = komplettMeldingDeleOmsorgsdager,
            callId = callId
        )

        logger.trace("Melding om deling av omsorgsdager lagt til prosessering.")
    }
}