package no.nav.omsorgsdageroverforingsoknad.soker

import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import no.nav.omsorgsdageroverforingsoknad.general.auth.IdTokenProvider
import no.nav.omsorgsdageroverforingsoknad.general.getCallId
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("nav.sokerApis")


@KtorExperimentalLocationsAPI
fun Route.søkerApis(
    søkerService: SøkerService,
    idTokenProvider: IdTokenProvider
) {

    @Location("/soker")
    class getSoker
    get { _: getSoker ->
        call.respond(
            søkerService.getSoker(
                idToken = idTokenProvider.getIdToken(call),
                callId = call.getCallId()
            )
        )
    }

    @Location("/sokerMelding")
    class getSokerMelding
    get { _: getSokerMelding ->
        call.respond(
            søkerService.getSoker(
                idToken = idTokenProvider.getIdToken(call),
                callId = call.getCallId()
            )
        )
    }
}
