-- Seed inventory for all existing products
-- Inventory dla wszystkich istniejących produktów z różnymi wartościami stock

INSERT INTO public.inventory (product_id, available_quantity, reserved_quantity, minimum_stock_level, created_at, updated_at, is_active)
SELECT 
    p.id,
    CASE 
        WHEN p.sku = 'RD-ANG-001' THEN 5
        WHEN p.sku = 'RK-LW-001' THEN 2
        WHEN p.sku = 'RM-PT-001' THEN 8
        WHEN p.sku = 'OB-PEJ-001' THEN 3
        WHEN p.sku = 'AK-POR-001' THEN 12
        WHEN p.sku = 'GR-LIN-001' THEN 20
        WHEN p.sku = 'WZ-WYS-001' THEN 6
        WHEN p.sku = 'FG-PT-001' THEN 15
        WHEN p.sku = 'BJ-NAS-001' THEN 25
        WHEN p.sku = 'LG-LMP-001' THEN 4
        ELSE 10
    END as available_quantity,
    0 as reserved_quantity,
    CASE 
        WHEN p.sku = 'RD-ANG-001' THEN 2
        WHEN p.sku = 'RK-LW-001' THEN 1
        WHEN p.sku = 'RM-PT-001' THEN 3
        WHEN p.sku = 'OB-PEJ-001' THEN 1
        WHEN p.sku = 'AK-POR-001' THEN 5
        WHEN p.sku = 'GR-LIN-001' THEN 10
        WHEN p.sku = 'WZ-WYS-001' THEN 2
        WHEN p.sku = 'FG-PT-001' THEN 5
        WHEN p.sku = 'BJ-NAS-001' THEN 10
        WHEN p.sku = 'LG-LMP-001' THEN 2
        ELSE 5
    END as minimum_stock_level,
    NOW(),
    NOW(),
    TRUE
FROM public.products p
WHERE p.deleted_at IS NULL
ON CONFLICT (product_id) DO NOTHING;

-- Adresy dla użytkowników
-- Adres dla user1@example.com
INSERT INTO public.addresses (user_id, line1, line2, city, region, postal_code, country, created_at, updated_at, is_active)
VALUES (
    (SELECT id FROM public.users WHERE email = 'user1@example.com'),
    'ul. Przykładowa 15',
    'Mieszkanie 42',
    'Warszawa',
    'Mazowieckie',
    '00-001',
    'Polska',
    NOW(),
    NOW(),
    TRUE
) ON CONFLICT DO NOTHING;

-- Adres dla testuser@example.com
INSERT INTO public.addresses (user_id, line1, line2, city, region, postal_code, country, created_at, updated_at, is_active)
VALUES (
    (SELECT id FROM public.users WHERE email = 'testuser@example.com'),
    'ul. Testowa 23',
    NULL,
    'Kraków',
    'Małopolskie',
    '30-001',
    'Polska',
    NOW(),
    NOW(),
    TRUE
) ON CONFLICT DO NOTHING;

-- Drugi adres dla testuser@example.com
INSERT INTO public.addresses (user_id, line1, line2, city, region, postal_code, country, created_at, updated_at, is_active)
VALUES (
    (SELECT id FROM public.users WHERE email = 'testuser@example.com'),
    'ul. Alternatywna 7',
    'Budynek B, lok. 5',
    'Gdańsk',
    'Pomorskie',
    '80-001',
    'Polska',
    NOW(),
    NOW(),
    TRUE
) ON CONFLICT DO NOTHING;

-- Adres dla owner@example.com
INSERT INTO public.addresses (user_id, line1, line2, city, region, postal_code, country, created_at, updated_at, is_active)
VALUES (
    (SELECT id FROM public.users WHERE email = 'owner@example.com'),
    'ul. Właścicielska 1',
    'Biuro',
    'Wrocław',
    'Dolnośląskie',
    '50-001',
    'Polska',
    NOW(),
    NOW(),
    TRUE
) ON CONFLICT DO NOTHING;

