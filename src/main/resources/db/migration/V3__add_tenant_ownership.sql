ALTER TABLE recipe_book
    ADD COLUMN user_id UUID;

ALTER TABLE recipe_book
    ADD COLUMN is_global BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE pantry
    ADD COLUMN user_id UUID;

ALTER TABLE pantry
    ADD COLUMN is_global BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE daily_meal
    ADD COLUMN user_id UUID;

ALTER TABLE daily_meal
    ADD COLUMN is_global BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE recipe_book
    ADD CONSTRAINT fk_recipe_book_user
        FOREIGN KEY (user_id)
            REFERENCES app_user(id);

ALTER TABLE pantry
    ADD CONSTRAINT fk_pantry_user
        FOREIGN KEY (user_id)
            REFERENCES app_user(id);

ALTER TABLE daily_meal
    ADD CONSTRAINT fk_daily_meal_user
        FOREIGN KEY (user_id)
            REFERENCES app_user(id);

ALTER TABLE daily_meal
    DROP CONSTRAINT daily_meal_meal_date_key;

ALTER TABLE daily_meal
    ADD CONSTRAINT uq_daily_meal_user_date
        UNIQUE (user_id, meal_date);
