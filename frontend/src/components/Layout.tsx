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
      <nav className="bg-green-700 text-white shadow-lg">
        <div className="max-w-7xl mx-auto px-4 py-3 flex items-center justify-between">
          <Link to="/" className="text-xl font-bold">🍽 MealPlan</Link>
          <div className="flex items-center gap-4 text-sm">
            <Link to="/meal-plans" className="hover:text-green-200">Планы питания</Link>
            <Link to="/recipes" className="hover:text-green-200">Рецепты</Link>
            <Link to="/progress" className="hover:text-green-200">Прогресс</Link>
            <Link to="/profile" className="hover:text-green-200">Профиль</Link>
            <Link to="/ingredients" className="hover:text-green-200">Продукты</Link>
            {isAdmin && <Link to="/admin" className="hover:text-yellow-300 font-semibold">Админ</Link>}
            <span className="text-green-200">{user?.username}</span>
            <button onClick={handleLogout} className="bg-green-800 px-3 py-1 rounded hover:bg-green-900">Выйти</button>
          </div>
        </div>
      </nav>
      <main className="max-w-7xl mx-auto px-4 py-6">
        <Outlet />
      </main>
    </div>
  );
}
