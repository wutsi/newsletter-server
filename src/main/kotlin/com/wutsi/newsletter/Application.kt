package com.wutsi.newsletter

import com.wutsi.platform.EnableWutsiCore
import com.wutsi.platform.EnableWutsiEmail
import com.wutsi.platform.EnableWutsiSecurity
import com.wutsi.platform.EnableWutsiSite
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity

@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWutsiCore
@EnableWutsiSecurity
@EnableWutsiSite
@EnableWutsiEmail
public class Application

public fun main(vararg args: String) {
    org.springframework.boot.runApplication<Application>(*args)
}
