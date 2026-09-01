import axios from 'axios'

// VITE_API_BASE_URL permite apuntar a un backend en otro dominio (por
// ejemplo, cuando el frontend esta en Vercel y el backend en
// Render/Railway/etc). Sin esa variable de entorno, usa "/api" relativo:
// es lo que corresponde cuando el React esta embebido dentro del mismo
// proceso Spring Boot (mismo origen).
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
})

/**
 * La API ya no usa cookies de sesion (ver JwtAuthenticationFilter en el
 * backend): el login devuelve un JWT que guardamos y reenviamos nosotros
 * mismos en cada pedido como header "Authorization: Bearer ...". Esto es
 * lo que hace que la app funcione igual de bien si el frontend y el
 * backend estan en el mismo origen o en dominios completamente distintos.
 */
export function setAuthToken(token) {
  if (token) {
    api.defaults.headers.common.Authorization = `Bearer ${token}`
  } else {
    delete api.defaults.headers.common.Authorization
  }
}

export default api
