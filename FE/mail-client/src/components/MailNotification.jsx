import React, { useState } from "react";
import useMailSocket from "../hooks/useMailSocket";

const MailNotification = () => {
  const [mails, setMails] = useState([]);
  const [isOpen, setIsOpen] = useState(false);

  useMailSocket((mail) => {
    const mailWithTimestamp = { ...mail, timestamp: new Date() };
    console.log("📥 Raw mail object:", mail, typeof mail);
    console.log("📥 Cập nhật mail mới:", mailWithTimestamp);
    setMails((prev) => [mailWithTimestamp, ...prev]);
  });

  const toggleDropdown = () => {
    setIsOpen(!isOpen);
  };

  return (
    <div className="position-relative d-inline-block">
      <div 
        className={`d-flex align-items-center cursor-pointer p-2 rounded ${mails.length > 0 ? 'bg-danger text-white' : 'text-muted'}`}
        onClick={toggleDropdown}
      >
        <span className="fs-5 me-2">🔔</span>
        <span className="badge bg-light text-dark rounded-pill">
          {mails.length}
        </span>
      </div>
      {isOpen && (
        <div 
          className="position-absolute end-0 mt-2 w-100"
          style={{ 
            minWidth: '300px', 
            maxWidth: '350px', 
            backgroundColor: '#fff', 
            borderRadius: '8px', 
            boxShadow: '0 2px 8px rgba(0,0,0,0.15)', 
            zIndex: 1000,
            maxHeight: '400px',
            overflowY: 'auto'
          }}
        >
          {mails.length === 0 ? (
            <p className="text-center text-muted p-3 mb-0">No new emails</p>
          ) : (
            <div>
              {mails.slice(0, 5).map((mail, index) => (
                <div 
                  key={index} 
                  className="d-flex align-items-center p-2 border-bottom"
                >
                  <div 
                    className="rounded-circle bg-light d-flex align-items-center justify-content-center me-2"
                    style={{ width: '32px', height: '32px', fontWeight: 'bold', fontSize: '14px' }}
                  >
                    {(mail.from || "?").charAt(0).toUpperCase()}
                  </div>
                  <div className="flex-grow-1 overflow-hidden">
                    <div className="d-flex justify-content-between mb-1">
                      <span className="fw-bold" style={{ fontSize: '14px' }}>
                        {mail.from || "Unknown"}
                      </span>
                      <span className="text-muted" style={{ fontSize: '12px' }}>
                        {mail.timestamp?.toLocaleTimeString?.() || ""}
                      </span>
                    </div>
                    <div 
                      className="text-truncate" 
                      style={{ fontSize: '13px' }}
                    >
                      {mail.subject || "(No subject)"}
                    </div>
                  </div>
                </div>
              ))}

              {mails.length > 5 && (
                <p className="text-center text-muted p-2 mb-0" style={{ fontSize: '12px' }}>
                  And {mails.length - 5} more emails...
                </p>
              )}
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default MailNotification;