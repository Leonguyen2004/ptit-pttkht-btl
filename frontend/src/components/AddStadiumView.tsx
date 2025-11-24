import { useState } from 'react';
import { useNavigate } from '@tanstack/react-router';
import { apiService, type Stadium } from '../services/api';
import '../routes/auth.css';

interface AddStadiumViewProps {
  onStadiumAdded?: (stadium: Stadium) => void;
  returnUrl?: string;
}

export function AddStadiumView({ onStadiumAdded, returnUrl }: AddStadiumViewProps) {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    name: '',
    address: '',
    capacity: '',
  });
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    // Validate
    if (!formData.name.trim()) {
      setError('Vui lòng nhập tên sân');
      return;
    }

    setIsSubmitting(true);

    try {
      const stadium: Stadium = {
        name: formData.name.trim(),
        address: formData.address.trim() || undefined,
        capacity: formData.capacity ? parseInt(formData.capacity, 10) : undefined,
      };

      const savedStadium = await apiService.createStadium(stadium);
      
      // Call callback if provided
      if (onStadiumAdded) {
        onStadiumAdded(savedStadium);
      } else if (returnUrl && savedStadium.id) {
        // Parse returnUrl and navigate back with stadiumId
        try {
          const url = new URL(returnUrl, window.location.origin);
          const pathParts = url.pathname.split('/').filter(p => p);
          
          // Handle /teams/add route
          if (pathParts[0] === 'teams' && pathParts[1] === 'add') {
            navigate({
              to: '/teams/add',
              search: { stadiumId: savedStadium.id.toString() },
            });
          }
          // Handle /tournaments/$leagueId/schedule/add-match route
          else if (pathParts[0] === 'tournaments' && pathParts[1] && pathParts[2] === 'schedule' && pathParts[3] === 'add-match') {
            const leagueId = pathParts[1];
            const existingSearch = new URLSearchParams(url.search);
            const roundId = existingSearch.get('roundId') || '';
            
            navigate({
              to: '/tournaments/$leagueId/schedule/add-match',
              params: { leagueId },
              search: { roundId, stadiumId: savedStadium.id.toString() },
            });
          } else {
            // Fallback: use window.location
            window.location.href = `${returnUrl}${returnUrl.includes('?') ? '&' : '?'}stadiumId=${savedStadium.id}`;
          }
        } catch (err) {
          // Fallback: use window.location
          window.location.href = `${returnUrl}${returnUrl.includes('?') ? '&' : '?'}stadiumId=${savedStadium.id}`;
        }
      } else {
        // Navigate back to previous page
        navigate({ to: -1 as any });
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Có lỗi xảy ra khi thêm sân vận động');
      setIsSubmitting(false);
    }
  };

  const handleCancel = () => {
    if (onStadiumAdded) {
      // If callback is provided, we're in a modal/embedded context
      // Just navigate back
      navigate({ to: -1 as any });
    } else {
      navigate({ to: -1 as any });
    }
  };

  return (
    <div className="add-stadium-container">
      <div className="add-stadium-card">
        <div className="add-stadium-header">
          <h1>Thêm sân vận động</h1>
        </div>

        {error && <div className="error-message">{error}</div>}

        <form className="add-stadium-form" onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="name">Tên sân</label>
            <input
              id="name"
              type="text"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              placeholder="Nhập tên sân vận động"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="address">Địa chỉ</label>
            <textarea
              id="address"
              value={formData.address}
              onChange={(e) => setFormData({ ...formData, address: e.target.value })}
              placeholder="Nhập địa chỉ sân vận động"
              rows={2}
            />
          </div>

          <div className="form-group">
            <label htmlFor="capacity">Số ghế</label>
            <input
              id="capacity"
              type="number"
              value={formData.capacity}
              onChange={(e) => setFormData({ ...formData, capacity: e.target.value })}
              placeholder="Nhập số ghế"
              min="0"
            />
          </div>

          <div className="add-stadium-actions">
            <button type="button" onClick={handleCancel} className="cancel-btn" disabled={isSubmitting}>
              Quay lại
            </button>
            <button type="submit" className="submit-btn" disabled={isSubmitting}>
              {isSubmitting ? 'Đang lưu...' : 'Lưu'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

