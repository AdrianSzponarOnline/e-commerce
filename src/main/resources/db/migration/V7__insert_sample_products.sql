-- Insert sample products with attribute values for craft categories

-- 1. Rzeźba drewniana - Rzeźba anioła
INSERT INTO products (id, category_id, name, description, short_description, price, sku, vat_rate, is_featured, shipping_cost, estimated_delivery_time, seo_slug, created_at, updated_at, is_active) VALUES
    (1, 5, 'Rzeźba anioła z drewna dębowego',
     'Unikalna rzeźba anioła wykonana ręcznie z dębu. Wysoka jakość wykonania, elegancka forma. Idealna jako dekoracja wnętrz lub prezent.',
     'Ręcznie wykonana rzeźba anioła z dębu',
     450.00, 'RD-ANG-001', 23.00, TRUE, 25.00, '3-5 dni roboczych', 'rzezba-anioka-z-drewna-debowego', NOW(), NOW(), TRUE)
ON CONFLICT ON CONSTRAINT products_sku_key DO NOTHING;

-- Atrybuty dla rzeźby drewnianej (kategoria 5: Rzeźby drewniane)
-- Atrybuty z kategorii nadrzędnej (2: Rzeźby)
INSERT INTO product_attribute_values (product_id, category_attribute_id, value, created_at, updated_at, is_active) VALUES
                                                                                                                       (1, 6, 'Drewno', NOW(), NOW(), TRUE),  -- Materiał rzeźby
                                                                                                                       (1, 7, '35', NOW(), NOW(), TRUE),       -- Wysokość (cm)
                                                                                                                       (1, 8, '20', NOW(), NOW(), TRUE),       -- Szerokość (cm)
                                                                                                                       (1, 9, '15', NOW(), NOW(), TRUE),       -- Głębokość (cm)
                                                                                                                       (1, 10, '2.5', NOW(), NOW(), TRUE),     -- Waga (kg)
                                                                                                                       (1, 11, 'Klasyczny', NOW(), NOW(), TRUE); -- Styl

-- Atrybuty specyficzne dla rzeźb drewnianych
INSERT INTO product_attribute_values (product_id, category_attribute_id, value, created_at, updated_at, is_active) VALUES
                                                                                                                       (1, 21, 'Dąb', NOW(), NOW(), TRUE),     -- Rodzaj drewna
                                                                                                                       (1, 22, 'Lakierowana', NOW(), NOW(), TRUE)
ON CONFLICT ON CONSTRAINT product_attribute_values_product_id_category_attribute_id_key DO NOTHING; -- Wykończenie

-- Atrybuty z głównej kategorii (1: Rękodzieło)
INSERT INTO product_attribute_values (product_id, category_attribute_id, value, created_at, updated_at, is_active) VALUES
                                                                                                                       (1, 1, 'Drewno dębowe', NOW(), NOW(), TRUE),
                                                                                                                       (1, 2, 'Ręczne rzeźbienie', NOW(), NOW(), TRUE),
                                                                                                                       (1, 3, '35x20x15 cm', NOW(), NOW(), TRUE),
                                                                                                                       (1, 4, 'Naturalny kolor drewna', NOW(), NOW(), TRUE),
                                                                                                                       (1, 5, 'Jan Kowalski', NOW(), NOW(), TRUE)
ON CONFLICT ON CONSTRAINT product_attribute_values_product_id_category_attribute_id_key DO NOTHING;

-- 2. Rzeźba kamienna - Lwia głowa
INSERT INTO products (id, category_id, name, description, short_description, price, sku, vat_rate, is_featured, shipping_cost, estimated_delivery_time, seo_slug, created_at, updated_at, is_active) VALUES
    (2, 6, 'Rzeźba lwiej głowy z marmuru',
     'Imponująca rzeźba lwiej głowy wykonana z białego marmuru. Precyzyjne detale, polerowana powierzchnia. Doskonała jako element dekoracyjny ogrodu lub wnętrza.',
     'Rzeźba lwiej głowy z białego marmuru',
     1200.00, 'RK-LW-001', 23.00, TRUE, 50.00, '5-7 dni roboczych', 'rzezba-lwiej-glowy-z-marmuru', NOW(), NOW(), TRUE)
