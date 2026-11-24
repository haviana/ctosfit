CREATE TABLE workout_drafts (
    id UUID PRIMARY KEY,
    box_id UUID NOT NULL REFERENCES boxes(id),
    title VARCHAR(160) NOT NULL,
    version INTEGER NOT NULL CHECK (version > 0),
    content_json JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT workout_drafts_title_not_blank CHECK (length(trim(title)) > 0),
    CONSTRAINT workout_drafts_id_box_unique UNIQUE (id, box_id)
);

CREATE INDEX workout_drafts_box_updated_idx ON workout_drafts (box_id, updated_at DESC);

CREATE TABLE workout_publications (
    id UUID PRIMARY KEY,
    box_id UUID NOT NULL REFERENCES boxes(id),
    workout_id UUID NOT NULL,
    version INTEGER NOT NULL CHECK (version > 0),
    title VARCHAR(160) NOT NULL,
    scheduled_for DATE NOT NULL,
    published_at TIMESTAMPTZ NOT NULL,
    published_by VARCHAR(255) NOT NULL,
    content_json JSONB NOT NULL,
    CONSTRAINT workout_publications_title_not_blank CHECK (length(trim(title)) > 0),
    CONSTRAINT workout_publications_workout_box_fk
        FOREIGN KEY (workout_id, box_id) REFERENCES workout_drafts(id, box_id),
    CONSTRAINT workout_publications_id_box_unique UNIQUE (id, box_id),
    CONSTRAINT workout_publications_workout_version_unique UNIQUE (box_id, workout_id, version)
);

CREATE INDEX workout_publications_schedule_idx
    ON workout_publications (box_id, scheduled_for, published_at DESC);

CREATE TABLE publication_metrics (
    box_id UUID NOT NULL,
    publication_id UUID NOT NULL,
    metric_id UUID NOT NULL,
    PRIMARY KEY (box_id, publication_id, metric_id),
    CONSTRAINT publication_metrics_publication_fk
        FOREIGN KEY (publication_id, box_id) REFERENCES workout_publications(id, box_id),
    CONSTRAINT publication_metrics_exercise_fk
        FOREIGN KEY (metric_id, box_id) REFERENCES exercises(id, box_id)
);

CREATE TABLE publication_prescriptions (
    box_id UUID NOT NULL,
    publication_id UUID NOT NULL,
    prescription_id VARCHAR(32) NOT NULL,
    PRIMARY KEY (box_id, publication_id, prescription_id),
    CONSTRAINT publication_prescriptions_publication_fk
        FOREIGN KEY (publication_id, box_id) REFERENCES workout_publications(id, box_id)
);

CREATE INDEX publication_metrics_metric_idx ON publication_metrics (box_id, metric_id);

CREATE FUNCTION reject_publication_mutation() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'published workout snapshots are immutable';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER workout_publications_immutable
    BEFORE UPDATE OR DELETE ON workout_publications
    FOR EACH ROW EXECUTE FUNCTION reject_publication_mutation();

CREATE TRIGGER publication_metrics_immutable
    BEFORE UPDATE OR DELETE ON publication_metrics
    FOR EACH ROW EXECUTE FUNCTION reject_publication_mutation();

CREATE TRIGGER publication_prescriptions_immutable
    BEFORE UPDATE OR DELETE ON publication_prescriptions
    FOR EACH ROW EXECUTE FUNCTION reject_publication_mutation();
