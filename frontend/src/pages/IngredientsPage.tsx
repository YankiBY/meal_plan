import { useState } from 'react';
import api from '../api/client';
import toast from 'react-hot-toast';
import type { Ingredient } from '../types';

export default function IngredientsPage() {
  const [barcode, setBarcode] = useState('');
  const [searchName, setSearchName] = useState('');
  const [result, setResult] = useState<Ingredient | null>(null);

  const searchByBarcode = async () => {
    try {
      const res = await api.get<Ingredient>(`/ingredients/barcode/${barcode}`);
      setResult(res.data);
      toast.success('Продукт найден');
    } catch {
      setResult(null);
      toast.error('Продукт не найден');
    }
  };

  const searchByName = async () => {
    try {
      const res = await api.get<Ingredient>(`/ingredients/search?name=${searchName}`);
      setResult(res.data);
      toast.success('Продукт найден');
    } catch {
      setResult(null);
      toast.error('Продукт не найден');
    }
  };

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-rose">Поиск продуктов</h1>

      <div className="bg-white p-6 rounded-lg shadow grid grid-cols-1 md:grid-cols-2 gap-6">
        <div className="space-y-3">
          <h2 className="font-semibold">По штрих-коду</h2>
          <div className="flex gap-2">
            <input type="text" value={barcode} onChange={e => setBarcode(e.target.value)} className="flex-1 px-3 py-2 border rounded" placeholder="Введите штрих-код" />
            <button onClick={searchByBarcode} className="bg-olive text-white px-4 py-2 rounded hover:bg-olive-dark" disabled={!barcode}>Найти</button>
          </div>
        </div>
        <div className="space-y-3">
          <h2 className="font-semibold">По названию</h2>
          <div className="flex gap-2">
            <input type="text" value={searchName} onChange={e => setSearchName(e.target.value)} className="flex-1 px-3 py-2 border rounded" placeholder="Название продукта" />
            <button onClick={searchByName} className="bg-olive text-white px-4 py-2 rounded hover:bg-olive-dark" disabled={!searchName}>Найти</button>
          </div>
        </div>
      </div>

      {result && (
        <div className="bg-white p-6 rounded-lg shadow">
          <h2 className="text-lg font-semibold text-rose">{result.name}</h2>
          {result.barcode && <p className="text-sm text-gray-500">Штрих-код: {result.barcode}</p>}
          <div className="grid grid-cols-4 gap-4 mt-4">
            <div className="bg-lime/30 p-3 rounded text-center"><p className="text-xs text-gray-500">Калории</p><p className="text-lg font-bold text-olive-dark">{result.calories}</p></div>
            <div className="bg-amber/20 p-3 rounded text-center"><p className="text-xs text-gray-500">Белки</p><p className="text-lg font-bold text-amber-dark">{result.proteins}</p></div>
            <div className="bg-coral-light p-3 rounded text-center"><p className="text-xs text-gray-500">Жиры</p><p className="text-lg font-bold text-coral-dark">{result.fats}</p></div>
            <div className="bg-rose-light p-3 rounded text-center"><p className="text-xs text-gray-500">Углеводы</p><p className="text-lg font-bold text-rose">{result.carbohydrates}</p></div>
          </div>
        </div>
      )}
    </div>
  );
}
