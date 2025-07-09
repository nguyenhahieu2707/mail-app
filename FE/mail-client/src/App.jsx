// // import { Routes, Route, Link, useNavigate } from 'react-router-dom';
// // import Inbox from './components/Inbox.jsx';
// // import SentBox from './components/SentBox.jsx';
// // import Login from './components/Login.jsx';
// // import EmailDetail from './components/EmailDetail.jsx';
// // import ComposeMail from './components/ComposeMail.jsx';
// // import LaoIDCallback from "./components/LaoIDCallback.jsx";
// // import Logout from './components/Logout.jsx';

// // function App() {
// //     const navigate = useNavigate();

// //     const handleLogout = () => {
// //         navigate('/logout');
// //     };

// //     return (
// //         <div className="container-fluid">
// //             <nav className="navbar navbar-expand-lg navbar-light bg-light mb-4">
// //                 <div className="container d-flex justify-content-between w-100">
// //                     <div className="navbar-nav">
// //                         <a className="navbar-brand" href="#">Email Client</a>
// //                         <Link to="/inbox" className="nav-link">Inbox</Link>
// //                         <Link to="/sent" className="nav-link">Sent Box</Link>
// //                         <Link to="/compose" className="nav-link">Compose</Link>
// //                     </div>
// //                     <button
// //                         onClick={handleLogout}
// //                         className="btn btn-outline-danger"
// //                     >
// //                         Logout
// //                     </button>
// //                 </div>
// //             </nav>
// //             <Routes>
// //                 <Route path="/" element={<Login />} />
// //                 <Route path="/inbox" element={<Inbox />} />
// //                 <Route path="/sent" element={<SentBox />} />
// //                 <Route path="/email/:id" element={<EmailDetail />} />
// //                 <Route path="/compose" element={<ComposeMail />} />
// //                 <Route path="/laoid/callback" element={<LaoIDCallback />} />
// //                 <Route path="/logout" element={<Logout />} />
// //             </Routes>
// //         </div>
// //     );
// // }

// // export default App;


// import { Routes, Route, Link, Outlet, useNavigate } from 'react-router-dom';
// import Inbox from './pages/Inbox.jsx';
// import SentBox from './pages/SentBox.jsx';
// import Login from './components/Login.jsx';
// import EmailDetail from './components/EmailDetail.jsx';
// import ComposeMail from './components/ComposeMail.jsx';
// import LaoIDCallback from './components/LaoIDCallback.jsx';
// import Logout from './components/Logout.jsx';

// // Layout for authenticated routes (with navbar)
// function MainLayout() {
//   const navigate = useNavigate();

//   const handleLogout = () => {
//     navigate('/logout');
//   };

//   return (
//     <div className="container-fluid">
//       <nav className="navbar navbar-expand-lg navbar-light bg-light mb-4">
//         <div className="container d-flex justify-content-between w-100">
//           <div className="navbar-nav">
//             <a className="navbar-brand" href="#">Email Client</a>
//             <Link to="/inbox" className="nav-link">Inbox</Link>
//             <Link to="/sent" className="nav-link">Sent Box</Link>
//             <Link to="/compose" className="nav-link">Compose</Link>
//           </div>
//           <button
//             onClick={handleLogout}
//             className="btn btn-outline-danger"
//           >
//             Logout
//           </button>
//         </div>
//       </nav>
//       <Outlet />
//     </div>
//   );
// }

// // Layout for auth-related routes (no navbar)
// function AuthLayout() {
//   return (
//     <div className="container-fluid">
//       <Outlet />
//     </div>
//   );
// }

// function App() {
//   return (
//     <Routes>
//       <Route element={<AuthLayout />}>
//         <Route path="/" element={<Login />} />
//         <Route path="/laoid/callback" element={<LaoIDCallback />} />
//         <Route path="/logout" element={<Logout />} />
//       </Route>
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
import Inbox from './pages/Inbox.jsx';         // Đã chuyển vào pages
import SentBox from './pages/SentBox.jsx';     // Đã chuyển vào pages
import Login from './components/Login.jsx';
import EmailDetail from './components/EmailDetail.jsx';
import ComposeMail from './components/ComposeMail.jsx';
import LaoIDCallback from './components/LaoIDCallback.jsx';
import Logout from './components/Logout.jsx';

// Import các file CSS mới
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
      </Route>
    </Routes>
  );
}

export default App;