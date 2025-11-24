-- Add missing columns to payments table
ALTER TABLE public.payments
    ADD COLUMN IF NOT EXISTS transaction_id character varying(255),
    ADD COLUMN IF NOT EXISTS notes character varying(500);

