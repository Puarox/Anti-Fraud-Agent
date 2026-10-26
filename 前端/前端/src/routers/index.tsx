import { lazy, Suspense, type ReactNode } from "react";
import { createBrowserRouter, Navigate } from "react-router";
import Layout from "../components/Layout";
import Loading from "../components/Loading";
import ProtectedRoute from "../components/ProtectedRoute";
import DashboardPage from "../pages/DashboardPage";
import SettingsPage from "../pages/SettingsPage";

const LoginPage = lazy(() => import("../pages/LoginPage"));
const RegisterPage = lazy(() => import("../pages/RegisterPage"));
const CaseManagementPage = lazy(() => import("../pages/CaseManagementPage"));
const UserManagementPage = lazy(() => import("../pages/UserManagementPage"));
const NotFoundPage = lazy(() => import("../pages/NotFoundPage"));
const AutoEvolutionPage = lazy(() => import("../pages/AutoEvolutionPage"));

const withSuspense = (children: ReactNode) => <Suspense fallback={<Loading />}>{children}</Suspense>;

const router = createBrowserRouter([
  {
    path: "login",
    element: withSuspense(<LoginPage />),
  },
  {
    path: "register",
    element: withSuspense(<RegisterPage />),
  },
  {
    path: "/",
    element: (
      <ProtectedRoute>
        <Layout />
      </ProtectedRoute>
    ),
    children: [
      {
        index: true,
        element: <Navigate to="/caseManagement" replace={true} />,
      },
      {
        path: "caseManagement",
        element: withSuspense(<CaseManagementPage />),
      },
      {
        path: "userManagement",
        element: withSuspense(<UserManagementPage />),
      },
      {
        path: "/autoEvolution",
        element: <AutoEvolutionPage />, // 你需要创建这个组件
      },
      {
        path: "/dashboard",
        element: <DashboardPage />, // 你需要创建这个组件
      },
       {
        path: "/settingsPage",
        element: <SettingsPage />, // 你需要创建这个组件
      }
    ],
  },
  {
    path: "*",
    element: withSuspense(<NotFoundPage />),
  },
]);

export default router;