ON CONFLICT ON CONSTRAINT products_sku_key DO NOTHING;

INSERT INTO product_attribute_values (product_id, category_attribute_id, value, created_at, updated_at, is_active) VALUES
                                                                                                                       (2, 6, 'Kamień', NOW(), NOW(), TRUE),
                                                                                                                       (2, 7, '45', NOW(), NOW(), TRUE),
                                                                                                                       (2, 8, '35', NOW(), NOW(), TRUE),
                                                                                                                       (2, 9, '25', NOW(), NOW(), TRUE),
                                                                                                                       (2, 10, '18.5', NOW(), NOW(), TRUE),
                                                                                                                       (2, 11, 'Realistyczny', NOW(), NOW(), TRUE),
                                                                                                                       (2, 23, 'Marmur biały', NOW(), NOW(), TRUE),
                                                                                                                       (2, 24, 'true', NOW(), NOW(), TRUE),
                                                                                                                       (2, 1, 'Marmur biały', NOW(), NOW(), TRUE),
                                                                                                                       (2, 2, 'Rzeźbienie w kamieniu', NOW(), NOW(), TRUE),
                                                                                                                       (2, 3, '45x35x25 cm', NOW(), NOW(), TRUE),
                                                                                                                       (2, 4, 'Biały', NOW(), NOW(), TRUE),
                                                                                                                       (2, 5, 'Anna Nowak', NOW(), NOW(), TRUE);

-- 3. Rzeźba metalowa - Ptak
INSERT INTO products (id, category_id, name, description, short_description, price, sku, vat_rate, is_featured, shipping_cost, estimated_delivery_time, seo_slug, created_at, updated_at, is_active) VALUES
    (3, 7, 'Rzeźba ptaka z brązu',
     'Elegancka rzeźba ptaka wykonana z brązu z patynowaną powierzchnią. Artystyczna forma, idealna do kolekcji sztuki.',
     'Rzeźba ptaka z patynowanego brązu',
     850.00, 'RM-PT-001', 23.00, FALSE, 35.00, '4-6 dni roboczych', 'rzezba-ptaka-z-brazu', NOW(), NOW(), TRUE)
ON CONFLICT ON CONSTRAINT products_sku_key DO NOTHING;

INSERT INTO product_attribute_values (product_id, category_attribute_id, value, created_at, updated_at, is_active) VALUES
                                                                                                                       (3, 6, 'Metal', NOW(), NOW(), TRUE),
                                                                                                                       (3, 7, '30', NOW(), NOW(), TRUE),
                                                                                                                       (3, 8, '25', NOW(), NOW(), TRUE),
                                                                                                                       (3, 9, '20', NOW(), NOW(), TRUE),
                                                                                                                       (3, 10, '4.2', NOW(), NOW(), TRUE),
                                                                                                                       (3, 11, 'Abstrakcyjny', NOW(), NOW(), TRUE),
                                                                                                                       (3, 25, 'Brąz', NOW(), NOW(), TRUE),
                                                                                                                       (3, 26, 'true', NOW(), NOW(), TRUE),
                                                                                                                       (3, 1, 'Brąz', NOW(), NOW(), TRUE),
                                                                                                                       (3, 2, 'Odlewanie i spawanie', NOW(), NOW(), TRUE),
                                                                                                                       (3, 3, '30x25x20 cm', NOW(), NOW(), TRUE),
                                                                                                                       (3, 4, 'Patynowany brąz', NOW(), NOW(), TRUE),
                                                                                                                       (3, 5, 'Piotr Wiśniewski', NOW(), NOW(), TRUE);

