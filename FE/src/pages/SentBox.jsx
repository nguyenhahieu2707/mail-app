import { useState, useEffect } from 'react';
import { getSent } from '../services/mailApi';
import EmailList from '../components/EmailList';

function SentBox() {
    const [sentEmails, setSentEmails] = useState([]);
    const [error, setError] = useState(null);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        const fetchSentEmails = async () => {
            try {
                const data = await getSent();
                setSentEmails(data.result);
            } catch (err) {
                setError('Không thể tải thư đã gửi. Vui lòng thử lại sau.');
            } finally {
                setIsLoading(false);
            }
        };
        fetchSentEmails();
    }, []);

    return (
        <EmailList
            title="Thư đã gửi"
            emails={sentEmails}
            type="sent"
            isLoading={isLoading}
            error={error}
        />
    );
}

export default SentBox;