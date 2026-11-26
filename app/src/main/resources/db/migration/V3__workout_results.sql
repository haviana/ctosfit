CREATE TABLE workout_results (
    id UUID PRIMARY KEY,
    box_id UUID NOT NULL REFERENCES boxes(id),
    athlete_id VARCHAR(255) NOT NULL,
    publication_id UUID NOT NULL,
    metric_id UUID NOT NULL,
    prescription_id VARCHAR(255) NOT NULL,
    submitted_at TIMESTAMPTZ NOT NULL,
    score_type VARCHAR(32) NOT NULL,
    load_amount NUMERIC,
    load_unit VARCHAR(16),
    elapsed_seconds BIGINT,
    elapsed_nanos INTEGER,
    time_outcome VARCHAR(24),
    integer_value INTEGER,
    rounds INTEGER,
    additional_reps INTEGER,
    distance_amount NUMERIC,
    distance_unit VARCHAR(16),
    completion_status VARCHAR(24),
    CONSTRAINT workout_results_score_type_check CHECK (
        score_type IN ('LOAD', 'TIME', 'REPS', 'ROUNDS_AND_REPS', 'DISTANCE', 'CALORIES', 'COMPLETION')
    ),
    CONSTRAINT workout_results_score_shape_check CHECK (
        (score_type = 'LOAD'
            AND load_amount IS NOT NULL AND load_amount >= 0
            AND load_unit IS NOT NULL AND load_unit IN ('KILOGRAM', 'POUND')
            AND elapsed_seconds IS NULL AND elapsed_nanos IS NULL AND time_outcome IS NULL
            AND integer_value IS NULL AND rounds IS NULL AND additional_reps IS NULL
            AND distance_amount IS NULL AND distance_unit IS NULL AND completion_status IS NULL)
        OR
        (score_type = 'TIME'
            AND load_amount IS NULL AND load_unit IS NULL
            AND elapsed_seconds IS NOT NULL AND elapsed_seconds >= 0
            AND elapsed_nanos IS NOT NULL AND elapsed_nanos BETWEEN 0 AND 999999999
            AND time_outcome IS NOT NULL AND time_outcome IN ('COMPLETED', 'TIME_CAP', 'DID_NOT_FINISH')
            AND integer_value IS NULL AND rounds IS NULL AND additional_reps IS NULL
            AND distance_amount IS NULL AND distance_unit IS NULL AND completion_status IS NULL)
        OR
        (score_type = 'REPS'
            AND load_amount IS NULL AND load_unit IS NULL
            AND elapsed_seconds IS NULL AND elapsed_nanos IS NULL AND time_outcome IS NULL
            AND integer_value IS NOT NULL AND integer_value >= 0
            AND rounds IS NULL AND additional_reps IS NULL
            AND distance_amount IS NULL AND distance_unit IS NULL AND completion_status IS NULL)
        OR
        (score_type = 'ROUNDS_AND_REPS'
            AND load_amount IS NULL AND load_unit IS NULL
            AND elapsed_seconds IS NULL AND elapsed_nanos IS NULL AND time_outcome IS NULL
            AND integer_value IS NULL
            AND rounds IS NOT NULL AND rounds >= 0 AND additional_reps IS NOT NULL AND additional_reps >= 0
            AND distance_amount IS NULL AND distance_unit IS NULL AND completion_status IS NULL)
        OR
        (score_type = 'DISTANCE'
            AND load_amount IS NULL AND load_unit IS NULL
            AND elapsed_seconds IS NULL AND elapsed_nanos IS NULL AND time_outcome IS NULL
            AND integer_value IS NULL AND rounds IS NULL AND additional_reps IS NULL
            AND distance_amount IS NOT NULL AND distance_amount >= 0
            AND distance_unit IS NOT NULL AND distance_unit IN ('METER', 'KILOMETER', 'MILE', 'FOOT')
            AND completion_status IS NULL)
        OR
        (score_type = 'CALORIES'
            AND load_amount IS NULL AND load_unit IS NULL
            AND elapsed_seconds IS NULL AND elapsed_nanos IS NULL AND time_outcome IS NULL
            AND integer_value IS NOT NULL AND integer_value >= 0
            AND rounds IS NULL AND additional_reps IS NULL
            AND distance_amount IS NULL AND distance_unit IS NULL AND completion_status IS NULL)
        OR
        (score_type = 'COMPLETION'
            AND load_amount IS NULL AND load_unit IS NULL
            AND elapsed_seconds IS NULL AND elapsed_nanos IS NULL AND time_outcome IS NULL
            AND integer_value IS NULL AND rounds IS NULL AND additional_reps IS NULL
            AND distance_amount IS NULL AND distance_unit IS NULL
            AND completion_status IS NOT NULL AND completion_status IN ('COMPLETED', 'NOT_COMPLETED'))
    ),
    CONSTRAINT workout_results_membership_fk
        FOREIGN KEY (athlete_id, box_id) REFERENCES box_memberships(user_id, box_id),
    CONSTRAINT workout_results_publication_fk
        FOREIGN KEY (publication_id, box_id) REFERENCES workout_publications(id, box_id),
    CONSTRAINT workout_results_metric_fk
        FOREIGN KEY (box_id, publication_id, metric_id)
        REFERENCES publication_metrics(box_id, publication_id, metric_id),
    CONSTRAINT workout_results_prescription_fk
        FOREIGN KEY (box_id, publication_id, prescription_id)
        REFERENCES publication_prescriptions(box_id, publication_id, prescription_id),
    CONSTRAINT workout_results_box_id_unique UNIQUE (box_id, id)
);

CREATE INDEX workout_results_athlete_history_idx
    ON workout_results (box_id, athlete_id, submitted_at DESC, id DESC);

CREATE INDEX workout_results_publication_metric_idx
    ON workout_results (box_id, publication_id, metric_id, submitted_at DESC);
