import { type Team, type Stadium } from '../services/api';
import '../routes/auth.css';

interface TeamFormData {
  fullName: string;
  shortName: string;
  headCoach: string;
  homeKitColor: string;
  awayKitColor: string;
  achievements: string;
  logoFile: File | null;
  logoPreview: string;
  stadium: Stadium | null;
}

interface ConfirmTeamViewProps {
  formData: TeamFormData;
  error: string;
  isSubmitting: boolean;
  onConfirm: () => void;
  onCancel: () => void;
}

export function ConfirmTeamView({ formData, error, isSubmitting, onConfirm, onCancel }: ConfirmTeamViewProps) {
  return (
    <div className="add-team-container">
      <div className="add-team-card">
        <div className="add-team-header">
          <h1>Xác nhận thông tin</h1>
        </div>

        {error && <div className="error-message">{error}</div>}

        <div className="confirmation-table-section">
          <table className="confirmation-table">
            <tbody>
              <tr>
                <td className="confirmation-label">Tên đội bóng</td>
                <td className="confirmation-value">{formData.fullName || '-'}</td>
              </tr>
              <tr>
                <td className="confirmation-label">Tên viết tắt</td>
                <td className="confirmation-value">{formData.shortName || '-'}</td>
              </tr>
              <tr>
                <td className="confirmation-label">Huấn luyện viên trưởng</td>
                <td className="confirmation-value">{formData.headCoach || '-'}</td>
              </tr>
              <tr>
                <td className="confirmation-label">Logo đội bóng</td>
                <td className="confirmation-value">
                  {formData.logoPreview && (
                    <img
                      src={formData.logoPreview}
                      alt="Team logo"
                      className="confirmation-logo"
                      onError={(e) => {
                        (e.target as HTMLImageElement).src = 'https://via.placeholder.com/150/CCCCCC/666666?text=LOGO';
                      }}
                    />
                  )}
                </td>
              </tr>
              <tr>
                <td className="confirmation-label">Màu áo chính</td>
                <td className="confirmation-value">{formData.homeKitColor || '-'}</td>
              </tr>
              <tr>
                <td className="confirmation-label">Màu áo phụ</td>
                <td className="confirmation-value">{formData.awayKitColor || '-'}</td>
              </tr>
              <tr>
                <td className="confirmation-label">Thông tin thành tích</td>
                <td className="confirmation-value">{formData.achievements || '-'}</td>
              </tr>
              <tr>
                <td className="confirmation-label">Sân nhà</td>
                <td className="confirmation-value">{formData.stadium?.name ? `SVĐ ${formData.stadium.name}` : '-'}</td>
              </tr>
            </tbody>
          </table>
        </div>

        <div className="add-team-actions">
          <button onClick={onCancel} className="cancel-btn" disabled={isSubmitting}>
            Quay lại
          </button>
          <button onClick={onConfirm} className="submit-btn" disabled={isSubmitting}>
            {isSubmitting ? 'Đang xử lý...' : 'Xác nhận'}
          </button>
        </div>
      </div>
    </div>
  );
}

