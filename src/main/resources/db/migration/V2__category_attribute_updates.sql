-- Align category_attributes with application model

-- Ensure type is varchar(50)
ALTER TABLE IF EXISTS category_attributes
    ALTER COLUMN type TYPE varchar(50);

-- Ensure unique (category_id, name)
DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'category_attributes_category_id_name_key'
    ) THEN
        ALTER TABLE category_attributes
            ADD CONSTRAINT category_attributes_category_id_name_key UNIQUE (category_id, name);
    END IF;
END $$;

-- Defaults and not nulls
UPDATE category_attributes SET is_active = TRUE WHERE is_active IS NULL;
ALTER TABLE category_attributes
    ALTER COLUMN is_active SET DEFAULT TRUE,
    ALTER COLUMN is_active SET NOT NULL,
    ALTER COLUMN created_at SET DEFAULT now(),
    ALTER COLUMN updated_at SET DEFAULT now(),
    ALTER COLUMN created_at SET NOT NULL,
    ALTER COLUMN updated_at SET NOT NULL;
