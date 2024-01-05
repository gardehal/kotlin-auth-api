package grd.kotlin.authapi.services

import grd.kotlin.authapi.models.System
import org.springframework.stereotype.Service

@Service
class SystemService : BaseService<System>(System::class.java, true)
