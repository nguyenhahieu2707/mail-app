import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { getEmailById, getInboxEmail } from '../services/mailApi';

function EmailDetail() {
    const { type, id } = useParams();
    const [email, setEmail] = useState(null);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchEmail = async () => {
            try {
                const data = type === 'inbox'
                   ? await getInboxEmail(id)
                   : await getEmailById(id);
                setEmail(data.result);
            } catch {
                setError('Không thể tải email');
            }
        };
        fetchEmail();
    }, [type, id]);

    const downloadAttachment = async () => {
      try {
        let response, filename, blob;

        if (type === 'inbox') {
          // GET trực tiếp endpoint stream
          response = await fetch(
            `/mail/email/inbox/${id}/attachment`,
            { headers: { Authorization: `Bearer ${localStorage.getItem('accessToken')}` } }
          );
          if (!response.ok) throw new Error();
          // Lấy tên file từ header
          const cd = response.headers.get('content-disposition');
          filename = cd?.split('filename="')[1]?.split('"')[0] || email.attachmentName;
          blob = await response.blob();

        } else {
          // Sent: POST path như trước
          const formData = new URLSearchParams();
          formData.append('path', email.attachmentPath);
          response = await fetch('/mail/attachment', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: formData.toString(),
          });
          if (!response.ok) throw new Error();
          blob = await response.blob();
          filename = email.attachmentName;
        }

        // Tạo và click link download
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

    return (
        <div className="container">
            <h2>Chi tiết Email</h2>
            <div className="card p-4">
                <h5>From: {email.from}</h5>
                <h5>To: {email.to}</h5>
                <h5>Subject: {email.sub}</h5>
                <p><strong>Date:</strong> {new Date(email.date).toLocaleString('vi-VN')}</p>
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

export default EmailDetail;
