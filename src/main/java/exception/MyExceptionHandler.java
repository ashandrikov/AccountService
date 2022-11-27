package exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.util.Set;

@ControllerAdvice
public class MyExceptionHandler {
    @ExceptionHandler(ConstraintViolationException.class)
    public void handleConstraintViolationException(ConstraintViolationException ex, HttpServletResponse response)
            throws IOException {
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        StringBuilder message = new StringBuilder();
        for (ConstraintViolation violation : violations) {
            message.append(violation.getMessage()).append(", ");
        }
        response.sendError(HttpStatus.BAD_REQUEST.value(), (message.substring(0, message.length() - 2)));
    }

    @ExceptionHandler(JsonProcessingException.class)
    public void handleInvalidJson(HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.BAD_REQUEST.value(), "Invalid JSON");
    }
}
