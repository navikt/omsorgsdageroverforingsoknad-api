package no.nav.omsorgsdageroverforingsoknad.mellomlagring

import com.typesafe.config.ConfigFactory
import io.ktor.config.HoconApplicationConfig
import io.ktor.util.KtorExperimentalAPI
import no.nav.omsorgsdageroverforingsoknad.Configuration
import no.nav.omsorgsdageroverforingsoknad.TestConfiguration
import no.nav.omsorgsdageroverforingsoknad.redis.RedisConfig
import no.nav.omsorgsdageroverforingsoknad.redis.RedisConfigurationProperties
import no.nav.omsorgsdageroverforingsoknad.redis.RedisMockUtil
import no.nav.omsorgsdageroverforingsoknad.redis.RedisStore
import org.junit.AfterClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

@KtorExperimentalAPI
class MellomlagringTest {
    private companion object {
        val redisClient = RedisConfig(RedisConfigurationProperties(true)).redisClient(
            Configuration(
                HoconApplicationConfig(ConfigFactory.parseMap(TestConfiguration.asMap()))
            )
        )
        val redisStore = RedisStore(
            redisClient
        )
        val mellomlagringService = MellomlagringService(
            redisStore,
            "VerySecretPass"
        )

        @AfterClass
        @JvmStatic
        fun teardown() {
            redisClient.shutdown()
            RedisMockUtil.stopRedisMocked()
        }
    }

    @Test
    internal fun `mellomlagre verdier`() {
        mellomlagringService.setMellomlagring("test", "test")

        val mellomlagring = mellomlagringService.getMellomlagring("test")

        assertEquals("test", mellomlagring)
    }

    @Test
    internal fun `verdier skal være krypterte`() {

        mellomlagringService.setMellomlagring("test", "test")

        val mellomlagring = mellomlagringService.getMellomlagring("test")
        assertNotNull(redisStore.get("mellomlagring_test"))
        assertNotEquals(mellomlagring, redisStore.get("test"))
    }

}