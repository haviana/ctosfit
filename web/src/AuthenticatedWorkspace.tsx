import { type FormEvent, useEffect, useMemo, useState } from "react";
import { apiRequest, beginSignIn, clearSession, finishSignIn, readOidcConfig, readSession } from "./oidc";
import type { OidcConfig } from "./oidc";

type Session = NonNullable<ReturnType<typeof readSession>>;
type Exercise = { id: string; name: string; category: string; equipment: string[] };
type MovementTarget = { kind: string; value: string; unit?: string | null };
type Movement = { exerciseId: string; movementName: string; variants: Record<string, MovementTarget[]> };
type Block = { id: string; title: string; intent: string; format: Record<string, unknown> | null; movements: Movement[]; instructions?: string | null };
type Draft = { id: string; title: string; version: number; blocks: Block[] };
type Publication = { id: string; title: string; workoutId: string; version: number; scheduledFor: string; publishedAt: string; blocks: Block[] };
type Result = { id: string; publicationId: string; metricId: string; prescriptionId: string; submittedAt: string; score: { type: string; amount?: number; unit?: string; elapsedSeconds?: number; timeOutcome?: string; reps?: number; rounds?: number; additionalReps?: number; calories?: number; completionStatus?: string } };
type Best = { metricId: string; prescriptionId: string; resultId: string; achievedAt: string; score: Result["score"] };
type Membership = { id: string; userId: string; boxId: string; role: Role; status: "ACTIVE" | "INACTIVE" };
type Role = "BOX_OWNER" | "COACH" | "ATHLETE";

const today = () => new Date().toISOString().slice(0, 10);
const initialConfig = readOidcConfig();

function parseError(error: unknown) { return error instanceof Error ? error.message : "Something went wrong."; }

export default function AuthenticatedWorkspace() {
  const [session, setSession] = useState<Session | null>(() => readSession());
  const [config] = useState<OidcConfig | null>(initialConfig);
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  const [boxId, setBoxId] = useState(() => sessionStorage.getItem("ctosfit.selectedBox") ?? "");
  const [selectedRole, setSelectedRole] = useState<Role>("COACH");
  useEffect(() => {
    if (!config) return;
    let cancelled = false;
    finishSignIn(config).then((finished) => {
      if (finished && !cancelled) { setSession(readSession()); setError(""); }
    }).catch((reason: unknown) => { if (!cancelled) setError(parseError(reason)); });
    return () => { cancelled = true; };
  }, [config]);
  const token = session?.accessToken;
  useEffect(() => {
    if (session && session.expiresAt <= Date.now()) { clearSession(); setSession(null); }
  }, [session]);

  async function signIn() {
    if (!config) return;
    setError("");
    try { await beginSignIn(config); } catch (reason) { setError(parseError(reason)); }
  }
  function signOut() { clearSession(); setSession(null); setError(""); }
  function changeBox(value: string) { setBoxId(value); sessionStorage.setItem("ctosfit.selectedBox", value); }

  async function createBox(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!config || !session) return;
    const form = new FormData(event.currentTarget);
    setError(""); setBusy(true);
    try {
      const created = await apiRequest<{ id: string }>(config, session, "", "/api/boxes", { method: "POST", body: JSON.stringify({ name: String(form.get("boxName")), timeZone: String(form.get("timeZone")) }) });
      changeBox(created.id); setSelectedRole("BOX_OWNER");
    } catch (reason) { setError(parseError(reason)); }
    finally { setBusy(false); }
  }

  return <section className="connected-section" id="member-workspace">
    <div className="connected-heading"><div><p className="eyebrow"><span className="eyebrow-line" /> MEMBER WORKSPACE</p><h2>Your box,<br /><em>in the flow.</em></h2><p>Sign in to work with your box’s live workouts and results. Access is checked against your active membership.</p></div>
      {session && <button className="button button-outline" onClick={signOut}>Sign out</button>}
    </div>
    {!config && <div className="connected-card"><h3>Sign-in needs setup</h3><p>Configure the OIDC issuer and client ID in <code>web/.env.local</code>. See the local development guide.</p></div>}
    {config && !session && <div className="connected-card auth-card"><div><span className="connected-icon">↗</span><h3>Sign in to your box</h3><p>Continue with your organization’s identity provider. The public demo remains available without an account.</p></div><button className="button button-dark" onClick={signIn}>Continue with secure sign-in <span aria-hidden="true">↗</span></button></div>}
    {session && <>
      <div className="connected-toolbar"><label>Box ID<input value={boxId} onChange={event => changeBox(event.target.value)} placeholder="Paste your box ID" spellCheck={false} /></label><label>Workspace<select value={selectedRole} onChange={event => setSelectedRole(event.target.value as Role)}><option value="BOX_OWNER">Box owner</option><option value="COACH">Coach</option><option value="ATHLETE">Athlete</option></select></label><span className="connected-session">Signed in · token held for this tab</span></div>
      {!boxId && <div className="connected-onboarding"><div><p className="demo-overline">GET STARTED</p><h3>Create your box</h3><p>Creating a box makes your signed-in account its first owner. Coaches and athletes can instead enter an existing box ID supplied by an owner.</p></div><form className="workspace-form" onSubmit={event => void createBox(event)}><label>Box name<input name="boxName" required maxLength={120} /></label><label>Time zone<input name="timeZone" required defaultValue={Intl.DateTimeFormat().resolvedOptions().timeZone || "UTC"} /></label><button className="button button-dark" disabled={busy}>Create box</button></form></div>}
      {boxId && config && token && <Workspace config={config} session={session} boxId={boxId} role={selectedRole} onError={setError} busy={busy} setBusy={setBusy} />}
    </>}
    {error && <div className="connected-error" role="alert">{error}</div>}
  </section>;
}

