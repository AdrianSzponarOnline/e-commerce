-- 1. Rzeźba drewniana - Rzeźba anioła
INSERT INTO products (category_id, name, description, short_description, price, sku, vat_rate, is_featured, shipping_cost, estimated_delivery_time, seo_slug, created_at, updated_at, is_active)
VALUES (
           (SELECT id FROM categories WHERE seo_slug = 'rzezby-drewniane'), -- Dynamiczne ID kategorii
           'Rzeźba anioła z drewna dębowego',
           'Unikalna rzeźba anioła wykonana ręcznie z dębu. Wysoka jakość wykonania, elegancka forma. Idealna jako dekoracja wnętrz lub prezent.',
           'Ręcznie wykonana rzeźba anioła z dębu',
           450.00, 'RD-ANG-001', 23.00, TRUE, 25.00, '3-5 dni roboczych', 'rzezba-anioka-z-drewna-debowego', NOW(), NOW(), TRUE
       ) ON CONFLICT (sku) DO NOTHING;

-- Atrybuty dla produktu: RD-ANG-001
INSERT INTO product_attribute_values (product_id, category_attribute_id, value, created_at, updated_at, is_active) VALUES
-- Atrybuty z kategorii 'Rzeźby'
((SELECT id FROM products WHERE sku = 'RD-ANG-001'), (SELECT id FROM category_attributes WHERE name = 'Materiał rzeźby' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'rzezby')), 'Drewno', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'RD-ANG-001'), (SELECT id FROM category_attributes WHERE name = 'Wysokość (cm)' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'rzezby')), '35', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'RD-ANG-001'), (SELECT id FROM category_attributes WHERE name = 'Szerokość (cm)' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'rzezby')), '20', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'RD-ANG-001'), (SELECT id FROM category_attributes WHERE name = 'Głębokość (cm)' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'rzezby')), '15', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'RD-ANG-001'), (SELECT id FROM category_attributes WHERE name = 'Waga (kg)' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'rzezby')), '2.5', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'RD-ANG-001'), (SELECT id FROM category_attributes WHERE name = 'Styl' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'rzezby')), 'Klasyczny', NOW(), NOW(), TRUE),
-- Atrybuty z kategorii 'Rzeźby drewniane'
((SELECT id FROM products WHERE sku = 'RD-ANG-001'), (SELECT id FROM category_attributes WHERE name = 'Rodzaj drewna' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'rzezby-drewniane')), 'Dąb', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'RD-ANG-001'), (SELECT id FROM category_attributes WHERE name = 'Wykończenie' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'rzezby-drewniane')), 'Lakierowana', NOW(), NOW(), TRUE),
-- Atrybuty z kategorii 'Rękodzieło'
((SELECT id FROM products WHERE sku = 'RD-ANG-001'), (SELECT id FROM category_attributes WHERE name = 'Materiał' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'rekodzielo')), 'Drewno dębowe', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'RD-ANG-001'), (SELECT id FROM category_attributes WHERE name = 'Technika wykonania' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'rekodzielo')), 'Ręczne rzeźbienie', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'RD-ANG-001'), (SELECT id FROM category_attributes WHERE name = 'Rozmiar' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'rekodzielo')), '35x20x15 cm', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'RD-ANG-001'), (SELECT id FROM category_attributes WHERE name = 'Kolor' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'rekodzielo')), 'Naturalny kolor drewna', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'RD-ANG-001'), (SELECT id FROM category_attributes WHERE name = 'Artysta/Rzemieślnik' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'rekodzielo')), 'Jan Kowalski', NOW(), NOW(), TRUE)
ON CONFLICT DO NOTHING;


