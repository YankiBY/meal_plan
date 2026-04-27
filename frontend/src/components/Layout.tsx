import { Link, NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import Button from './ui/Button';
import { cn } from './ui/cn';
import { useTheme } from '../context/ThemeContext';

export default function Layout() {
  const { user, logout, isAdmin } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const { theme, toggleTheme } = useTheme();
  const [collapsed, setCollapsed] = useState(false);

  useEffect(() => {
    const stored = localStorage.getItem('navCollapsed');
    if (stored) setCollapsed(stored === '1');
  }, []);

  const toggleCollapsed = () => {
    setCollapsed((c) => {
      const next = !c;
      localStorage.setItem('navCollapsed', next ? '1' : '0');
      return next;
    });
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const userNav = [
    { to: '/', label: 'Главная', icon: '🏠' },
    { to: '/meal-plans', label: 'Планы питания', icon: '📋' },
    { to: '/recipes', label: 'Рецепты', icon: '🍳' },
    { to: '/progress', label: 'Прогресс', icon: '📈' },
    { to: '/ingredients', label: 'Продукты', icon: '🔎' },
    { to: '/profile', label: 'Профиль', icon: '👤' },
  ] as const;

  const adminNav = [
    { to: '/', label: 'Главная', icon: '🏠' },
    { to: '/admin#stats', label: 'Статистика', icon: '📊' },
    { to: '/admin#users', label: 'Пользователи', icon: '👥' },
    { to: '/admin#moderation', label: 'Модерация', icon: '✅' },
    { to: '/admin#diseases', label: 'Заболевания', icon: '🩺' },
    { to: '/admin#allergens', label: 'Аллергены', icon: '⚠️' },
    { to: '/admin#ingredients', label: 'Ингредиенты', icon: '🥗' },
    { to: '/admin#categories', label: 'Категории', icon: '📚' },
  ] as const;

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-slate-950">
      <header className="sticky top-0 z-20 bg-white/80 dark:bg-slate-950/80 backdrop-blur border-b border-gray-200 dark:border-slate-800">
        <div className="mx-auto max-w-7xl px-4 py-3 flex items-center justify-between">
          <Link to="/" className="flex items-center gap-2 font-semibold text-gray-900 dark:text-slate-100">
            <span className="inline-flex h-9 w-9 items-center justify-center rounded-lg bg-rose text-white shadow-sm">🍽</span>
            <span className="hidden sm:inline">MealPlan</span>
          </Link>

          <div className="flex items-center gap-3">
            <div className="hidden sm:flex items-center gap-2 rounded-full bg-gray-50 dark:bg-slate-900 px-3 py-1 ring-1 ring-gray-200 dark:ring-slate-800">
              <span className="text-sm text-gray-600 dark:text-slate-400">Вы вошли как</span>
              <span className="text-sm font-medium text-gray-900 dark:text-slate-100">{user?.username}</span>
              {isAdmin && <span className="ml-1 rounded-full bg-amber/20 px-2 py-0.5 text-xs font-semibold text-amber-dark">админ</span>}
            </div>
            <Button variant="secondary" size="sm" onClick={toggleTheme}>
              {theme === 'dark' ? 'Светлая' : 'Тёмная'}
            </Button>
            <Button variant="secondary" size="sm" onClick={handleLogout}>
              Выйти
            </Button>
          </div>
        </div>
      </header>

      <div
        className={cn(
          'mx-auto max-w-7xl px-4 py-6 grid grid-cols-1 gap-6',
          collapsed ? 'lg:grid-cols-[56px_1fr]' : 'lg:grid-cols-[260px_1fr]',
        )}
      >
        <aside className={cn('lg:sticky lg:top-[72px] h-fit', collapsed && 'lg:w-[56px]')}>
          <nav
            className={cn(
              'rounded-xl bg-white dark:bg-slate-900 shadow-sm ring-1 ring-gray-200 dark:ring-slate-800',
              collapsed ? 'p-1.5' : 'p-2',
            )}
          >
            <button
              type="button"
              onClick={toggleCollapsed}
              className={cn(
                'mb-2 w-full flex items-center gap-3 rounded-lg py-2 text-sm transition-colors',
                collapsed ? 'justify-center px-0' : 'px-3',
                'text-gray-700 hover:bg-gray-100 dark:text-slate-200 dark:hover:bg-slate-800',
              )}
              title={collapsed ? 'Развернуть меню' : 'Свернуть меню'}
            >
              <span className="w-5 text-center">{collapsed ? '➡️' : '⬅️'}</span>
              {!collapsed && <span className="font-medium">Меню</span>}
            </button>

            {!isAdmin &&
              userNav.map((item) => (
                <NavLink
                  key={item.to}
                  to={item.to}
                  end={item.to === '/'}
                  className={({ isActive }) =>
                    cn(
                      'flex items-center gap-3 rounded-lg py-2 text-sm transition-colors',
                      collapsed ? 'justify-center px-0' : 'px-3',
                      isActive
                        ? 'bg-rose text-white'
                        : 'text-gray-700 hover:bg-gray-100 dark:text-slate-200 dark:hover:bg-slate-800',
                    )
                  }
                >
                  <span className="w-5 text-center">{item.icon}</span>
                  {!collapsed && <span className="font-medium">{item.label}</span>}
                </NavLink>
              ))}

            {isAdmin &&
              adminNav.map((item) => {
                const [path, hash] = item.to.split('#');
                const active =
                  path === '/'
                    ? location.pathname === '/'
                    : location.pathname === path && ((hash ? location.hash === `#${hash}` : true) || (!location.hash && hash === 'stats'));
                return (
                  <Link
                    key={item.to}
                    to={item.to}
                    className={cn(
                      'flex items-center gap-3 rounded-lg py-2 text-sm transition-colors',
                      collapsed ? 'justify-center px-0' : 'px-3',
                      active
                        ? 'bg-amber text-gray-900'
                        : 'text-gray-700 hover:bg-gray-100 dark:text-slate-200 dark:hover:bg-slate-800',
                    )}
                  >
                    <span className="w-5 text-center">{item.icon}</span>
                    {!collapsed && <span className="font-medium">{item.label}</span>}
                  </Link>
                );
              })}
          </nav>
        </aside>

        <main className="min-w-0">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
