import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { getInboxEmail } from '../services/mailApi';
import './EmailDetail.css'; // Import file CSS mới

export default function InboxEmailDetail() {
  const { id } = useParams();
  const [email, setEmail] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    getInboxEmail(id)
      .then(res => setEmail(res.result))
      .catch(() => setError('Không thể tải chi tiết thư đến.'));
  }, [id]);

  // Hàm downloadAttachment không thay đổi, giữ nguyên như cũ
  const downloadAttachment = async () => {
    try {
      const token = localStorage.getItem('accessToken');
      const response = await fetch(`/mail/email/inbox/${id}/attachment`, {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (!response.ok) throw new Error('Tải file thất bại');
      const cd = response.headers.get('content-disposition');
      const filename = cd?.match(/filename="(.+)"/)?.[1] || email.attachmentName || 'attachment';
      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = filename;
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
  
  const isHtml = typeof email.body === 'string' && /<[a-z][\s\S]*>/i.test(email.body);

  return (
    <div className="email-detail-container">
      <div className="email-header">
        <h2 className="email-subject">{email.sub}</h2>
        <div className="email-info">
          <span className="label">👤 Từ:</span>
          <span>{email.from}</span>
        </div>
        <div className="email-info">
          <span className="label">📅 Ngày nhận:</span>
          <span>{new Date(email.date).toLocaleString()}</span>
        </div>
      </div>

      <div className="email-body">
        {isHtml
          ? <div dangerouslySetInnerHTML={{ __html: email.body }} />
          : <p>{email.body}</p>
        }
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