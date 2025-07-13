import { useState, useEffect, useCallback } from 'react';
import { getInbox } from '../services/mailApi';
import EmailList from '../components/EmailList';
import useMailSocket from '../hooks/useMailSocket'; // 👈 để nghe mail mới

function Inbox() {
    const [emails, setEmails] = useState([]);
    const [error, setError] = useState(null);
    const [isLoading, setIsLoading] = useState(true);

    // 👇 Viết fetchEmails ra riêng để có thể gọi lại nhiều lần
    const fetchEmails = useCallback(async () => {
        try {
            setIsLoading(true);
            const data = await getInbox();
            setEmails(data.result);
            setError(null);
        } catch (err) {
            setError('Không thể tải hộp thư đến. Vui lòng thử lại sau.');
        } finally {
            setIsLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchEmails();
    }, [fetchEmails]);

    // 👇 Nghe khi có mail mới → gọi lại fetchEmails
    useMailSocket(() => {
        console.log("📬 Có mail mới, tải lại hộp thư...");
        fetchEmails();
    });

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
