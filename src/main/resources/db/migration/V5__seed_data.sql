-- Seed data migrated from data.sql (converted from COPY to INSERT)

-- Users
INSERT INTO public.users (id, first_name, last_name, email, password, created_at, updated_at, deleted_at, is_active) VALUES
(1, NULL, NULL, 'user1@example.com', '$2a$10$Ki2VfnXvlkZr.958sXRnJeWIq24roGPiidYJsfaV4zZgaYRJq2ogu', '2025-10-11 17:10:58.762698', '2025-10-11 17:10:58.762698', NULL, TRUE)
ON CONFLICT (id) DO NOTHING;

-- Roles
INSERT INTO public.roles (id, name, description, created_at, updated_at, deleted_at) VALUES
(1, 'ROLE_USER', 'Role for default user', '2025-10-11 17:10:46', '2025-10-11 17:10:48', NULL),
(2, 'ROLE_OWNER', 'Role for owner', '2025-10-11 17:10:42', '2025-10-11 17:10:49', NULL)
ON CONFLICT (id) DO NOTHING;

-- User roles
INSERT INTO public.user_roles (user_id, role_id, created_at, deleted_at) VALUES
(1, 1, '2025-10-11 17:10:58.762698', NULL)
ON CONFLICT DO NOTHING;

-- Sequences positions (safe idempotent bumps)
SELECT pg_catalog.setval('public.addresses_id_seq', 1, false);
SELECT pg_catalog.setval('public.categories_id_seq', 1, false);
SELECT pg_catalog.setval('public.category_attributes_id_seq', 1, false);
SELECT pg_catalog.setval('public.newsletter_subscriptions_id_seq', 1, false);
SELECT pg_catalog.setval('public.order_items_id_seq', 1, false);
SELECT pg_catalog.setval('public.orders_id_seq', 1, false);
SELECT pg_catalog.setval('public.pages_id_seq', 1, false);
SELECT pg_catalog.setval('public.payments_id_seq', 1, false);
SELECT pg_catalog.setval('public.product_attribute_values_id_seq', 1, false);
SELECT pg_catalog.setval('public.product_images_id_seq', 1, false);
SELECT pg_catalog.setval('public.products_id_seq', 1, false);
SELECT pg_catalog.setval('public.roles_id_seq', 2, true);
SELECT pg_catalog.setval('public.users_id_seq', 1, true);
