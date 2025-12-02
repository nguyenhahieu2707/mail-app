import { useState, useEffect, useCallback } from 'react';
import { getInbox, getMailDetail } from '../services/mailApi';
import useMailSocket from '../hooks/useMailSocket';
import { FiUser, FiInbox, FiPaperclip } from 'react-icons/fi';

// Helper components
const LoadingSpinner = () => (
    <div className="d-flex justify-content-center align-items-center h-100 p-5">
        <div className="spinner-border" role="status">
            <span className="visually-hidden">Loading...</span>
        </div>
    </div>
);

const ErrorMessage = ({ message }) => (
    <div className="alert alert-danger m-3">{message}</div>
);

// Component để hiển thị chi tiết một email
const EmailDetailView = ({ emailId, type }) => {
    const [email, setEmail] = useState(null);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        if (!emailId) {
            setEmail(null);
            return;
        };

        const fetchDetail = async () => {
            setIsLoading(true);
            setError(null);
            try {
                const data = await getMailDetail(type, emailId);
                setEmail(data.result);
            } catch (err) {
                setError('Không thể tải chi tiết email.');
            } finally {
                setIsLoading(false);
            }
        };

        fetchDetail();
    }, [emailId, type]);

    // TÍCH HỢP LẠI LOGIC DOWNLOAD ĐÚNG
    const downloadAttachment = async () => {
        try {
            const token = localStorage.getItem('accessToken');
            // Sử dụng URL động dựa trên type và emailId
            const response = await fetch(`/mail/email/${type}/${emailId}/attachment`, {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            if (!response.ok) throw new Error('Tải file thất bại');
            
            const cd = response.headers.get('content-disposition');
            const filename = cd?.match(/filename="(.+)"/)?.[1] || email.attachmentName || 'attachment';
            
            const blob = await response.blob();
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = filename;
            document.body.appendChild(a);
            a.click();
            a.remove();
            window.URL.revokeObjectURL(url);
        } catch (err) {
            console.error('Lỗi khi tải file:', err);
            alert(err.message);
        }
    };

    if (isLoading) return <LoadingSpinner />;
    if (error) return <ErrorMessage message={error} />;
    if (!email) {
        return (
            <div className="d-flex justify-content-center align-items-center h-100 text-muted flex-column">
                <FiInbox size={48} className="mb-3" />
                <h4>Chọn một thư để đọc</h4>
            </div>
        );
    }

    return (
        <div className="email-detail p-4">
            <h4 className="mb-4">{email.sub}</h4>
            <div className="d-flex align-items-center mb-4">
                <FiUser size={40} className="me-3 flex-shrink-0" />
                <div>
                    <h6 className="mb-0 text-truncate">{email.from}</h6>
                    <small className="text-muted">đến tôi</small>
                </div>
                <small className="ms-auto text-muted text-nowrap">
                    {new Date(email.date).toLocaleString('vi-VN')}
                </small>
            </div>
            <div className="email-body" dangerouslySetInnerHTML={{ __html: email.body }} />

            {/* SỬA LẠI THÀNH BUTTON VỚI ONCLICK */}
            {email.attachmentName && (
                <div className="mt-4 pt-4 border-top">
                    <h6 className="mb-3">Tệp đính kèm</h6>
                    <button 
                        onClick={downloadAttachment}
                        className="btn btn-outline-secondary d-inline-flex align-items-center"
                    >
                        <FiPaperclip className="me-2" />
                        <span>{email.attachmentName}</span>
                    </button>
                </div>
            )}
        </div>
    );
};


function Inbox() {
    const [emails, setEmails] = useState([]);
    const [error, setError] = useState(null);
    const [isLoading, setIsLoading] = useState(true);
    const [selectedEmailId, setSelectedEmailId] = useState(null);

    const fetchEmails = useCallback(async () => {
        try {
            if (emails.length === 0) setIsLoading(true);
            const data = await getInbox();
            setEmails(data.result);
            setError(null);
        } catch (err) {
            setError('Không thể tải hộp thư đến. Vui lòng thử lại sau.');
        } finally {
            setIsLoading(false);
        }
    }, [emails.length]);

    useEffect(() => {
        fetchEmails();
    }, [fetchEmails]);

    useMailSocket(() => {
        console.log("📬 Có mail mới, tải lại hộp thư...");
        fetchEmails();
    });

    const handleSelectEmail = (id) => {
        setSelectedEmailId(id);
        const newEmails = emails.map(e => e.id === id ? { ...e, read: true } : e);
        setEmails(newEmails);
    };

    return (
        <div className="row g-0">
            <div className="col-md-4 border-end vh-100 overflow-auto">
                <div className="p-3 border-bottom sticky-top bg-light">
                    <h5 className="mb-0">Hộp thư đến</h5>
                </div>
                {isLoading && <LoadingSpinner />}
                {error && <ErrorMessage message={error} />}
                {!isLoading && !error && (
                    <div className="list-group list-group-flush">
                        {emails.length > 0 ? emails.map(email => {
                            const bodyPreview = email.body?.startsWith('jakarta.mail.internet.MimeMultipart')
                                ? '[Thư có tệp đính kèm]'
                                : email.body?.substring(0, 80) ?? '';

                            return (
                                <div
                                    key={email.id}
                                    className={`list-group-item list-group-item-action email-list-item p-3 ${selectedEmailId === email.id ? 'active' : ''} ${!email.read ? 'fw-bold' : ''}`}
                                    onClick={() => handleSelectEmail(email.id)}
                                >
                                    <div className="d-flex w-100 justify-content-between">
                                        <p className="mb-1 text-truncate">{email.from}</p>
                                        <small className="text-nowrap">{new Date(email.date).toLocaleDateString('vi-VN')}</small>
                                    </div>
                                    <p className="mb-1 text-truncate">{email.sub}</p>
                                    <small className="text-muted text-truncate d-block">{bodyPreview}...</small>
                                </div>
                            );
                        }) : (
                            <p className="p-3 text-muted">Không có thư nào.</p>
                        )}
                    </div>
                )}
            </div>

            <div className="col-md-8 vh-100 overflow-auto">
                <EmailDetailView emailId={selectedEmailId} type="inbox" />
            </div>
        </div>
    );
}

export default Inbox;
