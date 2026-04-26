import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function HomePage() {
  const { user, isAdmin } = useAuth();

  const cards = [
    { to: '/meal-plans', title: 'Планы питания', desc: 'Генерация, просмотр и экспорт планов', icon: '📋' },
    { to: '/recipes', title: 'Рецепты', desc: 'Просмотр и создание рецептов', icon: '🍳' },
    { to: '/progress', title: 'Прогресс', desc: 'Отслеживание веса и БЖУ', icon: '📈' },
    { to: '/profile', title: 'Профиль', desc: 'Настройки здоровья и аватар', icon: '👤' },
    { to: '/ingredients', title: 'Продукты', desc: 'Поиск по штрих-коду и названию', icon: '🔍' },
  ];

  if (isAdmin) {
    cards.push({ to: '/admin', title: 'Администрирование', desc: 'Управление пользователями и данными', icon: '⚙️' });
  }

  return (
    <div>
      <h1 className="text-3xl font-bold text-rose mb-2">Добро пожаловать, {user?.username}!</h1>
      <p className="text-gray-500 mb-8">Система планирования питания с учётом здоровья</p>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {cards.map(card => (
          <Link key={card.to} to={card.to} className="bg-white p-6 rounded-lg shadow hover:shadow-lg transition-shadow border-l-4 border-olive">
            <div className="text-3xl mb-2">{card.icon}</div>
            <h2 className="text-lg font-semibold text-rose">{card.title}</h2>
            <p className="text-sm text-gray-500 mt-1">{card.desc}</p>
          </Link>
        ))}
      </div>
    </div>
  );
}
