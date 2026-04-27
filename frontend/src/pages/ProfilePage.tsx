import { useState, useEffect } from 'react';
import api from '../api/client';
import toast from 'react-hot-toast';
import type { UserDto, HealthProfileDto, Disease, Allergen, NutritionExplanationDto } from '../types';
import Button from '../components/ui/Button';
import Input from '../components/ui/Input';
import Badge from '../components/ui/Badge';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/Card';

const EMPTY_HEALTH: HealthProfileDto = {
  id: 0,
  weight: null,
  height: null,
  age: null,
  gender: null,
  activityLevel: null,
  dailyCalorieTarget: null,
  dailyProteinTarget: null,
  dailyFatTarget: null,
  dailyCarbTarget: null,
  diseaseIds: [],
  allergenIds: [],
  diseaseNames: [],
  allergenNames: [],
};

type FormState = { weight: string; height: string; age: string; gender: string; activityLevel: string; diseaseIds: number[]; allergenIds: number[] };

const healthToForm = (h: HealthProfileDto): FormState => ({
  weight: h.weight?.toString() ?? '',
  height: h.height?.toString() ?? '',
  age: h.age?.toString() ?? '',
  gender: h.gender ?? 'MALE',
  activityLevel: h.activityLevel ?? 'MODERATE',
  diseaseIds: Array.from(h.diseaseIds ?? []),
  allergenIds: Array.from(h.allergenIds ?? []),
});

