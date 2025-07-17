import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

const Logout = () => {
  const navigate = useNavigate();

  useEffect(() => {
    const token = localStorage.getItem('accessToken'); // ✅ Dùng đúng key

    if (!token) {
      console.warn('⚠️ No access token found. Redirecting...');
      navigate('/');
      return;
    }

    // Gọi API logout
    fetch('auth/logout', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`, // ✅ Bắt buộc nếu backend xác thực
      },
      body: JSON.stringify({ token }),
    })
      .then((res) => {
        if (!res.ok) {
          console.error('❌ Logout request failed with status:', res.status);
        }
      })
      .catch((error) => {
        console.error('🚨 Logout error:', error);
      })
      .finally(() => {
        // ✅ Xoá token sau khi gọi API xong
        localStorage.removeItem('accessToken');
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
