import { useEffect, useState, useMemo } from 'react';
import React from 'react';
import { useNavigate } from '@tanstack/react-router';
import { apiService, type Round, type Match, type League, type LeagueTeamMatch } from '../services/api';
import '../routes/auth.css';

interface ManageScheduleViewProps {
  leagueId: string;
}

export function ManageScheduleView({ leagueId }: ManageScheduleViewProps) {
  const navigate = useNavigate();
  const [league, setLeague] = useState<League | null>(null);
  const [rounds, setRounds] = useState<Round[]>([]);
  const [matches, setMatches] = useState<Match[]>([]);
  const [teamMatchesMap, setTeamMatchesMap] = useState<Record<number, LeagueTeamMatch[]>>({});
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchData = async () => {
      setIsLoading(true);
      setError('');

      try {
        const id = parseInt(leagueId, 10);
        if (isNaN(id)) {
          setError('ID giải đấu không hợp lệ');
          setIsLoading(false);
          return;
        }

        // Lấy thông tin giải đấu từ localStorage hoặc tìm kiếm
        const storedLeague = localStorage.getItem('selectedLeague');
        if (storedLeague) {
          try {
            const parsedLeague = JSON.parse(storedLeague) as League;
            if (parsedLeague.id === id) {
              setLeague(parsedLeague);
            }
          } catch (e) {
            // Ignore
          }
        }

        // Lấy danh sách vòng và trận đấu
        const [roundsData, matchesData] = await Promise.all([
          apiService.getRoundsByLeagueId(id),
          apiService.getMatchesByLeagueId(id),
        ]);

        setRounds(roundsData);
        setMatches(matchesData);

        // Nếu chưa có thông tin giải đấu, lấy từ rounds hoặc matches
        if (!league && roundsData.length > 0 && roundsData[0].league) {
          setLeague(roundsData[0].league);
        }

        // Lấy thông tin đội cho từng trận đấu
        const teamMatches: Record<number, LeagueTeamMatch[]> = {};
        await Promise.all(
          matchesData.map(async (match) => {
            if (!match.id) {
              return;
            }
            try {
              const leagueTeamMatches = await apiService.getLeagueTeamMatchesByMatchId(match.id);
              teamMatches[match.id] = leagueTeamMatches;
              // Debug log để kiểm tra dữ liệu
              if (leagueTeamMatches.length > 0) {
                console.log(`Match ${match.id} teams:`, leagueTeamMatches.map((ltm: LeagueTeamMatch) => ({
                  role: ltm.role,
                  teamName: ltm.leagueTeam?.team?.fullName
                })));
              }
            } catch (err) {
              console.error(`Error fetching team matches for match ${match.id}:`, err);
              teamMatches[match.id] = [];
            }
          })
        );
        setTeamMatchesMap(teamMatches);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Có lỗi xảy ra khi tải dữ liệu');
      } finally {
        setIsLoading(false);
      }
    };

    fetchData();
  }, [leagueId]);

  // Nhóm các trận đấu theo vòng và sắp xếp
  const matchesByRound = useMemo(() => {
    const grouped: Record<number, Match[]> = {};
    
    // Khởi tạo với tất cả các vòng
    rounds.forEach((round) => {
      grouped[round.id] = [];
    });

    // Phân loại trận đấu theo vòng
    matches.forEach((match) => {
      if (match.round?.id) {
        if (!grouped[match.round.id]) {
          grouped[match.round.id] = [];
        }
        grouped[match.round.id].push(match);
      }
    });

    // Sắp xếp các trận đấu trong mỗi vòng theo ngày và giờ
    Object.keys(grouped).forEach((roundId) => {
      grouped[parseInt(roundId)].sort((a, b) => {
        const dateA = a.date ? new Date(a.date).getTime() : 0;
        const dateB = b.date ? new Date(b.date).getTime() : 0;
        if (dateA !== dateB) return dateA - dateB;
        
        const timeA = a.timeStart || '00:00:00';
        const timeB = b.timeStart || '00:00:00';
        return timeA.localeCompare(timeB);
      });
    });

    return grouped;
  }, [rounds, matches]);

  const handleBack = () => {
    navigate({ to: '/tournaments/$leagueId', params: { leagueId } });
  };

  const handleAddMatch = () => {
    navigate({ to: '/tournaments/$leagueId/schedule/select-round', params: { leagueId } });
  };

  const formatDate = (dateString: string | undefined): string => {
    if (!dateString) return '';
    try {
      const date = new Date(dateString);
      const days = ['Chủ nhật', 'Thứ 2', 'Thứ 3', 'Thứ 4', 'Thứ 5', 'Thứ 6', 'Thứ 7'];
      const dayName = days[date.getDay()];
      const day = date.getDate().toString().padStart(2, '0');
      const month = (date.getMonth() + 1).toString().padStart(2, '0');
      const year = date.getFullYear();
      return `${dayName}, ${day}/${month}/${year}`;
    } catch {
      return dateString || '';
    }
  };

  const formatTime = (timeString: string | undefined): string => {
    if (!timeString) return '';
    try {
      // Format: "HH:mm:ss" -> "HH:mm"
      const parts = timeString.split(':');
      return `${parts[0]}:${parts[1]}`;
    } catch {
      return timeString;
    }
  };

  const getHomeTeam = (match: Match): string => {
    const teamMatches = (match.id ? teamMatchesMap[match.id] : undefined) || match.leagueTeamMatches || [];
    
    if (teamMatches.length === 0) {
      return 'N/A';
    }

    // So sánh không phân biệt hoa thường vì role có thể là "Home" hoặc "home"
    const homeTeam = teamMatches.find((ltm: LeagueTeamMatch) => 
      ltm.role && ltm.role.toLowerCase() === 'home'
    );
    
    if (homeTeam?.leagueTeam?.team?.fullName) {
      return homeTeam.leagueTeam.team.fullName;
    }

    // Fallback: Nếu không tìm thấy role='home', lấy đội đầu tiên
    if (teamMatches.length > 0 && teamMatches[0].leagueTeam?.team?.fullName) {
      return teamMatches[0].leagueTeam.team.fullName;
    }

    return 'N/A';
  };

  const getAwayTeam = (match: Match): string => {
    const teamMatches = (match.id ? teamMatchesMap[match.id] : undefined) || match.leagueTeamMatches || [];
    
    if (teamMatches.length === 0) {
      return 'N/A';
    }

    // So sánh không phân biệt hoa thường vì role có thể là "Away" hoặc "away"
    const awayTeam = teamMatches.find((ltm: LeagueTeamMatch) => 
      ltm.role && ltm.role.toLowerCase() === 'away'
    );
    
    if (awayTeam?.leagueTeam?.team?.fullName) {
      return awayTeam.leagueTeam.team.fullName;
    }

    // Fallback: Nếu không tìm thấy role='away', lấy đội thứ hai (nếu có)
    if (teamMatches.length > 1 && teamMatches[1].leagueTeam?.team?.fullName) {
      return teamMatches[1].leagueTeam.team.fullName;
    }

    return 'N/A';
  };

  if (isLoading) {
    return (
      <div className="schedule-container">
        <div className="schedule-card">
          <div className="loading-message">Đang tải...</div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="schedule-container">
        <div className="schedule-card">
          <div className="error-message">{error}</div>
          <div className="schedule-footer">
            <button onClick={handleBack} className="back-btn">
              Quay lại
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="schedule-container">
      <div className="schedule-card">
        <div className="schedule-header">
          <h1>Quản lý lịch thi đấu</h1>
        </div>

        <div className="schedule-header-actions">
          <button onClick={handleBack} className="back-btn">
            Quay lại
          </button>
          <button onClick={handleAddMatch} className="add-match-btn">
            Thêm trận đấu
          </button>
        </div>

        {league && (
          <div className="league-name-section">
            <h2>{league.name}</h2>
          </div>
        )}

        <div className="schedule-table-section">
          <table className="schedule-table">
            <thead>
              <tr>
                <th>Ngày</th>
                <th>Giờ</th>
                <th>SVĐ</th>
                <th>Đội chủ nhà</th>
                <th>Đội khách</th>
              </tr>
            </thead>
            <tbody>
              {rounds.length === 0 ? (
                <tr>
                  <td colSpan={5} className="empty-message">
                    Chưa có lịch thi đấu
                  </td>
                </tr>
              ) : (
                rounds.map((round) => {
                  const roundMatches = matchesByRound[round.id] || [];
                  if (roundMatches.length === 0) return null;

                  return (
                    <React.Fragment key={round.id}>
                      <tr className="round-header-row">
                        <td colSpan={5} className="round-header">
                          {round.name} {league?.name ? league.name : ''}
                        </td>
                      </tr>
                      {roundMatches.map((match) => (
                        <tr key={match.id}>
                          <td>{formatDate(match.date)}</td>
                          <td>{formatTime(match.timeStart)}</td>
                          <td>{match.stadium?.name ? `SVĐ ${match.stadium.name}` : `N/A`}</td>
                          <td>{getHomeTeam(match)}</td>
                          <td>{getAwayTeam(match)}</td>
                        </tr>
                      ))}
                    </React.Fragment>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

