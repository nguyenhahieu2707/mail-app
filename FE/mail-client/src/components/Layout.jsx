// // import React from 'react';
// // import { Link, Outlet, useNavigate } from 'react-router-dom';
// // import MailNotification from './MailNotification'; // 👈 Import notification component

// // export function MainLayout() {
// //   const navigate = useNavigate();

// //   const handleLogout = () => {
// //     navigate('/logout');
// //   };

// //   return (
// //     <div className="container-fluid">
// //       <nav className="navbar navbar-expand-lg navbar-light bg-light mb-4">
// //         <div className="container d-flex justify-content-between w-100">
// //           <div className="navbar-nav">
// //             <a className="navbar-brand" href="#">Email Client</a>
// //             <Link to="/inbox" className="nav-link">Inbox</Link>
// //             <Link to="/sent" className="nav-link">Sent Box</Link>
// //             <Link to="/compose" className="nav-link">Compose</Link>
// //           </div>
// //           <button
// //             onClick={handleLogout}
// //             className="btn btn-outline-danger"
// //           >
// //             Logout
// //           </button>
// //         </div>
// //       </nav>

// //       {/* 👇 Hiển thị danh sách hoặc popup mail realtime */}
// //       <MailNotification />

// //       {/* Outlet để render route con */}
// //       <Outlet />
// //     </div>
// //   );
// // }

// // export function AuthLayout() {
// //   return (
// //     <div className="container-fluid">
// //       <Outlet />
// //     </div>
// //   );
// // }

// import React from 'react';
// import { Link, Outlet, useNavigate } from 'react-router-dom';
// import MailNotification from './MailNotification';

// export function MainLayout() {
//   const navigate = useNavigate();

//   const handleLogout = () => {
//     navigate('/logout');
//   };

//   return (
//     <div className="container-fluid">
//       <nav className="navbar navbar-expand-lg navbar-light bg-light mb-4">
//         <div className="container d-flex justify-content-between w-100 align-items-center">
//           <div className="navbar-nav">
//             <a className="navbar-brand" href="#">Email Client</a>
//             <Link to="/inbox" className="nav-link">Inbox</Link>
//             <Link to="/sent" className="nav-link">Sent Box</Link>
//             <Link to="/compose" className="nav-link">Compose</Link>
//           </div>
//           <div className="d-flex align-items-center">
//             <MailNotification />
//             <button
//               onClick={handleLogout}
//               className="btn btn-outline-danger ms-3"
//             >
//               Logout
//             </button>
//           </div>
//         </div>
//       </nav>

//       {/* Outlet để render route con */}
//       <Outlet />
//     </div>
//   );
// }

// export function AuthLayout() {
//   return (
//     <div className="container-fluid">
//       <Outlet />
//     </div>
//   );
// }

import React, { useState } from 'react';
import { Link, Outlet, useNavigate } from 'react-router-dom';
import MailNotification from './MailNotification';

export function MainLayout() {
  const [searchQuery, setSearchQuery] = useState('');
  const navigate = useNavigate();

  const handleSearch = (e) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      navigate(`/search?query=${encodeURIComponent(searchQuery)}`);
    }
  };

  const handleLogout = () => {
    navigate('/logout');
  };

  return (
    <div className="container-fluid">
      <nav className="navbar navbar-expand-lg navbar-light bg-light mb-4">
        <div className="container d-flex justify-content-between w-100 align-items-center">
          <div className="navbar-nav">
            <a className="navbar-brand" href="#">Email Client</a>
            <Link to="/inbox" className="nav-link">Inbox</Link>
            <Link to="/sent" className="nav-link">Sent Box</Link>
            <Link to="/compose" className="nav-link">Compose</Link>
          </div>
          <div className="d-flex align-items-center">
            <form className="d-flex me-3" onSubmit={handleSearch}>
              <input
                className="form-control me-2"
                type="search"
                placeholder="Search emails"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
              <button className="btn btn-outline-success" type="submit">
                Search
              </button>
            </form>
            <MailNotification />
            <button
              onClick={handleLogout}
              className="btn btn-outline-danger ms-3"
            >
              Logout
            </button>
          </div>
        </div>
      </nav>
      <Outlet />
    </div>
  );
}

export function AuthLayout() {
  return (
    <div className="container-fluid">
      <Outlet />
    </div>
  );
}