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

// Add fold function for more functional approach
inline fun <T, R> RepositoryResult<T>.fold(
    onSuccess: (T) -> R,
    onFailure: (Exception) -> R
): R {
    return when (this) {
        is RepositoryResult.Success -> onSuccess(data)
        is RepositoryResult.Failure -> onFailure(exception)
    }
}