import { useState, useEffect } from 'react';
import api from '../api/client';
import toast from 'react-hot-toast';
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip, Legend, BarChart, Bar, XAxis, YAxis, CartesianGrid } from 'recharts';
import { useTheme } from '../context/ThemeContext';
import { useLocation } from 'react-router-dom';
import type { UserDto, RecipeDto, ActivityStatsDto, Disease, Allergen, Ingredient, Category } from '../types';

type Tab = 'stats' | 'users' | 'moderation' | 'diseases' | 'allergens' | 'ingredients' | 'categories';

const FIELD_LABELS: Record<string, string> = {
  name: 'Название',
  description: 'Описание',
  unit: 'Ед. изм.',
  calories: 'Ккал',
  proteins: 'Белки',
  fats: 'Жиры',
  carbohydrates: 'Углеводы',
  barcode: 'Штрих-код',
};

export default function AdminPage() {
  const { theme } = useTheme();
  const location = useLocation();
  const [tab, setTab] = useState<Tab>('stats');
  const [stats, setStats] = useState<ActivityStatsDto | null>(null);
  const [users, setUsers] = useState<UserDto[]>([]);
  const [editingUser, setEditingUser] = useState<UserDto | null>(null);
  const [editForm, setEditForm] = useState<{ username: string; email: string; blocked: boolean; role: string }>({ username: '', email: '', blocked: false, role: 'ROLE_USER' });
  const [pendingRecipes, setPendingRecipes] = useState<RecipeDto[]>([]);
  const [diseases, setDiseases] = useState<Disease[]>([]);
  const [allergens, setAllergens] = useState<Allergen[]>([]);
  const [ingredients, setIngredients] = useState<Ingredient[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);

  useEffect(() => {
    loadTab();
  }, [tab]);

  useEffect(() => {
    const raw = location.hash.replace('#', '');
    const allowed: Tab[] = ['stats', 'users', 'moderation', 'diseases', 'allergens', 'ingredients', 'categories'];
    if (allowed.includes(raw as Tab)) {
      setTab(raw as Tab);
    } else if (!raw) {
      setTab('stats');
    }
  }, [location.hash]);

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

  const startEditUser = (u: UserDto) => {
    setEditingUser(u);
    setEditForm({
      username: u.username,
      email: u.email,
      blocked: u.blocked,
      role: u.roles.includes('ROLE_ADMIN') ? 'ROLE_ADMIN' : 'ROLE_USER',
    });
  };

  const cancelEditUser = () => {
    setEditingUser(null);
  };

  const saveEditUser = async () => {
    if (!editingUser) return;
    try {
      await api.put(`/admin/users/${editingUser.id}`, editForm);
      toast.success('Пользователь обновлён');
      setEditingUser(null);
      loadTab();
    } catch (e: unknown) {
      const err = e as { response?: { data?: { message?: string } } };
      toast.error(err.response?.data?.message || 'Ошибка обновления');
    }
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

  const recipeData = stats ? [
    { name: 'Одобрено', value: stats.moderatedRecipes },
    { name: 'На модерации', value: stats.pendingRecipes },
  ] : [];

  const userData = stats ? [
    { name: 'Активные', value: stats.totalUsers - stats.blockedUsers },
    { name: 'Заблокированные', value: stats.blockedUsers },
  ] : [];

  const entityData = stats ? [
    { name: 'Ингредиенты', count: stats.totalIngredients },
    { name: 'Заболевания', count: stats.totalDiseases },
    { name: 'Планы', count: stats.totalMealPlans },
  ] : [];

  const COLORS = ['#b3cc57', '#ef746f', '#ffbe40', '#ab3e5b'];

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-rose">Панель администратора</h1>
      <div className="flex flex-wrap gap-2">
        {tabs.map(t => (
          <button 
            key={t.key} 
            onClick={() => setTab(t.key)} 
            className={`px-3 py-1 rounded text-sm transition-colors ${
              tab === t.key 
                ? 'bg-olive text-white' 
                : 'bg-gray-100 text-gray-700 dark:bg-slate-800 dark:text-slate-300 hover:bg-gray-200 dark:hover:bg-slate-700'
            }`}
          >
            {t.label}
          </button>
        ))}
      </div>

      {tab === 'stats' && stats && (
        <div className="space-y-6">
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

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <div className="bg-white dark:bg-slate-900 p-6 rounded-xl shadow-sm ring-1 ring-gray-200 dark:ring-slate-800">
              <h3 className="text-lg font-semibold mb-4 dark:text-slate-100 text-center">Статистика рецептов</h3>
              <div className="h-64">
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie
                      data={recipeData}
                      cx="50%"
                      cy="50%"
                      innerRadius={60}
                      outerRadius={80}
                      paddingAngle={5}
                      dataKey="value"
                    >
                      {recipeData.map((_, index) => (
                        <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                      ))}
                    </Pie>
                    <Tooltip />
                    <Legend />
                  </PieChart>
                </ResponsiveContainer>
              </div>
            </div>

            <div className="bg-white dark:bg-slate-900 p-6 rounded-xl shadow-sm ring-1 ring-gray-200 dark:ring-slate-800">
              <h3 className="text-lg font-semibold mb-4 dark:text-slate-100 text-center">Статус пользователей</h3>
              <div className="h-64">
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie
                      data={userData}
                      cx="50%"
                      cy="50%"
                      innerRadius={60}
                      outerRadius={80}
                      paddingAngle={5}
                      dataKey="value"
                    >
                      {userData.map((_, index) => (
                        <Cell key={`cell-${index}`} fill={COLORS[(index + 2) % COLORS.length]} />
                      ))}
                    </Pie>
                    <Tooltip />
                    <Legend />
                  </PieChart>
                </ResponsiveContainer>
              </div>
            </div>

            <div className="lg:col-span-2 bg-white dark:bg-slate-900 p-6 rounded-xl shadow-sm ring-1 ring-gray-200 dark:ring-slate-800">
              <h3 className="text-lg font-semibold mb-4 dark:text-slate-100 text-center">Объекты системы</h3>
              <div className="h-64">
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={entityData}>
                    <CartesianGrid strokeDasharray="3 3" stroke={theme === 'dark' ? '#334155' : '#e2e8f0'} />
                    <XAxis dataKey="name" stroke={theme === 'dark' ? '#94a3b8' : '#64748b'} />
                    <YAxis stroke={theme === 'dark' ? '#94a3b8' : '#64748b'} />
                    <Tooltip 
                      contentStyle={{ 
                        backgroundColor: theme === 'dark' ? '#1e293b' : '#ffffff', 
                        border: 'none', 
                        borderRadius: '8px', 
                        color: theme === 'dark' ? '#f1f5f9' : '#1e293b',
                        boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)'
                      }}
                      itemStyle={{ color: theme === 'dark' ? '#f1f5f9' : '#1e293b' }}
                    />
                    <Bar dataKey="count" fill="#b3cc57" radius={[4, 4, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            </div>
          </div>
        </div>
      )}

      {tab === 'users' && (
        <div className="bg-white dark:bg-slate-900 rounded-lg shadow ring-1 ring-gray-200 dark:ring-slate-800 overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-gray-50 dark:bg-slate-800/50">
                <th className="p-3 text-left text-gray-600 dark:text-slate-300">ID</th>
                <th className="p-3 text-left text-gray-600 dark:text-slate-300">Имя</th>
                <th className="p-3 text-left text-gray-600 dark:text-slate-300">Email</th>
                <th className="p-3 text-gray-600 dark:text-slate-300">Роли</th>
                <th className="p-3 text-gray-600 dark:text-slate-300">Статус</th>
                <th className="p-3 text-gray-600 dark:text-slate-300">Действия</th>
              </tr>
            </thead>
            <tbody>
              {users.map(u => (
                <tr key={u.id} className="border-t border-gray-100 dark:border-slate-800">
                  <td className="p-3 dark:text-slate-400">{u.id}</td>
                  <td className="p-3 font-medium dark:text-slate-200">{u.username}</td>
                  <td className="p-3 dark:text-slate-400">{u.email}</td>
                  <td className="p-3 text-center text-xs dark:text-slate-400">{u.roles.join(', ')}</td>
                  <td className="p-3 text-center">{u.blocked ? <span className="text-coral font-medium">Заблокирован</span> : <span className="text-olive">Активен</span>}</td>
                  <td className="p-3 text-center">
                    <div className="flex gap-1 justify-center flex-wrap">
                      <Btn onClick={() => startEditUser(u)} label="Редактировать" />
                      {u.blocked ? <Btn onClick={() => unblockUser(u.id)} label="Разблокировать" /> : <Btn onClick={() => blockUser(u.id)} label="Заблокировать" color="red" />}
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

      {editingUser && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4" onClick={cancelEditUser}>
          <div className="w-full max-w-md bg-white dark:bg-slate-900 rounded-xl shadow-lg ring-1 ring-gray-200 dark:ring-slate-800 p-6 animate-fade-in" onClick={e => e.stopPropagation()}>
            <h2 className="text-lg font-semibold mb-4 text-gray-900 dark:text-slate-100">Редактирование пользователя #{editingUser.id}</h2>
            <div className="space-y-3">
              <div>
                <label className="text-xs text-gray-500 dark:text-slate-400 mb-1 block">Имя пользователя</label>
                <input
                  className="block w-full px-3 py-2 border dark:border-slate-700 rounded text-sm bg-white dark:bg-slate-800 text-gray-900 dark:text-slate-100 focus:ring-2 focus:ring-olive focus:border-transparent outline-none"
                  value={editForm.username}
                  onChange={e => setEditForm({ ...editForm, username: e.target.value })}
                />
              </div>
              <div>
                <label className="text-xs text-gray-500 dark:text-slate-400 mb-1 block">Email</label>
                <input
                  type="email"
                  className="block w-full px-3 py-2 border dark:border-slate-700 rounded text-sm bg-white dark:bg-slate-800 text-gray-900 dark:text-slate-100 focus:ring-2 focus:ring-olive focus:border-transparent outline-none"
                  value={editForm.email}
                  onChange={e => setEditForm({ ...editForm, email: e.target.value })}
                />
              </div>
              <div>
                <label className="text-xs text-gray-500 dark:text-slate-400 mb-1 block">Роль</label>
                <select
                  className="block w-full px-3 py-2 border dark:border-slate-700 rounded text-sm bg-white dark:bg-slate-800 text-gray-900 dark:text-slate-100 focus:ring-2 focus:ring-olive focus:border-transparent outline-none"
                  value={editForm.role}
                  onChange={e => setEditForm({ ...editForm, role: e.target.value })}
                >
                  <option value="ROLE_USER">Пользователь</option>
                  <option value="ROLE_ADMIN">Администратор</option>
                </select>
              </div>
              <div>
                <label className="inline-flex items-center gap-2 text-sm text-gray-700 dark:text-slate-300 cursor-pointer">
                  <input
                    type="checkbox"
                    className="h-4 w-4 accent-coral"
                    checked={editForm.blocked}
                    onChange={e => setEditForm({ ...editForm, blocked: e.target.checked })}
                  />
                  Заблокирован
                </label>
              </div>
            </div>
            <div className="flex gap-2 justify-end mt-6">
              <button
                onClick={cancelEditUser}
                className="bg-gray-100 dark:bg-slate-800 text-gray-700 dark:text-slate-300 px-4 py-2 rounded text-sm hover:bg-gray-200 dark:hover:bg-slate-700 transition-colors"
              >
                Отмена
              </button>
              <button
                onClick={saveEditUser}
                className="bg-olive text-white px-4 py-2 rounded text-sm hover:bg-olive-dark transition-colors"
              >
                Сохранить
              </button>
            </div>
          </div>
        </div>
      )}

      {tab === 'moderation' && (
        <div className="space-y-4">
          {pendingRecipes.length === 0 && <p className="text-gray-500 dark:text-slate-400">Нет рецептов на модерации</p>}
          {pendingRecipes.map(r => (
            <div key={r.id} className="bg-white dark:bg-slate-900 p-4 rounded-lg shadow ring-1 ring-gray-200 dark:ring-slate-800">
              <h3 className="font-semibold dark:text-slate-100">{r.title}</h3>
              <p className="text-sm text-gray-500 dark:text-slate-400">{r.description}</p>
              <p className="text-xs text-gray-400 dark:text-slate-500 mt-1">Автор: {r.authorName} | Ккал: {r.totalCalories?.toFixed(0)}</p>
              <div className="flex gap-2 mt-2">
                <button onClick={() => moderateRecipe(r.id, true)} className="bg-olive text-white px-3 py-1 rounded text-sm hover:bg-olive-dark">Одобрить</button>
                <button onClick={() => moderateRecipe(r.id, false)} className="bg-coral text-white px-3 py-1 rounded text-sm hover:bg-coral-dark">Отклонить</button>
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
  const colors: Record<string, string> = { 
    red: 'text-coral-dark bg-coral-light dark:bg-rose/10 dark:text-coral', 
    yellow: 'text-amber-dark bg-amber/20 dark:bg-amber/10 dark:text-amber', 
    green: 'text-olive-dark bg-lime/30 dark:bg-olive/10 dark:text-olive' 
  };
  return (
    <div className={`p-4 rounded-lg shadow-sm ring-1 ring-black/5 dark:ring-white/5 ${color ? colors[color] : 'bg-gray-50 dark:bg-slate-800/50 text-gray-900 dark:text-slate-100'}`}>
      <p className="text-xs opacity-70">{label}</p>
      <p className="text-2xl font-bold">{value}</p>
    </div>
  );
}

function Btn({ onClick, label, color }: { onClick: () => void; label: string; color?: string }) {
  return (
    <button 
      onClick={onClick} 
      className={`text-xs px-2 py-1 rounded transition-colors ${
        color === 'red' 
          ? 'bg-coral-light text-coral-dark dark:bg-rose/10 dark:text-coral hover:bg-coral/20' 
          : 'bg-gray-100 text-gray-700 dark:bg-slate-800 dark:text-slate-300 hover:bg-gray-200 dark:hover:bg-slate-700'
      }`}
    >
      {label}
    </button>
  );
}

function CrudPanel<T extends { id: number }>({ items, name, fields, apiPath, reload }: { items: T[]; name: string; fields: string[]; apiPath: string; reload: () => void }) {
  const [showAdd, setShowAdd] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [form, setForm] = useState<Record<string, string>>({});

  const handleCreate = async () => {
    try {
      if (editingId) {
        await api.put(`${apiPath}/${editingId}`, form);
        toast.success(`${name} обновлен(а)`);
      } else {
        await api.post(apiPath, form);
        toast.success(`${name} создан(а)`);
      }
      setShowAdd(false);
      setEditingId(null);
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

  const startEdit = (item: T) => {
    const f: Record<string, string> = {};
    fields.forEach(field => {
      f[field] = String((item as Record<string, unknown>)[field] ?? '');
    });
    setForm(f);
    setEditingId(item.id);
    setShowAdd(true);
  };

  return (
    <div className="space-y-4">
      <button 
        onClick={() => {
          if (showAdd) {
            setShowAdd(false);
            setEditingId(null);
            setForm({});
          } else {
            setShowAdd(true);
          }
        }} 
        className="bg-olive text-white px-4 py-2 rounded text-sm hover:bg-olive-dark transition-colors"
      >
        {showAdd ? 'Отмена' : `Добавить ${name}`}
      </button>
      {showAdd && (
        <div className="bg-white dark:bg-slate-900 p-4 rounded-lg shadow ring-1 ring-gray-200 dark:ring-slate-800 flex flex-wrap gap-3 items-end animate-fade-in">
          {fields.map(f => (
            <div key={f} className="flex-1 min-w-[150px]">
              <label className="text-xs text-gray-500 dark:text-slate-400 mb-1 block">{FIELD_LABELS[f] || f}</label>
              <input 
                className="block w-full px-3 py-1.5 border dark:border-slate-700 rounded text-sm bg-white dark:bg-slate-800 text-gray-900 dark:text-slate-100 focus:ring-2 focus:ring-olive focus:border-transparent outline-none" 
                value={form[f] || ''} 
                onChange={e => setForm({...form, [f]: e.target.value})} 
              />
            </div>
          ))}
          <button onClick={handleCreate} className="bg-olive text-white px-4 py-1.5 rounded text-sm hover:bg-olive-dark transition-colors">
            {editingId ? 'Сохранить' : 'Добавить'}
          </button>
        </div>
      )}
      <div className="bg-white dark:bg-slate-900 rounded-lg shadow ring-1 ring-gray-200 dark:ring-slate-800 overflow-x-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="bg-gray-50 dark:bg-slate-800/50">
              <th className="p-3 text-left text-gray-600 dark:text-slate-300">ID</th>
              {fields.map(f => <th key={f} className="p-3 text-left text-gray-600 dark:text-slate-300">{FIELD_LABELS[f] || f}</th>)}
              <th className="p-3 text-gray-600 dark:text-slate-300 text-center">Действия</th>
            </tr>
          </thead>
          <tbody>
            {items.map(item => (
              <tr key={item.id} className="border-t border-gray-100 dark:border-slate-800 hover:bg-gray-50 dark:hover:bg-slate-800/50 transition-colors">
                <td className="p-3 text-gray-500 dark:text-slate-400">{item.id}</td>
                {fields.map(f => <td key={f} className="p-3 text-gray-700 dark:text-slate-300">{String((item as Record<string, unknown>)[f] ?? '')}</td>)}
                <td className="p-3">
                  <div className="flex gap-2 justify-center">
                    <button onClick={() => startEdit(item)} className="text-olive text-xs hover:underline">Изменить</button>
                    <button onClick={() => handleDelete(item.id)} className="text-coral text-xs hover:underline">Удалить</button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
