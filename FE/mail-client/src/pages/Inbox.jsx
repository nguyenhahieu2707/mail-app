// import { useState, useEffect } from 'react';
// import { Link } from 'react-router-dom';
// import { getInbox } from '../services/mailApi';

// function Inbox() {
//     const [emails, setEmails] = useState([]);
//     const [error, setError] = useState(null);

//     useEffect(() => {
//         const fetchEmails = async () => {
//             try {
//                 const data = await getInbox();
//                 console.log("📥 Inbox data:", data)
//                 setEmails(data.result);
//             } catch (err) {
//                 setError('Failed to fetch inbox');
//             }
//         };
//         fetchEmails();
//     }, []);

//     if (error) return <div className="alert alert-danger">{error}</div>;

//     return (
//         <div className="container">
//             <h2>Inbox</h2>
//             <table className="table table-striped">
//                 <thead>
//                     <tr>
//                         <th>From</th>
//                         <th>Subject</th>
//                         <th>Date</th>
//                     </tr>
//                 </thead>
//                 <tbody>
//                     {emails.map(email => (
//                         <tr key={email.id}>
//                             <td>{email.from}</td>
//                             <td>
//                                 <Link to={`/email/${email.id}`}>{email.sub}</Link>
//                             </td>
//                             <td>{email.date}</td>
//                         </tr>
//                     ))}
//                 </tbody>
//             </table>
//         </div>
//     );
// }

// export default Inbox;

// import { useState, useEffect } from 'react';
// import { Link } from 'react-router-dom';
// import { getInbox } from '../services/mailApi';
// import './Inbox.css';

// function Inbox() {
//     const [emails, setEmails] = useState([]);
//     const [error, setError] = useState(null);
//     const [isLoading, setIsLoading] = useState(true);

//     useEffect(() => {
//         const fetchEmails = async () => {
//             try {
//                 const data = await getInbox();
//                 console.log("📥 Inbox data:", data);
//                 setEmails(data.result);
//             } catch (err) {
//                 setError('Không thể tải hộp thư đến');
//             } finally {
//                 setIsLoading(false);
//             }
//         };
//         fetchEmails();
//     }, []);

//     if (isLoading) {
//         return <div className="loading">Đang tải...</div>;
//     }

//     if (error) {
//         return <div className="inbox-error">{error}</div>;
//     }

//     return (
//         <div className="inbox-container">
//             <h2>Hộp thư đến</h2>
//             <table className="email-table">
//                 <thead>
//                     <tr>
//                         <th>Từ</th>
//                         <th>Chủ đề</th>
//                         <th>Ngày</th>
//                     </tr>
//                 </thead>
//                 <tbody>
//                     {emails.map(email => (
//                         <tr key={email.id}>
//                             <td>{email.from}</td>
//                             <td>
//                                 <Link to={`/email/${email.id}`}>{email.sub}</Link>
//                             </td>
//                             <td>{email.date}</td>
//                         </tr>
//                     ))}
//                 </tbody>
//             </table>
//         </div>
//     );
// }

// export default Inbox;

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