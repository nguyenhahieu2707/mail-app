import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { getInboxEmail } from '../services/mailApi';

export default function InboxEmailDetail() {
  const { id } = useParams();
  const [email, setEmail] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    getInboxEmail(id)
      .then(res => {
        console.log('Inbox email payload:', res.result);
        setEmail(res.result);
      })
      .catch(() => setError('Không thể tải thư đến'));
  }, [id]);

  const downloadAttachment = async () => {
    try {
      const token = localStorage.getItem('accessToken');
      const response = await fetch(`/mail/email/inbox/${id}/attachment`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      if (!response.ok) {
        console.error('Server error:', response.statusText);
        throw new Error('Tải file thất bại');
      }

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

  if (error) return <div className="alert alert-danger">{error}</div>;
  if (!email) return <div>Loading...</div>;

  // Render plain text hoặc HTML tuỳ nội dung
  const isHtml = typeof email.body === 'string' && email.body.includes('<') && email.body.includes('>');
  
  return (
    <div className="container">
      <h2>Chi tiết Thư Đến</h2>
      <div className="card p-4">
        <h5>From: {email.from}</h5>
        <h5>Chủ đề: {email.sub}</h5>
        {isHtml
          ? <div dangerouslySetInnerHTML={{ __html: email.body }} />
          : <p>{email.body}</p>
        }
        {email.attachmentName && (
          <div className="mt-3">
            <strong>Đính kèm:</strong>{' '}
            <button className="btn btn-link p-0" onClick={downloadAttachment}>
              📎 {email.attachmentName}
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
