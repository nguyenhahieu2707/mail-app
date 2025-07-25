// import { Link } from 'react-router-dom';
// import { FiInbox, FiSend } from 'react-icons/fi'; // Cài đặt: npm install react-icons
// import './EmailList.css';

// // Component con cho trạng thái loading
// const LoadingSpinner = () => (
//     <div className="loading-container">
//         <div className="spinner"></div>
//         <p>Đang tải...</p>
//     </div>
// );

// // Component con cho thông báo lỗi
// const ErrorMessage = ({ message }) => (
//     <div className="error-container">
//         <p>{message}</p>
//     </div>
// );

// // Component chính
// function EmailList({ title, emails, type, isLoading, error }) {
//     if (isLoading) {
//         return <LoadingSpinner />;
//     }

//     if (error) {
//         return <ErrorMessage message={error} />;
//     }

//     // Xác định tiêu đề cột đầu tiên dựa vào loại hộp thư
//     const fromOrTo = type === 'inbox' ? 'Từ' : 'Đến';

//     return (
//         <div className="email-list-container">
//             <header className="email-list-header">
//                 {type === 'inbox' ? <FiInbox className="header-icon" /> : <FiSend className="header-icon" />}
//                 <h1>{title}</h1>
//             </header>

//             {emails && emails.length > 0 ? (
//                 <div className="table-wrapper">
//                     <table className="email-table">
//                         <thead>
//                             <tr>
//                                 <th className="col-sender">{fromOrTo}</th>
//                                 <th className="col-subject">Chủ đề</th>
//                                 <th className="col-date">Ngày</th>
//                             </tr>
//                         </thead>
//                         <tbody>
//                             {emails.map(email => (
//                                 <tr key={email.id} className={!email.read ? 'unread' : ''}>
//                                     <td>
//                                         <div className="sender-cell">
//                                             <div className="avatar-placeholder">{type === 'inbox' ? email.from[0] : email.to[0]}</div>
//                                             <span>{type === 'inbox' ? email.from : email.to}</span>
//                                         </div>
//                                     </td>
//                                     <td>
//                                         <Link to={`/email/${type}/${email.id}`} className="subject-link">
//                                             {email.sub}
//                                         </Link>
//                                     </td>
//                                     <td className="date-cell">{new Date(email.date).toLocaleDateString('vi-VN')}</td>
//                                 </tr>
//                             ))}
//                         </tbody>
//                     </table>
//                 </div>
//             ) : (
//                 <div className="empty-state">
//                     <p>Không có thư nào trong hộp thư này.</p>
//                 </div>
//             )}
//         </div>
//     );
// }

// export default EmailList;

import { Link } from 'react-router-dom';
import { FiInbox, FiSend } from 'react-icons/fi';
import './EmailList.css';

const LoadingSpinner = () => (
  <div className="loading-container">
    <div className="spinner"></div>
    <p>Đang tải...</p>
  </div>
);

const ErrorMessage = ({ message }) => (
  <div className="error-container">
    <p>{message}</p>
  </div>
);

function EmailList({ title, emails, type, isLoading, error }) {
  if (isLoading) return <LoadingSpinner />;
  if (error)     return <ErrorMessage message={error} />;

  const fromOrTo = type === 'inbox' ? 'Từ' : 'Đến';

  return (
    <div className="email-list-container">
      <header className="email-list-header">
        {type === 'inbox'
          ? <FiInbox className="header-icon" />
          : <FiSend  className="header-icon" />}
        <h1>{title}</h1>
      </header>

      {emails && emails.length > 0 ? (
        <div className="table-wrapper">
          <table className="email-table">
            <thead>
              <tr>
                <th className="col-sender">{fromOrTo}</th>
                <th className="col-subject">Chủ đề</th>
                <th className="col-date">Ngày</th>
              </tr>
            </thead>
            <tbody>
              {emails.map(email => {
                // build đúng path dựa vào type
                const detailPath = type === 'inbox'
                  ? `/email/inbox/${email.id}`
                  : `/email/sent/${email.id}`;

                // lấy ký tự đầu cho avatar
                const initial = type === 'inbox'
                  ? (email.from?.charAt(0) || '?')
                  : (email.to?.charAt(0)   || '?');

                return (
                  <tr key={email.id} className={!email.read ? 'unread' : ''}>
                    <td>
                      <div className="sender-cell">
                        <div className="avatar-placeholder">{initial}</div>
                        <span>{type === 'inbox' ? email.from : email.to}</span>
                      </div>
                    </td>
                    <td>
                      <Link to={detailPath} className="subject-link">
                        {email.sub}
                      </Link>
                    </td>
                    <td className="date-cell">
                      {new Date(email.date).toLocaleDateString('vi-VN')}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      ) : (
        <div className="empty-state">
          <p>Không có thư nào trong hộp thư này.</p>
        </div>
      )}
    </div>
  );
}

export default EmailList;
