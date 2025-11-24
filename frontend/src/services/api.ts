const API_BASE_URL = 'http://localhost:8080';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  employeeId: number;
  username: string;
  email: string;
  address?: string;
  phoneNumber?: string;
  dateOfBirth?: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  email: string;
  dateOfBirth?: string;
  address?: string;
  phoneNumber?: string;
}

export interface RegisterResponse {
  message: string;
  username: string;
}

export interface Employee {
  id: number;
  username: string;
  email: string;
  address?: string;
  phoneNumber?: string;
  dateOfBirth?: string;
}

export interface ApiError {
  error: string;
}

export interface League {
  id: number;
  name: string;
  startDate: string; // Format: "yyyy-MM-dd"
  endDate: string; // Format: "yyyy-MM-dd"
  description?: string;
}

export interface Team {
  id?: number;
  fullName: string;
  shortName?: string;
  headCoach?: string;
  homeKitColor?: string;
  awayKitColor?: string;
  achievements?: string;
  logo?: string;
  stadium?: Stadium;
}

export interface Stadium {
  id?: number;
  name?: string;
  address?: string;
  capacity?: number;
}

export interface Round {
  id: number;
  name: string;
  startDate?: string; // Format: "yyyy-MM-dd"
  endDate?: string; // Format: "yyyy-MM-dd"
  description?: string;
  league?: League;
}

export interface LeagueTeam {
  id?: number;
  team?: Team;
  league?: League;
}

export interface LeagueTeamMatch {
  id?: number;
  role?: string; // 'home' or 'away'
  goal?: number;
  result?: string;
  leagueTeam?: LeagueTeam;
}

export interface Match {
  id?: number;
  date: string; // Format: "yyyy-MM-dd"
  timeStart: string; // Format: "HH:mm:ss"
  description?: string;
  stadium?: Stadium;
  round?: Round;
  leagueTeamMatches?: LeagueTeamMatch[];
}

class ApiService {
  private async request<T>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<T> {
    const headers: HeadersInit = {
      'Content-Type': 'application/json',
      ...options.headers,
    };

    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
      ...options,
      headers,
    });

    const data = await response.json();

    if (!response.ok) {
      throw new Error((data as ApiError).error || 'An error occurred');
    }

    return data as T;
  }

  async login(credentials: LoginRequest): Promise<LoginResponse> {
    return this.request<LoginResponse>('/api/employee/login', {
      method: 'POST',
      body: JSON.stringify(credentials),
    });
  }

  async register(data: RegisterRequest): Promise<RegisterResponse> {
    return this.request<RegisterResponse>('/api/employee/register', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  async getMe(employeeId: number): Promise<Employee> {
    return this.request<Employee>(`/api/employee/me?employeeId=${employeeId}`, {
      method: 'GET',
    });
  }

  async searchLeagues(name: string): Promise<League[]> {
    const encodedName = encodeURIComponent(name);
    return this.request<League[]>(`/api/leagues?name=${encodedName}`, {
      method: 'GET',
    });
  }

  async getRoundsByLeagueId(leagueId: number): Promise<Round[]> {
    return this.request<Round[]>(`/api/rounds?leagueId=${leagueId}`, {
      method: 'GET',
    });
  }

  async searchRoundsByLeagueIdAndName(leagueId: number, name: string): Promise<Round[]> {
    const encodedName = encodeURIComponent(name);
    return this.request<Round[]>(`/api/rounds?leagueId=${leagueId}&name=${encodedName}`, {
      method: 'GET',
    });
  }

  async getMatchesByLeagueId(leagueId: number): Promise<Match[]> {
    return this.request<Match[]>(`/api/matches?leagueId=${leagueId}`, {
      method: 'GET',
    });
  }

  async getLeagueTeamMatchesByMatchId(matchId: number): Promise<LeagueTeamMatch[]> {
    return this.request<LeagueTeamMatch[]>(`/api/league-team-matches?matchId=${matchId}`, {
      method: 'GET',
    });
  }

  async searchLeagueTeams(name: string): Promise<LeagueTeam[]> {
    const encodedName = encodeURIComponent(name);
    return this.request<LeagueTeam[]>(`/api/league-teams?name=${encodedName}`, {
      method: 'GET',
    });
  }

  async searchStadiums(name: string): Promise<Stadium[]> {
    const encodedName = encodeURIComponent(name);
    return this.request<Stadium[]>(`/api/stadiums?name=${encodedName}`, {
      method: 'GET',
    });
  }

  async createMatch(match: Match): Promise<Match> {
    return this.request<Match>('/api/matches', {
      method: 'POST',
      body: JSON.stringify(match),
    });
  }

  async getTeams(): Promise<Team[]> {
    return this.request<Team[]>('/api/teams', {
      method: 'GET',
    });
  }

  async createTeam(team: Team, logoFile?: File): Promise<Team> {
    if (logoFile) {
      // Use FormData for multipart/form-data
      const formData = new FormData();
      formData.append('fullName', team.fullName);
      if (team.shortName) formData.append('shortName', team.shortName);
      if (team.headCoach) formData.append('headCoach', team.headCoach);
      if (team.homeKitColor) formData.append('homeKitColor', team.homeKitColor);
      if (team.awayKitColor) formData.append('awayKitColor', team.awayKitColor);
      if (team.achievements) formData.append('achievements', team.achievements);
      if (team.stadium?.id) formData.append('stadiumId', team.stadium.id.toString());
      formData.append('logo', logoFile);

      // Don't set Content-Type header - browser will set it automatically with boundary for FormData
      const response = await fetch(`${API_BASE_URL}/api/teams`, {
        method: 'POST',
        body: formData,
      });

      const data = await response.json();

      if (!response.ok) {
        throw new Error((data as ApiError).error || 'An error occurred');
      }

      return data as Team;
    } else {
      // Use JSON for text-only
      return this.request<Team>('/api/teams', {
        method: 'POST',
        body: JSON.stringify(team),
      });
    }
  }

  async createStadium(stadium: Stadium): Promise<Stadium> {
    return this.request<Stadium>('/api/stadiums', {
      method: 'POST',
      body: JSON.stringify(stadium),
    });
  }
}

export const apiService = new ApiService();

