import { afterEach, describe, expect, it, vi } from "vitest";
import { createApiClient } from "./api-client";

afterEach(() => vi.unstubAllGlobals());

describe("createApiClient", () => {
  it("adds bearer and selected-box headers to authenticated requests", async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify({ ok: true }), {
      status: 200,
      headers: { "Content-Type": "application/json" },
    }));
    vi.stubGlobal("fetch", fetchMock);

    const client = createApiClient({ apiUrl: "https://api.example.test/", accessToken: "token", boxId: "box-1" });
    await client.get<{ ok: boolean }>("/api/coaches/exercises");

    const [url, init] = fetchMock.mock.calls[0] as [string, RequestInit];
    const headers = new Headers(init.headers);
    expect(url).toBe("https://api.example.test/api/coaches/exercises");
    expect(headers.get("Authorization")).toBe("Bearer token");
    expect(headers.get("X-Box-Id")).toBe("box-1");
    expect(headers.has("Content-Type")).toBe(false);
  });

  it("omits the box header when no box is selected", async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response(null, { status: 204 }));
    vi.stubGlobal("fetch", fetchMock);

    const client = createApiClient({ apiUrl: "https://api.example.test", accessToken: "token" });
    await client.request<void>("/api/boxes", {
      method: "POST",
      headers: { "X-Box-Id": "caller-supplied" },
      body: JSON.stringify({ name: "Northline", timeZone: "Europe/Lisbon" }),
    });

    const headers = new Headers((fetchMock.mock.calls[0]?.[1] as RequestInit).headers);
    expect(headers.has("X-Box-Id")).toBe(false);
    expect(headers.get("Content-Type")).toBe("application/json");
  });

  it("serializes JSON bodies through the typed post helper", async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response(null, { status: 204 }));
    vi.stubGlobal("fetch", fetchMock);
    const client = createApiClient({ apiUrl: "https://api.example.test", accessToken: "token" });

    await expect(client.post<void>("/api/boxes", { name: "Northline" })).resolves.toBeUndefined();
    const [, init] = fetchMock.mock.calls[0] as [string, RequestInit];
    expect(init.method).toBe("POST");
    expect(init.body).toBe(JSON.stringify({ name: "Northline" }));
  });

  it("clears the local session boundary on unauthorized responses", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue(new Response(null, { status: 401 })));
    const onUnauthorized = vi.fn();
    const client = createApiClient({ apiUrl: "https://api.example.test", accessToken: "token", onUnauthorized });

    await expect(client.get("/api/athletes/results")).rejects.toMatchObject({
      status: 401,
      message: "Your session expired. Sign in again.",
    });
    expect(onUnauthorized).toHaveBeenCalledOnce();
  });

  it("keeps the session active on forbidden responses", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue(new Response(null, { status: 403 })));
    const onUnauthorized = vi.fn();
    const client = createApiClient({ apiUrl: "https://api.example.test", accessToken: "token", onUnauthorized });

    await expect(client.get("/api/coaches/exercises")).rejects.toMatchObject({ status: 403 });
    expect(onUnauthorized).not.toHaveBeenCalled();
  });

  it("does not expose upstream details for server errors", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue(new Response(JSON.stringify({ message: "database host db.internal failed" }), {
      status: 500,
      headers: { "Content-Type": "application/json" },
    })));
    const client = createApiClient({ apiUrl: "https://api.example.test", accessToken: "token" });

    await expect(client.get("/api/athletes/results")).rejects.toMatchObject({
      status: 500,
      message: "The service could not complete the request. Try again.",
    });
  });

  it("preserves the backend string error contract for client validation failures", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue(new Response(JSON.stringify({ error: "Invalid workout result" }), {
      status: 400,
      headers: { "Content-Type": "application/json" },
    })));
    const client = createApiClient({ apiUrl: "https://api.example.test", accessToken: "token" });

    await expect(client.post("/api/athletes/results", {})).rejects.toMatchObject({
      status: 400,
      message: "Invalid workout result",
    });
  });
});
