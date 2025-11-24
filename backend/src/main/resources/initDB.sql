-- Bảng không có khoá ngoại (hoặc ít phụ thuộc)

CREATE TABLE IF NOT EXISTS Employee (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    date_of_birth DATE,
    address VARCHAR(255),
    email VARCHAR(255) UNIQUE,
    phone_number VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS Stadium (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(255),
    capacity INT
);

CREATE TABLE IF NOT EXISTS League (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    start_date DATE,
    end_date DATE,
    description TEXT
);

-- Bảng phụ thuộc vào các bảng trên

CREATE TABLE IF NOT EXISTS Staff (
    id SERIAL PRIMARY KEY,
    "role" VARCHAR(100), -- "role" là từ khoá, nên dùng ngoặc kép
    employee_id INT NOT NULL UNIQUE, -- UNIQUE để đảm bảo quan hệ 1-1
    FOREIGN KEY (employee_id) REFERENCES Employee(id)
);

CREATE TABLE IF NOT EXISTS Team (
    id SERIAL PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    short_name VARCHAR(50),
    head_coach VARCHAR(255),
    home_kit_color VARCHAR(50),
    away_kit_color VARCHAR(50),
    achievements TEXT,
    stadium_id INT,
    FOREIGN KEY (stadium_id) REFERENCES Stadium(id)
);

CREATE TABLE IF NOT EXISTS Round (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    start_date DATE,
    end_date DATE,
    description TEXT,
    league_id INT NOT NULL,
    FOREIGN KEY (league_id) REFERENCES League(id)
);

-- Bảng "Match" (MATCH là từ khoá SQL, cần dùng ngoặc kép)
CREATE TABLE IF NOT EXISTS match (
    id SERIAL PRIMARY KEY,
    match_date DATE,
    time_start TIME,
    description TEXT,
    stadium_id INT,
    round_id INT NOT NULL,
    FOREIGN KEY (stadium_id) REFERENCES Stadium(id),
    FOREIGN KEY (round_id) REFERENCES Round(id)
);

-- Bảng join (bảng quan hệ)

CREATE TABLE IF NOT EXISTS LeagueTeam (
    id SERIAL PRIMARY KEY,
    league_id INT NOT NULL,
    team_id INT NOT NULL,
    FOREIGN KEY (league_id) REFERENCES League(id),
    FOREIGN KEY (team_id) REFERENCES Team(id),
    UNIQUE(league_id, team_id) -- Đảm bảo một đội chỉ tham gia một giải đấu một lần
);

CREATE TABLE IF NOT EXISTS LeagueTeamMatch (
    id SERIAL PRIMARY KEY,
    "role" VARCHAR(50), -- (ví dụ: 'home', 'away')
    goal INT,
    result VARCHAR(50), -- (ví dụ: 'Win', 'Loss', 'Draw')
    league_team_id INT NOT NULL,
    match_id INT NOT NULL,
    FOREIGN KEY (league_team_id) REFERENCES LeagueTeam(id),
    FOREIGN KEY (match_id) REFERENCES "Match"(id)
);