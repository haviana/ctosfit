import { describe, expect, it } from "vitest";
import { blockFormFrom, emptyBlockForm, moveListItem, nextPosition, toBlockRequest, validateBlockForm, type WorkoutBlockView } from "./workout-editor";

describe("workout editor payloads", () => {
  it("builds a for-time block with three repetition prescriptions", () => {
    const values = emptyBlockForm("ex-1");
    values.title = "Conditioning";
    values.duration = "600";
    values.movements[0] = { ...values.movements[0], movementName: "Thruster", rx: "21", scaled: "15", beginner: "9" };

    expect(toBlockRequest(values)).toEqual({
      title: "Conditioning",
      intent: "CONDITIONING",
      format: { kind: "FOR_TIME", timeCapSeconds: 600 },
      movements: [{
        exerciseId: "ex-1",
        movementName: "Thruster",
        variants: { RX: [{ kind: "REPETITIONS", value: "21" }], SCALED: [{ kind: "REPETITIONS", value: "15" }], BEGINNER: [{ kind: "REPETITIONS", value: "9" }] },
      }],
    });
  });

  it("requires an AMRAP duration and keeps a non-repetition movement unchanged", () => {
    const block: WorkoutBlockView = {
      id: "block-1",
      title: "Strength",
      intent: "STRENGTH",
      format: { kind: "EMOM", intervalSeconds: 60, intervalCount: 10 },
      instructions: "Unbroken",
      movements: [{
        exerciseId: "ex-2",
        movementName: "Back squat",
        variants: { RX: [{ kind: "LOAD", value: "100", unit: "KILOGRAM" }] },
      }],
    };
    const values = blockFormFrom(block);
    values.duration = "";
    expect(values.formatKind).toBe("UNCHANGED");
    expect(validateBlockForm({ ...values, formatKind: "AMRAP", duration: "" })).toMatch(/AMRAP duration/);
    expect(toBlockRequest(values).movements[0]?.variants).toEqual(block.movements[0]?.variants);
    expect(toBlockRequest(values).format).toEqual(block.format);
    expect(toBlockRequest(values).instructions).toBe("Unbroken");
  });

  it("does not invent scaled or beginner repetitions for an RX-only movement", () => {
    const values = blockFormFrom({
      id: "block-1",
      title: "Strength",
      intent: "STRENGTH",
      format: { kind: "FOR_TIME" },
      movements: [{
        exerciseId: "ex-1",
        movementName: "Thruster",
        variants: { RX: [{ kind: "REPETITIONS", value: "5", unit: null }] },
      }],
    });
    values.title = "Strength work";
    expect(toBlockRequest(values).movements[0]?.variants).toEqual({
      RX: [{ kind: "REPETITIONS", value: "5" }],
    });
  });

  it("moves a block or movement one place up or down", () => {
    expect(nextPosition(2, "up")).toBe(1);
    expect(nextPosition(0, "down")).toBe(1);
    expect(moveListItem(["thruster", "pull-up", "row"], 2, "up")).toEqual(["thruster", "row", "pull-up"]);
    expect(moveListItem(["thruster"], 0, "down")).toEqual(["thruster"]);
  });
});
