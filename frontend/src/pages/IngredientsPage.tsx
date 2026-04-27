import { useState } from 'react';
import api from '../api/client';
import toast from 'react-hot-toast';
import type { IngredientSafetyDto } from '../types';
import Button from '../components/ui/Button';
import Input from '../components/ui/Input';
import Badge from '../components/ui/Badge';
import { Card, CardContent, CardHeader, CardTitle } from '../components/ui/Card';

export default function IngredientsPage() {
  const [barcode, setBarcode] = useState('');
  const [searchName, setSearchName] = useState('');
  const [result, setResult] = useState<IngredientSafetyDto | null>(null);

  const searchByBarcode = async () => {
    try {
      const res = await api.get<IngredientSafetyDto>(`/ingredients/barcode/${barcode}/check`);
      setResult(res.data);
      toast.success('Продукт найден');
    } catch {
      setResult(null);
      toast.error('Продукт не найден');
    }
  };

  const searchByName = async () => {
    try {
      const res = await api.get<IngredientSafetyDto>(`/ingredients/search/check?name=${encodeURIComponent(searchName)}`);
      setResult(res.data);
      toast.success('Продукт найден');
    } catch {
      setResult(null);
      toast.error('Продукт не найден');
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold text-gray-900">Поиск продуктов</h1>
        <p className="text-sm text-gray-500">По штрих-коду или названию, с проверкой по вашему профилю</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <Card>
          <CardHeader>
            <CardTitle className="text-rose">По штрих-коду</CardTitle>
          </CardHeader>
          <CardContent className="flex gap-2">
            <Input value={barcode} onChange={(e) => setBarcode(e.target.value)} placeholder="Введите штрих-код" />
            <Button onClick={searchByBarcode} disabled={!barcode}>
              Найти
            </Button>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="text-rose">По названию</CardTitle>
          </CardHeader>
          <CardContent className="flex gap-2">
            <Input value={searchName} onChange={(e) => setSearchName(e.target.value)} placeholder="Название продукта" />
            <Button onClick={searchByName} disabled={!searchName}>
              Найти
            </Button>
          </CardContent>
        </Card>
      </div>

      {result && (
        <Card>
          <CardHeader className="flex-row items-start justify-between gap-3">
            <div>
              <CardTitle className="text-rose">{result.ingredient.name}</CardTitle>
              {result.ingredient.barcode && (
                <div className="text-sm text-gray-500">Штрих-код: {result.ingredient.barcode}</div>
              )}
            </div>
            {result.allowed ? (
              <Badge variant="success">разрешено</Badge>
            ) : (
              <Badge variant="danger">запрещено</Badge>
            )}
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="text-sm text-gray-600">{result.note}</div>
            {result.matchedAllergens?.length > 0 && (
              <div className="flex flex-wrap gap-2">
                {result.matchedAllergens.map((a) => (
                  <Badge key={a} variant="danger">
                    {a}
                  </Badge>
                ))}
              </div>
            )}

            <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
              <Macro label="Калории" value={result.ingredient.calories} />
              <Macro label="Белки" value={result.ingredient.proteins} />
              <Macro label="Жиры" value={result.ingredient.fats} />
              <Macro label="Углеводы" value={result.ingredient.carbohydrates} />
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
}

function Macro({ label, value }: { label: string; value: number }) {
  return (
    <div className="rounded-xl bg-gray-50 ring-1 ring-gray-100 p-3 text-center">
      <div className="text-xs text-gray-500">{label}</div>
      <div className="text-lg font-semibold text-gray-900">{Number.isFinite(value) ? value : 0}</div>
    </div>
  );
}
