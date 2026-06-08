CREATE TABLE recipe_book (
    id UUID PRIMARY KEY,
    name VARCHAR(255),
    description TEXT
);

CREATE TABLE recipe_ingredients (
    id UUID PRIMARY KEY,

    name VARCHAR(255),
    unit SMALLINT,
    amount DOUBLE PRECISION,
    category SMALLINT,

    food_type VARCHAR(255),
    preparation VARCHAR(255),

    recipe_id UUID,

    CONSTRAINT fk_recipe_ingredients_recipe
        FOREIGN KEY (recipe_id)
            REFERENCES recipe_book(id)
            ON DELETE CASCADE
);

CREATE TABLE pantry (
    id UUID PRIMARY KEY,

    name VARCHAR(255),
    unit SMALLINT,
    amount DOUBLE PRECISION,
    category SMALLINT,

    expiration_date DATE,
    purchase_date DATE,
    brand VARCHAR(255),
    price DOUBLE PRECISION NOT NULL DEFAULT 0
);

CREATE TABLE daily_meal (
    id UUID PRIMARY KEY,

    meal_date DATE NOT NULL UNIQUE,

    breakfast_id UUID,
    lunch_id UUID,
    dinner_id UUID,

    breakfast_servings INTEGER NOT NULL DEFAULT 0,
    lunch_servings INTEGER NOT NULL DEFAULT 0,
    dinner_servings INTEGER NOT NULL DEFAULT 0,

    CONSTRAINT fk_daily_meal_breakfast
        FOREIGN KEY (breakfast_id)
            REFERENCES recipe_book(id),

    CONSTRAINT fk_daily_meal_lunch
        FOREIGN KEY (lunch_id)
            REFERENCES recipe_book(id),

    CONSTRAINT fk_daily_meal_dinner
        FOREIGN KEY (dinner_id)
            REFERENCES recipe_book(id)
);