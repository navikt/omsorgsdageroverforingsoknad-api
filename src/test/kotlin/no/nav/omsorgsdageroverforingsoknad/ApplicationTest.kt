package no.nav.omsorgsdageroverforingsoknad

import com.github.tomakehurst.wiremock.http.Cookie
import com.typesafe.config.ConfigFactory
import io.ktor.config.ApplicationConfig
import io.ktor.config.HoconApplicationConfig
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.createTestEnvironment
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.util.KtorExperimentalAPI
import no.nav.helse.dusseldorf.testsupport.wiremock.WireMockBuilder
import no.nav.helse.getAuthCookie
import no.nav.omsorgsdageroverforingsoknad.meldingDeleOmsorgsdager.DELE_DAGER_API_URL
import no.nav.omsorgsdageroverforingsoknad.meldingDeleOmsorgsdager.DELE_DAGER_MOTTAK_URL
import no.nav.omsorgsdageroverforingsoknad.meldingDeleOmsorgsdager.MAX_ANTALL_DAGER_MAN_KAN_DELE
import no.nav.omsorgsdageroverforingsoknad.meldingDeleOmsorgsdager.MIN_ANTALL_DAGER_MAN_KAN_DELE
import no.nav.omsorgsdageroverforingsoknad.redis.RedisMockUtil
import no.nav.omsorgsdageroverforingsoknad.soknadOverforeDager.MAX_ANTALL_DAGER_MAN_KAN_OVERFØRE
import no.nav.omsorgsdageroverforingsoknad.soknadOverforeDager.MIN_ANTALL_DAGER_MAN_KAN_OVERFØRE
import no.nav.omsorgsdageroverforingsoknad.wiremock.*
import org.junit.AfterClass
import org.junit.BeforeClass
import org.skyscreamer.jsonassert.JSONAssert
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals


private const val fnr = "290990123456"
private const val ikkeMyndigFnr = "12125012345"
private val oneMinuteInMillis = Duration.ofMinutes(1).toMillis()
// Se https://github.com/navikt/dusseldorf-ktor#f%C3%B8dselsnummer
private val gyldigFodselsnummerA = "02119970078"
private val ikkeMyndigDato = "2050-12-12"

@KtorExperimentalAPI
class ApplicationTest {

    private companion object {

        private val logger: Logger = LoggerFactory.getLogger(ApplicationTest::class.java)

        val wireMockServer = WireMockBuilder()
            .withAzureSupport()
            .withNaisStsSupport()
            .withLoginServiceSupport()
            .omsorgsdageroverforingsoknadApiConfig()
            .build()
            .stubK9DokumentHealth()
            .stubOmsorgsoknadMottakHealth()
            .stubOppslagHealth()
            .stubLeggSoknadTilProsessering("v1/soknad/overfore-dager")
            .stubLeggSoknadTilProsessering("v1$DELE_DAGER_MOTTAK_URL")
            .stubK9OppslagSoker()
            .stubK9OppslagBarn()
            .stubK9Dokument()

        fun getConfig(): ApplicationConfig {

            val fileConfig = ConfigFactory.load()
            val testConfig = ConfigFactory.parseMap(TestConfiguration.asMap(wireMockServer = wireMockServer))
            val mergedConfig = testConfig.withFallback(fileConfig)

            return HoconApplicationConfig(mergedConfig)
        }


        val engine = TestApplicationEngine(createTestEnvironment {
            config = getConfig()
        })


        @BeforeClass
        @JvmStatic
        fun buildUp() {
            engine.start(wait = true)
        }

        @AfterClass
        @JvmStatic
        fun tearDown() {
            logger.info("Tearing down")
            wireMockServer.stop()
            RedisMockUtil.stopRedisMocked()
            logger.info("Tear down complete")
        }
    }

