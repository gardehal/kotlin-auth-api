package grd.kotlin.authapi.services

import com.chook.api.extensions.isNotNull
import com.chook.api.logging.LogFactory
import com.chook.api.logging.ILogService
import com.chook.api.settings.Settings
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
