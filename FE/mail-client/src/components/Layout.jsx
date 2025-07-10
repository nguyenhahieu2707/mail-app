// import { Outlet, NavLink, useNavigate } from 'react-router-dom';
// import { FiMail, FiSend, FiEdit3, FiLogOut, FiUser } from 'react-icons/fi';
// import './Layout.css';

// // Layout chính cho các trang sau khi đăng nhập
// export function MainLayout() {
//   const navigate = useNavigate();

//   const handleLogout = () => {
//     navigate('/logout');
//   };

//   return (
//     <div className="main-layout">
//       <nav className="navbar">
//         <div className="navbar-brand">
//           <FiMail className="brand-icon" />
//           <span>MailClient</span>
//         </div>
//         <div className="nav-links">
//           <NavLink to="/inbox" className="nav-link">
//             <FiMail />
//             <span>Hộp thư đến</span>
//           </NavLink>
//           <NavLink to="/sent" className="nav-link">
//             <FiSend />
//             <span>Thư đã gửi</span>
//           </NavLink>
//           <NavLink to="/compose" className="nav-link">
//             <FiEdit3 />
//             <span>Soạn thư</span>
//           </NavLink>
//         </div>
//         <div className="user-profile">
//           <FiUser className="user-avatar" />
//           <button onClick={handleLogout} className="logout-button">
//             <FiLogOut />
//             <span>Đăng xuất</span>
//           </button>
//         </div>
//       </nav>
//       <main className="content">
//         <Outlet />
//       </main>
//     </div>
//   );
// }

// // Layout cho các trang xác thực (Đăng nhập, Callback,...)
// export function AuthLayout() {
//   return (
//     <div className="auth-layout">
//       <Outlet />
//     </div>
//   );
// }

import React from 'react';
import { Link, Outlet, useNavigate } from 'react-router-dom';
import MailNotification from './MailNotification'; // 👈 Import notification component

export function MainLayout() {
  const navigate = useNavigate();

  const handleLogout = () => {
    navigate('/logout');
  };

  return (
    <div className="container-fluid">
      <nav className="navbar navbar-expand-lg navbar-light bg-light mb-4">
        <div className="container d-flex justify-content-between w-100">
          <div className="navbar-nav">
            <a className="navbar-brand" href="#">Email Client</a>
            <Link to="/inbox" className="nav-link">Inbox</Link>
            <Link to="/sent" className="nav-link">Sent Box</Link>
            <Link to="/compose" className="nav-link">Compose</Link>
          </div>
          <button
            onClick={handleLogout}
            className="btn btn-outline-danger"
          >
            Logout
          </button>
        </div>
      </nav>

      {/* 👇 Hiển thị danh sách hoặc popup mail realtime */}
      <MailNotification />

      {/* Outlet để render route con */}
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
