import { describe, expect, it } from "vitest";
import { resolveAppRoute, resolveRouteForRender } from "./routes";

describe("resolveAppRoute", () => {
  it.each([
    ["/coach", "coach"],
    ["/coach/", "coach"],
    ["/athlete", "athlete"],
    ["/athlete/", "athlete"],
    ["/", "home"],
    ["/unknown", "not-found"],
  ] as const)("maps %s to %s", (path, expected) => {
    expect(resolveAppRoute(path)).toBe(expected);
  });
});

describe("resolveRouteForRender", () => {
  it("renders the originating athlete route on a dedicated OIDC callback path", () => {
    expect(resolveRouteForRender("/auth/callback", true, "/athlete")).toBe("athlete");
  });

  it("keeps unknown non-callback paths on the not-found route", () => {
    expect(resolveRouteForRender("/auth/callback", false, "/athlete")).toBe("not-found");
  });
});
