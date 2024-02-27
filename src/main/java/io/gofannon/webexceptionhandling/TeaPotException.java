package io.gofannon.webexceptionhandling;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.I_AM_A_TEAPOT, reason = "No more tea")
public class TeaPotException extends RuntimeException {
}
