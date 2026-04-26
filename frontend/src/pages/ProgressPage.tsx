import { useState, useEffect } from 'react';
import api from '../api/client';
import toast from 'react-hot-toast';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import type { ProgressDto } from '../types';

export default function ProgressPage() {
  const [progress, setProgress] = useState<ProgressDto[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ date: new Date().toISOString().split('T')[0], weight: '', caloriesConsumed: '', proteinsConsumed: '', fatsConsumed: '', carbsConsumed: '', planComplied: false });

  useEffect(() => { loadProgress(); }, []);

  const loadProgress = async () => {
    const res = await api.get<ProgressDto[]>('/progress');
    setProgress(res.data);
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
      loadProgress();
    } catch {
      toast.error('Ошибка');
    }
  };

  const deleteProgress = async (id: number) => {
    await api.delete(`/progress/${id}`);
    toast.success('Запись удалена');
    loadProgress();
  };

  const chartData = progress.map(p => ({
    date: p.date,
    weight: p.weight,
    calories: p.caloriesConsumed,
    proteins: p.proteinsConsumed,
    fats: p.fatsConsumed,
    carbs: p.carbsConsumed,
  })).sort((a, b) => a.date.localeCompare(b.date));

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold text-green-700">Прогресс</h1>
        <button onClick={() => setShowForm(!showForm)} className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700">
          {showForm ? 'Отмена' : 'Добавить запись'}
        </button>
      </div>

      {showForm && (
        <div className="bg-white p-6 rounded-lg shadow space-y-4">
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div><label className="text-sm text-gray-600">Дата</label><input type="date" className="w-full px-3 py-2 border rounded" value={form.date} onChange={e => setForm({...form, date: e.target.value})} /></div>
            <div><label className="text-sm text-gray-600">Вес (кг)</label><input type="number" className="w-full px-3 py-2 border rounded" value={form.weight} onChange={e => setForm({...form, weight: e.target.value})} /></div>
            <div><label className="text-sm text-gray-600">Калории</label><input type="number" className="w-full px-3 py-2 border rounded" value={form.caloriesConsumed} onChange={e => setForm({...form, caloriesConsumed: e.target.value})} /></div>
            <div><label className="text-sm text-gray-600">Белки (г)</label><input type="number" className="w-full px-3 py-2 border rounded" value={form.proteinsConsumed} onChange={e => setForm({...form, proteinsConsumed: e.target.value})} /></div>
            <div><label className="text-sm text-gray-600">Жиры (г)</label><input type="number" className="w-full px-3 py-2 border rounded" value={form.fatsConsumed} onChange={e => setForm({...form, fatsConsumed: e.target.value})} /></div>
            <div><label className="text-sm text-gray-600">Углеводы (г)</label><input type="number" className="w-full px-3 py-2 border rounded" value={form.carbsConsumed} onChange={e => setForm({...form, carbsConsumed: e.target.value})} /></div>
            <div className="flex items-end"><label className="flex items-center gap-2"><input type="checkbox" checked={form.planComplied} onChange={e => setForm({...form, planComplied: e.target.checked})} /> <span className="text-sm">План выполнен</span></label></div>
          </div>
          <button onClick={addProgress} className="bg-green-600 text-white px-6 py-2 rounded hover:bg-green-700">Сохранить</button>
        </div>
      )}

      {chartData.length > 0 && (
        <div className="bg-white p-6 rounded-lg shadow">
          <h2 className="text-lg font-semibold text-green-700 mb-4">График веса</h2>
          <ResponsiveContainer width="100%" height={300}>
            <LineChart data={chartData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="date" tick={{ fontSize: 12 }} />
              <YAxis />
              <Tooltip />
              <Legend />
              <Line type="monotone" dataKey="weight" stroke="#16a34a" name="Вес (кг)" />
            </LineChart>
          </ResponsiveContainer>
        </div>
      )}

      {chartData.length > 0 && (
        <div className="bg-white p-6 rounded-lg shadow">
          <h2 className="text-lg font-semibold text-green-700 mb-4">Потребление БЖУ</h2>
          <ResponsiveContainer width="100%" height={300}>
            <LineChart data={chartData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="date" tick={{ fontSize: 12 }} />
              <YAxis />
              <Tooltip />
              <Legend />
              <Line type="monotone" dataKey="calories" stroke="#ef4444" name="Калории" />
              <Line type="monotone" dataKey="proteins" stroke="#3b82f6" name="Белки" />
              <Line type="monotone" dataKey="fats" stroke="#f59e0b" name="Жиры" />
              <Line type="monotone" dataKey="carbs" stroke="#8b5cf6" name="Углеводы" />
            </LineChart>
          </ResponsiveContainer>
        </div>
      )}

      <div className="bg-white p-6 rounded-lg shadow">
        <h2 className="text-lg font-semibold text-green-700 mb-4">История записей</h2>
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead><tr className="bg-gray-50"><th className="p-2 text-left">Дата</th><th className="p-2">Вес</th><th className="p-2">Ккал</th><th className="p-2">Б</th><th className="p-2">Ж</th><th className="p-2">У</th><th className="p-2">План</th><th className="p-2"></th></tr></thead>
            <tbody>
              {progress.sort((a, b) => b.date.localeCompare(a.date)).map(p => (
                <tr key={p.id} className="border-t">
                  <td className="p-2">{p.date}</td>
                  <td className="p-2 text-center">{p.weight ?? '—'}</td>
                  <td className="p-2 text-center">{p.caloriesConsumed ?? '—'}</td>
                  <td className="p-2 text-center">{p.proteinsConsumed ?? '—'}</td>
                  <td className="p-2 text-center">{p.fatsConsumed ?? '—'}</td>
                  <td className="p-2 text-center">{p.carbsConsumed ?? '—'}</td>
                  <td className="p-2 text-center">{p.planComplied ? 'Да' : 'Нет'}</td>
                  <td className="p-2"><button onClick={() => deleteProgress(p.id!)} className="text-red-500 text-xs">Удалить</button></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
