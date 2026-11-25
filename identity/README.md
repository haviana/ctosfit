# Identity module

**Owns:** identity and access concepts, including the BOX_OWNER, COACH and ATHLETE role vocabulary.

Hexagonal layout when implementing a use case:
- `domain/`: identity concepts and invariants; framework-free.
- `application/port/in/`: inbound use-case interfaces.
- `application/port/out/`: outbound dependency interfaces.
- `application/service/`: use-case orchestration.
- `adapter/in/`: REST or other inbound adapters.
- `adapter/out/`: persistence and external-provider adapters.

Rules: own the identity model; never expose persistence entities or Spring types through public contracts; do not depend on another business module. The app supports OIDC Authorization Code sign-in with PKCE and validates bearer JWTs from the configured issuer. Set CTOSFIT_OIDC_ENABLED=true, CTOSFIT_OIDC_ISSUER_URI, CTOSFIT_OIDC_CLIENT_ID and CTOSFIT_OIDC_CLIENT_SECRET to enable browser sign-in; register {public-base-url}/login/oauth2/code/ctosfit as the IdP callback. OIDC ID tokens and API access tokens must provide a roles string-array claim with recognized role values. API token validation additionally requires CTOSFIT_SECURITY_ISSUER_URI, CTOSFIT_SECURITY_JWK_SET_URI and CTOSFIT_SECURITY_AUDIENCE. API tokens must be signed by the configured issuer and target the configured audience. If required API token settings are absent, bearer authentication fails closed. Tenant membership lookup and trusted box context are a separate WOD-15 concern; role claims alone must not authorize tenant-owned data.

API access is stateless and deny-by-default. /api/box-owner/** is restricted to box owners, /api/coaches/** allows owners and coaches, /api/athletes/** is restricted to athletes, and unmatched API routes are denied. The athlete route policy does not replace object ownership checks. Keep authorization checks server-side and add tenant-scoped membership checks before exposing tenant-owned endpoints.
