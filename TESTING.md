# 🧪 Testing Authentication

## Prerequisitos

1. PostgreSQL corriendo en `localhost:5432`
2. Base de datos `socialhub_db` creada
3. Java 21 instalado
4. Maven instalado

## 🚀 Iniciar la aplicación

```bash
cd backend
mvn spring-boot:run
```

La aplicación estará disponible en: `http://localhost:8080`

## 📡 Probar Endpoints con cURL

### 1. Registro de Usuario

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com",
    "password": "password123",
    "fullName": "John Doe"
  }'
```

**Respuesta esperada:**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "user": {
      "id": 1,
      "username": "johndoe",
      "email": "john@example.com",
      "fullName": "John Doe",
      ...
    }
  }
}
```

### 2. Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "johndoe",
    "password": "password123"
  }'
```

### 3. Refresh Token

```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "tu_refresh_token_aqui"
  }'
```

## 📝 Probar con Postman

1. **Importar colección** (crear archivo `SocialHub.postman_collection.json`)
2. **Crear variable de entorno** `{{baseUrl}}` = `http://localhost:8080`
3. **Crear variable** `{{accessToken}}` para guardar el token

### Ejemplo de request autenticado:

```
GET http://localhost:8080/api/users/me
Headers:
  Authorization: Bearer {{accessToken}}
```

## 🔍 Verificar Swagger UI

Abrir en el navegador: `http://localhost:8080/swagger-ui.html`

Aquí puedes probar todos los endpoints de forma interactiva.

## 🐛 Troubleshooting

### Error: "Port 8080 is already in use"
```bash
# Linux/Mac
lsof -ti:8080 | xargs kill -9

# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

### Error: "Connection to database failed"
```bash
# Verificar que PostgreSQL esté corriendo
sudo service postgresql status

# Crear base de datos
psql -U postgres
CREATE DATABASE socialhub_db;
\q
```

### Error: "Table doesn't exist"
- Asegúrate de que `spring.jpa.hibernate.ddl-auto=update` esté en `application.properties`
- Las tablas se crean automáticamente al iniciar la aplicación

## 📊 Verificar datos en la base de datos

```bash
psql -U postgres -d socialhub_db

-- Ver usuarios
SELECT id, username, email, created_at FROM users;

-- Ver posts
SELECT id, content, author_id, created_at FROM posts;

-- Salir
\q
```
