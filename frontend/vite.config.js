export default {
  server: {
    port: 5173,
    host: '0.0.0.0',
    watch: {
      usePolling: true // Для Windows та Docker
    }
  },
  // Проксі для API запитів
  define: {
    'process.env.VITE_API_URL': JSON.stringify(process.env.VITE_API_URL || 'http://localhost:8081'),
    'process.env.VITE_KEYCLOAK_URL': JSON.stringify(process.env.VITE_KEYCLOAK_URL || 'http://localhost:8080')
  }
};