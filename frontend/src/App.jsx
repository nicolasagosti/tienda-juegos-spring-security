import { useState, useCallback } from 'react'
import { AuthProvider, useAuth } from './context/AuthContext'
import Navbar from './components/Navbar'
import Toast from './components/Toast'
import LoginPage from './components/LoginPage'
import CatalogPage from './components/CatalogPage'
import GameFormPage from './components/GameFormPage'
import AdminDashboard from './components/AdminDashboard'
import AdminUsersPage from './components/AdminUsersPage'
import UserFormPage from './components/UserFormPage'
import AdminSectionsPage from './components/AdminSectionsPage'
import SecurityPage from './components/SecurityPage'

// Navegacion simple basada en estado (sin react-router): esta SPA vive
// entera en "/" para no pisarse con las rutas del backend (que ademas
// sigue sirviendo la version clasica en Thymeleaf en /juegos, /admin, etc).
function Shell() {
  const { user, loading } = useAuth()
  const [view, setView] = useState({ name: 'catalog', params: {} })
  const [toast, setToast] = useState(null)

  const notify = useCallback((type, message) => {
    setToast({ type, message, key: Date.now() })
  }, [])

  const navigate = useCallback((name, params = {}) => {
    setView({ name, params })
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }, [])

  if (loading) {
    return (
      <div className="loading-screen">
        <div className="spinner" />
      </div>
    )
  }

  if (!user) {
    return <LoginPage onLoggedIn={() => navigate('catalog')} />
  }

  return (
    <div className="app-shell">
      <Navbar user={user} navigate={navigate} activeView={view.name} notify={notify} />
      <Toast toast={toast} onDone={() => setToast(null)} />
      <main className="container">
        {view.name === 'catalog' && (
          <CatalogPage user={user} navigate={navigate} notify={notify} />
        )}
        {view.name === 'gameForm' && (
          <GameFormPage gameId={view.params.id} navigate={navigate} notify={notify} />
        )}
        {view.name === 'adminDashboard' && <AdminDashboard navigate={navigate} />}
        {view.name === 'adminUsers' && (
          <AdminUsersPage navigate={navigate} notify={notify} />
        )}
        {view.name === 'userForm' && (
          <UserFormPage userId={view.params.id} navigate={navigate} notify={notify} />
        )}
        {view.name === 'adminSections' && <AdminSectionsPage notify={notify} />}
        {view.name === 'security' && <SecurityPage notify={notify} />}
      </main>
    </div>
  )
}

export default function App() {
  return (
    <AuthProvider>
      <Shell />
    </AuthProvider>
  )
}
