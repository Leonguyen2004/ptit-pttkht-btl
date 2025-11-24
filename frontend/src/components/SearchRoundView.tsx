import { useState } from 'react';
import { useNavigate } from '@tanstack/react-router';
import { apiService, type Round } from '../services/api';
import '../routes/auth.css';

interface SearchRoundViewProps {
  leagueId: string;
}

export function SearchRoundView({ leagueId }: SearchRoundViewProps) {
  const navigate = useNavigate();
  const [searchName, setSearchName] = useState('');
  const [rounds, setRounds] = useState<Round[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSearch = async () => {
    if (!searchName.trim()) {
      setError('Vui lòng nhập tên vòng đấu');
      return;
    }

    setIsLoading(true);
    setError('');

    try {
      const id = parseInt(leagueId, 10);
      if (isNaN(id)) {
        setError('ID giải đấu không hợp lệ');
        setIsLoading(false);
        return;
      }

      const results = await apiService.searchRoundsByLeagueIdAndName(id, searchName.trim());
      setRounds(results);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Có lỗi xảy ra khi tìm kiếm');
      setRounds([]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleBack = () => {
    navigate({ to: '/tournaments/$leagueId/schedule', params: { leagueId } });
  };

  const handleSelect = (round: Round) => {
    navigate({ 
      to: '/tournaments/$leagueId/schedule/add-match', 
      params: { leagueId },
      search: { roundId: round.id.toString() }
    });
  };

  const formatDate = (dateString: string | undefined): string => {
    if (!dateString) return 'N/A';
    try {
      const date = new Date(dateString);
      const day = date.getDate();
      const month = date.getMonth() + 1;
      const year = date.getFullYear();
      return `${day}/${month}/${year}`;
    } catch {
      return dateString;
    }
  };

  return (
    <div className="select-round-container">
      <div className="select-round-card">
        <div className="select-round-header">
          <h1>Chọn vòng đấu</h1>
        </div>

        <div className="select-round-actions">
          <button onClick={handleBack} className="back-btn">
            Quay lại
          </button>
        </div>

        <div className="search-section">
          <input
            type="text"
            className="search-input"
            placeholder="Nhập tên vòng đấu"
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
          <table className="select-round-table">
            <thead>
              <tr>
                <th>Tên vòng đấu</th>
                <th>Thời gian bắt đầu</th>
                <th>Thời gian kết thúc</th>
                <th>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              {rounds.length === 0 ? (
                <tr>
                  <td colSpan={4} className="empty-message">
                    {searchName ? 'Không tìm thấy vòng đấu nào' : 'Nhập tên vòng đấu và nhấn Tìm kiếm để xem danh sách'}
                  </td>
                </tr>
              ) : (
                rounds.map((round) => (
                  <tr key={round.id}>
                    <td>{round.name}</td>
                    <td>{formatDate(round.startDate)}</td>
                    <td>{formatDate(round.endDate)}</td>
                    <td>
                      <button
                        onClick={() => handleSelect(round)}
                        className="select-btn"
                      >
                        Chọn
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

