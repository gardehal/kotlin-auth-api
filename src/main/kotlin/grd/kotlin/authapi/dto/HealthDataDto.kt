package grd.kotlin.authapi.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Duration
import java.time.Instant

data class HealthDataDto(
    @Schema(description = "Datetime now")
    var now: Instant? = null,

    @Schema(description = "Datetime API was started at")
    var apiUpAt: Instant? = null,

    @Schema(description = "Uptime of API")
    var apiUptime: Duration? = null,

    @Schema(description = "Call to database Firebase in in duration it took to respond")
    var dbCallFirebase: Duration? = null,

    @Schema(description = "Call to scraper target NRK in duration it took to respond")
    var scraperCallNrk: Duration? = null,

    @Schema(description = "Call to scraper target Kolonial in duration it took to respond")
    var scraperCallKolonial: Duration? = null,

    @Schema(description = "Health messages")
    var messages: MutableList<String>? = null,
)
