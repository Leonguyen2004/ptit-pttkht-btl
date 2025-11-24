import { useEffect, useState } from 'react';
import { useNavigate } from '@tanstack/react-router';
import { apiService, type League } from '../services/api';
import '../routes/auth.css';

interface ManageLeagueViewProps {
  leagueId: string;
}

export function ManageLeagueView({ leagueId }: ManageLeagueViewProps) {
  const navigate = useNavigate();
  const [league, setLeague] = useState<League | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchLeagueDetails = async () => {
      setIsLoading(true);
      setError('');

      try {
        // Kiểm tra xem có thông tin giải đấu trong localStorage không
        const storedLeague = localStorage.getItem('selectedLeague');
        if (storedLeague) {
          try {
            const parsedLeague = JSON.parse(storedLeague) as League;
            if (parsedLeague.id.toString() === leagueId) {
              // Sử dụng thông tin từ localStorage nếu ID khớp
              setLeague(parsedLeague);
              setIsLoading(false);
              // Xóa dữ liệu tạm sau khi sử dụng
              localStorage.removeItem('selectedLeague');
              return;
            }
          } catch (e) {
            // Nếu parse lỗi, tiếp tục tìm kiếm
          }
        }

        // Nếu không có trong localStorage, tìm kiếm lại
        const id = parseInt(leagueId, 10);
        if (isNaN(id)) {
          setError('ID giải đấu không hợp lệ');
          setIsLoading(false);
          return;
        }

        // Thử tìm kiếm với từ khóa rỗng để lấy tất cả giải đấu
        // Sau đó filter theo ID
        try {
          const leagues = await apiService.searchLeagues('');
          const foundLeague = leagues.find((l) => l.id === id);
          
          if (foundLeague) {
            setLeague(foundLeague);
          } else {
            // Nếu không tìm thấy, thử tìm với một từ khóa chung
            // Hoặc có thể cần thêm API mới để lấy theo ID
            setError('Không tìm thấy giải đấu');
          }
        } catch (searchError) {
          setError('Không thể tải thông tin giải đấu. Vui lòng thử lại.');
        }
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Có lỗi xảy ra khi tải thông tin giải đấu');
      } finally {
        setIsLoading(false);
      }
    };

    fetchLeagueDetails();
  }, [leagueId]);

  const handleBack = () => {
    navigate({ to: '/tournaments' });
  };

  const handleManageSchedule = () => {
    navigate({ to: '/tournaments/$leagueId/schedule', params: { leagueId } });
  };

  const formatDate = (dateString: string | undefined): string => {
    if (!dateString) return 'N/A';
    try {
      const date = new Date(dateString);
      return date.toLocaleDateString('vi-VN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
      });
    } catch {
      return dateString;
    }
  };

  if (isLoading) {
    return (
      <div className="tournaments-container">
        <div className="tournaments-card">
          <div className="loading-message">Đang tải...</div>
        </div>
      </div>
    );
  }

  if (error || !league) {
    return (
      <div className="tournaments-container">
        <div className="tournaments-card">
          <div className="error-message">{error || "Không tìm thấy giải đấu"}</div>
          <div className="tournaments-footer">
            <button onClick={handleBack} className="back-btn">
              Quay lại
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="tournaments-container">
      <div className="tournaments-card">
        <div className="tournaments-header">
          <h1>Quản lý giải đấu</h1>
        </div>

        <div className="tournaments-footer" style={{ justifyContent: "flex-start", marginBottom: "24px" }}>
          <button onClick={handleBack} className="back-btn">
            Quay lại
          </button>
        </div>

        <div className="tournament-detail-section">
          <h2 className="tournament-name">{league.name}</h2>

          <div className="tournament-info">
            <div className="info-row">
              <span className="info-label">Tên giải đấu:</span>
              <span className="info-value">{league.name}</span>
            </div>
            <div className="info-row">
              <span className="info-label">Ngày bắt đầu:</span>
              <span className="info-value">{formatDate(league.startDate)}</span>
            </div>
            <div className="info-row">
              <span className="info-label">Ngày kết thúc:</span>
              <span className="info-value">{formatDate(league.endDate)}</span>
            </div>
            {league.description && (
              <div className="info-row">
                <span className="info-label">Mô tả:</span>
                <span className="info-value">{league.description}</span>
              </div>
            )}
          </div>

          <div className="tournaments-footer" style={{ justifyContent: "flex-start", marginTop: "24px" }}>
            <button onClick={handleManageSchedule} className="schedule-btn">
              Quản lý lịch thi đấu
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

