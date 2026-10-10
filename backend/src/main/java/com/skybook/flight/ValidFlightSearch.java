package com.skybook.flight;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

/**
 * Cross-field rules for {@link FlightSearchRequest} that single-field annotations can't express.
 * Put on the class (not a field) so the validator can see the whole object, and it reports each
 * problem on the field the client should fix (e.g. "returnDate"), not on the class.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidFlightSearch.Validator.class)
public @interface ValidFlightSearch {

	String message() default "invalid flight search";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	class Validator implements ConstraintValidator<ValidFlightSearch, FlightSearchRequest> {

		@Override
		public boolean isValid(FlightSearchRequest request, ConstraintValidatorContext context) {
			context.disableDefaultConstraintViolation();
			boolean valid = true;

			// Null values are reported by @NotNull/@NotBlank on the fields; only judge complete input here
			if (request.origin() != null && request.origin().equalsIgnoreCase(request.destination())) {
				addViolation(context, "destination", "must be different from origin");
				valid = false;
			}
			if (request.date() != null && request.returnDate() != null
					&& request.returnDate().isBefore(request.date())) {
				addViolation(context, "returnDate", "must not be before date");
				valid = false;
			}
			return valid;
		}

		private static void addViolation(ConstraintValidatorContext context, String field, String message) {
			context.buildConstraintViolationWithTemplate(message)
					.addPropertyNode(field)
					.addConstraintViolation();
		}
	}
}
