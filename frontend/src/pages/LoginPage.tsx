import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import toast from 'react-hot-toast';

export default function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await login(username, password);
      toast.success('Успешный вход');
      navigate('/');
    } catch {
      toast.error('Неверный логин или пароль');
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center">
      <div className="bg-white p-8 rounded-lg shadow-md w-full max-w-md">
        <h1 className="text-2xl font-bold text-center mb-6 text-rose">Вход в MealPlan</h1>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700">Логин</label>
            <input type="text" value={username} onChange={e => setUsername(e.target.value)}
              className="mt-1 w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-olive focus:border-olive" required />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700">Пароль</label>
            <input type="password" value={password} onChange={e => setPassword(e.target.value)}
              className="mt-1 w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-olive focus:border-olive" required />
          </div>
          <button type="submit" className="w-full bg-olive text-white py-2 rounded-md hover:bg-olive-dark">Войти</button>
        </form>
        <p className="mt-4 text-center text-sm text-gray-600">
          Нет аккаунта? <Link to="/register" className="text-rose hover:underline">Зарегистрироваться</Link>
        </p>
      </div>
    </div>
  );
}
