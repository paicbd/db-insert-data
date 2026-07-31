ALTER TABLE public.cdr
    ADD COLUMN IF NOT EXISTS local_translation_type  text,
    ADD COLUMN IF NOT EXISTS remote_translation_type text;
