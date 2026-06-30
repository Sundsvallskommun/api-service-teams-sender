# TODO — Open items

Updated 2026-06-12. The service is the **delegated chat flow**: a configured
sender account posts real 1:1 Teams chat messages; MSAL token cache in
MariaDB; one-time interactive login bootstrap. The client-credentials
activity-feed alternative (bell notification, no DB, no login) is parked on
branch `hydran-2128-refactor-to-activity-feed` — switch only if the chat
requirement is dropped, since it needs tenant-side work (admin consent for
`TeamsActivity.Send`, Teams app manifest, `teamsAppId`).

---

## Before/at go-live

### 1. One-time login bootstrap (runbook)

After every fresh deploy against an empty `token_cache` table (and whenever
the refresh token dies):

1. Open `GET /api/teamssender/{municipalityId}/login` in a browser.
2. Sign in as the configured sender account (`AZURE_USER`).
3. Azure redirects to `/api/teamssender/callback?code=...&state={municipalityId}`;
   expect "Token successfully saved".
4. Smoke test: `POST /{municipalityId}/teams/messages` with
   `{"recipient": "you@sundsvall.se", "message": "Live test"}` → 204 and a
   chat message from the sender account arrives.

A 401 from the send endpoint with "No cached login found for user ..." means
step 1–3 must be (re)done.

### 2. Azure prerequisites (verify, not build)

- Delegated Graph scopes for the chat flow consented on the app registration.
- `AZURE_REDIRECT_URI` exactly matches the registered redirect URI
  (`.../api/teamssender/callback`).
- `AZURE_LOGIN_URL` includes `state=<municipalityId>` and the same redirect URI.
- Sender account is Teams-licensed.

---

## Hardening (not blockers)

### Token canary (architect-recommended)

The structural liability of delegated auth: the refresh token dies silently
(password reset, conditional-access/MFA changes, inactivity) and the failure
is discovered on the next send. Add a `@Dept44Scheduled` daily job that calls
`acquireTokenSilently` per configured municipality and alarms on failure, so
a dead token is noticed before it matters. Build when ops confirms the
alerting target.

### Callback CSRF posture

`/callback` validates `code` (non-blank) and `state` (valid, configured
municipality) but there is no server-generated nonce in `state` — the login
URL is statically configured. Accepted risk for a one-time internal admin
bootstrap; revisit if the endpoint is ever exposed beyond the internal
network (would require building the authorize URL dynamically and storing a
nonce).

### Recipient validator looseness

`recipient` is `@NotBlank` only. Graph's `users().byUserId(...)` accepts both
UPNs and AAD object ids, so anything stricter (e.g. `@Email`) would block a
valid input form. Unknown recipients already surface as a clean 404. Tighten
only if product wants UPN-only.

### One-AAD-app-for-all vs per-municipality

`AzureConfig` is a `Map<String, Azure>` keyed by municipality. If only
Sundsvall (2281) ever uses this service the map is overkill but harmless.
Confirm the deployment model with ops.
