# Login API

A small Java 17 / Spring Boot app with a MySQL-backed user table, BCrypt password hashing, a session-based login API, and a static login page.

## Run locally

The app connects to MySQL by default with the local credentials:

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/login_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "root"
```

You can override those values in the environment if needed, but the defaults are configured for the local MySQL root/root setup.

Run the app with Maven:

```powershell
mvn spring-boot:run
```

Open [http://localhost:8080](http://localhost:8080), create an account, and sign in.

Hibernate creates/updates the `app_users` table on startup. Passwords are stored as BCrypt hashes, never as plain text.

## Swagger / OpenAPI

After starting the app, open [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) to explore and call the documented API. The OpenAPI JSON is available at `/v3/api-docs`. Sign in through `POST /api/auth/login` before calling `GET /api/auth/me`; the browser will use the returned session cookie.

## API

All request bodies use JSON.

### Register — `POST /api/auth/register`

```json
{
  "username": "alex",
  "firstName": "Alex",
  "lastName": "Morgan",
  "email": "alex@example.com",
  "password": "a-strong-password"
}
```

Returns `201 Created` with the new user's profile. Usernames must be 3–50 characters, email addresses must be unique, and passwords must be 8–72 characters and at most 72 UTF-8 bytes. Only registered accounts can sign in. Passwords are hashed before being stored.

### Login — `POST /api/auth/login`

```json
{
  "username": "alex",
  "password": "a-strong-password"
}
```

Sign in with the registered username and password. Returns the user profile and sets an HTTP-only session cookie.

### Current user — `GET /api/auth/me`

Returns the logged-in user's profile using the session cookie. Returns `401 Unauthorized` when there is no active login.

Validation errors return `400`; invalid credentials return `401`; an already-used username returns `409`.
