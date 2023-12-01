package grd.kotlin.authapi.controllers

import grd.kotlin.authapi.dto.*
import grd.kotlin.authapi.exceptions.*
import grd.kotlin.authapi.models.System
import grd.kotlin.authapi.services.BaseService
import org.springframework.web.bind.annotation.*

@RequestMapping("/system")
@RestController
class SystemController : BaseController<System, SystemDto, BaseService<System>>(System::class.java, SystemDto::class.java)
