DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'products'
        AND column_name = 'is_featured'
    ) THEN
ALTER TABLE products
    ADD COLUMN is_featured BOOLEAN DEFAULT FALSE NOT NULL;

CREATE INDEX idx_products_is_featured ON products(is_featured) WHERE is_featured = TRUE;
END IF;
END $$;