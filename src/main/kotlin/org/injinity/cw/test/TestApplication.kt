package org.injinity.cw.test

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.invoke
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
class TestApplication

fun main(args: Array<String>) {
    runApplication<TestApplication>(*args)
}

@Configuration
@EnableWebFluxSecurity
@Profile("prod")
class SecurityConfig {
    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain = http {
        authorizeExchange {
            authorize(anyExchange, authenticated)
        }
        oauth2ResourceServer {
            jwt {}
        }
    }
}

@RestController
@RequestMapping("/test")
class TestController {

    @GetMapping
    fun test(): String = "{\"test\":\"test resource string\"}"
}