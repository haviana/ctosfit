import { afterEach, describe, expect, it, vi } from "vitest";
import { finishSignInOnce, resolveSignInReturnPath } from "./oidc";

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
