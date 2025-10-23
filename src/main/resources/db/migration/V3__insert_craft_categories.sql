-- Insert craft categories and attributes

-- Insert main categories
INSERT INTO categories (id, name, parent_id, description, seo_slug, created_at, updated_at, is_active) VALUES
(1, 'Rękodzieło', NULL, 'Unikalne wyroby ręcznie wykonane przez artystów i rzemieślników', 'rekodzielo', NOW(), NOW(), TRUE),
(2, 'Rzeźby', 1, 'Rzeźby wykonane z różnych materiałów - drewno, kamień, metal, glina', 'rzeźby', NOW(), NOW(), TRUE),
(3, 'Obrazy', 1, 'Malarstwo i grafika artystyczna - obrazy olejne, akwarele, rysunki', 'obrazy', NOW(), NOW(), TRUE),
(4, 'Wyroby ze szkła', 1, 'Przedmioty wykonane ze szkła - wazony, figurki, biżuteria, dekoracje', 'wyroby-ze-szkla', NOW(), NOW(), TRUE);

-- Insert subcategories
INSERT INTO categories (id, name, parent_id, description, seo_slug, created_at, updated_at, is_active) VALUES
(5, 'Rzeźby drewniane', 2, 'Rzeźby wykonane z drewna - figurki, maski, dekoracje', 'rzeźby-drewniane', NOW(), NOW(), TRUE),
(6, 'Rzeźby kamienne', 2, 'Rzeźby wykonane z kamienia - marmur, granit, piaskowiec', 'rzeźby-kamienne', NOW(), NOW(), TRUE),
(7, 'Rzeźby metalowe', 2, 'Rzeźby wykonane z metalu - brąz, miedź, żelazo', 'rzeźby-metalowe', NOW(), NOW(), TRUE),
(8, 'Obrazy olejne', 3, 'Obrazy malowane farbami olejnymi na płótnie', 'obrazy-olejne', NOW(), NOW(), TRUE),
(9, 'Akwarele', 3, 'Obrazy malowane techniką akwarelową', 'akwarele', NOW(), NOW(), TRUE),
(10, 'Grafika artystyczna', 3, 'Rysunki, szkice, grafiki wykonane różnymi technikami', 'grafika-artystyczna', NOW(), NOW(), TRUE),
(11, 'Wazony szklane', 4, 'Wazony i naczynia wykonane ze szkła', 'wazony-szklane', NOW(), NOW(), TRUE),
(12, 'Figurki szklane', 4, 'Figurki i dekoracje wykonane ze szkła', 'figurki-szklane', NOW(), NOW(), TRUE),
(13, 'Biżuteria szklana', 4, 'Biżuteria wykonana ze szkła - naszyjniki, kolczyki, bransoletki', 'bizuteria-szklana', NOW(), NOW(), TRUE),
(14, 'Szkło artystyczne', 4, 'Unikalne wyroby ze szkła artystycznego - lampy, dekoracje', 'szklo-artystyczne', NOW(), NOW(), TRUE);

-- Update sequences
SELECT setval('categories_id_seq', 14, true);

-- Insert category attributes for main category
INSERT INTO category_attributes (id, category_id, name, type, created_at, updated_at, is_active) VALUES
(1, 1, 'Materiał', 'TEXT', NOW(), NOW(), TRUE),
(2, 1, 'Technika wykonania', 'TEXT', NOW(), NOW(), TRUE),
(3, 1, 'Rozmiar', 'TEXT', NOW(), NOW(), TRUE),
(4, 1, 'Kolor', 'TEXT', NOW(), NOW(), TRUE),
(5, 1, 'Artysta/Rzemieślnik', 'TEXT', NOW(), NOW(), TRUE);

-- Insert category attributes for sculptures
INSERT INTO category_attributes (id, category_id, name, type, created_at, updated_at, is_active) VALUES
(6, 2, 'Materiał rzeźby', 'TEXT', NOW(), NOW(), TRUE),
(7, 2, 'Wysokość (cm)', 'NUMBER', NOW(), NOW(), TRUE),
(8, 2, 'Szerokość (cm)', 'NUMBER', NOW(), NOW(), TRUE),
(9, 2, 'Głębokość (cm)', 'NUMBER', NOW(), NOW(), TRUE),
(10, 2, 'Waga (kg)', 'NUMBER', NOW(), NOW(), TRUE),
(11, 2, 'Styl', 'TEXT', NOW(), NOW(), TRUE);

-- Insert category attributes for paintings
INSERT INTO category_attributes (id, category_id, name, type, created_at, updated_at, is_active) VALUES
(12, 3, 'Technika malarska', 'TEXT', NOW(), NOW(), TRUE),
(13, 3, 'Wymiary (cm)', 'TEXT', NOW(), NOW(), TRUE),
(14, 3, 'Rodzaj podłoża', 'TEXT', NOW(), NOW(), TRUE),
(15, 3, 'Rama', 'BOOLEAN', NOW(), NOW(), TRUE),
(16, 3, 'Rok powstania', 'NUMBER', NOW(), NOW(), TRUE);

