import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'node:path'

// Dos formas de compilar este frontend:
//
//  - "embebido" (modo por defecto, uso local): el resultado se copia
//    directo adentro del backend Spring Boot
//    (src/main/resources/static), para correr todo en un solo proceso y
//    un solo puerto, sin CORS de por medio.
//
//  - "standalone" (VITE_STANDALONE_BUILD=true, el que usa Vercel): el
//    resultado queda en ./dist, como cualquier proyecto Vite comun, listo
//    para desplegarse separado del backend. En ese modo el frontend le
//    habla a la API por HTTP a otro dominio (ver VITE_API_BASE_URL en
//    src/api/client.js) usando JWT en vez de cookies de sesion.
const standalone = process.env.VITE_STANDALONE_BUILD === 'true'

export default defineConfig({
  plugins: [react()],
  build: {
    outDir: standalone ? 'dist' : path.resolve(import.meta.dirname, '../src/main/resources/static'),
    emptyOutDir: true,
  },
  server: {
    // Solo se usa corriendo "npm run dev" contra el backend ya levantado en otro puerto.
    proxy: {
      '/api': 'http://localhost:8081',
      '/uploads': 'http://localhost:8081',
    },
  },
})
