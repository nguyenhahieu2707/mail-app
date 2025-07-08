import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

const Logout = () => {
  const navigate = useNavigate();

  useEffect(() => {
    const token = localStorage.getItem('accessToken');
    console.log(token);

    if (!token) {
      // Nếu không có token thì logout luôn
      navigate('/');
      return;
    }

    // Gọi API logout
    fetch('http://localhost:8080/mailapp/auth/logout', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ token }), // LogoutRequest: chỉ cần token
    })
      .then((res) => {
        if (!res.ok) {
          throw new Error('Logout failed');
        }
      })
      .catch((error) => {
        console.error('Logout error:', error);
      })
      .finally(() => {
        // Xóa token và chuyển hướng
        localStorage.removeItem('access_token');
        navigate('/');
      });
  }, [navigate]);

  return (
    <div className="flex items-center justify-center min-h-screen bg-gray-100">
      <div className="text-center">
        <h2 className="text-xl font-semibold text-gray-800">Đang đăng xuất...</h2>
      </div>
    </div>
  );
};

export default Logout;
