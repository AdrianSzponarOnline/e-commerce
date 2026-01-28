-- Add shop_description setting for short shop description (used in FooterDataDTO)
INSERT INTO public.shop_settings (setting_key, setting_value, description) VALUES
('shop_description', 'Sklep z rękodziełem i produktami handmade.', 'Krótki opis sklepu wyświetlany w stopce i na stronie głównej')
ON CONFLICT (setting_key) DO NOTHING;
