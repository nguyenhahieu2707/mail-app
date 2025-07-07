import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { sendMail } from '../services/mailApi';

function ComposeMail() {
    const [mail, setMail] = useState({ to: '', sub: '', body: '' });
    const [error, setError] = useState(null);
    const navigate = useNavigate();

    const handleChange = (e) => {
        setMail({ ...mail, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await sendMail(mail);
            alert('Email sent successfully!');
            navigate('/sent');
        } catch (err) {
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
                        name="subject"
                        value={mail.subject}
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
                <button className="btn btn-primary" onClick={handleSubmit}>
                    Send
                </button>
            </div>
        </div>
    );
}

export default ComposeMail;