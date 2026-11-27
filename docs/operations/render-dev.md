# Render development deployment

`render.yaml` defines the development environment as a Render Blueprint:

- `ctosfit-api-dev` is a Docker-based Spring Boot web service in Frankfurt.

The API exposes `/actuator/health/readiness` as its Render HTTP health check. The current service has no checked-in database migrations, so this deployment does not provision a database.

OIDC values and signing configuration use `sync: false` and must be entered in Render without committing secret values. The browser client ID and redirect URI are public configuration; never add a browser client secret.

## Deployment checklist

1. Push `render.yaml` and `Dockerfile` to the intended branch.
2. Create or update the Blueprint in the confirmed Render workspace.
3. Fill the `sync: false` values for the API service.
4. Wait for the service to become live and verify `https://ctosfit-api-dev.onrender.com/actuator/health/readiness` returns HTTP 200.

After the Blueprint syncs, Render starts development deployments when a commit
lands on the linked `main` branch. This includes pull request merges and any
direct pushes GitHub allows. Require pull requests and passing checks on
`main` before enabling this trigger so CI runs before deployment.
