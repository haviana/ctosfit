import { renderToStaticMarkup } from "react-dom/server";
import { describe, expect, it } from "vitest";
import AppShell from "./AppShell";

describe("AppShell", () => {
  it.each([
    ["coach", "Coach workspace", "/coach"],
    ["athlete", "Athlete workspace", "/athlete"],
  ] as const)("renders the %s route shell", (role, label, activePath) => {
    const markup = renderToStaticMarkup(
      <AppShell role={role} title="Today"><p>Route content</p></AppShell>,
    );

    expect(markup).toContain(label);
    expect(markup).toContain(`href="${activePath}" aria-current="page"`);
    const inactivePath = role === "coach" ? "/athlete" : "/coach";
    expect(markup).not.toContain(`href="${inactivePath}" aria-current="page"`);
    expect(markup).toContain("<main");
    expect(markup).toContain("Route content");
  });
});
