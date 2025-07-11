import { useState, useEffect } from 'react';
import { getInbox } from '../services/mailApi';
import EmailList from '../components/EmailList';

function Inbox() {
    const [emails, setEmails] = useState([]);
    const [error, setError] = useState(null);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        const fetchEmails = async () => {
            try {
                const data = await getInbox();
                setEmails(data.result);
            } catch (err) {
                setError('Không thể tải hộp thư đến. Vui lòng thử lại sau.');
            } finally {
                setIsLoading(false);
            }
        };
        fetchEmails();
    }, []);

    return (
        <EmailList
            title="Hộp thư đến"
            emails={emails}
            type="inbox"
            isLoading={isLoading}
            error={error}
        />
    );
}

export default Inbox;