    @Test
    fun `test isready, isalive, health og metrics`() {
        with(engine) {
            handleRequest(HttpMethod.Get, "/isready") {}.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                handleRequest(HttpMethod.Get, "/isalive") {}.apply {
                    assertEquals(HttpStatusCode.OK, response.status())
                    handleRequest(HttpMethod.Get, "/metrics") {}.apply {
                        assertEquals(HttpStatusCode.OK, response.status())
                        handleRequest(HttpMethod.Get, "/health") {}.apply {
                            assertEquals(HttpStatusCode.OK, response.status())
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `Henting av barn`() {
        requestAndAssert(
            httpMethod = HttpMethod.Get,
            path = "/barn",
            expectedCode = HttpStatusCode.OK,
            //language=JSON
            expectedResponse = """
            {
                "barn": [{
                    "fødselsdato": "2000-08-27",
                    "fornavn": "BARN",
                    "mellomnavn": "EN",
                    "etternavn": "BARNESEN",
                    "aktørId": "1000000000001"
                }, 
                {
                    "fødselsdato": "2001-04-10",
                    "fornavn": "BARN",
                    "mellomnavn": "TO",
                    "etternavn": "BARNESEN",
                    "aktørId": "1000000000002"
                }]
            }
            """.trimIndent(),
            cookie = getAuthCookie(fnr)
        )
    }

    @Test
    fun `Har ingen registrerte barn`() {
        requestAndAssert(
            httpMethod = HttpMethod.Get,
            path = "/barn",
            expectedCode = HttpStatusCode.OK,
            expectedResponse = """
            {
                "barn": []
            }
            """.trimIndent(),
            cookie = getAuthCookie("07077712345")
        )
    }

    @Test
    fun `Feil ved henting av barn skal returnere tom liste`() {
        wireMockServer.stubK9OppslagBarn(simulerFeil = true)
        requestAndAssert(
            httpMethod = HttpMethod.Get,
            path = "/barn",
            expectedCode = HttpStatusCode.OK,
            expectedResponse = """
            {
                "barn": []
            }
            """.trimIndent(),
            cookie = getAuthCookie(fnr)
        )
        wireMockServer.stubK9OppslagBarn()
    }

    fun expectedGetSokerJson(
        fodselsnummer: String,
        fodselsdato: String = "1997-05-25",
        myndig: Boolean = true
    ) = """
    {
        "etternavn": "MORSEN",
        "fornavn": "MOR",
        "mellomnavn": "HEISANN",
        "fødselsnummer": "$fodselsnummer",
        "aktørId": "12345",
        "fødselsdato": "$fodselsdato",
        "myndig": $myndig
    }
""".trimIndent()

    @Test
    fun `Hente søker`() {
        requestAndAssert(
            httpMethod = HttpMethod.Get,
            path = "/soker",
            expectedCode = HttpStatusCode.OK,
            expectedResponse = expectedGetSokerJson(fnr)
        )
    }

    @Test
    fun `Hente søker for melding av deling av omsorgsdager`() {
        requestAndAssert(
            httpMethod = HttpMethod.Get,
            path = "/sokerMelding",
            expectedCode = HttpStatusCode.OK,
            expectedResponse = expectedGetSokerJson(fnr)
        )
    }

    @Test
    fun `Hente søker som ikke er myndig`() {
        requestAndAssert(
            httpMethod = HttpMethod.Get,
            path = "/soker",
            expectedCode = HttpStatusCode.OK,
            expectedResponse = expectedGetSokerJson(
                fodselsnummer = ikkeMyndigFnr,
                fodselsdato = ikkeMyndigDato,
                myndig = false
            ),
            cookie = getAuthCookie(ikkeMyndigFnr)
        )
    }

    @Test
    fun `Sende overføre-dager ikke myndig`() {
        val cookie = getAuthCookie(ikkeMyndigFnr)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/soknad/overfore-omsorgsdager",
            expectedResponse = """
                {
                    "type": "/problem-details/unauthorized",
                    "title": "unauthorized",
                    "status": 403,
                    "detail": "Søkeren er ikke myndig og kan ikke sende inn søknaden.",
                    "instance": "about:blank"
                }
            """.trimIndent(),
            expectedCode = HttpStatusCode.Forbidden,
            cookie = cookie,
            requestEntity = SøknadOverføreDagerUtils.fullBody()
        )
    }

    @Test
    fun `Sende full gyldig søknad for overføring av dager`(){
        val cookie = getAuthCookie(gyldigFodselsnummerA)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/soknad/overfore-omsorgsdager",
            expectedResponse = null,
            expectedCode = HttpStatusCode.Accepted,
            cookie = cookie,
            requestEntity = SøknadOverføreDagerUtils.fullBody()
        )
    }

    @Test
    fun `Sende full søknad for overføring av dager hvor listen over arbeidssituasjon er tom`(){
        val cookie = getAuthCookie(gyldigFodselsnummerA)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/soknad/overfore-omsorgsdager",
            expectedResponse = """
                    {
                      "type": "/problem-details/invalid-request-parameters",
                      "title": "invalid-request-parameters",
                      "status": 400,
                      "detail": "Requesten inneholder ugyldige paramtere.",
                      "instance": "about:blank",
                      "invalid_parameters": [
                        {
                          "type": "entity",
                          "name": "arbeidssituasjon",
                          "reason": "List over arbeidssituasjon kan ikke være tomt. Må inneholde minst 1 verdi",
                          "invalid_value": []
                        }
                      ]
                    }
            """.trimIndent(),
            expectedCode = HttpStatusCode.BadRequest,
            cookie = cookie,
            requestEntity = SøknadOverføreDagerUtils.fullBody(arbeidssituasjon = listOf())
        )
    }

    @Test
    fun `Sende full søknad for overføring av dager hvor landkode er tom`(){
        val cookie = getAuthCookie(gyldigFodselsnummerA)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/soknad/overfore-omsorgsdager",
            expectedResponse = """
                {
                  "type": "/problem-details/invalid-request-parameters",
                  "title": "invalid-request-parameters",
                  "status": 400,
                  "detail": "Requesten inneholder ugyldige paramtere.",
                  "instance": "about:blank",
                  "invalid_parameters": [
                    {
                      "type": "entity",
                      "name": "Utenlandsopphold[0].landkode",
                      "reason": "Landkode er ikke satt",
                      "invalid_value": "landkode"
                    }
                  ]
                }
            """.trimIndent(),
            expectedCode = HttpStatusCode.BadRequest,
            cookie = cookie,
            requestEntity = SøknadOverføreDagerUtils.fullBodyMedMedlemskap(landkode = "")
        )
    }

    @Test
    fun `Sende full søknad for overføring av dager hvor personnummer for mottaker er ugyldig`(){
        val cookie = getAuthCookie(gyldigFodselsnummerA)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/soknad/overfore-omsorgsdager",
            expectedResponse = """
                {
                  "type": "/problem-details/invalid-request-parameters",
                  "title": "invalid-request-parameters",
                  "status": 400,
                  "detail": "Requesten inneholder ugyldige paramtere.",
                  "instance": "about:blank",
                  "invalid_parameters": [
                    {
                      "type": "entity",
                      "name": "fnrMottaker",
                      "reason": "Ikke gyldig norskIdentifikator på mottaker av dager",
                      "invalid_value": "123456789"
                    }
                  ]
                }
            """.trimIndent(),
            expectedCode = HttpStatusCode.BadRequest,
            cookie = cookie,
            requestEntity = SøknadOverføreDagerUtils.fullBody(fnrMottaker = "123456789")
        )
    }

