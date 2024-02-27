# Etude d'une solution globale de gestion des erreurs REST avec SpringBoot  

## Introduction

### Contexte
Basé sur HTTP, le protocole REST retourne, en cas d'erreur, un code indiquant la nature générale du problème (4xx et 5xx) et, au besoin, des informations complémentaires dans le corps de la réponse.

En Java comme en Kotlin, les erreurs sont gérées par le mécanisme d'exception.

Dans le cadre de développement de services REST avec le framework SpringBoot, différentes approches non exclusives sont proposées, parmi lesquelles :
* Intersection de toutes les exceptions directement au sein de la méthode requêtée du *@Controller* avec conversion ad-hoc des exceptions en réponse HTTP.
* Levée d'exceptions intégrant des informations de conversion en réponse HTTP:
  * Levée d'une exception ErrorResponseException (dont ResponseStatusException)
  * Levée d'une exception annotée avec *@ResponseStatus*.
* Interception de toutes les exceptions des méthodes requêtées du *@Controller* au sein de sa propre classe et conversion de l'exception en une réponse HTTP.
* Interception de toutes les exceptions des méthodes requêtées dans tous les *@Controller* au sein d'une classe dédiée implémentant *HandlerExceptionResolver* et conversion de l'exception en réponse HTTP.

Spring fournit plusieurs approches web. 
* La première, implémentée dans *spring-web*, utilise des mécanismes de base (Servlet, REST, etc.).
* La seconde, implémentée dans *spring-webmvc*, est Spring MVC (Model-View-Controller) qui est dédiée au développement d'application avec une UI.
Du fait de sa nature, l'approche MVC est moins adaptée, car plus spécialisée et porteuse d'une architecture dédiée. 


### Objectif
L'objectif de ce document est d'étudier chacune des approches et de trouver une solution globale permettant de traiter différents cas d'utilisation :
* [UC1] *Mutualisation du code* afin d'éviter de la duplication de code.
* [UC2] *Extensibilité et souplesse* afin de supporter les différentes erreurs des codes existants. 
* [UC3] *Gestion des exceptions non prévues* afin de prendre en compte tous les cas possibles et empêcher le serveur REST de tomber.
* [UC4] *Construction personnalisée des réponses* afin de respecter les normes choisies ou les demandes spécifiques du client de l'interface.


## Approches
La première approche est d'effectuer l'interception de toutes les exceptions, même l'exception de base (*RuntimeException*), au sein de la méthode requêtée et de les convertir en réponses HTTP.
Si cette approche couvre bien tous les cas d'erreur, elle nécessite de la redite et des méthodes requêtées de taille conséquente. 

D'autres approches sont possibles, mais elles doivent satisfaire tous les cas d'utilisation précédemment cités.
Aucune des méthodes techniques ne couvre l'ensemble des cas, mais en combinant plusieurs techniques, il est peut-être possible de trouver une solution pour couvrir une majorité, voire la totalité des cas.



## Cas nominal
Le cas consiste en une méthode requêtée qui ne provoque aucune erreur.

La méthode requêtée est la suivante :
```java
    @GetMapping(value = "/hello", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getHello() {
        return """
                Hello you !
                """;
    }
```

La commande du client est :
```shell
$ curl -s -v http://localhost:9000/home/hello
```

La réponse du serveur est la suivante :
```text
Hello you !
* Host localhost:9000 was resolved.
* IPv6: ::1
* IPv4: 127.0.0.1
*   Trying [::1]:9000...
* Connected to localhost (::1) port 9000
> GET /home/hello HTTP/1.1
> Host: localhost:9000
> User-Agent: curl/8.6.0
> Accept: */*
>
< HTTP/1.1 200
< Content-Type: text/plain;charset=UTF-8
< Content-Length: 11
< Date: Tue, 27 Feb 2024 19:20:56 GMT
<
{ [11 bytes data]
* Connection #0 to host localhost left intact
```


## Levée d'une exception ResponseStatusException
Cette approche consiste à lever une exception de type *ResponseStatusException* qui accepte en paramètre plusieurs critères liés à une réponse HTTP:
* code d'erreur HTTP
* message d'erreur
* configuration du header de la réponse HTTP

