-- No pre-loaded data: the mock Azure AD configuration short-circuits MSAL,
-- so the token_cache table is not touched by the IT chat-send flow. The
-- SELECT keeps Spring's script runner happy (it rejects empty scripts).
SELECT 1;
