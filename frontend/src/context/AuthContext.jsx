import { createContext, useContext, useEffect, useState, useCallback } from 'react'
import api, { setAuthToken } from '../api/client'

const AuthContext = createContext(null)
const TOKEN_KEY = 'gamestore_token'

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  // Al cargar la pagina, si hay un token guardado de una sesion anterior
  // lo mandamos a validar contra el backend (GET /api/auth/me). Si el
  // token no existe, expiro o es invalido, mostramos el login.
  const refresh = useCallback(async () => {
    const token = localStorage.getItem(TOKEN_KEY)
    if (!token) {
      setLoading(false)
      return
    }
    setAuthToken(token)
    try {
      const { data } = await api.get('/auth/me')
      setUser(data)
    } catch {
      localStorage.removeItem(TOKEN_KEY)
      setAuthToken(null)
      setUser(null)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    refresh()
  }, [refresh])

  const login = async (username, password) => {
    const { data } = await api.post('/auth/login', { username, password })
    localStorage.setItem(TOKEN_KEY, data.token)
    setAuthToken(data.token)
    setUser(data.usuario)
    return data.usuario
  }

  const logout = async () => {
    try {
      await api.post('/auth/logout')
    } finally {
      localStorage.removeItem(TOKEN_KEY)
      setAuthToken(null)
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