function Workspace({ config, session, boxId, role, onError, busy, setBusy }: { config: OidcConfig; session: Session; boxId: string; role: Role; onError: (value: string) => void; busy: boolean; setBusy: (value: boolean) => void }) {
  const [exercises, setExercises] = useState<Exercise[]>([]);
  const [drafts, setDrafts] = useState<Draft[]>([]);
  const [schedule, setSchedule] = useState<Publication[]>([]);
  const [history, setHistory] = useState<Result[]>([]);
  const [bests, setBests] = useState<Best[]>([]);
  const [date, setDate] = useState(today());
  const [selectedPublication, setSelectedPublication] = useState("");
  const currentPublication = schedule.find(item => item.id === selectedPublication) ?? schedule[0];
  const actions = useMemo(() => ({
    call: <T,>(path: string, init?: RequestInit) => apiRequest<T>(config, session, boxId, path, init),
  }), [config, session, boxId]);
  async function load() {
    onError(""); setBusy(true);
    try {
      if (role === "COACH") setExercises(await actions.call<Exercise[]>("/api/coaches/exercises"));
      else {
        const [workouts, results, records] = await Promise.all([
          actions.call<Publication[]>(`/api/athletes/workouts?date=${encodeURIComponent(date)}`),
          actions.call<Result[]>("/api/athletes/results"),
          actions.call<Best[]>("/api/athletes/personal-bests"),
        ]);
        setSchedule(workouts); setHistory(results); setBests(records);
      }
    } catch (reason) { onError(parseError(reason)); }
    finally { setBusy(false); }
  }
  useEffect(() => { void load(); }, [role, boxId, date]);
  async function submit(event: FormEvent<HTMLFormElement>, work: (form: FormData) => Promise<void>) {
    event.preventDefault(); onError(""); setBusy(true);
    try { await work(new FormData(event.currentTarget)); await load(); }
    catch (reason) { onError(parseError(reason)); }
    finally { setBusy(false); }
  }
  if (role === "BOX_OWNER") return <OwnerMemberships actions={actions} onError={onError} busy={busy} setBusy={setBusy} />;
  if (role === "COACH") return <div className="workspace-card">
    <div className="workspace-card-heading"><div><p className="demo-overline">COACH DESK</p><h3>Programming and exercise library</h3></div><button className="button button-outline" onClick={() => void load()} disabled={busy}>Refresh</button></div>
    <div className="workspace-columns">
      <form className="workspace-form" onSubmit={event => void submit(event, async form => {
        const created = await actions.call<Exercise>("/api/coaches/exercises", { method: "POST", body: JSON.stringify({ name: String(form.get("name")), category: String(form.get("category")), equipment: [] }) });
        setExercises(items => [created, ...items]);
      })}><h4>Add exercise</h4><label>Name<input required name="name" maxLength={120} /></label><label>Category<input required name="category" maxLength={80} /></label><button className="button button-dark" disabled={busy}>Save exercise</button></form>
      <form className="workspace-form" onSubmit={event => void submit(event, async form => {
        const exerciseId = String(form.get("exerciseId"));
        if (!exerciseId) throw new Error("Add an exercise before creating a workout.");
        const reps = Number(form.get("reps"));
        const title = String(form.get("title"));
        const formatKind = String(form.get("format"));
        const duration = Number(form.get("duration"));
        const block = { title: String(form.get("blockTitle")), intent: "CONDITIONING", format: formatKind === "AMRAP" ? { kind: "AMRAP", durationSeconds: duration } : { kind: "FOR_TIME", timeCapSeconds: duration || null }, movements: [{ exerciseId, movementName: String(form.get("movementName")), variants: { RX: [{ kind: "REPETITIONS", value: String(reps) }], SCALED: [{ kind: "REPETITIONS", value: String(Math.max(1, Math.floor(reps * 0.75))) }], BEGINNER: [{ kind: "REPETITIONS", value: String(Math.max(1, Math.floor(reps * 0.5))) }] } }] };
        const created = await actions.call<Draft>("/api/coaches/workouts", { method: "POST", body: JSON.stringify({ title, blocks: [block] }) });
        setDrafts(items => [created, ...items]);
      })}><h4>Create workout draft</h4><label>Session title<input name="title" required maxLength={160} /></label><label>Exercise<select name="exerciseId" required defaultValue=""><option value="" disabled>Select an exercise</option>{exercises.map(item => <option key={item.id} value={item.id}>{item.name}</option>)}</select></label><label>Movement name<input name="movementName" required /></label><div className="workspace-form-row"><label>Block title<input name="blockTitle" required defaultValue="Conditioning" /></label><label>Format<select name="format"><option value="FOR_TIME">For time</option><option value="AMRAP">AMRAP</option></select></label></div><div className="workspace-form-row"><label>Repetitions<input name="reps" type="number" min="1" required defaultValue="21" /></label><label>{"Duration / time cap (seconds)"}<input name="duration" type="number" min="1" defaultValue="600" /></label></div><button className="button button-dark" disabled={busy}>Save draft</button></form>
    </div>
    <div className="live-list"><h4>Exercises in this box</h4>{exercises.length ? exercises.map(item => <div className="live-list-row" key={item.id}><strong>{item.name}</strong><span>{item.category}</span><code>{item.id}</code></div>) : <p>No exercises loaded yet.</p>}</div>
    <div className="live-list"><h4>Drafts created this session</h4>{drafts.length ? drafts.map(item => <DraftRow key={item.id} draft={item} config={config} session={session} boxId={boxId} onError={onError} onBusy={setBusy} onPublished={load} />) : <p>Create a draft to publish a dated immutable workout.</p>}</div>
  </div>;
  return <div className="workspace-card">
    <div className="workspace-card-heading"><div><p className="demo-overline">ATHLETE TRAINING</p><h3>Today, history and personal bests</h3></div><label className="date-control">Schedule date<input type="date" value={date} onChange={event => setDate(event.target.value)} /></label></div>
    <div className="live-list"><h4>Published sessions</h4>{schedule.length ? schedule.map(item => <button className={`publication-choice ${currentPublication?.id === item.id ? "selected" : ""}`} key={item.id} onClick={() => setSelectedPublication(item.id)}><strong>{item.title}</strong><span>Published v{item.version} · {item.scheduledFor}</span></button>) : <p>No published workouts are scheduled for this date.</p>}</div>
    {currentPublication && <PublicationCard publication={currentPublication} actions={actions} onError={onError} onBusy={setBusy} onSaved={load} busy={busy} />}
    <div className="workspace-columns"><div className="live-list"><h4>Workout history</h4>{history.length ? history.map(item => <div className="live-list-row" key={item.id}><strong>{scoreLabel(item.score)}</strong><span>{item.prescriptionId}</span><code>{item.publicationId.slice(0, 8)}</code></div>) : <p>No results yet.</p>}</div><div className="live-list"><h4>Personal bests</h4>{bests.length ? bests.map(item => <div className="live-list-row" key={`${item.metricId}-${item.prescriptionId}`}><strong>{scoreLabel(item.score)}</strong><span>{item.prescriptionId}</span><code>{item.metricId.slice(0, 8)}</code></div>) : <p>Personal bests appear after you log a result.</p>}</div></div>
  </div>;
}

