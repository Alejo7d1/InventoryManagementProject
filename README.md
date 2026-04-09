# DCava Backend

## Descripción
Backend del sistema DCava desarrollado con Spring Boot. Proporciona una API REST para la gestión de inventario, productos, categorías, anuncios y ventas.

El sistema está diseñado para integrarse con el frontend oficial de DCavalier, permitiendo la exposición pública de productos y la gestión interna mediante un único usuario administrador autenticado con Firebase.

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

## Arquitectura
Arquitectura en capas:
Controller → Service → Repository → Database

Autenticación:
Frontend obtiene token de Firebase → Backend valida token en cada request

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