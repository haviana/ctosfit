# Ctosfit web app

The web app contains a public landing page, a fictional browser-only demo, and a separate authenticated workspace connected to the REST API.

The application routes are:

- `/` — public landing page, demo, and sign-in entry point.
- `/coach` — responsive coach workspace shell.
- `/athlete` — responsive athlete workspace shell.

Routes select the user experience only. They do not grant permissions: the API
continues to enforce active membership, role, and box scope on every request.

## Configure OIDC sign-in

1. Copy `.env.example` to `.env.local`.
2. Set `VITE_OIDC_ISSUER_URL` to the OIDC issuer URL, `VITE_OIDC_CLIENT_ID` to a public browser client, `VITE_OIDC_REDIRECT_URI` to the exact registered callback URL, and `VITE_API_URL` to the Ctosfit API origin.
3. Configure the OIDC client for Authorization Code + PKCE (`S256`) and allow the local redirect URI. The issuer must expose its standard `.well-known/openid-configuration` document and permit browser CORS for discovery and token exchange.
4. Set backend `CTOSFIT_SECURITY_ISSUER_URI`, `CTOSFIT_SECURITY_JWK_SET_URI`, and `CTOSFIT_SECURITY_AUDIENCE` to the matching issuer, signing keys, and API audience (`ctosfit-api` by default). Configure the API CORS policy to allow the web origin.
5. Run `npm run dev` and select **Sign in**. After authentication, provide the UUID of a box where the signed-in subject has active membership.

The browser flow uses OIDC state, nonce, and PKCE. The bearer token is stored only in `sessionStorage` for that tab, is never logged, and is removed on sign-out or expiry. The box ID is a selection hint; every request also requires server-side membership and role checks. Do not put a client secret in this app. The identity provider and API must use HTTPS outside local development.

## Live workspace

Choose Coach to create an exercise, create a structured workout draft, and publish a dated immutable snapshot. Choose Athlete to view the selected date's published workouts, submit a typed result linked to its publication, and review history and comparable personal bests. Actions call the authenticated API and do not write to the fictional demo.

The demo at **Explore the demo** remains isolated from the API and uses sample Northline data only.
