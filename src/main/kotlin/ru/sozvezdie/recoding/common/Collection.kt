package ru.sozvezdie.recoding.common

import ru.sozvezdie.recoding.config.Constant

fun stringListOf(vararg args: Any): List<String> = args.map(Any::toString)

fun stringArrayOf(vararg args: Any): Array<String> = stringListOf(*args).toTypedArray()

fun composeKey(vararg args: Any): String = stringListOf(*args).joinToString(Constant.KEY_DELIMITER)
