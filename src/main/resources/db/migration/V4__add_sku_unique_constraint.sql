-- Add unique constraint for SKU in products table

-- Check if the constraint doesn't already exist before adding it
DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'products_sku_key' 
        AND conrelid = 'products'::regclass
    ) THEN
        ALTER TABLE products
            ADD CONSTRAINT products_sku_key UNIQUE (sku);
    END IF;
END $$;
