import { type FormEvent, useState } from "react";
import AuthenticatedWorkspace from "./AuthenticatedWorkspace";

type DemoRole = "owner" | "coach" | "athlete";
type Prescription = "RX" | "Scaled" | "Beginner";
type ScoreType = "Time" | "Load" | "Reps" | "Rounds + reps" | "Distance" | "Calories" | "Completion";

const roleLabels: Record<DemoRole, string> = {
  owner: "Box owner",
  coach: "Coach",
  athlete: "Athlete",
};

function Arrow({ diagonal = false }: { diagonal?: boolean }) {
  return <span aria-hidden="true">{diagonal ? "↗" : "→"}</span>;
}

function Header({ onDemo }: { onDemo: () => void }) {
  return (
    <header className="site-header">
      <a className="wordmark" href="#top" aria-label="Ctosfit home">
        <span className="brand-mark" aria-hidden="true">C</span>
        <span>ctosfit</span>
      </a>
      <nav className="main-nav" aria-label="Main navigation">
        <a href="#how-it-works">How it works</a>
        <a href="#for-boxes">For your box</a>
        <a href="#member-workspace">Sign in</a>
      </nav>
      <button className="button button-dark header-cta" onClick={onDemo}>
        Explore the demo <Arrow diagonal />
      </button>
    </header>
  );
}

function SessionPreview() {
  return (
    <div className="preview-shell" aria-label="Preview of a workout plan">
      <div className="preview-windowbar">
        <span className="window-dot" />
        <span className="window-dot" />
        <span className="window-dot" />
        <span className="window-label">COACH DESK / TODAY</span>
      </div>
      <div className="preview-content">
        <div className="preview-topline">
          <span>MONDAY, 24 NOVEMBER</span>
          <span className="live-pill"><i /> Published</span>
        </div>
        <h2>Engine room</h2>
        <p className="preview-subtitle">A steady push, then a fast finish.</p>
        <div className="preview-block">
          <span className="block-index">01</span>
          <div><strong>Strength</strong><p>Back squat · 5 × 5</p></div>
          <span className="block-time">18 min</span>
        </div>
        <div className="preview-block preview-block-active">
          <span className="block-index">02</span>
          <div><strong>Conditioning</strong><p>AMRAP · 12 minutes</p></div>
          <span className="block-time">12 min</span>
        </div>
        <div className="preview-movements">
          <span>10 cal row</span><b>·</b><span>12 box jumps</span><b>·</b><span>14 sit-ups</span>
        </div>
        <div className="preview-footer">
          <div className="avatar-stack" aria-hidden="true"><i>J</i><i>M</i><i>A</i><i>+</i></div>
          <span>24 athletes ready</span>
          <span className="spark-bars" aria-label="Strong attendance trend"><i /><i /><i /><i /><i /><i /><i /></span>
        </div>
      </div>
      <div className="floating-note">
        <span className="note-icon">✓</span>
        <span><strong>One clear plan</strong><small>Every athlete sees the same version</small></span>
      </div>
    </div>
  );
}

