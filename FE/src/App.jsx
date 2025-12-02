import { Routes, Route, Navigate } from 'react-router-dom';
import { MainLayout, AuthLayout } from './components/Layout.jsx';
import Inbox from './pages/Inbox.jsx';
import SentBox from './pages/SentBox.jsx';
import Login from './components/Login.jsx';
import ComposeMail from './components/ComposeMail.jsx';
import LaoIDCallback from './components/LaoIDCallback.jsx';
import Logout from './components/Logout.jsx';
import SearchResults from './pages/SearchResults.jsx';
import { remoteLogger } from './utils/remoteLogger';

// Import CSS cho các thành phần chính ở đây nếu cần
import './components/Layout.css';
import './components/Login.css';

function App() {
  return (
    <Routes>
      {/* Các route không cần xác thực, sử dụng AuthLayout */}
      <Route element={<AuthLayout />}>
        {/* Cả hai đường dẫn / và /login đều trỏ đến Login.jsx */}
        <Route path="/login" element={<Login />} />
        <Route path="/" element={<Navigate to="/login" replace />} />

        {/* Các route callback hoặc logout khác */}
        <Route path="/laoid-callback" element={<LaoIDCallback />} />
        <Route path="/logout" element={<Logout />} />
      </Route>

      {/* Các route cần xác thực, sử dụng MainLayout với sidebar */}
      <Route element={<MainLayout />}>
        <Route path="/inbox" element={<Inbox />} />
        <Route path="/sent" element={<SentBox />} />
        <Route path="/compose" element={<ComposeMail />} />
        <Route path="/search" element={<SearchResults />} />
        {/*
          Các route chi tiết email cũ đã được xóa vì logic đã được tích hợp
          vào màn hình Inbox và SentBox với bố cục hai cột.
          <Route path="/email/sent/:id" element={<SentEmailDetail />} />
          <Route path="/email/inbox/:id" element={<InboxEmailDetail />} />
        */}
      </Route>
    </Routes>
  );
}

export default App;
