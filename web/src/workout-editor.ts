export const BLOCK_INTENTS = ["WARM_UP", "STRENGTH", "CONDITIONING", "SKILL", "ACCESSORY"] as const;

export type MovementTargetView = { kind: string; value: string; unit?: string | null };

export type WorkoutMovementView = {
  exerciseId: string;
  movementName: string;
  variants: Record<string, MovementTargetView[]>;
};

export type WorkoutBlockView = {
  id: string;
  title: string;
  intent: string;
  format: Record<string, unknown> | null;
  movements: WorkoutMovementView[];
  instructions?: string | null;
};

export type WorkoutDraftView = {
  id: string;
  title: string;
  version: number;
  blocks: WorkoutBlockView[];
};

export type MovementForm = {
  exerciseId: string;
  movementName: string;
  rx: string;
  scaled: string;
  beginner: string;
  repetitionVariants: RepetitionVariant[] | null;
  preservedVariants: Record<string, MovementTargetView[]> | null;
};

export type BlockFormValues = {
  title: string;
  intent: string;
  formatKind: "FOR_TIME" | "AMRAP" | "UNCHANGED";
  duration: string;
  instructions: string;
  movements: MovementForm[];
  preservedFormat: Record<string, unknown> | null;
};

const SIMPLE_VARIANTS = ["RX", "SCALED", "BEGINNER"] as const;
export type RepetitionVariant = typeof SIMPLE_VARIANTS[number];

export function emptyMovement(exerciseId = ""): MovementForm {
  return {
    exerciseId,
    movementName: "",
    rx: "10",
    scaled: "8",
    beginner: "5",
    repetitionVariants: ["RX", "SCALED", "BEGINNER"],
    preservedVariants: null,
  };
}

export function emptyBlockForm(exerciseId = ""): BlockFormValues {
  return {
    title: "",
    intent: "CONDITIONING",
    formatKind: "FOR_TIME",
    duration: "",
    instructions: "",
    movements: [emptyMovement(exerciseId)],
    preservedFormat: null,
  };
}

function repetitionValue(targets: MovementTargetView[] | undefined): string {
  return targets?.find((target) => target.kind === "REPETITIONS")?.value ?? "";
}

function isSimpleMovement(movement: WorkoutMovementView): boolean {
  const names = Object.keys(movement.variants);
  if (names.length === 0 || names.some((name) => !SIMPLE_VARIANTS.includes(name as typeof SIMPLE_VARIANTS[number]))) return false;
  return names.every((name) => {
    const targets = movement.variants[name] ?? [];
    return targets.length === 1 && targets[0]?.kind === "REPETITIONS" && !targets[0].unit;
  });
}

export function blockFormFrom(block: WorkoutBlockView): BlockFormValues {
  const kind = String(block.format?.kind ?? "");
  const known = kind === "FOR_TIME" || kind === "AMRAP";
  return {
    title: block.title,
    intent: block.intent,
    formatKind: known ? kind : "UNCHANGED",
    duration: known
      ? String(kind === "AMRAP" ? block.format?.durationSeconds ?? "" : block.format?.timeCapSeconds ?? "")
      : "",
    instructions: block.instructions ?? "",
    movements: block.movements.map((movement) => {
      const simple = isSimpleMovement(movement);
      return {
        exerciseId: movement.exerciseId,
        movementName: movement.movementName,
        rx: repetitionValue(movement.variants.RX),
        scaled: repetitionValue(movement.variants.SCALED),
        beginner: repetitionValue(movement.variants.BEGINNER),
        repetitionVariants: simple ? SIMPLE_VARIANTS.filter((variant) => (movement.variants[variant]?.length ?? 0) > 0) : null,
        preservedVariants: simple ? null : movement.variants,
      };
    }),
    preservedFormat: known || !block.format ? null : block.format,
  };
}

export function describeIntent(intent: string): string {
  return intent.replaceAll("_", " ").toLowerCase();
}

