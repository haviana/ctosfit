CREATE TABLE membership_invitations (
    id UUID PRIMARY KEY,
    box_id UUID NOT NULL REFERENCES boxes(id),
    email VARCHAR(320) NOT NULL,
    role VARCHAR(32) NOT NULL,
    token_hash CHAR(64) NOT NULL UNIQUE,
    invited_by VARCHAR(255) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    accepted_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT membership_invitations_role_check CHECK (role IN ('COACH', 'ATHLETE')),
    CONSTRAINT membership_invitations_email_not_blank CHECK (length(trim(email)) > 3)
);

CREATE INDEX membership_invitations_pending_idx ON membership_invitations (box_id, email) WHERE accepted_at IS NULL;
