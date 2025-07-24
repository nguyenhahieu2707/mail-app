import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { sendMail } from '../services/mailApi';
import { remoteLogger } from '../utils/remoteLogger'; // 🔹 Import logger

function ComposeMail() {
  const [mail, setMail] = useState({ to: '', sub: '', body: '' });
  const [attachment, setAttachment] = useState(null);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  const handleChange = (e) => {
    const { name, value } = e.target;
    setMail((prev) => ({ ...prev, [name]: value }));
    remoteLogger.debug(`Field changed: ${name} = ${value}`);
  };

  const handleFileChange = (e) => {
    const file = e.target.files[0];
    setAttachment(file);
    remoteLogger.info(`Attachment selected: ${file?.name}`);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    remoteLogger.info('Submit triggered - preparing to send email');

    const formData = new FormData();
    formData.append('to', mail.to);
    formData.append('sub', mail.sub);
    formData.append('body', mail.body);
    if (attachment) {
      formData.append('attachment', attachment);
    }

    try {
      remoteLogger.debug(`Sending email to ${mail.to} with subject "${mail.sub}"`);
      await sendMail(formData);
      remoteLogger.info(`Email sent successfully to ${mail.to}`);
      alert('Email sent successfully!');
      navigate('/sent');
    } catch (err) {
      console.error(err);
      remoteLogger.error(`Send mail failed: ${err.message}`);
      setError('Failed to send email');
    }
  };

  return (
    <div className="container">
      <h2>Compose Mail</h2>
      {error && <div className="alert alert-danger">{error}</div>}
      <div className="card p-4">
        <div className="mb-3">
          <label htmlFor="to" className="form-label">To:</label>
          <input
            type="email"
            className="form-control"
            id="to"
            name="to"
            value={mail.to}
            onChange={handleChange}
            required
          />
        </div>
        <div className="mb-3">
          <label htmlFor="subject" className="form-label">Subject:</label>
          <input
            type="text"
            className="form-control"
            id="subject"
            name="sub"
            value={mail.sub}
            onChange={handleChange}
          />
        </div>
        <div className="mb-3">
          <label htmlFor="body" className="form-label">Body:</label>
          <textarea
            className="form-control"
            id="body"
            name="body"
            rows="5"
            value={mail.body}
            onChange={handleChange}
          ></textarea>
        </div>
        <div className="mb-3">
          <label htmlFor="attachment" className="form-label">Attachment:</label>
          <input
            type="file"
            className="form-control"
            id="attachment"
            onChange={handleFileChange}
          />
        </div>
        <button className="btn btn-primary" onClick={handleSubmit}>
          Send
        </button>
      </div>
    </div>
  );
}

export default ComposeMail;
