import { createContext, useContext, useEffect, useState, useCallback } from 'react'
import api, { login as apiLogin, logout as apiLogout, guardarTokens, limpiarTokens, hayTokenGuardado } from '../api/client'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  // Al cargar la pagina: si venimos de un login con Google, el backend
  // nos redirigio con los tokens en el fragmento de la URL (#token=...).
  // Los guardamos y limpiamos la URL antes de mostrar nada.
  const consumirCallbackDeGoogle = () => {
    if (!window.location.hash.includes('token=')) return false
    const params = new URLSearchParams(window.location.hash.replace(/^#\/oauth-callback\??/, ''))
    const token = params.get('token')
    const refreshToken = params.get('refreshToken')
    if (token && refreshToken) {
      guardarTokens(token, refreshToken)
      window.history.replaceState(null, '', window.location.pathname)
      return true
    }
    return false
  }

  const refresh = useCallback(async () => {
    consumirCallbackDeGoogle()
    if (!hayTokenGuardado()) {
      setLoading(false)
      return
    }
    try {
      const { data } = await api.get('/auth/me')
      setUser(data)
    } catch {
      limpiarTokens()
      setUser(null)
    } finally {
      setLoading(false)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  useEffect(() => {
    refresh()
  }, [refresh])

  const login = async (username, password, totpCode) => {
    const usuario = await apiLogin(username, password, totpCode)
    setUser(usuario)
    return usuario
  }

  const logout = async () => {
    try {
      await apiLogout()
    } finally {
      setUser(null)
    }
  }

  return (
    <AuthContext.Provider value={{ user, loading, login, logout, refresh }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}
