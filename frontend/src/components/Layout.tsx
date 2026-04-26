import { Link, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Layout() {
  const { user, logout, isAdmin } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <nav className="bg-rose text-white shadow-lg">
        <div className="max-w-7xl mx-auto px-4 py-3 flex items-center justify-between">
          <Link to="/" className="text-xl font-bold">🍽 MealPlan</Link>
          <div className="flex items-center gap-4 text-sm">
            <Link to="/meal-plans" className="hover:text-lime">Планы питания</Link>
            <Link to="/recipes" className="hover:text-lime">Рецепты</Link>
            <Link to="/progress" className="hover:text-lime">Прогресс</Link>
            <Link to="/profile" className="hover:text-lime">Профиль</Link>
            <Link to="/ingredients" className="hover:text-lime">Продукты</Link>
            {isAdmin && <Link to="/admin" className="hover:text-amber font-semibold">Админ</Link>}
            <span className="text-lime">{user?.username}</span>
            <button onClick={handleLogout} className="bg-rose-dark px-3 py-1 rounded hover:bg-rose-dark/80">Выйти</button>
          </div>
        </div>
      </nav>
      <main className="max-w-7xl mx-auto px-4 py-6">
        <Outlet />
      </main>
    </div>
  );
}
