# Kotlin Auth API

Authentication API for various other projects.

| Environment | Swagger                                        | Database |
|-------------|------------------------------------------------|----------|
| Local       | http://localhost:9001/swagger-ui/index.html    | Firebase |
| Test        | http://192.168.0.73:9001/swagger-ui/index.html | Firebase |
| Production  | xxx                                            | Firebase |

# TODO

- DB
  - get working locally
  - postgres
  - local SQL on server 
  - server must also be secure, more than firewall
- safe login and auth return
  - back up and running after updates and db change
- remove unnessecary nullables
  - update code/remove null checks
  - update tests

- Security
    - Security checklist https://snyk.io/blog/spring-boot-security-best-practices/
      1. HTTPS for prod
      2. test deps and find Spring boot vulnerabilities
      3. enable CSRF
      4. content security for Spring Boot XSS protection
      5. use OpenID Connect for auth
      6. Password hashing
      7. latest releases
      8. Store secrets securely
      9. Pen test
      10. Security team reviews
    - https://docs.spring.io/spring-security/reference/servlet/authorization/authorize-http-requests.html
      - jwt filter interactions? can combine?
      - hasAuthority updates
      - tests (integration)
    - User updates:
        - GDPR docs for usage, why where etc.
          - Email for contact? or someting
          - username, password, about etc. shouldn't be personal information
          - changes (excep pass) are logged for x months (?)
          - must agree to these terms to use api
        - change password
        - ONLY POST where passwords are involved
    - Moderation
        - email blacklist (gdpr??), other prevention strategies?
        - set users as inactive when not logged on (get token) in x months/years - tests
- Ansible and hosting on server
- Later:
  - Put logging and basics (BaseX, enums, exceptions, annotations etc.) in packages (1 for logging, 1 for basics)