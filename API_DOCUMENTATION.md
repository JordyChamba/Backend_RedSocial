# 📡 SocialHub API - Documentación Completa de Endpoints

## Base URL
```
http://localhost:8080/api
```

## 🔐 Autenticación

Todos los endpoints protegidos requieren el header:
```
Authorization: Bearer {accessToken}
```

---

## 📋 Authentication Endpoints

### 1. Registro de Usuario
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "password123",
  "fullName": "John Doe"
}
```

**Respuesta (201):**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "accessToken": "eyJhbGci...",
    "refreshToken": "eyJhbGci...",
    "tokenType": "Bearer",
    "user": { ... }
  }
}
```

### 2. Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "usernameOrEmail": "johndoe",
  "password": "password123"
}
```

### 3. Refresh Token
```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGci..."
}
```

---

## 👤 User Endpoints

### 4. Obtener Usuario Actual
```http
GET /api/users/me
Authorization: Bearer {token}
```

### 5. Obtener Usuario por ID
```http
GET /api/users/{id}
```

### 6. Obtener Usuario por Username
```http
GET /api/users/username/{username}
```

### 7. Buscar Usuarios
```http
GET /api/users/search?query=john
```

### 8. Actualizar Perfil
```http
PUT /api/users/me
Authorization: Bearer {token}
Content-Type: application/json

{
  "fullName": "John Doe Updated",
  "bio": "Software Developer",
  "location": "San Francisco, CA",
  "website": "https://johndoe.com"
}
```

### 9. Actualizar Imagen de Perfil
```http
PUT /api/users/me/profile-image?imageUrl=https://example.com/image.jpg
Authorization: Bearer {token}
```

### 10. Actualizar Imagen de Portada
```http
PUT /api/users/me/cover-image?imageUrl=https://example.com/cover.jpg
Authorization: Bearer {token}
```

### 11. Cambiar Contraseña
```http
PUT /api/users/me/password
Authorization: Bearer {token}
Content-Type: application/json

{
  "currentPassword": "oldpassword",
  "newPassword": "newpassword123"
}
```

### 12. Seguir Usuario
```http
POST /api/users/{id}/follow
Authorization: Bearer {token}
```

### 13. Dejar de Seguir Usuario
```http
DELETE /api/users/{id}/unfollow
Authorization: Bearer {token}
```

### 14. Obtener Seguidores
```http
GET /api/users/{id}/followers
```

### 15. Obtener Seguidos
```http
GET /api/users/{id}/following
```

---

## 📝 Post Endpoints

### 16. Crear Post
```http
POST /api/posts
Authorization: Bearer {token}
Content-Type: application/json

{
  "content": "This is my first post!",
  "imageUrls": [
    "https://example.com/image1.jpg",
    "https://example.com/image2.jpg"
  ]
}
```

### 17. Obtener Post por ID
```http
GET /api/posts/{id}
```

### 18. Obtener Todos los Posts (Paginado)
```http
GET /api/posts?page=0&size=10
```

### 19. Obtener Posts de un Usuario
```http
GET /api/posts/user/{userId}?page=0&size=10
```

### 20. Obtener Feed Personalizado
```http
GET /api/posts/feed?page=0&size=10
Authorization: Bearer {token}
```
*Muestra posts de usuarios que sigues*

### 21. Obtener Posts Trending
```http
GET /api/posts/trending?limit=10
```
*Posts más populares de las últimas 24 horas*

### 22. Buscar Posts
```http
GET /api/posts/search?query=javascript&page=0&size=10
```

### 23. Actualizar Post
```http
PUT /api/posts/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "content": "Updated content"
}
```

### 24. Eliminar Post
```http
DELETE /api/posts/{id}
Authorization: Bearer {token}
```

### 25. Dar Like a Post
```http
POST /api/posts/{id}/like
Authorization: Bearer {token}
```

### 26. Quitar Like de Post
```http
DELETE /api/posts/{id}/unlike
Authorization: Bearer {token}
```

---

## 💬 Comment Endpoints

### 27. Crear Comentario
```http
POST /api/posts/{postId}/comments
Authorization: Bearer {token}
Content-Type: application/json

