import type { ReactNode } from "react";

type ShellRole = "coach" | "athlete";

const roleCopy: Record<ShellRole, { label: string; eyebrow: string; title: string }> = {
  coach: { label: "Coach workspace", eyebrow: "COACH DESK", title: "Build the day" },
  athlete: { label: "Athlete workspace", eyebrow: "ATHLETE TRAINING", title: "Do the work" },
};

export default function AppShell({ role, title, children }: { role: ShellRole; title: string; children: ReactNode }) {
  const copy = roleCopy[role];
  const activePath = `/${role}`;
  return <div className={`app-shell app-shell-${role}`}>
    <a className="skip-link" href="#route-content">Skip to workspace</a>
    <aside className="app-shell-sidebar" aria-label={`${copy.label} navigation`}>
      <a className="wordmark app-shell-brand" href="/" aria-label="Ctosfit home">
        <span className="brand-mark" aria-hidden="true">C</span><span>ctosfit</span>
      </a>
      <p className="app-shell-role">{copy.label}</p>
      <nav className="app-shell-nav" aria-label="Workspace">
        <a href="/coach" aria-current={activePath === "/coach" ? "page" : undefined}>
          <span aria-hidden="true">▤</span><span>Coach</span>
        </a>
        <a href="/athlete" aria-current={activePath === "/athlete" ? "page" : undefined}>
          <span aria-hidden="true">◷</span><span>Athlete</span>
        </a>
      </nav>
      <div className="app-shell-sidebar-note">
        <span aria-hidden="true">●</span>
        <p><strong>Server-authorized</strong><small>Your membership controls what the API allows.</small></p>
      </div>
    </aside>
    <div className="app-shell-stage">
      <header className="app-shell-header">
        <div><p>{copy.eyebrow}</p><h1>{copy.title}</h1></div>
        <span className="app-shell-context">{title}</span>
      </header>
      <main id="route-content" className="app-shell-content" tabIndex={-1}>{children}</main>
    </div>
  </div>;
}
