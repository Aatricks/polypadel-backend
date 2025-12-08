-- Add fields to track failed login attempts and lockout
ALTER TABLE utilisateur
ADD COLUMN IF NOT EXISTS failed_login_attempts integer NOT NULL DEFAULT 0,
ADD COLUMN IF NOT EXISTS lockout_until timestamptz NULL;