La méthode requêtée est la suivante :
```java
    @GetMapping("/forbidden")
    public String getForbidden() {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You shall not pass !");
    }
```

La commande du client est :
```shell
$ curl -s -v http://localhost:9000/home/forbidden
```

La réponse du serveur est la suivante :
```text
{"timestamp":"2024-02-27T19:53:20.531+00:00","status":403,"error":"Forbidden","trace":"org.springframework.web.server.ResponseStatusException: 403 FORBIDDEN \"You shall not pass !\"\r\n\tat io.gofannon.webexceptionhandling.MyWebController.getForbidden(MyWebController.java:35)\r\n\tat java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103)\r\n\tat java.base/java.lang.reflect.Method.invoke(Method.java:580)\r\n\tat org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:259)\r\n\tat org.springframework.web.method.support.InvocableHandlerMethod.invokeForRequest(InvocableHandlerMethod.java:192)\r\n\tat org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod.invokeAndHandle(ServletInvocableHandlerMethod.java:118)\r\n\tat org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.invokeHandlerMethod(RequestMappingHandlerAdapter.java:920)\r\n\tat org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.handleInternal(RequestMappingHandlerAdapter.java:830)\r\n\tat org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter.handle(AbstractHandlerMethodAdapter.java:87)\r\n\tat org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:1089)\r\n\tat org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:979)\r\n\tat org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:1014)\r\n\tat org.springframework.web.servlet.FrameworkServlet.doGet(FrameworkServlet.java:903)\r\n\tat jakarta.servlet.http.HttpServlet.service(HttpServlet.java:564)\r\n\tat org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:885)\r\n\tat jakarta.servlet.http.HttpServlet.service(HttpServlet.java:658)\r\n\tat org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:205)\r\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:149)\r\n\tat org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:51)\r\n\tat org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:174)\r\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:149)\r\n\tat io.gofannon.webexceptionhandling.MyFilter.doFilter(MyFilter.java:36)\r\n\tat org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:174)\r\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:149)\r\n\tat org.springframework.web.filter.RequestContextFilter.doFilterInternal(RequestContextFilter.java:100)\r\n\tat org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)\r\n\tat org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:174)\r\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:149)\r\n\tat org.springframework.web.filter.FormContentFilter.doFilterInternal(FormContentFilter.java:93)\r\n\tat org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)\r\n\tat org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:174)\r\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:149)\r\n\tat org.springframework.web.filter.CharacterEncodingFilter.doFilterInternal(CharacterEncodingFilter.java:201)\r\n\tat org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)\r\n\tat org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:174)\r\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:149)\r\n\tat org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:167)\r\n\tat org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:90)\r\n\tat org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:482)\r\n\tat org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:115)\r\n\tat org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:93)\r\n\tat org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:74)\r\n\tat org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:344)\r\n\tat org.apache.coyote.http11.Http11Processor.service(Http11Processor.java:391)\r\n\tat org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:63)\r\n\tat org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:896)\r\n\tat org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1744)\r\n\tat org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:52)\r\n\tat org.apache.tomcat.util.threads.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1191)\r\n\tat org.apache.tomcat.util.threads.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:659)\r\n\tat org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:63)\r\n\tat java.base/java.lang.Thread.run(Thread.java:1583)\r\n","message":"You shall not pass !","path":"/home/forbidden"}* Host localhost:9000 was resolved.
* IPv6: ::1
* IPv4: 127.0.0.1
*   Trying [::1]:9000...
* Connected to localhost (::1) port 9000
> GET /home/forbidden HTTP/1.1
> Host: localhost:9000
> User-Agent: curl/8.6.0
> Accept: */*
>
< HTTP/1.1 403
< Content-Type: application/json
< Transfer-Encoding: chunked
< Date: Tue, 27 Feb 2024 19:53:20 GMT
<
{ [5360 bytes data]
* Leftovers after chunking: 5 bytes
* Connection #0 to host localhost left intact
```
Cette réponse fournit des résultats cohérents et attendus (dont le code HTTP), elle contient néanmoins trop d'informations que le développeur n'a pas choisies.
Ainsi, la stacktrace constitue une faille de sécurité.
L'usage de *ResponseStatusException* seul n'est donc pas satisfaisant.  


