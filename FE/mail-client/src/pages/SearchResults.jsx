import React, { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import axios from 'axios';
//import { searchEmails } from '../mailApp.js';

const API_URL = 'http://localhost:8080';

const authHeader = () => ({
  headers: {
    Authorization: `Bearer ${localStorage.getItem('accessToken')}`
  }
});

export default function SearchResults() {
  const [emails, setEmails] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const location = useLocation();
  const query = new URLSearchParams(location.search).get('query');

  useEffect(() => {
    const fetchSearchResults = async () => {
      try {
        const response = await axios.post(
          `${API_URL}/mail/search`,
          { query, page: 0, size: 10 },
          authHeader()
        );
        setEmails(response.data.result.content);
        setLoading(false);
      } catch (err) {
        setError('Không thể tải kết quả tìm kiếm');
        setLoading(false);
      }
    };

    if (query) {
      fetchSearchResults();
    } else {
      setError('Không có từ khóa tìm kiếm');
      setLoading(false);
    }
  }, [query]);

  if (loading) return <div>Đang tải...</div>;
  if (error) return <div>{error}</div>;

  return (
    <div>
      <h2>Kết quả tìm kiếm cho "{query}"</h2>
      {emails.length > 0 ? (
        emails.map((email) => (
          <div key={email.id} className="mb-3 p-3 border">
            <p><strong>Từ:</strong> {email.from}</p>
            <p><strong>Chủ đề:</strong> {email.sub}</p>
            <p>{email.body.substring(0, 100)}...</p>
          </div>
        ))
      ) : (
        <p>Không tìm thấy kết quả nào</p>
      )}
    </div>
  );
}