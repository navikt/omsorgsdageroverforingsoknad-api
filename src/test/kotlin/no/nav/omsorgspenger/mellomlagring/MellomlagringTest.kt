package no.nav.omsorgspenger.mellomlagring

import com.typesafe.config.ConfigFactory
import io.ktor.config.HoconApplicationConfig
import io.ktor.util.KtorExperimentalAPI
import no.nav.omsorgspenger.Configuration
import no.nav.omsorgspenger.TestConfiguration
import no.nav.omsorgspenger.redis.RedisConfig
import no.nav.omsorgspenger.redis.RedisConfigurationProperties
import no.nav.omsorgspenger.redis.RedisMockUtil
import no.nav.omsorgspenger.redis.RedisStore
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