import { useState, useEffect } from 'react';
import api from '../api/client';
import toast from 'react-hot-toast';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import type { ProgressDto } from '../types';
import Button from '../components/ui/Button';
import Input from '../components/ui/Input';
import { Card, CardContent, CardHeader, CardTitle } from '../components/ui/Card';
import ProgressCalendar from '../components/ProgressCalendar';

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
        <div>
          <h1 className="text-2xl font-semibold text-gray-900">Прогресс</h1>
          <p className="text-sm text-gray-500">Календарь, графики и история выполнения</p>
        </div>
        <Button onClick={() => setShowForm(!showForm)}>
          {showForm ? 'Отмена' : 'Добавить запись'}
        </Button>
      </div>

      {showForm && (
        <Card>
          <CardHeader>
            <CardTitle className="text-rose">Новая запись</CardTitle>
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
              <div className="flex items-end">
                <label className="flex items-center gap-2 text-sm text-gray-700">
                  <input
                    type="checkbox"
                    checked={form.planComplied}
                    onChange={(e) => setForm({ ...form, planComplied: e.target.checked })}
                  />
                  План выполнен
                </label>
              </div>
            </div>
            <Button onClick={addProgress} className="w-full sm:w-auto">
              Сохранить
            </Button>
          </CardContent>
        </Card>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <ProgressCalendar />

        {chartData.length > 0 && (
          <Card>
            <CardHeader>
              <CardTitle className="text-rose">График веса</CardTitle>
            </CardHeader>
            <CardContent>
              <ResponsiveContainer width="100%" height={300}>
                <LineChart data={chartData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="date" tick={{ fontSize: 12 }} />
                  <YAxis />
                  <Tooltip />
                  <Legend />
                  <Line type="monotone" dataKey="weight" stroke="#b3cc57" name="Вес (кг)" />
                </LineChart>
              </ResponsiveContainer>
            </CardContent>
          </Card>
        )}

        {chartData.length > 0 && (
          <Card className="lg:col-span-2">
            <CardHeader>
              <CardTitle className="text-rose">Потребление БЖУ</CardTitle>
            </CardHeader>
            <CardContent>
              <ResponsiveContainer width="100%" height={300}>
                <LineChart data={chartData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="date" tick={{ fontSize: 12 }} />
                  <YAxis />
                  <Tooltip />
                  <Legend />
                  <Line type="monotone" dataKey="calories" stroke="#ef746f" name="Калории" />
                  <Line type="monotone" dataKey="proteins" stroke="#3b82f6" name="Белки" />
                  <Line type="monotone" dataKey="fats" stroke="#ffbe40" name="Жиры" />
                  <Line type="monotone" dataKey="carbs" stroke="#8b5cf6" name="Углеводы" />
                </LineChart>
              </ResponsiveContainer>
            </CardContent>
          </Card>
        )}
      </div>

      <Card>
        <CardHeader className="flex-row items-center justify-between">
          <CardTitle className="text-rose">История записей</CardTitle>
          <div className="text-xs text-gray-500">{progress.length} записей</div>
        </CardHeader>
        <CardContent className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-gray-50 text-gray-600">
                <th className="p-2 text-left font-medium">Дата</th>
                <th className="p-2 font-medium">Вес</th>
                <th className="p-2 font-medium">Ккал</th>
                <th className="p-2 font-medium">Б</th>
                <th className="p-2 font-medium">Ж</th>
                <th className="p-2 font-medium">У</th>
                <th className="p-2 font-medium">План</th>
                <th className="p-2" />
              </tr>
            </thead>
            <tbody>
              {progress
                .slice()
                .sort((a, b) => b.date.localeCompare(a.date))
                .map((p) => (
                  <tr key={p.id} className="border-t">
                    <td className="p-2">{p.date}</td>
                    <td className="p-2 text-center">{p.weight ?? '—'}</td>
                    <td className="p-2 text-center">{p.caloriesConsumed ?? '—'}</td>
                    <td className="p-2 text-center">{p.proteinsConsumed ?? '—'}</td>
                    <td className="p-2 text-center">{p.fatsConsumed ?? '—'}</td>
                    <td className="p-2 text-center">{p.carbsConsumed ?? '—'}</td>
                    <td className="p-2 text-center">{p.planComplied ? 'Да' : 'Нет'}</td>
                    <td className="p-2 text-right">
                      <Button variant="ghost" size="sm" onClick={() => deleteProgress(p.id!)} className="text-coral-dark">
                        Удалить
                      </Button>
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

function Field({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div className="space-y-1">
      <div className="text-sm font-medium text-gray-700">{label}</div>
      {children}
    </div>
  );
}
