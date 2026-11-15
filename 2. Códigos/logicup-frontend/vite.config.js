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
      // Sprint 10: Entrar com Google (ida e volta tratadas pelo Spring Security). Só '/login/oauth2':
      // '/login' sozinho é a tela de login do React.
      // changeOrigin: false MANTÉM o cabeçalho Host "localhost:5173". O Spring monta o endereço de volta
      // (redirect_uri) a partir dele, e esse endereço tem que ser igual ao cadastrado no Google.
      // (a forma curta '/x': 'http://...' do Vite troca o Host para localhost:8080)
      '/oauth2': { target: 'http://localhost:8080', changeOrigin: false },
      '/login/oauth2': { target: 'http://localhost:8080', changeOrigin: false },
    },
  },
})
