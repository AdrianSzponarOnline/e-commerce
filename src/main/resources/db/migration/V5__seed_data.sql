-- Seed data migrated from data.sql (converted from COPY to INSERT)

-- Users
INSERT INTO public.users (id, first_name, last_name, email, password, created_at, updated_at, deleted_at, is_active) VALUES
(1, NULL, NULL, 'user1@example.com', '$2a$10$Ki2VfnXvlkZr.958sXRnJeWIq24roGPiidYJsfaV4zZgaYRJq2ogu', '2025-10-11 17:10:58.762698', '2025-10-11 17:10:58.762698', NULL, TRUE)
ON CONFLICT ON CONSTRAINT users_email_key DO NOTHING;

-- Roles
INSERT INTO public.roles (id, name, description, created_at, updated_at, deleted_at) VALUES
(1, 'ROLE_USER', 'Role for default user', '2025-10-11 17:10:46', '2025-10-11 17:10:48', NULL),
(2, 'ROLE_OWNER', 'Role for owner', '2025-10-11 17:10:42', '2025-10-11 17:10:49', NULL)
ON CONFLICT ON CONSTRAINT roles_name_key DO NOTHING;

-- User roles
INSERT INTO public.user_roles (user_id, role_id, created_at, deleted_at) VALUES
(1, 1, '2025-10-11 17:10:58.762698', NULL)
ON CONFLICT ON CONSTRAINT user_roles_pkey DO NOTHING;

-- Sequences positions (synchronize with actual max IDs in tables)
-- Use COALESCE to handle empty tables - set to 1 if table is empty, otherwise use MAX(id)
SELECT pg_catalog.setval('public.addresses_id_seq', COALESCE((SELECT MAX(id) FROM addresses), 1), true);
SELECT pg_catalog.setval('public.categories_id_seq', COALESCE((SELECT MAX(id) FROM categories), 1), true);
SELECT pg_catalog.setval('public.category_attributes_id_seq', COALESCE((SELECT MAX(id) FROM category_attributes), 1), true);
SELECT pg_catalog.setval('public.newsletter_subscriptions_id_seq', COALESCE((SELECT MAX(id) FROM newsletter_subscriptions), 1), true);
SELECT pg_catalog.setval('public.order_items_id_seq', COALESCE((SELECT MAX(id) FROM order_items), 1), true);
SELECT pg_catalog.setval('public.orders_id_seq', COALESCE((SELECT MAX(id) FROM orders), 1), true);
SELECT pg_catalog.setval('public.pages_id_seq', COALESCE((SELECT MAX(id) FROM pages), 1), true);
SELECT pg_catalog.setval('public.payments_id_seq', COALESCE((SELECT MAX(id) FROM payments), 1), true);
SELECT pg_catalog.setval('public.product_attribute_values_id_seq', COALESCE((SELECT MAX(id) FROM product_attribute_values), 1), true);
SELECT pg_catalog.setval('public.product_images_id_seq', COALESCE((SELECT MAX(id) FROM product_images), 1), true);
SELECT pg_catalog.setval('public.products_id_seq', COALESCE((SELECT MAX(id) FROM products), 1), true);
SELECT pg_catalog.setval('public.roles_id_seq', COALESCE((SELECT MAX(id) FROM roles), 1), true);
SELECT pg_catalog.setval('public.users_id_seq', COALESCE((SELECT MAX(id) FROM users), 1), true);
