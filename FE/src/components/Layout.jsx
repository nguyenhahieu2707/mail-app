import { Outlet, useNavigate, NavLink } from 'react-router-dom';
import MailNotification from './MailNotification';

// Import CSS và các thành phần icon từ react-icons
import './Layout.css';
import { FiEdit, FiInbox, FiSend, FiSearch, FiUser } from 'react-icons/fi';

export function MainLayout() {
  const navigate = useNavigate();
  const userEmail = localStorage.getItem('email') || 'user@example.com';

  const handleLogout = () => {
    if (window.confirm('Bạn có chắc chắn muốn đăng xuất không?')) {
      navigate('/logout');
    }
  };

  return (
    <div className="layout-container">
      {/* Sidebar */}
      <div className="sidebar">
        <h4 className="mb-4">Mail App</h4>
        
        <div className="d-grid mb-4">
          <NavLink to="/compose" className="btn btn-primary d-flex align-items-center justify-content-center">
            <FiEdit className="me-2" />Soạn thư
          </NavLink>
        </div>

        {/* Menu */}
        <ul className="nav flex-column nav-pills">
          <li className="nav-item">
            <NavLink to="/inbox" className="nav-link">
              <FiInbox className="me-2" />Hộp thư đến
            </NavLink>
          </li>
          <li className="nav-item">
            <NavLink to="/sent" className="nav-link">
              <FiSend className="me-2" />Đã gửi
            </NavLink>
          </li>
          <li className="nav-item">
            <NavLink to="/search" className="nav-link">
              <FiSearch className="me-2" />Tìm kiếm
            </NavLink>
          </li>
        </ul>

        {/* User Info & Logout */}
        <div className="position-absolute bottom-0 start-0 p-3 w-100">
          <div className="d-flex align-items-center mb-2">
            <MailNotification />
          </div>
          <div className="d-flex align-items-center">
            <FiUser size={32} className="me-2 flex-shrink-0" />
            <div className="text-truncate">
              <strong className="d-block text-truncate">{userEmail}</strong>
              <a href="#" onClick={handleLogout} className="d-block text-muted small text-decoration-none">
                Đăng xuất
              </a>
            </div>
          </div>
        </div>
      </div>

      {/* Main Content Area */}
      <main className="main-content">
        <Outlet />
      </main>
    </div>
  );
}

export function AuthLayout() {
  return (
    <div>
      <Outlet />
    </div>
  );
}
