package org.shandrikov.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.shandrikov.util.StringPool.NO_EMPLOYEE_IN_DB;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = NO_EMPLOYEE_IN_DB)
public class EmployeeNotFoundException extends RuntimeException {
    public EmployeeNotFoundException() {
        super();
    }
}