-- 4. Obraz olejny - Pejzaż
INSERT INTO products (id, category_id, name, description, short_description, price, sku, vat_rate, is_featured, shipping_cost, estimated_delivery_time, seo_slug, created_at, updated_at, is_active) VALUES
    (4, 8, 'Pejzaż górski - obraz olejny',
     'Piękny obraz olejny przedstawiający górski pejzaż. Namalowany na płótnie bawełnianym farbami olejnymi. Oprawiony w elegancką ramę. Z certyfikatem autentyczności.',
     'Obraz olejny - pejzaż górski na płótnie',
     650.00, 'OB-PEJ-001', 23.00, TRUE, 30.00, '2-3 dni roboczych', 'pejzaz-gorski-obraz-olejny', NOW(), NOW(), TRUE)
ON CONFLICT ON CONSTRAINT products_sku_key DO NOTHING;

INSERT INTO product_attribute_values (product_id, category_attribute_id, value, created_at, updated_at, is_active) VALUES
                                                                                                                       (4, 12, 'Olejna', NOW(), NOW(), TRUE),
                                                                                                                       (4, 13, '50x70 cm', NOW(), NOW(), TRUE),
                                                                                                                       (4, 14, 'Płótno bawełniane', NOW(), NOW(), TRUE),
                                                                                                                       (4, 15, 'true', NOW(), NOW(), TRUE),
                                                                                                                       (4, 16, '2024', NOW(), NOW(), TRUE),
                                                                                                                       (4, 27, 'Farba olejna', NOW(), NOW(), TRUE),
                                                                                                                       (4, 28, 'Płótno bawełniane', NOW(), NOW(), TRUE),
                                                                                                                       (4, 29, 'true', NOW(), NOW(), TRUE),
                                                                                                                       (4, 1, 'Farba olejna', NOW(), NOW(), TRUE),
                                                                                                                       (4, 2, 'Malarstwo olejne', NOW(), NOW(), TRUE),
                                                                                                                       (4, 3, '50x70 cm', NOW(), NOW(), TRUE),
                                                                                                                       (4, 4, 'Wielobarwny', NOW(), NOW(), TRUE),
                                                                                                                       (4, 5, 'Maria Zielińska', NOW(), NOW(), TRUE);

-- 5. Akwarela - Portret
INSERT INTO products (id, category_id, name, description, short_description, price, sku, vat_rate, is_featured, shipping_cost, estimated_delivery_time, seo_slug, created_at, updated_at, is_active) VALUES
    (5, 9, 'Portret akwarelowy',
     'Delikatny portret wykonany techniką akwarelową na wysokiej jakości papierze. Pastelowe kolory, subtelne przejścia. Idealny prezent.',
     'Portret akwarelowy na papierze',
     320.00, 'AK-POR-001', 23.00, FALSE, 20.00, '2-3 dni roboczych', 'portret-akwarelowy', NOW(), NOW(), TRUE)
ON CONFLICT ON CONSTRAINT products_sku_key DO NOTHING;

INSERT INTO product_attribute_values (product_id, category_attribute_id, value, created_at, updated_at, is_active) VALUES
                                                                                                                       (5, 12, 'Akwarelowa', NOW(), NOW(), TRUE),
                                                                                                                       (5, 13, '30x40 cm', NOW(), NOW(), TRUE),
                                                                                                                       (5, 14, 'Papier akwarelowy', NOW(), NOW(), TRUE),
                                                                                                                       (5, 15, 'false', NOW(), NOW(), TRUE),
                                                                                                                       (5, 16, '2024', NOW(), NOW(), TRUE),
                                                                                                                       (5, 30, 'Papier Canson 300g', NOW(), NOW(), TRUE),
                                                                                                                       (5, 31, 'Mokre na mokrym', NOW(), NOW(), TRUE),
                                                                                                                       (5, 1, 'Farba akwarelowa', NOW(), NOW(), TRUE),
                                                                                                                       (5, 2, 'Technika akwarelowa', NOW(), NOW(), TRUE),
                                                                                                                       (5, 3, '30x40 cm', NOW(), NOW(), TRUE),
                                                                                                                       (5, 4, 'Pastelowy', NOW(), NOW(), TRUE),
                                                                                                                       (5, 5, 'Tomasz Kowalczyk', NOW(), NOW(), TRUE);