-- 2. Rzeźba kamienna - Lwia głowa
INSERT INTO products (category_id, name, description, short_description, price, sku, vat_rate, is_featured, shipping_cost, estimated_delivery_time, seo_slug, created_at, updated_at, is_active)
VALUES (
           (SELECT id FROM categories WHERE seo_slug = 'rzezby-kamienne'),
           'Rzeźba lwiej głowy z marmuru',
           'Imponująca rzeźba lwiej głowy wykonana z białego marmuru. Precyzyjne detale, polerowana powierzchnia. Doskonała jako element dekoracyjny ogrodu lub wnętrza.',
           'Rzeźba lwiej głowy z białego marmuru',
           1200.00, 'RK-LW-001', 23.00, TRUE, 50.00, '5-7 dni roboczych', 'rzezba-lwiej-glowy-z-marmuru', NOW(), NOW(), TRUE
       ) ON CONFLICT (sku) DO NOTHING;

INSERT INTO product_attribute_values (product_id, category_attribute_id, value, created_at, updated_at, is_active) VALUES
-- Kat: Rzeźby
((SELECT id FROM products WHERE sku = 'RK-LW-001'), (SELECT id FROM category_attributes WHERE name = 'Materiał rzeźby' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'rzezby')), 'Kamień', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'RK-LW-001'), (SELECT id FROM category_attributes WHERE name = 'Wysokość (cm)' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'rzezby')), '45', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'RK-LW-001'), (SELECT id FROM category_attributes WHERE name = 'Szerokość (cm)' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'rzezby')), '35', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'RK-LW-001'), (SELECT id FROM category_attributes WHERE name = 'Głębokość (cm)' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'rzezby')), '25', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'RK-LW-001'), (SELECT id FROM category_attributes WHERE name = 'Waga (kg)' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'rzezby')), '18.5', NOW(), NOW(), TRUE),
-- Kat: Rzeźby kamienne
((SELECT id FROM products WHERE sku = 'RK-LW-001'), (SELECT id FROM category_attributes WHERE name = 'Rodzaj kamienia' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'rzezby-kamienne')), 'Marmur biały', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'RK-LW-001'), (SELECT id FROM category_attributes WHERE name = 'Polerowanie' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'rzezby-kamienne')), 'true', NOW(), NOW(), TRUE),
-- Kat: Rękodzieło
((SELECT id FROM products WHERE sku = 'RK-LW-001'), (SELECT id FROM category_attributes WHERE name = 'Materiał' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'rekodzielo')), 'Marmur biały', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'RK-LW-001'), (SELECT id FROM category_attributes WHERE name = 'Technika wykonania' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'rekodzielo')), 'Rzeźbienie w kamieniu', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'RK-LW-001'), (SELECT id FROM category_attributes WHERE name = 'Artysta/Rzemieślnik' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'rekodzielo')), 'Anna Nowak', NOW(), NOW(), TRUE)
ON CONFLICT DO NOTHING;


-- 3. Rzeźba metalowa - Ptak
INSERT INTO products (category_id, name, description, short_description, price, sku, vat_rate, is_featured, shipping_cost, estimated_delivery_time, seo_slug, created_at, updated_at, is_active)
VALUES (
           (SELECT id FROM categories WHERE seo_slug = 'rzezby-metalowe'),
           'Rzeźba ptaka z brązu',
           'Elegancka rzeźba ptaka wykonana z brązu z patynowaną powierzchnią. Artystyczna forma, idealna do kolekcji sztuki.',
           'Rzeźba ptaka z patynowanego brązu',
           850.00, 'RM-PT-001', 23.00, FALSE, 35.00, '4-6 dni roboczych', 'rzezba-ptaka-z-brazu', NOW(), NOW(), TRUE
       ) ON CONFLICT (sku) DO NOTHING;