Un code supplémentaire est nécessaire pour filtrer et réorganiser l'information contenue dans la réponse.
Il s'agit d'intercepter les exceptions de type *ResponseStatusException* et de construire une réponse HTTP basée sur le contenu de l'exception.

Le code suivant est ajouté :
````java
@ControllerAdvice
public class MyExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handleResponseStatusException(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(""" { "message": "%s" } """.formatted(ex.getReason()).trim());
    }
}
````

Cette fois-ci, la réponse retournée est :
```text
{
    "message": "You shall not pass !"
}
* Host localhost:9000 was resolved.
* IPv6: ::1
* IPv4: 127.0.0.1
*   Trying [::1]:9000...
* Connected to localhost (::1) port 9000
> GET /home/forbidden HTTP/1.1
> Host: localhost:9000
> User-Agent: curl/8.6.0
> Accept: */*
>
< HTTP/1.1 403
< Content-Type: application/json
< Content-Length: 41
< Date: Tue, 27 Feb 2024 19:55:28 GMT
<
{ [41 bytes data]
* Connection #0 to host localhost left intact
```
La réponse est en adéquation avec le résultat voulu.
Le code HTTP est toujours le bon, et le corps de la réponse est simplement celui fournit dans l'attribut *message* de l'exception levée.



## Exceptions levées dans servlet Filter

Le principe est, au sein d'un filtre *jakarta.servlet.Filter*, de lever une exception et de tenter de l'intercepter via *ResponseEntityExceptionHandler*.

Le code java est le suivant :
```java
@Component
public class MyFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        if (request.getRequestURI().toLowerCase().contains("private")) {
            throw new PrivateAccessException("Stop ! This access is private");
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }
}

////////////////////////////////

@ControllerAdvice
public class MyExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(PrivateAccessException.class)
    public ResponseEntity<String> handlePrivateAccessException(PrivateAccessException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                            "message": "%s"
                        }
                        """.formatted(ex.getMessage()).trim());
    }
}
```

La commande du client est :
```shell
$ curl -s -v http://localhost:9000/home/private
```

La réponse du serveur est :
```text
{"timestamp":"2024-02-27T20:23:02.009+00:00","status":500,"error":"Internal Server Error","trace":"io.gofannon.webexceptionhandling.PrivateAccessException: Stop ! This end-point is private\r\n\tat io.gofannon.webexceptionhandling.MyFilter.doFilter(MyFilter.java:33)\r\n\tat org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:174)\r\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:149)\r\n\tat org.springframework.web.filter.RequestContextFilter.doFilterInternal(RequestContextFilter.java:100)\r\n\tat org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)\r\n\tat org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:174)\r\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:149)\r\n\tat org.springframework.web.filter.FormContentFilter.doFilterInternal(FormContentFilter.java:93)\r\n\tat org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)\r\n\tat org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:174)\r\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:149)\r\n\tat org.springframework.web.filter.CharacterEncodingFilter.doFilterInternal(CharacterEncodingFilter.java:201)\r\n\tat org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)\r\n\tat org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:174)\r\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:149)\r\n\tat org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:167)\r\n\tat org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:90)\r\n\tat org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:482)\r\n\tat org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:115)\r\n\tat org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:93)\r\n\tat org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:74)\r\n\tat org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:344)\r\n\tat org.apache.coyote.http11.Http11Processor.service(Http11Processor.java:391)\r\n\tat org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:63)\r\n\tat org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:896)\r\n\tat org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1744)\r\n\tat org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:52)\r\n\tat org.apache.tomcat.util.threads.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1191)\r\n\tat org.apache.tomcat.util.threads.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:659)\r\n\tat org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:63)\r\n\tat java.base/java.lang.Thread.run(Thread.java:1583)\r\n","message":"Stop ! This end-point is private","path":"/home/private"}* Host localhost:9000 was resolved.
* IPv6: ::1
* IPv4: 127.0.0.1
*   Trying [::1]:9000...
* Connected to localhost (::1) port 9000
> GET /home/private HTTP/1.1
> Host: localhost:9000
> User-Agent: curl/8.6.0
> Accept: */*
>
< HTTP/1.1 500
< Content-Type: application/json
< Transfer-Encoding: chunked
< Date: Tue, 27 Feb 2024 20:23:02 GMT
< Connection: close
<
{ [3199 bytes data]
* Leftovers after chunking: 5 bytes
* Closing connection
```

