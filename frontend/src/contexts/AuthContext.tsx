import { createContext, useContext, useState, useEffect, type ReactNode } from 'react';
import { apiService, type LoginResponse, type Employee } from '../services/api';

interface AuthContextType {
  user: Employee | null;
  isLoading: boolean;
  login: (username: string, password: string) => Promise<void>;
  register: (
    username: string,
    password: string,
    email: string,
    dateOfBirth?: string,
    address?: string,
    phoneNumber?: string
  ) => Promise<void>;
  logout: () => void;
  isAuthenticated: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<Employee | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // Kiểm tra user info trong localStorage khi component mount
    const storedUser = localStorage.getItem('user');
    if (storedUser) {
      try {
        const userData = JSON.parse(storedUser) as Employee;
        setUser(userData);
      } catch (e) {
        // Invalid data, remove it
        localStorage.removeItem('user');
      }
    }
    setIsLoading(false);
  }, []);

  const login = async (username: string, password: string) => {
    const response: LoginResponse = await apiService.login({ username, password });
    
    // Convert LoginResponse to Employee
    const userData: Employee = {
      id: response.employeeId,
      username: response.username,
      email: response.email,
      address: response.address,
      phoneNumber: response.phoneNumber,
      dateOfBirth: response.dateOfBirth,
    };
    
    // Lưu user info vào localStorage
    localStorage.setItem('user', JSON.stringify(userData));
    setUser(userData);
  };

  const register = async (
    username: string,
    password: string,
    email: string,
    dateOfBirth?: string,
    address?: string,
    phoneNumber?: string
  ) => {
    await apiService.register({
      username,
      password,
      email,
      dateOfBirth,
      address,
      phoneNumber,
    });
    // Sau khi đăng ký thành công, tự động đăng nhập
    await login(username, password);
  };

  const logout = () => {
    localStorage.removeItem('user');
    setUser(null);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        isLoading,
        login,
        register,
        logout,
        isAuthenticated: !!user,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}

