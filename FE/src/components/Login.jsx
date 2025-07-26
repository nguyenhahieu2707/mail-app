import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { FiMail, FiLock, FiLoader, FiGrid, FiUser } from 'react-icons/fi';
import './Login.css';

function Login() {
  // State cho chế độ (đăng nhập / đăng ký)
  const [isRegisterMode, setIsRegisterMode] = useState(false);

  // State cho các form
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState(''); // Thêm cho form đăng ký
  const [firstName, setFirstName] = useState('');             // Thêm cho form đăng ký
  const [lastName, setLastName] = useState('');               // Thêm cho form đăng ký

  // State cho thông báo và tải
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  
  const navigate = useNavigate();

  // --- HÀM XỬ LÝ ---

  const handleLogin = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    setError('');
    setSuccessMessage('');

    try {
      const response = await axios.post('/auth/token', { email, password });
      const { result } = response.data;
      localStorage.setItem('accessToken', result.token);
      localStorage.setItem('refreshToken', result.refreshToken);
      localStorage.setItem('email', email);
      navigate('/inbox');
    } catch (err) {
      setError('Đăng nhập thất bại. Vui lòng kiểm tra lại email hoặc mật khẩu.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    if (password !== confirmPassword) {
      setError('Mật khẩu xác nhận không khớp.');
      return;
    }
    
    setIsLoading(true);
    setError('');
    setSuccessMessage('');

    try {
      const requestBody = { email, password, firstName, lastName };
      await axios.post('/users', requestBody); // Gọi API đăng ký
      setSuccessMessage('Đăng ký thành công! Vui lòng đăng nhập.');
      toggleMode(); // Chuyển về màn hình đăng nhập
    } catch (err) {
      // Xử lý lỗi từ backend (ví dụ: email đã tồn tại)
      const errorMessage = err.response?.data?.message || 'Đăng ký thất bại. Vui lòng thử lại.';
      setError(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  const handleLaoIDLogin = () => {
    // Logic không đổi
    const clientId = '660dfa27-5a95-4c88-8a55-abe1310bf579';
    const redirectUri = 'http://localhost/laoid/auth/callback';
    const loginUrl = `https://demo-sso.tinasoft.io/login?client_id=${clientId}&redirect_uri=${encodeURIComponent(redirectUri)}&use_callback_uri=true`;
    window.location.href = loginUrl;
  };

  const toggleMode = () => {
    setIsRegisterMode(!isRegisterMode);
    // Reset state khi chuyển chế độ
    setError('');
    setSuccessMessage('');
    setEmail('');
    setPassword('');
    setConfirmPassword('');
    setFirstName('');
    setLastName('');
  };

  // --- GIAO DIỆN ---

  return (
    <div className="login-container">
      <div className="login-card">
        <h2 className="login-title">{isRegisterMode ? 'Tạo tài khoản mới' : 'Chào mừng trở lại'}</h2>
        <p className="login-subtitle">{isRegisterMode ? 'Điền thông tin để bắt đầu' : 'Đăng nhập để tiếp tục'}</p>

        {error && <div className="login-error">{error}</div>}
        {successMessage && <div className="login-success">{successMessage}</div>}

        <form onSubmit={isRegisterMode ? handleRegister : handleLogin} className="login-form">
          {/* Trường cho đăng ký */}
          {isRegisterMode && (
            <>
              <div className="input-wrapper">
                <FiUser className="input-icon" />
                <input type="text" className="form-input" value={firstName} onChange={(e) => setFirstName(e.target.value)} required placeholder="Họ" disabled={isLoading} />
              </div>
              <div className="input-wrapper">
                <FiUser className="input-icon" />
                <input type="text" className="form-input" value={lastName} onChange={(e) => setLastName(e.target.value)} required placeholder="Tên" disabled={isLoading} />
              </div>
            </>
          )}

          {/* Trường chung */}
          <div className="input-wrapper">
            <FiMail className="input-icon" />
            <input type="email" className="form-input" value={email} onChange={(e) => setEmail(e.target.value)} required placeholder="Email" disabled={isLoading} />
          </div>
          <div className="input-wrapper">
            <FiLock className="input-icon" />
            <input type="password" className="form-input" value={password} onChange={(e) => setPassword(e.target.value)} required placeholder="Mật khẩu" disabled={isLoading} />
          </div>
          
          {/* Trường xác nhận mật khẩu cho đăng ký */}
          {isRegisterMode && (
            <div className="input-wrapper">
              <FiLock className="input-icon" />
              <input type="password" className="form-input" value={confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)} required placeholder="Xác nhận mật khẩu" disabled={isLoading} />
            </div>
          )}

          <button type="submit" className="login-button" disabled={isLoading}>
            {isLoading ? <FiLoader className="spinner" /> : (isRegisterMode ? 'Đăng ký' : 'Đăng nhập')}
          </button>
        </form>

        <div className="divider">hoặc</div>

        <button className="laoid-button" onClick={handleLaoIDLogin}>
          <FiGrid />
          <span>Đăng nhập với LaoID</span>
        </button>

        <p className="toggle-mode">
          {isRegisterMode ? 'Đã có tài khoản? ' : 'Chưa có tài khoản? '}
          <span onClick={toggleMode} className="toggle-mode-link">
            {isRegisterMode ? 'Đăng nhập ngay' : 'Đăng ký'}
          </span>
        </p>
      </div>
    </div>
  );
}

export default Login;