export function describeBlockFormat(format: Record<string, unknown> | null | undefined): string {
  const kind = String(format?.kind ?? "");
  if (kind === "FOR_TIME") {
    const cap = format?.timeCapSeconds;
    return typeof cap === "number" ? `For time, ${cap}s cap` : "For time";
  }
  if (kind === "AMRAP") return `AMRAP, ${format?.durationSeconds ?? "?"}s`;
  if (!kind) return "No format";
  return kind.replaceAll("_", " ").toLowerCase();
}

export function describeMovement(movement: WorkoutMovementView): string {
  const parts = SIMPLE_VARIANTS.flatMap((variant) => {
    const targets = movement.variants[variant];
    if (!targets?.length) return [];
    const text = targets.map((target) => target.unit ? `${target.value} ${target.unit}` : target.value).join(" + ");
    const label = variant === "RX" ? "RX" : variant === "SCALED" ? "Scaled" : "Beginner";
    return [`${label} ${text}`];
  });
  return `${movement.movementName} · ${parts.join(" · ")}`;
}

function wholeNumber(value: string): number | null {
  if (!/^\d+$/.test(value.trim())) return null;
  const parsed = Number(value);
  return parsed > 0 ? parsed : null;
}

export function validateBlockForm(values: BlockFormValues): string | null {
  if (!values.title.trim()) return "Enter a block title.";
  if (!values.intent.trim()) return "Choose a block intent.";
  if (values.formatKind === "AMRAP" && wholeNumber(values.duration) == null) {
    return "Enter the AMRAP duration in whole seconds.";
  }
  if (values.formatKind === "FOR_TIME" && values.duration.trim() && wholeNumber(values.duration) == null) {
    return "A time cap must be a whole number of seconds greater than zero.";
  }
  if (values.formatKind === "UNCHANGED" && !values.preservedFormat) return "Choose a workout format.";
  if (values.movements.length === 0) return "Add at least one movement.";
  for (const [index, movement] of values.movements.entries()) {
    const position = index + 1;
    if (!movement.exerciseId) return `Choose an exercise for movement ${position}.`;
    if (!movement.movementName.trim()) return `Enter a name for movement ${position}.`;
    if (movement.preservedVariants) continue;
    for (const variant of movement.repetitionVariants ?? []) {
      const raw = variant === "RX" ? movement.rx : variant === "SCALED" ? movement.scaled : movement.beginner;
      const label = variant === "RX" ? "RX" : variant === "SCALED" ? "Scaled" : "Beginner";
      if (wholeNumber(raw) == null) return `${label} repetitions for movement ${position} must be a whole number greater than zero.`;
    }
  }
  return null;
}

export function toBlockRequest(values: BlockFormValues) {
  const error = validateBlockForm(values);
  if (error) throw new Error(error);
  const format = values.formatKind === "UNCHANGED"
    ? values.preservedFormat
    : values.formatKind === "AMRAP"
      ? { kind: "AMRAP", durationSeconds: wholeNumber(values.duration) }
      : { kind: "FOR_TIME", ...(values.duration.trim() ? { timeCapSeconds: wholeNumber(values.duration) } : {}) };
  return {
    title: values.title.trim(),
    intent: values.intent,
    format,
    ...(values.instructions.trim() ? { instructions: values.instructions.trim() } : {}),
    movements: values.movements.map((movement) => ({
      exerciseId: movement.exerciseId,
      movementName: movement.movementName.trim(),
      variants: movement.preservedVariants ?? Object.fromEntries((movement.repetitionVariants ?? []).map((variant) => {
        const raw = variant === "RX" ? movement.rx : variant === "SCALED" ? movement.scaled : movement.beginner;
        return [variant, [{ kind: "REPETITIONS", value: String(wholeNumber(raw)) }]];
      })),
    })),
  };
}

export function nextPosition(currentIndex: number, direction: "up" | "down"): number {
  return direction === "up" ? currentIndex - 1 : currentIndex + 1;
}

export function moveListItem<T>(items: readonly T[], index: number, direction: "up" | "down"): T[] {
  const target = nextPosition(index, direction);
  if (target < 0 || target >= items.length || target === index) return [...items];
  const next = [...items];
  const [item] = next.splice(index, 1);
  if (item === undefined) return [...items];
  next.splice(target, 0, item);
  return next;
}