function OwnerMemberships({ actions, onError, busy, setBusy }: { actions: { call: <T>(path: string, init?: RequestInit) => Promise<T> }; onError: (value: string) => void; busy: boolean; setBusy: (value: boolean) => void }) {
  const [members, setMembers] = useState<Membership[]>([]);
  async function loadMembers() {
    setBusy(true); onError("");
    try { setMembers(await actions.call<Membership[]>("/api/box-owner/memberships")); }
    catch (reason) { onError(parseError(reason)); }
    finally { setBusy(false); }
  }
  useEffect(() => { void loadMembers(); }, []);
  async function addMember(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); const element = event.currentTarget; const form = new FormData(element); setBusy(true); onError("");
    try {
      await actions.call<Membership>("/api/box-owner/memberships", { method: "POST", body: JSON.stringify({ userId: String(form.get("userId")), role: String(form.get("role")) }) });
      element.reset(); await loadMembers();
    } catch (reason) { onError(parseError(reason)); }
    finally { setBusy(false); }
  }
  async function deactivate(userId: string) {
    setBusy(true); onError("");
    try { await actions.call<Membership>(`/api/box-owner/memberships/${encodeURIComponent(userId)}`, { method: "DELETE" }); await loadMembers(); }
    catch (reason) { onError(parseError(reason)); }
    finally { setBusy(false); }
  }
  async function updateRole(userId: string, role: Role) {
    setBusy(true); onError("");
    try { await actions.call<Membership>(`/api/box-owner/memberships/${encodeURIComponent(userId)}`, { method: "PATCH", body: JSON.stringify({ role }) }); await loadMembers(); }
    catch (reason) { onError(parseError(reason)); }
    finally { setBusy(false); }
  }
  return <div className="workspace-card"><div className="workspace-card-heading"><div><p className="demo-overline">BOX OWNER</p><h3>Memberships</h3><p>Add a coach or athlete by their identity-provider user ID. Roles are enforced by the API for this box.</p></div><button className="button button-outline" onClick={() => void loadMembers()} disabled={busy}>Refresh</button></div>
    <form className="workspace-form membership-form" onSubmit={event => void addMember(event)}><h4>Add or reactivate a member</h4><label>User ID<input name="userId" required /></label><label>Role<select name="role"><option value="COACH">Coach</option><option value="ATHLETE">Athlete</option><option value="BOX_OWNER">Box owner</option></select></label><button className="button button-dark" disabled={busy}>Add membership</button></form>
    <div className="live-list"><h4>Members in this box</h4>{members.length ? members.map(member => <MembershipRow key={member.id} member={member} disabled={busy} onDeactivate={deactivate} onUpdateRole={updateRole} />) : <p>No memberships loaded.</p>}</div>
  </div>;
}

