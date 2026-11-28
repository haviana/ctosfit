import { type FormEvent, useState } from "react";
import {
  BLOCK_INTENTS,
  type BlockFormValues,
  type MovementForm,
  type WorkoutDraftView,
  blockFormFrom,
  describeBlockFormat,
  describeIntent,
  describeMovement,
  emptyBlockForm,
  emptyMovement,
  moveListItem,
  nextPosition,
  toBlockRequest,
  validateBlockForm,
} from "./workout-editor";

type ExerciseOption = { id: string; name: string };

type WorkoutDraftEditorProps = {
  draft: WorkoutDraftView;
  exercises: ExerciseOption[];
  disabled?: boolean;
  request: <T>(path: string, init?: RequestInit) => Promise<T>;
  onDraft: (draft: WorkoutDraftView) => void;
  onError: (message: string) => void;
  onBusy?: (busy: boolean) => void;
  onEditingChange?: (editing: boolean) => void;
};

export default function WorkoutDraftEditor({ draft, exercises, disabled = false, request, onDraft, onError, onBusy, onEditingChange }: WorkoutDraftEditorProps) {
  const [addForm, setAddForm] = useState(() => emptyBlockForm(exercises[0]?.id ?? ""));
  const [editingId, setEditingId] = useState<string | null>(null);
  const [editForm, setEditForm] = useState<BlockFormValues | null>(null);
  const [formError, setFormError] = useState<{ scope: string; message: string } | null>(null);
  const [pending, setPending] = useState(false);
  const locked = disabled || pending;

  async function send(path: string, init: RequestInit): Promise<WorkoutDraftView | null> {
    setPending(true);
    onBusy?.(true);
    setFormError(null);
    onError("");
    try {
      const next = await request<WorkoutDraftView>(path, init);
      onDraft(next);
      return next;
    } catch (error) {
      onError(error instanceof Error ? error.message : "The workout could not be updated.");
      return null;
    } finally {
      setPending(false);
      onBusy?.(false);
    }
  }

  function blocksPath(blockId = "") {
    const workout = encodeURIComponent(draft.id);
    const block = blockId ? `/${encodeURIComponent(blockId)}` : "";
    return `/api/coaches/workouts/${workout}/blocks${block}`;
  }

  async function submitBlock(scope: string, values: BlockFormValues, path: string, method: "POST" | "PUT") {
    const message = validateBlockForm(values);
    if (message) {
      setFormError({ scope, message });
      return;
    }
    let block;
    try {
      block = toBlockRequest(values);
    } catch (error) {
      setFormError({ scope, message: error instanceof Error ? error.message : "Check the block fields." });
      return;
    }
    const next = await send(path, { method, body: JSON.stringify(method === "POST" ? { block } : block) });
    if (!next) return;
    if (method === "POST") setAddForm(emptyBlockForm(exercises[0]?.id ?? ""));
    if (method === "PUT") closeEdit();
  }

  function closeEdit() {
    setEditingId(null);
    setEditForm(null);
    setFormError(null);
    onEditingChange?.(false);
  }

  function startEdit(blockId: string) {
    const block = draft.blocks.find((item) => item.id === blockId);
    if (!block) return;
    setEditingId(blockId);
    setEditForm(blockFormFrom(block));
    setFormError(null);
    onEditingChange?.(true);
  }

  return <section className="workout-block-editor" aria-label={`Blocks in ${draft.title}`}>
    <div className="workout-block-heading"><span>Workout blocks</span><b>{String(draft.blocks.length).padStart(2, "0")}</b></div>
    {draft.blocks.length === 0 && <p>This draft has no blocks yet. Add one before publishing.</p>}
    <ol>
      {draft.blocks.map((block, index) => <li key={block.id}>
        <article className="workout-block">
          <header>
            <span>{String(index + 1).padStart(2, "0")}</span>
            <strong>{block.title}</strong>
            <span>{describeIntent(block.intent)}</span>
            <span>{describeBlockFormat(block.format)}</span>
          </header>
          {block.movements.length > 0 && <ol>
            {block.movements.map((movement, movementIndex) => <li key={`${movement.exerciseId}-${movementIndex}`}>{describeMovement(movement)}</li>)}
          </ol>}
          <div className="workout-block-actions">
            <button type="button" className="subtle-button" disabled={locked || index === 0} aria-label={`Move up ${block.title}`} onClick={() => void send(`${blocksPath(block.id)}/position`, { method: "PATCH", body: JSON.stringify({ position: nextPosition(index, "up") }) })}>Move up</button>
            <button type="button" className="subtle-button" disabled={locked || index === draft.blocks.length - 1} aria-label={`Move down ${block.title}`} onClick={() => void send(`${blocksPath(block.id)}/position`, { method: "PATCH", body: JSON.stringify({ position: nextPosition(index, "down") }) })}>Move down</button>
            <button type="button" className="subtle-button" disabled={locked} aria-label={`Edit ${block.title}`} onClick={() => startEdit(block.id)}>Edit</button>
            <button type="button" className="subtle-button" disabled={locked} aria-label={`Remove ${block.title}`} onClick={() => void send(blocksPath(block.id), { method: "DELETE" })}>Remove</button>
          </div>
          {editingId === block.id && editForm && <BlockForm
            legend={`Edit ${block.title}`}
            submitLabel="Save block"
            values={editForm}
            exercises={exercises}
            disabled={locked}
            error={formError?.scope === block.id ? formError.message : ""}
            onChange={setEditForm}
            onCancel={closeEdit}
            onSubmit={(values) => void submitBlock(block.id, values, blocksPath(block.id), "PUT")}
          />}
        </article>
      </li>)}
    </ol>
    <BlockForm
      legend="Add block"
      submitLabel="Add block"
      values={addForm}
      exercises={exercises}
      disabled={locked}
      error={formError?.scope === "add" ? formError.message : ""}
      onChange={setAddForm}
      onSubmit={(values) => void submitBlock("add", values, blocksPath(), "POST")}
    />
  </section>;
}

