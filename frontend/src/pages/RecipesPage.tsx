import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../api/client';
import toast from 'react-hot-toast';
import type { RecipeDto, Ingredient, Category } from '../types';
import { Card, CardContent } from '../components/ui/Card';
import Button from '../components/ui/Button';
import Input from '../components/ui/Input';
import { cn } from '../components/ui/cn';

export default function RecipesPage() {
  const [recipes, setRecipes] = useState<RecipeDto[]>([]);
  const [myRecipes, setMyRecipes] = useState<RecipeDto[]>([]);
  const [tab, setTab] = useState<'all' | 'my' | 'create'>('all');
  const [ingredients, setIngredients] = useState<Ingredient[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [search, setSearch] = useState('');
  const navigate = useNavigate();

  const [form, setForm] = useState({
    title: '',
    description: '',
    instructions: '',
    prepTime: '',
    cookTime: '',
    ingredients: [] as { ingredientId: number; amount: number }[],
    categoryIds: [] as number[],
    image: null as File | null
  });

  useEffect(() => { loadData(); }, []);

  const loadData = async () => {
    const [recipesRes, myRes, ingRes, catRes] = await Promise.all([
      api.get<RecipeDto[]>('/recipes'),
      api.get<RecipeDto[]>('/recipes/my'),
      api.get<Ingredient[]>('/reference/ingredients'),
      api.get<Category[]>('/reference/categories'),
    ]);
    // Вкладка "Все" должна показывать одобренные рецепты + свои неодобренные
    const allVisible = recipesRes.data;
    const myUnmoderated = myRes.data.filter(r => !r.moderated);
    
    // Объединяем, убирая дубликаты по ID
    const combined = [...allVisible];
    myUnmoderated.forEach(ur => {
      if (!combined.find(c => c.id === ur.id)) {
        combined.push(ur);
      }
    });

    setRecipes(combined);
    setMyRecipes(myRes.data);
    setIngredients(ingRes.data);
    setCategories(catRes.data);
  };

  const deleteRecipe = async (id: number) => {
    if (!confirm('Вы уверены, что хотите удалить этот рецепт?')) return;
    try {
      await api.delete(`/recipes/${id}`);
      toast.success('Рецепт удален');
      loadData();
    } catch {
      toast.error('Ошибка при удалении');
    }
  };

  const createRecipe = async () => {
    try {
      if (form.image) {
        if (form.image.size > 5 * 1024 * 1024) {
          toast.error('Файл слишком большой (макс. 5МБ)');
          return;
        }
        if (!form.image.type.startsWith('image/')) {
          toast.error('Допускаются только изображения');
          return;
        }
      }

      const formData = new FormData();
      const recipeData = {
        ...form,
        prepTime: parseInt(form.prepTime) || 0,
        cookTime: parseInt(form.cookTime) || 0,
      };
      delete (recipeData as any).image;

      formData.append('recipe', new Blob([JSON.stringify(recipeData)], { type: 'application/json' }));
      if (form.image) {
        formData.append('image', form.image);
      }

      await api.post('/recipes', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });
      
      toast.success('Рецепт отправлен на модерацию');
      setForm({ title: '', description: '', instructions: '', prepTime: '', cookTime: '', ingredients: [], categoryIds: [], image: null });
      setTab('my');
      loadData();
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Ошибка');
    }
  };

  const addIngredient = () => {
    if (ingredients.length === 0) return;
    setForm({ ...form, ingredients: [...form.ingredients, { ingredientId: ingredients[0].id, amount: 100 }] });
  };

  const toggleCategory = (id: number) => {
    setForm({ ...form, categoryIds: form.categoryIds.includes(id) ? form.categoryIds.filter(c => c !== id) : [...form.categoryIds, id] });
  };

  const currentList = (tab === 'all' ? recipes : myRecipes).filter(r =>
    (r.title + ' ' + (r.description || '')).toLowerCase().includes(search.toLowerCase()),
  );

  const [showNewIngredient, setShowNewIngredient] = useState(false);
  const [newIngredient, setNewIngredient] = useState({ name: '', unit: 'г', calories: '', proteins: '', fats: '', carbohydrates: '' });

  const createNewIngredient = async () => {
    try {
      const res = await api.post<Ingredient>('/admin/ingredients', newIngredient);
      setIngredients([...ingredients, res.data]);
      toast.success('Ингредиент добавлен');
      setShowNewIngredient(false);
      setNewIngredient({ name: '', unit: 'г', calories: '', proteins: '', fats: '', carbohydrates: '' });
    } catch {
      toast.error('Ошибка при создании ингредиента');
    }
  };

  return (
    <div className="space-y-6 animate-fade-in">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <h1 className="text-3xl font-bold text-rose">Рецепты</h1>
        <div className="flex bg-gray-100 dark:bg-slate-800 p-1 rounded-xl w-fit">
          {(['all', 'my', 'create'] as const).map(t => (
            <button 
              key={t} 
              onClick={() => setTab(t)}
              className={cn(
                "px-4 py-1.5 rounded-lg text-sm font-medium transition-all",
                tab === t ? "bg-white dark:bg-slate-700 text-rose shadow-sm" : "text-gray-500 hover:text-gray-700 dark:hover:text-slate-300"
              )}
            >
              {t === 'all' ? 'Все' : t === 'my' ? 'Мои' : 'Создать'}
            </button>
          ))}
        </div>
      </div>

      {tab !== 'create' && (
        <>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
            <Card className="bg-rose text-white border-none shadow-md">
              <CardContent className="p-4 flex flex-col justify-between h-full">
                <p className="text-rose-light text-[10px] font-bold uppercase tracking-wider">Найдено рецептов</p>
                <p className="text-3xl font-bold mt-1 leading-none">{currentList.length}</p>
              </CardContent>
            </Card>
            <div className="sm:col-span-1 lg:col-span-3">
              <Input 
                value={search} 
                onChange={e => setSearch(e.target.value)} 
                placeholder="Поиск по названиям и описаниям..." 
                className="h-full bg-white dark:bg-slate-900 border-gray-200 dark:border-slate-800"
              />
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {currentList.map(recipe => (
              <Card 
                key={recipe.id} 
                className="group hover-lift overflow-hidden border-none shadow-sm ring-1 ring-gray-200 dark:ring-slate-800"
              >
                <Link to={`/recipes/${recipe.id}`}>
                  <div className="aspect-[4/3] bg-gray-100 relative overflow-hidden">
                    {recipe.imagePath ? (
                      <img 
                        src={`/api/files/${recipe.imagePath}`} 
                        alt={recipe.title} 
                        className="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105" 
                      />
                    ) : (
                      <div className="w-full h-full flex items-center justify-center text-gray-400">
                        Нет фото
                      </div>
                    )}
                    <div className="absolute top-3 right-3 flex gap-1">
                      {!recipe.moderated && (
                        <span className="px-2 py-0.5 bg-amber/90 backdrop-blur text-[10px] font-bold text-amber-900 rounded-full">
                          Черновик
                        </span>
                      )}
                      {recipe.categories.slice(0, 2).map(cat => (
                        <span key={cat} className="px-2 py-0.5 bg-white/90 backdrop-blur text-[10px] font-bold text-rose rounded-full">
                          {cat}
                        </span>
                      ))}
                    </div>
                  </div>
                  <CardContent className="p-4">
                    <div className="flex justify-between items-start gap-2">
                      <h3 className="font-bold text-gray-900 dark:text-slate-100 line-clamp-1 group-hover:text-rose transition-colors">
                        {recipe.title}
                      </h3>
                      {(tab === 'my' || (recipe.authorName === 'admin' && tab === 'all')) && (
                        <button 
                          onClick={(e) => { e.preventDefault(); e.stopPropagation(); deleteRecipe(recipe.id); }}
                          className="text-gray-400 hover:text-coral transition-colors p-1"
                          title="Удалить"
                        >
                          <span className="text-lg">×</span>
                        </button>
                      )}
                    </div>
                    <p className="text-sm text-gray-500 dark:text-slate-400 line-clamp-2 mt-1 h-10">
                      {recipe.description}
                    </p>
                    <div className="mt-4 pt-4 border-t border-gray-100 dark:border-slate-800 grid grid-cols-4 gap-2 text-center">
                      <MacroItem label="Ккал" value={recipe.totalCalories} color="text-rose" />
                      <MacroItem label="Б" value={recipe.totalProteins} color="text-blue-500" />
                      <MacroItem label="Ж" value={recipe.totalFats} color="text-amber-500" />
                      <MacroItem label="У" value={recipe.totalCarbohydrates} color="text-purple-500" />
                    </div>
                  </CardContent>
                </Link>
              </Card>
            ))}
          </div>
        </>
      )}

      {tab === 'create' && (
        <Card className="max-w-3xl mx-auto">
          <CardContent className="p-6 space-y-6">
            <h2 className="text-2xl font-bold text-gray-900 dark:text-slate-100">Новый рецепт</h2>
            
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="md:col-span-2">
                <label className="text-sm font-medium text-gray-700 dark:text-slate-300 block mb-1">Фото блюда</label>
                <input 
                  type="file" 
                  accept="image/*" 
                  onChange={e => setForm({...form, image: e.target.files?.[0] || null})}
                  className="w-full text-sm text-gray-500 file:mr-4 file:py-2 file:px-4 file:rounded-full file:border-0 file:text-sm file:font-semibold file:bg-rose-light file:text-rose hover:file:bg-rose/20"
                />
              </div>

              <div className="md:col-span-2">
                <label className="text-sm font-medium text-gray-700 dark:text-slate-300 block mb-1">Название</label>
                <Input value={form.title} onChange={e => setForm({...form, title: e.target.value})} placeholder="Напр: Омлет с томатами" />
              </div>

              <div className="md:col-span-2">
                <label className="text-sm font-medium text-gray-700 dark:text-slate-300 block mb-1">Короткое описание</label>
                <textarea 
                  className="w-full px-3 py-2 border rounded-lg dark:bg-slate-900 dark:border-slate-800" 
                  rows={2}
                  value={form.description} 
                  onChange={e => setForm({...form, description: e.target.value})} 
                  placeholder="Кратко о блюде..."
                />
              </div>

              <div className="md:col-span-2">
                <label className="text-sm font-medium text-gray-700 dark:text-slate-300 block mb-1">Способ приготовления</label>
                <textarea 
                  className="w-full px-3 py-2 border rounded-lg dark:bg-slate-900 dark:border-slate-800" 
                  rows={5}
                  value={form.instructions} 
                  onChange={e => setForm({...form, instructions: e.target.value})} 
                  placeholder="Шаг за шагом..."
                />
              </div>

              <div>
                <label className="text-sm font-medium text-gray-700 dark:text-slate-300 block mb-1">Подготовка (мин)</label>
                <Input type="number" value={form.prepTime} onChange={e => setForm({...form, prepTime: e.target.value})} />
              </div>
              <div>
                <label className="text-sm font-medium text-gray-700 dark:text-slate-300 block mb-1">Готовка (мин)</label>
                <Input type="number" value={form.cookTime} onChange={e => setForm({...form, cookTime: e.target.value})} />
              </div>
            </div>

            <div className="space-y-3">
              <div className="flex justify-between items-center">
                <label className="text-sm font-medium text-gray-700 dark:text-slate-300">Ингредиенты</label>
                <div className="flex gap-2">
                  <Button variant="ghost" size="sm" onClick={() => setShowNewIngredient(!showNewIngredient)} className="text-rose">
                    {showNewIngredient ? 'Отмена' : '+ Создать новый'}
                  </Button>
                  <Button variant="ghost" size="sm" onClick={addIngredient} className="text-rose">+ Добавить из базы</Button>
                </div>
              </div>

              {showNewIngredient && (
                <div className="p-4 bg-gray-50 dark:bg-slate-800/50 rounded-xl space-y-3 animate-fade-in border border-rose/10">
                  <p className="text-xs font-bold text-rose uppercase tracking-wider">Новый ингредиент</p>
                  <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
                    <div className="sm:col-span-2">
                      <Input placeholder="Название" value={newIngredient.name} onChange={e => setNewIngredient({...newIngredient, name: e.target.value})} />
                    </div>
                    <div>
                      <select 
                        className="w-full h-10 px-3 py-2 border rounded-lg text-sm dark:bg-slate-900 dark:border-slate-800"
                        value={newIngredient.unit}
                        onChange={e => setNewIngredient({...newIngredient, unit: e.target.value})}
                      >
                        <option value="г">г</option>
                        <option value="мл">мл</option>
                        <option value="шт">шт</option>
                      </select>
                    </div>
                    <Input type="number" placeholder="Ккал" value={newIngredient.calories} onChange={e => setNewIngredient({...newIngredient, calories: e.target.value})} />
                    <Input type="number" placeholder="Белки" value={newIngredient.proteins} onChange={e => setNewIngredient({...newIngredient, proteins: e.target.value})} />
                    <Input type="number" placeholder="Жиры" value={newIngredient.fats} onChange={e => setNewIngredient({...newIngredient, fats: e.target.value})} />
                    <Input type="number" placeholder="Угл" value={newIngredient.carbohydrates} onChange={e => setNewIngredient({...newIngredient, carbohydrates: e.target.value})} />
                  </div>
                  <Button onClick={createNewIngredient} className="w-full" size="sm" disabled={!newIngredient.name}>Создать и добавить в список</Button>
                </div>
              )}

              {form.ingredients.map((ing, i) => (
                <div key={i} className="flex gap-2 items-center">
                  <select 
                    className="flex-1 px-3 py-2 border rounded-lg text-sm dark:bg-slate-900 dark:border-slate-800" 
                    value={ing.ingredientId} 
                    onChange={e => { const n = [...form.ingredients]; n[i].ingredientId = parseInt(e.target.value); setForm({...form, ingredients: n}); }}
                  >
                    {ingredients.map(ig => <option key={ig.id} value={ig.id}>{ig.name}</option>)}
                  </select>
                  <div className="flex items-center gap-1">
                    <Input 
                      type="number" 
                      className="w-20" 
                      value={ing.amount} 
                      onChange={e => { const n = [...form.ingredients]; n[i].amount = parseFloat(e.target.value); setForm({...form, ingredients: n}); }} 
                      placeholder="Кол-во" 
                    />
                    <span className="text-xs text-gray-400 font-medium w-8">
                      {ingredients.find(ig => ig.id === ing.ingredientId)?.unit || 'г'}
                    </span>
                  </div>
                  <button onClick={() => setForm({...form, ingredients: form.ingredients.filter((_, j) => j !== i)})} className="text-coral p-2 hover:bg-coral/10 rounded-full">×</button>
                </div>
              ))}
            </div>

            <div className="space-y-2">
              <label className="text-sm font-medium text-gray-700 dark:text-slate-300">Категории</label>
              <div className="flex flex-wrap gap-2">
                {categories.map(c => (
                  <button 
                    key={c.id} 
                    onClick={() => toggleCategory(c.id)} 
                    className={cn(
                      "px-3 py-1 rounded-full text-xs font-semibold transition-all",
                      form.categoryIds.includes(c.id) ? "bg-rose text-white" : "bg-gray-100 text-gray-600 dark:bg-slate-800 dark:text-slate-400"
                    )}
                  >
                    {c.name}
                  </button>
                ))}
              </div>
            </div>

            <Button onClick={createRecipe} className="w-full py-6 text-lg" disabled={!form.title}>
              Отправить на модерацию
            </Button>
          </CardContent>
        </Card>
      )}
    </div>
  );
}

function MacroItem({ label, value, color }: { label: string; value: number; color: string }) {
  return (
    <div>
      <div className="text-[10px] text-gray-400 uppercase font-bold">{label}</div>
      <div className={cn("text-xs font-bold", color)}>{value.toFixed(0)}</div>
    </div>
  );
}
