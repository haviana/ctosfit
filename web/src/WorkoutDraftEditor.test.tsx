// @vitest-environment jsdom
import { cleanup, render, screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, describe, expect, it } from "vitest";
import WorkoutDraftEditor from "./WorkoutDraftEditor";
import type { WorkoutDraftView } from "./workout-editor";

const exercises = [
  { id: "ex-1", name: "Thruster" },
  { id: "ex-2", name: "Pull-up" },
];

function movement(exerciseId: string, name: string) {
  return {
    exerciseId,
    movementName: name,
    variants: {
      RX: [{ kind: "REPETITIONS", value: "21", unit: null }],
      SCALED: [{ kind: "REPETITIONS", value: "15", unit: null }],
      BEGINNER: [{ kind: "REPETITIONS", value: "9", unit: null }],
    },
  };
}

const draft: WorkoutDraftView = {
  id: "draft-1",
  title: "Friday",
  version: 2,
  blocks: [
    { id: "block-1", title: "Warm-up", intent: "WARM_UP", format: { kind: "FOR_TIME", timeCapSeconds: 300 }, movements: [movement("ex-1", "Thruster")] },
    { id: "block-2", title: "Strength", intent: "STRENGTH", format: { kind: "AMRAP", durationSeconds: 480 }, movements: [movement("ex-2", "Pull-up")] },
  ],
};

afterEach(() => cleanup());

function renderEditor(onRequest: (path: string, init?: RequestInit) => WorkoutDraftView = () => draft, current: WorkoutDraftView = draft) {
  const calls: Array<{ path: string; init?: RequestInit }> = [];
  const saved: WorkoutDraftView[] = [];
  const view = render(
    <WorkoutDraftEditor
      draft={current}
      exercises={exercises}
      request={async <T,>(path: string, init?: RequestInit) => {
        calls.push({ path, init });
        return onRequest(path, init) as T;
      }}
      onDraft={(next) => saved.push(next)}
      onError={() => undefined}
    />,
  );
  return { ...view, calls, saved };
}

