import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getInbox } from '../services/mailApi';

function Inbox() {
    const [emails, setEmails] = useState([]);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchEmails = async () => {
            try {
                const data = await getInbox();
                console.log("📥 Inbox data:", data)
                setEmails(data.result);
            } catch (err) {
                setError('Failed to fetch inbox');
            }
        };
        fetchEmails();
    }, []);

    if (error) return <div className="alert alert-danger">{error}</div>;

    return (
        <div className="container">
            <h2>Inbox</h2>
            <table className="table table-striped">
                <thead>
                    <tr>
                        <th>From</th>
                        <th>Subject</th>
                        <th>Date</th>
                    </tr>
                </thead>
                <tbody>
                    {emails.map(email => (
                        <tr key={email.id}>
                            <td>{email.from}</td>
                            <td>
                                <Link to={`/email/${email.id}`}>{email.sub}</Link>
                            </td>
                            <td>{email.date}</td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
}

export default Inbox;