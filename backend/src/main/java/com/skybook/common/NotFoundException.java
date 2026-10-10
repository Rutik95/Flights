package com.skybook.common;

/**
 * A requested resource doesn't exist (mapped to 404 by {@link GlobalExceptionHandler}).
 * Services throw domain exceptions like this one and stay unaware of HTTP.
 */
public class NotFoundException extends RuntimeException {

	public NotFoundException(String message) {
		super(message);
	}
}
