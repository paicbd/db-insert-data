ALTER TABLE public.cdr
    ADD COLUMN IF NOT EXISTS correlation_id VARCHAR (100);
