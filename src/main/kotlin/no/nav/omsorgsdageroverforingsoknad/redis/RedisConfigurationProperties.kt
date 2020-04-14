package no.nav.omsorgsdageroverforingsoknad.redis

import no.nav.omsorgsdageroverforingsoknad.redis.RedisMockUtil.startRedisMocked

class RedisConfigurationProperties(private val redisMocked: Boolean) {

    fun startInMemoryRedisIfMocked() {
        if (redisMocked) {
            startRedisMocked()
        }
    }
}