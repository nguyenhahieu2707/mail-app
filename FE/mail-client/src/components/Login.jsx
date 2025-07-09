// import { useState, useEffect } from 'react';
// import { useNavigate } from 'react-router-dom';
// import axios from 'axios';
// import { FiMail, FiLock, FiLoader, FiGrid } from 'react-icons/fi'; // Thêm icons
// import './Login.css';

// // Gợi ý: Nên đưa URL này vào file môi trường (.env) để dễ quản lý
// const API_URL = 'http://localhost:8080/mailapp/auth/token';

// function Login() {
//   const [email, setEmail] = useState('');
//   const [password, setPassword] = useState('');
//   const [error, setError] = useState('');
//   const [isLoading, setIsLoading] = useState(false); // Thêm trạng thái loading
//   const navigate = useNavigate();

//   const handleLogin = async (e) => {
//     e.preventDefault();
//     setIsLoading(true); // Bắt đầu loading
//     setError('');

//     try {
//       const response = await axios.post(API_URL, { email, password });
//       const { result } = response.data;
//       localStorage.setItem('accessToken', result.token);
//       localStorage.setItem('refreshToken', result.refreshToken);
//       navigate('/inbox');
//     } catch (err) {
//       setError('Đăng nhập thất bại. Vui lòng kiểm tra lại email hoặc mật khẩu.');
//     } finally {
//       setIsLoading(false); // Kết thúc loading
//     }
//   };
  
//   // Script cho LaoID vẫn giữ nguyên
//   useEffect(() => {
//     const script = document.createElement('script');
//     script.src = 'https://demo-sso.tinasoft.io/laoid.auth.js';
//     script.async = true;
//     document.body.appendChild(script);

//     // Dọn dẹp script khi component bị hủy
//     return () => {
//       document.body.removeChild(script);
//     };
//   }, []);

//   useEffect(() => {
//     const script = document.createElement('script');
//     script.src = 'https://demo-sso.tinasoft.io/laoid.auth.js';
//     script.async = true;
//     script.onload = () => {
//       const callbackUrl = 'http://localhost/laoid/auth/callback';
//       window.LaoIdSSO.init(
//         '660dfa27-5a95-4c88-8a55-abe1310bf579',
//         callbackUrl,
//         true
//       );
//     };

//     document.body.appendChild(script);

//     return () => {
//       document.body.removeChild(script);
//     };
//   }, []);



//   return (
//     <div className="login-container">
//       <div className="login-card">
//         <h2 className="login-title">Chào mừng trở lại</h2>
//         <p className="login-subtitle">Đăng nhập để tiếp tục</p>

//         {error && <div className="login-error">{error}</div>}
        
//         <form onSubmit={handleLogin} className="login-form">
//           <div className="input-wrapper">
//             <FiMail className="input-icon" />
//             <input
//               type="email"
//               className="form-input"
//               value={email}
//               onChange={(e) => setEmail(e.target.value)}
//               required
//               placeholder="Email"
//               disabled={isLoading}
//             />
//           </div>
//           <div className="input-wrapper">
//             <FiLock className="input-icon" />
//             <input
//               type="password"
//               className="form-input"
//               value={password}
//               onChange={(e) => setPassword(e.target.value)}
//               required
//               placeholder="Mật khẩu"
//               disabled={isLoading}
//             />
//           </div>
//           <button type="submit" className="login-button" disabled={isLoading}>
//             {isLoading ? <FiLoader className="spinner" /> : 'Đăng nhập'}
//           </button>
//         </form>
        
//         <div className="divider">hoặc</div>
//         <button className="laoid-button" onClick={() => {
//           // Redirect đến URL login
//           window.location.href = `https://demo-sso.tinasoft.io/login?client_id=660dfa27-5a95-4c88-8a55-abe1310bf579&redirect_uri=http://localhost/laoid/auth/callback&use_callback_uri=true`;
//         }}>
//           <FiGrid />
//           <span>Đăng nhập với LaoID</span>
//         </button>  
//       </div>
//     </div>
//   );
// }

// export default Login;


import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { FiMail, FiLock, FiLoader, FiGrid } from 'react-icons/fi';
import './Login.css';

const API_URL = 'http://localhost:8080/auth/token';

function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    setError('');

    try {
      const response = await axios.post(API_URL, { email, password });
      const { result } = response.data;
      localStorage.setItem('accessToken', result.token);
      localStorage.setItem('refreshToken', result.refreshToken);
      navigate('/inbox');
    } catch (err) {
      setError('Đăng nhập thất bại. Vui lòng kiểm tra lại email hoặc mật khẩu.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleLaoIDLogin = () => {
    const clientId = '660dfa27-5a95-4c88-8a55-abe1310bf579';
    const redirectUri = 'http://localhost/laoid/auth/callback';
    const loginUrl = `https://demo-sso.tinasoft.io/login?client_id=${clientId}&redirect_uri=${encodeURIComponent(redirectUri)}&use_callback_uri=true`;
    window.location.href = loginUrl;
  };

  return (
    <div className="login-container">
      <div className="login-card">
        <h2 className="login-title">Chào mừng trở lại</h2>
        <p className="login-subtitle">Đăng nhập để tiếp tục</p>

        {error && <div className="login-error">{error}</div>}

        <form onSubmit={handleLogin} className="login-form">
          <div className="input-wrapper">
            <FiMail className="input-icon" />
            <input
              type="email"
              className="form-input"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              placeholder="Email"
              disabled={isLoading}
            />
          </div>
          <div className="input-wrapper">
            <FiLock className="input-icon" />
            <input
              type="password"
              className="form-input"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              placeholder="Mật khẩu"
              disabled={isLoading}
            />
          </div>
          <button type="submit" className="login-button" disabled={isLoading}>
            {isLoading ? <FiLoader className="spinner" /> : 'Đăng nhập'}
          </button>
        </form>

        <div className="divider">hoặc</div>
        <button className="laoid-button" onClick={handleLaoIDLogin}>
          <FiGrid />
          <span>Đăng nhập với LaoID</span>
        </button>
      </div>
    </div>
  );
}

export default Login;
