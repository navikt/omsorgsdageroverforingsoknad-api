package no.nav.omsorgsdageroverforingsoknad.general.auth

data class ApiGatewayApiKey(val value : String, val headerKey : String = "x-nav-apiKey")