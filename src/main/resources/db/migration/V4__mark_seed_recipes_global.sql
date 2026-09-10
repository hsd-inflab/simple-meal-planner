UPDATE recipe_book
SET is_global = TRUE
WHERE user_id IS NULL
  AND is_global = FALSE
  AND name IN (
      'Grundbasis Bolognese',
      'Caffè Latte',
      'Schnelle Apfeltarte'
  );
