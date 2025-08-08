import React, { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';

export default function SearchBarWithFilters({ onSearch }) {
  const [searchParams] = useSearchParams();

  const [query, setQuery] = useState('');
  const [fromDate, setFromDate] = useState('');
  const [toDate, setToDate] = useState('');
  const [hasAttachment, setHasAttachment] = useState(false);
  const [type, setType] = useState('ALL');

  useEffect(() => {
    setQuery(searchParams.get('query') || '');
    setFromDate(searchParams.get('fromDate') || '');
    setToDate(searchParams.get('toDate') || '');
    setHasAttachment(searchParams.get('hasAttachment') === 'true');
    setType(searchParams.get('type') || 'ALL');
  }, [searchParams]);

  const handleSubmit = (e) => {
    e.preventDefault();
    onSearch({ query, fromDate, toDate, hasAttachment, type });
  };

  return (
    <form onSubmit={handleSubmit} className="mb-3">
      <div className="row g-2">
        <div className="col-md-3">
          <input
            type="text"
            className="form-control"
            placeholder="Tìm kiếm nội dung hoặc tiêu đề..."
            value={query}
            onChange={(e) => setQuery(e.target.value)}
          />
        </div>
        <div className="col-md-2">
          <input
            type="date"
            className="form-control"
            value={fromDate}
            onChange={(e) => setFromDate(e.target.value)}
          />
        </div>
        <div className="col-md-2">
          <input
            type="date"
            className="form-control"
            value={toDate}
            onChange={(e) => setToDate(e.target.value)}
          />
        </div>
        <div className="col-md-1 d-flex align-items-center">
          <div className="form-check">
            <input
              className="form-check-input"
              type="checkbox"
              id="hasAttachment"
              checked={hasAttachment}
              onChange={(e) => setHasAttachment(e.target.checked)}
            />
            <label className="form-check-label" htmlFor="hasAttachment">
              Đính kèm
            </label>
          </div>
        </div>
        <div className="col-md-2">
          <select
            className="form-select"
            value={type}
            onChange={(e) => setType(e.target.value)}
          >
            <option value="ALL">Tất cả</option>
            <option value="SENT">Đã gửi</option>
            <option value="INBOX">Đã nhận</option>
          </select>
        </div>
        <div className="col-md-2">
          <button type="submit" className="btn btn-primary w-100">
            Tìm kiếm
          </button>
        </div>
      </div>
    </form>
  );
}
