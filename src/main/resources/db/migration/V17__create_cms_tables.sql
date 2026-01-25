-- CMS Tables Migration
-- Adds support for Pages, Shop Settings, Social Links, and FAQ

-- Create pages table if it doesn't exist (with all new columns)
-- Use EXECUTE for dynamic SQL in case table doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.tables 
        WHERE table_schema = 'public' 
        AND table_name = 'pages'
    ) THEN
        EXECUTE 'CREATE TABLE public.pages (
            id BIGSERIAL PRIMARY KEY,
            slug VARCHAR(100) NOT NULL,
            title VARCHAR(100) NOT NULL,
            content TEXT,
            is_system BOOLEAN DEFAULT false NOT NULL,
            created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT now() NOT NULL,
            updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT now() NOT NULL,
            deleted_at TIMESTAMP WITHOUT TIME ZONE,
            is_active BOOLEAN DEFAULT true NOT NULL
        )';
        
        EXECUTE 'ALTER TABLE public.pages ADD CONSTRAINT pages_slug_key UNIQUE (slug)';
    END IF;
END $$;

-- Update existing pages table if it was created with INTEGER id or missing columns
DO $$
BEGIN
    -- Change id column type from INTEGER to BIGINT if it's currently INTEGER
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
        AND table_name = 'pages' 
        AND column_name = 'id' 
        AND data_type = 'integer'
    ) THEN
        ALTER TABLE public.pages ALTER COLUMN id DROP DEFAULT;
        ALTER TABLE public.pages ALTER COLUMN id TYPE BIGINT USING id::BIGINT;
        ALTER TABLE public.pages ALTER COLUMN id SET DEFAULT nextval('public.pages_id_seq'::regclass);
    END IF;
    
    -- Add new columns if they don't exist (for existing tables from V1)
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
        AND table_name = 'pages' 
        AND column_name = 'is_system'
    ) THEN
        ALTER TABLE public.pages ADD COLUMN is_system BOOLEAN DEFAULT false NOT NULL;
    END IF;
    
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
        AND table_name = 'pages' 
        AND column_name = 'created_at'
    ) THEN
        ALTER TABLE public.pages ADD COLUMN created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT now() NOT NULL;
    END IF;
    
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
        AND table_name = 'pages' 
        AND column_name = 'updated_at'
    ) THEN
        ALTER TABLE public.pages ADD COLUMN updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT now() NOT NULL;
    END IF;
    
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
        AND table_name = 'pages' 
        AND column_name = 'deleted_at'
    ) THEN
        ALTER TABLE public.pages ADD COLUMN deleted_at TIMESTAMP WITHOUT TIME ZONE;
    END IF;
    
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
        AND table_name = 'pages' 
        AND column_name = 'is_active'
    ) THEN
        ALTER TABLE public.pages ADD COLUMN is_active BOOLEAN DEFAULT true NOT NULL;
    END IF;
    
    -- Add unique constraint to slug if not exists
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'pages_slug_key'
    ) THEN
        ALTER TABLE public.pages ADD CONSTRAINT pages_slug_key UNIQUE (slug);
    END IF;
END $$;

-- Create shop_settings table
CREATE TABLE IF NOT EXISTS public.shop_settings (
    id BIGSERIAL PRIMARY KEY,
    setting_key VARCHAR(100) NOT NULL UNIQUE,
    setting_value VARCHAR(1000),
    description VARCHAR(500)
);

-- Create social_links table
CREATE TABLE IF NOT EXISTS public.social_links (
    id BIGSERIAL PRIMARY KEY,
    platform_name VARCHAR(100) NOT NULL UNIQUE,
    url VARCHAR(500) NOT NULL,
    icon_code VARCHAR(100),
    sort_order INTEGER DEFAULT 0 NOT NULL,
    is_active BOOLEAN DEFAULT true NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT now() NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT now() NOT NULL,
    deleted_at TIMESTAMP WITHOUT TIME ZONE
);

