-- Add key_attribute column to category_attributes table
ALTER TABLE public.category_attributes 
ADD COLUMN key_attribute boolean DEFAULT false NOT NULL;

