import { useState, useEffect } from 'react';
import api from '../api/client';
import toast from 'react-hot-toast';
import type { MealPlanDto, RecipeDto, ProgressDto, HealthProfileDto } from '../types';
import { Card, CardContent, CardHeader, CardTitle } from '../components/ui/Card';
import Button from '../components/ui/Button';
import Input from '../components/ui/Input';
import { cn } from '../components/ui/cn';
import { useNavigate } from 'react-router-dom';

export default function MealPlansPage() {
  const [progress, setProgress] = useState<ProgressDto[]>([]);
  const [profile, setProfile] = useState<HealthProfileDto | null>(null);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ 
    date: new Date().toISOString().split('T')[0], 
    weight: '', 
    caloriesConsumed: '', 
    proteinsConsumed: '', 
    fatsConsumed: '', 
    carbsConsumed: '', 
    planComplied: false 
  });
  
  const navigate = useNavigate();

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    const [progRes, profRes] = await Promise.all([
      api.get<ProgressDto[]>('/progress'),
      api.get<HealthProfileDto>('/profile/health')
    ]);
    setProgress(progRes.data);
    setProfile(profRes.data);
  };

  const addProgress = async () => {
    try {
      await api.post('/progress', {
        date: form.date,
        weight: parseFloat(form.weight) || null,
        caloriesConsumed: parseFloat(form.caloriesConsumed) || null,
        proteinsConsumed: parseFloat(form.proteinsConsumed) || null,
        fatsConsumed: parseFloat(form.fatsConsumed) || null,
        carbsConsumed: parseFloat(form.carbsConsumed) || null,
        planComplied: form.planComplied,
      });
      toast.success('Запись добавлена');
      setShowForm(false);
      loadData();
    } catch {
      toast.error('Ошибка');
    }
  };

  const deleteProgress = async (id: number) => {
    if (!confirm('Удалить запись?')) return;
    await api.delete(`/progress/${id}`);
    toast.success('Запись удалена');
    loadData();
  };

  const todayProgress = progress.find(p => p.date === new Date().toISOString().split('T')[0]);

  return (
    <div className="space-y-8 animate-fade-in">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold text-gray-900 dark:text-slate-100">Планы питания</h1>
          <p className="text-gray-500 mt-1">Отслеживайте приемы пищи и прогресс здесь</p>
        </div>
        <Button onClick={() => setShowForm(!showForm)}>
          {showForm ? 'Отмена' : 'Добавить запись прогресса'}
        </Button>
      </div>

      {/* Трекер БЖУ */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <NutritionCard 
          label="Калории" 
          current={todayProgress?.caloriesConsumed || 0} 
          target={profile?.dailyCalorieTarget || 0} 
          unit="ккал" 
          color="bg-rose" 
        />
        <NutritionCard 
          label="Белки" 
          current={todayProgress?.proteinsConsumed || 0} 
          target={profile?.dailyProteinTarget || 0} 
          unit="г" 
          color="bg-blue-500" 
        />
        <NutritionCard 
          label="Жиры" 
          current={todayProgress?.fatsConsumed || 0} 
          target={profile?.dailyFatTarget || 0} 
          unit="г" 
          color="bg-amber-500" 
        />
        <NutritionCard 
          label="Углеводы" 
          current={todayProgress?.carbsConsumed || 0} 
          target={profile?.dailyCarbTarget || 0} 
          unit="г" 
          color="bg-purple-500" 
        />
      </div>

      {/* Кнопки приемов пищи */}
      <Card className="border-none shadow-sm ring-1 ring-gray-200 dark:ring-slate-800">
        <CardHeader>
          <CardTitle className="text-xl">Приемы пищи на сегодня</CardTitle>
        </CardHeader>
        <CardContent className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          <MealButton label="Завтрак" onClick={() => navigate('/meal-plans/select-recipe?type=завтрак')} />
          <MealButton label="Обед" onClick={() => navigate('/meal-plans/select-recipe?type=обед')} />
          <MealButton label="Ужин" onClick={() => navigate('/meal-plans/select-recipe?type=ужин')} />
          <MealButton label="Перекус" onClick={() => navigate('/meal-plans/select-recipe?type=перекус')} />
        </CardContent>
      </Card>

      {/* Форма добавления прогресса */}
      {showForm && (
        <Card className="border-rose/20 shadow-lg">
          <CardHeader>
            <CardTitle className="text-rose">Новая запись прогресса</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">
              <Field label="Дата">
                <Input type="date" value={form.date} onChange={(e) => setForm({ ...form, date: e.target.value })} />
              </Field>
              <Field label="Вес (кг)">
                <Input type="number" value={form.weight} onChange={(e) => setForm({ ...form, weight: e.target.value })} />
              </Field>
              <Field label="Калории">
                <Input type="number" value={form.caloriesConsumed} onChange={(e) => setForm({ ...form, caloriesConsumed: e.target.value })} />
              </Field>
              <Field label="Белки (г)">
                <Input type="number" value={form.proteinsConsumed} onChange={(e) => setForm({ ...form, proteinsConsumed: e.target.value })} />
              </Field>
              <Field label="Жиры (г)">
                <Input type="number" value={form.fatsConsumed} onChange={(e) => setForm({ ...form, fatsConsumed: e.target.value })} />
              </Field>
              <Field label="Углеводы (г)">
                <Input type="number" value={form.carbsConsumed} onChange={(e) => setForm({ ...form, carbsConsumed: e.target.value })} />
              </Field>
              <div className="flex items-end pb-1">
                <label className="flex items-center gap-2 text-sm text-gray-700 dark:text-slate-300 cursor-pointer">
                  <input 
                    type="checkbox" 
                    className="rounded border-gray-300 text-rose focus:ring-rose"
                    checked={form.planComplied} 
                    onChange={(e) => setForm({ ...form, planComplied: e.target.checked })} 
                  />
                  План соблюден
                </label>
              </div>
            </div>
            <div className="flex justify-end gap-3 pt-2">
              <Button variant="secondary" onClick={() => setShowForm(false)}>Отмена</Button>
              <Button onClick={addProgress}>Сохранить</Button>
            </div>
          </CardContent>
        </Card>
      )}

      {/* История записей */}
      <Card className="border-none shadow-sm ring-1 ring-gray-200 dark:ring-slate-800">
        <CardHeader className="flex-row items-center justify-between">
          <CardTitle className="text-xl">История записей</CardTitle>
          <div className="text-xs text-gray-500 font-medium uppercase tracking-wider">{progress.length} записей всего</div>
        </CardHeader>
        <CardContent className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-gray-50 dark:bg-slate-800/50 text-gray-600 dark:text-slate-400">
                <th className="p-3 text-left font-bold">Дата</th>
                <th className="p-3 font-bold">Вес</th>
                <th className="p-3 font-bold">Ккал</th>
                <th className="p-3 font-bold">Б</th>
                <th className="p-3 font-bold">Ж</th>
                <th className="p-3 font-bold">У</th>
                <th className="p-3 font-bold">План</th>
                <th className="p-3" />
              </tr>
            </thead>
            <tbody>
              {progress
                .slice()
                .sort((a, b) => b.date.localeCompare(a.date))
                .map((p) => (
                  <tr key={p.id} className="border-t border-gray-100 dark:border-slate-800 hover:bg-gray-50 dark:hover:bg-slate-800/30 transition-colors">
                    <td className="p-3 font-medium">{p.date}</td>
                    <td className="p-3 text-center">{p.weight ?? '—'}</td>
                    <td className="p-3 text-center">{p.caloriesConsumed ?? '—'}</td>
                    <td className="p-3 text-center">{p.proteinsConsumed ?? '—'}</td>
                    <td className="p-3 text-center">{p.fatsConsumed ?? '—'}</td>
                    <td className="p-3 text-center">{p.carbsConsumed ?? '—'}</td>
                    <td className="p-3 text-center">
                      <span className={cn(
                        "px-2 py-0.5 rounded-full text-[10px] font-bold uppercase",
                        p.planComplied ? "bg-green-100 text-green-700" : "bg-gray-100 text-gray-500"
                      )}>
                        {p.planComplied ? 'Да' : 'Нет'}
                      </span>
                    </td>
                    <td className="p-3 text-right">
                      <button 
                        onClick={() => deleteProgress(p.id!)} 
                        className="text-coral hover:text-coral-dark text-xs font-bold transition-colors"
                      >
                        Удалить
                      </button>
                    </td>
                  </tr>
                ))}
            </tbody>
          </table>
        </CardContent>
      </Card>
    </div>
  );
}