describe("WorkoutDraftEditor", () => {
  it("shows each block and movement without raw workout text", () => {
    renderEditor();
    expect(screen.getByRole("region", { name: "Blocks in Friday" })).toBeTruthy();
    expect(screen.getByText("Thruster · RX 21 · Scaled 15 · Beginner 9")).toBeTruthy();
    expect(screen.getByText("Pull-up · RX 21 · Scaled 15 · Beginner 9")).toBeTruthy();
    expect(screen.getByRole("button", { name: "Move up Warm-up" })).toHaveProperty("disabled", true);
    expect(screen.getByRole("button", { name: "Move down Strength" })).toHaveProperty("disabled", true);
    expect(screen.queryByRole("textbox", { name: /json/i })).toBeNull();
  });

  it("adds a second-style block through the coach API", async () => {
    const user = userEvent.setup();
    const { calls } = renderEditor();
    const form = screen.getByRole("form", { name: "Add block" });
    await user.type(within(form).getByLabelText("Block title"), "Accessory");
    await user.selectOptions(within(form).getByLabelText("Exercise for movement 1"), "ex-2");
    await user.clear(within(form).getByLabelText("Name for movement 1"));
    await user.type(within(form).getByLabelText("Name for movement 1"), "Pull-up");
    await user.selectOptions(within(form).getByLabelText("Format"), "AMRAP");
    await user.type(within(form).getByLabelText("Duration (seconds)"), "600");
    await user.click(within(form).getByRole("button", { name: "Add block" }));

    expect(calls).toHaveLength(1);
    expect(calls[0]?.path).toBe("/api/coaches/workouts/draft-1/blocks");
    expect(calls[0]?.init?.method).toBe("POST");
    const body = JSON.parse(String(calls[0]?.init?.body));
    expect(body.block.title).toBe("Accessory");
    expect(body.block.format).toEqual({ kind: "AMRAP", durationSeconds: 600 });
    expect(body.block.movements).toHaveLength(1);
    expect(body.block.movements[0].movementName).toBe("Pull-up");
  });

  it("edits, reorders, and removes a block", async () => {
    const user = userEvent.setup();
    const { calls, saved } = renderEditor();

    await user.click(screen.getByRole("button", { name: "Edit Warm-up" }));
    const edit = screen.getByRole("form", { name: "Edit Warm-up" });
    const title = within(edit).getByLabelText("Block title");
    await user.clear(title);
    await user.type(title, "Skill");
    await user.click(within(edit).getByRole("button", { name: "Save block" }));

    await user.click(screen.getByRole("button", { name: "Move down Warm-up" }));
    await user.click(screen.getByRole("button", { name: "Remove Strength" }));

    expect(calls.map((call) => [call.path, call.init?.method])).toEqual([
      ["/api/coaches/workouts/draft-1/blocks/block-1", "PUT"],
      ["/api/coaches/workouts/draft-1/blocks/block-1/position", "PATCH"],
      ["/api/coaches/workouts/draft-1/blocks/block-2", "DELETE"],
    ]);
    const savedBlock = JSON.parse(String(calls[0]?.init?.body));
    expect(savedBlock.title).toBe("Skill");
    expect(savedBlock.format).toEqual({ kind: "FOR_TIME", timeCapSeconds: 300 });
    expect(savedBlock.movements[0].variants).toEqual({
      RX: [{ kind: "REPETITIONS", value: "21" }],
      SCALED: [{ kind: "REPETITIONS", value: "15" }],
      BEGINNER: [{ kind: "REPETITIONS", value: "9" }],
    });
    expect(JSON.parse(String(calls[1]?.init?.body))).toEqual({ position: 1 });
    expect(saved).toHaveLength(3);
  });

  it("keeps an RX-only movement from gaining scaled variants on save", async () => {
    const user = userEvent.setup();
    const rxOnly: WorkoutDraftView = {
      ...draft,
      blocks: [{
        id: "block-1",
        title: "Strength",
        intent: "STRENGTH",
        format: { kind: "FOR_TIME" },
        movements: [{
          exerciseId: "ex-1",
          movementName: "Thruster",
          variants: { RX: [{ kind: "REPETITIONS", value: "5", unit: null }] },
        }],
      }],
    };
    const { calls } = renderEditor(() => rxOnly, rxOnly);
    await user.click(screen.getByRole("button", { name: "Edit Strength" }));
    const edit = screen.getByRole("form", { name: "Edit Strength" });
    expect(within(edit).queryByLabelText("Scaled repetitions for movement 1")).toBeNull();
    expect(within(edit).getByLabelText("RX repetitions for movement 1")).toBeTruthy();
    await user.click(screen.getByRole("button", { name: "Save block" }));
    expect(JSON.parse(String(calls[0]?.init?.body)).movements[0].variants).toEqual({
      RX: [{ kind: "REPETITIONS", value: "5" }],
    });
  });

  it("adds, reorders, and removes movements before saving the block", async () => {
    const user = userEvent.setup();
    const { calls } = renderEditor();
    const form = screen.getByRole("form", { name: "Add block" });
    await user.type(within(form).getByLabelText("Block title"), "Conditioning");
    await user.type(within(form).getByLabelText("Name for movement 1"), "Thruster");
    await user.click(within(form).getByRole("button", { name: "Add movement" }));
    await user.selectOptions(within(form).getByLabelText("Exercise for movement 2"), "ex-2");
    await user.type(within(form).getByLabelText("Name for movement 2"), "Pull-up");
    await user.click(within(form).getByRole("button", { name: "Move up movement 2" }));
    await user.click(within(form).getByRole("button", { name: "Remove movement 2" }));
    await user.click(within(form).getByRole("button", { name: "Add block" }));

    const body = JSON.parse(String(calls[0]?.init?.body));
    expect(body.block.movements.map((item: { movementName: string }) => item.movementName)).toEqual(["Pull-up"]);
  });

  it("keeps an invalid block off the API", async () => {
    const user = userEvent.setup();
    const { calls } = renderEditor();
    const form = screen.getByRole("form", { name: "Add block" });
    await user.click(within(form).getByRole("button", { name: "Add block" }));
    expect(screen.getByRole("alert").textContent).toMatch(/block title/i);
    expect(calls).toHaveLength(0);
  });
});
