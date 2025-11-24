-- Add mock users for testing
-- User 2: Normal user with ROLE_USER
-- User 3: Owner with ROLE_OWNER
-- Password for both: "Password" (bcrypt hash: $2a$12$67MMMGKN7MS/EY4izuIIi.Yc.beQKQC9nm4Yf7B8O6crBm7.1Gema)

INSERT INTO public.users (first_name, last_name, email, password, created_at, updated_at, deleted_at, is_active)
VALUES ('Test', 'User', 'testuser@example.com', '$2a$12$67MMMGKN7MS/EY4izuIIi.Yc.beQKQC9nm4Yf7B8O6crBm7.1Gema', NOW(), NOW(), NULL, TRUE)
ON CONFLICT (email) DO NOTHING;


INSERT INTO public.users (first_name, last_name, email, password, created_at, updated_at, deleted_at, is_active)
VALUES ('Test', 'Owner', 'owner@example.com', '$2a$12$67MMMGKN7MS/EY4izuIIi.Yc.beQKQC9nm4Yf7B8O6crBm7.1Gema', NOW(), NOW(), NULL, TRUE)
ON CONFLICT (email) DO NOTHING;


INSERT INTO public.user_roles (user_id, role_id, created_at, deleted_at)
VALUES (
           (SELECT id FROM public.users WHERE email = 'testuser@example.com'),
           (SELECT id FROM public.roles WHERE name = 'ROLE_USER'),
           NOW(),
           NULL
       )
ON CONFLICT ON CONSTRAINT user_roles_pkey DO NOTHING;


INSERT INTO public.user_roles (user_id, role_id, created_at, deleted_at)
VALUES (
           (SELECT id FROM public.users WHERE email = 'owner@example.com'),
           (SELECT id FROM public.roles WHERE name = 'ROLE_OWNER'),
           NOW(),
           NULL
       )