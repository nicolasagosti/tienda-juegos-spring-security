import { useState } from 'react'
import { useAuth } from '../context/AuthContext'
import { backendOrigin } from '../api/client'

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
  const [totpCode, setTotpCode] = useState('')
  const [requiere2fa, setRequiere2fa] = useState(false)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await login(username, password, requiere2fa ? totpCode : undefined)
      onLoggedIn()
    } catch (err) {
      const data = err.response?.data
      if (data?.requiere2fa) {
        setRequiere2fa(true)
        setError(data.mensaje)
      } else {
        setError(data?.mensaje || 'Usuario o contraseña incorrectos')
      }
    } finally {
      setLoading(false)
    }
  }

  const quickFill = (u, p) => {
    setUsername(u)
    setPassword(p)
    setError('')
    setRequiere2fa(false)
  }

  const continuarConGoogle = () => {
    window.location.href = `${backendOrigin}/oauth2/authorization/google`
  }

  return (
    <div className="login-body">
      <div className="login-card">
        <h1>GameStore</h1>
        <p className="subtitle">Demo de Spring Security + React &mdash; login con 3 roles</p>

        {error && <div className="alert alert-error">{error}</div>}

        {!requiere2fa && (
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
        )}

        {requiere2fa && (
          <form onSubmit={handleSubmit} className="login-form">
            <label htmlFor="totpCode">Codigo de tu app de autenticacion</label>
            <input
              id="totpCode"
              value={totpCode}
              onChange={(e) => setTotpCode(e.target.value)}
              inputMode="numeric"
              maxLength={6}
              placeholder="000000"
              autoFocus
              required
            />
            <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
              {loading ? 'Verificando...' : 'Verificar'}
            </button>
            <button
              type="button"
              className="btn btn-secondary btn-block"
              onClick={() => {
                setRequiere2fa(false)
                setTotpCode('')
                setError('')
              }}
            >
              Volver
            </button>
          </form>
        )}

        {!requiere2fa && (
          <>
            <div className="login-divider">o</div>
            <button type="button" className="btn btn-google btn-block" onClick={continuarConGoogle}>
              Continuar con Google
            </button>
          </>
        )}

        {!requiere2fa && (
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
        )}
      </div>
    </div>
  )
}
