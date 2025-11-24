import { useState, useEffect } from 'react';
import { useNavigate } from '@tanstack/react-router';
import { apiService, type Team, type Stadium } from '../services/api';
import { ConfirmTeamView } from './ConfirmTeamView';
import '../routes/auth.css';

interface AddTeamViewProps {
  stadiumId?: string;
}

export function AddTeamView({ stadiumId }: AddTeamViewProps) {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    fullName: '',
    shortName: '',
    headCoach: '',
    homeKitColor: '',
    awayKitColor: '',
    achievements: '',
  });
  const [logoFile, setLogoFile] = useState<File | null>(null);
  const [logoPreview, setLogoPreview] = useState<string>('');
  const [stadiumSearch, setStadiumSearch] = useState('');
  const [stadiumResults, setStadiumResults] = useState<Stadium[]>([]);
  const [selectedStadium, setSelectedStadium] = useState<Stadium | null>(null);
  const [isSearchingStadium, setIsSearchingStadium] = useState(false);
  const [showConfirmation, setShowConfirmation] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState('');

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

  const handleSelectStadium = (stadium: Stadium) => {
    setSelectedStadium(stadium);
    setStadiumResults([]);
    setStadiumSearch('');
  };

  // Load stadium if stadiumId is provided (from navigation after adding stadium)
  useEffect(() => {
    if (stadiumId) {
      const loadStadium = async () => {
        try {
          // Search for the stadium by ID
          const results = await apiService.searchStadiums('');
          const foundStadium = results.find(s => s.id?.toString() === stadiumId);
          if (foundStadium) {
            setSelectedStadium(foundStadium);
            setStadiumResults([]);
            setStadiumSearch('');
            // Clear the stadiumId from URL
            navigate({
              to: '/teams/add',
              search: { stadiumId: undefined },
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
  }, [stadiumId]);

  const handleAddStadium = () => {
    navigate({
      to: '/stadiums/add',
      search: {
        returnUrl: '/teams/add',
      },
    });
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setLogoFile(file);
      // Create preview URL
      const reader = new FileReader();
      reader.onloadend = () => {
        setLogoPreview(reader.result as string);
      };
      reader.readAsDataURL(file);
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    // Validate
    if (!formData.fullName.trim()) {
      setError('Vui lòng nhập tên đội bóng');
      return;
    }

    setShowConfirmation(true);
  };

  const handleConfirm = async () => {
    setIsSubmitting(true);
    setError('');

    try {
      const team: Team = {
        fullName: formData.fullName.trim(),
        shortName: formData.shortName.trim() || undefined,
        headCoach: formData.headCoach.trim() || undefined,
        homeKitColor: formData.homeKitColor.trim() || undefined,
        awayKitColor: formData.awayKitColor.trim() || undefined,
        achievements: formData.achievements.trim() || undefined,
        stadium: selectedStadium || undefined,
      };

      await apiService.createTeam(team, logoFile || undefined);
      
      // Navigate back to teams page
      navigate({ to: '/teams' });
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Có lỗi xảy ra khi thêm đội bóng');
      setIsSubmitting(false);
    }
  };

  const handleBack = () => {
    if (showConfirmation) {
      setShowConfirmation(false);
    } else {
      navigate({ to: '/teams' });
    }
  };

  // Get logo preview URL
  const getLogoUrl = (): string => {
    if (logoPreview) {
      return logoPreview;
    }
    const teamName = formData.fullName || formData.shortName || 'team';
    return `https://via.placeholder.com/200/FF6B35/FFFFFF?text=${encodeURIComponent(teamName.substring(0, 2).toUpperCase())}`;
  };

  if (showConfirmation) {
    return (
      <ConfirmTeamView
        formData={{
          ...formData,
          logoFile,
          logoPreview: logoPreview || getLogoUrl(),
          stadium: selectedStadium,
        }}
        error={error}
        isSubmitting={isSubmitting}
        onConfirm={handleConfirm}
        onCancel={handleBack}
      />
    );
  }

  return (
    <div className="add-team-container">
      <div className="add-team-card">
        <div className="add-team-header">
          <h1>Thêm đội bóng mới</h1>
        </div>

        {error && <div className="error-message">{error}</div>}

        <form onSubmit={handleSubmit} className="add-team-form">
          <div className="add-team-form-content">
            {/* Left Section - Basic Information */}
            <div className="form-section">
              <h2 className="form-section-title">Thông tin cơ bản</h2>
              
              <div className="form-group">
                <label htmlFor="fullName">Tên đội bóng</label>
                <input
                  id="fullName"
                  type="text"
                  value={formData.fullName}
                  onChange={(e) => setFormData({ ...formData, fullName: e.target.value })}
                  required
                  placeholder="Nhập tên đội bóng"
                />
              </div>

              <div className="form-group">
                <label htmlFor="shortName">Tên viết tắt</label>
                <input
                  id="shortName"
                  type="text"
                  value={formData.shortName}
                  onChange={(e) => setFormData({ ...formData, shortName: e.target.value })}
                  placeholder="Nhập tên viết tắt"
                />
              </div>

              <div className="form-group">
                <label htmlFor="headCoach">Huấn luyện viên trưởng</label>
                <input
                  id="headCoach"
                  type="text"
                  value={formData.headCoach}
                  onChange={(e) => setFormData({ ...formData, headCoach: e.target.value })}
                  placeholder="Nhập tên huấn luyện viên"
                />
              </div>

              <div className="form-group">
                <label htmlFor="logo">Logo đội bóng</label>
                <div className="logo-preview">
                  <img
                    src={getLogoUrl()}
                    alt="Team logo"
                    onError={(e) => {
                      (e.target as HTMLImageElement).src = 'https://via.placeholder.com/200/CCCCCC/666666?text=LOGO';
                    }}
                  />
                </div>
                <input
                  id="logo"
                  type="file"
                  accept="image/*"
                  onChange={handleFileChange}
                  className="logo-file-input"
                />
              </div>

              <div className="form-group">
                <label htmlFor="homeKitColor">Màu áo chính</label>
                <input
                  id="homeKitColor"
                  type="text"
                  value={formData.homeKitColor}
                  onChange={(e) => setFormData({ ...formData, homeKitColor: e.target.value })}
                  placeholder="Nhập màu áo chính"
                />
              </div>

              <div className="form-group">
                <label htmlFor="awayKitColor">Màu áo phụ</label>
                <input
                  id="awayKitColor"
                  type="text"
                  value={formData.awayKitColor}
                  onChange={(e) => setFormData({ ...formData, awayKitColor: e.target.value })}
                  placeholder="Nhập màu áo phụ"
                />
              </div>

              <div className="form-group">
                <label htmlFor="achievements">Thông tin thành tích</label>
                <textarea
                  id="achievements"
                  value={formData.achievements}
                  onChange={(e) => setFormData({ ...formData, achievements: e.target.value })}
                  placeholder="Nhập thông tin thành tích"
                  rows={4}
                />
              </div>
            </div>

            {/* Right Section - Stadium Information */}
            <div className="form-section">
              <h2 className="form-section-title">Thông tin sân nhà</h2>
              
              <div className="form-group">
                <label>Tìm kiếm sân vận động</label>
                <div className="stadium-search-group">
                  <input
                    type="text"
                    className="stadium-search-input"
                    placeholder="Nhập tên sân vận động"
                    value={stadiumSearch}
                    onChange={(e) => setStadiumSearch(e.target.value)}
                    onKeyPress={(e) => {
                      if (e.key === 'Enter') {
                        e.preventDefault();
                        handleSearchStadium();
                      }
                    }}
                  />
                  <button
                    type="button"
                    onClick={handleSearchStadium}
                    className="search-stadium-btn"
                    disabled={isSearchingStadium}
                  >
                    {isSearchingStadium ? 'Đang tìm...' : 'Tìm'}
                  </button>
                </div>
              </div>

              {selectedStadium && (
                <div className="selected-stadium">
                  <strong>Đã chọn:</strong> {selectedStadium.name}
                  <button
                    type="button"
                    onClick={() => setSelectedStadium(null)}
                    className="remove-stadium-btn"
                  >
                    ✕
                  </button>
                </div>
              )}

              {stadiumResults.length > 0 && (
                <div className="stadium-results">
                  <table className="stadium-results-table">
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
                              type="button"
                              onClick={() => handleSelectStadium(stadium)}
                              className="select-stadium-btn"
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

              <button
                type="button"
                onClick={handleAddStadium}
                className="add-stadium-btn"
              >
                Thêm sân vận động
              </button>
            </div>
          </div>

          {/* Form Actions */}
          <div className="add-team-actions">
            <button type="button" onClick={handleBack} className="cancel-btn" disabled={isSubmitting}>
              Quay lại
            </button>
            <button type="submit" className="submit-btn" disabled={isSubmitting}>
              {isSubmitting ? 'Đang thêm...' : 'Thêm đội bóng'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

