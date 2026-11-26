CREATE TABLE boxes (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    time_zone VARCHAR(100) NOT NULL DEFAULT 'UTC',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT boxes_name_not_blank CHECK (length(trim(name)) > 0)
);

CREATE TABLE box_memberships (
    id UUID PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    box_id UUID NOT NULL REFERENCES boxes(id),
    role VARCHAR(32) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT box_memberships_role_check CHECK (role IN ('BOX_OWNER', 'COACH', 'ATHLETE')),
    CONSTRAINT box_memberships_status_check CHECK (status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT box_memberships_user_box_unique UNIQUE (user_id, box_id),
    CONSTRAINT box_memberships_id_box_unique UNIQUE (id, box_id)
);

CREATE INDEX box_memberships_box_role_idx ON box_memberships (box_id, role, status);

CREATE TABLE exercises (
    id UUID PRIMARY KEY,
    box_id UUID NOT NULL REFERENCES boxes(id),
    name VARCHAR(120) NOT NULL,
    category VARCHAR(80) NOT NULL,
    instructions TEXT,
    media_reference TEXT,
    archived_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT exercises_name_not_blank CHECK (length(trim(name)) > 0),
    CONSTRAINT exercises_category_not_blank CHECK (length(trim(category)) > 0),
    CONSTRAINT exercises_id_box_unique UNIQUE (id, box_id)
);

CREATE INDEX exercises_box_name_idx ON exercises (box_id, lower(name));
CREATE INDEX exercises_box_active_idx ON exercises (box_id, archived_at);

CREATE TABLE exercise_equipment (
    exercise_id UUID NOT NULL,
    box_id UUID NOT NULL,
    equipment VARCHAR(120) NOT NULL,
    PRIMARY KEY (exercise_id, box_id, equipment),
    CONSTRAINT exercise_equipment_not_blank CHECK (length(trim(equipment)) > 0),
    CONSTRAINT exercise_equipment_exercise_box_fk
        FOREIGN KEY (exercise_id, box_id) REFERENCES exercises(id, box_id)
);
