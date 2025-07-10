import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
    plugins: [react()],
    server: {
        port: 80,
        host: true,
        proxy: {
            '/inbox': 'http://localhost:8080',
            '/sent': 'http://localhost:8080',
            '/email': 'http://localhost:8080',
            '/mail': 'http://localhost:8080'
        }
    }
});

// import { defineConfig } from 'vite';
// import react from '@vitejs/plugin-react';
// import rollupNodePolyFill from 'rollup-plugin-node-polyfills';

// export default defineConfig({
//   plugins: [react()],
//   resolve: {
//     alias: {
//       process: 'process/browser',
//       global: 'globalthis', // 👈 fix lỗi "global is not defined"
//     },
//   },
//   optimizeDeps: {
//     include: ['process'],
//   },
//   build: {
//     rollupOptions: {
//       plugins: [rollupNodePolyFill()],
//     },
//   },
//   server: {
//     port: 80,
//     host: true,
//     proxy: {
//       '/inbox': 'http://localhost:8080',
//       '/sent': 'http://localhost:8080',
//       '/email': 'http://localhost:8080',
//       '/mail': 'http://localhost:8080',
//     },
//   },
// });


