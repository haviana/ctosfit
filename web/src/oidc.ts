export type OidcConfig = {
  issuerUrl: string;
  clientId: string;
  redirectUri: string;
  apiUrl: string;
};

type OidcMetadata = { issuer: string; authorization_endpoint: string; token_endpoint: string; jwks_uri: string };
export type Session = { accessToken: string; expiresAt: number };
type SignInReturnPath = "/" | "/coach" | "/athlete";
type OidcFlow = { verifier: string; state: string; nonce: string; returnPath: SignInReturnPath };

const SESSION_KEY = "ctosfit.oidc.session";
const FLOW_KEY = "ctosfit.oidc.flow";
let signInCompletion: Promise<boolean> | null = null;
const base64Url = (bytes: Uint8Array) => btoa(String.fromCharCode(...bytes)).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
const randomString = (size = 32) => {
  const bytes = crypto.getRandomValues(new Uint8Array(size));
  return base64Url(bytes);
};
const digest = async (value: string) => base64Url(new Uint8Array(await crypto.subtle.digest("SHA-256", new TextEncoder().encode(value))));
const normalizeIssuer = (value: string) => value.replace(/\/+$/, "");
const decodeBase64Url = (value: string) => {
  const normalized = value.replace(/-/g, "+").replace(/_/g, "/");
  return Uint8Array.from(atob(normalized.padEnd(Math.ceil(normalized.length / 4) * 4, "=")), character => character.charCodeAt(0));
};

export function resolveSignInReturnPath(pathname: string | null | undefined): SignInReturnPath {
  const normalized = pathname && pathname.length > 1 ? pathname.replace(/\/+$/, "") : pathname;
  return normalized === "/coach" || normalized === "/athlete" ? normalized : "/";
}

export function readSignInReturnPath(): SignInReturnPath {
  const stored = sessionStorage.getItem(FLOW_KEY);
  if (!stored) return "/";
  try {
    const flow = JSON.parse(stored) as Partial<OidcFlow>;
    return resolveSignInReturnPath(flow.returnPath);
  } catch {
    return "/";
  }
}

export function readOidcConfig(): OidcConfig | null {
  const issuerUrl = import.meta.env.VITE_OIDC_ISSUER_URL?.trim();
  const clientId = import.meta.env.VITE_OIDC_CLIENT_ID?.trim();
  const redirectUri = import.meta.env.VITE_OIDC_REDIRECT_URI?.trim() || window.location.origin;
  const apiUrl = import.meta.env.VITE_API_URL?.trim() || "http://localhost:8080";
  if (!issuerUrl || !clientId) return null;
  return { issuerUrl: normalizeIssuer(issuerUrl), clientId, redirectUri, apiUrl: apiUrl.replace(/\/+$/, "") };
}

async function getMetadata(config: OidcConfig): Promise<OidcMetadata> {
  const response = await fetch(`${config.issuerUrl}/.well-known/openid-configuration`);
  if (!response.ok) throw new Error("Could not load sign-in configuration from the identity provider.");
  const metadata = await response.json() as OidcMetadata;
  if (normalizeIssuer(metadata.issuer) !== config.issuerUrl || !metadata.authorization_endpoint || !metadata.token_endpoint || !metadata.jwks_uri) {
    throw new Error("The identity provider returned invalid OpenID configuration.");
  }
  return metadata;
}

export async function beginSignIn(config: OidcConfig) {
  const metadata = await getMetadata(config);
  const verifier = randomString(48);
  const state = randomString();
  const nonce = randomString();
  const returnPath = resolveSignInReturnPath(window.location.pathname);
  sessionStorage.setItem(FLOW_KEY, JSON.stringify({ verifier, state, nonce, returnPath } satisfies OidcFlow));
  const authorize = new URL(metadata.authorization_endpoint);
  authorize.search = new URLSearchParams({
    client_id: config.clientId,
    redirect_uri: config.redirectUri,
    response_type: "code",
    scope: "openid profile email",
    code_challenge: await digest(verifier),
    code_challenge_method: "S256",
    state,
    nonce,
  }).toString();
  window.location.assign(authorize.toString());
}

