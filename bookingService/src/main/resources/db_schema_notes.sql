-- Database schema and notes for booking_db
-- Tables: diagnostic_centres, diagnostic_tests, centre_tests, bookings
-- PostgreSQL DDL (illustrative). Adjust types and constraints as needed.

-- diagnostic_centres
CREATE TABLE diagnostic_centres (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  location VARCHAR(512) NOT NULL
);

-- diagnostic_tests
CREATE TABLE diagnostic_tests (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  description TEXT NOT NULL
);

-- centre_tests: price specific to centre+test. Composite PK (centre_id, test_id)
CREATE TABLE centre_tests (
  centre_id BIGINT NOT NULL REFERENCES diagnostic_centres(id) ON DELETE CASCADE,
  test_id BIGINT NOT NULL REFERENCES diagnostic_tests(id) ON DELETE CASCADE,
  price NUMERIC(10,2) NOT NULL,
  PRIMARY KEY (centre_id, test_id)
);

-- bookings
CREATE TABLE bookings (
  id BIGSERIAL PRIMARY KEY,
  user_id VARCHAR(255) NOT NULL,
  centre_id BIGINT NOT NULL REFERENCES diagnostic_centres(id),
  test_id BIGINT NOT NULL REFERENCES diagnostic_tests(id),
  appointment_time TIMESTAMP WITH TIME ZONE NOT NULL,
  amount NUMERIC(10,2) NOT NULL,
  status VARCHAR(32) NOT NULL CHECK (status IN ('PENDING','CONFIRMED','FAILED','CANCELLED')),
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE
);

-- Indexes for common queries
CREATE INDEX idx_bookings_user_id ON bookings(user_id);
CREATE INDEX idx_bookings_appointment_time ON bookings(appointment_time);

-- Payment events (for webhook idempotency and audit)
CREATE TABLE payment_events (
  id BIGSERIAL PRIMARY KEY,
  provider_event_id VARCHAR(255) NOT NULL UNIQUE,
  booking_id BIGINT NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
  status VARCHAR(32) NOT NULL, -- e.g. SUCCESS/FAILED
  amount NUMERIC(10,2),
  received_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

-- Sample transactional flow for booking + payment (pseudocode):
-- BEGIN;
-- SELECT price FROM centre_tests WHERE centre_id = X AND test_id = Y FOR UPDATE;
-- -- verify not already reserved/other invariants
-- INSERT INTO bookings (user_id, centre_id, test_id, appointment_time, amount, status) VALUES (..., 'PENDING');
-- -- call payment (external/mock) and wait for response OR record outbound intent and process async
-- -- on payment SUCCESS: UPDATE bookings SET status='CONFIRMED', updated_at=now() WHERE id = <booking_id>;
-- -- on payment FAILED: UPDATE bookings SET status='FAILED', updated_at=now() WHERE id = <booking_id>;
-- COMMIT;

-- Webhook idempotency pattern (recommended):
-- 1) Receive webhook with provider_event_id, booking_id, status
-- 2) Attempt to insert into payment_events(provider_event_id, booking_id, status, amount). Use INSERT ... ON CONFLICT DO NOTHING or check existence.
-- 3) If insert succeeded (new event), then update the bookings row accordingly (set status). If insert did not create a new row (already received), ignore.
-- Example (Postgres):
-- INSERT INTO payment_events (provider_event_id, booking_id, status, amount)
-- VALUES ('evt_123', 42, 'SUCCESS', 799.00)
-- ON CONFLICT (provider_event_id) DO NOTHING;
-- -- then check if insert created a row (by checking RETURNING id); if yes, update bookings.

-- Concurrency and consistency notes:
-- - Use SELECT ... FOR UPDATE on centre_tests to prevent concurrent reservations when capacity is limited.
-- - If capacity per centre_test >1, store an "available_slots" column and decrement atomically in a transaction.
-- - Consider optimistic locking (version column) on bookings or centre_tests for simpler scale-out.

-- Authorization and ownership notes:
-- - Do not accept user_id from client without validation. Derive authenticated user's id from JWT and enforce it on booking create/list/get.
-- - Admin roles can be added to allow listing all bookings.

-- Migration guidance:
-- - Use Flyway or Liquibase to manage DDL. Seed reference data (diagnostic_centres, diagnostic_tests, centre_tests) via migration scripts, not data.sql in production.

-- Sample queries:
-- List available tests for a centre:
-- SELECT t.id, t.name, t.description, ct.price
-- FROM diagnostic_tests t
-- JOIN centre_tests ct ON ct.test_id = t.id
-- WHERE ct.centre_id = :centre_id;

-- Get user's bookings:
-- SELECT * FROM bookings WHERE user_id = :user_id ORDER BY appointment_time DESC;

-- End of schema notes