-- Więcej produktów - Rzeźby drewniane
-- 11. Rzeźba drewniana - Ptak
INSERT INTO products (category_id, name, description, short_description, price, sku, vat_rate, is_featured, shipping_cost, estimated_delivery_time, seo_slug, created_at, updated_at, is_active)
VALUES (
    (SELECT id FROM categories WHERE seo_slug = 'rzezby-drewniane'),
    'Rzeźba ptaka z drewna wiśniowego',
    'Delikatna rzeźba ptaka wykonana z drewna wiśniowego. Naturalne wykończenie, precyzyjne detale. Idealna jako dekoracja wnętrz.',
    'Rzeźba ptaka z drewna wiśniowego',
    380.00, 'RD-PT-002', 23.00, FALSE, 25.00, '3-5 dni roboczych', 'rzezba-ptaka-z-drewna-wisniowego', NOW(), NOW(), TRUE
) ON CONFLICT (sku) DO NOTHING;

INSERT INTO product_attribute_values (product_id, attribute_id, value, created_at, updated_at, is_active) VALUES
((SELECT id FROM products WHERE sku = 'RD-PT-002'), (SELECT id FROM attributes WHERE name = 'Materiał rzeźby'), 'Drewno', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'RD-PT-002'), (SELECT id FROM attributes WHERE name = 'Wysokość (cm)'), '25', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'RD-PT-002'), (SELECT id FROM attributes WHERE name = 'Szerokość (cm)'), '18', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'RD-PT-002'), (SELECT id FROM attributes WHERE name = 'Głębokość (cm)'), '12', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'RD-PT-002'), (SELECT id FROM attributes WHERE name = 'Waga (kg)'), '1.2', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'RD-PT-002'), (SELECT id FROM attributes WHERE name = 'Styl'), 'Naturalistyczny', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'RD-PT-002'), (SELECT id FROM attributes WHERE name = 'Rodzaj drewna'), 'Wiśnia', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'RD-PT-002'), (SELECT id FROM attributes WHERE name = 'Wykończenie'), 'Naturalne', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'RD-PT-002'), (SELECT id FROM attributes WHERE name = 'Artysta/Rzemieślnik'), 'Jan Kowalski', NOW(), NOW(), TRUE)
ON CONFLICT DO NOTHING;

-- 12. Rzeźba kamienna - Anioł
INSERT INTO products (category_id, name, description, short_description, price, sku, vat_rate, is_featured, shipping_cost, estimated_delivery_time, seo_slug, created_at, updated_at, is_active)
VALUES (
    (SELECT id FROM categories WHERE seo_slug = 'rzezby-kamienne'),
    'Rzeźba anioła z piaskowca',
    'Piękna rzeźba anioła wykonana z piaskowca. Naturalna faktura kamienia, elegancka forma. Idealna do ogrodu lub wnętrza.',
    'Rzeźba anioła z piaskowca',
    750.00, 'RK-ANG-002', 23.00, FALSE, 45.00, '5-7 dni roboczych', 'rzezba-anioka-z-piaskowca', NOW(), NOW(), TRUE
) ON CONFLICT (sku) DO NOTHING;

INSERT INTO product_attribute_values (product_id, attribute_id, value, created_at, updated_at, is_active) VALUES
((SELECT id FROM products WHERE sku = 'RK-ANG-002'), (SELECT id FROM attributes WHERE name = 'Materiał rzeźby'), 'Kamień', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'RK-ANG-002'), (SELECT id FROM attributes WHERE name = 'Wysokość (cm)'), '40', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'RK-ANG-002'), (SELECT id FROM attributes WHERE name = 'Szerokość (cm)'), '25', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'RK-ANG-002'), (SELECT id FROM attributes WHERE name = 'Głębokość (cm)'), '20', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'RK-ANG-002'), (SELECT id FROM attributes WHERE name = 'Waga (kg)'), '15.0', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'RK-ANG-002'), (SELECT id FROM attributes WHERE name = 'Rodzaj kamienia'), 'Piaskowiec', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'RK-ANG-002'), (SELECT id FROM attributes WHERE name = 'Polerowanie'), 'false', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'RK-ANG-002'), (SELECT id FROM attributes WHERE name = 'Artysta/Rzemieślnik'), 'Anna Nowak', NOW(), NOW(), TRUE)
ON CONFLICT DO NOTHING;

