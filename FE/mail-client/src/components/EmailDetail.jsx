import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { getEmailById } from '../services/mailApi';

function EmailDetail() {
    const { id } = useParams();
    const [email, setEmail] = useState(null);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchEmail = async () => {
            try {
                const data = await getEmailById(id);
                // console.log(data)
                setEmail(data.result);
            } catch (err) {
                setError('Failed to fetch email');
            }
        };
        fetchEmail();
    }, [id]);

    if (error) return <div className="alert alert-danger">{error}</div>;
    if (!email) return <div>Loading...</div>;

    return (
        <div className="container">
            <h2>Email Details</h2>
            <div className="card p-4">
                <h5>From: {email.from}</h5>
                <h5>To: {email.to}</h5>
                <h5>Subject: {email.sub}</h5>
                <p><strong>Date:</strong> {email.date}</p>
                <hr />
                <p>{email.body}</p>
            </div>
        </div>
    );
}

export default EmailDetail;