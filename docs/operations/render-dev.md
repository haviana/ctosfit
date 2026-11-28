# Render development deployment

`render.yaml` defines the development environment as a Render Blueprint:

- `ctosfit-api-dev` is a Docker web service in Frankfurt. Its health check is `/actuator/health/readiness`.
- `ctosfit-web-dev` is a static site built from `web/`.
- `ctosfit-db-dev` is the PostgreSQL database the API reads.

OIDC values and signing configuration use `sync: false` and must be entered in Render without committing secret values. The browser client ID and redirect URI are public configuration; never add a browser client secret.

Both services set `autoDeployTrigger: off`. After a Blueprint sync, Render does not start a deploy when a commit lands on the linked branch. GitHub Actions is the only automatic deploy path, so a merge does not deploy twice.

## Deploy from GitHub Actions

The Deploy workflow starts after both the CI and Quality workflows have succeeded for the same push to `main`. The web build and tests live in Quality, so a green CI run is not enough on its own. It can also be started by hand from the Actions tab: open Deploy, choose **Run workflow**, and select `main`. A manual run deploys only when those two workflows already succeeded for the current `main` commit. Re-running an older CI or Quality workflow does not deploy once `main` has moved on.

The job POSTs each service's Render deploy hook and appends that commit SHA as `ref`. Render then deploys that commit. A response outside HTTP 2xx fails the job. The hook URLs stay in repository secrets and are masked in the log.

Add these Actions secrets (Settings > Secrets and variables > Actions):

| Secret | Render service |
| --- | --- |
| `RENDER_DEPLOY_HOOK_API` | `ctosfit-api-dev` |
| `RENDER_DEPLOY_HOOK_WEB` | `ctosfit-web-dev` |

In Render, open the service, then **Settings > Deploy Hook**, and copy that URL into the matching secret. The URL is a credential. Do not print it, commit it, or paste it into the workflow file. If a secret is missing or is not a single-line https URL, the job fails and names the secret to add or replace.

## Deployment checklist

1. Push `render.yaml` and `Dockerfile` to the intended branch.
2. Create or update the Blueprint in the confirmed Render workspace. After the sync, confirm both services keep automatic deploys off.
3. Fill the `sync: false` values for the API service and the web OIDC values.
4. Add `RENDER_DEPLOY_HOOK_API` and `RENDER_DEPLOY_HOOK_WEB`.
5. Merge to `main` and wait for CI and Quality to succeed. The Deploy workflow then starts both services. To deploy the current `main` commit by hand, run the Deploy workflow on `main`.
6. Verify `https://ctosfit-api-dev.onrender.com/actuator/health/readiness` returns HTTP 200.

To roll back, redeploy an earlier successful deploy from the Render dashboard. The GitHub hook deploys the tip of `main` after CI and Quality have succeeded for that commit.
