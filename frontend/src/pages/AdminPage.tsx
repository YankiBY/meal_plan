import { useState, useEffect } from 'react';
import api from '../api/client';
import toast from 'react-hot-toast';
import type { UserDto, RecipeDto, ActivityStatsDto, Disease, Allergen, Ingredient, Category } from '../types';

type Tab = 'stats' | 'users' | 'moderation' | 'diseases' | 'allergens' | 'ingredients' | 'categories';

export default function AdminPage() {
  const [tab, setTab] = useState<Tab>('stats');
  const [stats, setStats] = useState<ActivityStatsDto | null>(null);
  const [users, setUsers] = useState<UserDto[]>([]);
  const [pendingRecipes, setPendingRecipes] = useState<RecipeDto[]>([]);
  const [diseases, setDiseases] = useState<Disease[]>([]);
  const [allergens, setAllergens] = useState<Allergen[]>([]);
  const [ingredients, setIngredients] = useState<Ingredient[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);

  useEffect(() => {
    loadTab();
  }, [tab]);

  const loadTab = async () => {
    try {
      switch (tab) {
        case 'stats': { const r = await api.get<ActivityStatsDto>('/admin/stats'); setStats(r.data); break; }
        case 'users': { const r = await api.get<UserDto[]>('/admin/users'); setUsers(r.data); break; }
        case 'moderation': { const r = await api.get<RecipeDto[]>('/admin/moderation/recipes'); setPendingRecipes(r.data); break; }
        case 'diseases': { const r = await api.get<Disease[]>('/admin/diseases'); setDiseases(r.data); break; }
        case 'allergens': { const r = await api.get<Allergen[]>('/admin/allergens'); setAllergens(r.data); break; }
        case 'ingredients': { const r = await api.get<Ingredient[]>('/admin/ingredients'); setIngredients(r.data); break; }
        case 'categories': { const r = await api.get<Category[]>('/admin/categories'); setCategories(r.data); break; }
      }
    } catch {
      toast.error('Ошибка загрузки данных');
    }
  };

  const blockUser = async (id: number) => { await api.post(`/admin/users/${id}/block`); toast.success('Заблокирован'); loadTab(); };
  const unblockUser = async (id: number) => { await api.post(`/admin/users/${id}/unblock`); toast.success('Разблокирован'); loadTab(); };
  const changeRole = async (id: number, role: string) => { await api.post(`/admin/users/${id}/role?role=${role}`); toast.success('Роль изменена'); loadTab(); };
  const resetPassword = async (id: number) => {
    const pw = prompt('Новый пароль:');
    if (!pw) return;
    await api.post(`/admin/users/${id}/reset-password`, { password: pw });
    toast.success('Пароль сброшен');
  };
  const deleteUser = async (id: number) => {
    if (!confirm('Удалить пользователя?')) return;
    await api.delete(`/admin/users/${id}`);
    toast.success('Пользователь удалён');
    loadTab();
  };

  const moderateRecipe = async (id: number, approve: boolean) => {
    await api.post(`/admin/moderation/recipes/${id}?approve=${approve}`);
    toast.success(approve ? 'Одобрен' : 'Отклонён');
    loadTab();
  };

  const tabs: { key: Tab; label: string }[] = [
    { key: 'stats', label: 'Статистика' },
    { key: 'users', label: 'Пользователи' },
    { key: 'moderation', label: 'Модерация' },
    { key: 'diseases', label: 'Заболевания' },
    { key: 'allergens', label: 'Аллергены' },
    { key: 'ingredients', label: 'Ингредиенты' },
    { key: 'categories', label: 'Категории' },
  ];

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-rose">Панель администратора</h1>
      <div className="flex flex-wrap gap-2">
        {tabs.map(t => (
          <button key={t.key} onClick={() => setTab(t.key)} className={`px-3 py-1 rounded text-sm ${tab === t.key ? 'bg-olive text-white' : 'bg-gray-100'}`}>{t.label}</button>
        ))}
      </div>

      {tab === 'stats' && stats && (
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          <StatCard label="Пользователей" value={stats.totalUsers} />
          <StatCard label="Заблокированных" value={stats.blockedUsers} color="red" />
          <StatCard label="Рецептов" value={stats.totalRecipes} />
          <StatCard label="На модерации" value={stats.pendingRecipes} color="yellow" />
          <StatCard label="Одобрено" value={stats.moderatedRecipes} color="green" />
          <StatCard label="Планов питания" value={stats.totalMealPlans} />
          <StatCard label="Ингредиентов" value={stats.totalIngredients} />
          <StatCard label="Заболеваний" value={stats.totalDiseases} />
        </div>
      )}

      {tab === 'users' && (
        <div className="bg-white rounded-lg shadow overflow-x-auto">
          <table className="w-full text-sm">
            <thead><tr className="bg-gray-50"><th className="p-3 text-left">ID</th><th className="p-3 text-left">Имя</th><th className="p-3 text-left">Email</th><th className="p-3">Роли</th><th className="p-3">Статус</th><th className="p-3">Действия</th></tr></thead>
            <tbody>
              {users.map(u => (
                <tr key={u.id} className="border-t">
                  <td className="p-3">{u.id}</td>
                  <td className="p-3 font-medium">{u.username}</td>
                  <td className="p-3">{u.email}</td>
                  <td className="p-3 text-center text-xs">{u.roles.join(', ')}</td>
                  <td className="p-3 text-center">{u.blocked ? <span className="text-coral font-medium">Заблокирован</span> : <span className="text-olive">Активен</span>}</td>
                  <td className="p-3 text-center">
                    <div className="flex gap-1 justify-center flex-wrap">
                      {u.blocked ? <Btn onClick={() => unblockUser(u.id)} label="Разблокировать" /> : <Btn onClick={() => blockUser(u.id)} label="Заблокировать" color="red" />}
                      <Btn onClick={() => changeRole(u.id, u.roles.includes('ROLE_ADMIN') ? 'ROLE_USER' : 'ROLE_ADMIN')} label={u.roles.includes('ROLE_ADMIN') ? 'Сделать User' : 'Сделать Admin'} />
                      <Btn onClick={() => resetPassword(u.id)} label="Сброс пароля" />
                      <Btn onClick={() => deleteUser(u.id)} label="Удалить" color="red" />
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {tab === 'moderation' && (
        <div className="space-y-4">
          {pendingRecipes.length === 0 && <p className="text-gray-500">Нет рецептов на модерации</p>}
          {pendingRecipes.map(r => (
            <div key={r.id} className="bg-white p-4 rounded-lg shadow">
              <h3 className="font-semibold">{r.title}</h3>
              <p className="text-sm text-gray-500">{r.description}</p>
              <p className="text-xs text-gray-400 mt-1">Автор: {r.authorName} | Ккал: {r.totalCalories?.toFixed(0)}</p>
              <div className="flex gap-2 mt-2">
                <button onClick={() => moderateRecipe(r.id, true)} className="bg-olive text-white px-3 py-1 rounded text-sm">Одобрить</button>
                <button onClick={() => moderateRecipe(r.id, false)} className="bg-coral text-white px-3 py-1 rounded text-sm">Отклонить</button>
              </div>
            </div>
          ))}
        </div>
      )}

      {tab === 'diseases' && <CrudPanel<Disease> items={diseases} name="заболевание" fields={['name', 'description']} apiPath="/admin/diseases" reload={loadTab} />}
      {tab === 'allergens' && <CrudPanel<Allergen> items={allergens} name="аллерген" fields={['name']} apiPath="/admin/allergens" reload={loadTab} />}
      {tab === 'ingredients' && <CrudPanel<Ingredient> items={ingredients} name="ингредиент" fields={['name', 'unit', 'calories', 'proteins', 'fats', 'carbohydrates', 'barcode']} apiPath="/admin/ingredients" reload={loadTab} />}
      {tab === 'categories' && <CrudPanel<Category> items={categories} name="категорию" fields={['name']} apiPath="/admin/categories" reload={loadTab} />}
    </div>
  );
}

function StatCard({ label, value, color }: { label: string; value: number; color?: string }) {
  const colors: Record<string, string> = { red: 'text-coral-dark bg-coral-light', yellow: 'text-amber-dark bg-amber/20', green: 'text-olive-dark bg-lime/30' };
  return (
    <div className={`p-4 rounded-lg ${color ? colors[color] : 'bg-gray-50'}`}>
      <p className="text-xs text-gray-500">{label}</p>
      <p className="text-2xl font-bold">{value}</p>
    </div>
  );
}

function Btn({ onClick, label, color }: { onClick: () => void; label: string; color?: string }) {
  return <button onClick={onClick} className={`text-xs px-2 py-1 rounded ${color === 'red' ? 'bg-coral-light text-coral-dark' : 'bg-gray-100 text-gray-700'} hover:opacity-80`}>{label}</button>;
}

function CrudPanel<T extends { id: number }>({ items, name, fields, apiPath, reload }: { items: T[]; name: string; fields: string[]; apiPath: string; reload: () => void }) {
  const [showAdd, setShowAdd] = useState(false);
  const [form, setForm] = useState<Record<string, string>>({});

  const handleCreate = async () => {
    try {
      await api.post(apiPath, form);
      toast.success(`${name} создан(а)`);
      setShowAdd(false);
      setForm({});
      reload();
    } catch { toast.error('Ошибка'); }
  };

  const handleDelete = async (id: number) => {
    if (!confirm(`Удалить ${name}?`)) return;
    await api.delete(`${apiPath}/${id}`);
    toast.success('Удалено');
    reload();
  };

  return (
    <div className="space-y-4">
      <button onClick={() => setShowAdd(!showAdd)} className="bg-olive text-white px-4 py-2 rounded text-sm hover:bg-olive-dark">
        {showAdd ? 'Отмена' : `Добавить ${name}`}
      </button>
      {showAdd && (
        <div className="bg-white p-4 rounded-lg shadow flex flex-wrap gap-2 items-end">
          {fields.map(f => (
            <div key={f}><label className="text-xs text-gray-500">{f}</label><input className="block px-2 py-1 border rounded text-sm w-36" value={form[f] || ''} onChange={e => setForm({...form, [f]: e.target.value})} /></div>
          ))}
          <button onClick={handleCreate} className="bg-olive text-white px-3 py-1 rounded text-sm">Добавить</button>
        </div>
      )}
      <div className="bg-white rounded-lg shadow overflow-x-auto">
        <table className="w-full text-sm">
          <thead><tr className="bg-gray-50"><th className="p-2 text-left">ID</th>{fields.map(f => <th key={f} className="p-2 text-left">{f}</th>)}<th className="p-2">Действия</th></tr></thead>
          <tbody>
            {items.map(item => (
              <tr key={item.id} className="border-t">
                <td className="p-2">{item.id}</td>
                {fields.map(f => <td key={f} className="p-2">{String((item as Record<string, unknown>)[f] ?? '')}</td>)}
                <td className="p-2 text-center"><button onClick={() => handleDelete(item.id)} className="text-coral text-xs">Удалить</button></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