-- Insert category attributes for glass products
INSERT INTO category_attributes (id, category_id, name, type, created_at, updated_at, is_active) VALUES
(17, 4, 'Rodzaj szkła', 'TEXT', NOW(), NOW(), TRUE),
(18, 4, 'Technika wykonania', 'TEXT', NOW(), NOW(), TRUE),
(19, 4, 'Kolor szkła', 'TEXT', NOW(), NOW(), TRUE),
(20, 4, 'Przeznaczenie', 'TEXT', NOW(), NOW(), TRUE);

-- Insert specific attributes for wooden sculptures
INSERT INTO category_attributes (id, category_id, name, type, created_at, updated_at, is_active) VALUES
(21, 5, 'Rodzaj drewna', 'TEXT', NOW(), NOW(), TRUE),
(22, 5, 'Wykończenie', 'TEXT', NOW(), NOW(), TRUE);

-- Insert specific attributes for stone sculptures
INSERT INTO category_attributes (id, category_id, name, type, created_at, updated_at, is_active) VALUES
(23, 6, 'Rodzaj kamienia', 'TEXT', NOW(), NOW(), TRUE),
(24, 6, 'Polerowanie', 'BOOLEAN', NOW(), NOW(), TRUE);

-- Insert specific attributes for metal sculptures
INSERT INTO category_attributes (id, category_id, name, type, created_at, updated_at, is_active) VALUES
(25, 7, 'Rodzaj metalu', 'TEXT', NOW(), NOW(), TRUE),
(26, 7, 'Patynowanie', 'BOOLEAN', NOW(), NOW(), TRUE);

-- Insert specific attributes for oil paintings
INSERT INTO category_attributes (id, category_id, name, type, created_at, updated_at, is_active) VALUES
(27, 8, 'Rodzaj farb', 'TEXT', NOW(), NOW(), TRUE),
(28, 8, 'Rodzaj płótna', 'TEXT', NOW(), NOW(), TRUE),
(29, 8, 'Certyfikat autentyczności', 'BOOLEAN', NOW(), NOW(), TRUE);

-- Insert specific attributes for watercolors
INSERT INTO category_attributes (id, category_id, name, type, created_at, updated_at, is_active) VALUES
(30, 9, 'Rodzaj papieru', 'TEXT', NOW(), NOW(), TRUE),
(31, 9, 'Technika akwarelowa', 'TEXT', NOW(), NOW(), TRUE);

-- Insert specific attributes for graphic art
INSERT INTO category_attributes (id, category_id, name, type, created_at, updated_at, is_active) VALUES
(32, 10, 'Technika graficzna', 'TEXT', NOW(), NOW(), TRUE),
(33, 10, 'Rodzaj papieru', 'TEXT', NOW(), NOW(), TRUE),
(34, 10, 'Numeracja', 'BOOLEAN', NOW(), NOW(), TRUE);

-- Insert specific attributes for glass vases
INSERT INTO category_attributes (id, category_id, name, type, created_at, updated_at, is_active) VALUES
(35, 11, 'Pojemność (ml)', 'NUMBER', NOW(), NOW(), TRUE),
(36, 11, 'Kształt', 'TEXT', NOW(), NOW(), TRUE),
(37, 11, 'Przeznaczenie', 'TEXT', NOW(), NOW(), TRUE);

-- Insert specific attributes for glass figurines
INSERT INTO category_attributes (id, category_id, name, type, created_at, updated_at, is_active) VALUES
(38, 12, 'Motyw', 'TEXT', NOW(), NOW(), TRUE),
(39, 12, 'Wysokość (cm)', 'NUMBER', NOW(), NOW(), TRUE),
(40, 12, 'Podstawa', 'BOOLEAN', NOW(), NOW(), TRUE);

-- Insert specific attributes for glass jewelry
INSERT INTO category_attributes (id, category_id, name, type, created_at, updated_at, is_active) VALUES
(41, 13, 'Rodzaj biżuterii', 'TEXT', NOW(), NOW(), TRUE),
(42, 13, 'Długość (cm)', 'NUMBER', NOW(), NOW(), TRUE),
(43, 13, 'Zapięcie', 'TEXT', NOW(), NOW(), TRUE),
(44, 13, 'Zestaw', 'BOOLEAN', NOW(), NOW(), TRUE);

-- Insert specific attributes for artistic glass
INSERT INTO category_attributes (id, category_id, name, type, created_at, updated_at, is_active) VALUES
(45, 14, 'Funkcjonalność', 'TEXT', NOW(), NOW(), TRUE),
(46, 14, 'Źródło światła', 'TEXT', NOW(), NOW(), TRUE),
(47, 14, 'Montaż', 'TEXT', NOW(), NOW(), TRUE);

-- Update category_attributes sequence
SELECT setval('category_attributes_id_seq', 47, true);
