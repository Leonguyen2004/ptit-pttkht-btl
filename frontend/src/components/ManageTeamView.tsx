import { useEffect, useState } from 'react';
import { useNavigate } from '@tanstack/react-router';
import { apiService, type Team } from '../services/api';
import '../routes/auth.css';

export function ManageTeamView() {
  const navigate = useNavigate();
  const [teams, setTeams] = useState<Team[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchTeams = async () => {
      setIsLoading(true);
      setError('');

      try {
        const teamsData = await apiService.getTeams();
        setTeams(teamsData);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Có lỗi xảy ra khi tải danh sách đội bóng');
      } finally {
        setIsLoading(false);
      }
    };

    fetchTeams();
  }, []);

  const handleBack = () => {
    navigate({ to: '/dashboard' });
  };

  const handleAddTeam = () => {
    navigate({ 
      to: '/teams/add',
      search: { stadiumId: undefined }
    });
  };

  // Lấy URL logo của đội bóng
  const getTeamImageUrl = (team: Team): string => {
    // Nếu có logo từ database, sử dụng logo đó
    if (team.logo) {
      // Nếu logo là đường dẫn tương đối (uploads/logos/...), convert thành URL đầy đủ
      if (team.logo.startsWith('uploads/')) {
        return `http://localhost:8080/${team.logo}`;
      }
      // Nếu là URL đầy đủ, sử dụng trực tiếp
      if (team.logo.startsWith('http://') || team.logo.startsWith('https://')) {
        return team.logo;
      }
      // Nếu là đường dẫn tương đối khác, thêm base URL
      return `http://localhost:8080/${team.logo}`;
    }
    
    // Fallback: sử dụng placeholder nếu không có logo
    const teamName = team.fullName || team.shortName || 'Team';
    const initials = teamName.substring(0, 2).toUpperCase();
    return `https://placehold.co/150/0066CC/FFFFFF?text=${encodeURIComponent(initials)}`;
  };

  if (isLoading) {
    return (
      <div className="team-management-container">
        <div className="team-management-card">
          <div className="loading-message">Đang tải...</div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="team-management-container">
        <div className="team-management-card">
          <div className="error-message">{error}</div>
          <div className="team-management-actions">
            <button onClick={handleBack} className="back-btn">
              Quay lại
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="team-management-container">
      <div className="team-management-card">
        <div className="team-management-header">
          <h1>Quản lý đội bóng</h1>
        </div>

        <div className="team-management-actions">
          <button onClick={handleBack} className="back-btn">
            Quay lại
          </button>
          <button onClick={handleAddTeam} className="add-team-btn">
            Thêm đội bóng
          </button>
        </div>

        <div className="teams-grid">
          {teams.length === 0 ? (
            <div className="empty-message">Chưa có đội bóng nào</div>
          ) : (
            teams.map((team) => (
              <div key={team.id} className="team-card">
                <div className="team-logo">
                  <img
                    src={getTeamImageUrl(team)}
                    alt={team.fullName}
                    onError={(e) => {
                      // Fallback nếu ảnh lỗi
                      (e.target as HTMLImageElement).src = 'https://via.placeholder.com/150/CCCCCC/666666?text=LOGO';
                    }}
                  />
                </div>
                <div className="team-name">{team.fullName}</div>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
}