function Landing({ onDemo }: { onDemo: () => void }) {
  return (
    <>
      <section className="hero-section" id="top">
        <div className="hero-copy">
          <p className="eyebrow"><span className="eyebrow-line" /> WORKOUTS, IN SYNC</p>
          <h1>Make great<br />training <em>feel simple.</em></h1>
          <p className="hero-description">
            One home for the work your coaches program and the progress your athletes make.
            Built for the rhythm of a CrossFit box.
          </p>
          <div className="hero-actions">
            <button className="button button-lime" onClick={onDemo}>Explore the demo <Arrow diagonal /></button>
            <a className="text-link" href="#how-it-works">See how it works <Arrow /></a>
          </div>
          <div className="hero-proof">
            <span className="proof-star" aria-hidden="true">✳</span>
            <span>For the coach on the whiteboard<br />and the athlete on the floor.</span>
          </div>
        </div>
        <div className="hero-visual">
          <div className="visual-orbit orbit-one" />
          <div className="visual-orbit orbit-two" />
          <div className="visual-label label-top">PROGRAM WITH INTENT <span>↗</span></div>
          <SessionPreview />
          <div className="visual-label label-bottom"><span className="lime-square" /> BUILT AROUND THE WORKOUT</div>
        </div>
      </section>

      <section className="signal-band" aria-label="Ctosfit workflow">
        <span>DESIGN THE SESSION</span><b>↗</b><span>PUBLISH WITH CONFIDENCE</span><b>↗</b><span>TRACK WHAT MATTERS</span>
      </section>

      <section className="workflow-section" id="how-it-works">
        <div className="section-heading">
          <p className="eyebrow"><span className="eyebrow-line" /> THE WORKFLOW</p>
          <h2>From the first rep<br />to the next <em>personal best.</em></h2>
          <p>Programming, publishing and progress belong in one flow. Ctosfit keeps the details connected so your people can focus on training.</p>
        </div>
        <div className="workflow-grid">
          <article className="workflow-card">
            <span className="card-number">01 / PROGRAM</span>
            <div className="card-illustration program-illustration" aria-hidden="true">
              <div className="illustration-line line-a" /><div className="illustration-line line-b" /><div className="illustration-line line-c" />
              <span className="illustration-tag">AMRAP · 12:00</span><span className="illustration-dot dot-a" /><span className="illustration-dot dot-b" />
            </div>
            <h3>Build the session,<br />block by block.</h3>
            <p>Give every part of the day its structure, movements and intent. Your programming stays easy to scan and easy to reuse.</p>
          </article>
          <article className="workflow-card">
            <span className="card-number">02 / PUBLISH</span>
            <div className="card-illustration publish-illustration" aria-hidden="true">
              <div className="publish-sheet"><span>MONDAY WOD</span><i /><i /><i /></div><span className="publish-stamp">READY<br />TO GO</span>
            </div>
            <h3>One version.<br />The whole box.</h3>
            <p>When a coach publishes, athletes get the same clear plan. The published workout remains part of your training history.</p>
          </article>
          <article className="workflow-card">
            <span className="card-number">03 / PROGRESS</span>
            <div className="card-illustration progress-illustration" aria-hidden="true">
              <div className="progress-number">08:42<small>BEST TIME</small></div><div className="progress-chart"><i /><i /><i /><i /><i /><i /><i /></div>
            </div>
            <h3>Results that<br />mean something.</h3>
            <p>Typed scores stay tied to the workout and the athlete. Review history and recognize progress without losing context.</p>
          </article>
        </div>
      </section>

      <section className="box-section" id="for-boxes">
        <div className="box-section-inner">
          <div className="box-copy">
            <p className="eyebrow"><span className="eyebrow-line" /> MADE FOR BOX LIFE</p>
            <h2>Less chasing.<br /><em>More coaching.</em></h2>
            <p>Give owners a clear view of the box, coaches a focused place to program, and athletes a simple way to show up and log the work.</p>
            <button className="button button-light" onClick={onDemo}>See the roles in action <Arrow diagonal /></button>
          </div>
          <div className="role-stack">
            <div className="role-row"><span className="role-symbol owner-symbol">O</span><span><strong>Owner</strong><small>Keep the box organized</small></span><span className="role-arrow">↗</span></div>
            <div className="role-row"><span className="role-symbol coach-symbol">C</span><span><strong>Coach</strong><small>Program with a plan</small></span><span className="role-arrow">↗</span></div>
            <div className="role-row"><span className="role-symbol athlete-symbol">A</span><span><strong>Athlete</strong><small>See the work. Log the result.</small></span><span className="role-arrow">↗</span></div>
            <div className="role-note"><span className="lime-square" /> One training story, from both sides of the whiteboard.</div>
          </div>
        </div>
      </section>

      <section className="closing-section">
        <div className="closing-grid" aria-hidden="true">{Array.from({ length: 8 }, (_, index) => <i key={index} />)}</div>
        <div className="closing-content">
          <p className="eyebrow"><span className="eyebrow-line" /> TAKE A LOOK AROUND</p>
          <h2>Your next training<br />day starts <em>here.</em></h2>
          <button className="button button-lime" onClick={onDemo}>Explore the demo <Arrow diagonal /></button>
          <p className="closing-footnote">A guided preview with sample box data. No account or live data required.</p>
        </div>
      </section>
    </>
  );
}

