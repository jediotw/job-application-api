# Deployment

HRiring is split into two deployable application tiers backed by managed PostgreSQL.

## Frontend

- Framework: React + Vite
- Working directory: `frontend`
- Build: `npm run build`
- Output: `dist`
- Production API URL: `VITE_API_BASE_URL`
- SPA routing: `frontend/vercel.json` provides the Vercel fallback; `frontend/nginx.conf` provides the equivalent fallback for container deployments.

## Backend

- Framework: Spring Boot 3.4.4
- Java: 17
- Build: `./mvnw -DskipTests package`
- Container: root `Dockerfile`
- Runtime port: `PORT` (falls back to `SERVER_PORT`, then 8080)
- Binds to `0.0.0.0`

### Required production environment

```text
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
JWT_SECRET
FRONTEND_URL
```

`FRONTEND_URL` must be the exact browser origin that calls the API. Do not use `*` for CORS.

### Supabase PostgreSQL

Use the JDBC connection string supplied by Supabase's Connect dialog. For a persistent Spring Boot backend, use the direct connection when the hosting network supports IPv6, or Supabase's Session Pooler when it does not.

Keep the database password and JWT secret in the hosting provider's environment/secrets configuration. Never commit them.

## Local environment

Copy the root `.env.example` values into your shell/environment and set real local values. The frontend uses its existing Vite proxy for local API requests.

## Deployment order

1. Provision PostgreSQL and obtain its connection details.
2. Deploy the Spring Boot backend with the database and JWT environment variables.
3. Set `FRONTEND_URL` to the deployed frontend origin.
4. Deploy the frontend with `VITE_API_BASE_URL` pointing to the deployed backend origin.
5. Verify login, candidate, recruiter, job, and application flows end-to-end.

No hosting provider is required by this repository configuration; the application can be deployed later to a provider that supports the required frontend/static hosting and Java/container runtime.