export default function ProfilePage() {
  const [profile, setProfile] = useState<UserDto | null>(null);
  const [health, setHealth] = useState<HealthProfileDto>(EMPTY_HEALTH);
  const [explanation, setExplanation] = useState<NutritionExplanationDto | null>(null);
  const [diseases, setDiseases] = useState<Disease[]>([]);
  const [allergens, setAllergens] = useState<Allergen[]>([]);
  const [editHealth, setEditHealth] = useState(false);
  const [loading, setLoading] = useState(true);
  const [newDisease, setNewDisease] = useState('');
  const [newAllergen, setNewAllergen] = useState('');
  const [form, setForm] = useState<FormState>(healthToForm(EMPTY_HEALTH));

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setLoading(true);
    try {
      const [profileRes, healthRes, diseasesRes, allergensRes] = await Promise.all([
        api.get<UserDto>('/profile'),
        api.get<HealthProfileDto>('/profile/health'),
        api.get<Disease[]>('/reference/diseases'),
        api.get<Allergen[]>('/reference/allergens'),
      ]);
      setProfile(profileRes.data);
      const h = healthRes.data ?? EMPTY_HEALTH;
      setHealth(h);
      setDiseases(diseasesRes.data);
      setAllergens(allergensRes.data);
      setForm(healthToForm(h));

      try {
        const explRes = await api.get<NutritionExplanationDto>('/profile/health/explanation');
        setExplanation(explRes.data);
      } catch {
        setExplanation(null);
      }
    } finally {
      setLoading(false);
    }
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
      const updated = res.data ?? EMPTY_HEALTH;
      setHealth(updated);
      setForm(healthToForm(updated));
      setEditHealth(false);
      toast.success('Профиль здоровья обновлён');
      try {
        const explRes = await api.get<NutritionExplanationDto>('/profile/health/explanation');
        setExplanation(explRes.data);
      } catch {
        setExplanation(null);
      }
    } catch {
      toast.error('Ошибка сохранения');
    }
  };

  const addDisease = async () => {
    const name = newDisease.trim();
    if (!name) return;
    try {
      await api.post('/reference/diseases', { name, description: '', recommendedCaloriesMultiplier: 1.0 });
      setNewDisease('');
      toast.success('Заболевание добавлено');
      loadData();
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Не удалось добавить');
    }
  };

  const addAllergen = async () => {
    const name = newAllergen.trim();
    if (!name) return;
    try {
      await api.post('/reference/allergens', { name });
      setNewAllergen('');
      toast.success('Аллерген добавлен');
      loadData();
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Не удалось добавить');
    }
  };

  const toggleId = (arr: number[], id: number) =>
    arr.includes(id) ? arr.filter(x => x !== id) : [...arr, id];

  return (
    <div className="space-y-6">
      <div className="flex items-start justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold text-gray-900">Профиль</h1>
          <p className="text-sm text-gray-500">Данные аккаунта и профиль здоровья</p>
        </div>
      </div>

      {loading && (
        <Card className="animate-fade-in">
          <CardContent className="py-8">
            <div className="h-5 w-48 rounded bg-gray-100" />
            <div className="mt-3 h-4 w-64 rounded bg-gray-100" />
          </CardContent>
        </Card>
      )}

      {!loading && profile && (
        <Card>
          <CardContent className="flex flex-col sm:flex-row sm:items-center gap-5 animate-fade-up">
            <div className="relative h-20 w-20 shrink-0">
              {profile.avatarPath ? (
                <img
                  src={profile.avatarPath}
                  alt="Avatar"
                  className="h-20 w-20 rounded-2xl object-cover ring-2 ring-olive/30"
                />
              ) : (
                <div className="h-20 w-20 rounded-2xl bg-lime/30 flex items-center justify-center text-2xl text-rose font-semibold ring-2 ring-olive/30">
                  {profile.username[0].toUpperCase()}
                </div>
              )}
              <label className="absolute -bottom-2 -right-2 cursor-pointer">
                <span className="inline-flex h-9 w-9 items-center justify-center rounded-full bg-white shadow-sm ring-1 ring-gray-200 hover:bg-gray-50">
                  📷
                </span>
                <input type="file" accept="image/*" className="hidden" onChange={handleAvatarUpload} />
              </label>
            </div>
            <div className="min-w-0 flex-1">
              <div className="flex items-center gap-2 flex-wrap">
                <div className="text-lg font-semibold text-gray-900 truncate">{profile.username}</div>
                {profile.blocked && <Badge variant="danger">заблокирован</Badge>}
                {profile.roles.includes('ROLE_ADMIN') && <Badge variant="warning">admin</Badge>}
              </div>
              <div className="text-sm text-gray-500">{profile.email}</div>
              <div className="mt-2 flex gap-2 flex-wrap">
                {profile.roles.map((r) => (
                  <Badge key={r}>{r}</Badge>
                ))}
              </div>
            </div>
          </CardContent>
        </Card>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <Card className="lg:col-span-2">
          <CardHeader className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
            <div>
              <CardTitle className="text-rose">Профиль здоровья</CardTitle>
              <CardDescription>Вес, рост, активность, заболевания и аллергены</CardDescription>
            </div>
            <Button variant="secondary" size="sm" onClick={() => setEditHealth(!editHealth)} className="w-full sm:w-auto">
              {editHealth ? 'Отмена' : 'Редактировать'}
            </Button>
          </CardHeader>

          <CardContent>
            {loading ? (
              <div className="grid grid-cols-2 md:grid-cols-4 gap-3 animate-fade-in">
                {Array.from({ length: 8 }).map((_, i) => (
                  <div key={i} className="rounded-xl bg-gray-50 ring-1 ring-gray-100 p-3">
                    <div className="h-3 w-20 rounded bg-gray-100" />
                    <div className="mt-2 h-5 w-16 rounded bg-gray-100" />
                  </div>
                ))}
              </div>
            ) : !editHealth ? (
              <div className="space-y-4">
                <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
                  <InfoCard label="Вес" value={health.weight != null ? `${health.weight} кг` : '—'} />
                  <InfoCard label="Рост" value={health.height != null ? `${health.height} см` : '—'} />
                  <InfoCard label="Возраст" value={health.age != null ? health.age.toString() : '—'} />
                  <InfoCard label="Пол" value={health.gender === 'MALE' ? 'Мужской' : health.gender === 'FEMALE' ? 'Женский' : '—'} />
                  <InfoCard label="Калории/день" value={health.dailyCalorieTarget?.toFixed(0) ?? '—'} accent />
                  <InfoCard label="Белки/день" value={health.dailyProteinTarget?.toFixed(0) ?? '—'} />
                  <InfoCard label="Жиры/день" value={health.dailyFatTarget?.toFixed(0) ?? '—'} />
                  <InfoCard label="Углеводы/день" value={health.dailyCarbTarget?.toFixed(0) ?? '—'} />
                </div>

                <div className="space-y-1">
                  <div className="text-sm text-gray-500 dark:text-slate-400">Заболевания</div>
                  {health.diseaseNames && health.diseaseNames.length > 0 ? (
                    <div className="flex flex-wrap gap-2">
                      {Array.from(health.diseaseNames).map((n) => (
                        <Badge key={n} variant="warning">{n}</Badge>
                      ))}
                    </div>
                  ) : (
                    <div className="text-sm italic text-gray-400 dark:text-slate-500">Не указаны</div>
                  )}
                </div>

                <div className="space-y-1">
                  <div className="text-sm text-gray-500 dark:text-slate-400">Аллергены</div>
                  {health.allergenNames && health.allergenNames.length > 0 ? (
                    <div className="flex flex-wrap gap-2">
                      {Array.from(health.allergenNames).map((n) => (
                        <Badge key={n} variant="danger">{n}</Badge>
                      ))}
                    </div>
                  ) : (
                    <div className="text-sm italic text-gray-400 dark:text-slate-500">Не указаны</div>
                  )}
                </div>

                {health.weight == null && health.height == null && health.age == null && (
                  <p className="text-sm italic text-gray-500 dark:text-slate-400">
                    Профиль здоровья ещё не заполнен. Нажмите «Редактировать», чтобы добавить данные.
                  </p>
                )}
              </div>
            ) : (
              <div className="space-y-4 animate-fade-in">
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
                  <Field label="Вес (кг)">
                    <Input type="number" value={form.weight} onChange={(e) => setForm({ ...form, weight: e.target.value })} />
                  </Field>
                  <Field label="Рост (см)">
                    <Input type="number" value={form.height} onChange={(e) => setForm({ ...form, height: e.target.value })} />
                  </Field>
                  <Field label="Возраст">
                    <Input type="number" value={form.age} onChange={(e) => setForm({ ...form, age: e.target.value })} />
                  </Field>
                  <Field label="Пол">
                    <select
                      value={form.gender}
                      onChange={(e) => setForm({ ...form, gender: e.target.value })}
                      className="h-10 w-full rounded-md border border-gray-200 bg-white px-3 text-sm text-gray-900 dark:border-slate-800 dark:bg-slate-900 dark:text-slate-100 focus:outline-none focus:ring-2 focus:ring-olive focus:border-olive"
                    >
                      <option value="MALE">Мужской</option>
                      <option value="FEMALE">Женский</option>
                    </select>
                  </Field>
                  <Field label="Активность">
                    <select
                      value={form.activityLevel}
                      onChange={(e) => setForm({ ...form, activityLevel: e.target.value })}
                      className="h-10 w-full rounded-md border border-gray-200 bg-white px-3 text-sm text-gray-900 dark:border-slate-800 dark:bg-slate-900 dark:text-slate-100 focus:outline-none focus:ring-2 focus:ring-olive focus:border-olive"
                    >
                      <option value="SEDENTARY">Сидячий</option>
                      <option value="LIGHT">Лёгкая</option>
                      <option value="MODERATE">Умеренная</option>
                      <option value="ACTIVE">Активная</option>
                      <option value="VERY_ACTIVE">Очень активная</option>
                    </select>
                  </Field>
                </div>

                <div className="space-y-2">
                  <div className="text-sm font-medium text-gray-700">Заболевания</div>
                  <div className="flex gap-2">
                    <Input value={newDisease} onChange={(e) => setNewDisease(e.target.value)} placeholder="Добавить заболевание…" />
                    <Button variant="secondary" onClick={addDisease} disabled={!newDisease.trim()}>
                      Добавить
                    </Button>
                  </div>
                  {diseases.length === 0 ? (
                    <div className="text-sm italic text-gray-400 dark:text-slate-500">Список пуст. Добавьте первое заболевание выше.</div>
                  ) : (
                    <div className="flex flex-wrap gap-2">
                      {diseases.map((d) => {
                        const active = form.diseaseIds.includes(d.id);
                        return (
                          <button
                            key={d.id}
                            type="button"
                            onClick={() => setForm({ ...form, diseaseIds: toggleId(form.diseaseIds, d.id) })}
                            className={`rounded-full px-3 py-1 text-sm ring-1 transition-colors ${
                              active
                                ? 'bg-amber/25 text-amber-dark ring-amber/30'
                                : 'bg-gray-50 text-gray-700 ring-gray-200 hover:bg-gray-100 dark:bg-slate-800 dark:text-slate-200 dark:ring-slate-700 dark:hover:bg-slate-700'
                            }`}
                          >
                            {active ? '✓ ' : ''}{d.name}
                          </button>
                        );
                      })}
                    </div>
                  )}
                </div>

                <div className="space-y-2">
                  <div className="text-sm font-medium text-gray-700">Аллергены</div>
                  <div className="flex gap-2">
                    <Input value={newAllergen} onChange={(e) => setNewAllergen(e.target.value)} placeholder="Добавить аллерген…" />
                    <Button variant="secondary" onClick={addAllergen} disabled={!newAllergen.trim()}>
                      Добавить
                    </Button>
                  </div>
                  {allergens.length === 0 ? (
                    <div className="text-sm italic text-gray-400 dark:text-slate-500">Список пуст. Добавьте первый аллерген выше.</div>
                  ) : (
                    <div className="flex flex-wrap gap-2">
                      {allergens.map((a) => {
                        const active = form.allergenIds.includes(a.id);
                        return (
                          <button
                            key={a.id}
                            type="button"
                            onClick={() => setForm({ ...form, allergenIds: toggleId(form.allergenIds, a.id) })}
                            className={`rounded-full px-3 py-1 text-sm ring-1 transition-colors ${
                              active
                                ? 'bg-coral-light text-coral-dark ring-coral/25'
                                : 'bg-gray-50 text-gray-700 ring-gray-200 hover:bg-gray-100 dark:bg-slate-800 dark:text-slate-200 dark:ring-slate-700 dark:hover:bg-slate-700'
                            }`}
                          >
                            {active ? '✓ ' : ''}{a.name}
                          </button>
                        );
                      })}
                    </div>
                  )}
                </div>

                <Button onClick={handleSaveHealth} className="w-full sm:w-auto">
                  Сохранить
                </Button>
              </div>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="text-rose">Как посчитали нормы</CardTitle>
            <CardDescription>BMR → активность → коэффициенты болезней → БЖУ</CardDescription>
          </CardHeader>
          <CardContent>
            {!explanation ? (
              <div className="text-sm text-gray-500">
                Заполните вес/рост/возраст и сохраните профиль — появится расчёт с пояснениями.
              </div>
            ) : (
              <div className="space-y-3 text-sm animate-fade-in">
                <Row label="BMR" value={explanation.bmr.toFixed(2)} />
                <Row label="Активность" value={`× ${explanation.activityMultiplier}`} />
                {explanation.diseaseMultipliers.length > 0 ? (
                  <div className="space-y-1">
                    <div className="text-xs text-gray-500">Коэффициенты заболеваний</div>
                    <div className="flex flex-wrap gap-2">
                      {explanation.diseaseMultipliers.map((d) => (
                        <Badge key={d.diseaseId} variant="warning">
                          {d.diseaseName}: × {d.multiplier ?? 1}
                        </Badge>
                      ))}
                    </div>
                  </div>
                ) : (
                  <Row label="Заболевания" value="—" />
                )}

                <div className="h-px bg-gray-100" />
                <div className="grid grid-cols-2 gap-3">
                  <InfoCard label="Ккал/день" value={explanation.dailyCalories.toFixed(0)} accent />
                  <InfoCard label="Белки (г)" value={explanation.dailyProteins.toFixed(0)} />
                  <InfoCard label="Жиры (г)" value={explanation.dailyFats.toFixed(0)} />
                  <InfoCard label="Углеводы (г)" value={explanation.dailyCarbs.toFixed(0)} />
                </div>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}

function InfoCard({ label, value, accent }: { label: string; value: string; accent?: boolean }) {
  return (
    <div className={`rounded-xl p-3 ${accent ? 'bg-lime/20 ring-1 ring-olive/20 dark:bg-olive/10 dark:ring-olive/30' : 'bg-gray-50 ring-1 ring-gray-100 dark:bg-slate-800 dark:ring-slate-700'}`}>
      <p className="text-xs text-gray-500 dark:text-slate-400">{label}</p>
      <p className={`text-lg font-semibold ${accent ? 'text-rose dark:text-rose-light' : 'text-gray-900 dark:text-slate-100'}`}>{value}</p>
    </div>
  );
}

function Field({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div className="space-y-1">
      <div className="text-sm font-medium text-gray-700 dark:text-slate-200">{label}</div>
      {children}
    </div>
  );
}

function Row({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex items-center justify-between gap-3">
      <div className="text-gray-600 dark:text-slate-400">{label}</div>
      <div className="font-medium text-gray-900 dark:text-slate-100">{value}</div>
    </div>
  );
}
