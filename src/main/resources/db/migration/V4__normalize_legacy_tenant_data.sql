-- Normalizes rows that were created before multi-tenancy was introduced in V3.
--
-- Recipes without an owner are the standard recipes seeded by the autofiller. They are shared by every user and
-- are therefore promoted to global. Recipes that already carry an owner stay private.
UPDATE recipe_book
SET is_global = TRUE
WHERE user_id IS NULL;

-- Pantry items and daily meals are personal by definition. Rows without an owner belong to nobody, are not
-- reachable through any tenant scoped query and are removed. This deletes the demo data of earlier autofill runs.
DELETE FROM daily_meal
WHERE user_id IS NULL;

DELETE FROM pantry
WHERE user_id IS NULL;