-- 6. Grafika artystyczna - Linoryt
INSERT INTO products (id, category_id, name, description, short_description, price, sku, vat_rate, is_featured, shipping_cost, estimated_delivery_time, seo_slug, created_at, updated_at, is_active) VALUES
    (6, 10, 'Linoryt - Motywy roślinne',
     'Oryginalny linoryt przedstawiający motywy roślinne. Wykonany techniką druku wypukłego. Numerowany egzemplarz z serii limitowanej.',
     'Linoryt - motywy roślinne, numerowany',
     180.00, 'GR-LIN-001', 23.00, FALSE, 15.00, '2-4 dni roboczych', 'linoryt-motywy-roslinne', NOW(), NOW(), TRUE)
ON CONFLICT ON CONSTRAINT products_sku_key DO NOTHING;

INSERT INTO product_attribute_values (product_id, category_attribute_id, value, created_at, updated_at, is_active) VALUES
                                                                                                                       (6, 12, 'Grafika', NOW(), NOW(), TRUE),
                                                                                                                       (6, 13, '25x35 cm', NOW(), NOW(), TRUE),
                                                                                                                       (6, 14, 'Papier graficzny', NOW(), NOW(), TRUE),
                                                                                                                       (6, 15, 'false', NOW(), NOW(), TRUE),
                                                                                                                       (6, 16, '2023', NOW(), NOW(), TRUE),
                                                                                                                       (6, 32, 'Linoryt', NOW(), NOW(), TRUE),
                                                                                                                       (6, 33, 'Papier graficzny 250g', NOW(), NOW(), TRUE),
                                                                                                                       (6, 34, 'true', NOW(), NOW(), TRUE),
                                                                                                                       (6, 1, 'Tusz drukarski', NOW(), NOW(), TRUE),
                                                                                                                       (6, 2, 'Druk wypukły', NOW(), NOW(), TRUE),
                                                                                                                       (6, 3, '25x35 cm', NOW(), NOW(), TRUE),
                                                                                                                       (6, 4, 'Czarno-biały', NOW(), NOW(), TRUE),
                                                                                                                       (6, 5, 'Katarzyna Lewandowska', NOW(), NOW(), TRUE);

-- 7. Wazon szklany - Wysoki
INSERT INTO products (id, category_id, name, description, short_description, price, sku, vat_rate, is_featured, shipping_cost, estimated_delivery_time, seo_slug, created_at, updated_at, is_active) VALUES
    (7, 11, 'Wysoki wazon szklany ręcznie formowany',
     'Elegancki wysoki wazon wykonany ze szkła ręcznie formowanego. Unikalny kształt, kolorowe szkło. Idealny do kwiatów lub jako dekoracja.',
     'Wysoki wazon ręcznie formowany ze szkła',
     280.00, 'WZ-WYS-001', 23.00, FALSE, 25.00, '3-5 dni roboczych', 'wysoki-wazon-szklany-recznie-formowany', NOW(), NOW(), TRUE)
ON CONFLICT ON CONSTRAINT products_sku_key DO NOTHING;

