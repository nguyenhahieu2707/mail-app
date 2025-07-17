// import { defineConfig } from 'vite';
// import react from '@vitejs/plugin-react';

// export default defineConfig({
//     plugins: [react()],
//     server: {
//         port: 80,
//         host: true,
//         proxy: {
//             '/inbox': 'http://localhost:8080',
//             '/sent': 'http://localhost:8080',
//             '/email': 'http://localhost:8080',
//             '/mail': 'http://localhost:8080'
//         }
//     }
// });


import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Check if in development mode
const isDev = process.env.NODE_ENV !== 'production'

export default defineConfig({
  plugins: [react()],
  server: isDev ? {
    port: 80,
    host: true,
    proxy: {
      '/inbox': 'http://localhost:8080',
      '/sent': 'http://localhost:8080',
      '/email': 'http://localhost:8080',
      '/mail': 'http://localhost:8080',
      '/auth': 'http://localhost:8080',
      '/laoid': 'http://localhost:8080',
      '/ws-mail': {
        target: 'ws://localhost:8080',
        ws: true,
      }
    }
  } : undefined
})
