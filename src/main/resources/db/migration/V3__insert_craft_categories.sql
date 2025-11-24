
INSERT INTO categories (name, parent_id, description, seo_slug, created_at, updated_at, is_active)
VALUES ('Rękodzieło', NULL, 'Unikalne wyroby ręcznie wykonane przez artystów i rzemieślników', 'rekodzielo', NOW(), NOW(), TRUE);


INSERT INTO categories (name, parent_id, description, seo_slug, created_at, updated_at, is_active)
VALUES
    ('Rzeźby', (SELECT id FROM categories WHERE seo_slug = 'rekodzielo'), 'Rzeźby wykonane z różnych materiałów - drewno, kamień, metal, glina', 'rzezby', NOW(), NOW(), TRUE),
    ('Obrazy', (SELECT id FROM categories WHERE seo_slug = 'rekodzielo'), 'Malarstwo i grafika artystyczna - obrazy olejne, akwarele, rysunki', 'obrazy', NOW(), NOW(), TRUE),
    ('Wyroby ze szkła', (SELECT id FROM categories WHERE seo_slug = 'rekodzielo'), 'Przedmioty wykonane ze szkła - wazony, figurki, biżuteria, dekoracje', 'wyroby-ze-szkla', NOW(), NOW(), TRUE);


INSERT INTO categories (name, parent_id, description, seo_slug, created_at, updated_at, is_active)
VALUES

('Rzeźby drewniane', (SELECT id FROM categories WHERE seo_slug = 'rzezby'), 'Rzeźby wykonane z drewna - figurki, maski, dekoracje', 'rzezby-drewniane', NOW(), NOW(), TRUE),
('Rzeźby kamienne', (SELECT id FROM categories WHERE seo_slug = 'rzezby'), 'Rzeźby wykonane z kamienia - marmur, granit, piaskowiec', 'rzezby-kamienne', NOW(), NOW(), TRUE),
('Rzeźby metalowe', (SELECT id FROM categories WHERE seo_slug = 'rzezby'), 'Rzeźby wykonane z metalu - brąz, miedź, żelazo', 'rzezby-metalowe', NOW(), NOW(), TRUE),


('Obrazy olejne', (SELECT id FROM categories WHERE seo_slug = 'obrazy'), 'Obrazy malowane farbami olejnymi na płótnie', 'obrazy-olejne', NOW(), NOW(), TRUE),
('Akwarele', (SELECT id FROM categories WHERE seo_slug = 'obrazy'), 'Obrazy malowane techniką akwarelową', 'akwarele', NOW(), NOW(), TRUE),
('Grafika artystyczna', (SELECT id FROM categories WHERE seo_slug = 'obrazy'), 'Rysunki, szkice, grafiki wykonane różnymi technikami', 'grafika-artystyczna', NOW(), NOW(), TRUE),


('Wazony szklane', (SELECT id FROM categories WHERE seo_slug = 'wyroby-ze-szkla'), 'Wazony i naczynia wykonane ze szkła', 'wazony-szklane', NOW(), NOW(), TRUE),
('Figurki szklane', (SELECT id FROM categories WHERE seo_slug = 'wyroby-ze-szkla'), 'Figurki i dekoracje wykonane ze szkła', 'figurki-szklane', NOW(), NOW(), TRUE),
('Biżuteria szklana', (SELECT id FROM categories WHERE seo_slug = 'wyroby-ze-szkla'), 'Biżuteria wykonana ze szkła - naszyjniki, kolczyki, bransoletki', 'bizuteria-szklana', NOW(), NOW(), TRUE),
('Szkło artystyczne', (SELECT id FROM categories WHERE seo_slug = 'wyroby-ze-szkla'), 'Unikalne wyroby ze szkła artystycznego - lampy, dekoracje', 'szklo-artystyczne', NOW(), NOW(), TRUE);



INSERT INTO category_attributes (category_id, name, type, created_at, updated_at, is_active) VALUES
                                                                                                 ((SELECT id FROM categories WHERE seo_slug = 'rekodzielo'), 'Materiał', 'TEXT', NOW(), NOW(), TRUE),
                                                                                                 ((SELECT id FROM categories WHERE seo_slug = 'rekodzielo'), 'Technika wykonania', 'TEXT', NOW(), NOW(), TRUE),
                                                                                                 ((SELECT id FROM categories WHERE seo_slug = 'rekodzielo'), 'Rozmiar', 'TEXT', NOW(), NOW(), TRUE),
                                                                                                 ((SELECT id FROM categories WHERE seo_slug = 'rekodzielo'), 'Kolor', 'TEXT', NOW(), NOW(), TRUE),
                                                                                                 ((SELECT id FROM categories WHERE seo_slug = 'rekodzielo'), 'Artysta/Rzemieślnik', 'TEXT', NOW(), NOW(), TRUE);

