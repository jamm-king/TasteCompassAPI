package com.service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(basePackages = ["com.service", "com.config"])
class TasteCompassApplication

fun main(args: Array<String>) {
	val context = runApplication<TasteCompassApplication>(*args)
	startController(context)
}

private fun startController(context: ApplicationContext) {
	val mainController = context.getBean(MainController::class.java)
	mainController.start()
}