{
  "content": "Great post!",
  "parentCommentId": null  // Para reply, incluir ID del comentario padre
}
```

### 28. Obtener Comentarios de un Post
```http
GET /api/posts/{postId}/comments?page=0&size=10
```
*Solo comentarios de nivel superior (no replies)*

### 29. Obtener Replies de un Comentario
```http
GET /api/comments/{commentId}/replies
```

### 30. Actualizar Comentario
```http
PUT /api/comments/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "content": "Updated comment"
}
```

### 31. Eliminar Comentario
```http
DELETE /api/comments/{id}
Authorization: Bearer {token}
```

---

## 🔔 Notification Endpoints

### 32. Obtener Notificaciones
```http
GET /api/notifications?page=0&size=20
Authorization: Bearer {token}
```

### 33. Obtener Contador de No Leídas
```http
GET /api/notifications/unread-count
Authorization: Bearer {token}
```

### 34. Marcar Notificación como Leída
```http
PUT /api/notifications/{id}/read
Authorization: Bearer {token}
```

### 35. Marcar Todas como Leídas
```http
PUT /api/notifications/read-all
Authorization: Bearer {token}
```

### 36. Eliminar Notificación
```http
DELETE /api/notifications/{id}
Authorization: Bearer {token}
```

### 37. Eliminar Todas las Notificaciones
```http
DELETE /api/notifications
Authorization: Bearer {token}
```

---

## 🌐 WebSocket Endpoint

### Conectarse a WebSocket
```javascript
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    // Suscribirse a notificaciones del usuario
    stompClient.subscribe('/user/queue/notifications', function(message) {
        const notification = JSON.parse(message.body);
        console.log('Nueva notificación:', notification);
    });
});
```

**Tipos de Notificaciones:**
- `LIKE` - Alguien dio like a tu post
- `COMMENT` - Alguien comentó en tu post
- `REPLY` - Alguien respondió a tu comentario
- `FOLLOW` - Alguien te siguió
- `MENTION` - Alguien te mencionó (futuro)

---

## 📊 Response Format

### Success Response
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... },
  "timestamp": "2024-02-23T10:30:00"
}
```

### Error Response
```json
{
  "success": false,
  "message": "Error description",
  "data": null,
  "timestamp": "2024-02-23T10:30:00"
}
```

### Validation Error
```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "username": "Username is required",
    "email": "Email must be valid"
  },
  "timestamp": "2024-02-23T10:30:00"
}
```

---

## 🔍 Paginación

La mayoría de los endpoints que retornan listas soportan paginación:

**Parámetros:**
- `page` - Número de página (empezando en 0)
- `size` - Cantidad de elementos por página

**Respuesta:**
```json
{
  "success": true,
  "data": {
    "content": [ ... ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10
    },
    "totalPages": 5,
    "totalElements": 45,
    "last": false,
    "first": true
  }
}
```

---

## 🧪 Testing con cURL

### Ejemplo completo de flujo:

```bash
# 1. Registrar usuario
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "fullName": "Test User"
  }'

# 2. Guardar el token de la respuesta
TOKEN="eyJhbGciOiJIUzI1NiJ9..."

# 3. Crear un post
curl -X POST http://localhost:8080/api/posts \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "content": "My first post!"
  }'

# 4. Obtener feed
curl -X GET http://localhost:8080/api/posts/feed \
  -H "Authorization: Bearer $TOKEN"
```

---

## 📚 Más Información

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **API Docs:** http://localhost:8080/api-docs
- **README:** Ver archivo README.md
- **Testing Guide:** Ver archivo TESTING.md

---

**Total de Endpoints:** 37
**Endpoints Públicos:** 8
**Endpoints Protegidos:** 29
