# Kotlin Auth API

Authentication API for various other projects.

| Environment | Swagger                                        | Database |
|-------------|------------------------------------------------|----------|
| Local       | http://localhost:9001/swagger-ui/index.html    | Firebase |
| Test        | http://192.168.0.73:9001/swagger-ui/index.html | Firebase |
| Production  | xxx                                            | Firebase |

# TODO

- DB - local SQL on server - server must also be secure, more than firewall
- safe login and auth return
- logic for multiple keys in rotation?

- Security
    - Security checklist https://snyk.io/blog/spring-boot-security-best-practices/
    - User updates and moderation tools:
        - GDPR docs for usage, why where etc.
        - email blacklist (gdpr??), other prevention strategies?
        - other user functions like change password
        - ONLY POST where passwords are involved
        - set users as inactive when not active in x months/years
- Ansible and hosting on server
- Later:
  - Put logging and basics (BaseX, enums, exceptions, annotations etc.) in packages (1 for logging, 1 for basics)