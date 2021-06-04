
# OPA Hello World

This is a simple demo of OPA access control using Spring voting-based access decision.

## How to demo

- Start OPA server
```
docker run -p 8181:8181 openpolicyagent/opa run --server
```
- Load policy in file example.rego into OPA server
```
curl -X PUT --data-binary @example.rego localhost:8181/v1/policies/example
```
- Start the application
```
./mvnw spring-boot:run
```
- Test policy (should allow)
```
curl localhost:8080/greeting
{"id":1,"content":"Hello, World!"}
```
- Change policy file example.rego to always deny
```
package http.authz

allow = false
```
- Load updated policy
```
curl -X PUT --data-binary @example.rego localhost:8181/v1/policies/example
{}
```
- Test policy (should deny)
```
curl localhost:8080/greeting
{"timestamp":"2021-02-23T16:19:11.652+00:00","status":403,"error":"Forbidden","message":"","path":"/greeting"}
```
- Let's try out some policy rules. Change contents of example.rego to this
```
package http.authz

default allow = false

# Allow request to endpoint /greeting if it's a GET using curl with a local remote ip address, otherwise deny.
allow {
    input.path == ["greeting"]
    input.method == "GET"
    contains(input.headers[_], "curl")
    input.remoteipaddress == "127.0.0.1"
}
```
- Load updated policy again (see above) and test policy again (see above). Should allow request

## How to run with docker compose

This loads policy `example.rego` into opa automatically at startup first time. Then reload policy and make requests with
curl the same way as above.

- Start opa server and app server (add `-d` to run in detached mode)
```
docker compose up
```
- Stop servers with Ctrl+c (or `docker compose down` if in detached mode)

## References

https://github.com/open-policy-agent/contrib/tree/master/spring_authz

https://www.openpolicyagent.org/docs/latest/http-api-authorization

https://docs.spring.io/spring-security/site/docs/5.4.5/reference/html5/#authz-voting-based

https://play.openpolicyagent.org
