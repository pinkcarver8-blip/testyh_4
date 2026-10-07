import { createContext, useContext, useEffect, useState } from "react";
import { api, getToken, setToken } from "./api";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [ready, setReady] = useState(() => !getToken());

  useEffect(() => {
    if (!getToken()) return;
    api("/api/auth/me")
      .then(setUser)
      .catch((e) => {
        if (e.status === 401) setToken(null);
      })
      .finally(() => setReady(true));
  }, []);

  const signIn = async (path, username, password) => {
    const res = await api(path, { method: "POST", body: { username, password } });
    setToken(res.token);
    setUser(res.user);
  };

  const value = {
    user,
    ready,
    login: (username, password) => signIn("/api/auth/login", username, password),
    signup: (username, password) => signIn("/api/auth/signup", username, password),
    logout: async () => {
      await api("/api/auth/logout", { method: "POST" }).catch(() => {});
      setToken(null);
      setUser(null);
    },
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
  return useContext(AuthContext);
}
