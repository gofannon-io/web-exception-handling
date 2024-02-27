/*
 * Copyright (c) 2024. gwenlr
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.gofannon.webexceptionhandling;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class MyWebController {

    @GetMapping(value = "/hello", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getHello() {
        return "Hello you !";
    }

    @GetMapping("/forbidden")
    public String getForbidden() {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You shall not pass !");
    }

    @GetMapping(value = "/secret", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getSecret() {
        return "This is a secret";
    }

    @GetMapping(value = "/private", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getPrivate() {
        return "This is private";
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
