import axios from 'axios'

// VITE_API_BASE_URL permite apuntar a un backend en otro dominio (por
// ejemplo, cuando el frontend esta en Vercel y el backend en
// Render/Railway/etc). Sin esa variable de entorno, usa "/api" relativo:
// es lo que corresponde cuando el React esta embebido dentro del mismo
// proceso Spring Boot (mismo origen).
const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || '/api'

const api = axios.create({
  baseURL: apiBaseUrl,
})

// Origen del backend sin el "/api" final, por ejemplo
// "https://tienda-juegos-backend.onrender.com". En modo embebido
// (apiBaseUrl = "/api") queda en "" a proposito. Se exporta porque
// tambien lo necesita el boton "Continuar con Google" (redirige de
// entrada a esa URL, no es un pedido AJAX).
export const backendOrigin = apiBaseUrl.replace(/\/api\/?$/, '')

/**
 * Las imagenes de los juegos (juego.imagenUrl) vienen del backend como
 * rutas relativas, ej "/uploads/xxx.png" -- el backend las sirve el
 * mismo (ver WebConfig). Eso alcanza cuando frontend y backend comparten
 * origen (modo embebido), pero si estan en dominios distintos (Vercel +
 * Render) el navegador intentaria cargarlas desde el dominio de Vercel,
 * donde no existen. Esta funcion arma la URL completa hacia el backend
 * cuando corresponde.
 */
export function resolveImageUrl(path) {
  if (!path) return null
  if (/^https?:\/\//.test(path)) return path
  return `${backendOrigin}${path}`
}

// ---------- Tokens (access + refresh) ----------
const TOKEN_KEY = 'gamestore_token'
const REFRESH_KEY = 'gamestore_refresh_token'

function setAuthToken(token) {
  if (token) {
    api.defaults.headers.common.Authorization = `Bearer ${token}`
  } else {
    delete api.defaults.headers.common.Authorization
  }
}

export function guardarTokens(token, refreshToken) {
  localStorage.setItem(TOKEN_KEY, token)
  localStorage.setItem(REFRESH_KEY, refreshToken)
  setAuthToken(token)
}

export function limpiarTokens() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(REFRESH_KEY)
  setAuthToken(null)
}

export function hayTokenGuardado() {
  return !!localStorage.getItem(TOKEN_KEY)
}

// Si ya habia un token de una sesion anterior (recarga de pagina), lo
// cargamos ya mismo para que el primer GET /auth/me salga con el header.
if (hayTokenGuardado()) {
  setAuthToken(localStorage.getItem(TOKEN_KEY))
}

export async function login(username, password, totpCode) {
  const { data } = await api.post('/auth/login', { username, password, totpCode })
  guardarTokens(data.token, data.refreshToken)
  return data.usuario
}

export async function logout() {
  const refreshToken = localStorage.getItem(REFRESH_KEY)
  try {
    await api.post('/auth/logout', { refreshToken })
  } finally {
    limpiarTokens()
  }
}

/**
 * Refresco automatico y transparente del access token.
 *
 * El access token dura poco (15 min) a proposito. En vez de que el
 * usuario tenga que volver a loguearse cada 15 minutos, este interceptor
 * agarra cualquier 401 ("el access token vencio"), pide uno nuevo con el
 * refresh token guardado, y reintenta el pedido original -- todo antes
 * de que el componente que hizo el pedido se entere de que hubo un 401.
 * Si el refresh token TAMBIEN esta vencido/revocado, ahi si dejamos que
 * el 401 se propague (AuthContext lo interpreta como "hay que loguearse
 * de nuevo").
 */
let refrescoEnCurso = null

function refrescarToken() {
  if (!refrescoEnCurso) {
    const refreshToken = localStorage.getItem(REFRESH_KEY)
    if (!refreshToken) {
      return Promise.resolve(null)
    }
    refrescoEnCurso = axios
      .post(`${apiBaseUrl}/auth/refresh`, { refreshToken })
      .then(({ data }) => {
        guardarTokens(data.token, data.refreshToken)
        return data.token
      })
      .catch(() => {
        limpiarTokens()
        return null
      })
      .finally(() => {
        refrescoEnCurso = null
      })
  }
  return refrescoEnCurso
}

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const original = error.config
    const esLoginORefresh = original?.url?.includes('/auth/login') || original?.url?.includes('/auth/refresh')

    if (error.response?.status === 401 && !original._reintentado && !esLoginORefresh) {
      original._reintentado = true
      const nuevoToken = await refrescarToken()
      if (nuevoToken) {
        original.headers.Authorization = `Bearer ${nuevoToken}`
        return api(original)
      }
    }
    return Promise.reject(error)
  }
)

export default api
