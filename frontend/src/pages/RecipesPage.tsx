import { useState, useEffect } from 'react';
import api from '../api/client';
import toast from 'react-hot-toast';
import type { RecipeDto, Ingredient, Category } from '../types';

export default function RecipesPage() {
  const [recipes, setRecipes] = useState<RecipeDto[]>([]);
  const [myRecipes, setMyRecipes] = useState<RecipeDto[]>([]);
  const [tab, setTab] = useState<'all' | 'my' | 'create'>('all');
  const [ingredients, setIngredients] = useState<Ingredient[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [form, setForm] = useState({ title: '', description: '', instructions: '', prepTime: '', cookTime: '', ingredients: [] as { ingredientId: number; amount: number }[], categoryIds: [] as number[] });
  const [selected, setSelected] = useState<RecipeDto | null>(null);
  const [search, setSearch] = useState('');

  useEffect(() => { loadData(); }, []);

  const loadData = async () => {
    const [recipesRes, myRes, ingRes, catRes] = await Promise.all([
      api.get<RecipeDto[]>('/recipes'),
      api.get<RecipeDto[]>('/recipes/my'),
      api.get<Ingredient[]>('/reference/ingredients'),
      api.get<Category[]>('/reference/categories'),
    ]);
    setRecipes(recipesRes.data);
    setMyRecipes(myRes.data);
    setIngredients(ingRes.data);
    setCategories(catRes.data);
  };

  const createRecipe = async () => {
    try {
      await api.post('/recipes', {
        ...form,
        prepTime: parseInt(form.prepTime) || 0,
        cookTime: parseInt(form.cookTime) || 0,
      });
      toast.success('Рецепт отправлен на модерацию');
      setForm({ title: '', description: '', instructions: '', prepTime: '', cookTime: '', ingredients: [], categoryIds: [] });
      setTab('my');
      loadData();
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Ошибка');
    }
  };

  const deleteRecipe = async (id: number) => {
    if (!confirm('Удалить рецепт?')) return;
    await api.delete(`/recipes/${id}`);
    toast.success('Рецепт удалён');
    loadData();
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

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <h1 className="text-2xl font-bold text-rose">Рецепты</h1>
        <div className="flex gap-2">
          {(['all', 'my', 'create'] as const).map(t => (
            <button key={t} onClick={() => { setTab(t); setSelected(null); }}
              className={`px-3 py-1 rounded text-sm ${tab === t ? 'bg-olive text-white' : 'bg-gray-100'}`}>
              {t === 'all' ? 'Все' : t === 'my' ? 'Мои' : 'Создать'}
            </button>
          ))}
        </div>
      </div>

      {tab !== 'create' && (
        <div className="grid grid-cols-1 md:grid-cols-4 gap-3">
          <div className="bg-white p-4 rounded-lg shadow"><p className="text-xs text-gray-500">Найдено рецептов</p><p className="text-xl font-semibold">{currentList.length}</p></div>
          <div className="bg-white p-4 rounded-lg shadow"><p className="text-xs text-gray-500">На модерации</p><p className="text-xl font-semibold">{myRecipes.filter(r => !r.moderated).length}</p></div>
          <div className="md:col-span-2 bg-white p-4 rounded-lg shadow">
            <label className="text-xs text-gray-500 block mb-1">Поиск по названию/описанию</label>
            <input value={search} onChange={e => setSearch(e.target.value)} className="w-full px-3 py-2 border rounded" placeholder="Введите текст..." />
          </div>
        </div>
      )}

      {tab === 'create' && (
        <div className="bg-white p-6 rounded-lg shadow space-y-4">
          <h2 className="text-lg font-semibold">Новый рецепт</h2>
          <div className="grid grid-cols-2 gap-4">
            <div className="col-span-2"><label className="text-sm text-gray-600">Название</label><input className="w-full px-3 py-2 border rounded" value={form.title} onChange={e => setForm({...form, title: e.target.value})} /></div>
            <div className="col-span-2"><label className="text-sm text-gray-600">Описание</label><textarea className="w-full px-3 py-2 border rounded" value={form.description} onChange={e => setForm({...form, description: e.target.value})} /></div>
            <div className="col-span-2"><label className="text-sm text-gray-600">Инструкция</label><textarea className="w-full px-3 py-2 border rounded" rows={4} value={form.instructions} onChange={e => setForm({...form, instructions: e.target.value})} /></div>
            <div><label className="text-sm text-gray-600">Время подготовки (мин)</label><input type="number" className="w-full px-3 py-2 border rounded" value={form.prepTime} onChange={e => setForm({...form, prepTime: e.target.value})} /></div>
            <div><label className="text-sm text-gray-600">Время готовки (мин)</label><input type="number" className="w-full px-3 py-2 border rounded" value={form.cookTime} onChange={e => setForm({...form, cookTime: e.target.value})} /></div>
          </div>

          <div>
            <div className="flex justify-between items-center"><label className="text-sm text-gray-600">Ингредиенты</label><button onClick={addIngredient} className="text-sm text-olive">+ Добавить</button></div>
            {form.ingredients.map((ing, i) => (
              <div key={i} className="flex gap-2 mt-1">
                <select className="flex-1 px-2 py-1 border rounded text-sm" value={ing.ingredientId} onChange={e => { const n = [...form.ingredients]; n[i].ingredientId = parseInt(e.target.value); setForm({...form, ingredients: n}); }}>
                  {ingredients.map(ig => <option key={ig.id} value={ig.id}>{ig.name}</option>)}
                </select>
                <input type="number" className="w-24 px-2 py-1 border rounded text-sm" value={ing.amount} onChange={e => { const n = [...form.ingredients]; n[i].amount = parseFloat(e.target.value); setForm({...form, ingredients: n}); }} placeholder="г" />
                <button onClick={() => setForm({...form, ingredients: form.ingredients.filter((_, j) => j !== i)})} className="text-coral text-sm">x</button>
              </div>
            ))}
          </div>

          <div>
            <label className="text-sm text-gray-600">Категории</label>
            <div className="flex flex-wrap gap-2 mt-1">
              {categories.map(c => (
                <button key={c.id} onClick={() => toggleCategory(c.id)} className={`px-3 py-1 rounded text-sm ${form.categoryIds.includes(c.id) ? 'bg-olive text-white' : 'bg-gray-100'}`}>{c.name}</button>
              ))}
            </div>
          </div>

          <button onClick={createRecipe} className="bg-olive text-white px-6 py-2 rounded hover:bg-olive-dark" disabled={!form.title}>Создать рецепт</button>
        </div>
      )}

      {tab !== 'create' && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {currentList.map(recipe => (
            <div key={recipe.id} className={`bg-white p-4 rounded-lg shadow cursor-pointer border-2 ${selected?.id === recipe.id ? 'border-olive' : 'border-transparent'}`} onClick={() => setSelected(recipe)}>
              <h3 className="font-semibold text-rose">{recipe.title}</h3>
              <p className="text-sm text-gray-500 line-clamp-2">{recipe.description}</p>
              <p className="text-xs text-gray-400 mt-1">Ккал: {recipe.totalCalories?.toFixed(0)} | Б: {recipe.totalProteins?.toFixed(0)} | Ж: {recipe.totalFats?.toFixed(0)} | У: {recipe.totalCarbohydrates?.toFixed(0)}</p>
              {!recipe.moderated && <span className="text-xs bg-yellow-100 text-yellow-700 px-2 py-0.5 rounded mt-1 inline-block">На модерации</span>}
              {tab === 'my' && <button onClick={e => { e.stopPropagation(); deleteRecipe(recipe.id); }} className="text-xs text-coral mt-1 block">Удалить</button>}
            </div>
          ))}
        </div>
      )}

      {selected && (
        <div className="bg-white p-6 rounded-lg shadow">
          <h2 className="text-xl font-semibold text-rose">{selected.title}</h2>
          <p className="text-gray-600 mt-2">{selected.description}</p>
          <p className="mt-3 text-sm whitespace-pre-wrap">{selected.instructions}</p>
          {selected.ingredients && selected.ingredients.length > 0 && (
            <div className="mt-4">
              <h3 className="font-medium text-sm text-gray-600">Ингредиенты:</h3>
              <ul className="list-disc list-inside text-sm">
                {selected.ingredients.map((ing, i) => (
                  <li key={i}>{ing.ingredientName} — {ing.amount} {ing.unit}</li>
                ))}
              </ul>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