function NutritionCard({ label, current, target, unit, color }: { label: string; current: number; target: number; unit: string; color: string }) {
  const percent = Math.min(100, (current / target) * 100);
  return (
    <Card className="border-none shadow-sm ring-1 ring-gray-200 dark:ring-slate-800 overflow-hidden">
      <CardContent className="p-4 space-y-3">
        <div className="flex justify-between items-end">
          <div className="text-xs font-bold text-gray-400 uppercase tracking-wider">{label}</div>
          <div className="text-right">
            <span className="text-lg font-bold text-gray-900 dark:text-slate-100">{current.toFixed(0)}</span>
            <span className="text-xs text-gray-400"> / {target.toFixed(0)} {unit}</span>
          </div>
        </div>
        <div className="h-2 w-full bg-gray-100 dark:bg-slate-800 rounded-full overflow-hidden">
          <div className={cn("h-full transition-all duration-500", color)} style={{ width: `${percent}%` }} />
        </div>
      </CardContent>
    </Card>
  );
}

function MealButton({ label, onClick }: { label: string; onClick: () => void }) {
  return (
    <button 
      onClick={onClick}
      className="group flex flex-col items-center justify-center p-6 rounded-2xl bg-gray-50 dark:bg-slate-800/50 hover:bg-rose hover:text-white transition-all duration-300 ring-1 ring-gray-200 dark:ring-slate-800 hover:ring-rose shadow-sm"
    >
      <div className="w-12 h-12 rounded-full bg-rose/10 group-hover:bg-white/20 flex items-center justify-center mb-3">
        <span className="text-rose group-hover:text-white text-xl">+</span>
      </div>
      <span className="font-bold tracking-wide uppercase text-xs">{label}</span>
    </button>
  );
}

function Field({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div className="space-y-1">
      <div className="text-xs font-bold text-gray-500 uppercase tracking-wider ml-1">{label}</div>
      {children}
    </div>
  );
}
