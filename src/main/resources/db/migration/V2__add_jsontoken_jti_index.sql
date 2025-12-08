-- Add index on json_token.jti for revocation lookup
CREATE INDEX IF NOT EXISTS idx_jsontoken_jti ON json_token (jti);