function BlockForm({
  legend, submitLabel, values, exercises, disabled, error, onChange, onSubmit, onCancel,
}: {
  legend: string;
  submitLabel: string;
  values: BlockFormValues;
  exercises: ExerciseOption[];
  disabled: boolean;
  error: string;
  onChange: (values: BlockFormValues) => void;
  onSubmit: (values: BlockFormValues) => void;
  onCancel?: () => void;
}) {
  const intents = BLOCK_INTENTS.includes(values.intent as typeof BLOCK_INTENTS[number]) || !values.intent
    ? BLOCK_INTENTS
    : [values.intent, ...BLOCK_INTENTS];
  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    onSubmit(values);
  }
  function updateMovement(index: number, patch: Partial<MovementForm>) {
    onChange({ ...values, movements: values.movements.map((movement, movementIndex) => movementIndex === index ? { ...movement, ...patch } : movement) });
  }
  return <form className="workspace-form block-form" aria-label={legend} onSubmit={submit}>
    <h4>{legend}</h4>
    {error && <p className="block-form-error" role="alert">{error}</p>}
    <label>Block title<input value={values.title} maxLength={160} onChange={(event) => onChange({ ...values, title: event.target.value })} /></label>
    <div className="workspace-form-row">
      <label>Intent<select value={values.intent} onChange={(event) => onChange({ ...values, intent: event.target.value })}>{intents.map((intent) => <option key={intent} value={intent}>{describeIntent(intent)}</option>)}</select></label>
      <label>Format<select value={values.formatKind} onChange={(event) => onChange({ ...values, formatKind: event.target.value as BlockFormValues["formatKind"] })}>
        {values.preservedFormat && <option value="UNCHANGED">Keep current format</option>}
        <option value="FOR_TIME">For time</option>
        <option value="AMRAP">AMRAP</option>
      </select></label>
    </div>
    {values.formatKind !== "UNCHANGED" && <label>{values.formatKind === "AMRAP" ? "Duration (seconds)" : "Time cap (seconds, optional)"}<input inputMode="numeric" value={values.duration} onChange={(event) => onChange({ ...values, duration: event.target.value })} /></label>}
    <label>Coach notes<textarea value={values.instructions} maxLength={2000} onChange={(event) => onChange({ ...values, instructions: event.target.value })} /></label>
    {values.movements.map((movement, index) => <fieldset className="movement-editor" key={index}>
      <legend>Movement {index + 1}</legend>
      <div className="movement-editor-grid">
        <label>Exercise<select aria-label={`Exercise for movement ${index + 1}`} value={movement.exerciseId} onChange={(event) => updateMovement(index, { exerciseId: event.target.value })}>
          <option value="">Select an exercise</option>
          {exercises.map((exercise) => <option key={exercise.id} value={exercise.id}>{exercise.name}</option>)}
          {movement.exerciseId && !exercises.some((exercise) => exercise.id === movement.exerciseId) && <option value={movement.exerciseId}>{movement.exerciseId}</option>}
        </select></label>
        <label>Movement name<input aria-label={`Name for movement ${index + 1}`} value={movement.movementName} onChange={(event) => updateMovement(index, { movementName: event.target.value })} /></label>
        {movement.preservedVariants
          ? <p>This movement keeps its saved targets.</p>
          : (movement.repetitionVariants ?? []).map((variant) => {
            const label = variant === "RX" ? "RX" : variant === "SCALED" ? "Scaled" : "Beginner";
            const value = variant === "RX" ? movement.rx : variant === "SCALED" ? movement.scaled : movement.beginner;
            return <label key={variant}>{label}<input aria-label={`${label} repetitions for movement ${index + 1}`} inputMode="numeric" value={value} onChange={(event) => updateMovement(index, variant === "RX" ? { rx: event.target.value } : variant === "SCALED" ? { scaled: event.target.value } : { beginner: event.target.value })} /></label>;
          })}
      </div>
      <div className="movement-editor-actions">
        <button type="button" className="subtle-button" disabled={disabled || index === 0} aria-label={`Move up movement ${index + 1}`} onClick={() => onChange({ ...values, movements: moveListItem(values.movements, index, "up") })}>Move up</button>
        <button type="button" className="subtle-button" disabled={disabled || index === values.movements.length - 1} aria-label={`Move down movement ${index + 1}`} onClick={() => onChange({ ...values, movements: moveListItem(values.movements, index, "down") })}>Move down</button>
        <button type="button" className="subtle-button" disabled={disabled || values.movements.length === 1} aria-label={`Remove movement ${index + 1}`} onClick={() => onChange({ ...values, movements: values.movements.filter((_, movementIndex) => movementIndex !== index) })}>Remove movement</button>
      </div>
    </fieldset>)}
    <div className="workout-block-actions">
      <button type="button" className="subtle-button" disabled={disabled} onClick={() => onChange({ ...values, movements: [...values.movements, emptyMovement(exercises[0]?.id ?? "")] })}>Add movement</button>
      <button type="submit" className="button button-dark" disabled={disabled}>{submitLabel}</button>
      {onCancel && <button type="button" className="subtle-button" disabled={disabled} onClick={onCancel}>Cancel</button>}
    </div>
  </form>;
}
