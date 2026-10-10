package com.skybook.common;

/**
 * The request is well-formed but breaks a business rule that only the service can check,
 * e.g. an airport code that doesn't exist (mapped to 400 by {@link GlobalExceptionHandler}).
 */
public class InvalidRequestException extends RuntimeException {

	public InvalidRequestException(String message) {
		super(message);
	}
}
