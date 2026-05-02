import { useEffect, useMemo, useState } from 'react';
import toast from 'react-hot-toast';
import Button from '../components/ui/Button';
import Input from '../components/ui/Input';
import { Card, CardContent, CardHeader, CardTitle } from '../components/ui/Card';

type ShoppingItem = {
  id: number;
  name: string;
  checked: boolean;
};

const STORAGE_KEY = 'shopping-list-items-v1';

export default function IngredientsPage() {
  const [query, setQuery] = useState('');
  const [items, setItems] = useState<ShoppingItem[]>(() => {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return [];
    try {
      const parsed = JSON.parse(raw) as ShoppingItem[];
      return Array.isArray(parsed) ? parsed : [];
    } catch {
      localStorage.removeItem(STORAGE_KEY);
      return [];
    }
  });

  useEffect(() => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(items));
  }, [items]);

  const addItem = () => {
    const name = query.trim();
    if (!name) return;
    if (items.some(item => item.name.toLowerCase() === name.toLowerCase())) {
      toast('Этот продукт уже есть в списке');
      return;
    }
    setItems(prev => [{ id: Date.now(), name, checked: false }, ...prev]);
    setQuery('');
  };

  const toggleItem = (id: number) => {
    setItems(prev => prev.map(item => item.id === id ? { ...item, checked: !item.checked } : item));
  };

  const removeItem = (id: number) => {
    setItems(prev => prev.filter(item => item.id !== id));
  };

  const clearBought = () => {
    setItems(prev => prev.filter(item => !item.checked));
  };

  const uncheckedCount = useMemo(() => items.filter(item => !item.checked).length, [items]);
  const checkedCount = items.length - uncheckedCount;

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold text-gray-900">Список покупок</h1>
        <p className="text-sm text-gray-500">Добавляйте продукты, отмечайте купленное и очищайте завершённые позиции</p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-rose">Добавить продукт</CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          <div className="flex flex-col sm:flex-row gap-2">
            <Input
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Например: молоко, яблоки, рис"
              onKeyDown={(e) => {
                if (e.key === 'Enter') {
                  e.preventDefault();
                  addItem();
                }
              }}
            />
            <Button onClick={addItem} disabled={!query.trim()}>
              Добавить
            </Button>
          </div>
          <div className="text-xs text-gray-500">Список работает локально и не зависит от БД</div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader className="flex-row items-center justify-between gap-3">
          <CardTitle className="text-rose">Мои покупки</CardTitle>
          <div className="text-sm text-gray-500">
            Осталось: <span className="font-semibold text-gray-900">{uncheckedCount}</span> / Куплено: <span className="font-semibold text-gray-900">{checkedCount}</span>
          </div>
        </CardHeader>
        <CardContent className="space-y-2">
          {items.length === 0 && (
            <div className="rounded-xl bg-gray-50 ring-1 ring-gray-100 p-6 text-center text-sm text-gray-500">
              Список пуст. Добавьте первую позицию выше.
            </div>
          )}

          {items.map((item) => (
            <div
              key={item.id}
              className={`flex items-center gap-3 rounded-xl border px-3 py-2 transition-all duration-300 ${
                item.checked
                  ? 'bg-gray-50 border-gray-200'
                  : 'bg-white border-gray-200 hover:border-olive/60'
              }`}
            >
              <button
                type="button"
                onClick={() => toggleItem(item.id)}
                className={`inline-flex h-6 w-6 shrink-0 items-center justify-center rounded-md border transition-colors ${
                  item.checked
                    ? 'bg-olive border-olive text-white'
                    : 'bg-white border-gray-300 text-transparent hover:border-olive'
                }`}
                aria-label={item.checked ? 'Отметить как не куплено' : 'Отметить как куплено'}
              >
                ✓
              </button>
              <div className="min-w-0 flex-1">
                <span className={`shopping-item-text ${item.checked ? 'is-checked' : ''}`}>
                  {item.name}
                </span>
              </div>
              <button
                type="button"
                onClick={() => removeItem(item.id)}
                className="text-sm text-gray-400 hover:text-coral-dark transition-colors"
              >
                удалить
              </button>
            </div>
          ))}

          {checkedCount > 0 && (
            <div className="pt-2">
              <Button variant="secondary" onClick={clearBought}>
                Очистить купленные
              </Button>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
