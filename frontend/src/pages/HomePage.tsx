import { Link } from 'react-router-dom';
import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import Button from '../components/ui/Button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/Card';

export default function HomePage() {
  const { user, isAdmin } = useAuth();
  const [slide, setSlide] = useState(0);

  const foodSlides = [
    {
      image:
        'https://images.unsplash.com/photo-1547592180-85f173990554?auto=format&fit=crop&w=1400&q=80',
      caption: 'Еда, которая поддерживает энергию и концентрацию',
    },
    {
      image:
        'https://images.unsplash.com/photo-1498837167922-ddd27525d352?auto=format&fit=crop&w=1400&q=80',
      caption: 'Сбалансированное питание — ваш ежедневный ресурс',
    },
    {
      image:
        'https://images.unsplash.com/photo-1512621776951-a57141f2eefd?auto=format&fit=crop&w=1400&q=80',
      caption: 'Небольшие шаги каждый день дают большой результат',
    },
  ];

  useEffect(() => {
    const id = window.setInterval(() => {
      setSlide((s) => (s + 1) % foodSlides.length);
    }, 4500);
    return () => window.clearInterval(id);
  }, [foodSlides.length]);

  if (isAdmin) {
    const adminCards = [
      { to: '/admin#stats', title: 'Статистика', desc: 'Графики активности, пользователей и контента', icon: '📊' },
      { to: '/admin#users', title: 'Пользователи', desc: 'Роли, блокировки и сброс паролей', icon: '👥' },
      { to: '/admin#moderation', title: 'Модерация', desc: 'Проверка и публикация пользовательских рецептов', icon: '✅' },
      { to: '/admin#ingredients', title: 'Справочники', desc: 'Заболевания, аллергены, ингредиенты, категории', icon: '📚' },
    ];

    return (
      <div className="space-y-6">
        <div className="rounded-2xl bg-[radial-gradient(80%_60%_at_20%_0%,rgba(255,190,64,0.18)_0%,transparent_60%),radial-gradient(80%_60%_at_80%_100%,rgba(171,62,91,0.16)_0%,transparent_60%)] p-6 ring-1 ring-gray-200 dark:ring-slate-800 animate-fade-up bg-white dark:bg-slate-900 shadow-sm">
          <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4">
            <div className="min-w-0">
              <h1 className="text-2xl sm:text-3xl font-semibold text-gray-900 dark:text-slate-100">
                Админ‑панель, <span className="text-rose">{user?.username}</span>
              </h1>
              <p className="mt-1 text-sm sm:text-base text-gray-600 dark:text-slate-400">
                Управление системой питания: пользователи, модерация, справочники и аналитика.
              </p>
            </div>
            <div className="flex flex-col sm:flex-row gap-2">
              <Link to="/admin#stats">
                <Button className="w-full sm:w-auto">Открыть аналитику</Button>
              </Link>
              <Link to="/admin#moderation">
                <Button variant="secondary" className="w-full sm:w-auto">Перейти к модерации</Button>
              </Link>
            </div>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {adminCards.map((card) => (
            <Link key={card.to} to={card.to} className="rounded-xl bg-white dark:bg-slate-900 ring-1 ring-gray-200 dark:ring-slate-800 p-4 hover:bg-gray-50 dark:hover:bg-slate-800 transition-colors hover-lift">
              <div className="flex items-start gap-3">
                <div className="text-2xl">{card.icon}</div>
                <div className="min-w-0">
                  <div className="font-semibold text-gray-900 dark:text-slate-100">{card.title}</div>
                  <div className="text-sm text-gray-600 dark:text-slate-400">{card.desc}</div>
                </div>
              </div>
            </Link>
          ))}
        </div>
      </div>
    );
  }

  const cards = [
    { to: '/meal-plans', title: 'Планы питания', desc: 'Генерация, просмотр, экспорт и список продуктов', icon: '📋' },
    { to: '/recipes', title: 'Рецепты', desc: 'Каталог, ваши рецепты и отправка на модерацию', icon: '🍳' },
    { to: '/progress', title: 'Прогресс', desc: 'Календарь выполнения + графики веса и БЖУ', icon: '📈' },
    { to: '/profile', title: 'Профиль', desc: 'Данные здоровья, аллергены и расчёт норм', icon: '👤' },
    { to: '/ingredients', title: 'Продукты', desc: 'Поиск и проверка безопасности по профилю', icon: '🔎' },
  ];

  if (isAdmin) {
    cards.push({ to: '/admin', title: 'Администрирование', desc: 'Управление пользователями и данными', icon: '⚙️' });
  }

  return (
      <div className="space-y-6">
        <div className="rounded-2xl bg-[radial-gradient(80%_60%_at_20%_0%,rgba(171,62,91,0.15)_0%,transparent_60%),radial-gradient(80%_60%_at_80%_100%,rgba(179,204,87,0.15)_0%,transparent_60%)] p-6 ring-1 ring-gray-200 dark:ring-slate-800 animate-fade-up bg-white dark:bg-slate-900 shadow-sm">
          <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4">
          <div className="min-w-0">
            <h1 className="text-2xl sm:text-3xl font-semibold text-gray-900 dark:text-slate-100">
              Добро пожаловать, <span className="text-rose">{user?.username}</span>
            </h1>
            <p className="mt-1 text-sm sm:text-base text-gray-600 dark:text-slate-300">
              Планирование питания с учётом профиля здоровья, аллергий и ограничений.
            </p>
          </div>
          <div className="flex flex-col sm:flex-row gap-2">
            <Link to="/meal-plans">
              <Button className="w-full sm:w-auto">Создать план</Button>
            </Link>
            <Link to="/profile">
              <Button variant="secondary" className="w-full sm:w-auto">Заполнить профиль</Button>
            </Link>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <Card className="lg:col-span-2 hover-lift overflow-hidden">
          <CardHeader>
            <CardTitle className="text-rose">Каждый день — шаг к лучшему самочувствию</CardTitle>
            <CardDescription>
              Регулярное питание и контроль БЖУ помогают сохранять энергию, сон и стабильный прогресс.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="relative h-[280px] rounded-xl overflow-hidden ring-1 ring-gray-200 dark:ring-slate-800">
              {foodSlides.map((item, idx) => (
                <div
                  key={item.image}
                  className="absolute inset-0 transition-opacity duration-700"
                  style={{ opacity: slide === idx ? 1 : 0 }}
                >
                  <img src={item.image} alt="Полезная еда" className="h-full w-full object-cover" />
                  <div className="absolute inset-x-0 bottom-0 bg-gradient-to-t from-black/60 to-transparent p-4">
                    <p className="text-sm sm:text-base font-medium text-white">{item.caption}</p>
                  </div>
                </div>
              ))}
            </div>
            <div className="mt-3 flex justify-center gap-2">
              {foodSlides.map((_, idx) => (
                <button
                  key={idx}
                  type="button"
                  aria-label={`Слайд ${idx + 1}`}
                  onClick={() => setSlide(idx)}
                  className={`h-2 w-2 rounded-full transition-colors ${slide === idx ? 'bg-rose' : 'bg-gray-300 dark:bg-slate-600'}`}
                />
              ))}
            </div>
          </CardContent>
        </Card>

        <Card className="hover-lift">
          <CardHeader>
            <CardTitle className="text-rose">Разделы</CardTitle>
            <CardDescription>Всё в одном месте</CardDescription>
          </CardHeader>
          <CardContent className="space-y-2">
            {cards.map((card) => (
              <Link
                key={card.to}
                to={card.to}
                className="block rounded-xl bg-gray-50 dark:bg-slate-800 ring-1 ring-gray-100 dark:ring-slate-700 p-3 hover:bg-gray-100 dark:hover:bg-slate-700 transition-colors"
              >
                <div className="flex items-start gap-3">
                  <div className="text-xl">{card.icon}</div>
                  <div className="min-w-0">
                    <div className="font-medium text-gray-900 dark:text-slate-100">{card.title}</div>
                    <div className="text-xs text-gray-600 dark:text-slate-400">{card.desc}</div>
                  </div>
                </div>
              </Link>
            ))}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
