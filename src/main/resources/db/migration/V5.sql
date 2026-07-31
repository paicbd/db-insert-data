alter table public.cdr
    ADD COLUMN IF NOT EXISTS mno_message_id varchar(50);