function MembershipRow({ member, disabled, onDeactivate, onUpdateRole }: { member: Membership; disabled: boolean; onDeactivate: (id: string) => Promise<void>; onUpdateRole: (id: string, role: Role) => Promise<void> }) {
  const [role, setRole] = useState<Role>(member.role);
  return <div className="member-row"><div><strong>{member.userId}</strong><small>{member.status}</small></div>{member.status === "ACTIVE" && <><select aria-label={`Role for ${member.userId}`} value={role} onChange={event => setRole(event.target.value as Role)}>{["BOX_OWNER", "COACH", "ATHLETE"].map(value => <option key={value} value={value}>{value.replaceAll("_", " ")}</option>)}</select><button type="button" className="subtle-button" disabled={disabled || role === member.role} onClick={() => void onUpdateRole(member.userId, role)}>Save role</button><button type="button" className="subtle-button" disabled={disabled} onClick={() => void onDeactivate(member.userId)}>Deactivate</button></>}</div>;
}

function DraftRow({ draft, config, session, boxId, onError, onBusy, onPublished }: { draft: Draft; config: OidcConfig; session: Session; boxId: string; onError: (value: string) => void; onBusy: (value: boolean) => void; onPublished: () => Promise<void> }) {
  const [title, setTitle] = useState(draft.title);
  const [savedTitle, setSavedTitle] = useState(draft.title);
  async function rename() {
    onBusy(true); onError("");
    try { await apiRequest<Draft>(config, session, boxId, `/api/coaches/workouts/${draft.id}/title`, { method: "PUT", body: JSON.stringify({ title }) }); setSavedTitle(title.trim()); await onPublished(); }
    catch (reason) { onError(parseError(reason)); }
    finally { onBusy(false); }
  }
  return <form className="publish-row" onSubmit={event => {
    event.preventDefault(); const form = new FormData(event.currentTarget); onBusy(true); onError("");
    apiRequest<Publication>(config, session, boxId, `/api/coaches/workouts/${draft.id}/publish`, { method: "POST", body: JSON.stringify({ scheduledFor: String(form.get("scheduledFor")) }) })
      .then(() => onPublished()).catch(error => onError(parseError(error))).finally(() => onBusy(false));
  }}><label>Draft title<input value={title} onChange={event => setTitle(event.target.value)} maxLength={160} /></label><span><small>Draft v{draft.version} · {draft.blocks.length} blocks</small><button className="subtle-button" type="button" disabled={!title.trim() || title.trim() === savedTitle} onClick={() => void rename()}>Save title</button></span><label>Publish date<input name="scheduledFor" type="date" required defaultValue={today()} /></label><button className="button button-lime" type="submit">Publish snapshot</button></form>;
}