-- 13. Rzeźba metalowa - Koń
INSERT INTO products (category_id, name, description, short_description, price, sku, vat_rate, is_featured, shipping_cost, estimated_delivery_time, seo_slug, created_at, updated_at, is_active)
VALUES (
    (SELECT id FROM categories WHERE seo_slug = 'rzezby-metalowe'),
    'Rzeźba konia z miedzi',
    'Dynamiczna rzeźba konia wykonana z miedzi. Patynowana powierzchnia, artystyczna forma. Doskonała do kolekcji.',
    'Rzeźba konia z patynowanej miedzi',
    950.00, 'RM-KON-002', 23.00, TRUE, 40.00, '4-6 dni roboczych', 'rzezba-konia-z-miedzi', NOW(), NOW(), TRUE
) ON CONFLICT (sku) DO NOTHING;

INSERT INTO product_attribute_values (product_id, attribute_id, value, created_at, updated_at, is_active) VALUES
((SELECT id FROM products WHERE sku = 'RM-KON-002'), (SELECT id FROM attributes WHERE name = 'Materiał rzeźby'), 'Metal', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'RM-KON-002'), (SELECT id FROM attributes WHERE name = 'Wysokość (cm)'), '35', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'RM-KON-002'), (SELECT id FROM attributes WHERE name = 'Szerokość (cm)'), '45', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'RM-KON-002'), (SELECT id FROM attributes WHERE name = 'Głębokość (cm)'), '20', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'RM-KON-002'), (SELECT id FROM attributes WHERE name = 'Waga (kg)'), '8.5', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'RM-KON-002'), (SELECT id FROM attributes WHERE name = 'Rodzaj metalu'), 'Miedź', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'RM-KON-002'), (SELECT id FROM attributes WHERE name = 'Patynowanie'), 'true', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'RM-KON-002'), (SELECT id FROM attributes WHERE name = 'Artysta/Rzemieślnik'), 'Piotr Wiśniewski', NOW(), NOW(), TRUE)
ON CONFLICT DO NOTHING;

-- 14. Obraz olejny - Portret
INSERT INTO products (category_id, name, description, short_description, price, sku, vat_rate, is_featured, shipping_cost, estimated_delivery_time, seo_slug, created_at, updated_at, is_active)
VALUES (
    (SELECT id FROM categories WHERE seo_slug = 'obrazy-olejne'),
    'Portret olejny - Kobieta z kwiatami',
    'Piękny portret olejny przedstawiający kobietę z kwiatami. Namalowany na płótnie bawełnianym. Oprawiony w złotą ramę. Z certyfikatem autentyczności.',
    'Portret olejny - kobieta z kwiatami',
    780.00, 'OB-POR-002', 23.00, TRUE, 30.00, '2-3 dni roboczych', 'portret-olejny-kobieta-z-kwiatami', NOW(), NOW(), TRUE
) ON CONFLICT (sku) DO NOTHING;

INSERT INTO product_attribute_values (product_id, attribute_id, value, created_at, updated_at, is_active) VALUES
((SELECT id FROM products WHERE sku = 'OB-POR-002'), (SELECT id FROM attributes WHERE name = 'Technika malarska'), 'Olejna', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'OB-POR-002'), (SELECT id FROM attributes WHERE name = 'Wymiary (cm)'), '60x80 cm', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'OB-POR-002'), (SELECT id FROM attributes WHERE name = 'Rama'), 'true', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'OB-POR-002'), (SELECT id FROM attributes WHERE name = 'Rodzaj farb'), 'Farba olejna', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'OB-POR-002'), (SELECT id FROM attributes WHERE name = 'Rodzaj płótna'), 'Płótno bawełniane', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'OB-POR-002'), (SELECT id FROM attributes WHERE name = 'Certyfikat autentyczności'), 'true', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'OB-POR-002'), (SELECT id FROM attributes WHERE name = 'Artysta/Rzemieślnik'), 'Maria Zielińska', NOW(), NOW(), TRUE)
ON CONFLICT DO NOTHING;

-- 15. Akwarela - Pejzaż
INSERT INTO products (category_id, name, description, short_description, price, sku, vat_rate, is_featured, shipping_cost, estimated_delivery_time, seo_slug, created_at, updated_at, is_active)
VALUES (
    (SELECT id FROM categories WHERE seo_slug = 'akwarele'),
    'Pejzaż morski - akwarela',
    'Delikatny pejzaż morski wykonany techniką akwarelową. Pastelowe kolory, subtelne przejścia. Na wysokiej jakości papierze akwarelowym.',
    'Pejzaż morski - akwarela',
    420.00, 'AK-PEJ-002', 23.00, FALSE, 20.00, '2-3 dni roboczych', 'pejzaz-morski-akwarela', NOW(), NOW(), TRUE
) ON CONFLICT (sku) DO NOTHING;

