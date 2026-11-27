import { afterEach, describe, expect, it, vi } from "vitest";
import { finishSignInOnce, readSession, resolveSignInReturnPath } from "./oidc";

afterEach(() => vi.unstubAllGlobals());

describe("resolveSignInReturnPath", () => {
  it.each([
    ["/coach", "/coach"],
    ["/coach/", "/coach"],
    ["/athlete", "/athlete"],
    ["/athlete/", "/athlete"],
    ["/", "/"],
    ["/auth/callback", "/"],
    ["https://attacker.example/", "/"],
    [null, "/"],
  ] as const)("maps %s to an allowlisted local route", (path, expected) => {
    expect(resolveSignInReturnPath(path)).toBe(expected);
  });
});

describe("finishSignInOnce", () => {
  it.each(["/", "/auth/callback"])("stores the session after a single token exchange at %s", async pathname => {
    const origin = "https://app.example";
    const issuer = "https://issuer.example";
    const redirectUri = `${origin}${pathname}`;
    const location = { origin, pathname, search: "?code=example&state=state" };
    const storage = new Map([["ctosfit.oidc.flow", JSON.stringify({ verifier: "verifier", state: "state", nonce: "nonce", returnPath: "/coach" })]]);
    vi.stubGlobal("sessionStorage", {
      getItem: (key: string) => storage.get(key) ?? null,
      setItem: (key: string, value: string) => storage.set(key, value),
      removeItem: (key: string) => storage.delete(key),
    });
    vi.stubGlobal("window", {
      location,
      history: { replaceState: (_state: unknown, _title: string, path: string) => {
        location.pathname = path;
        location.search = "";
      } },
    });
    const header = btoa(JSON.stringify({ alg: "RS256", kid: "key" }));
    const claims = btoa(JSON.stringify({ iss: issuer, aud: "client", nonce: "nonce", exp: Math.floor(Date.now() / 1000) + 3600 }));
    vi.stubGlobal("crypto", { subtle: { importKey: vi.fn().mockResolvedValue({}), verify: vi.fn().mockResolvedValue(true) } });
    const fetchMock = vi.fn()
      .mockResolvedValueOnce(Response.json({ issuer, authorization_endpoint: `${issuer}/authorize`, token_endpoint: `${issuer}/token`, jwks_uri: `${issuer}/keys` }))
      .mockResolvedValueOnce(Response.json({ access_token: "example-token", id_token: `${header}.${claims}.c2ln` }))
      .mockResolvedValueOnce(Response.json({ keys: [{ kid: "key" }] }));
    vi.stubGlobal("fetch", fetchMock);

    const config = { issuerUrl: issuer, clientId: "client", redirectUri, apiUrl: "https://api.example" };
    await expect(Promise.all([finishSignInOnce(config), finishSignInOnce(config)])).resolves.toEqual([true, true]);
    expect(fetchMock.mock.calls.filter(([url]) => url === `${issuer}/token`)).toHaveLength(1);
    expect(readSession()?.accessToken).toBe("example-token");
    expect(location.pathname).toBe("/coach");
  });

  it.each(["/", "/auth/callback"])("shares the callback exchange at %s and retains the retry destination", async (pathname) => {
    const flow = JSON.stringify({ verifier: "verifier", state: "state", nonce: "nonce", returnPath: "/athlete" });
    const location = { origin: "https://app.example", pathname, search: "?code=code&state=state" };
    const replaceState = vi.fn((_state, _title, nextPath: string) => {
      location.pathname = nextPath;
      location.search = "";
    });
    vi.stubGlobal("window", {
      location,
      history: { replaceState },
    });
    vi.stubGlobal("sessionStorage", {
      getItem: vi.fn().mockReturnValue(flow),
      removeItem: vi.fn(),
    });
    const fetchMock = vi.fn().mockResolvedValue(new Response(null, { status: 503 }));
    vi.stubGlobal("fetch", fetchMock);
    const config = { issuerUrl: "https://issuer.example", clientId: "client", redirectUri: `https://app.example${pathname}`, apiUrl: "https://api.example" };

    const first = finishSignInOnce(config);
    const replay = finishSignInOnce(config);

    expect(replay).toBe(first);
    expect(sessionStorage.removeItem).toHaveBeenCalledOnce();
    expect(replaceState).toHaveBeenCalledWith(null, "", "/athlete");
    await expect(first).rejects.toThrow("Could not load sign-in configuration");
    expect(fetchMock).toHaveBeenCalledOnce();
    expect(location.pathname).toBe("/athlete");
    expect(location.search).toBe("");
    await expect(finishSignInOnce(config)).resolves.toBe(false);
  });
});