-- Create faq_items table
CREATE TABLE IF NOT EXISTS public.faq_items (
    id BIGSERIAL PRIMARY KEY,
    question VARCHAR(500) NOT NULL UNIQUE,
    answer VARCHAR(2000) NOT NULL,
    sort_order INTEGER DEFAULT 0 NOT NULL,
    is_active BOOLEAN DEFAULT true NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT now() NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT now() NOT NULL,
    deleted_at TIMESTAMP WITHOUT TIME ZONE
);

-- Seed data: Shop Settings
INSERT INTO public.shop_settings (setting_key, setting_value, description) VALUES
('shop_name', 'E-Shop', 'Nazwa sklepu wyświetlana w nagłówku'),
('logo_url', '/uploads/logo.png', 'URL do logo sklepu'),
('footer_copyright', '© 2025 E-Shop. Wszelkie prawa zastrzeżone.', 'Tekst w stopce'),
('contact_phone', '+48 123 456 789', 'Telefon kontaktowy'),
('contact_email', 'kontakt@sklep.pl', 'Email kontaktowy'),
('contact_address', 'ul. Przykładowa 123, 00-000 Warszawa', 'Adres kontaktowy'),
('opening_hours', 'Pon-Pt: 9:00-17:00, Sob: 10:00-14:00', 'Godziny otwarcia')
ON CONFLICT (setting_key) DO NOTHING;

-- Seed data: Pages (system pages)
INSERT INTO public.pages (slug, title, content, is_system, is_active) VALUES
('o-nas', 'O nas', '<h1>O nas</h1><p>Jesteśmy super sklepem z rękodziełem...</p>', true, true),
('regulamin', 'Regulamin', '<h1>Regulamin</h1><p>Paragraf 1. Postanowienia ogólne...</p>', true, true),
('polityka-prywatnosci', 'Polityka Prywatności', '<h1>Polityka Prywatności</h1><p>RODO - informacje o przetwarzaniu danych...</p>', true, true),
('dostawa-i-zwroty', 'Dostawa i zwroty', '<h1>Dostawa i zwroty</h1><p>Informacje o dostawie i zwrotach...</p>', true, true),
('cookies', 'Polityka Cookies', '<h1>Polityka Cookies</h1><p>Informacje o wykorzystaniu plików cookies...</p>', true, true)
ON CONFLICT (slug) DO NOTHING;

-- Seed data: Social Links
INSERT INTO public.social_links (platform_name, url, icon_code, sort_order, is_active) VALUES
('Facebook', 'https://facebook.com/mojsklep', 'fa-facebook', 1, true),
('Instagram', 'https://instagram.com/mojsklep', 'fa-instagram', 2, true),
('Twitter', 'https://twitter.com/mojsklep', 'fa-twitter', 3, true)
ON CONFLICT (platform_name) DO NOTHING;

-- Seed data: FAQ Items
INSERT INTO public.faq_items (question, answer, sort_order, is_active) VALUES
('Jak złożyć zamówienie?', 'Aby złożyć zamówienie, dodaj produkty do koszyka i przejdź do kasy. Możesz zamówić jako gość lub po zalogowaniu.', 1, true),
('Jakie są metody płatności?', 'Akceptujemy płatności kartą kredytową, przelewem bankowym oraz płatności online.', 2, true),
('Ile trwa dostawa?', 'Standardowa dostawa trwa 3-5 dni roboczych. Ekspresowa dostawa (1-2 dni) jest dostępna za dodatkową opłatą.', 3, true),
('Czy mogę zwrócić produkt?', 'Tak, masz 14 dni na zwrot produktu od daty otrzymania. Produkt musi być w oryginalnym opakowaniu.', 4, true),
('Jak mogę śledzić moje zamówienie?', 'Po złożeniu zamówienia otrzymasz email z numerem śledzenia. Możesz również sprawdzić status w panelu użytkownika.', 5, true)
ON CONFLICT (question) DO NOTHING;