INSERT INTO product_attribute_values (product_id, attribute_id, value, created_at, updated_at, is_active) VALUES
((SELECT id FROM products WHERE sku = 'AK-PEJ-002'), (SELECT id FROM attributes WHERE name = 'Technika malarska'), 'Akwarelowa', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'AK-PEJ-002'), (SELECT id FROM attributes WHERE name = 'Rodzaj podłoża'), 'Papier akwarelowy', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'AK-PEJ-002'), (SELECT id FROM attributes WHERE name = 'Wymiary (cm)'), '40x50 cm', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'AK-PEJ-002'), (SELECT id FROM attributes WHERE name = 'Rodzaj papieru'), 'Papier Canson 300g', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'AK-PEJ-002'), (SELECT id FROM attributes WHERE name = 'Technika akwarelowa'), 'Mokre na mokrym', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'AK-PEJ-002'), (SELECT id FROM attributes WHERE name = 'Artysta/Rzemieślnik'), 'Tomasz Kowalczyk', NOW(), NOW(), TRUE)
ON CONFLICT DO NOTHING;

-- 16. Grafika artystyczna - Drzeworyt
INSERT INTO products (category_id, name, description, short_description, price, sku, vat_rate, is_featured, shipping_cost, estimated_delivery_time, seo_slug, created_at, updated_at, is_active)
VALUES (
    (SELECT id FROM categories WHERE seo_slug = 'grafika-artystyczna'),
    'Drzeworyt - Las jesienny',
    'Oryginalny drzeworyt przedstawiający las jesienny. Wykonany techniką druku wypukłego. Numerowany egzemplarz z serii limitowanej.',
    'Drzeworyt - las jesienny, numerowany',
    220.00, 'GR-DRW-002', 23.00, FALSE, 15.00, '2-4 dni roboczych', 'drzeworyt-las-jesienny', NOW(), NOW(), TRUE
) ON CONFLICT (sku) DO NOTHING;

INSERT INTO product_attribute_values (product_id, attribute_id, value, created_at, updated_at, is_active) VALUES
((SELECT id FROM products WHERE sku = 'GR-DRW-002'), (SELECT id FROM attributes WHERE name = 'Technika graficzna'), 'Drzeworyt', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'GR-DRW-002'), (SELECT id FROM attributes WHERE name = 'Rodzaj papieru'), 'Papier akwarelowy', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'GR-DRW-002'), (SELECT id FROM attributes WHERE name = 'Numeracja'), 'true', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'GR-DRW-002'), (SELECT id FROM attributes WHERE name = 'Technika wykonania'), 'Druk wypukły', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'GR-DRW-002'), (SELECT id FROM attributes WHERE name = 'Artysta/Rzemieślnik'), 'Katarzyna Lewandowska', NOW(), NOW(), TRUE)
ON CONFLICT DO NOTHING;

-- 17. Wazon szklany - Niski
INSERT INTO products (category_id, name, description, short_description, price, sku, vat_rate, is_featured, shipping_cost, estimated_delivery_time, seo_slug, created_at, updated_at, is_active)
VALUES (
    (SELECT id FROM categories WHERE seo_slug = 'wazony-szklane'),
    'Niski wazon szklany ręcznie formowany',
    'Elegancki niski wazon wykonany ze szkła ręcznie formowanego. Unikalny kształt, zielone szkło. Idealny do kwiatów lub jako dekoracja.',
    'Niski wazon ręcznie formowany ze szkła',
    240.00, 'WZ-NIS-002', 23.00, FALSE, 25.00, '3-5 dni roboczych', 'niski-wazon-szklany-recznie-formowany', NOW(), NOW(), TRUE
) ON CONFLICT (sku) DO NOTHING;