function OwnerDemo() {
  const [invitePreview, setInvitePreview] = useState(false);
  return (
    <div className="demo-layout">
      <aside className="demo-sidebar">
        <p className="sidebar-kicker">YOUR BOX</p>
        <div className="box-identity"><span className="box-logo">N</span><span><strong>Northline</strong><small>CrossFit · Lisbon</small></span></div>
        <div className="sidebar-rule" />
        <a className="sidebar-link active" href="#demo"><span>▦</span> Overview</a>
        <a className="sidebar-link" href="#demo"><span>♙</span> Members</a>
        <a className="sidebar-link" href="#demo"><span>◷</span> Programming</a>
        <p className="sidebar-kicker sidebar-bottom-label">BOX TOOLS</p>
        <a className="sidebar-link" href="#demo"><span>⚙</span> Settings</a>
        <div className="sidebar-profile"><span className="profile-avatar">LV</span><span><strong>Lucas Vieira</strong><small>Box owner</small></span><span className="more-dots">···</span></div>
      </aside>
      <div className="demo-main">
        <div className="demo-topbar"><span>Monday, November 24, 2026</span><span className="demo-context">NORTHLINE / OWNER VIEW</span></div>
        <div className="demo-page-heading"><div><p className="demo-overline">BOX OVERVIEW</p><h3>Good morning, Lucas <span>✳</span></h3><p>Here’s what’s happening at Northline today.</p></div><button className="button button-dark compact-button" onClick={() => setInvitePreview(true)}>＋ Invite member</button></div>
        {invitePreview && <div className="demo-toast" role="status"><span>Demo action</span> Invitation preview opened. No invite was sent. <button onClick={() => setInvitePreview(false)} aria-label="Dismiss message">×</button></div>}
        <div className="metric-grid">
          <div className="metric-card"><span>ACTIVE MEMBERS</span><strong>128</strong><small><b>+6</b> this month</small><span className="metric-graphic mini-bars"><i /><i /><i /><i /><i /><i /></span></div>
          <div className="metric-card"><span>COACHES</span><strong>6</strong><small>Across 12 weekly sessions</small><span className="metric-graphic member-dots">● ● ● ● ● <i>+1</i></span></div>
          <div className="metric-card"><span>THIS WEEK</span><strong>18 <small>sessions</small></strong><small>4 workouts ready to publish</small><span className="metric-graphic week-strip"><i /><i /><i /><i /><i /><i /><i /></span></div>
        </div>
        <div className="dashboard-lower">
          <section className="panel today-panel"><div className="panel-heading"><div><span className="panel-kicker">ON THE FLOOR</span><h4>Today’s programming</h4></div><a href="#demo">View schedule <Arrow /></a></div><div className="schedule-date"><strong>MON</strong><span>24</span><small>NOV</small></div><div className="schedule-details"><div className="schedule-row"><span className="schedule-time">06:30</span><span className="session-status session-published" /><span><strong>Engine room</strong><small>Published · 18 athletes</small></span><b>↗</b></div><div className="schedule-row"><span className="schedule-time">12:00</span><span className="session-status session-draft" /><span><strong>Midday strength</strong><small>Draft · Coach Maya</small></span><b>···</b></div><div className="schedule-row"><span className="schedule-time">18:00</span><span className="session-status session-published" /><span><strong>Engine room</strong><small>Published · 22 athletes</small></span><b>↗</b></div></div></section>
          <section className="panel activity-panel"><div className="panel-heading"><div><span className="panel-kicker">YOUR COMMUNITY</span><h4>Recent activity</h4></div><button className="icon-button" aria-label="More activity options">···</button></div><div className="activity-row"><span className="activity-avatar aqua">MR</span><span><strong>Maya Ribeiro</strong><small>Published a workout</small></span><time>18m</time></div><div className="activity-row"><span className="activity-avatar sand">TG</span><span><strong>Tomás Gomes</strong><small>Logged a new personal best</small></span><time>1h</time></div><div className="activity-row"><span className="activity-avatar violet">IF</span><span><strong>Inês Ferreira</strong><small>Joined the box</small></span><time>3h</time></div><div className="activity-footer"><span>MEMBERSHIP HEALTH</span><strong>Looking good <i /></strong></div></section>
        </div>
        <p className="demo-footnote">Northline is a fictional sample box. All displayed data is for demonstration only.</p>
      </div>
    </div>
  );
}

