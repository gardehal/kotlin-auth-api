package grd.kotlin.authapi.services

import grd.kotlin.authapi.extensions.isNotNull
import grd.kotlin.authapi.logging.LogFactory
import grd.kotlin.authapi.logging.ILogService
import grd.kotlin.authapi.settings.Settings
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class LogService
{
    @Autowired
    lateinit var settings: Settings
    @Autowired
    lateinit var factory: LogFactory

    lateinit var logger: ILogService

    @PostConstruct
    fun initialize()
    {
        if(settings.logging.logSink.isNotNull())
            logger = factory.createFromLogSink(settings.logging.logSink!!)
    }
}
