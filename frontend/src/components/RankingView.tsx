import { useEffect, useState } from 'react';
import { useNavigate } from '@tanstack/react-router';
import { apiService, type RankingDTO } from '../services/api';
import '../routes/auth.css';

interface RankingViewProps {
  leagueId: string;
}

export function RankingView({ leagueId }: RankingViewProps) {
  const navigate = useNavigate();
  const [rankings, setRankings] = useState<RankingDTO[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchRankings = async () => {
      setIsLoading(true);
      setError('');

      try {
        const id = parseInt(leagueId, 10);
        if (isNaN(id)) {
          setError('ID giải đấu không hợp lệ');
          setIsLoading(false);
          return;
        }

        const data = await apiService.getRankingByLeague(id);
        setRankings(data);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Có lỗi xảy ra khi tải bảng xếp hạng');
      } finally {
        setIsLoading(false);
      }
    };

    fetchRankings();
  }, [leagueId]);

  const handleBack = () => {
    navigate({ to: '/tournaments/$leagueId', params: { leagueId } });
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

  if (error) {
    return (
      <div className="tournaments-container">
        <div className="tournaments-card">
          <div className="error-message">{error}</div>
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
          <h1>Bảng xếp hạng các đội bóng</h1>
        </div>

        <div className="tournaments-footer" style={{ justifyContent: "flex-start", marginBottom: "24px" }}>
          <button onClick={handleBack} className="back-btn">
            Quay lại
          </button>
        </div>

        <div style={{ overflowX: 'auto' }}>
          <table style={{
            width: '100%',
            borderCollapse: 'collapse',
            backgroundColor: 'white',
            boxShadow: '0 1px 3px rgba(0,0,0,0.1)'
          }}>
            <thead>
              <tr style={{ backgroundColor: '#dc2626', color: 'white' }}>
                <th style={{ padding: '12px 8px', textAlign: 'center', border: '1px solid #ddd' }}>TH</th>
                <th style={{ padding: '12px 8px', textAlign: 'left', border: '1px solid #ddd' }}>Đội bóng</th>
                <th style={{ padding: '12px 8px', textAlign: 'center', border: '1px solid #ddd' }}>Trận</th>
                <th style={{ padding: '12px 8px', textAlign: 'center', border: '1px solid #ddd' }}>T</th>
                <th style={{ padding: '12px 8px', textAlign: 'center', border: '1px solid #ddd' }}>H</th>
                <th style={{ padding: '12px 8px', textAlign: 'center', border: '1px solid #ddd' }}>B</th>
                <th style={{ padding: '12px 8px', textAlign: 'center', border: '1px solid #ddd' }}>BT-BB</th>
                <th style={{ padding: '12px 8px', textAlign: 'center', border: '1px solid #ddd' }}>HS</th>
                <th style={{ padding: '12px 8px', textAlign: 'center', border: '1px solid #ddd' }}>TY</th>
                <th style={{ padding: '12px 8px', textAlign: 'center', border: '1px solid #ddd' }}>TD</th>
                <th style={{ padding: '12px 8px', textAlign: 'center', border: '1px solid #ddd' }}>Điểm</th>
                <th style={{ padding: '12px 8px', textAlign: 'center', border: '1px solid #ddd' }}>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              {rankings.map((ranking, index) => (
                <tr key={ranking.leagueTeamId} style={{ 
                  backgroundColor: index % 2 === 0 ? 'white' : '#f9fafb',
                  borderBottom: '1px solid #e5e7eb'
                }}>
                  <td style={{ padding: '12px 8px', textAlign: 'center', border: '1px solid #ddd' }}>{ranking.rank}</td>
                  <td style={{ padding: '12px 8px', textAlign: 'left', border: '1px solid #ddd' }}>{ranking.teamName}</td>
                  <td style={{ padding: '12px 8px', textAlign: 'center', border: '1px solid #ddd' }}>{ranking.played}</td>
                  <td style={{ padding: '12px 8px', textAlign: 'center', border: '1px solid #ddd' }}>{ranking.won}</td>
                  <td style={{ padding: '12px 8px', textAlign: 'center', border: '1px solid #ddd' }}>{ranking.drawn}</td>
                  <td style={{ padding: '12px 8px', textAlign: 'center', border: '1px solid #ddd' }}>{ranking.lost}</td>
                  <td style={{ padding: '12px 8px', textAlign: 'center', border: '1px solid #ddd' }}>
                    {ranking.goalsFor} - {ranking.goalsAgainst}
                  </td>
                  <td style={{ padding: '12px 8px', textAlign: 'center', border: '1px solid #ddd' }}>{ranking.goalDifference}</td>
                  <td style={{ padding: '12px 8px', textAlign: 'center', border: '1px solid #ddd' }}>0</td>
                  <td style={{ padding: '12px 8px', textAlign: 'center', border: '1px solid #ddd' }}>0</td>
                  <td style={{ padding: '12px 8px', textAlign: 'center', border: '1px solid #ddd', fontWeight: 'bold' }}>
                    {ranking.points}
                  </td>
                  <td style={{ padding: '12px 8px', textAlign: 'center', border: '1px solid #ddd' }}>
                    <button 
                      style={{
                        backgroundColor: '#3b82f6',
                        color: 'white',
                        padding: '6px 12px',
                        border: 'none',
                        borderRadius: '4px',
                        cursor: 'pointer',
                        fontSize: '14px'
                      }}
                      onClick={() => {
                        navigate({ 
                          to: '/tournaments/$leagueId/history/$leagueTeamId', 
                          params: { leagueId, leagueTeamId: String(ranking.leagueTeamId) }
                        });
                      }}
                    >
                      Xem
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
