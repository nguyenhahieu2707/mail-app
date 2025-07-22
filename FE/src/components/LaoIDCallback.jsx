import { useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";

export default function LaoIDCallback() {
  const navigate = useNavigate();
  const didRun = useRef(false); // ✅ flag tránh gọi 2 lần

  useEffect(() => {
    if (didRun.current) return;
    didRun.current = true;

    const params = new URLSearchParams(window.location.search);
    const code = params.get("authorization_code");

    if (code) {
      console.log("🔐 Mã authorization_code:", code);

      fetch("/laoid", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ code }),
      })
        .then(res => res.json())
        .then(data => {
          const token = data?.result?.token;
          const refreshToken = data?.result?.refreshToken;

          if (token) {
            localStorage.setItem("accessToken", token);
            if (refreshToken) {
              localStorage.setItem("refreshToken", refreshToken);
            }
            navigate("/inbox");
          } else {
            console.error("❌ Không tìm thấy token trong phản hồi:", data);
          }
        })
        .catch(err => {
          console.error("❌ Lỗi khi gọi API /laoid:", err);
        });
    } else {
      console.error("❌ Không tìm thấy authorization_code trên URL");
    }
  }, [navigate]);

  return <div>🔄 Đang xử lý đăng nhập LaoID...</div>;
}
