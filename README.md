# Kotlin Auth API

Authentication API for various other projects.

| Environment | Swagger                                        | Database |
|-------------|------------------------------------------------|----------|
| Local       | http://localhost:9001/swagger-ui/index.html    | Firebase |
| Test        | http://192.168.0.73:9001/swagger-ui/index.html | Firebase |
| Production  | xxx                                            | Firebase |

# TODO

- DB - postgres
  - get working locally
  - local SQL on server 
  - timed automatic monthly(?) backup of data? (must rotate out old data)
  - server must also be secure, firewall, sql injection etc.
- fix the weird duplicate of jwt service (see spring security checklist below, was done because of security filter and circular dependencies)
- safe login and auth return
  - back up and running after updates and db change
- remove unnecessary nullables
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
          - Email for newsletter/password changes
          - username, password, about etc. shouldn't be and are not expected to be personal information (uses own fault for using personal info here?)
          - changes (excep pass) are logged for x months (?)
          - must agree to these terms to use api
          - GDPR compliant with automatic account delete (implications for other parts of API, like number of people registered etc. ?) 
          - users may see what info is stored related to user
        - change password (email verification (code or something? requires sending of emails and valid/existing emails at save...))
        - ONLY POST where passwords are involved
          - Login/tokens
          - change password
    - Moderation
        - email, ip blacklist (gdpr??), other prevention strategies?
        - set users as inactive when not logged on (get token) in x months/years - tests
- Ansible and hosting on server
- Later:
  - Put basics (BaseX, enums, exceptions, annotations etc.) in a Gradle package (no publish)