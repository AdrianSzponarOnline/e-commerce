-- Create inventory table for stock management
CREATE SEQUENCE public.inventory_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.inventory_id_seq OWNER TO postgres;

CREATE TABLE public.inventory (
    id bigint DEFAULT nextval('public.inventory_id_seq'::regclass) NOT NULL,
    product_id bigint NOT NULL,
    available_quantity integer DEFAULT 0 NOT NULL,
    reserved_quantity integer DEFAULT 0 NOT NULL,
    minimum_stock_level integer DEFAULT 0 NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL,
    deleted_at timestamp without time zone,
    is_active boolean DEFAULT true NOT NULL
);

ALTER TABLE public.inventory OWNER TO postgres;

-- Primary key constraint
ALTER TABLE ONLY public.inventory
    ADD CONSTRAINT inventory_pkey PRIMARY KEY (id);

-- Unique constraint: one inventory record per product
ALTER TABLE ONLY public.inventory
    ADD CONSTRAINT inventory_product_id_key UNIQUE (product_id);

-- Foreign key constraint to products
ALTER TABLE ONLY public.inventory
    ADD CONSTRAINT inventory_product_id_fkey FOREIGN KEY (product_id) REFERENCES public.products(id) ON DELETE RESTRICT;

-- Index for faster queries
CREATE INDEX idx_inventory_product_id ON public.inventory USING btree (product_id);

