package net.averkhoglyad.grex.arrow.core.util

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

// Log4j
interface Log4j<R> {
    operator fun getValue(thisRef: R, property: KProperty<*>): Logger
}

fun log4j(): Log4j<Any> = object : Log4j<Any> {
    override operator fun getValue(thisRef: Any, property: KProperty<*>): Logger = LogManager.getLogger(thisRef::class.java)
}

fun log4j(name: String): Log4j<Any?> = object : Log4j<Any?> {
    override operator fun getValue(thisRef: Any?, property: KProperty<*>): Logger = LogManager.getLogger(name)
}

fun log4j(clazz: KClass<*>): Log4j<Any?> = object : Log4j<Any?> {
    override operator fun getValue(thisRef: Any?, property: KProperty<*>): Logger = LogManager.getLogger(clazz.java)
}
