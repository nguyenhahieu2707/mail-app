import { useEffect } from 'react';

function LoginTest() {
  useEffect(() => {
    const script = document.createElement('script');
    script.src = 'https://demo-sso.tinasoft.io/laoid.auth.js';
    script.async = true;

    script.onload = () => {
      console.log('✅ LaoID SDK loaded');
      console.log('📦 LaoIdSSO object:', window.LaoIdSSO);
    };

    script.onerror = () => {
      console.error('❌ Failed to load LaoID SDK');
    };

    document.body.appendChild(script);
  }, []);

  return (
    <div className="container mt-5">
      <h2>🧪 LaoID SDK Full Test</h2>
      <p>Nhấn nút bên dưới để kiểm tra popup LaoID.</p>

      <button id="laoid-signin" className="btn btn-outline-primary">
        Đăng nhập bằng LaoID
      </button>
    </div>
  );
}

export default LoginTest;
