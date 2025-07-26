import { Routes, Route } from 'react-router-dom';
import { MainLayout, AuthLayout } from './components/Layout.jsx';
import Inbox from './pages/Inbox.jsx';
import SentBox from './pages/SentBox.jsx';
import Login from './components/Login.jsx';
import InboxEmailDetail from './components/InboxEmailDetail.jsx';
import SentEmailDetail from './components/SentMailDetail.jsx';
import ComposeMail from './components/ComposeMail.jsx';
import LaoIDCallback from './components/LaoIDCallback.jsx';
import Logout from './components/Logout.jsx';
import SearchResults from './pages/SearchResults.jsx'; 
import { remoteLogger } from './utils/remoteLogger';

import './components/Layout.css';
import './components/Login.css';

// ... (code window.onerror và window.onunhandledrejection giữ nguyên)

function App() {
  return (
    <Routes>
      {/* Các route không cần xác thực */}
      <Route element={<AuthLayout />}>
        <Route path="/" element={<Login />} />
        {/* ✅ SỬA LẠI PATH Ở ĐÂY */}
        <Route path="/laoid-callback" element={<LaoIDCallback />} />
        <Route path="/logout" element={<Logout />} />
      </Route>

      {/* Các route cần xác thực, sử dụng MainLayout */}
      <Route element={<MainLayout />}>
        <Route path="/inbox" element={<Inbox />} />
        <Route path="/sent" element={<SentBox />} />
        <Route path="/email/sent/:id" element={<SentEmailDetail />} />
        <Route path="/email/inbox/:id" element={<InboxEmailDetail />} />
        <Route path="/compose" element={<ComposeMail />} />
        <Route path="/search" element={<SearchResults />} />
      </Route>
    </Routes>
  );
}

export default App;