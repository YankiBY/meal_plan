import { useEffect, useMemo, useState } from 'react';
import api from '../api/client';
import type { ProgressCalendarDayDto } from '../types';
import Button from './ui/Button';
import { Card, CardContent, CardHeader, CardTitle } from './ui/Card';
import { cn } from './ui/cn';

const ruMonths = [
  'Январь',
  'Февраль',
  'Март',
  'Апрель',
  'Май',
  'Июнь',
  'Июль',
  'Август',
  'Сентябрь',
  'Октябрь',
  'Ноябрь',
  'Декабрь',
];

const ruWeek = ['Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб', 'Вс'];

function toIso(date: Date) {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const d = String(date.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
}

function startOfMonth(d: Date) {
  return new Date(d.getFullYear(), d.getMonth(), 1);
}

function endOfMonth(d: Date) {
  return new Date(d.getFullYear(), d.getMonth() + 1, 0);
}

function mondayIndex(jsDay: number) {
  // JS: 0=Sun..6=Sat  → 0=Mon..6=Sun
  return (jsDay + 6) % 7;
}

export default function ProgressCalendar() {
  const [cursor, setCursor] = useState(() => new Date());
  const [days, setDays] = useState<ProgressCalendarDayDto[]>([]);
  const [loading, setLoading] = useState(false);

  const monthStart = useMemo(() => startOfMonth(cursor), [cursor]);
  const monthEnd = useMemo(() => endOfMonth(cursor), [cursor]);

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      try {
        const res = await api.get<ProgressCalendarDayDto[]>(
          `/progress/calendar?start=${toIso(monthStart)}&end=${toIso(monthEnd)}`,
        );
        setDays(res.data);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [monthStart, monthEnd]);

  const map = useMemo(() => {
    const m = new Map<string, ProgressCalendarDayDto>();
    for (const d of days) m.set(d.date, d);
    return m;
  }, [days]);

  const cells = useMemo(() => {
    const s = startOfMonth(cursor);
    const e = endOfMonth(cursor);
    const pad = mondayIndex(s.getDay());
    const total = pad + e.getDate();
    const rows = Math.ceil(total / 7) * 7;
    const out: Array<{ date: Date | null; iso?: string; data?: ProgressCalendarDayDto }> = [];
    for (let i = 0; i < rows; i++) {
      const dayNum = i - pad + 1;
      if (dayNum < 1 || dayNum > e.getDate()) {
        out.push({ date: null });
      } else {
        const date = new Date(cursor.getFullYear(), cursor.getMonth(), dayNum);
        const iso = toIso(date);
        out.push({ date, iso, data: map.get(iso) });
      }
    }
    return out;
  }, [cursor, map]);

  return (
    <Card>
      <CardHeader className="flex-row items-center justify-between gap-3">
        <CardTitle className="text-rose">Календарь</CardTitle>
        <div className="flex items-center gap-2">
          <Button
            size="sm"
            variant="secondary"
            onClick={() => setCursor((d) => new Date(d.getFullYear(), d.getMonth() - 1, 1))}
          >
            ←
          </Button>
          <div className="min-w-[180px] text-center text-sm font-semibold text-gray-900">
            {ruMonths[cursor.getMonth()]} {cursor.getFullYear()}
          </div>
          <Button
            size="sm"
            variant="secondary"
            onClick={() => setCursor((d) => new Date(d.getFullYear(), d.getMonth() + 1, 1))}
          >
            →
          </Button>
        </div>
      </CardHeader>
      <CardContent>
        <div className="grid grid-cols-7 gap-2">
          {ruWeek.map((w) => (
            <div key={w} className="text-xs font-medium text-gray-500 text-center">
              {w}
            </div>
          ))}

          {cells.map((c, idx) => {
            if (!c.date) {
              return <div key={idx} className="h-12 rounded-lg bg-transparent" />;
            }
            const d = c.data;
            const base =
              !d?.hasEntry
                ? 'bg-gray-50 ring-1 ring-gray-100 text-gray-500'
                : d.planComplied
                  ? 'bg-lime/30 ring-1 ring-olive/20 text-gray-900'
                  : 'bg-amber/25 ring-1 ring-amber/30 text-gray-900';
            return (
              <div
                key={c.iso}
                className={cn('h-12 rounded-lg px-2 py-1 flex flex-col justify-between', base)}
                title={
                  !d?.hasEntry
                    ? 'Нет записи'
                    : d.planComplied
                      ? 'План выполнен'
                      : 'Запись есть, план не выполнен'
                }
              >
                <div className="text-xs font-semibold">{c.date.getDate()}</div>
                <div className="text-[10px] text-gray-600">
                  {loading ? '' : d?.hasEntry && d.weight != null ? `${d.weight} кг` : ''}
                </div>
              </div>
            );
          })}
        </div>

        <div className="mt-4 flex flex-wrap gap-2 text-xs text-gray-600">
          <span className="inline-flex items-center gap-2">
            <span className="h-3 w-3 rounded bg-gray-50 ring-1 ring-gray-100" /> нет записи
          </span>
          <span className="inline-flex items-center gap-2">
            <span className="h-3 w-3 rounded bg-amber/25 ring-1 ring-amber/30" /> запись есть
          </span>
          <span className="inline-flex items-center gap-2">
            <span className="h-3 w-3 rounded bg-lime/30 ring-1 ring-olive/20" /> план выполнен
          </span>
        </div>
      </CardContent>
    </Card>
  );
}

