import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { getEmailById } from '../services/mailApi';

function SentEmailDetail() {
  const { id } = useParams();
  const [email, setEmail] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchEmail = async () => {
      try {
        const data = await getEmailById(id);
        console.log('Email received:', data.result);
        setEmail(data.result);
      } catch (err) {
        setError('Không thể tải thư đã gửi');
      }
    };
    fetchEmail();
  }, [id]);

  const downloadAttachment = async () => {
    try {
      const formData = new URLSearchParams();
      formData.append('path', email.attachmentPath); // giống như file từng chạy

      const response = await fetch('/mail/attachment', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: formData.toString(),
      });

      if (!response.ok) {
        throw new Error('Tải file thất bại');
      }

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

  if (error) return <div className="alert alert-danger">{error}</div>;
  if (!email) return <div>Loading...</div>;

  return (
    <div className="container">
      <h2>Chi tiết Thư Đã Gửi</h2>
      <div className="card p-4">
        <h5>From: {email.from}</h5>
        <h5>To: {email.to}</h5>
        <h5>Subject: {email.sub}</h5>
        <p><strong>Date:</strong> {email.date}</p>
        <hr />
        <p>{email.body}</p>

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

export default SentEmailDetail;
