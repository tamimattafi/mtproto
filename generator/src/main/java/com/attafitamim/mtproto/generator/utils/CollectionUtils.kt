package com.attafitamim.mtproto.generator.utils

fun <T : Collection<Any>> T.takeIfSingleElement() = takeIf { collection ->
    collection.size == 1
}