INSERT INTO product_attribute_values (product_id, attribute_id, value, created_at, updated_at, is_active) VALUES
((SELECT id FROM products WHERE sku = 'WZ-NIS-002'), (SELECT id FROM attributes WHERE name = 'Rodzaj szkła'), 'Szkło sodowe', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'WZ-NIS-002'), (SELECT id FROM attributes WHERE name = 'Technika wykonania'), 'Formowanie ręczne', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'WZ-NIS-002'), (SELECT id FROM attributes WHERE name = 'Kolor szkła'), 'Zielony', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'WZ-NIS-002'), (SELECT id FROM attributes WHERE name = 'Pojemność (ml)'), '800', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'WZ-NIS-002'), (SELECT id FROM attributes WHERE name = 'Kształt'), 'Niski, szeroki', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'WZ-NIS-002'), (SELECT id FROM attributes WHERE name = 'Artysta/Rzemieślnik'), 'Robert Szklarz', NOW(), NOW(), TRUE)
ON CONFLICT DO NOTHING;

-- 18. Figurka szklana - Kot
INSERT INTO products (category_id, name, description, short_description, price, sku, vat_rate, is_featured, shipping_cost, estimated_delivery_time, seo_slug, created_at, updated_at, is_active)
VALUES (
    (SELECT id FROM categories WHERE seo_slug = 'figurki-szklane'),
    'Figurka kota ze szkła artystycznego',
    'Delikatna figurka kota wykonana ze szkła artystycznego. Precyzyjne detale, żywe kolory. W zestawie podstawka. Unikalny egzemplarz.',
    'Figurka kota ze szkła z podstawką',
    180.00, 'FG-KOT-002', 23.00, FALSE, 20.00, '2-3 dni roboczych', 'figurka-kota-ze-szkla-artystycznego', NOW(), NOW(), TRUE
) ON CONFLICT (sku) DO NOTHING;

INSERT INTO product_attribute_values (product_id, attribute_id, value, created_at, updated_at, is_active) VALUES
((SELECT id FROM products WHERE sku = 'FG-KOT-002'), (SELECT id FROM attributes WHERE name = 'Rodzaj szkła'), 'Szkło kryształowe', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'FG-KOT-002'), (SELECT id FROM attributes WHERE name = 'Motyw'), 'Kot', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'FG-KOT-002'), (SELECT id FROM attributes WHERE name = 'Wysokość (cm)'), '12', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'FG-KOT-002'), (SELECT id FROM attributes WHERE name = 'Podstawa'), 'true', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'FG-KOT-002'), (SELECT id FROM attributes WHERE name = 'Artysta/Rzemieślnik'), 'Agnieszka Srebrna', NOW(), NOW(), TRUE)
ON CONFLICT DO NOTHING;

-- 19. Biżuteria szklana - Kolczyki
INSERT INTO products (category_id, name, description, short_description, price, sku, vat_rate, is_featured, shipping_cost, estimated_delivery_time, seo_slug, created_at, updated_at, is_active)
VALUES (
    (SELECT id FROM categories WHERE seo_slug = 'bizuteria-szklana'),
    'Kolczyki ze szklanymi koralikami',
    'Eleganckie kolczyki wykonane ręcznie ze szklanych koralików. Unikalna kolorystyka, precyzyjne wykonanie. Idealne na prezent.',
    'Kolczyki ze szklanymi koralikami',
    75.00, 'BJ-KOL-002', 23.00, FALSE, 10.00, '2-3 dni roboczych', 'kolczyki-ze-szklanymi-koralikami', NOW(), NOW(), TRUE
) ON CONFLICT (sku) DO NOTHING;

INSERT INTO product_attribute_values (product_id, attribute_id, value, created_at, updated_at, is_active) VALUES
((SELECT id FROM products WHERE sku = 'BJ-KOL-002'), (SELECT id FROM attributes WHERE name = 'Rodzaj szkła'), 'Szkło Murano', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'BJ-KOL-002'), (SELECT id FROM attributes WHERE name = 'Rodzaj biżuterii'), 'Kolczyki', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'BJ-KOL-002'), (SELECT id FROM attributes WHERE name = 'Długość (cm)'), '3', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'BJ-KOL-002'), (SELECT id FROM attributes WHERE name = 'Zapięcie'), 'Zapięcie francuskie', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'BJ-KOL-002'), (SELECT id FROM attributes WHERE name = 'Zestaw'), 'true', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'BJ-KOL-002'), (SELECT id FROM attributes WHERE name = 'Artysta/Rzemieślnik'), 'Ewa Perłowa', NOW(), NOW(), TRUE)
ON CONFLICT DO NOTHING;

