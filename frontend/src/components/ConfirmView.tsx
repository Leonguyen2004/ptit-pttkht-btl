import { type LeagueTeam, type Stadium } from '../services/api';
import '../routes/auth.css';

interface MatchFormData {
  date: string;
  time: string;
  homeTeam: LeagueTeam | null;
  awayTeam: LeagueTeam | null;
  stadium: Stadium | null;
}

interface ConfirmViewProps {
  formData: MatchFormData;
  error: string;
  isSubmitting: boolean;
  onConfirm: () => void;
  onCancel: () => void;
}

export function ConfirmView({ formData, error, isSubmitting, onConfirm, onCancel }: ConfirmViewProps) {
  const formatDateForDisplay = (dateString: string): string => {
    if (!dateString) return '';
    try {
      let date: Date;
      // Handle dd/mm/yyyy format
      if (dateString.includes('/')) {
        const parts = dateString.split('/');
        if (parts.length === 3) {
          date = new Date(parseInt(parts[2], 10), parseInt(parts[1], 10) - 1, parseInt(parts[0], 10));
        } else {
          return dateString;
        }
      } else {
        date = new Date(dateString);
      }
      const days = ['Chủ nhật', 'Thứ 2', 'Thứ 3', 'Thứ 4', 'Thứ 5', 'Thứ 6', 'Thứ 7'];
      const dayName = days[date.getDay()];
      const day = date.getDate().toString().padStart(2, '0');
      const month = (date.getMonth() + 1).toString().padStart(2, '0');
      const year = date.getFullYear();
      return `${dayName}, ${day}/${month}/${year}`;
    } catch {
      return dateString;
    }
  };

  return (
    <div className="add-match-container">
      <div className="add-match-card">
        <div className="add-match-header">
          <h1>Xác nhận thông tin</h1>
        </div>

        {error && <div className="error-message">{error}</div>}

        <div className="confirmation-table-section">
          <table className="confirmation-table">
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
              <tr>
                <td>{formatDateForDisplay(formData.date)}</td>
                <td>{formData.time}</td>
                <td>{formData.stadium?.name || ''}</td>
                <td>{formData.homeTeam?.team?.fullName || ''}</td>
                <td>{formData.awayTeam?.team?.fullName || ''}</td>
              </tr>
            </tbody>
          </table>
        </div>

        <div className="add-match-actions">
          <button onClick={onCancel} className="cancel-btn" disabled={isSubmitting}>
            Huỷ
          </button>
          <button onClick={onConfirm} className="confirm-btn" disabled={isSubmitting}>
            {isSubmitting ? 'Đang xử lý...' : 'Xác nhận'}
          </button>
        </div>
      </div>
    </div>
  );
}

