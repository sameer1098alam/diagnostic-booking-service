Payment table schema mapping and implementation notes

Schema (payments table)
- id: bigint, primary key (generated)
- booking_id: text/varchar, NOT NULL
- provider_payment_id: text/varchar, UNIQUE (nullable)
- event_id: text/varchar, UNIQUE (nullable)
- amount: numeric(12,2), NOT NULL
- status: enum/string, NOT NULL
- created_at: timestamptz, auto-populated
- updated_at: timestamptz, auto-populated

Implementation details
- Entity: com.eve.payment.entity.Payment maps to the above columns. Uses Hibernate @CreationTimestamp/@UpdateTimestamp for timestamps.
- Unique constraints: provider_payment_id and event_id have UNIQUE constraints. PostgreSQL allows multiple NULLs for UNIQUE columns; if you require uniqueness even for NULLs, create a partial unique index (e.g. CREATE UNIQUE INDEX ON payments(provider_payment_id) WHERE provider_payment_id IS NOT NULL;).
- Amount: mapped with precision=12, scale=2 to represent currency safely. Consider using a Money type or separate cents-integer if preferred.
- Timestamps: columnDefinition="timestamp with time zone" to store timezone-aware instants.

Behavioral notes
- Webhook idempotency: handleWebhook checks event_id and skips processing if event already exists (simple idempotency). For richer audit/history consider a separate webhook_events table.
- Provider matching: webhook may match payment by provider_payment_id or by internal payment id.
- Booking integration: BookingClient.notifyBooking(bookingId, status) is called after status update; currently BookingClient is a noop placeholder. Replace with an HTTP client to Booking Service and add retries/queue for reliability.

Next steps
- Add PostgreSQL config and Flyway/Liquibase migration to create exact production schema.
- Implement BookingClient HTTP calls and robust retry logic.
- Optionally add a webhook_events table for complete auditability and replayability.