function CoachDemo() {
  const [published, setPublished] = useState(false);
  const [addedBlock, setAddedBlock] = useState(false);
  return (
    <div className="coach-demo">
      <div className="coach-sidebar"><div className="coach-brand"><span className="box-logo">N</span><span><strong>Northline</strong><small>Coach workspace</small></span></div><div className="coach-nav-label">WORKSPACE</div><a className="coach-nav active" href="#demo">◷ <span>Program</span></a><a className="coach-nav" href="#demo">▧ <span>Exercise library</span></a><a className="coach-nav" href="#demo">▤ <span>Templates</span></a><div className="coach-week"><small>THIS WEEK</small><span>Mon 24 <i>Selected</i></span><span>Tue 25</span><span>Wed 26</span><span>Thu 27</span></div><div className="coach-user"><span className="activity-avatar aqua">MR</span><span><strong>Maya Ribeiro</strong><small>Coach</small></span></div></div>
      <div className="coach-content"><div className="coach-topline"><span>MONDAY, NOVEMBER 24</span><span>WEEK 48 / 2026</span></div><div className="coach-title-row"><div><p className="demo-overline">WORKOUT BUILDER</p><h3>Monday training</h3><p>Plan the work. Your athletes see the published version.</p></div><span className={`publish-state ${published ? "state-live" : "state-draft"}`}><i /> {published ? "Published · v1" : "Draft"}</span></div><div className="workout-editor"><div className="editor-title"><div><span className="panel-kicker">SESSION NAME</span><h4>Engine room</h4></div><button className="icon-button" aria-label="More workout options">···</button></div><label className="editor-field">COACH NOTE<textarea defaultValue="Keep the first set controlled. Build pace each round." /></label><div className="editor-block-heading"><span className="panel-kicker">WORKOUT BLOCKS <b>2</b></span><button className="subtle-button" onClick={() => setAddedBlock(true)}>＋ Add block</button></div><article className="editor-block"><span className="editor-block-num">01</span><span className="editor-block-kind">STRENGTH</span><div><strong>Back squat</strong><p>5 sets · 5 reps · Rest 2:00</p></div><span className="editor-block-more">···</span></article><article className="editor-block highlighted-block"><span className="editor-block-num">02</span><span className="editor-block-kind">CONDITIONING</span><div><strong>AMRAP · 12 minutes</strong><p>10 cal row · 12 box jumps · 14 sit-ups</p></div><span className="editor-block-more">···</span></article>{addedBlock && <article className="editor-block added-block" role="status"><span className="editor-block-num">03</span><span className="editor-block-kind">NEW BLOCK</span><div><strong>New block added</strong><p>Demo-only draft block</p></div><button className="remove-block" onClick={() => setAddedBlock(false)}>Remove</button></article>}<div className="prescription-row"><span className="panel-kicker">PRESCRIPTIONS</span><span>RX</span><span>Scaled</span><span>Beginner</span><button className="subtle-button">Edit</button></div><div className="editor-footer"><span>Last edited just now</span><div><button className="button button-outline" onClick={() => setPublished(false)}>Save draft</button><button className="button button-lime" onClick={() => setPublished(true)}>{published ? "Published ✓" : "Publish workout"}</button></div></div></div><p className="demo-footnote">Demo changes are temporary and are not saved or sent to a server.</p></div>
    </div>
  );
}