INSERT INTO product_attribute_values (product_id, category_attribute_id, value, created_at, updated_at, is_active) VALUES
-- Kat: Rzeźby
((SELECT id FROM products WHERE sku = 'RM-PT-001'), (SELECT id FROM category_attributes WHERE name = 'Materiał rzeźby' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'rzezby')), 'Metal', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'RM-PT-001'), (SELECT id FROM category_attributes WHERE name = 'Wysokość (cm)' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'rzezby')), '30', NOW(), NOW(), TRUE),
-- Kat: Rzeźby metalowe
((SELECT id FROM products WHERE sku = 'RM-PT-001'), (SELECT id FROM category_attributes WHERE name = 'Rodzaj metalu' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'rzezby-metalowe')), 'Brąz', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'RM-PT-001'), (SELECT id FROM category_attributes WHERE name = 'Patynowanie' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'rzezby-metalowe')), 'true', NOW(), NOW(), TRUE),
-- Kat: Rękodzieło
((SELECT id FROM products WHERE sku = 'RM-PT-001'), (SELECT id FROM category_attributes WHERE name = 'Materiał' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'rekodzielo')), 'Brąz', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'RM-PT-001'), (SELECT id FROM category_attributes WHERE name = 'Artysta/Rzemieślnik' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'rekodzielo')), 'Piotr Wiśniewski', NOW(), NOW(), TRUE)
ON CONFLICT DO NOTHING;


-- 4. Obraz olejny - Pejzaż
INSERT INTO products (category_id, name, description, short_description, price, sku, vat_rate, is_featured, shipping_cost, estimated_delivery_time, seo_slug, created_at, updated_at, is_active)
VALUES (
           (SELECT id FROM categories WHERE seo_slug = 'obrazy-olejne'),
           'Pejzaż górski - obraz olejny',
           'Piękny obraz olejny przedstawiający górski pejzaż. Namalowany na płótnie bawełnianym farbami olejnymi. Oprawiony w elegancką ramę. Z certyfikatem autentyczności.',
           'Obraz olejny - pejzaż górski na płótnie',
           650.00, 'OB-PEJ-001', 23.00, TRUE, 30.00, '2-3 dni roboczych', 'pejzaz-gorski-obraz-olejny', NOW(), NOW(), TRUE
       ) ON CONFLICT (sku) DO NOTHING;

INSERT INTO product_attribute_values (product_id, category_attribute_id, value, created_at, updated_at, is_active) VALUES
-- Kat: Obrazy
((SELECT id FROM products WHERE sku = 'OB-PEJ-001'), (SELECT id FROM category_attributes WHERE name = 'Technika malarska' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'obrazy')), 'Olejna', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'OB-PEJ-001'), (SELECT id FROM category_attributes WHERE name = 'Wymiary (cm)' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'obrazy')), '50x70 cm', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'OB-PEJ-001'), (SELECT id FROM category_attributes WHERE name = 'Rama' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'obrazy')), 'true', NOW(), NOW(), TRUE),
-- Kat: Obrazy olejne
((SELECT id FROM products WHERE sku = 'OB-PEJ-001'), (SELECT id FROM category_attributes WHERE name = 'Rodzaj farb' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'obrazy-olejne')), 'Farba olejna', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'OB-PEJ-001'), (SELECT id FROM category_attributes WHERE name = 'Certyfikat autentyczności' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'obrazy-olejne')), 'true', NOW(), NOW(), TRUE),
-- Kat: Rękodzieło
((SELECT id FROM products WHERE sku = 'OB-PEJ-001'), (SELECT id FROM category_attributes WHERE name = 'Technika wykonania' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'rekodzielo')), 'Malarstwo olejne', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'OB-PEJ-001'), (SELECT id FROM category_attributes WHERE name = 'Artysta/Rzemieślnik' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'rekodzielo')), 'Maria Zielińska', NOW(), NOW(), TRUE)
ON CONFLICT DO NOTHING;


-- 5. Akwarela - Portret
INSERT INTO products (category_id, name, description, short_description, price, sku, vat_rate, is_featured, shipping_cost, estimated_delivery_time, seo_slug, created_at, updated_at, is_active)
VALUES (
           (SELECT id FROM categories WHERE seo_slug = 'akwarele'),
           'Portret akwarelowy',
           'Delikatny portret wykonany techniką akwarelową na wysokiej jakości papierze. Pastelowe kolory, subtelne przejścia. Idealny prezent.',
           'Portret akwarelowy na papierze',
           320.00, 'AK-POR-001', 23.00, FALSE, 20.00, '2-3 dni roboczych', 'portret-akwarelowy', NOW(), NOW(), TRUE
       ) ON CONFLICT (sku) DO NOTHING;

