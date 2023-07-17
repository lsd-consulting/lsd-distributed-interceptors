CREATE SCHEMA IF NOT EXISTS lsd;
CREATE TABLE IF NOT EXISTS lsd.intercepted_interactions
(
    id               integer PRIMARY KEY generated always as identity,
    trace_id         VARCHAR(30),
    body             TEXT,
    request_headers  TEXT,
    response_headers TEXT,
    service_name     VARCHAR(200),
    target           VARCHAR(200),
    path             VARCHAR(200),
    http_status      VARCHAR(35),
    http_method      VARCHAR(7),
    interaction_type VARCHAR(8),
    profile          VARCHAR(20),
    elapsed_time     NUMERIC(20),
    created_at       timestamp with time zone
);
CREATE INDEX IF NOT EXISTS admin_query1_idx ON lsd.intercepted_interactions (created_at);
CREATE INDEX IF NOT EXISTS admin_query2_idx ON lsd.intercepted_interactions (trace_id);
