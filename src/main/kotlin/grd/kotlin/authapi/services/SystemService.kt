package grd.kotlin.authapi.services

import grd.kotlin.authapi.exceptions.*
import grd.kotlin.authapi.models.System
import org.springframework.context.annotation.DependsOn
import org.springframework.stereotype.Service

@Service
@DependsOn("FirebaseInitialize")
class SystemService : BaseService<System>(System::class.java, true)
{
}
