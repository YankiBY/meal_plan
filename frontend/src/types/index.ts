export interface UserDto {
  id: number;
  username: string;
  email: string;
  avatarPath: string | null;
  blocked: boolean;
  roles: string[];
}

export interface JwtResponse {
  token: string;
  type: string;
  id: number;
  username: string;
  email: string;
  roles: string[];
}

export interface HealthProfileDto {
  id?: number;
  weight: number | null;
  height: number | null;
  age: number | null;
  gender: string | null;
  activityLevel: string | null;
  dailyCalorieTarget: number | null;
  dailyProteinTarget: number | null;
  dailyFatTarget: number | null;
  dailyCarbTarget: number | null;
  diseaseIds: number[];
  allergenIds: number[];
  diseaseNames: string[];
  allergenNames: string[];
}

export interface RecipeDto {
  id: number;
  title: string;
  description: string;
  instructions: string;
  prepTime: number;
  cookTime: number;
  totalCalories: number;
  totalProteins: number;
  totalFats: number;
  totalCarbohydrates: number;
  moderated: boolean;
  authorName: string;
  authorId: number;
  imagePath: string | null;
  ingredients: RecipeIngredientDto[];
  categories: string[];
}

export interface RecipeIngredientDto {
  ingredientId: number;
  ingredientName: string;
  unit: string;
  amount: number;
  calories: number;
  proteins: number;
  fats: number;
  carbohydrates: number;
}

export interface MealPlanDto {
  id: number;
  name: string;
  startDate: string;
  endDate: string;
  days: MealPlanDayDto[];
  totalCalories: number;
  totalProteins: number;
  totalFats: number;
  totalCarbs: number;
}

export interface MealPlanDayDto {
  id: number;
  date: string;
  meals: MealDto[];
  dayCalories: number;
  dayProteins: number;
  dayFats: number;
  dayCarbs: number;
}

export interface MealDto {
  id: number;
  mealType: string;
  recipeId: number;
  recipeTitle: string;
  calories: number;
  proteins: number;
  fats: number;
  carbohydrates: number;
}

export interface ProgressDto {
  id?: number;
  date: string;
  weight: number | null;
  caloriesConsumed: number | null;
  proteinsConsumed: number | null;
  fatsConsumed: number | null;
  carbsConsumed: number | null;
  planComplied: boolean;
}

export interface ProgressCalendarDayDto {
  date: string;
  hasEntry: boolean;
  planComplied: boolean;
  weight: number | null;
}

export interface NutritionExplanationDto {
  bmr: number;
  activityMultiplier: number;
  diseaseMultipliers: Array<{ diseaseId: number; diseaseName: string; multiplier: number | null }>;
  dailyCalories: number;
  dailyProteins: number;
  dailyFats: number;
  dailyCarbs: number;
}

export interface ActivityStatsDto {
  totalUsers: number;
  totalRecipes: number;
  moderatedRecipes: number;
  pendingRecipes: number;
  totalMealPlans: number;
  totalIngredients: number;
  totalDiseases: number;
  totalAllergens: number;
  blockedUsers: number;
}

export interface Disease {
  id: number;
  name: string;
  description: string;
  recommendedCaloriesMultiplier: number;
}

export interface Allergen {
  id: number;
  name: string;
}

export interface Category {
  id: number;
  name: string;
}

export interface Ingredient {
  id: number;
  name: string;
  unit: string;
  calories: number;
  proteins: number;
  fats: number;
  carbohydrates: number;
  barcode: string;
}

export interface IngredientSafetyDto {
  ingredient: Ingredient;
  allowed: boolean;
  matchedAllergens: string[];
  note: string;
}
