# WOD-12 specification

## Objective

Create a React and TypeScript application foundation in which coach and athlete
workspaces have stable routes and share a responsive, accessible application
shell. Preserve the public landing page, fictional demo, and authenticated API
workspace already in the repository.

## Assumptions

- `/coach` and `/athlete` are browser routes served by the existing Render SPA
  rewrite to `index.html`.
- Route visibility is navigation only; the API remains authoritative for roles,
  membership, and tenant scope.
- The existing OIDC Authorization Code + PKCE flow remains unchanged.
- The existing visual language is the design-system source; this task extracts
  reusable semantic primitives rather than redesigning the product.

## Tech stack and commands

- React 19, TypeScript 5.9, Vite 7.
- Build: `cd web && npm run build`
- Test: `cd web && npm test`
- Development: `cd web && npm run dev`

## Structure

- `web/src/routes.ts`: pure URL-to-route contract.
- `web/src/AppShell.tsx`: responsive coach/athlete shell and shared primitives.
- `web/src/api-client.ts`: typed HTTP boundary for authenticated API calls.
- `web/src/AuthenticatedWorkspace.tsx`: live workspace selected by route role.
- `web/src/styles.css`: semantic shell tokens and responsive layout.

## Code style

```tsx
<AppShell role="coach" title="Programming">
  <AuthenticatedWorkspace initialRole="COACH" lockRole />
</AppShell>
```

Use focused components, semantic HTML, explicit TypeScript unions, and server
authorization as the source of truth.

## Testing strategy

- Unit-test route recognition and navigation fallbacks.
- Unit-test API headers, safe error behavior, and unauthorized-session handling.
- Server-render both route shells to prove their semantic structure without a
  browser DOM dependency.
- Run the production build and inspect both routes at mobile and desktop widths.

## Boundaries

- Always: keep public/demo behavior, keyboard navigation, visible focus, and the
  server-side membership boundary.
- Ask first: backend API changes, new runtime state libraries, or deployment
  topology changes.
- Never: infer authorization from a route, persist tokens outside session
  storage, put secrets in the browser bundle, or trust a box ID as proof of
  access.

## Success criteria

- Direct visits to `/coach` and `/athlete` render distinct role-labelled shells.
- Each shell works at 320px and desktop widths without horizontal page overflow.
- Navigation exposes the active route and is keyboard accessible.
- Authenticated requests use one typed client and preserve bearer/session and
  server-validated box selection semantics.
- `npm test` and `npm run build` pass.

## Non-goals

- New coach or athlete business features.
- Backend authorization, persistence, or OpenAPI changes.
- A new design language, global state library, or router dependency.
