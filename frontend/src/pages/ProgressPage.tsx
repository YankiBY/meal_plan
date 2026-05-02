import { useState, useEffect } from 'react';
import api from '../api/client';
import toast from 'react-hot-toast';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import type { ProgressDto } from '../types';
import { Card, CardContent, CardHeader, CardTitle } from '../components/ui/Card';
import ProgressCalendar from '../components/ProgressCalendar';

export default function ProgressPage() {
  const [progress, setProgress] = useState<ProgressDto[]>([]);

  useEffect(() => { loadProgress(); }, []);

  const loadProgress = async () => {
    const res = await api.get<ProgressDto[]>('/progress');
    setProgress(res.data);
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
    <div className="space-y-6 animate-fade-in">
      <div>
        <h1 className="text-3xl font-bold text-gray-900 dark:text-slate-100">Прогресс</h1>
        <p className="text-gray-500 mt-1">Визуализация ваших достижений и календарь активности</p>
      </div>

      <ProgressCalendar />

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {chartData.length > 0 && (
          <Card>
            <CardHeader>
              <CardTitle className="text-rose">Потребление калорий</CardTitle>
            </CardHeader>
            <CardContent>
              <ResponsiveContainer width="100%" height={300}>
                <LineChart data={chartData}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
                  <XAxis dataKey="date" tick={{ fontSize: 10 }} stroke="#94a3b8" />
                  <YAxis tick={{ fontSize: 10 }} stroke="#94a3b8" />
                  <Tooltip 
                    contentStyle={{ borderRadius: '12px', border: 'none', boxShadow: '0 10px 15px -3px rgb(0 0 0 / 0.1)' }}
                  />
                  <Legend />
                  <Line type="monotone" dataKey="calories" stroke="#ef746f" strokeWidth={3} dot={{ r: 4 }} activeDot={{ r: 6 }} name="Калории (ккал)" />
                </LineChart>
              </ResponsiveContainer>
            </CardContent>
          </Card>
        )}

        {chartData.length > 0 && (
          <Card>
            <CardHeader>
              <CardTitle className="text-rose">Потребление БЖУ</CardTitle>
            </CardHeader>
            <CardContent>
              <ResponsiveContainer width="100%" height={300}>
                <LineChart data={chartData}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
                  <XAxis dataKey="date" tick={{ fontSize: 10 }} stroke="#94a3b8" />
                  <YAxis tick={{ fontSize: 10 }} stroke="#94a3b8" />
                  <Tooltip 
                    contentStyle={{ borderRadius: '12px', border: 'none', boxShadow: '0 10px 15px -3px rgb(0 0 0 / 0.1)' }}
                  />
                  <Legend />
                  <Line type="monotone" dataKey="proteins" stroke="#3b82f6" strokeWidth={2} name="Белки (г)" />
                  <Line type="monotone" dataKey="fats" stroke="#ffbe40" strokeWidth={2} name="Жиры (г)" />
                  <Line type="monotone" dataKey="carbs" stroke="#8b5cf6" strokeWidth={2} name="Углеводы (г)" />
                </LineChart>
              </ResponsiveContainer>
            </CardContent>
          </Card>
        )}
      </div>
      
      {!chartData.length && (
        <Card className="p-12 text-center border-dashed">
          <p className="text-gray-500">Данные для графиков появятся после добавления записей во вкладке "Планы питания"</p>
        </Card>
      )}
    </div>
  );
}
