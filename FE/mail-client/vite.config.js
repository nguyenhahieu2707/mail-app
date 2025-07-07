import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
    plugins: [react()],
    server: {
        proxy: {
            '/inbox': 'http://localhost:8080',
            '/sent': 'http://localhost:8080',
            '/email': 'http://localhost:8080',
            '/mail': 'http://localhost:8080'
        }
    }
});