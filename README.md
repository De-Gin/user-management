# User Management System

A Spring Boot web application for browsing and registering user records, built on a layered controller–service–repository architecture with server-rendered Thymeleaf views.

Users are listed by primary key and addressable at `/users/{id}`. Registration is validated on both the application and database layers, and validation failures re-render the form with per-field messages rather than throwing.

<!-- Add a screenshot here — the user list or the registration form with a validation error showing:
![Screenshot](docs/screenshot.png)
-->

## Stack

| | |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2.5 (Web, Thymeleaf, Data JPA, Validation) |
| Database | H2, in-memory |
| Build | Gradle |

## Running it

```bash
./gradlew bootRun
```

Then open <http://localhost:8080/users>. The schema is created on startup and seeded from `data.sql`; no external database is needed.

The H2 console is enabled at <http://localhost:8080/h2-console> (JDBC URL `jdbc:h2:mem:testdb`).

## Routes

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/users` | List all users |
| `GET` | `/users/{id}` | Single user by primary key |
| `GET` | `/users/username/{username}` | Single user by username, resolved in the database |
| `GET` | `/users/register` | Registration form |
| `POST` | `/users/register` | Create a user, then redirect to the list |

`/users/register` is mapped ahead of `/users/{id}` so that `register` isn't matched as an identifier.

## Design notes

**Layering.** `UserController` depends only on the `UserService` interface; `UserServiceImpl` holds the implementation and is the only class that touches the repository. Dependencies are injected through constructors rather than fields, which keeps the classes testable and their requirements explicit.

**Repository.** `UserRepository` extends `CrudRepository` and declares `findByUsername` and `findByEmail`. Spring Data derives both queries from the method names, so no SQL is written by hand. `CrudRepository.findAll()` returns `Iterable`, which the service converts to a `List` before it reaches the view.

**Validation runs twice, deliberately.** Bean Validation annotations on the entity (`@Size`, `@Email`, `@NotBlank`) catch bad input and produce readable messages; `@Column(unique = true)` enforces uniqueness at the database level, where it can't be bypassed. Application-level checks are for the user, database constraints are for correctness.

**Errors.** A missing record throws `UserNotFoundException`, handled by a `@ControllerAdvice` that renders an error page instead of a stack trace.

**Passwords are never rendered**, on any view, in any form.

**Post/Redirect/Get.** A successful registration redirects to `/users` rather than returning the list directly, so refreshing the result page doesn't resubmit the form.

## Configuration

`application.yaml` sets `ddl-auto: create-drop`, so the schema is built from the entity classes at startup and dropped at shutdown. `defer-datasource-initialization: true` is required alongside it — without it `data.sql` runs before Hibernate has created the tables and startup fails.

Two constraints are worth knowing before editing anything:

- The entity is mapped to `@Table(name = "users")` because `user` is a reserved keyword in H2. `data.sql` must use the same table name.
- Seed data has to satisfy the entity's own validation, so seeded usernames are at least 10 characters.

## Project structure

```
src/main/
├── java/com/example/demo/
│   ├── controller/   UserController, exception handler
│   ├── entity/       User
│   ├── exceptions/   UserNotFoundException
│   ├── repository/   UserRepository
│   └── service/      UserService, UserServiceImpl
└── resources/
    ├── static/css/   style.css
    ├── templates/    list, detail, form, error
    ├── application.yaml
    └── data.sql
```

## Known gaps

Passwords are stored as plain text. This was built as a coursework exercise where hashing was out of scope; a production version would run them through `BCryptPasswordEncoder` before persisting. There is no test suite yet.
