import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import api from '../api/client';
import type { RecipeDto } from '../types';
import { Card, CardContent, CardHeader, CardTitle } from '../components/ui/Card';
import Button from '../components/ui/Button';

export default function RecipeDetailPage() {
  const { id } = useParams<{ id: string }>();
  const [recipe, setRecipe] = useState<RecipeDto | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadRecipe = async () => {
      try {
        const res = await api.get<RecipeDto>(`/recipes/${id}`);
        setRecipe(res.data);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    loadRecipe();
  }, [id]);

  if (loading) return <div className="p-8 text-center">Загрузка...</div>;
  if (!recipe) return <div className="p-8 text-center">Рецепт не найден</div>;

  return (
    <div className="max-w-4xl mx-auto space-y-6 animate-fade-in">
      <Link to="/recipes">
        <Button variant="secondary" size="sm" className="mb-4">← Назад к рецептам</Button>
      </Link>

      <Card className="overflow-hidden">
        <div className="aspect-video w-full bg-gray-100 relative overflow-hidden">
          {recipe.imagePath ? (
            <img 
              src={`/api/files/${recipe.imagePath}`} 
              alt={recipe.title} 
              className="w-full h-full object-cover" 
            />
          ) : (
            <div className="w-full h-full flex items-center justify-center text-gray-400">
              Нет изображения
            </div>
          )}
          <div className="absolute top-4 right-4 flex gap-2">
            {recipe.categories.map(cat => (
              <span key={cat} className="px-3 py-1 bg-white/90 backdrop-blur text-rose text-xs font-semibold rounded-full shadow-sm">
                {cat}
              </span>
            ))}
          </div>
        </div>

        <CardHeader>
          <div className="flex justify-between items-start">
            <div>
              <CardTitle className="text-3xl font-bold text-gray-900">{recipe.title}</CardTitle>
              <p className="text-gray-500 mt-1">Автор: {recipe.authorName}</p>
            </div>
            <div className="text-right">
              <div className="text-2xl font-bold text-rose">{recipe.totalCalories.toFixed(0)} ккал</div>
              <div className="text-xs text-gray-500 mt-1">на 100г или порцию</div>
            </div>
          </div>
        </CardHeader>

        <CardContent className="space-y-8">
          <div className="grid grid-cols-4 gap-4 p-4 bg-gray-50 dark:bg-slate-800/50 rounded-xl">
            <NutritionItem label="Белки" value={recipe.totalProteins} unit="г" color="text-blue-500" />
            <NutritionItem label="Жиры" value={recipe.totalFats} unit="г" color="text-amber-500" />
            <NutritionItem label="Углеводы" value={recipe.totalCarbohydrates} unit="г" color="text-purple-500" />
            <NutritionItem label="Время" value={recipe.prepTime + recipe.cookTime} unit="мин" color="text-rose" />
          </div>

          <div className="space-y-4">
            <h3 className="text-xl font-semibold border-b pb-2">Описание</h3>
            <p className="text-gray-700 dark:text-slate-300 leading-relaxed">
              {recipe.description || 'Описание отсутствует.'}
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <div className="md:col-span-1 space-y-4">
              <h3 className="text-xl font-semibold border-b pb-2">Ингредиенты</h3>
              <ul className="space-y-2">
                {recipe.ingredients.map((ing, i) => (
                  <li key={i} className="flex justify-between text-sm py-1 border-b border-gray-100 dark:border-slate-800 last:border-0">
                    <span className="text-gray-700 dark:text-slate-300">{ing.ingredientName}</span>
                    <span className="font-medium">{ing.amount} {ing.unit}</span>
                  </li>
                ))}
              </ul>
            </div>

            <div className="md:col-span-2 space-y-4">
              <h3 className="text-xl font-semibold border-b pb-2">Способ приготовления</h3>
              <div className="prose dark:prose-invert max-w-none">
                <p className="whitespace-pre-wrap text-gray-700 dark:text-slate-300 leading-relaxed">
                  {recipe.instructions}
                </p>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}

function NutritionItem({ label, value, unit, color }: { label: string; value: number; unit: string; color: string }) {
  return (
    <div className="text-center">
      <div className="text-xs text-gray-500 uppercase font-medium">{label}</div>
      <div className={`text-lg font-bold ${color}`}>{value.toFixed(1)}{unit}</div>
    </div>
  );
}
