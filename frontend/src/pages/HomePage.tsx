import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
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
    return (
      <div className="space-y-6">
        <div className="rounded-2xl bg-[radial-gradient(80%_60%_at_20%_0%,rgba(255,190,64,0.18)_0%,transparent_60%),radial-gradient(80%_60%_at_80%_100%,rgba(171,62,91,0.16)_0%,transparent_60%)] p-6 ring-1 ring-gray-200 dark:ring-slate-800 animate-fade-up bg-white dark:bg-slate-900 shadow-sm">
          <div className="min-w-0">
            <h1 className="text-2xl sm:text-3xl font-semibold text-gray-900 dark:text-slate-100">
              Админ‑панель, <span className="text-rose">{user?.username}</span>
            </h1>
            <p className="mt-1 text-sm sm:text-base text-gray-600 dark:text-slate-400">
              Управление системой питания: пользователи, модерация, справочники и аналитика.
            </p>
            <p className="mt-2 text-xs text-gray-500 dark:text-slate-500">
              Используйте боковое меню для перехода между разделами.
            </p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="rounded-2xl bg-[radial-gradient(80%_60%_at_20%_0%,rgba(171,62,91,0.15)_0%,transparent_60%),radial-gradient(80%_60%_at_80%_100%,rgba(179,204,87,0.15)_0%,transparent_60%)] p-6 ring-1 ring-gray-200 dark:ring-slate-800 animate-fade-up bg-white dark:bg-slate-900 shadow-sm">
        <div className="min-w-0">
          <h1 className="text-2xl sm:text-3xl font-semibold text-gray-900 dark:text-slate-100">
            Добро пожаловать, <span className="text-rose">{user?.username}</span>
          </h1>
          <p className="mt-1 text-sm sm:text-base text-gray-600 dark:text-slate-300">
            Планирование питания с учётом профиля здоровья, аллергий и ограничений.
          </p>
          <p className="mt-2 text-xs text-gray-500 dark:text-slate-500">
            Используйте боковое меню для перехода между разделами.
          </p>
        </div>
      </div>

      <Card className="hover-lift overflow-hidden">
        <CardHeader>
          <CardTitle className="text-rose">Каждый день — шаг к лучшему самочувствию</CardTitle>
          <CardDescription>
            Регулярное питание и контроль БЖУ помогают сохранять энергию, сон и стабильный прогресс.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="relative h-[280px] sm:h-[360px] rounded-xl overflow-hidden ring-1 ring-gray-200 dark:ring-slate-800">
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
    </div>
  );
}
