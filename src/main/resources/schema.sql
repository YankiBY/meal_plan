-- Схема БД для приложения Meal Plan (3НФ)

-- 1. Роли пользователей
CREATE TABLE IF NOT EXISTS roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- 2. Пользователи
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    avatar_path VARCHAR(500),
    blocked BOOLEAN DEFAULT FALSE
);

-- 3. Связь пользователей и ролей (Many-to-Many)
CREATE TABLE IF NOT EXISTS user_roles (
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id INTEGER NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- 4. Заболевания (Справочник)
CREATE TABLE IF NOT EXISTS diseases (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    recommended_calories_multiplier DOUBLE PRECISION DEFAULT 1.0
);

-- 5. Аллергены (Справочник)
CREATE TABLE IF NOT EXISTS allergens (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

-- 6. Профили здоровья пользователей (One-to-One с users)
CREATE TABLE IF NOT EXISTS health_profiles (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    weight DOUBLE PRECISION,
    height DOUBLE PRECISION,
    age INTEGER,
    gender VARCHAR(10),
    activity_level VARCHAR(50),
    daily_calorie_target DOUBLE PRECISION,
    daily_protein_target DOUBLE PRECISION,
    daily_fat_target DOUBLE PRECISION,
    daily_carb_target DOUBLE PRECISION
);

-- 7. Ограничения профиля по заболеваниям (Many-to-Many)
CREATE TABLE IF NOT EXISTS profile_diseases (
    profile_id INTEGER NOT NULL REFERENCES health_profiles(id) ON DELETE CASCADE,
    disease_id INTEGER NOT NULL REFERENCES diseases(id) ON DELETE CASCADE,
    PRIMARY KEY (profile_id, disease_id)
);

-- 8. Ограничения профиля по аллергенам (Many-to-Many)
CREATE TABLE IF NOT EXISTS profile_allergens (
    profile_id INTEGER NOT NULL REFERENCES health_profiles(id) ON DELETE CASCADE,
    allergen_id INTEGER NOT NULL REFERENCES allergens(id) ON DELETE CASCADE,
    PRIMARY KEY (profile_id, allergen_id)
);

-- 9. Ингредиенты
CREATE TABLE IF NOT EXISTS ingredients (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    unit VARCHAR(20),
    calories DOUBLE PRECISION,
    proteins DOUBLE PRECISION,
    fats DOUBLE PRECISION,
    carbohydrates DOUBLE PRECISION,
    barcode VARCHAR(50)
);

-- 10. Рецепты
CREATE TABLE IF NOT EXISTS recipes (
    id SERIAL PRIMARY KEY,
    author_id INTEGER REFERENCES users(id) ON DELETE SET NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    instructions TEXT,
    prep_time INTEGER,
    cook_time INTEGER,
    total_calories DOUBLE PRECISION,
    total_proteins DOUBLE PRECISION,
    total_fats DOUBLE PRECISION,
    total_carbohydrates DOUBLE PRECISION,
    is_moderated BOOLEAN DEFAULT FALSE
);

-- 11. Состав рецептов (Many-to-Many с доп. полем amount)
CREATE TABLE IF NOT EXISTS recipe_ingredients (
    id SERIAL PRIMARY KEY,
    recipe_id INTEGER NOT NULL REFERENCES recipes(id) ON DELETE CASCADE,
    ingredient_id INTEGER NOT NULL REFERENCES ingredients(id) ON DELETE CASCADE,
    amount DOUBLE PRECISION NOT NULL
);

-- 12. Категории блюд
CREATE TABLE IF NOT EXISTS categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- 13. Категории рецептов (Many-to-Many)
CREATE TABLE IF NOT EXISTS recipe_categories (
    recipe_id INTEGER NOT NULL REFERENCES recipes(id) ON DELETE CASCADE,
    category_id INTEGER NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    PRIMARY KEY (recipe_id, category_id)
);

-- 14. Планы питания
CREATE TABLE IF NOT EXISTS meal_plans (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(100),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL
);

-- 15. Дни в плане питания
CREATE TABLE IF NOT EXISTS meal_plan_days (
    id SERIAL PRIMARY KEY,
    meal_plan_id INTEGER NOT NULL REFERENCES meal_plans(id) ON DELETE CASCADE,
    date DATE NOT NULL
);

-- 16. Приемы пищи в конкретный день
CREATE TABLE IF NOT EXISTS meals (
    id SERIAL PRIMARY KEY,
    meal_plan_day_id INTEGER NOT NULL REFERENCES meal_plan_days(id) ON DELETE CASCADE,
    recipe_id INTEGER NOT NULL REFERENCES recipes(id),
    meal_type VARCHAR(20) NOT NULL
);

-- 17. Прогресс пользователя
CREATE TABLE IF NOT EXISTS user_progress (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    weight DOUBLE PRECISION,
    calories_consumed DOUBLE PRECISION,
    proteins_consumed DOUBLE PRECISION,
    fats_consumed DOUBLE PRECISION,
    carbs_consumed DOUBLE PRECISION,
    plan_complied BOOLEAN DEFAULT FALSE
);

-- Начальные данные
INSERT INTO roles (name) VALUES ('ROLE_USER') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('ROLE_ADMIN') ON CONFLICT (name) DO NOTHING;
