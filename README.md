# Kotlin Auth API

Authentication API for various other projects.

| Environment | Swagger                                        | Database |
|-------------|------------------------------------------------|----------|
| Local       | http://localhost:9001/swagger-ui/index.html    | Firebase |
| Test        | http://192.168.0.73:9001/swagger-ui/index.html | Firebase |
| Production  | xxx                                            | Firebase |

# TODO

- DB
- implement logic from chook
- safe login and auth return
- various keys?

- Security
    - Security checklist https://snyk.io/blog/spring-boot-security-best-practices/
    - User updates and moderation tools:
        - GDPR info (Topic: Security, Articles: GDPR, Terms and Conditions)
        - email blacklist (gdpr??), other prevention strategies?
        - Input sanitation implementation
        - implement salt, pepper in passwords - https://reflectoring.io/spring-security-password-handling/ if not already done by bcrypt
        - other user functions like change password
        - ONLY POST where passwords are involved
        - set users as inactive when not active in x months/years
- Web 
    - Small interface for login, simple user info
- Ansible and hosting on server
- Later:
  - Put logging and basics (BaseX, enums, exceptions, annotations etc.) in packages (1 for logging, 1 for basics)