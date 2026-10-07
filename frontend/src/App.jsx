import { useEffect, useState } from "react";

const API_URL = import.meta.env.VITE_API_URL || "http://localhost:8080";

function App() {
  const [message, setMessage] = useState("불러오는 중...");

  useEffect(() => {
    fetch(`${API_URL}/api/hello`)
      .then((res) => res.text())
      .then((data) => setMessage(data))
      .catch(() => setMessage("서버 연결 실패 😢"));
  }, []);

  return <h1>{message}</h1>;
}

export default App;
