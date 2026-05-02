import { createContext, useContext, useState, useEffect, type ReactNode } from 'react';
import api from '../api/client';
import type { JwtResponse } from '../types';

interface AuthUser {
  id: number;
  username: string;
  email: string;
  roles: string[];
  token: string;
}

interface AuthContextType {
  user: AuthUser | null;
  login: (username: string, password: string) => Promise<void>;
  register: (username: string, email: string, password: string) => Promise<void>;
  logout: () => void;
  isAdmin: boolean;
}

const AuthContext = createContext<AuthContextType | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null);

  useEffect(() => {
    const stored = localStorage.getItem('user');
    if (stored) {
      setUser(JSON.parse(stored));
    }
  }, []);

  const login = async (username: string, password: string) => {
    const res = await api.post<JwtResponse>('/auth/login', { username, password });
    const authUser: AuthUser = {
      id: res.data.id,
      username: res.data.username,
      email: res.data.email,
      roles: Array.from(res.data.roles),
      token: res.data.token,
    };
    localStorage.setItem('token', res.data.token);
    localStorage.setItem('user', JSON.stringify(authUser));
    setUser(authUser);
  };

  const register = async (username: string, email: string, password: string) => {
    await api.post('/auth/register', { username, email, password });
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
  };

  const isAdmin = user?.roles?.includes('ROLE_ADMIN') ?? false;

  return (
    <AuthContext.Provider value={{ user, login, register, logout, isAdmin }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be inside AuthProvider');
  return ctx;
}
