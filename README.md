# DCava Backend

## Descripción
Backend del sistema DCava desarrollado con Spring Boot. Proporciona una API REST para la gestión de inventario, productos, categorías, anuncios y ventas.

El sistema está diseñado para integrarse con el frontend oficial de DCavalier, permitiendo la exposición pública de productos y la gestión interna mediante un único usuario administrador autenticado con Firebase.

> **Documentación completa de la API:** consulta [`docs/API.md`](docs/API.md) para la referencia detallada de todos los endpoints, objetos, autenticación y ejemplos.

## Características principales
- CRUD de productos, categorías y anuncios
- Gestión de ventas y cálculo de ganancias
- Control de stock con registro de cambios
- Autenticación mediante Firebase (ID Token)
- API REST separada en endpoints públicos y privados

## Stack
- Java 21
- Spring Boot
- MySQL 8
- Maven
- Nginx (Reverse Proxy)
- Firebase Authentication
- Docker / Docker Compose

## Arquitectura
Arquitectura en capas:
Controller → Service → Repository → Database

Autenticación:
Frontend obtiene token de Firebase → Backend valida token en cada request

## Ejecución con Docker (recomendado)

### Requisitos
- Docker Engine + Docker Compose (o Docker Desktop)

### Pasos
1. Configurar las credenciales en el archivo `.env` (Firebase, R2, base de datos).
2. Levantar la infraestructura:
```bash
docker compose up -d --build
```
- El contenedor `mysql` crea la base de datos y aplica el esquema inicial (`02-init.sql`) automáticamente la primera vez.
- El contenedor `backend` espera a que MySQL esté sano antes de arrancar.
3. Verificar el estado:
```bash
docker compose ps
docker compose logs -f backend
```
4. La API queda disponible en `http://localhost:8080` (o el puerto indicado por `APP_PORT` en el `.env`).
5. Documentación interactiva (Swagger UI): `http://localhost:8080/swagger-ui.html`
   - Esquema OpenAPI (JSON): `http://localhost:8080/v3/api-docs`
   - Referencia detallada en Markdown: [`docs/API.md`](docs/API.md)

### Persistencia de datos
Los datos se guardan en volúmenes con nombre, por lo que **sobreviven** a la destrucción o actualización de los contenedores:
- `dcava_mysql_data` → datos de MySQL (`/var/lib/mysql`)
- `dcava_uploads_data` → archivos subidos localmente (`/app/uploads`)

### Comandos útiles
```bash
docker compose down          # Detiene los contenedores (conserva los datos)
docker compose up -d --build # Reconstruye y vuelve a levantar (conserva los datos)
docker compose down -v       # ¡OJO! Elimina los contenedores Y los volúmenes (pierdes los datos)
```

### Acceso a MySQL desde el host
MySQL se publica en el puerto `3307` del host para no chocar con una instalación local en `3306`:
```bash
mysql -h 127.0.0.1 -P 3307 -u dcava_app -p
```
Si no necesitas este acceso, elimina la sección `ports` del servicio `mysql` en `docker-compose.yml`.

## Ejecución en local

### Requisitos
- Java 21
- Maven
- MySQL 8

### Pasos
1. Crear base de datos
2. Configurar variables de entorno
3. Crear ejecutable:
```bash
mvn -DskipTests clean package
```
-Resultado esperado: target/dcava-backend-{version}.jar
4. Ejecutar dcava-backend-{version}.jar