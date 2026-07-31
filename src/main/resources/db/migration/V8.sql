ALTER TABLE public.cdr
    ADD COLUMN IF NOT EXISTS optional_parameters TEXT;