INSERT INTO product_attribute_values (product_id, category_attribute_id, value, created_at, updated_at, is_active) VALUES
-- Kat: Obrazy
((SELECT id FROM products WHERE sku = 'AK-POR-001'), (SELECT id FROM category_attributes WHERE name = 'Technika malarska' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'obrazy')), 'Akwarelowa', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'AK-POR-001'), (SELECT id FROM category_attributes WHERE name = 'Rodzaj podłoża' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'obrazy')), 'Papier akwarelowy', NOW(), NOW(), TRUE),
-- Kat: Akwarele
((SELECT id FROM products WHERE sku = 'AK-POR-001'), (SELECT id FROM category_attributes WHERE name = 'Rodzaj papieru' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'akwarele')), 'Papier Canson 300g', NOW(), NOW(), TRUE),
-- Kat: Rękodzieło
((SELECT id FROM products WHERE sku = 'AK-POR-001'), (SELECT id FROM category_attributes WHERE name = 'Artysta/Rzemieślnik' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'rekodzielo')), 'Tomasz Kowalczyk', NOW(), NOW(), TRUE)
ON CONFLICT DO NOTHING;


-- 6. Grafika artystyczna - Linoryt
INSERT INTO products (category_id, name, description, short_description, price, sku, vat_rate, is_featured, shipping_cost, estimated_delivery_time, seo_slug, created_at, updated_at, is_active)
VALUES (
           (SELECT id FROM categories WHERE seo_slug = 'grafika-artystyczna'),
           'Linoryt - Motywy roślinne',
           'Oryginalny linoryt przedstawiający motywy roślinne. Wykonany techniką druku wypukłego. Numerowany egzemplarz z serii limitowanej.',
           'Linoryt - motywy roślinne, numerowany',
           180.00, 'GR-LIN-001', 23.00, FALSE, 15.00, '2-4 dni roboczych', 'linoryt-motywy-roslinne', NOW(), NOW(), TRUE
       ) ON CONFLICT (sku) DO NOTHING;

INSERT INTO product_attribute_values (product_id, category_attribute_id, value, created_at, updated_at, is_active) VALUES
-- Kat: Grafika
((SELECT id FROM products WHERE sku = 'GR-LIN-001'), (SELECT id FROM category_attributes WHERE name = 'Technika graficzna' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'grafika-artystyczna')), 'Linoryt', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'GR-LIN-001'), (SELECT id FROM category_attributes WHERE name = 'Numeracja' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'grafika-artystyczna')), 'true', NOW(), NOW(), TRUE),
-- Kat: Rękodzieło
((SELECT id FROM products WHERE sku = 'GR-LIN-001'), (SELECT id FROM category_attributes WHERE name = 'Technika wykonania' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'rekodzielo')), 'Druk wypukły', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'GR-LIN-001'), (SELECT id FROM category_attributes WHERE name = 'Artysta/Rzemieślnik' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'rekodzielo')), 'Katarzyna Lewandowska', NOW(), NOW(), TRUE)
ON CONFLICT DO NOTHING;


-- 7. Wazon szklany - Wysoki
INSERT INTO products (category_id, name, description, short_description, price, sku, vat_rate, is_featured, shipping_cost, estimated_delivery_time, seo_slug, created_at, updated_at, is_active)
VALUES (
           (SELECT id FROM categories WHERE seo_slug = 'wazony-szklane'),
           'Wysoki wazon szklany ręcznie formowany',
           'Elegancki wysoki wazon wykonany ze szkła ręcznie formowanego. Unikalny kształt, kolorowe szkło. Idealny do kwiatów lub jako dekoracja.',
           'Wysoki wazon ręcznie formowany ze szkła',
           280.00, 'WZ-WYS-001', 23.00, FALSE, 25.00, '3-5 dni roboczych', 'wysoki-wazon-szklany-recznie-formowany', NOW(), NOW(), TRUE
       ) ON CONFLICT (sku) DO NOTHING;