    @Test
    fun `Sende full søknad for overføring av dager hvor det er flere feil`(){
        val cookie = getAuthCookie(gyldigFodselsnummerA)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/soknad/overfore-omsorgsdager",
            expectedResponse = """
                        {
              "type": "/problem-details/invalid-request-parameters",
              "title": "invalid-request-parameters",
              "status": 400,
              "detail": "Requesten inneholder ugyldige paramtere.",
              "instance": "about:blank",
              "invalid_parameters": [
                {
                  "type": "entity",
                  "name": "arbeidssituasjon",
                  "reason": "List over arbeidssituasjon kan ikke være tomt. Må inneholde minst 1 verdi",
                  "invalid_value": [
                    
                  ]
                },
                {
                  "type": "entity",
                  "name": "Utenlandsopphold[0].landkode",
                  "reason": "Landkode er ikke satt",
                  "invalid_value": "landkode"
                },
                {
                  "type": "entity",
                  "name": "fnrMottaker",
                  "reason": "Ikke gyldig norskIdentifikator på mottaker av dager",
                  "invalid_value": "123456789"
                }
              ]
            }
            """.trimIndent(),
            expectedCode = HttpStatusCode.BadRequest,
            cookie = cookie,
            requestEntity = SøknadOverføreDagerUtils.fullBodyMedMedlemskap(fnrMottaker = "123456789", arbeidssituasjon = listOf(), landkode = "")
        )
    }

    @Test
    fun `Sende full søknad for overføring av dager hvor personnummer for fosterbarn er ugyldig`(){
        val cookie = getAuthCookie(gyldigFodselsnummerA)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/soknad/overfore-omsorgsdager",
            expectedResponse = """
                {
                  "type": "/problem-details/invalid-request-parameters",
                  "title": "invalid-request-parameters",
                  "status": 400,
                  "detail": "Requesten inneholder ugyldige paramtere.",
                  "instance": "about:blank",
                  "invalid_parameters": [
                    {
                      "type": "entity",
                      "name": "fosterbarn[0].fødselsnummer",
                      "reason": "Ikke gyldig fødselsnummer.",
                      "invalid_value": "111"
                    }
                  ]
                }
            """.trimIndent(),
            expectedCode = HttpStatusCode.BadRequest,
            cookie = cookie,
            requestEntity = SøknadOverføreDagerUtils.fullBodyMedMedlemskap(fnrFosterbarn = "111")
        )
    }

    @Test
    fun `Sende full søknad for overføring av dager hvor navn på mottaker ikke er satt`(){
        val cookie = getAuthCookie(gyldigFodselsnummerA)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/soknad/overfore-omsorgsdager",
            expectedResponse = """
                    {
                      "type": "/problem-details/invalid-request-parameters",
                      "title": "invalid-request-parameters",
                      "status": 400,
                      "detail": "Requesten inneholder ugyldige paramtere.",
                      "instance": "about:blank",
                      "invalid_parameters": [
                        {
                          "type": "entity",
                          "name": "navnMottaker",
                          "reason": "Navn på mottaker må være satt, kan ikke være tomt eller blankt",
                          "invalid_value": " "
                        }
                      ]
                    }
            """.trimIndent(),
            expectedCode = HttpStatusCode.BadRequest,
            cookie = cookie,
            requestEntity = SøknadOverføreDagerUtils.fullBodyMedMedlemskap(navnMottaker = " ")
        )
    }

    @Test
    fun `Sende full søknad for overføring av dager hvor dager overført er høyere tillatt verdi`(){
        val cookie = getAuthCookie(gyldigFodselsnummerA)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/soknad/overfore-omsorgsdager",
            expectedResponse = """
                {
                  "type": "/problem-details/invalid-request-parameters",
                  "title": "invalid-request-parameters",
                  "status": 400,
                  "detail": "Requesten inneholder ugyldige paramtere.",
                  "instance": "about:blank",
                  "invalid_parameters": [
                    {
                      "type": "entity",
                      "name": "antallDager",
                      "reason": "Tillatt antall dager man kan overføre må ligge mellom $MIN_ANTALL_DAGER_MAN_KAN_OVERFØRE og $MAX_ANTALL_DAGER_MAN_KAN_OVERFØRE dager.",
                      "invalid_value": 1000
                    }
                  ]
                }
            """.trimIndent(),
            expectedCode = HttpStatusCode.BadRequest,
            cookie = cookie,
            requestEntity = SøknadOverføreDagerUtils.søknadOverføreDager.copy(
                antallDager = 1000
            ).somJson()
        )

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/soknad/overfore-omsorgsdager",
            expectedResponse = """
                {
                  "type": "/problem-details/invalid-request-parameters",
                  "title": "invalid-request-parameters",
                  "status": 400,
                  "detail": "Requesten inneholder ugyldige paramtere.",
                  "instance": "about:blank",
                  "invalid_parameters": [
                    {
                      "type": "entity",
                      "name": "antallDager",
                      "reason": "Tillatt antall dager man kan overføre må ligge mellom $MIN_ANTALL_DAGER_MAN_KAN_OVERFØRE og $MAX_ANTALL_DAGER_MAN_KAN_OVERFØRE dager.",
                      "invalid_value": 0
                    }
                  ]
                }
            """.trimIndent(),
            expectedCode = HttpStatusCode.BadRequest,
            cookie = cookie,
            requestEntity = SøknadOverføreDagerUtils.søknadOverføreDager.copy(
                antallDager = 0
            ).somJson()
        )
    }

    @Test
    fun `Sende gyldig melding om deling av omsorgsdager`(){
        val cookie = getAuthCookie(gyldigFodselsnummerA)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = DELE_DAGER_API_URL,
            expectedResponse = null,
            expectedCode = HttpStatusCode.Accepted,
            cookie = cookie,
            requestEntity = MeldingDeleOmsorgsdagerUtils.fullBody()
        )
    }

    @Test
    fun `Sende ugyldig melding om deling av omsorgsdager`(){
        val cookie = getAuthCookie(gyldigFodselsnummerA)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = DELE_DAGER_API_URL,
            //language=JSON
            expectedResponse = """
                    {
                      "type": "/problem-details/invalid-request-parameters",
                      "title": "invalid-request-parameters",
                      "status": 400,
                      "detail": "Requesten inneholder ugyldige paramtere.",
                      "instance": "about:blank",
                      "invalid_parameters": [
                        {
                          "type": "entity",
                          "name": "harBekreftetOpplysninger",
                          "reason": "Opplysningene må bekreftes for å sende inn søknad.",
                          "invalid_value": false
                        },
                        {
                          "type": "entity",
                          "name": "harForståttRettigheterOgPlikter",
                          "reason": "Må ha forstått rettigheter og plikter for å sende inn søknad.",
                          "invalid_value": false
                        }
                      ]
                    }
            """.trimIndent(),
            expectedCode = HttpStatusCode.BadRequest,
            cookie = cookie,
            requestEntity = MeldingDeleOmsorgsdagerUtils.meldingDeleOmsorgsdager.copy(
                harForståttRettigheterOgPlikter = false,
                harBekreftetOpplysninger = false
            ).somJson()
        )
    }

    @Test
    fun `Sende medling om deling av omsorgsdager hvor søker ikke myndig`() {
        val cookie = getAuthCookie(ikkeMyndigFnr)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = DELE_DAGER_API_URL,
            expectedResponse = """
                {
                    "type": "/problem-details/unauthorized",
                    "title": "unauthorized",
                    "status": 403,
                    "detail": "Søkeren er ikke myndig og kan ikke sende inn søknaden.",
                    "instance": "about:blank"
                }
            """.trimIndent(),
            expectedCode = HttpStatusCode.Forbidden,
            cookie = cookie,
            requestEntity = MeldingDeleOmsorgsdagerUtils.fullBody()
        )
    }

    @Test
    fun `Sende medling om deling av omsorgsdager med flere feil`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = DELE_DAGER_API_URL,
            expectedResponse = """
                {
                  "type": "/problem-details/invalid-request-parameters",
                  "title": "invalid-request-parameters",
                  "status": 400,
                  "detail": "Requesten inneholder ugyldige paramtere.",
                  "instance": "about:blank",
                  "invalid_parameters": [
                    {
                      "type": "entity",
                      "name": "harAleneomsorgFor && harAleneomsorg",
                      "reason": "Dersom harAleneomsorg er true kan ikke harAleneomsorgFor være tom",
                      "invalid_value": [
                        
                      ]
                    },
                    {
                      "type": "entity",
                      "name": "harUtvidetRettFor && harUtvidetRett",
                      "reason": "Dersom harUtvidetRett er true kan ikke harUtvidetRettFor være tom",
                      "invalid_value": [
                        
                      ]
                    },
                    {
                      "type": "entity",
                      "name": "fnrMottaker",
                      "reason": "fnrMottaker er ikke gyldig norsk identifikator",
                      "invalid_value": "ikke gyldig"
                    },
                    {
                      "type": "entity",
                      "name": "antallDagerTilOverføre",
                      "reason": "antallDagerTilOverføre må være mellom $MIN_ANTALL_DAGER_MAN_KAN_DELE og ${MAX_ANTALL_DAGER_MAN_KAN_DELE}",
                      "invalid_value": -1
                    }
                  ]
                }
            """.trimIndent(),
            expectedCode = HttpStatusCode.BadRequest,
            cookie = cookie,
            requestEntity = MeldingDeleOmsorgsdagerUtils.meldingDeleOmsorgsdager.copy(
                harAleneomsorg = true,
                harAleneomsorgFor = listOf(),
                harUtvidetRett = true,
                harUtvidetRettFor = listOf(),
                antallDagerTilOverføre = -1,
                fnrMottaker = "ikke gyldig"
            ).somJson()
        )
    }

   private fun requestAndAssert(
        httpMethod: HttpMethod,
        path: String,
        requestEntity: String? = null,
        expectedResponse: String?,
        expectedCode: HttpStatusCode,
        leggTilCookie: Boolean = true,
        cookie: Cookie = getAuthCookie(fnr)
    ) {
        with(engine) {
            handleRequest(httpMethod, path) {
                if (leggTilCookie) addHeader(HttpHeaders.Cookie, cookie.toString())
                logger.info("Request Entity = $requestEntity")
                addHeader(HttpHeaders.Accept, "application/json")
                if (requestEntity != null) addHeader(HttpHeaders.ContentType, "application/json")
                if (requestEntity != null) setBody(requestEntity)
            }.apply {
                logger.info("Response Entity = ${response.content}")
                logger.info("Expected Entity = $expectedResponse")
                assertEquals(expectedCode, response.status())
                if (expectedResponse != null) {
                    JSONAssert.assertEquals(expectedResponse, response.content!!, true)
                } else {
                    assertEquals(expectedResponse, response.content)
                }
            }
        }
    }
}
