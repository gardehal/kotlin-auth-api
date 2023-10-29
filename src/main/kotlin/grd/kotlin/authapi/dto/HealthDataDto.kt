package grd.kotlin.authapi.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Duration
import java.time.Instant
import javax.validation.constraints.NotNull

data class HealthDataDto(
    @Schema(description = "Datetime now")
    @NotNull
    var now: Instant? = null,

    @Schema(description = "Datetime API was started at")
    @NotNull
    var apiUpAt: Instant? = null,

    @Schema(description = "Uptime of API")
    @NotNull
    var apiUptime: Duration? = null,

    @Schema(description = "Call to database Firebase in in duration it took to respond")
    @NotNull
    var dbCallFirebase: Duration? = null,

    @Schema(description = "Call to scraper target NRK in duration it took to respond")
    @NotNull
    var scraperCallNrk: Duration? = null,

    @Schema(description = "Call to scraper target Kolonial in duration it took to respond")
    @NotNull
    var scraperCallKolonial: Duration? = null,

    @Schema(description = "Health messages")
    @NotNull
    var messages: MutableList<String>? = null,
)