-- Atrybuty dla 'Rzeźby'
INSERT INTO category_attributes (category_id, name, type, created_at, updated_at, is_active) VALUES
                                                                                                 ((SELECT id FROM categories WHERE seo_slug = 'rzezby'), 'Materiał rzeźby', 'TEXT', NOW(), NOW(), TRUE),
                                                                                                 ((SELECT id FROM categories WHERE seo_slug = 'rzezby'), 'Wysokość (cm)', 'NUMBER', NOW(), NOW(), TRUE),
                                                                                                 ((SELECT id FROM categories WHERE seo_slug = 'rzezby'), 'Szerokość (cm)', 'NUMBER', NOW(), NOW(), TRUE),
                                                                                                 ((SELECT id FROM categories WHERE seo_slug = 'rzezby'), 'Głębokość (cm)', 'NUMBER', NOW(), NOW(), TRUE),
                                                                                                 ((SELECT id FROM categories WHERE seo_slug = 'rzezby'), 'Waga (kg)', 'NUMBER', NOW(), NOW(), TRUE),
                                                                                                 ((SELECT id FROM categories WHERE seo_slug = 'rzezby'), 'Styl', 'TEXT', NOW(), NOW(), TRUE);

INSERT INTO category_attributes (category_id, name, type, created_at, updated_at, is_active) VALUES
                                                                                                 ((SELECT id FROM categories WHERE seo_slug = 'obrazy'), 'Technika malarska', 'TEXT', NOW(), NOW(), TRUE),
                                                                                                 ((SELECT id FROM categories WHERE seo_slug = 'obrazy'), 'Wymiary (cm)', 'TEXT', NOW(), NOW(), TRUE),
                                                                                                 ((SELECT id FROM categories WHERE seo_slug = 'obrazy'), 'Rodzaj podłoża', 'TEXT', NOW(), NOW(), TRUE),
                                                                                                 ((SELECT id FROM categories WHERE seo_slug = 'obrazy'), 'Rama', 'BOOLEAN', NOW(), NOW(), TRUE),
                                                                                                 ((SELECT id FROM categories WHERE seo_slug = 'obrazy'), 'Rok powstania', 'NUMBER', NOW(), NOW(), TRUE);

INSERT INTO category_attributes (category_id, name, type, created_at, updated_at, is_active) VALUES
                                                                                                 ((SELECT id FROM categories WHERE seo_slug = 'wyroby-ze-szkla'), 'Rodzaj szkła', 'TEXT', NOW(), NOW(), TRUE),
                                                                                                 ((SELECT id FROM categories WHERE seo_slug = 'wyroby-ze-szkla'), 'Technika wykonania', 'TEXT', NOW(), NOW(), TRUE),
                                                                                                 ((SELECT id FROM categories WHERE seo_slug = 'wyroby-ze-szkla'), 'Kolor szkła', 'TEXT', NOW(), NOW(), TRUE),
                                                                                                 ((SELECT id FROM categories WHERE seo_slug = 'wyroby-ze-szkla'), 'Przeznaczenie', 'TEXT', NOW(), NOW(), TRUE);

INSERT INTO category_attributes (category_id, name, type, created_at, updated_at, is_active) VALUES
                                                                                                 ((SELECT id FROM categories WHERE seo_slug = 'rzezby-drewniane'), 'Rodzaj drewna', 'TEXT', NOW(), NOW(), TRUE),
                                                                                                 ((SELECT id FROM categories WHERE seo_slug = 'rzezby-drewniane'), 'Wykończenie', 'TEXT', NOW(), NOW(), TRUE);

INSERT INTO category_attributes (category_id, name, type, created_at, updated_at, is_active) VALUES
                                                                                                 ((SELECT id FROM categories WHERE seo_slug = 'rzezby-kamienne'), 'Rodzaj kamienia', 'TEXT', NOW(), NOW(), TRUE),
                                                                                                 ((SELECT id FROM categories WHERE seo_slug = 'rzezby-kamienne'), 'Polerowanie', 'BOOLEAN', NOW(), NOW(), TRUE);

-- Atrybuty dla 'Rzeźby metalowe'
INSERT INTO category_attributes (category_id, name, type, created_at, updated_at, is_active) VALUES
                                                                                                 ((SELECT id FROM categories WHERE seo_slug = 'rzezby-metalowe'), 'Rodzaj metalu', 'TEXT', NOW(), NOW(), TRUE),
                                                                                                 ((SELECT id FROM categories WHERE seo_slug = 'rzezby-metalowe'), 'Patynowanie', 'BOOLEAN', NOW(), NOW(), TRUE);

