package com.tastecompass

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TasteCompassApplication

fun main(args: Array<String>) {
	runApplication<TasteCompassApplication>(*args)
}
