import { useState } from 'react';
import { useNavigate } from '@tanstack/react-router';
import { apiService, type League } from '../services/api';
import '../routes/auth.css';

export function SearchLeagueView() {
  const [searchName, setSearchName] = useState('');
  const [leagues, setLeagues] = useState<League[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleSearch = async () => {
    if (!searchName.trim()) {
      setError('Vui lòng nhập tên giải đấu');
      return;
    }

    setIsLoading(true);
    setError('');

    try {
      const results = await apiService.searchLeagues(searchName.trim());
      setLeagues(results);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Có lỗi xảy ra khi tìm kiếm');
      setLeagues([]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleBack = () => {
    navigate({ to: '/dashboard' });
  };

  const handleView = (league: League) => {
    // Lưu thông tin giải đấu vào localStorage để sử dụng ở trang chi tiết
    localStorage.setItem('selectedLeague', JSON.stringify(league));
    navigate({ 
      to: '/tournaments/$leagueId', 
      params: { leagueId: league.id.toString() }
    });
  };

  const formatYear = (dateString: string | undefined): string => {
    if (!dateString) return '';
    try {
      const date = new Date(dateString);
      return date.getFullYear().toString();
    } catch {
      return '';
    }
  };

  return (
    <div className="tournaments-container">
      <div className="tournaments-card">
        <div className="tournaments-header">
          <h1>Quản lý giải đấu</h1>
        </div>

        <div className="search-section">
          <input
            type="text"
            className="search-input"
            placeholder="Nhập tên giải đấu"
            value={searchName}
            onChange={(e) => setSearchName(e.target.value)}
            onKeyPress={(e) => {
              if (e.key === 'Enter') {
                handleSearch();
              }
            }}
          />
          <button
            onClick={handleSearch}
            className="search-btn"
            disabled={isLoading}
          >
            {isLoading ? 'Đang tìm...' : 'Tìm kiếm'}
          </button>
        </div>

        {error && <div className="error-message">{error}</div>}

        <div className="table-section">
          <table className="tournaments-table">
            <thead>
              <tr>
                <th>Tên giải đấu</th>
                <th>Thời gian tổ chức</th>
                <th>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              {leagues.length === 0 ? (
                <tr>
                  <td colSpan={3} className="empty-message">
                    {searchName ? 'Không tìm thấy giải đấu nào' : 'Tìm kiếm giải đấu'}
                  </td>
                </tr>
              ) : (
                leagues.map((league) => (
                  <tr key={league.id}>
                    <td>{league.name}</td>
                    <td>{formatYear(league.startDate) || formatYear(league.endDate) || 'N/A'}</td>
                    <td>
                      <button
                        onClick={() => handleView(league)}
                        className="view-btn"
                      >
                        Xem
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        <div className="tournaments-footer">
          <button onClick={handleBack} className="back-btn">
            Quay lại
          </button>
        </div>
      </div>
    </div>
  );
}

