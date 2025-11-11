-- Add mock users for testing
-- User 2: Normal user with ROLE_USER
-- User 3: Owner with ROLE_OWNER
-- Password for both: "password" (bcrypt hash: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy)

-- Add normal user (ROLE_USER)
INSERT INTO public.users (id, first_name, last_name, email, password, created_at, updated_at, deleted_at, is_active) VALUES
    (2, 'Test', 'User', 'testuser@example.com', '$2a$20$44.Akuxc7iEFxqB19k7JIOpeLDMzDLuC8fuUPwBkUjdLx7baz6SRW', NOW(), NOW(), NULL, TRUE)
ON CONFLICT ON CONSTRAINT users_email_key DO NOTHING;

-- Add owner user (ROLE_OWNER)
INSERT INTO public.users (id, first_name, last_name, email, password, created_at, updated_at, deleted_at, is_active) VALUES
    (3, 'Test', 'Owner', 'owner@example.com', '$2a$20$44.Akuxc7iEFxqB19k7JIOpeLDMzDLuC8fuUPwBkUjdLx7baz6SRW', NOW(), NOW(), NULL, TRUE)
ON CONFLICT ON CONSTRAINT users_email_key DO NOTHING;

-- Assign ROLE_USER to test user
INSERT INTO public.user_roles (user_id, role_id, created_at, deleted_at) VALUES
    (2, 1, NOW(), NULL)
ON CONFLICT ON CONSTRAINT user_roles_pkey DO NOTHING;

-- Assign ROLE_OWNER to owner user
INSERT INTO public.user_roles (user_id, role_id, created_at, deleted_at) VALUES
    (3, 2, NOW(), NULL)
ON CONFLICT ON CONSTRAINT user_roles_pkey DO NOTHING;

-- Update users sequence to continue from 3
SELECT pg_catalog.setval('public.users_id_seq', 3, true);
