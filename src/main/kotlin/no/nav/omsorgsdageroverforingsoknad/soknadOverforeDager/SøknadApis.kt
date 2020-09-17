package no.nav.omsorgsdageroverforingsoknad.soknadOverforeDager

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.omsorgsdageroverforingsoknad.barn.BarnService
import no.nav.omsorgsdageroverforingsoknad.general.auth.IdTokenProvider
import no.nav.omsorgsdageroverforingsoknad.general.getCallId
import no.nav.omsorgsdageroverforingsoknad.meldingDeleOmsorgsdager.DELE_DAGER_API_URL
import no.nav.omsorgsdageroverforingsoknad.meldingDeleOmsorgsdager.MeldingDeleOmsorgsdager
import no.nav.omsorgsdageroverforingsoknad.meldingDeleOmsorgsdager.MeldingDeleOmsorgsdagerService
import no.nav.omsorgsdageroverforingsoknad.meldingDeleOmsorgsdager.valider
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("nav.soknadApis")

@KtorExperimentalLocationsAPI
fun Route.søknadApis(
    søknadOverføreDagerService: SøknadOverføreDagerService,
    meldingDeleOmsorgsdagerService: MeldingDeleOmsorgsdagerService,
    barnService: BarnService,
    idTokenProvider: IdTokenProvider
) {


    @Location("/soknad/overfore-omsorgsdager")
    class sendSoknadOverforeDager

    post { _ : sendSoknadOverforeDager ->
        logger.trace("Mottatt ny søknad for overføring av dager. Mapper søknad.")
        val søknadOverføreDager = call.receive<SøknadOverføreDager>()
        logger.trace("Søknad mappet. Validerer")

        søknadOverføreDager.valider()
        logger.trace("Validering OK. Registrerer søknad.")

        søknadOverføreDagerService.registrer(
            søknadOverføreDager = søknadOverføreDager,
            callId = call.getCallId(),
            idToken = idTokenProvider.getIdToken(call)
        )

        logger.trace("Søknad registrert.")
        call.respond(HttpStatusCode.Accepted)
    }

    @Location(DELE_DAGER_API_URL)
    class sendMeldingDeleOmsorgsdager

    post { _ : sendMeldingDeleOmsorgsdager ->
        logger.trace("Mottatt ny melding for deling av omsorgsdager. Mapper melding.")
        val melding = call.receive<MeldingDeleOmsorgsdager>()
        logger.trace("Søknad mappet.")

        logger.trace("Oppdaterer barn med fnr")
        val listeOverBarn = barnService.hentNaaverendeBarn(idTokenProvider.getIdToken(call), call.getCallId())
        melding.oppdaterBarnUtvidetMedFnr(listeOverBarn)
        logger.trace("Oppdatering av barn OK")

        logger.trace("Validerer melding.")
        logger.trace("---> {}", melding.toString())
        melding.valider()
        logger.trace("Validering OK. Registrerer melding.")

        meldingDeleOmsorgsdagerService.registrer(
            melding = melding,
            callId = call.getCallId(),
            idToken = idTokenProvider.getIdToken(call)
        )

        logger.trace("Melding registrert.")
        call.respond(HttpStatusCode.Accepted)
    }
}
