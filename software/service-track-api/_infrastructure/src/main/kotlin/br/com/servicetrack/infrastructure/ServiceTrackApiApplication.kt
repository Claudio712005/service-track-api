package br.com.servicetrack.infrastructure

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ServiceTrackApiApplication

fun main(args: Array<String>) {
    runApplication<ServiceTrackApiApplication>(*args)
}