INSERT INTO product_attribute_values (product_id, category_attribute_id, value, created_at, updated_at, is_active) VALUES
                                                                                                                       (7, 17, 'Szkło sodowe', NOW(), NOW(), TRUE),
                                                                                                                       (7, 18, 'Formowanie ręczne', NOW(), NOW(), TRUE),
                                                                                                                       (7, 19, 'Błękitny', NOW(), NOW(), TRUE),
                                                                                                                       (7, 20, 'Dekoracyjny', NOW(), NOW(), TRUE),
                                                                                                                       (7, 35, '1500', NOW(), NOW(), TRUE),
                                                                                                                       (7, 36, 'Wysoki, smukły', NOW(), NOW(), TRUE),
                                                                                                                       (7, 37, 'Dekoracyjny', NOW(), NOW(), TRUE),
                                                                                                                       (7, 1, 'Szkło sodowe', NOW(), NOW(), TRUE),
                                                                                                                       (7, 2, 'Formowanie ręczne na piszczeli', NOW(), NOW(), TRUE),
                                                                                                                       (7, 3, '35x12 cm', NOW(), NOW(), TRUE),
                                                                                                                       (7, 4, 'Błękitny', NOW(), NOW(), TRUE),
                                                                                                                       (7, 5, 'Robert Szklarz', NOW(), NOW(), TRUE);

-- 8. Figurka szklana - Ptak
INSERT INTO products (id, category_id, name, description, short_description, price, sku, vat_rate, is_featured, shipping_cost, estimated_delivery_time, seo_slug, created_at, updated_at, is_active) VALUES
    (8, 12, 'Figurka ptaka ze szkła artystycznego',
     'Delikatna figurka ptaka wykonana ze szkła artystycznego. Precyzyjne detale, żywe kolory. W zestawie podstawka. Unikalny egzemplarz.',
     'Figurka ptaka ze szkła z podstawką',
     150.00, 'FG-PT-001', 23.00, FALSE, 20.00, '2-3 dni roboczych', 'figurka-ptaka-ze-szkla-artystycznego', NOW(), NOW(), TRUE)
ON CONFLICT ON CONSTRAINT products_sku_key DO NOTHING;

INSERT INTO product_attribute_values (product_id, category_attribute_id, value, created_at, updated_at, is_active) VALUES
                                                                                                                       (8, 17, 'Szkło kryształowe', NOW(), NOW(), TRUE),
                                                                                                                       (8, 18, 'Tłoczenie i szlifowanie', NOW(), NOW(), TRUE),
                                                                                                                       (8, 19, 'Wielokolorowy', NOW(), NOW(), TRUE),
                                                                                                                       (8, 20, 'Dekoracyjny', NOW(), NOW(), TRUE),
                                                                                                                       (8, 38, 'Ptak', NOW(), NOW(), TRUE),
                                                                                                                       (8, 39, '15', NOW(), NOW(), TRUE),
                                                                                                                       (8, 40, 'true', NOW(), NOW(), TRUE),
                                                                                                                       (8, 1, 'Szkło kryształowe', NOW(), NOW(), TRUE),
                                                                                                                       (8, 2, 'Tłoczenie i szlifowanie', NOW(), NOW(), TRUE),
                                                                                                                       (8, 3, '15x8x6 cm', NOW(), NOW(), TRUE),
                                                                                                                       (8, 4, 'Wielokolorowy', NOW(), NOW(), TRUE),
                                                                                                                       (8, 5, 'Agnieszka Srebrna', NOW(), NOW(), TRUE);

-- 9. Biżuteria szklana - Naszyjnik
INSERT INTO products (id, category_id, name, description, short_description, price, sku, vat_rate, is_featured, shipping_cost, estimated_delivery_time, seo_slug, created_at, updated_at, is_active) VALUES
    (9, 13, 'Naszyjnik ze szklanymi koralikami',
     'Elegancki naszyjnik wykonany ręcznie ze szklanych koralików. Unikalna kolorystyka, precyzyjne wykonanie. W zestawie zapięcie. Idealny na prezent.',
     'Naszyjnik ze szklanymi koralikami',
     95.00, 'BJ-NAS-001', 23.00, FALSE, 10.00, '2-3 dni roboczych', 'naszyjnik-ze-szklanymi-koralikami', NOW(), NOW(), TRUE)
