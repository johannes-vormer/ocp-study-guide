CREATE TABLE IF NOT EXISTS session (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    mode          TEXT    NOT NULL,          -- practice | exam
    title         TEXT    NOT NULL,
    question_ids  TEXT    NOT NULL,          -- comma separated, in order
    started_at    TEXT    NOT NULL,
    finished_at   TEXT,
    time_limit    INTEGER                    -- minutes, exam mode only
);

CREATE TABLE IF NOT EXISTS attempt (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    session_id   INTEGER NOT NULL REFERENCES session(id),
    question_id  TEXT    NOT NULL,
    selected     TEXT    NOT NULL,           -- e.g. "A,C"
    correct      INTEGER NOT NULL,
    answered_at  TEXT    NOT NULL,
    UNIQUE (session_id, question_id)
);
CREATE INDEX IF NOT EXISTS idx_attempt_question ON attempt(question_id, answered_at);

CREATE TABLE IF NOT EXISTS topic_progress (
    topic_id    TEXT PRIMARY KEY,
    status      TEXT NOT NULL DEFAULT 'not-started',   -- not-started | reading | done
    confidence  INTEGER NOT NULL DEFAULT 0,           -- 0..5
    notes       TEXT NOT NULL DEFAULT '',
    updated_at  TEXT
);

CREATE TABLE IF NOT EXISTS plan_day (
    day_no   INTEGER PRIMARY KEY,
    done_at  TEXT
);

CREATE TABLE IF NOT EXISTS setting (
    key    TEXT PRIMARY KEY,
    value  TEXT NOT NULL
);
