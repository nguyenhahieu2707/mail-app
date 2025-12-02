import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { FiMail, FiLock, FiLoader, FiUser } from 'react-icons/fi';
import './Login.css'; // Đảm bảo CSS được import

function Login() {
  const [isRegisterMode, setIsRegisterMode] = useState(false);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();

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
      await axios.post('/users', requestBody);
      setSuccessMessage('Đăng ký thành công! Vui lòng đăng nhập.');
      toggleMode();
    } catch (err) {
      const errorMessage = err.response?.data?.message || 'Đăng ký thất bại. Vui lòng thử lại.';
      setError(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  const toggleMode = () => {
    setIsRegisterMode(!isRegisterMode);
    setError('');
    setSuccessMessage('');
    setEmail('');
    setPassword('');
    setConfirmPassword('');
    setFirstName('');
    setLastName('');
  };

  // Render form đăng nhập hoặc đăng ký dựa trên state
  const renderLogin = () => (
    <div className="card p-4 login-card">
      <div className="card-body">
        <div className="text-center mb-4">
          <h2 className="card-title fw-bold">Chào mừng trở lại</h2>
          <p className="text-muted">Đăng nhập để tiếp tục</p>
        </div>
        <form onSubmit={handleLogin}>
          <div className="mb-3 position-relative">
            <FiMail className="form-icon" />
            <input type="email" className="form-control form-control-icon" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="Email" required disabled={isLoading} />
          </div>
          <div className="mb-3 position-relative">
            <FiLock className="form-icon" />
            <input type="password" className="form-control form-control-icon" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="Mật khẩu" required disabled={isLoading} />
          </div>
          <div className="d-grid mb-3">
            <button type="submit" className="btn btn-primary" disabled={isLoading}>
              {isLoading ? <FiLoader className="spinner" /> : 'Đăng nhập'}
            </button>
          </div>
        </form>
        <p className="text-center mt-4">
          Chưa có tài khoản? <a href="#" onClick={toggleMode}>Đăng ký</a>
        </p>
      </div>
    </div>
  );

  const renderRegister = () => (
    <div className="card p-4 login-card">
      <div className="card-body">
        <div className="text-center mb-4">
          <h2 className="card-title fw-bold">Tạo tài khoản mới</h2>
          <p className="text-muted">Điền thông tin để bắt đầu</p>
        </div>
        <form onSubmit={handleRegister}>
          <div className="row">
            <div className="col-md-6 mb-3 position-relative">
              <FiUser className="form-icon" />
              <input type="text" className="form-control form-control-icon" value={firstName} onChange={(e) => setFirstName(e.target.value)} placeholder="Họ" required disabled={isLoading} />
            </div>
            <div className="col-md-6 mb-3 position-relative">
              <FiUser className="form-icon" />
              <input type="text" className="form-control form-control-icon" value={lastName} onChange={(e) => setLastName(e.target.value)} placeholder="Tên" required disabled={isLoading} />
            </div>
          </div>
          <div className="mb-3 position-relative">
            <FiMail className="form-icon" />
            <input type="email" className="form-control form-control-icon" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="Email" required disabled={isLoading} />
          </div>
          <div className="mb-3 position-relative">
            <FiLock className="form-icon" />
            <input type="password" className="form-control form-control-icon" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="Mật khẩu" required disabled={isLoading} />
          </div>
          <div className="mb-3 position-relative">
            <FiLock className="form-icon" />
            <input type="password" className="form-control form-control-icon" value={confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)} placeholder="Xác nhận mật khẩu" required disabled={isLoading} />
          </div>
          <div className="d-grid mb-3">
            <button type="submit" className="btn btn-primary" disabled={isLoading}>
              {isLoading ? <FiLoader className="spinner" /> : 'Đăng ký'}
            </button>
          </div>
        </form>
        <p className="text-center mt-4">
          Đã có tài khoản? <a href="#" onClick={toggleMode}>Đăng nhập ngay</a>
        </p>
      </div>
    </div>
  );

  return (
    <div className="login-container">
      <div className="col-md-6 col-lg-4">
        {error && <div className="alert alert-danger mb-3">{error}</div>}
        {successMessage && <div className="alert alert-success mb-3">{successMessage}</div>}
        {isRegisterMode ? renderRegister() : renderLogin()}
      </div>
    </div>
  );
}

export default Login;
