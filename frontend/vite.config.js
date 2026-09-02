import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'node:path'

// Dos formas de compilar este frontend:
//
//  - "embebido" (modo por defecto, uso local): el resultado se copia
//    adentro del api-gateway (services/api-gateway/src/main/resources/static),
//    que lo sirve en "/" y rutea /api/** a los microservicios. Un solo
//    puerto (8080) para todo.
//
//  - "standalone" (VITE_STANDALONE_BUILD=true, el que usa Vercel): el
//    resultado queda en ./dist, listo para desplegarse separado. En ese
//    modo el frontend le habla al gateway por HTTP a otro dominio (ver
//    VITE_API_BASE_URL en src/api/client.js) con JWT.
const standalone = process.env.VITE_STANDALONE_BUILD === 'true'

const gatewayStatic = path.resolve(
  import.meta.dirname,
  '../services/api-gateway/src/main/resources/static',
)

export default defineConfig({
  plugins: [react()],
  build: {
    outDir: standalone ? 'dist' : gatewayStatic,
    emptyOutDir: true,
  },
  server: {
    // Solo se usa corriendo "npm run dev": pega contra el gateway (:8080).
    proxy: {
      '/api': 'http://localhost:8080',
      '/uploads': 'http://localhost:8080',
    },
  },
})
