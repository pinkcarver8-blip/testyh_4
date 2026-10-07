import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../auth";

function AuthForm({ mode }) {
  const isLogin = mode === "login";
  const { login, signup } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const from = location.state?.from || "/";

  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setError("");
    try {
      await (isLogin ? login : signup)(username, password);
      navigate(from, { replace: true });
    } catch (err) {
      setError(err.message);
      setSubmitting(false);
    }
  };

  return (
    <section className="auth">
      <h1 className="page-title">{isLogin ? "로그인" : "회원가입"}</h1>
      <form className="card form" onSubmit={handleSubmit}>
        <label className="field">
          <span>아이디</span>
          <input value={username} onChange={(e) => setUsername(e.target.value)} maxLength={30} autoFocus />
        </label>
        <label className="field">
          <span>비밀번호</span>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder={isLogin ? "" : "4자 이상"}
          />
        </label>
        {error && <p className="error">{error}</p>}
        <button type="submit" className="btn btn-primary btn-block" disabled={submitting}>
          {isLogin ? "로그인" : "가입하기"}
        </button>
        <p className="muted center">
          {isLogin ? "계정이 없으신가요? " : "이미 계정이 있으신가요? "}
          <Link to={isLogin ? "/signup" : "/login"} state={{ from }}>
            {isLogin ? "회원가입" : "로그인"}
          </Link>
        </p>
      </form>
    </section>
  );
}

export default AuthForm;
