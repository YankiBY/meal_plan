import { useState, useEffect } from 'react';
import api from '../api/client';
import toast from 'react-hot-toast';
import type { UserDto, HealthProfileDto, Disease, Allergen } from '../types';

export default function ProfilePage() {
  const [profile, setProfile] = useState<UserDto | null>(null);
  const [health, setHealth] = useState<HealthProfileDto | null>(null);
  const [diseases, setDiseases] = useState<Disease[]>([]);
  const [allergens, setAllergens] = useState<Allergen[]>([]);
  const [editHealth, setEditHealth] = useState(false);
  const [form, setForm] = useState({ weight: '', height: '', age: '', gender: 'MALE', activityLevel: 'MODERATE', diseaseIds: [] as number[], allergenIds: [] as number[] });

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    const [profileRes, healthRes, diseasesRes, allergensRes] = await Promise.all([
      api.get<UserDto>('/profile'),
      api.get<HealthProfileDto>('/profile/health'),
      api.get<Disease[]>('/reference/diseases'),
      api.get<Allergen[]>('/reference/allergens'),
    ]);
    setProfile(profileRes.data);
    setHealth(healthRes.data);
    setDiseases(diseasesRes.data);
    setAllergens(allergensRes.data);

    const h = healthRes.data;
    setForm({
      weight: h.weight?.toString() || '',
      height: h.height?.toString() || '',
      age: h.age?.toString() || '',
      gender: h.gender || 'MALE',
      activityLevel: h.activityLevel || 'MODERATE',
      diseaseIds: h.diseaseIds || [],
      allergenIds: h.allergenIds || [],
    });
  };

  const handleAvatarUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    const fd = new FormData();
    fd.append('file', file);
    try {
      const res = await api.post<UserDto>('/profile/avatar', fd, { headers: { 'Content-Type': 'multipart/form-data' } });
      setProfile(res.data);
      toast.success('Аватар обновлён');
    } catch {
      toast.error('Ошибка загрузки аватара');
    }
  };

  const handleSaveHealth = async () => {
    try {
      const res = await api.put<HealthProfileDto>('/profile/health', {
        weight: parseFloat(form.weight) || null,
        height: parseFloat(form.height) || null,
        age: parseInt(form.age) || null,
        gender: form.gender,
        activityLevel: form.activityLevel,
        diseaseIds: form.diseaseIds,
        allergenIds: form.allergenIds,
      });
      setHealth(res.data);
      setEditHealth(false);
      toast.success('Профиль здоровья обновлён');
    } catch {
      toast.error('Ошибка сохранения');
    }
  };

  const toggleId = (arr: number[], id: number) =>
    arr.includes(id) ? arr.filter(x => x !== id) : [...arr, id];

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-rose">Профиль</h1>

      {profile && (
        <div className="bg-white p-6 rounded-lg shadow flex items-center gap-6">
          <div className="relative">
            {profile.avatarPath ? (
              <img src={profile.avatarPath} alt="Avatar" className="w-24 h-24 rounded-full object-cover border-4 border-olive" />
            ) : (
              <div className="w-24 h-24 rounded-full bg-lime flex items-center justify-center text-3xl text-rose font-bold border-4 border-olive">
                {profile.username[0].toUpperCase()}
              </div>
            )}
            <label className="absolute bottom-0 right-0 bg-olive text-white rounded-full p-1 cursor-pointer hover:bg-olive-dark">
              <svg xmlns="http://www.w3.org/2000/svg" className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z" /><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 13a3 3 0 11-6 0 3 3 0 016 0z" /></svg>
              <input type="file" accept="image/*" className="hidden" onChange={handleAvatarUpload} />
            </label>
          </div>
          <div>
            <h2 className="text-xl font-semibold">{profile.username}</h2>
            <p className="text-gray-500">{profile.email}</p>
            <p className="text-sm text-gray-400">Роли: {profile.roles.join(', ')}</p>
          </div>
        </div>
      )}

      <div className="bg-white p-6 rounded-lg shadow">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-xl font-semibold text-rose">Профиль здоровья</h2>
          <button onClick={() => setEditHealth(!editHealth)} className="text-sm bg-lime/30 text-rose px-3 py-1 rounded hover:bg-lime">
            {editHealth ? 'Отмена' : 'Редактировать'}
          </button>
        </div>

        {!editHealth && health ? (
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <InfoCard label="Вес" value={health.weight ? `${health.weight} кг` : '—'} />
            <InfoCard label="Рост" value={health.height ? `${health.height} см` : '—'} />
            <InfoCard label="Возраст" value={health.age?.toString() || '—'} />
            <InfoCard label="Пол" value={health.gender === 'MALE' ? 'Мужской' : health.gender === 'FEMALE' ? 'Женский' : '—'} />
            <InfoCard label="Калории/день" value={health.dailyCalorieTarget?.toFixed(0) || '—'} accent />
            <InfoCard label="Белки/день" value={health.dailyProteinTarget?.toFixed(0) || '—'} />
            <InfoCard label="Жиры/день" value={health.dailyFatTarget?.toFixed(0) || '—'} />
            <InfoCard label="Углеводы/день" value={health.dailyCarbTarget?.toFixed(0) || '—'} />
            {health.diseaseNames && health.diseaseNames.length > 0 && (
              <div className="col-span-2"><span className="text-sm text-gray-500">Заболевания:</span> <span className="text-sm">{Array.from(health.diseaseNames).join(', ')}</span></div>
            )}
            {health.allergenNames && health.allergenNames.length > 0 && (
              <div className="col-span-2"><span className="text-sm text-gray-500">Аллергены:</span> <span className="text-sm">{Array.from(health.allergenNames).join(', ')}</span></div>
            )}
          </div>
        ) : (
          <div className="space-y-4">
            <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
              <div>
                <label className="block text-sm text-gray-600">Вес (кг)</label>
                <input type="number" value={form.weight} onChange={e => setForm({...form, weight: e.target.value})} className="w-full px-3 py-2 border rounded" />
              </div>
              <div>
                <label className="block text-sm text-gray-600">Рост (см)</label>
                <input type="number" value={form.height} onChange={e => setForm({...form, height: e.target.value})} className="w-full px-3 py-2 border rounded" />
              </div>
              <div>
                <label className="block text-sm text-gray-600">Возраст</label>
                <input type="number" value={form.age} onChange={e => setForm({...form, age: e.target.value})} className="w-full px-3 py-2 border rounded" />
              </div>
              <div>
                <label className="block text-sm text-gray-600">Пол</label>
                <select value={form.gender} onChange={e => setForm({...form, gender: e.target.value})} className="w-full px-3 py-2 border rounded">
                  <option value="MALE">Мужской</option>
                  <option value="FEMALE">Женский</option>
                </select>
              </div>
              <div>
                <label className="block text-sm text-gray-600">Активность</label>
                <select value={form.activityLevel} onChange={e => setForm({...form, activityLevel: e.target.value})} className="w-full px-3 py-2 border rounded">
                  <option value="SEDENTARY">Сидячий</option>
                  <option value="LIGHT">Лёгкая</option>
                  <option value="MODERATE">Умеренная</option>
                  <option value="ACTIVE">Активная</option>
                  <option value="VERY_ACTIVE">Очень активная</option>
                </select>
              </div>
            </div>
            <div>
              <label className="block text-sm text-gray-600 mb-1">Заболевания</label>
              <div className="flex flex-wrap gap-2">
                {diseases.map(d => (
                  <button key={d.id} onClick={() => setForm({...form, diseaseIds: toggleId(form.diseaseIds, d.id)})}
                    className={`px-3 py-1 rounded text-sm ${form.diseaseIds.includes(d.id) ? 'bg-coral text-white' : 'bg-gray-100 text-gray-700'}`}>
                    {d.name}
                  </button>
                ))}
              </div>
            </div>
            <div>
              <label className="block text-sm text-gray-600 mb-1">Аллергены</label>
              <div className="flex flex-wrap gap-2">
                {allergens.map(a => (
                  <button key={a.id} onClick={() => setForm({...form, allergenIds: toggleId(form.allergenIds, a.id)})}
                    className={`px-3 py-1 rounded text-sm ${form.allergenIds.includes(a.id) ? 'bg-orange-500 text-white' : 'bg-gray-100 text-gray-700'}`}>
                    {a.name}
                  </button>
                ))}
              </div>
            </div>
            <button onClick={handleSaveHealth} className="bg-olive text-white px-6 py-2 rounded hover:bg-olive-dark">Сохранить</button>
          </div>
        )}
      </div>
    </div>
  );
}

function InfoCard({ label, value, accent }: { label: string; value: string; accent?: boolean }) {
  return (
    <div className={`p-3 rounded-lg ${accent ? 'bg-lime/20 border border-lime-dark' : 'bg-gray-50'}`}>
      <p className="text-xs text-gray-500">{label}</p>
      <p className={`text-lg font-semibold ${accent ? 'text-rose' : ''}`}>{value}</p>
    </div>
  );
}