function AthleteDemo() {
  const [prescription, setPrescription] = useState<Prescription>("RX");
  const [scoreType, setScoreType] = useState<ScoreType>("Time");
  const [score, setScore] = useState("");
  const [savedScore, setSavedScore] = useState("");
  const [error, setError] = useState("");

  function isValidScore(value: string): boolean {
    switch (scoreType) {
      case "Time": return /^(?:[0-9]+):[0-5][0-9]$/.test(value);
      case "Load":
      case "Distance": return /^(?:0|[1-9]\d*)(?:\.\d{1,2})?$/.test(value);
      case "Reps":
      case "Calories": return /^(?:0|[1-9]\d*)$/.test(value);
      case "Rounds + reps": return /^(?:0|[1-9]\d*)\s*\+\s*(?:0|[1-9]\d*)$/.test(value);
      case "Completion": return value === "Completed" || value === "Not completed";
    }
  }

  function submitScore(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const value = score.trim();
    if (!isValidScore(value)) {
      setError(scoreType === "Time" ? "Enter a time as mm:ss (for example, 08:42)." : "Enter a valid score for the selected type.");
      return;
    }
    setError("");
    setSavedScore(value);
  }

  const unit = scoreType === "Time" ? "mm:ss" : scoreType === "Load" ? "kg" : scoreType === "Reps" ? "reps" : scoreType === "Rounds + reps" ? "rounds + reps" : scoreType === "Distance" ? "m" : scoreType === "Calories" ? "cal" : "status";
  return (
    <div className="athlete-demo">
      <header className="athlete-topbar"><a className="wordmark" href="#top"><span className="brand-mark">C</span><span>ctosfit</span></a><span className="athlete-box-label"><span className="box-logo">N</span> Northline</span><span className="profile-avatar">JR</span></header>
      <div className="athlete-layout"><nav className="athlete-nav" aria-label="Athlete navigation"><a className="active" href="#demo"><span>▦</span> Today</a><a href="#demo"><span>▧</span> History</a><a href="#demo"><span>↗</span> Progress</a><div className="athlete-nav-bottom"><span className="profile-avatar">JR</span><span><strong>Joana Reis</strong><small>Athlete</small></span></div></nav>
        <main className="athlete-main"><div className="athlete-date"><span>MONDAY, 24 NOVEMBER</span><span>YOUR TRAINING</span></div><div className="athlete-heading"><div><p className="demo-overline">TODAY AT NORTHLINE</p><h3>Engine room</h3><p>Coach Maya · Published at 06:12</p></div><span className="published-chip"><i /> Published v1</span></div><div className="athlete-workout-card"><div className="workout-section"><div className="workout-section-head"><span className="section-step">01</span><span><small>STRENGTH</small><strong>Back squat</strong></span><span className="section-detail">5 × 5<br />Rest 2:00</span></div><div className="athlete-note">Build across. Leave one good rep in the tank.</div></div><div className="workout-section"><div className="workout-section-head"><span className="section-step">02</span><span><small>CONDITIONING · AMRAP 12</small><strong>Move at your pace</strong></span><span className="section-detail">12 min</span></div><div className="movement-list"><div><span>10</span><small>cal row</small></div><b>+</b><div><span>12</span><small>box jumps</small></div><b>+</b><div><span>14</span><small>sit-ups</small></div></div></div></div><form className="score-form" onSubmit={submitScore}><div className="score-form-heading"><div><p className="demo-overline">YOUR RESULT</p><h4>Log this workout</h4></div><span className="score-lock">Linked to published v1</span></div><div className="score-fields"><label>Prescription<select value={prescription} onChange={event => { setPrescription(event.target.value as Prescription); setSavedScore(""); setError(""); }}><option>RX</option><option>Scaled</option><option>Beginner</option></select></label><label>Score type<select value={scoreType} onChange={event => { setScoreType(event.target.value as ScoreType); setScore(""); setSavedScore(""); setError(""); }}><option>Time</option><option>Load</option><option>Reps</option><option>Rounds + reps</option><option>Distance</option><option>Calories</option><option>Completion</option></select></label><label className="score-value-label">Your score <span>{unit}</span>{scoreType === "Completion" ? <select value={score} onChange={event => { setScore(event.target.value); setSavedScore(""); setError(""); }}><option value="">Choose status</option><option>Completed</option><option>Not completed</option></select> : <input value={score} onChange={event => { setScore(event.target.value); setSavedScore(""); setError(""); }} placeholder={scoreType === "Time" ? "e.g. 08:42" : scoreType === "Load" ? "e.g. 60" : scoreType === "Reps" ? "e.g. 128" : scoreType === "Rounds + reps" ? "e.g. 5 + 8" : scoreType === "Distance" ? "e.g. 1200" : "e.g. 80"} inputMode={scoreType === "Time" || scoreType === "Rounds + reps" ? "text" : "decimal"} aria-describedby={error ? "score-error" : undefined} />}</label></div>{error && <p className="form-error" id="score-error" role="alert">{error}</p>}<div className="score-submit-row"><span>Your result is only visible to your box.</span><button className="button button-dark" type="submit">Save result <Arrow /></button></div>{savedScore && <div className="saved-score" role="status"><span className="saved-check">✓</span><span><strong>Result saved in this demo</strong><small>{savedScore} {unit} · {prescription} · Engine room, published v1</small></span><button type="button" onClick={() => setSavedScore("")}>Clear</button></div>}</form><div className="athlete-demo-privacy">DEMO ONLY <span>Sample data stays in this browser session. No live box or athlete data is accessed.</span></div></main>
      </div>
    </div>
  );
}

