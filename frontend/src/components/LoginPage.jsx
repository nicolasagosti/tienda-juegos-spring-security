import { useState } from 'react'
import { useAuth } from '../context/AuthContext'

const DEMO_USERS = [
  { username: 'admin', password: 'admin123', role: 'ADMIN', badgeClass: 'badge-admin' },
  { username: 'vendedor1', password: 'vendedor123', role: 'VENDEDOR', badgeClass: 'badge-vendedor' },
  { username: 'vendedor2', password: 'vendedor123', role: 'VENDEDOR', badgeClass: 'badge-vendedor' },
  { username: 'comprador1', password: 'comprador123', role: 'COMPRADOR', badgeClass: 'badge-comprador' },
]

export default function LoginPage({ onLoggedIn }) {
  const { login } = useAuth()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await login(username, password)
      onLoggedIn()
    } catch (err) {
      setError(err.response?.data?.mensaje || 'Usuario o contraseña incorrectos')
    } finally {
      setLoading(false)
    }
  }

  const quickFill = (u, p) => {
    setUsername(u)
    setPassword(p)
    setError('')
  }

  return (
    <div className="login-body">
      <div className="login-card">
        <h1>GameStore</h1>
        <p className="subtitle">Demo de Spring Security + React &mdash; login con 3 roles</p>

        {error && <div className="alert alert-error">{error}</div>}

        <form onSubmit={handleSubmit} className="login-form">
          <label htmlFor="username">Usuario</label>
          <input
            id="username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            autoFocus
            required
          />

          <label htmlFor="password">Contraseña</label>
          <input
            id="password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />

          <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
            {loading ? 'Ingresando...' : 'Ingresar'}
          </button>
        </form>

        <div className="demo-users">
          <h3>Usuarios de prueba (click para autocompletar)</h3>
          <ul>
            {DEMO_USERS.map((u) => (
              <li key={u.username} onClick={() => quickFill(u.username, u.password)}>
                <strong>{u.username}</strong> / {u.password}
                <span className={`badge ${u.badgeClass}`}>{u.role}</span>
              </li>
            ))}
          </ul>
        </div>
      </div>
    </div>
  )
}