ON CONFLICT ON CONSTRAINT products_sku_key DO NOTHING;

INSERT INTO product_attribute_values (product_id, category_attribute_id, value, created_at, updated_at, is_active) VALUES
                                                                                                                       (9, 17, 'Szkło Murano', NOW(), NOW(), TRUE),
                                                                                                                       (9, 18, 'Tłoczenie i nawlekanie', NOW(), NOW(), TRUE),
                                                                                                                       (9, 19, 'Turkusowy i złoty', NOW(), NOW(), TRUE),
                                                                                                                       (9, 20, 'Biżuteria', NOW(), NOW(), TRUE),
                                                                                                                       (9, 41, 'Naszyjnik', NOW(), NOW(), TRUE),
                                                                                                                       (9, 42, '45', NOW(), NOW(), TRUE),
                                                                                                                       (9, 43, 'Karabinek', NOW(), NOW(), TRUE),
                                                                                                                       (9, 44, 'false', NOW(), NOW(), TRUE),
                                                                                                                       (9, 1, 'Szkło Murano', NOW(), NOW(), TRUE),
                                                                                                                       (9, 2, 'Tłoczenie i nawlekanie', NOW(), NOW(), TRUE),
                                                                                                                       (9, 3, '45 cm', NOW(), NOW(), TRUE),
                                                                                                                       (9, 4, 'Turkusowy i złoty', NOW(), NOW(), TRUE),
                                                                                                                       (9, 5, 'Ewa Perłowa', NOW(), NOW(), TRUE);

-- 10. Szkło artystyczne - Lampa
INSERT INTO products (id, category_id, name, description, short_description, price, sku, vat_rate, is_featured, shipping_cost, estimated_delivery_time, seo_slug, created_at, updated_at, is_active) VALUES
    (10, 14, 'Lampa artystyczna ze szkła',
     'Wyjątkowa lampa wykonana ze szkła artystycznego. Unikalny design, ciepłe światło. Idealna do salonu lub sypialni. W zestawie źródło światła LED.',
     'Lampa artystyczna ze szkła z LED',
     580.00, 'LG-LMP-001', 23.00, TRUE, 40.00, '4-6 dni roboczych', 'lampa-artystyczna-ze-szkla', NOW(), NOW(), TRUE)
ON CONFLICT ON CONSTRAINT products_sku_key DO NOTHING;

INSERT INTO product_attribute_values (product_id, category_attribute_id, value, created_at, updated_at, is_active) VALUES
                                                                                                                       (10, 17, 'Szkło artystyczne', NOW(), NOW(), TRUE),
                                                                                                                       (10, 18, 'Formowanie i fusing', NOW(), NOW(), TRUE),
                                                                                                                       (10, 19, 'Bursztynowy', NOW(), NOW(), TRUE),
                                                                                                                       (10, 20, 'Oświetlenie', NOW(), NOW(), TRUE),
                                                                                                                       (10, 45, 'Lampa stołowa', NOW(), NOW(), TRUE),
                                                                                                                       (10, 46, 'LED', NOW(), NOW(), TRUE),
                                                                                                                       (10, 47, 'Stały', NOW(), NOW(), TRUE),
                                                                                                                       (10, 1, 'Szkło artystyczne', NOW(), NOW(), TRUE),
                                                                                                                       (10, 2, 'Formowanie i fusing szkła', NOW(), NOW(), TRUE),
                                                                                                                       (10, 3, '40x25 cm', NOW(), NOW(), TRUE),
                                                                                                                       (10, 4, 'Bursztynowy', NOW(), NOW(), TRUE),
                                                                                                                       (10, 5, 'Maciej Świetlny', NOW(), NOW(), TRUE);

-- Update sequences
SELECT pg_catalog.setval('public.products_id_seq', 10, true);
SELECT pg_catalog.setval('public.product_attribute_values_id_seq', (SELECT COALESCE(MAX(id), 0) FROM product_attribute_values), true);
