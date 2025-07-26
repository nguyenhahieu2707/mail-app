import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { getEmailById } from '../services/mailApi';
import './EmailDetail.css'; // Import file CSS mới

function SentEmailDetail() {
  const { id } = useParams();
  const [email, setEmail] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchEmail = async () => {
      try {
        const data = await getEmailById(id);
        setEmail(data.result);
      } catch (err) {
        setError('Không thể tải chi tiết thư đã gửi.');
      }
    };
    fetchEmail();
  }, [id]);
  
  // Hàm downloadAttachment không thay đổi, giữ nguyên như cũ
  const downloadAttachment = async () => {
    try {
      const formData = new URLSearchParams();
      formData.append('path', email.attachmentPath);
      const response = await fetch('/mail/attachment', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: formData.toString(),
      });
      if (!response.ok) throw new Error('Tải file thất bại');
      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = email.attachmentName || 'file';
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      console.error('Lỗi khi tải file:', err);
    }
  };

  if (error) return <div className="error-text">{error}</div>;
  if (!email) return <div className="loading-text">Đang tải...</div>;

  return (
    <div className="email-detail-container">
      <div className="email-header">
        <h2 className="email-subject">{email.sub}</h2>
        <div className="email-info">
          <span className="label">👤 Tới:</span>
          <span>{email.to}</span>
        </div>
        <div className="email-info">
          <span className="label">📅 Ngày gửi:</span>
          <span>{new Date(email.date).toLocaleString()}</span>
        </div>
      </div>

      <div className="email-body">
        <p>{email.body}</p>
      </div>

      {email.attachmentName && (
        <div className="email-attachment-section">
          <button className="attachment-button" onClick={downloadAttachment}>
            📎
            <span>{email.attachmentName}</span>
          </button>
        </div>
      )}
    </div>
  );
}

export default SentEmailDetail;