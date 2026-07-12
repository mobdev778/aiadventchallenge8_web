package com.github.mobdev778.aiadventchallenge.web

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication(
    scanBasePackages = ["com.github.mobdev778.aiadventchallenge"]
)
@EnableAsync
@EnableJpaRepositories(
    basePackages = ["com.github.mobdev778.aiadventchallenge.domain.chat.persistence"]
)
@EntityScan(
    basePackages = ["com.github.mobdev778.aiadventchallenge.domain.chat.persistence"]
)
class WebApplication

fun main(args: Array<String>) {
    runApplication<WebApplication>(*args)
}
