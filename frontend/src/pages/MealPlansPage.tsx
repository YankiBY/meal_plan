import { useState, useEffect } from 'react';
import api from '../api/client';
import toast from 'react-hot-toast';
import type { MealPlanDto, RecipeDto } from '../types';

export default function MealPlansPage() {
  const [plans, setPlans] = useState<MealPlanDto[]>([]);
  const [showGenerate, setShowGenerate] = useState(false);
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [planName, setPlanName] = useState('');
  const [selectedPlan, setSelectedPlan] = useState<MealPlanDto | null>(null);
  const [recipes, setRecipes] = useState<RecipeDto[]>([]);

  useEffect(() => { loadPlans(); loadRecipes(); }, []);

  const loadPlans = async () => {
    const res = await api.get<MealPlanDto[]>('/meal-plans');
    setPlans(res.data);
  };

  const loadRecipes = async () => {
    const res = await api.get<RecipeDto[]>('/recipes');
    setRecipes(res.data);
  };

  const generatePlan = async () => {
    try {
      const res = await api.post<MealPlanDto>('/meal-plans/generate', { startDate, endDate, name: planName || undefined });
      setPlans([...plans, res.data]);
      setShowGenerate(false);
      setPlanName(''); setStartDate(''); setEndDate('');
      toast.success('План питания сгенерирован');
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Ошибка генерации');
    }
  };

  const deletePlan = async (id: number) => {
    if (!confirm('Удалить план?')) return;
    await api.delete(`/meal-plans/${id}`);
    setPlans(plans.filter(p => p.id !== id));
    if (selectedPlan?.id === id) setSelectedPlan(null);
    toast.success('План удалён');
  };

  const replaceMeal = async (planId: number, mealId: number, recipeId: number) => {
    try {
      const res = await api.put<MealPlanDto>(`/meal-plans/${planId}/meals/${mealId}/replace?recipeId=${recipeId}`);
      setSelectedPlan(res.data);
      setPlans(plans.map(p => p.id === planId ? res.data : p));
      toast.success('Блюдо заменено');
    } catch {
      toast.error('Ошибка замены');
    }
  };

  const exportPdf = (id: number) => {
    window.open(`/api/meal-plans/${id}/export/pdf`, '_blank');
  };

  const exportExcel = (id: number) => {
    window.open(`/api/meal-plans/${id}/export/excel`, '_blank');
  };

  const mealTypeLabel: Record<string, string> = { BREAKFAST: 'Завтрак', LUNCH: 'Обед', DINNER: 'Ужин', SNACK: 'Перекус' };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold text-green-700">Планы питания</h1>
        <button onClick={() => setShowGenerate(!showGenerate)} className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700">
          {showGenerate ? 'Отмена' : 'Создать план'}
        </button>
      </div>

      {showGenerate && (
        <div className="bg-white p-6 rounded-lg shadow space-y-4">
          <h2 className="text-lg font-semibold">Генерация плана питания</h2>
          <div className="grid grid-cols-3 gap-4">
            <div>
              <label className="block text-sm text-gray-600">Название</label>
              <input type="text" value={planName} onChange={e => setPlanName(e.target.value)} className="w-full px-3 py-2 border rounded" placeholder="Мой план" />
            </div>
            <div>
              <label className="block text-sm text-gray-600">Начало</label>
              <input type="date" value={startDate} onChange={e => setStartDate(e.target.value)} className="w-full px-3 py-2 border rounded" required />
            </div>
            <div>
              <label className="block text-sm text-gray-600">Конец</label>
              <input type="date" value={endDate} onChange={e => setEndDate(e.target.value)} className="w-full px-3 py-2 border rounded" required />
            </div>
          </div>
          <button onClick={generatePlan} className="bg-green-600 text-white px-6 py-2 rounded hover:bg-green-700" disabled={!startDate || !endDate}>Сгенерировать</button>
        </div>
      )}

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {plans.map(plan => (
          <div key={plan.id} className={`bg-white p-4 rounded-lg shadow cursor-pointer border-2 ${selectedPlan?.id === plan.id ? 'border-green-500' : 'border-transparent'}`} onClick={() => setSelectedPlan(plan)}>
            <h3 className="font-semibold text-green-700">{plan.name}</h3>
            <p className="text-sm text-gray-500">{plan.startDate} — {plan.endDate}</p>
            <p className="text-sm mt-1">Ккал: {plan.totalCalories?.toFixed(0)} | Б: {plan.totalProteins?.toFixed(0)} | Ж: {plan.totalFats?.toFixed(0)} | У: {plan.totalCarbs?.toFixed(0)}</p>
            <div className="flex gap-2 mt-3">
              <button onClick={e => { e.stopPropagation(); exportPdf(plan.id); }} className="text-xs bg-red-100 text-red-700 px-2 py-1 rounded">PDF</button>
              <button onClick={e => { e.stopPropagation(); exportExcel(plan.id); }} className="text-xs bg-blue-100 text-blue-700 px-2 py-1 rounded">Excel</button>
              <button onClick={e => { e.stopPropagation(); deletePlan(plan.id); }} className="text-xs bg-gray-100 text-gray-700 px-2 py-1 rounded hover:bg-red-100">Удалить</button>
            </div>
          </div>
        ))}
      </div>

      {selectedPlan && (
        <div className="bg-white p-6 rounded-lg shadow">
          <h2 className="text-xl font-semibold text-green-700 mb-4">{selectedPlan.name}</h2>
          {selectedPlan.days?.map(day => (
            <div key={day.id} className="mb-4">
              <h3 className="font-medium text-gray-700 border-b pb-1 mb-2">{day.date} — Ккал: {day.dayCalories?.toFixed(0)}</h3>
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-2">
                {day.meals?.map(meal => (
                  <div key={meal.id} className="bg-gray-50 p-3 rounded">
                    <p className="text-xs text-gray-500 font-medium">{mealTypeLabel[meal.mealType] || meal.mealType}</p>
                    <p className="font-medium text-sm">{meal.recipeTitle}</p>
                    <p className="text-xs text-gray-400">Ккал: {meal.calories?.toFixed(0) || 0}</p>
                    <select className="mt-1 text-xs w-full border rounded p-1" defaultValue="" onChange={e => { if (e.target.value) replaceMeal(selectedPlan.id, meal.id, parseInt(e.target.value)); }}>
                      <option value="">Заменить...</option>
                      {recipes.map(r => <option key={r.id} value={r.id}>{r.title}</option>)}
                    </select>
                  </div>
                ))}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
