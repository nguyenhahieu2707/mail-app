// import { useState, useEffect } from 'react';
// import { Link } from 'react-router-dom';
// import { getSent } from '../services/mailApi';

// function SentBox() {
//     const [sentEmails, setSentEmails] = useState([]);
//     const [error, setError] = useState(null);

//     useEffect(() => {
//         const fetchSentEmails = async () => {
//             try {
//                 const data = await getSent();
//                 setSentEmails(data.result);
//             } catch (err) {
//                 setError('Failed to fetch sent emails');
//             }
//         };
//         fetchSentEmails();
//     }, []);

//     if (error) return <div className="alert alert-danger">{error}</div>;

//     return (
//         <div className="container">
//             <h2>Sent Box</h2>
//             <table className="table table-striped">
//                 <thead>
//                     <tr>
//                         <th>To</th>
//                         <th>Subject</th>
//                         <th>Date</th>
//                     </tr>
//                 </thead>
//                 <tbody>
//                     {sentEmails.map(email => (
//                         <tr key={email.id}>
//                             <td>{email.to}</td>
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

// export default SentBox;

// import { useState, useEffect } from 'react';
// import { Link } from 'react-router-dom';
// import { getSent } from '../services/mailApi';
// import './SentBox.css';

// function SentBox() {
//     const [sentEmails, setSentEmails] = useState([]);
//     const [error, setError] = useState(null);
//     const [isLoading, setIsLoading] = useState(true);

//     useEffect(() => {
//         const fetchSentEmails = async () => {
//             try {
//                 const data = await getSent();
//                 setSentEmails(data.result);
//             } catch (err) {
//                 setError('Failed to fetch sent emails');
//             } finally {
//                 setIsLoading(false);
//             }
//         };
//         fetchSentEmails();
//     }, []);

//     if (isLoading) {
//         return <div className="loading">Loading...</div>;
//     }

//     if (error) {
//         return <div className="sentbox-error">{error}</div>;
//     }

//     return (
//         <div className="sentbox-container">
//             <h2>Sent Box</h2>
//             <table className="email-table">
//                 <thead>
//                     <tr>
//                         <th>To</th>
//                         <th>Subject</th>
//                         <th>Date</th>
//                     </tr>
//                 </thead>
//                 <tbody>
//                     {sentEmails.map(email => (
//                         <tr key={email.id}>
//                             <td>{email.to}</td>
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

// export default SentBox;

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