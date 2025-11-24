import { useEffect, useState } from 'react';
import { useNavigate } from '@tanstack/react-router';
import { apiService, type Match } from '../services/api';
import '../routes/auth.css';

interface HistoryMatchViewProps {
  leagueTeamId: string;
  leagueId: string;
}

export function HistoryMatchView({ leagueTeamId, leagueId }: HistoryMatchViewProps) {
  const navigate = useNavigate();
  const [matches, setMatches] = useState<Match[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [teamName, setTeamName] = useState('');

  useEffect(() => {
    const fetchMatches = async () => {
      setIsLoading(true);
      setError('');

      try {
        const id = parseInt(leagueTeamId, 10);
        if (isNaN(id)) {
          setError('ID đội bóng không hợp lệ');
          setIsLoading(false);
          return;
        }

        const data = await apiService.getMatchesByLeagueTeamId(id);
        setMatches(data);

        // Extract team name from first match if available
        if (data.length > 0 && data[0].leagueTeamMatches) {
          const teamMatch = data[0].leagueTeamMatches.find(
            ltm => ltm.leagueTeam?.id === id
          );
          if (teamMatch?.leagueTeam?.team?.fullName) {
            setTeamName(teamMatch.leagueTeam.team.fullName);
          }
        }
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Có lỗi xảy ra khi tải lịch sử thi đấu');
      } finally {
        setIsLoading(false);
      }
    };

    fetchMatches();
  }, [leagueTeamId]);

  const handleBack = () => {
    navigate({ to: '/tournaments/$leagueId/ranking', params: { leagueId } });
  };

  const formatDate = (dateStr: string) => {
    const date = new Date(dateStr);
    const daysOfWeek = ['Chủ Nhật', 'Thứ 2', 'Thứ 3', 'Thứ 4', 'Thứ 5', 'Thứ 6', 'Thứ 7'];
    const dayOfWeek = daysOfWeek[date.getDay()];
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const year = date.getFullYear();
    return `${dayOfWeek}, ${day}/${month}/${year}`;
  };

  const formatTime = (timeStr: string) => {
    // timeStr is in format "HH:mm:ss"
    return timeStr.substring(0, 5); // Return "HH:mm"
  };

  const getMatchInfo = (match: Match) => {
    if (!match.leagueTeamMatches || match.leagueTeamMatches.length < 2) {
      return { homeTeam: 'N/A', awayTeam: 'N/A', score: 'N/A' };
    }

    const homeMatch = match.leagueTeamMatches.find(ltm => ltm.role === 'Home');
    const awayMatch = match.leagueTeamMatches.find(ltm => ltm.role === 'Away');

    const homeTeam = homeMatch?.leagueTeam?.team?.fullName || 'N/A';
    const awayTeam = awayMatch?.leagueTeam?.team?.fullName || 'N/A';
    const homeGoal = homeMatch?.goal ?? 0;
    const awayGoal = awayMatch?.goal ?? 0;
    const score = `${homeGoal} - ${awayGoal}`;

    return { homeTeam, awayTeam, score };
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
          <h1>Lịch sử thi đấu</h1>
          {teamName && <p style={{ marginTop: '8px', fontSize: '16px' }}>{teamName}</p>}
        </div>

        <div className="tournaments-footer" style={{ justifyContent: "flex-start", marginBottom: "24px" }}>
          <button onClick={handleBack} className="back-btn">
            Quay lại
          </button>
        </div>

        {matches.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '24px', color: '#6b7280' }}>
            Chưa có trận đấu nào
          </div>
        ) : (
          <div style={{ overflowX: 'auto' }}>
            <table style={{
              width: '100%',
              borderCollapse: 'collapse',
              backgroundColor: 'white',
              boxShadow: '0 1px 3px rgba(0,0,0,0.1)'
            }}>
              <thead>
                <tr style={{ backgroundColor: '#dc2626', color: 'white' }}>
                  <th style={{ padding: '12px 8px', textAlign: 'left', border: '1px solid #ddd' }}>Ngày</th>
                  <th style={{ padding: '12px 8px', textAlign: 'center', border: '1px solid #ddd' }}>Giờ</th>
                  <th style={{ padding: '12px 8px', textAlign: 'left', border: '1px solid #ddd' }}>SVĐ</th>
                  <th style={{ padding: '12px 8px', textAlign: 'left', border: '1px solid #ddd' }}>Đội chủ nhà</th>
                  <th style={{ padding: '12px 8px', textAlign: 'center', border: '1px solid #ddd' }}>Tỉ số</th>
                  <th style={{ padding: '12px 8px', textAlign: 'left', border: '1px solid #ddd' }}>Đội khách</th>
                </tr>
              </thead>
              <tbody>
                {matches.map((match, index) => {
                  const { homeTeam, awayTeam, score } = getMatchInfo(match);
                  return (
                    <tr key={match.id} style={{ 
                      backgroundColor: index % 2 === 0 ? 'white' : '#f9fafb',
                      borderBottom: '1px solid #e5e7eb'
                    }}>
                      <td style={{ padding: '12px 8px', textAlign: 'left', border: '1px solid #ddd' }}>
                        {formatDate(match.date)}
                      </td>
                      <td style={{ padding: '12px 8px', textAlign: 'center', border: '1px solid #ddd' }}>
                        {formatTime(match.timeStart)}
                      </td>
                      <td style={{ padding: '12px 8px', textAlign: 'left', border: '1px solid #ddd' }}>
                        {match.stadium?.name || 'N/A'}
                      </td>
                      <td style={{ padding: '12px 8px', textAlign: 'left', border: '1px solid #ddd' }}>
                        {homeTeam}
                      </td>
                      <td style={{ padding: '12px 8px', textAlign: 'center', border: '1px solid #ddd', fontWeight: 'bold' }}>
                        {score}
                      </td>
                      <td style={{ padding: '12px 8px', textAlign: 'left', border: '1px solid #ddd' }}>
                        {awayTeam}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
