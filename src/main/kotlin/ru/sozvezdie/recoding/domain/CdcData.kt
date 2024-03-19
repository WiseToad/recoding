package ru.sozvezdie.recoding.domain

class CdcData<T>(
    val before: T?,
    val after: T?,
    val op: String,

    val data: T = after ?: before ?: throw IllegalArgumentException("The CDC data contains neither 'after' nor 'before' record state")
)
