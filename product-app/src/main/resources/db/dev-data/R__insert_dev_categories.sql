-- kategorie główne
INSERT INTO categories (name, parent_id)
SELECT 'Komputery', NULL
    WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Komputery');

INSERT INTO categories (name, parent_id)
SELECT 'Smartfony i tablety', NULL
    WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Smartfony i tablety');

INSERT INTO categories (name, parent_id)
SELECT 'Audio', NULL
    WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Audio');

-- podkategorie: rodzic wyszukany po nazwie
INSERT INTO categories (name, parent_id)
SELECT 'Laptopy', c.id FROM categories c
WHERE c.name = 'Komputery'
  AND NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Laptopy');

INSERT INTO categories (name, parent_id)
SELECT 'Komputery stacjonarne', c.id FROM categories c
WHERE c.name = 'Komputery'
  AND NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Komputery stacjonarne');

INSERT INTO categories (name, parent_id)
SELECT 'Smartfony', c.id FROM categories c
WHERE c.name = 'Smartfony i tablety'
  AND NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Smartfony');

INSERT INTO categories (name, parent_id)
SELECT 'Tablety', c.id FROM categories c
WHERE c.name = 'Smartfony i tablety'
  AND NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Tablety');

INSERT INTO categories (name, parent_id)
SELECT 'Słuchawki', c.id FROM categories c
WHERE c.name = 'Audio'
  AND NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Słuchawki');

INSERT INTO categories (name, parent_id)
SELECT 'Głośniki', c.id FROM categories c
WHERE c.name = 'Audio'
  AND NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Głośniki');