INSERT INTO product_attribute_values (product_id, category_attribute_id, value, created_at, updated_at, is_active) VALUES
-- Kat: Wyroby ze szkła
((SELECT id FROM products WHERE sku = 'WZ-WYS-001'), (SELECT id FROM category_attributes WHERE name = 'Rodzaj szkła' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'wyroby-ze-szkla')), 'Szkło sodowe', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'WZ-WYS-001'), (SELECT id FROM category_attributes WHERE name = 'Technika wykonania' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'wyroby-ze-szkla')), 'Formowanie ręczne', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'WZ-WYS-001'), (SELECT id FROM category_attributes WHERE name = 'Kolor szkła' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'wyroby-ze-szkla')), 'Błękitny', NOW(), NOW(), TRUE),
-- Kat: Wazony szklane
((SELECT id FROM products WHERE sku = 'WZ-WYS-001'), (SELECT id FROM category_attributes WHERE name = 'Pojemność (ml)' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'wazony-szklane')), '1500', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'WZ-WYS-001'), (SELECT id FROM category_attributes WHERE name = 'Kształt' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'wazony-szklane')), 'Wysoki, smukły', NOW(), NOW(), TRUE),
-- Kat: Rękodzieło
((SELECT id FROM products WHERE sku = 'WZ-WYS-001'), (SELECT id FROM category_attributes WHERE name = 'Artysta/Rzemieślnik' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'rekodzielo')), 'Robert Szklarz', NOW(), NOW(), TRUE)
ON CONFLICT DO NOTHING;


-- 8. Figurka szklana - Ptak
INSERT INTO products (category_id, name, description, short_description, price, sku, vat_rate, is_featured, shipping_cost, estimated_delivery_time, seo_slug, created_at, updated_at, is_active)
VALUES (
           (SELECT id FROM categories WHERE seo_slug = 'figurki-szklane'),
           'Figurka ptaka ze szkła artystycznego',
           'Delikatna figurka ptaka wykonana ze szkła artystycznego. Precyzyjne detale, żywe kolory. W zestawie podstawka. Unikalny egzemplarz.',
           'Figurka ptaka ze szkła z podstawką',
           150.00, 'FG-PT-001', 23.00, FALSE, 20.00, '2-3 dni roboczych', 'figurka-ptaka-ze-szkla-artystycznego', NOW(), NOW(), TRUE
       ) ON CONFLICT (sku) DO NOTHING;

INSERT INTO product_attribute_values (product_id, category_attribute_id, value, created_at, updated_at, is_active) VALUES
-- Kat: Wyroby ze szkła
((SELECT id FROM products WHERE sku = 'FG-PT-001'), (SELECT id FROM category_attributes WHERE name = 'Rodzaj szkła' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'wyroby-ze-szkla')), 'Szkło kryształowe', NOW(), NOW(), TRUE),
-- Kat: Figurki szklane
((SELECT id FROM products WHERE sku = 'FG-PT-001'), (SELECT id FROM category_attributes WHERE name = 'Motyw' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'figurki-szklane')), 'Ptak', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'FG-PT-001'), (SELECT id FROM category_attributes WHERE name = 'Podstawa' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'figurki-szklane')), 'true', NOW(), NOW(), TRUE),
-- Kat: Rękodzieło
((SELECT id FROM products WHERE sku = 'FG-PT-001'), (SELECT id FROM category_attributes WHERE name = 'Artysta/Rzemieślnik' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'rekodzielo')), 'Agnieszka Srebrna', NOW(), NOW(), TRUE)
ON CONFLICT DO NOTHING;


-- 9. Biżuteria szklana - Naszyjnik
INSERT INTO products (category_id, name, description, short_description, price, sku, vat_rate, is_featured, shipping_cost, estimated_delivery_time, seo_slug, created_at, updated_at, is_active)
VALUES (
           (SELECT id FROM categories WHERE seo_slug = 'bizuteria-szklana'),
           'Naszyjnik ze szklanymi koralikami',
           'Elegancki naszyjnik wykonany ręcznie ze szklanych koralików. Unikalna kolorystyka, precyzyjne wykonanie. W zestawie zapięcie. Idealny na prezent.',
           'Naszyjnik ze szklanymi koralikami',
           95.00, 'BJ-NAS-001', 23.00, FALSE, 10.00, '2-3 dni roboczych', 'naszyjnik-ze-szklanymi-koralikami', NOW(), NOW(), TRUE
       ) ON CONFLICT (sku) DO NOTHING;

