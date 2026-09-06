package com.mobees.app.ui

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

fun Throwable.userMessage(): String = when (this) {
    is java.net.UnknownHostException -> "You appear to be offline. Check your connection and try again."
    is java.net.SocketTimeoutException -> "The request timed out. Please try again."
    is retrofit2.HttpException -> when (code()) {
        401 -> "The TMDB API key was rejected. Check TMDB_API_KEY."
        404 -> "That title could not be found."
        429 -> "Too many requests. Please wait a moment."
        else -> "TMDB returned an error (${code()})."
    }
    is NoSuchElementException -> message ?: "That title could not be found."
    else -> message?.takeIf { it.isNotBlank() } ?: "Unexpected error."
}
