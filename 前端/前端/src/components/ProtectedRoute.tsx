import { useEffect, type ReactNode } from "react";
import { useNavigate } from "react-router";
import { useAuthStore } from "../stores/auth-store";

interface IProtectedRouteProps {
  children: ReactNode;
}

export default function ProtectedRoute({ children }: IProtectedRouteProps) {
  const navigate = useNavigate();

  useEffect(() => {
    (async () => {
      const ok = await useAuthStore.getState().initialize();
      if (!ok) {
        navigate("/login", { replace: true });
      }
    })();
  }, [navigate]);

  return <>{children}</>;
}
