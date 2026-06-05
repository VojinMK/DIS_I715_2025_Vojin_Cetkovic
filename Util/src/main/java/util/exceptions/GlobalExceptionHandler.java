package util.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoDataFoundException.class)
    public ResponseEntity<?> handleNoDataFound(NoDataFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ExceptionModel(
                        ex.getMessage(),
                        "Check whether the requested resource exists.",
                        HttpStatus.NOT_FOUND
                ));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<?> handleConflict(ConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ExceptionModel(
                        ex.getMessage(),
                        "Check whether the resource already exists or use different data.",
                        HttpStatus.CONFLICT
                ));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> handleMissingParams(MissingServletRequestParameterException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ExceptionModel(
                        ex.getMessage(),
                        "Please provide all required request parameters.",
                        HttpStatus.BAD_REQUEST
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneralException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ExceptionModel(
                        "An internal error occurred.",
                        "Check application logs or contact support if the issue persists.",
                        HttpStatus.INTERNAL_SERVER_ERROR
                ));
    }
    
    @ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<?> handleInvalidInput(DataIntegrityViolationException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ExceptionModel(ex.getMessage(),
				"Please enter valid input.", HttpStatus.BAD_REQUEST));
	}
}