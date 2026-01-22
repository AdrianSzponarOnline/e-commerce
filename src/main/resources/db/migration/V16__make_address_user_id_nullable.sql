-- Make user_id nullable in addresses table to support guest orders
ALTER TABLE public.addresses ALTER COLUMN user_id DROP NOT NULL;

-- Add guest contact information fields to orders table
ALTER TABLE public.orders 
    ADD COLUMN guest_email VARCHAR(255),
    ADD COLUMN guest_first_name VARCHAR(100),
    ADD COLUMN guest_last_name VARCHAR(100),
    ADD COLUMN guest_phone VARCHAR(20);
