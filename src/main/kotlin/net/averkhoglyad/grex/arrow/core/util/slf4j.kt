package net.averkhoglyad.grex.arrow.core.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

// Slf4j
interface Slf4j<R> {
    operator fun getValue(thisRef: R, property: KProperty<*>): Logger
}

fun slf4j(): Slf4j<Any> = object : Slf4j<Any> {
    override operator fun getValue(thisRef: Any, property: KProperty<*>): Logger = LoggerFactory.getLogger(thisRef::class.java)
}

fun slf4j(name: String): Slf4j<Any?> = object : Slf4j<Any?> {
    override operator fun getValue(thisRef: Any?, property: KProperty<*>): Logger = LoggerFactory.getLogger(name)
}

fun slf4j(clazz: KClass<*>): Slf4j<Any?> = object : Slf4j<Any?> {
    override operator fun getValue(thisRef: Any?, property: KProperty<*>): Logger = LoggerFactory.getLogger(clazz.java)
}
