import React, { useState } from 'react';

function SearchBarWithFilters({ onSearch, initialQuery }) {
  const [query, setQuery] = useState(initialQuery || '');
  const [fromDate, setFromDate] = useState('');
  const [toDate, setToDate] = useState('');
  const [hasAttachment, setHasAttachment] = useState(false);

  const handleSubmit = (e) => {
    e.preventDefault();
    onSearch({ query, fromDate, toDate, hasAttachment });
  };

  return (
    <form className="row g-3 mb-3" onSubmit={handleSubmit}>
      <div className="col-md-4">
        <input
          className="form-control"
          placeholder="Từ khóa tìm kiếm"
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
      <div className="col-md-2 d-flex align-items-center">
        <div className="form-check">
          <input
            type="checkbox"
            className="form-check-input"
            checked={hasAttachment}
            onChange={(e) => setHasAttachment(e.target.checked)}
            id="attach-check"
          />
          <label className="form-check-label" htmlFor="attach-check">
            Có đính kèm
          </label>
        </div>
      </div>
      <div className="col-md-2">
        <button className="btn btn-primary w-100" type="submit">Tìm kiếm</button>
      </div>
    </form>
  );
}

export default SearchBarWithFilters;
