import { useEffect } from "react";
import { useNavigate } from "react-router-dom";

export default function LaoIDCallback() {
  const navigate = useNavigate();

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const code = params.get("authorization_code");

    if (code) {
      fetch("http://localhost:8080/api/auth/laoid", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ code }),
      })
        .then(res => res.json())
        .then(data => {
          localStorage.setItem("access_token", data.token);
          navigate("/inbox");
        });
    }
  }, []);

  return <div>Đang xử lý đăng nhập LaoID...</div>;
}
