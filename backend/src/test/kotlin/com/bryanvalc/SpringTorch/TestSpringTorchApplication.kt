package com.bryanvalc.SpringTorch

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
	fromApplication<SpringTorchApplication>().with(TestcontainersConfiguration::class).run(*args)
}
