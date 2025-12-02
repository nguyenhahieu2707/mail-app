import { useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { sendMail } from '../services/mailApi';
import { remoteLogger } from '../utils/remoteLogger';

// Import CSS và các thành phần icon từ react-icons
import './ComposeMail.css';
import { FiSend, FiPaperclip, FiTrash2, FiLoader } from 'react-icons/fi';

function ComposeMail() {
  const [mail, setMail] = useState({ to: '', sub: '', body: '' });
  const [attachment, setAttachment] = useState(null);
  const [error, setError] = useState(null);
  const [isSending, setIsSending] = useState(false);
  const navigate = useNavigate();
  const fileInputRef = useRef(null);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setMail((prev) => ({ ...prev, [name]: value }));
  };

  const handleFileChange = (e) => {
    const file = e.target.files[0];
    setAttachment(file);
  };

  const handleAttachClick = () => {
    fileInputRef.current.click();
  };

  const handleDiscard = () => {
    if (window.confirm('Bạn có chắc chắn muốn hủy thư này không?')) {
      navigate(-1);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsSending(true);
    setError(null);
    remoteLogger.info('Submit triggered - preparing to send email');

    const formData = new FormData();
    formData.append('to', mail.to);
    formData.append('sub', mail.sub);
    formData.append('body', mail.body);
    if (attachment) {
      formData.append('attachment', attachment);
    }

    try {
      await sendMail(formData);
      alert('Email sent successfully!');
      navigate('/sent');
    } catch (err) {
      remoteLogger.error(`Send mail failed: ${err.message}`);
      setError('Failed to send email. Please check the recipient address and try again.');
    } finally {
      setIsSending(false);
    }
  };

  return (
    <div className="compose-container p-4 bg-white rounded shadow-sm">
      <h4 className="mb-4 border-bottom pb-3">Thư mới</h4>
      
      {error && <div className="alert alert-danger">{error}</div>}

      <form onSubmit={handleSubmit}>
        <div className="mb-3">
          <label htmlFor="compose-to" className="form-label">Đến</label>
          <input
            type="email"
            className="form-control"
            id="compose-to"
            name="to"
            value={mail.to}
            onChange={handleChange}
            placeholder="nguoinhan@example.com"
            required
            disabled={isSending}
          />
        </div>
        <div className="mb-3">
          <label htmlFor="compose-subject" className="form-label">Chủ đề</label>
          <input
            type="text"
            className="form-control"
            id="compose-subject"
            name="sub"
            value={mail.sub}
            onChange={handleChange}
            disabled={isSending}
          />
        </div>
        <div className="mb-3">
          <label htmlFor="compose-body" className="form-label">Nội dung</label>
          <textarea
            className="form-control"
            id="compose-body"
            name="body"
            rows="12"
            value={mail.body}
            onChange={handleChange}
            disabled={isSending}
          ></textarea>
        </div>

        {attachment && (
          <div className="mb-3 text-muted">
            <FiPaperclip className="me-2" /> {attachment.name}
          </div>
        )}

        <input
          type="file"
          ref={fileInputRef}
          onChange={handleFileChange}
          style={{ display: 'none' }}
          disabled={isSending}
        />

        <div className="d-flex justify-content-between">
          <div>
            <button type="submit" className="btn btn-primary d-flex align-items-center" disabled={isSending}>
              {isSending ? <FiLoader className="spinner me-2" /> : <FiSend className="me-2" />}
              {isSending ? 'Đang gửi...' : 'Gửi'}
            </button>
            <button type="button" className="btn btn-outline-secondary ms-2" onClick={handleAttachClick} disabled={isSending}>
              <FiPaperclip />
            </button>
          </div>
          <button type="button" className="btn btn-outline-danger" onClick={handleDiscard} disabled={isSending}>
            <FiTrash2 />
          </button>
        </div>
      </form>
    </div>
  );
}

export default ComposeMail;
