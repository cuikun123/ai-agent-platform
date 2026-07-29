import { Routes, Route, Navigate } from 'react-router-dom'
import AuthGuard from './components/AuthGuard'
import MainLayout from './components/MainLayout'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/register/RegisterPage'
import HomePage from './pages/HomePage'

function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route
        path="/"
        element={
          <AuthGuard>
            <MainLayout />
          </AuthGuard>
        }
      >
        <Route index element={<HomePage />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

export default App