Cela signifie que les exceptions levées dans les instances de *Filter* ne sont pas interceptées par le mécanisme *ResponseEntityExceptionHandler*.



## Exceptions levées par Interceptor (MVC)
Le principe est, au sein d'un interceptor *org.springframework.web.servlet.HandlerInterceptor*, de lever une exception et de tenter de l'intercepter via *ResponseEntityExceptionHandler*.

Le code Java est le suivant :
```java
@RestController
public class MyWebController {
  
    @GetMapping(value = "/secret", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getSecret() {
        return "This is a secret";
    }

  // [...]
}

/////////////////////////////
@Component
public class MyInterceptor implements HandlerInterceptor    {

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    if (request.getRequestURI().toLowerCase().contains("secret")) {
      throw new PrivateAccessException("STOP ! This access is forbidden");
    }

    return HandlerInterceptor.super.preHandle(request, response, handler);
  }

  @Override
  public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
  }
}

//////////////
@Configuration
public class MyConfig implements WebMvcConfigurer {

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new MyInterceptor());
  }
}
```


La commande du client est :
```shell
$ curl -s -v http://localhost:9000/home/secret
```

La réponse du serveur est :
```text
{
    "message": "STOP ! This end-point is secret"
}
* Host localhost:9000 was resolved.
* IPv6: ::1
* IPv4: 127.0.0.1
*   Trying [::1]:9000...
* Connected to localhost (::1) port 9000
> GET /home/secret HTTP/1.1
> Host: localhost:9000
> User-Agent: curl/8.6.0
> Accept: */*
>
< HTTP/1.1 401
< Content-Type: application/json
< Content-Length: 52
< Date: Tue, 27 Feb 2024 20:27:18 GMT
<
{ [52 bytes data]
* Connection #0 to host localhost left intact
```

## Exception non traitée
Il s'agit d'intercepter toutes les exceptions non traitées spécifiquement.
Le principe est d'ajouter un `@ExceptionHandler(Exception.class)` en *toute fin* de la classe `@ControllerAdvice`.

Le code Java est le suivant :
```java
@RestController
public class MyWebController {
    
  @GetMapping(value = "/trouble", produces = MediaType.TEXT_PLAIN_VALUE)
  public String getUnexpectedTrouble() {
    throw new IllegalArgumentException("Oups, I didn't expect this trouble");
  }
}

////////////////////////

@ControllerAdvice
public class MyExceptionHandler extends ResponseEntityExceptionHandler {

    // [...]

  @ExceptionHandler(Exception.class)
  public ResponseEntity<String> handleRemainingException(Exception ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .contentType(MediaType.APPLICATION_JSON)
            .body("""
                    {
                        "message": "Oups, Houston, we have a problem"
                    }
                    """.trim());
  }
}
```

La commande du client est :
```shell
$ curl -s -v http://localhost:9000/home/trouble
```

La réponse du serveur est :
```text
{
    "message": "Oups, Houston, we have a problem"
}
* Host localhost:9000 was resolved.
* IPv6: ::1
* IPv4: 127.0.0.1
*   Trying [::1]:9000...
* Connected to localhost (::1) port 9000
> GET /home/trouble HTTP/1.1
> Host: localhost:9000
> User-Agent: curl/8.6.0
> Accept: */*
>
< HTTP/1.1 500
< Content-Type: application/json
< Content-Length: 53
< Date: Tue, 27 Feb 2024 20:30:00 GMT
< Connection: close
<
{ [53 bytes data]
* Closing connection
```

L'interception s'est bien effectuée, l'approche est validée.



## Exceptions annotées avec @ResponseStatus
Spring permet d'annoter des exceptions avec l'annotation *@ResponseStatus*.
Contenant un code HTTP et un message HTTP (*reason*), cette annotation peut être exploitée par le handler par défaut pour construire une réponse.

