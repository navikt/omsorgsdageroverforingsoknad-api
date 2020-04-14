package no.nav.omsorgsdageroverforingsoknad.soknadOverforeDager

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import no.nav.omsorgsdageroverforingsoknad.general.auth.IdTokenProvider
import no.nav.omsorgsdageroverforingsoknad.general.getCallId
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("nav.soknadApis")

@KtorExperimentalLocationsAPI
fun Route.søknadApis(
    søknadOverføreDagerService: SøknadOverføreDagerService,
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
}