function Demo() {
  const [role, setRole] = useState<DemoRole>("coach");
  return (
    <section className="demo-section" id="demo">
      <div className="demo-section-head"><div><p className="eyebrow"><span className="eyebrow-line" /> PRODUCT WALKTHROUGH</p><h2>Take the <em>floor.</em></h2><p>See a day at Northline from each side of the whiteboard.</p></div><span className="sample-badge"><i /> SAMPLE BOX · DEMO DATA</span></div>
      <div className="demo-frame"><div className="demo-rolebar"><span className="demo-role-prompt">SWITCH YOUR VIEW</span><div className="role-tabs" role="group" aria-label="Choose a demo role">{(Object.keys(roleLabels) as DemoRole[]).map((item) => <button type="button" key={item} aria-pressed={role === item} className={role === item ? "selected" : ""} onClick={() => setRole(item)}>{roleLabels[item]}</button>)}</div><span className="demo-sample-warning">All changes are simulated</span></div>{role === "owner" ? <OwnerDemo /> : role === "coach" ? <CoachDemo /> : <AthleteDemo />}</div>
      <p className="demo-disclaimer">This interactive walkthrough uses fictional Northline data only. Changes stay in this page and reset when you refresh. It does not connect to a live box, account or production API.</p>
    </section>
  );
}

function Footer() {
  return <footer className="site-footer"><a className="wordmark footer-wordmark" href="#top"><span className="brand-mark">C</span><span>ctosfit</span></a><span>Training, in sync.</span><a href="#top">Back to top ↑</a><small>© 2026 Ctosfit · Built for the work.</small></footer>;
}

export default function App() {
  function openDemo() {
    document.getElementById("demo")?.scrollIntoView({ behavior: "smooth", block: "start" });
    window.history.replaceState(null, "", "#demo");
  }

  return <><a className="skip-link" href="#main-content">Skip to content</a><Header onDemo={openDemo} /><main id="main-content"><Landing onDemo={openDemo} /><Demo /><AuthenticatedWorkspace /></main><Footer /></>;
}