Le code Java est le suivant :
```java
@ResponseStatus(value = HttpStatus.I_AM_A_TEAPOT, reason = "No more tea")
public class TeaPotException extends RuntimeException {
}

////////////////////////

@ControllerAdvice
public class MyExceptionHandler extends ResponseEntityExceptionHandler {
    // [...]
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleRemainingException(Exception ex) {
        ResponseStatus responseStatusAnnotation = ex.getClass().getAnnotation(ResponseStatus.class);
        if (responseStatusAnnotation != null) {
            return toErrorResponse(responseStatusAnnotation.code(), responseStatusAnnotation.reason());
        }
        return toErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, DEFAULT_MESSAGE);
    }
  // [...]
}

///////////////////////

@RestController
public class MyWebController {
  // [...]
  @GetMapping(value = "/teatime", produces = MediaType.TEXT_PLAIN_VALUE)
  public String getTeaTime() {
    throw new TeaPotException();
  }
}
```

La commande du client est :
```shell
$ curl -s -v http://localhost:9000/home/teatime
```

La réponse du serveur est :
```text
{
    "message": "No more tea"
}* Host localhost:9000 was resolved.
* IPv6: ::1
* IPv4: 127.0.0.1
*   Trying [::1]:9000...
* Connected to localhost (::1) port 9000
> GET /home/teatime HTTP/1.1
> Host: localhost:9000
> User-Agent: curl/8.6.0
> Accept: */*
>
< HTTP/1.1 500
< Content-Type: application/json
< Content-Length: 32
< Date: Tue, 27 Feb 2024 20:32:30 GMT
< Connection: close
<
{ [32 bytes data]
* Closing connection
```

Ainsi, dans le handler par défaut des exceptions, il est possible d'exploiter le contenu de l'annotation *@ResponseStatus*.


## Conclusion

Ces différents cas d'utilisation et leur implémentation permet de dessiner une approche globale de la gestion des erreurs HTTP.


Cette approche est la suivante :
* [S1] La création d'une classe d'interception des exceptions contenant :
  * [S1A] une méthode d'interception pour chaque exception métier connue
  * [S1B] une méthode d'interception par défaut pour les autres exceptions 
* [S2] L'usage des filtres de flux HTTP : 
  * [S2A] dans *HandlerInterceptor*, les exceptions sont exploitées par la classe d'interception.
  * [S2B] dans *Filter*, les exceptions ne sont pas interceptables par la classe d'interception. C'est donc au sein du filtre qu'il faudra générer une réponse HTTP d'erreur.


Cette approche globale permet de couvrir les cas d'utilisation énoncés au début de l'étude :
* [UC1] *Mutualisation du code* afin d'éviter de la duplication de code.
  * [S1] les traitements peuvent être centralisés par défaut dans la classe d'interception
  * [S1A] les traitements d'exceptions ayant le même ancêtre peuvent être communalisés
  * [S1B] les traitements par défaut sont centralisés
* [UC2] *Extensibilité et souplesse* afin de supporter les différentes erreurs des codes existants.
  * [S1A] permet d'ajouter des traitements spécifiques
* [UC3] *Gestion des exceptions non prévues* afin de prendre en compte tous les cas possibles et empêcher le serveur REST de tomber.
  * [S1B] cette méthode permet de traiter les cas non prévus hormis ceux du type [S2B].
* [UC4] *Construction personnalisée des réponses* afin de respecter les normes choisies ou les demandes spécifiques du client de l'interface.
  * [S1A] et [S1B] permettent de créer des réponses spécifiques


## Sources
* [1] [Spring Web MVC Exceptions](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-exceptionhandler.html)
* [2] [Error Handling for REST with Spring](https://www.baeldung.com/exception-handling-for-rest-with-spring)
* [3] [Complete Guide to Exception Handling in Spring Boot](https://reflectoring.io/spring-boot-exception-handling/)
* [4] [HandlerInterceptors vs. Filters in Spring MVC](https://www.baeldung.com/spring-mvc-handlerinterceptor-vs-filter)