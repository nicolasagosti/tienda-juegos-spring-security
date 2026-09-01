import { useAuth } from '../context/AuthContext'

const ROLE_CLASS = { ADMIN: 'badge-admin', VENDEDOR: 'badge-vendedor', COMPRADOR: 'badge-comprador' }

export default function Navbar({ user, navigate, activeView, notify }) {
  const { logout } = useAuth()

  const handleLogout = async () => {
    await logout()
    notify('success', 'Sesion cerrada correctamente')
  }

  const linkClass = (name) => `link-btn${activeView === name ? ' link-btn-active' : ''}`

  return (
    <nav className="navbar">
      <button className="navbar-brand" onClick={() => navigate('catalog')}>
        GameStore
      </button>
      <div className="navbar-links">
        <button className={linkClass('catalog')} onClick={() => navigate('catalog')}>
          Catalogo
        </button>
        {(user.rol === 'VENDEDOR' || user.rol === 'ADMIN') && (
          <button className={linkClass('gameForm')} onClick={() => navigate('gameForm')}>
            Publicar juego
          </button>
        )}
        {user.rol === 'ADMIN' && (
          <>
            <button className={linkClass('adminDashboard')} onClick={() => navigate('adminDashboard')}>
              Panel admin
            </button>
            <button className={linkClass('adminUsers')} onClick={() => navigate('adminUsers')}>
              Usuarios
            </button>
            <button className={linkClass('adminSections')} onClick={() => navigate('adminSections')}>
              Secciones
            </button>
          </>
        )}
      </div>
      <div className="navbar-user">
        <span className="username">{user.nombreCompleto}</span>
        <span className={`badge ${ROLE_CLASS[user.rol]}`}>{user.rol}</span>
        <button className="btn-logout" onClick={handleLogout}>
          Salir
        </button>
      </div>
    </nav>
  )
}