function PublicationCard({ publication, actions, onError, onBusy, onSaved, busy }: { publication: Publication; actions: { call: <T>(path: string, init?: RequestInit) => Promise<T> }; onError: (value: string) => void; onBusy: (value: boolean) => void; onSaved: () => Promise<void>; busy: boolean }) {
  const [scoreType, setScoreType] = useState("TIME");
  const [prescription, setPrescription] = useState("RX");
  const firstMovement = publication.blocks.flatMap(block => block.movements)[0];
  async function log(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); if (!firstMovement) { onError("This publication has no movement to score."); return; }
    const form = new FormData(event.currentTarget); const value = String(form.get("score"));
    let score: Record<string, unknown>;
    switch (scoreType) {
      case "TIME": { const [minutes, seconds] = value.split(":").map(Number); score = { type: "TIME", elapsedSeconds: minutes * 60 + seconds, timeOutcome: "COMPLETED" }; break; }
      case "LOAD": score = { type: "LOAD", amount: Number(value), unit: String(form.get("unit")) }; break;
      case "REPS": score = { type: "REPS", reps: Number(value) }; break;
      case "ROUNDS_AND_REPS": { const [rounds, additionalReps] = value.split("+").map(part => Number(part.trim())); score = { type: "ROUNDS_AND_REPS", rounds, additionalReps }; break; }
      case "DISTANCE": score = { type: "DISTANCE", amount: Number(value), unit: String(form.get("unit")) }; break;
      case "CALORIES": score = { type: "CALORIES", calories: Number(value) }; break;
      default: score = { type: "COMPLETION", completionStatus: value };
    }
    onBusy(true); onError("");
    try {
      await actions.call<Result>("/api/athletes/results", { method: "POST", body: JSON.stringify({ publicationId: publication.id, metricId: firstMovement.exerciseId, prescriptionId: prescription, score }) });
      await onSaved();
    } catch (reason) { onError(parseError(reason)); }
    finally { onBusy(false); }
  }
  return <article className="publication-card"><p className="demo-overline">PUBLISHED VERSION {publication.version} · {publication.scheduledFor}</p><h4>{publication.title}</h4>{publication.blocks.map(block => <section className="publication-block" key={block.id}><strong>{block.title}</strong><span>{block.intent.replaceAll("_", " ")}</span>{block.movements.map(move => <p key={move.exerciseId}>{move.movementName}: {Object.entries(move.variants).map(([variant, targets]) => `${variant} ${targets.map(target => target.value).join(" + ")}`).join(" · ")}</p>)}</section>)}
    <form className="score-live-form" onSubmit={event => void log(event)}><h4>Log a result for this publication</h4><div className="workspace-form-row"><label>Prescription<select value={prescription} onChange={event => setPrescription(event.target.value)}>{["RX", "SCALED", "BEGINNER"].map(value => <option key={value}>{value}</option>)}</select></label><label>Score type<select value={scoreType} onChange={event => setScoreType(event.target.value)}>{["TIME", "LOAD", "REPS", "ROUNDS_AND_REPS", "DISTANCE", "CALORIES", "COMPLETION"].map(value => <option key={value}>{value}</option>)}</select></label></div><label>Score{scoreType === "COMPLETION" ? <select name="score"><option value="COMPLETED">Completed</option><option value="NOT_COMPLETED">Not completed</option></select> : <input name="score" required type={scoreType === "TIME" || scoreType === "ROUNDS_AND_REPS" ? "text" : "number"} min="0" step={scoreType === "LOAD" || scoreType === "DISTANCE" ? "0.01" : "1"} placeholder={scoreType === "TIME" ? "08:42" : scoreType === "ROUNDS_AND_REPS" ? "5 + 8" : "Enter score"} />}</label>{(scoreType === "LOAD" || scoreType === "DISTANCE") && <label>Unit<select name="unit">{(scoreType === "LOAD" ? ["KILOGRAM", "POUND"] : ["METER", "KILOMETER", "MILE", "FOOT"]).map(unit => <option key={unit}>{unit}</option>)}</select></label>}<button className="button button-dark" disabled={busy}>Save result</button></form>
  </article>;
}

function scoreLabel(score: Result["score"]) {
  switch (score.type) {
    case "TIME": return `${Math.floor((score.elapsedSeconds ?? 0) / 60)}:${String((score.elapsedSeconds ?? 0) % 60).padStart(2, "0")}`;
    case "LOAD": case "DISTANCE": return `${score.amount} ${score.unit}`;
    case "REPS": return `${score.reps} reps`;
    case "ROUNDS_AND_REPS": return `${score.rounds} + ${score.additionalReps}`;
    case "CALORIES": return `${score.calories} cal`;
    default: return score.completionStatus ?? score.type;
  }
}
