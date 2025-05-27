# Dockerfile для Vite frontend
FROM node:18-alpine

WORKDIR /app

# Копіюємо package.json та package-lock.json
COPY package*.json ./

# Встановлюємо залежності
RUN npm ci

# Копіюємо решту файлів
COPY . .

# Відкриваємо порт
EXPOSE 5173

# Запускаємо в режимі розробки з доступом ззовні
CMD ["npm", "run", "dev", "--", "--host", "0.0.0.0", "--port", "5173"]