-- 20. Szkło artystyczne - Wazon dekoracyjny
INSERT INTO products (category_id, name, description, short_description, price, sku, vat_rate, is_featured, shipping_cost, estimated_delivery_time, seo_slug, created_at, updated_at, is_active)
VALUES (
    (SELECT id FROM categories WHERE seo_slug = 'szklo-artystyczne'),
    'Wazon dekoracyjny ze szkła artystycznego',
    'Wyjątkowy wazon dekoracyjny wykonany ze szkła artystycznego. Unikalny design, kolorowe szkło. Idealny do salonu lub sypialni.',
    'Wazon dekoracyjny ze szkła artystycznego',
    520.00, 'LG-WAZ-002', 23.00, FALSE, 35.00, '4-6 dni roboczych', 'wazon-dekoracyjny-ze-szkla-artystycznego', NOW(), NOW(), TRUE
) ON CONFLICT (sku) DO NOTHING;

INSERT INTO product_attribute_values (product_id, attribute_id, value, created_at, updated_at, is_active) VALUES
((SELECT id FROM products WHERE sku = 'LG-WAZ-002'), (SELECT id FROM attributes WHERE name = 'Rodzaj szkła'), 'Szkło artystyczne', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'LG-WAZ-002'), (SELECT id FROM attributes WHERE name = 'Funkcjonalność'), 'Wazon dekoracyjny', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'LG-WAZ-002'), (SELECT id FROM attributes WHERE name = 'Montaż'), 'Stojący', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'LG-WAZ-002'), (SELECT id FROM attributes WHERE name = 'Artysta/Rzemieślnik'), 'Maciej Świetlny', NOW(), NOW(), TRUE)
ON CONFLICT DO NOTHING;

-- Inventory dla nowych produktów
INSERT INTO public.inventory (product_id, available_quantity, reserved_quantity, minimum_stock_level, created_at, updated_at, is_active)
SELECT 
    p.id,
    CASE 
        WHEN p.sku = 'RD-PT-002' THEN 7
        WHEN p.sku = 'RK-ANG-002' THEN 3
        WHEN p.sku = 'RM-KON-002' THEN 4
        WHEN p.sku = 'OB-POR-002' THEN 2
        WHEN p.sku = 'AK-PEJ-002' THEN 10
        WHEN p.sku = 'GR-DRW-002' THEN 18
        WHEN p.sku = 'WZ-NIS-002' THEN 8
        WHEN p.sku = 'FG-KOT-002' THEN 20
        WHEN p.sku = 'BJ-KOL-002' THEN 30
        WHEN p.sku = 'LG-WAZ-002' THEN 5
        ELSE 10
    END as available_quantity,
    0 as reserved_quantity,
    CASE 
        WHEN p.sku = 'RD-PT-002' THEN 3
        WHEN p.sku = 'RK-ANG-002' THEN 1
        WHEN p.sku = 'RM-KON-002' THEN 2
        WHEN p.sku = 'OB-POR-002' THEN 1
        WHEN p.sku = 'AK-PEJ-002' THEN 5
        WHEN p.sku = 'GR-DRW-002' THEN 8
        WHEN p.sku = 'WZ-NIS-002' THEN 3
        WHEN p.sku = 'FG-KOT-002' THEN 8
        WHEN p.sku = 'BJ-KOL-002' THEN 12
        WHEN p.sku = 'LG-WAZ-002' THEN 2
        ELSE 5
    END as minimum_stock_level,
    NOW(),
    NOW(),
    TRUE
FROM public.products p
WHERE p.deleted_at IS NULL
  AND p.sku IN ('RD-PT-002', 'RK-ANG-002', 'RM-KON-002', 'OB-POR-002', 'AK-PEJ-002', 'GR-DRW-002', 'WZ-NIS-002', 'FG-KOT-002', 'BJ-KOL-002', 'LG-WAZ-002')
ON CONFLICT (product_id) DO NOTHING;

-- Aktualizacja sekwencji
SELECT pg_catalog.setval('public.addresses_id_seq', COALESCE((SELECT MAX(id) FROM addresses), 1), true);
SELECT pg_catalog.setval('public.inventory_id_seq', COALESCE((SELECT MAX(id) FROM inventory), 1), true);
SELECT pg_catalog.setval('public.products_id_seq', COALESCE((SELECT MAX(id) FROM products), 1), true);
SELECT pg_catalog.setval('public.product_attribute_values_id_seq', COALESCE((SELECT MAX(id) FROM product_attribute_values), 1), true);

