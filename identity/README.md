# Identity module

**Owns:** identity and access concepts, including the BOX_OWNER, COACH and ATHLETE role vocabulary.

Hexagonal layout when implementing a use case:
- `domain/`: identity concepts and invariants; framework-free.
- `application/port/in/`: inbound use-case interfaces.
- `application/port/out/`: outbound dependency interfaces.
- `application/service/`: use-case orchestration.
- `adapter/in/`: REST or other inbound adapters.
- `adapter/out/`: persistence and external-provider adapters.

Rules: own the identity model; never expose persistence entities or Spring types through public contracts; do not depend on another business module. The app validates bearer JWTs from a configured OIDC/OAuth2 issuer. Tokens must be signed by the configured issuer and contain a roles string-array claim with recognized role values. Configure CTOSFIT_SECURITY_ISSUER_URI and CTOSFIT_SECURITY_JWK_SET_URI; if either is absent, authentication fails closed. The JWT subject identifies the user for active box-membership lookup. A request-selected box is only context selection; the JDBC membership adapter verifies it and supplies the box-scoped role before tenant-owned operations. Role claims alone do not authorize tenant-owned data.

API access is stateless. /api/box-owner/** is restricted to box owners, /api/coaches/** allows owners and coaches, /api/athletes/** allows all three roles, and other /api/** routes require an authenticated token. Tenant-owned use cases additionally require an active membership with the necessary role for the selected box. Keep that server-side check in place even when the endpoint route applies a token role filter.

Owners manage membership records only within the box selected by `X-Box-Id`, after active owner membership is resolved from the authenticated subject. The membership API can add/reactivate an existing authenticated identity, change its box-scoped role, or deactivate it. It does not send external invitations. An owner cannot remove their own owner role, and database updates are serialized per box so the last active owner cannot be demoted or deactivated.
