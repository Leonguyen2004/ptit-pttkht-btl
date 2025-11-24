import { createFileRoute, useNavigate } from '@tanstack/react-router';
import { useAuth } from '../contexts/AuthContext';
import './auth.css';

export const Route = createFileRoute('/dashboard')({
  component: DashboardPage,
});

function DashboardPage() {
  const { user, logout, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  // Nếu chưa đăng nhập, chuyển hướng về trang login
  if (!isAuthenticated) {
    navigate({ to: '/login' });
    return null;
  }

  const handleLogout = () => {
    logout();
    navigate({ to: '/login' });
  };

  const handleTournamentManagement = () => {
    navigate({ to: '/tournaments' });
  };

  const handleTeamManagement = () => {
    navigate({ to: '/teams' });
  };

  return (
    <div className="dashboard-container">
      <div className="dashboard-card">
        <div className="dashboard-left">
          <div className="user-info-section">
            <div className="user-info-line">
              <span className="info-label">Tên:</span>
              <span className="info-value">{user?.username || 'Nguyen Van A'}</span>
            </div>
            <div className="user-info-line">
              <span className="info-label">Chức vụ:</span>
              <span className="info-value">Quản lý giải đấu</span>
            </div>
          </div>
          <button onClick={handleLogout} className="logout-btn">
            Đăng xuất
          </button>
        </div>
        <div className="dashboard-right">
          <button onClick={handleTournamentManagement} className="management-btn">
            Quản lý giải đấu
          </button>
          <button onClick={handleTeamManagement} className="management-btn">
            Quản lý đội bóng
          </button>
        </div>
      </div>
    </div>
  );
}

