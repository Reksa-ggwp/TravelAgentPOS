// FILE: app/src/main/java/com/travelagent/pos/repository/RepositoryResult.kt
package com.travelagent.pos.repository

sealed class RepositoryResult<out T> {
    data class Success<T>(val data: T) : RepositoryResult<T>()
    data class Failure(val exception: Exception) : RepositoryResult<Nothing>()
}

// Extension function for easier use
inline fun <T> RepositoryResult<T>.onSuccess(action: (T) -> Unit): RepositoryResult<T> {
    if (this is RepositoryResult.Success) action(data)
    return this
}

inline fun <T> RepositoryResult<T>.onFailure(action: (Exception) -> Unit): RepositoryResult<T> {
    if (this is RepositoryResult.Failure) action(exception)
    return this
}