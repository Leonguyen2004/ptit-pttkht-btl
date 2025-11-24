import { createFileRoute, Navigate } from '@tanstack/react-router';
import { useAuth } from '../contexts/AuthContext';

export const Route = createFileRoute('/')({
  component: Index,
});

function Index() {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div style={{ 
        display: 'flex', 
        justifyContent: 'center', 
        alignItems: 'center', 
        minHeight: '100vh',
        fontSize: '18px',
        color: '#666'
      }}>
        Đang tải...
      </div>
    );
  }

  if (isAuthenticated) {
    return <Navigate to="/dashboard" />;
  }

  return <Navigate to="/login" />;
}

