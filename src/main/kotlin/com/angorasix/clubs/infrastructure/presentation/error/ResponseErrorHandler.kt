package com.angorasix.clubs.infrastructure.presentation.error

import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
suspend fun resolveExceptionResponse(ex: Exception, element: String? = "Element"): ServerResponse =
        when (ex) {
            is IllegalArgumentException -> ServerResponse.badRequest().bodyValueAndAwait(ErrorResponseBody(HttpStatus.BAD_REQUEST, ex.javaClass.simpleName, element, ex.message))
            else -> ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).bodyValueAndAwait(ErrorResponseBody(HttpStatus.INTERNAL_SERVER_ERROR, ex.javaClass.simpleName, element, ex.message))
        }

suspend fun resolveNotFound(message: String? = "Element not found", element: String? = "Element"): ServerResponse = ServerResponse.status(HttpStatus.NOT_FOUND).bodyValueAndAwait(ErrorResponseBody(HttpStatus.NOT_FOUND, "ElementNotFound", element, message))
suspend fun resolveBadRequest(message: String? = "Element is invalid", element: String? = "Element"): ServerResponse = ServerResponse.status(HttpStatus.BAD_REQUEST).bodyValueAndAwait(ErrorResponseBody(HttpStatus.BAD_REQUEST, "ElementInvalid", element, message))

data class ErrorResponseBody private constructor(
        val status: Int = HttpStatus.INTERNAL_SERVER_ERROR.value(),
        val error: String? = "",
        val errorCode: String? = "UNCAUGHT_ERROR",
        val message: String? = "",
) {
    constructor(status: HttpStatus, error: String?, element: String?, message: String?) : this(status.value(), error, resolveErrorCode(status, element), message ?: resolveErrorMessage(status, element))
}

private fun resolveErrorCode(status: HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR, element: String? = "ELEMENT"): String = when (status) {
    HttpStatus.BAD_REQUEST -> "${element?.replace(" ", "_")?.uppercase()}_INVALID"
    HttpStatus.NOT_FOUND -> "${element?.replace(" ", "_")?.uppercase()}_NOT_FOUND"
    else -> "${element?.replace(" ", "_")?.uppercase()}_ERROR"
}

private fun resolveErrorMessage(status: HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR, element: String? = "Element"): String = when (status) {
    HttpStatus.BAD_REQUEST -> "$element is invalid"
    HttpStatus.NOT_FOUND -> "$element not found"
    else -> "error with $element"
}