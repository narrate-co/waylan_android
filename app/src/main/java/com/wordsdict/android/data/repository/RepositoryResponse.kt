package com.wordsdict.android.data.repository

import kotlin.reflect.KClass


sealed class RepositoryResponse<T>(val type: KClass<out WordSource>)

class RepositoryEmptyResponse<T>(type: KClass<out WordSource>) : RepositoryResponse<T>(type)

class RepositoryErrorResponse<T>(type: KClass<out WordSource>, val errorMessage: String): RepositoryResponse<T>(type)

class RepositorySuccessResponse<T>(type: KClass<out WordSource>, val data: T) : RepositoryResponse<T>(type)
