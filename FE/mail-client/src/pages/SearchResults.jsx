import React, { useEffect, useState } from 'react';
import { useLocation } from 'react-router-dom';
import axios from 'axios';
import SearchBarWithFilters from './SearchBarWithFilters.jsx';
import EmailList from '../components/EmailList.jsx';

const API_URL = 'http://localhost:8080';

const authHeader = () => ({
  headers: {
    Authorization: `Bearer ${localStorage.getItem('accessToken')}`
  }
});

function SearchResults() {
  const location = useLocation();
  const queryParam = new URLSearchParams(location.search).get('query') || '';

  const [emails, setEmails] = useState([]);
  const [searchParams, setSearchParams] = useState({
    query: queryParam,
    fromDate: null,
    toDate: null,
    hasAttachment: false
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchEmails = async () => {
    setLoading(true);
    try {
      const response = await axios.post(
        `${API_URL}/mail/search`,
        {
          ...searchParams,
          page: 0,
          size: 20
        },
        authHeader()
      );
      setEmails(response.data.result.content);
      setError(null);
    } catch (err) {
      setError("Không thể tải kết quả tìm kiếm");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (searchParams.query) {
      fetchEmails();
    }
  }, [searchParams]);

  return (
    <div className="container">
      <SearchBarWithFilters
        onSearch={(params) => setSearchParams(params)}
        initialQuery={queryParam}
      />
      <EmailList
        title={`Kết quả tìm kiếm cho "${searchParams.query}"`}
        emails={emails}
        type="search"
        isLoading={loading}
        error={error}
      />
    </div>
  );
}

export default SearchResults;
