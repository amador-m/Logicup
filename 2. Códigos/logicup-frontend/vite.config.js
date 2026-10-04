import { defineConfig } from 'vite'
import react, { reactCompilerPreset } from '@vitejs/plugin-react'
import babel from '@rolldown/plugin-babel'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    react(),
    babel({ presets: [reactCompilerPreset()] })
  ],
  server: {
    // PE3: em desenvolvimento, o React chama "/api/..." no PRÓPRIO endereço (localhost:5173)
    // e o Vite repassa para o Spring Boot (localhost:8080). Para o navegador é tudo o mesmo site,
    // então o cookie httpOnly funciona sem CORS.
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