INSERT INTO product_attribute_values (product_id, category_attribute_id, value, created_at, updated_at, is_active) VALUES
-- Kat: Wyroby ze szkła
((SELECT id FROM products WHERE sku = 'BJ-NAS-001'), (SELECT id FROM category_attributes WHERE name = 'Rodzaj szkła' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'wyroby-ze-szkla')), 'Szkło Murano', NOW(), NOW(), TRUE),
-- Kat: Biżuteria szklana
((SELECT id FROM products WHERE sku = 'BJ-NAS-001'), (SELECT id FROM category_attributes WHERE name = 'Rodzaj biżuterii' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'bizuteria-szklana')), 'Naszyjnik', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'BJ-NAS-001'), (SELECT id FROM category_attributes WHERE name = 'Długość (cm)' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'bizuteria-szklana')), '45', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'BJ-NAS-001'), (SELECT id FROM category_attributes WHERE name = 'Zapięcie' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'bizuteria-szklana')), 'Karabinek', NOW(), NOW(), TRUE),
-- Kat: Rękodzieło
((SELECT id FROM products WHERE sku = 'BJ-NAS-001'), (SELECT id FROM category_attributes WHERE name = 'Artysta/Rzemieślnik' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'rekodzielo')), 'Ewa Perłowa', NOW(), NOW(), TRUE)
ON CONFLICT DO NOTHING;


-- 10. Szkło artystyczne - Lampa
INSERT INTO products (category_id, name, description, short_description, price, sku, vat_rate, is_featured, shipping_cost, estimated_delivery_time, seo_slug, created_at, updated_at, is_active)
VALUES (
           (SELECT id FROM categories WHERE seo_slug = 'szklo-artystyczne'),
           'Lampa artystyczna ze szkła',
           'Wyjątkowa lampa wykonana ze szkła artystycznego. Unikalny design, ciepłe światło. Idealna do salonu lub sypialni. W zestawie źródło światła LED.',
           'Lampa artystyczna ze szkła z LED',
           580.00, 'LG-LMP-001', 23.00, TRUE, 40.00, '4-6 dni roboczych', 'lampa-artystyczna-ze-szkla', NOW(), NOW(), TRUE
       ) ON CONFLICT (sku) DO NOTHING;

INSERT INTO product_attribute_values (product_id, category_attribute_id, value, created_at, updated_at, is_active) VALUES
-- Kat: Wyroby ze szkła
((SELECT id FROM products WHERE sku = 'LG-LMP-001'), (SELECT id FROM category_attributes WHERE name = 'Rodzaj szkła' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'wyroby-ze-szkla')), 'Szkło artystyczne', NOW(), NOW(), TRUE),
-- Kat: Szkło artystyczne
((SELECT id FROM products WHERE sku = 'LG-LMP-001'), (SELECT id FROM category_attributes WHERE name = 'Funkcjonalność' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'szklo-artystyczne')), 'Lampa stołowa', NOW(), NOW(), TRUE),
((SELECT id FROM products WHERE sku = 'LG-LMP-001'), (SELECT id FROM category_attributes WHERE name = 'Źródło światła' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'szklo-artystyczne')), 'LED', NOW(), NOW(), TRUE),
-- Kat: Rękodzieło
((SELECT id FROM products WHERE sku = 'LG-LMP-001'), (SELECT id FROM category_attributes WHERE name = 'Artysta/Rzemieślnik' AND category_id = (SELECT id FROM categories WHERE seo_slug = 'rekodzielo')), 'Maciej Świetlny', NOW(), NOW(), TRUE)
ON CONFLICT DO NOTHING;