async function validateIdToken(token: string, config: OidcConfig, metadata: OidcMetadata, nonce: string) {
  const [encodedHeader, encodedPayload, encodedSignature] = token.split(".");
  if (!encodedHeader || !encodedPayload || !encodedSignature) throw new Error("The identity provider returned an invalid ID token.");
  const header = JSON.parse(new TextDecoder().decode(decodeBase64Url(encodedHeader))) as { alg?: string; kid?: string };
  const claims = JSON.parse(new TextDecoder().decode(decodeBase64Url(encodedPayload))) as Record<string, unknown>;
  const keysResponse = await fetch(metadata.jwks_uri);
  if (!keysResponse.ok) throw new Error("Could not verify the identity provider’s ID token.");
  const keySet = await keysResponse.json() as { keys?: JsonWebKey[] };
  const jwk = keySet.keys?.find(key => (key as JsonWebKey & { kid?: string }).kid === header.kid);
  if (!jwk || !header.kid) throw new Error("The identity provider’s signing key was not found.");
  let algorithm: AlgorithmIdentifier | RsaPssParams | EcdsaParams;
  let importAlgorithm: AlgorithmIdentifier | RsaHashedImportParams | EcKeyImportParams;
  if (header.alg === "RS256") {
    algorithm = { name: "RSASSA-PKCS1-v1_5" };
    importAlgorithm = { name: "RSASSA-PKCS1-v1_5", hash: "SHA-256" };
  } else if (header.alg === "ES256") {
    algorithm = { name: "ECDSA", hash: "SHA-256" } as EcdsaParams;
    importAlgorithm = { name: "ECDSA", namedCurve: "P-256" };
  } else {
    throw new Error("The identity provider uses an unsupported ID token signing algorithm (use RS256 or ES256).");
  }
  const publicKey = await crypto.subtle.importKey("jwk", jwk, importAlgorithm, false, ["verify"]);
  const validSignature = await crypto.subtle.verify(algorithm, publicKey, decodeBase64Url(encodedSignature), new TextEncoder().encode(`${encodedHeader}.${encodedPayload}`));
  const audiences = Array.isArray(claims.aud) ? claims.aud : [claims.aud];
  const invalidExtraAudience = audiences.length > 1 && claims.azp !== config.clientId;
  if (!validSignature || normalizeIssuer(String(claims.iss ?? "")) !== config.issuerUrl || !audiences.includes(config.clientId) || invalidExtraAudience || claims.nonce !== nonce || typeof claims.exp !== "number" || claims.exp * 1000 <= Date.now()) {
    throw new Error("The ID token failed issuer, audience, expiry, nonce, or signature validation.");
  }
}

async function finishSignIn(config: OidcConfig, params: URLSearchParams): Promise<boolean> {
  const stored = sessionStorage.getItem(FLOW_KEY);
  sessionStorage.removeItem(FLOW_KEY);
  if (params.has("error")) throw new Error("The identity provider did not complete sign-in.");
  const code = params.get("code");
  if (!code) return false;
  if (!stored) throw new Error("The sign-in session expired. Please try again.");
  const flow = JSON.parse(stored) as OidcFlow;
  if (params.get("state") !== flow.state) throw new Error("Sign-in state did not match. Please try again.");
  const metadata = await getMetadata(config);
  const response = await fetch(metadata.token_endpoint, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: new URLSearchParams({
      grant_type: "authorization_code",
      client_id: config.clientId,
      redirect_uri: config.redirectUri,
      code,
      code_verifier: flow.verifier,
    }),
  });
  if (!response.ok) throw new Error("Sign-in could not be completed.");
  const token = await response.json() as { access_token?: string; id_token?: string; expires_in?: number };
  if (!token.access_token || !token.id_token) throw new Error("The identity provider did not return the required tokens.");
  await validateIdToken(token.id_token, config, metadata, flow.nonce);
  const session: Session = { accessToken: token.access_token, expiresAt: Date.now() + Math.max(0, token.expires_in ?? 3600) * 1000 };
  sessionStorage.setItem(SESSION_KEY, JSON.stringify(session));
  return true;
}

export function isSignInCallback(config: OidcConfig): boolean {
  const redirect = new URL(config.redirectUri, window.location.origin);
  const params = new URLSearchParams(window.location.search);
  return redirect.origin === window.location.origin && redirect.pathname === window.location.pathname
    && (params.has("code") || params.has("error"));
}

export function finishSignInOnce(config: OidcConfig): Promise<boolean> {
  if (signInCompletion) return signInCompletion;
  if (!isSignInCallback(config)) return Promise.resolve(false);
  const returnPath = readSignInReturnPath();
  const params = new URLSearchParams(window.location.search);
  window.history.replaceState(null, "", returnPath);
  signInCompletion = finishSignIn(config, params).finally(() => { signInCompletion = null; });
  return signInCompletion;
}

export function readSession(): Session | null {
  const raw = sessionStorage.getItem(SESSION_KEY);
  if (!raw) return null;
  try {
    const session = JSON.parse(raw) as Session;
    if (session.expiresAt <= Date.now()) {
      clearSession();
      return null;
    }
    return session;
  } catch {
    clearSession();
    return null;
  }
}

export function clearSession() { sessionStorage.removeItem(SESSION_KEY); }
