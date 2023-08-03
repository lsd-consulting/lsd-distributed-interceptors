package io.lsdconsulting.lsd.distributed.interceptor.config

import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

class ApplicationContextProvider : ApplicationContextAware {
    @Throws(BeansException::class)
    override fun setApplicationContext(ctx: ApplicationContext) {
        context = ctx
    }

    companion object {
        lateinit var context: ApplicationContext
            private set
    }
}
