import { useState, useEffect } from 'react';
import { useNavigate } from '@tanstack/react-router';
import { apiService, type LeagueTeam, type Stadium, type Match, type LeagueTeamMatch, type Round } from '../services/api';
import { ConfirmView } from './ConfirmView';
import '../routes/auth.css';

interface MatchFormData {
  date: string;
  time: string;
  homeTeam: LeagueTeam | null;
  awayTeam: LeagueTeam | null;
  stadium: Stadium | null;
}

interface AddMatchViewProps {
  leagueId: string;
  roundId: string;
  stadiumId?: string;
}

export function AddMatchView({ leagueId, roundId, stadiumId }: AddMatchViewProps) {
  const navigate = useNavigate();
  const [showConfirmation, setShowConfirmation] = useState(false);
  const [formData, setFormData] = useState<MatchFormData>({
    date: '',
    time: '19:00',
    homeTeam: null,
    awayTeam: null,
    stadium: null,
  });

  // Home team search
  const [homeTeamSearch, setHomeTeamSearch] = useState('');
  const [homeTeamResults, setHomeTeamResults] = useState<LeagueTeam[]>([]);
  const [isSearchingHomeTeam, setIsSearchingHomeTeam] = useState(false);

  // Away team search
  const [awayTeamSearch, setAwayTeamSearch] = useState('');
  const [awayTeamResults, setAwayTeamResults] = useState<LeagueTeam[]>([]);
  const [isSearchingAwayTeam, setIsSearchingAwayTeam] = useState(false);

  // Stadium search
  const [stadiumSearch, setStadiumSearch] = useState('');
  const [stadiumResults, setStadiumResults] = useState<Stadium[]>([]);
  const [isSearchingStadium, setIsSearchingStadium] = useState(false);

  // Confirmation and submission
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState('');

  const handleSelectStadium = (stadium: Stadium) => {
    setFormData({ ...formData, stadium });
    setStadiumResults([]);
    setStadiumSearch('');
  };

  // Load stadium if stadiumId is provided (from navigation after adding stadium)
  useEffect(() => {
    if (stadiumId) {
      const loadStadium = async () => {
        try {
          // Search for the stadium by ID - we'll need to search and find it
          // Since we don't have a getStadiumById API, we'll search with empty string to get all
          // and then find the one with matching ID
          const results = await apiService.searchStadiums('');
          const foundStadium = results.find(s => s.id?.toString() === stadiumId);
          if (foundStadium) {
            setFormData(prev => ({ ...prev, stadium: foundStadium }));
            setStadiumResults([]);
            setStadiumSearch('');
            // Clear the stadiumId from URL
            navigate({
              to: '/tournaments/$leagueId/schedule/add-match',
              params: { leagueId },
              search: { roundId, stadiumId: undefined },
              replace: true,
            });
          }
        } catch (err) {
          console.error('Error loading stadium:', err);
        }
      };
      loadStadium();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [stadiumId, leagueId, roundId]);

  const handleSearchHomeTeam = async () => {
    if (!homeTeamSearch.trim()) {
      setHomeTeamResults([]);
      return;
    }

    setIsSearchingHomeTeam(true);
    try {
      const results = await apiService.searchLeagueTeams(homeTeamSearch.trim());
      setHomeTeamResults(results);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Có lỗi xảy ra khi tìm kiếm đội chủ nhà');
      setHomeTeamResults([]);
    } finally {
      setIsSearchingHomeTeam(false);
    }
  };

  const handleSearchAwayTeam = async () => {
    if (!awayTeamSearch.trim()) {
      setAwayTeamResults([]);
      return;
    }

    setIsSearchingAwayTeam(true);
    try {
      const results = await apiService.searchLeagueTeams(awayTeamSearch.trim());
      setAwayTeamResults(results);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Có lỗi xảy ra khi tìm kiếm đội khách');
      setAwayTeamResults([]);
    } finally {
      setIsSearchingAwayTeam(false);
    }
  };

  const handleSearchStadium = async () => {
    if (!stadiumSearch.trim()) {
      setStadiumResults([]);
      return;
    }

    setIsSearchingStadium(true);
    try {
      const results = await apiService.searchStadiums(stadiumSearch.trim());
      setStadiumResults(results);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Có lỗi xảy ra khi tìm kiếm sân vận động');
      setStadiumResults([]);
    } finally {
      setIsSearchingStadium(false);
    }
  };

  const handleSelectHomeTeam = (team: LeagueTeam) => {
    setFormData({ ...formData, homeTeam: team });
    setHomeTeamResults([]);
    setHomeTeamSearch('');
  };

  const handleSelectAwayTeam = (team: LeagueTeam) => {
    setFormData({ ...formData, awayTeam: team });
    setAwayTeamResults([]);
    setAwayTeamSearch('');
  };

  const handleAdd = () => {
    // Validate form
    if (!formData.date || !formData.time) {
      setError('Vui lòng nhập ngày và giờ thi đấu');
      return;
    }
    if (!formData.homeTeam) {
      setError('Vui lòng chọn đội chủ nhà');
      return;
    }
    if (!formData.awayTeam) {
      setError('Vui lòng chọn đội khách');
      return;
    }
    if (!formData.stadium) {
      setError('Vui lòng chọn sân vận động');
      return;
    }
    if (formData.homeTeam.id === formData.awayTeam.id) {
      setError('Đội chủ nhà và đội khách phải khác nhau');
      return;
    }

    setError('');
    setShowConfirmation(true);
  };

  const handleConfirm = async () => {
    setIsSubmitting(true);
    setError('');

    try {
      if (!roundId) {
        throw new Error('Vòng đấu không hợp lệ');
      }

      // Format date to yyyy-MM-dd
      const dateParts = formData.date.split('/');
      if (dateParts.length !== 3) {
        throw new Error('Định dạng ngày không hợp lệ');
      }
      const formattedDate = `${dateParts[2]}-${dateParts[1].padStart(2, '0')}-${dateParts[0].padStart(2, '0')}`;
      
      // Format time to HH:mm:ss
      const formattedTime = `${formData.time}:00`;

      // Create match object
      const match: Match = {
        date: formattedDate,
        timeStart: formattedTime,
        round: { id: parseInt(roundId, 10) } as Round,
        stadium: formData.stadium!,
        leagueTeamMatches: [
          {
            leagueTeam: formData.homeTeam!,
            role: 'Home',
          } as LeagueTeamMatch,
          {
            leagueTeam: formData.awayTeam!,
            role: 'Away',
          } as LeagueTeamMatch,
        ],
      };

      await apiService.createMatch(match);
      
      // Navigate back to schedule page
      navigate({ to: '/tournaments/$leagueId/schedule', params: { leagueId } });
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Có lỗi xảy ra khi thêm trận đấu');
      setIsSubmitting(false);
    }
  };

  const handleCancel = () => {
    if (showConfirmation) {
      setShowConfirmation(false);
    } else {
      navigate({ to: '/tournaments/$leagueId/schedule/select-round', params: { leagueId } });
    }
  };

  if (showConfirmation) {
    return (
      <ConfirmView
        formData={formData}
        error={error}
        isSubmitting={isSubmitting}
        onConfirm={handleConfirm}
        onCancel={handleCancel}
      />
    );
  }

  return (
    <div className="add-match-container">
      <div className="add-match-card">
        <div className="add-match-header">
          <h1>Thêm trận đấu</h1>
        </div>

        {error && <div className="error-message">{error}</div>}

        <div className="match-form">
          {/* Date and Time */}
          <div className="form-row">
            <div className="form-group">
              <label>Ngày thi đấu</label>
              <div className="input-with-icon">
                <input
                  type="date"
                  value={formData.date ? (() => {
                    // Convert dd/mm/yyyy to yyyy-mm-dd for date input
                    if (formData.date.includes('/')) {
                      const parts = formData.date.split('/');
                      if (parts.length === 3) {
                        return `${parts[2]}-${parts[1].padStart(2, '0')}-${parts[0].padStart(2, '0')}`;
                      }
                    }
                    return formData.date;
                  })() : ''}
                  onChange={(e) => {
                    if (e.target.value) {
                      const date = new Date(e.target.value);
                      const day = date.getDate();
                      const month = date.getMonth() + 1;
                      const year = date.getFullYear();
                      setFormData({ ...formData, date: `${day}/${month}/${year}` });
                    } else {
                      setFormData({ ...formData, date: '' });
                    }
                  }}
                />
                <span className="icon">📅</span>
              </div>
            </div>

            <div className="form-group">
              <label>Giờ thi đấu</label>
              <div className="input-with-icon">
                <input
                  type="time"
                  value={formData.time}
                  onChange={(e) => setFormData({ ...formData, time: e.target.value })}
                />
                <span className="icon">🕐</span>
              </div>
            </div>
          </div>

          {/* Home Team */}
          <div className="search-section">
            <label>Đội chủ nhà</label>
            <div className="search-input-group">
              <input
                type="text"
                className="search-input"
                placeholder="Nhập tên đội"
                value={homeTeamSearch}
                onChange={(e) => setHomeTeamSearch(e.target.value)}
                onKeyPress={(e) => {
                  if (e.key === 'Enter') {
                    handleSearchHomeTeam();
                  }
                }}
              />
              <button
                onClick={handleSearchHomeTeam}
                className="search-btn"
                disabled={isSearchingHomeTeam}
              >
                {isSearchingHomeTeam ? 'Đang tìm...' : 'Tìm kiếm'}
              </button>
            </div>
            {formData.homeTeam && (
              <div className="selected-item">
                Đã chọn: <strong>{formData.homeTeam.team?.fullName}</strong> ({formData.homeTeam.team?.shortName})
                <button
                  onClick={() => setFormData({ ...formData, homeTeam: null })}
                  className="remove-btn"
                >
                  ✕
                </button>
              </div>
            )}
            {homeTeamResults.length > 0 && (
              <div className="results-table-section">
                <table className="results-table">
                  <thead>
                    <tr>
                      <th>Tên đội</th>
                      <th>Tên viết tắt</th>
                      <th>Thao tác</th>
                    </tr>
                  </thead>
                  <tbody>
                    {homeTeamResults.map((team) => (
                      <tr key={team.id}>
                        <td>{team.team?.fullName}</td>
                        <td>{team.team?.shortName}</td>
                        <td>
                          <button
                            onClick={() => handleSelectHomeTeam(team)}
                            className="select-btn"
                          >
                            Chọn
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>

          {/* Away Team */}
          <div className="search-section">
            <label>Đội khách</label>
            <div className="search-input-group">
              <input
                type="text"
                className="search-input"
                placeholder="Nhập tên đội"
                value={awayTeamSearch}
                onChange={(e) => setAwayTeamSearch(e.target.value)}
                onKeyPress={(e) => {
                  if (e.key === 'Enter') {
                    handleSearchAwayTeam();
                  }
                }}
              />
              <button
                onClick={handleSearchAwayTeam}
                className="search-btn"
                disabled={isSearchingAwayTeam}
              >
                {isSearchingAwayTeam ? 'Đang tìm...' : 'Tìm kiếm'}
              </button>
            </div>
            {formData.awayTeam && (
              <div className="selected-item">
                Đã chọn: <strong>{formData.awayTeam.team?.fullName}</strong> ({formData.awayTeam.team?.shortName})
                <button
                  onClick={() => setFormData({ ...formData, awayTeam: null })}
                  className="remove-btn"
                >
                  ✕
                </button>
              </div>
            )}
            {awayTeamResults.length > 0 && (
              <div className="results-table-section">
                <table className="results-table">
                  <thead>
                    <tr>
                      <th>Tên đội</th>
                      <th>Tên viết tắt</th>
                      <th>Thao tác</th>
                    </tr>
                  </thead>
                  <tbody>
                    {awayTeamResults.map((team) => (
                      <tr key={team.id}>
                        <td>{team.team?.fullName}</td>
                        <td>{team.team?.shortName}</td>
                        <td>
                          <button
                            onClick={() => handleSelectAwayTeam(team)}
                            className="select-btn"
                          >
                            Chọn
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>

          {/* Stadium */}
          <div className="search-section">
            <label>Sân thi đấu</label>
            <div className="search-input-group">
              <input
                type="text"
                className="search-input"
                placeholder="Nhập tên sân vận động"
                value={stadiumSearch}
                onChange={(e) => setStadiumSearch(e.target.value)}
                onKeyPress={(e) => {
                  if (e.key === 'Enter') {
                    handleSearchStadium();
                  }
                }}
              />
              <button
                onClick={handleSearchStadium}
                className="search-btn"
                disabled={isSearchingStadium}
              >
                {isSearchingStadium ? 'Đang tìm...' : 'Tìm kiếm'}
              </button>
              {/* <button
                onClick={() => {
                  navigate({
                    to: '/stadiums/add',
                    search: {
                      returnUrl: `/tournaments/${leagueId}/schedule/add-match?roundId=${roundId}`,
                    },
                  });
                }}
                className="add-stadium-btn"
                type="button"
              >
                Thêm sân vận động
              </button> */}
            </div>
            {formData.stadium && (
              <div className="selected-item">
                Đã chọn: <strong>{formData.stadium.name}</strong>
                <button
                  onClick={() => setFormData({ ...formData, stadium: null })}
                  className="remove-btn"
                >
                  ✕
                </button>
              </div>
            )}
            {stadiumResults.length > 0 && (
              <div className="results-table-section">
                <table className="results-table">
                  <thead>
                    <tr>
                      <th>Tên SVĐ</th>
                      <th>Thao tác</th>
                    </tr>
                  </thead>
                  <tbody>
                    {stadiumResults.map((stadium) => (
                      <tr key={stadium.id}>
                        <td>{stadium.name}</td>
                        <td>
                          <button
                            onClick={() => handleSelectStadium(stadium)}
                            className="select-btn"
                          >
                            Chọn
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>

          {/* Actions */}
          <div className="add-match-actions">
            <button onClick={handleCancel} className="cancel-btn">
              Huỷ
            </button>
            <button onClick={handleAdd} className="add-btn">
              Thêm
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

