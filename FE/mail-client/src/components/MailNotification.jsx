import React, { useState } from "react";
import useMailSocket from "../hooks/useMailSocket";

const MailNotification = () => {
  const [mails, setMails] = useState([]);

  useMailSocket((mail) => {
    setMails((prev) => [mail, ...prev]); // Thêm mail mới vào đầu danh sách
  });

  return (
    <div>
      <h2>📬 Mail Realtime</h2>
      {mails.length === 0 && <p>No new emails</p>}
      <ul>
        {mails.map((mail, index) => (
          <li key={index}>
            <strong>From:</strong> {mail.from} <br />
            <strong>Subject:</strong> {mail.subject}
          </li>
        ))}
      </ul>
    </div>
  );
};

export default MailNotification;
