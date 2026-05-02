import { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import api from '../api/client';
import type { RecipeDto, HealthProfileDto } from '../types';
import { Card, CardContent, CardHeader, CardTitle } from '../components/ui/Card';
import Button from '../components/ui/Button';
import { cn } from '../components/ui/cn';

export default function SelectRecipePage() {
  const [recipes, setRecipes] = useState<RecipeDto[]>([]);
  const [profile, setProfile] = useState<HealthProfileDto | null>(null);
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  
  const mealType = searchParams.get('type') || 'завтрак';
  const date = searchParams.get('date') || new Date().toISOString().split('T')[0];

  useEffect(() => {
    const loadData = async () => {
      const [recRes, profRes] = await Promise.all([
        api.get<RecipeDto[]>('/recipes'),
        api.get<HealthProfileDto>('/profile/health')
      ]);
      setRecipes(recRes.data);
      setProfile(profRes.data);
    };
    loadData();
  }, []);

  const handleSelect = async (recipeId: number) => {
    try {
      await api.post('/progress/add-recipe', null, {
        params: {
          recipeId,
          date
        }
      });
      navigate('/meal-plans');
    } catch (err) {
      console.error('Ошибка при выборе рецепта:', err);
    }
  };

  // Простая фильтрация по аллергиям
  const filteredRecipes = recipes.filter(r => {
    if (!profile) return true;
    const hasAllergen = r.ingredients.some(ing => 
      profile.allergenNames.some(an => ing.ingredientName.toLowerCase().includes(an.toLowerCase()))
    );
    return !hasAllergen;
  });

  return (
    <div className="space-y-6 animate-fade-in">
      <div className="flex justify-between items-end">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Выбор рецепта: {mealType}</h1>
          <p className="text-gray-500">Рекомендации с учетом вашего профиля здоровья</p>
        </div>
        <Button variant="secondary" onClick={() => navigate('/meal-plans')}>Отмена</Button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-[1fr_300px] gap-6">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {filteredRecipes.map(recipe => (
            <Card key={recipe.id} className="hover:ring-2 hover:ring-rose transition-all cursor-pointer overflow-hidden flex flex-col" onClick={() => handleSelect(recipe.id)}>
              <div className="h-40 bg-gray-100 relative">
                {recipe.imagePath && <img src={`/api/files/${recipe.imagePath}`} alt="" className="w-full h-full object-cover" />}
                <div className="absolute top-2 right-2 px-2 py-0.5 bg-white/90 rounded-full text-[10px] font-bold text-rose">
                  {recipe.totalCalories.toFixed(0)} ккал
                </div>
              </div>
              <CardContent className="p-4 flex-1 flex flex-col">
                <h3 className="font-bold text-gray-900 line-clamp-1">{recipe.title}</h3>
                <p className="text-xs text-gray-500 line-clamp-2 mt-1 mb-3">{recipe.description}</p>
                <div className="mt-auto flex justify-between items-center text-[10px] font-medium text-gray-400 uppercase tracking-wider">
                  <span>Б: {recipe.totalProteins.toFixed(0)}г</span>
                  <span>Ж: {recipe.totalFats.toFixed(0)}г</span>
                  <span>У: {recipe.totalCarbohydrates.toFixed(0)}г</span>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>

        <aside className="space-y-4">
          <Card className="sticky top-24 bg-rose text-white border-none shadow-lg">
            <CardHeader>
              <CardTitle className="text-white text-lg">Остаток на день</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <RemainingItem label="Калории" current={0} target={profile?.dailyCalorieTarget || 0} unit="ккал" />
              <RemainingItem label="Белки" current={0} target={profile?.dailyProteinTarget || 0} unit="г" />
              <RemainingItem label="Жиры" current={0} target={profile?.dailyFatTarget || 0} unit="г" />
              <RemainingItem label="Углеводы" current={0} target={profile?.dailyCarbTarget || 0} unit="г" />
            </CardContent>
          </Card>
          
          <div className="p-4 rounded-xl bg-amber/10 border border-amber/20">
            <h4 className="text-sm font-bold text-amber-dark mb-1">Совет</h4>
            <p className="text-xs text-amber-800/80 leading-relaxed">
              Выбирайте блюда, которые помогают равномерно распределить нутриенты в течение дня.
            </p>
          </div>
        </aside>
      </div>
    </div>
  );
}

function RemainingItem({ label, current, target, unit }: { label: string; current: number; target: number; unit: string }) {
  const remaining = target - current;
  const percent = Math.min(100, (current / target) * 100);

  return (
    <div className="space-y-1">
      <div className="flex justify-between text-xs font-medium">
        <span>{label}</span>
        <span>{remaining.toFixed(0)} {unit}</span>
      </div>
      <div className="h-1.5 w-full bg-white/20 rounded-full overflow-hidden">
        <div className="h-full bg-white transition-all duration-500" style={{ width: `${percent}%` }} />
      </div>
    </div>
  );
}
