# University API

API REST institucional simulada para UniVote. Representa un sistema académico externo que posee el directorio oficial de estudiantes. UniVote no accede directamente a esa base de datos: valida al estudiante mediante HTTP/JSON.

## Requisitos

- Node.js 24 o superior.
- MySQL 8.
- Base `universidad_institucional` cargada desde `../database/institutional/university_directory.sql`.

## Configuración

```bash
cd university-api
cp .env.example .env
```

Edite únicamente `.env` con el usuario técnico local de MySQL. El archivo está ignorado por Git.

## Instalación y ejecución

```bash
npm install
npm start
```

La API escucha por defecto en `http://127.0.0.1:8081`.

## Endpoints

### Salud

```http
GET /api/health
```

### Validar estudiante

```http
POST /api/students/validate
Content-Type: application/json
```

```json
{
  "studentCode": "20260001",
  "document": "1001001001",
  "institutionalEmail": "ana.torres@universidad.test"
}
```

Cuando los tres datos coinciden, el estudiante está activo y está habilitado para votar, la API devuelve la identidad institucional necesaria para el registro. Los fallos de elegibilidad usan un mensaje genérico para no exponer el directorio académico.
