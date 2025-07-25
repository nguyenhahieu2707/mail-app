// import { Routes, Route } from 'react-router-dom';
// import { MainLayout, AuthLayout } from './components/Layout.jsx';
// import Inbox from './pages/Inbox.jsx';         // Đã chuyển vào pages
// import SentBox from './pages/SentBox.jsx';     // Đã chuyển vào pages
// import Login from './components/Login.jsx';
// import EmailDetail from './components/EmailDetail.jsx';
// import ComposeMail from './components/ComposeMail.jsx';
// import LaoIDCallback from './components/LaoIDCallback.jsx';
// import Logout from './components/Logout.jsx';


// // Import các file CSS mới
// import './components/Layout.css';
// import './components/Login.css';

// function App() {
//   return (
//     <Routes>
//       {/* Các route không cần xác thực */}
//       <Route element={<AuthLayout />}>
//         <Route path="/" element={<Login />} />
//         <Route path="/laoid/auth/callback" element={<LaoIDCallback />} />
//         <Route path="/logout" element={<Logout />} />
//       </Route>

//       {/* Các route cần xác thực, sử dụng MainLayout */}
//       <Route element={<MainLayout />}>
//         <Route path="/inbox" element={<Inbox />} />
//         <Route path="/sent" element={<SentBox />} />
//         <Route path="/email/:id" element={<EmailDetail />} />
//         <Route path="/compose" element={<ComposeMail />} />
//       </Route>
//     </Routes>
//   );
// }

// export default App;

import { Routes, Route } from 'react-router-dom';
import { MainLayout, AuthLayout } from './components/Layout.jsx';
import Inbox from './pages/Inbox.jsx';
import SentBox from './pages/SentBox.jsx';
import Login from './components/Login.jsx';
import EmailDetail from './components/EmailDetail.jsx';
import ComposeMail from './components/ComposeMail.jsx';
import LaoIDCallback from './components/LaoIDCallback.jsx';
import Logout from './components/Logout.jsx';
import SearchResults from './pages/SearchResults.jsx'; // Thêm import

import './components/Layout.css';
import './components/Login.css';

function App() {
  return (
    <Routes>
      {/* Các route không cần xác thực */}
      <Route element={<AuthLayout />}>
        <Route path="/" element={<Login />} />
        <Route path="/laoid/auth/callback" element={<LaoIDCallback />} />
        <Route path="/logout" element={<Logout />} />
      </Route>

      {/* Các route cần xác thực, sử dụng MainLayout */}
      <Route element={<MainLayout />}>
        <Route path="/inbox" element={<Inbox />} />
        <Route path="/sent" element={<SentBox />} />
        <Route path="/email/:id" element={<EmailDetail />} />
        <Route path="/compose" element={<ComposeMail />} />
        <Route path="/search" element={<SearchResults />} /> {/* Thêm route */}
      </Route>
    </Routes>
  );
}

export default App;