-- Atrybuty dla 'Obrazy olejne'
INSERT INTO category_attributes (category_id, name, type, created_at, updated_at, is_active) VALUES
                                                                                                 ((SELECT id FROM categories WHERE seo_slug = 'obrazy-olejne'), 'Rodzaj farb', 'TEXT', NOW(), NOW(), TRUE),
                                                                                                 ((SELECT id FROM categories WHERE seo_slug = 'obrazy-olejne'), 'Rodzaj płótna', 'TEXT', NOW(), NOW(), TRUE),
                                                                                                 ((SELECT id FROM categories WHERE seo_slug = 'obrazy-olejne'), 'Certyfikat autentyczności', 'BOOLEAN', NOW(), NOW(), TRUE);

-- Atrybuty dla 'Akwarele'
INSERT INTO category_attributes (category_id, name, type, created_at, updated_at, is_active) VALUES
                                                                                                 ((SELECT id FROM categories WHERE seo_slug = 'akwarele'), 'Rodzaj papieru', 'TEXT', NOW(), NOW(), TRUE),
                                                                                                 ((SELECT id FROM categories WHERE seo_slug = 'akwarele'), 'Technika akwarelowa', 'TEXT', NOW(), NOW(), TRUE);

-- Atrybuty dla 'Grafika artystyczna'
INSERT INTO category_attributes (category_id, name, type, created_at, updated_at, is_active) VALUES
                                                                                                 ((SELECT id FROM categories WHERE seo_slug = 'grafika-artystyczna'), 'Technika graficzna', 'TEXT', NOW(), NOW(), TRUE),
                                                                                                 ((SELECT id FROM categories WHERE seo_slug = 'grafika-artystyczna'), 'Rodzaj papieru', 'TEXT', NOW(), NOW(), TRUE),
                                                                                                 ((SELECT id FROM categories WHERE seo_slug = 'grafika-artystyczna'), 'Numeracja', 'BOOLEAN', NOW(), NOW(), TRUE);

-- Atrybuty dla 'Wazony szklane'
INSERT INTO category_attributes (category_id, name, type, created_at, updated_at, is_active) VALUES
                                                                                                 ((SELECT id FROM categories WHERE seo_slug = 'wazony-szklane'), 'Pojemność (ml)', 'NUMBER', NOW(), NOW(), TRUE),
                                                                                                 ((SELECT id FROM categories WHERE seo_slug = 'wazony-szklane'), 'Kształt', 'TEXT', NOW(), NOW(), TRUE),
                                                                                                 ((SELECT id FROM categories WHERE seo_slug = 'wazony-szklane'), 'Przeznaczenie', 'TEXT', NOW(), NOW(), TRUE);

-- Atrybuty dla 'Figurki szklane'
INSERT INTO category_attributes (category_id, name, type, created_at, updated_at, is_active) VALUES
                                                                                                 ((SELECT id FROM categories WHERE seo_slug = 'figurki-szklane'), 'Motyw', 'TEXT', NOW(), NOW(), TRUE),
                                                                                                 ((SELECT id FROM categories WHERE seo_slug = 'figurki-szklane'), 'Wysokość (cm)', 'NUMBER', NOW(), NOW(), TRUE),
                                                                                                 ((SELECT id FROM categories WHERE seo_slug = 'figurki-szklane'), 'Podstawa', 'BOOLEAN', NOW(), NOW(), TRUE);

-- Atrybuty dla 'Biżuteria szklana'
INSERT INTO category_attributes (category_id, name, type, created_at, updated_at, is_active) VALUES
                                                                                                 ((SELECT id FROM categories WHERE seo_slug = 'bizuteria-szklana'), 'Rodzaj biżuterii', 'TEXT', NOW(), NOW(), TRUE),
                                                                                                 ((SELECT id FROM categories WHERE seo_slug = 'bizuteria-szklana'), 'Długość (cm)', 'NUMBER', NOW(), NOW(), TRUE),
                                                                                                 ((SELECT id FROM categories WHERE seo_slug = 'bizuteria-szklana'), 'Zapięcie', 'TEXT', NOW(), NOW(), TRUE),
                                                                                                 ((SELECT id FROM categories WHERE seo_slug = 'bizuteria-szklana'), 'Zestaw', 'BOOLEAN', NOW(), NOW(), TRUE);

-- Atrybuty dla 'Szkło artystyczne'
INSERT INTO category_attributes (category_id, name, type, created_at, updated_at, is_active) VALUES
                                                                                                 ((SELECT id FROM categories WHERE seo_slug = 'szklo-artystyczne'), 'Funkcjonalność', 'TEXT', NOW(), NOW(), TRUE),
                                                                                                 ((SELECT id FROM categories WHERE seo_slug = 'szklo-artystyczne'), 'Źródło światła', 'TEXT', NOW(), NOW(), TRUE),
                                                                                                 ((SELECT id FROM categories WHERE seo_slug = 'szklo-artystyczne'), 'Montaż', 'TEXT', NOW(), NOW(), TRUE);

SELECT setval('categories_id_seq', (SELECT MAX(id) FROM categories));