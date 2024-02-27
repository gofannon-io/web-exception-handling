package io.gofannon.webexceptionhandling;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class MyWebController {

    @GetMapping(value = "/hello", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getHello() {
        return """
                Hello you !
                """;
    }

    @GetMapping("/forbidden")
    public String getForbidden() {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You shall not pass !");
    }

    @GetMapping(value = "/secret1", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getSecret1() {
        return "This is a secret 1";
    }

    @GetMapping(value = "/secret2", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getSecret2() {
        return "This is a secret 2";
    }

    @GetMapping(value = "/trouble", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getTrouble() {
        throw new IllegalArgumentException("Oups, I didn't expect this trouble");
    }

    @GetMapping(value = "/teatime", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getTeaTime() {
        throw new TeaPotException();
    }
}
