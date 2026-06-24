# Perfulandia Soporte Service

Microservicio de soporte técnico para el sistema **Perfulandia FullStack**.
Este servicio permite gestionar tickets de soporte asociados a usuarios, registrar mensajes dentro de cada ticket, asignar responsables y controlar el estado de atención.

---

## 1. Descripción general

El microservicio **Soporte Service** forma parte de la arquitectura de microservicios de Perfulandia. Su responsabilidad principal es administrar la comunicación entre clientes y personal de soporte mediante tickets.

Este servicio no almacena información completa del usuario. Solo utiliza identificadores como `idUsuario` e `idUsuarioAsignado`, manteniendo una baja dependencia con el `usuario-service`.

---

## 2. Tecnologías utilizadas

* Java 25
* Spring Boot
* Spring WebMVC
* Spring Data JPA
* Spring Validation
* Spring HATEOAS
* MySQL / MariaDB
* H2 Database para pruebas
* Lombok
* Maven
* JUnit 5
* Mockito
* MockMvc
* JaCoCo

---

## 3. Puerto del microservicio

El servicio se ejecuta por defecto en el puerto:

```properties
server.port=8086
```

URL base local:

```txt
http://localhost:8086
```

---

## 4. Base de datos

Para ejecución local se utiliza MySQL/MariaDB.

Nombre de la base de datos:

```sql
perfulandia_soporte_db
```

Configuración principal:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/perfulandia_soporte_db
spring.datasource.username=root
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
```

Para pruebas automatizadas se utiliza H2 en memoria mediante el perfil `test`.

---

## 5. Estructura del proyecto

```txt
src/main/java/com/perfulandia/soporte
├── controller
├── dto
├── exception
├── model
├── repository
├── service
└── SoporteSeviceApplication.java
```

### Descripción de capas

| Capa       | Responsabilidad                                        |
| ---------- | ------------------------------------------------------ |
| controller | Expone los endpoints REST y respuestas HATEOAS.        |
| service    | Contiene la lógica de negocio del microservicio.       |
| repository | Acceso a datos mediante Spring Data JPA.               |
| model      | Entidades principales del dominio.                     |
| dto        | Objetos de transferencia para solicitudes específicas. |
| exception  | Manejo centralizado de errores.                        |

---

## 6. Modelo principal

### TicketSoporte

Representa un ticket de soporte creado por un usuario.

Campos principales:

```txt
idTicket
idUsuario
asunto
descripcion
prioridad
estado
fechaCreacion
fechaActualizacion
fechaCierre
idUsuarioAsignado
mensajes
```

### MensajeTicket

Representa un mensaje asociado a un ticket.

Campos principales:

```txt
idMensaje
ticket
idUsuario
mensaje
fechaEnvio
tipoAutor
```

---

## 7. Estados de ticket

```txt
ABIERTO
EN_REVISION
RESPONDIDO
CERRADO
CANCELADO
```

---

## 8. Prioridades de ticket

```txt
BAJA
MEDIA
ALTA
URGENTE
```

---

## 9. Tipos de autor de mensaje

```txt
CLIENTE
SOPORTE
ADMIN
```

---

## 10. Reglas de negocio

El microservicio aplica las siguientes reglas:

* Todo ticket nuevo se crea con estado `ABIERTO`.
* Si no se define prioridad, se asigna prioridad `MEDIA`.
* Un ticket `CERRADO` no puede ser actualizado.
* Un ticket `CANCELADO` no puede ser actualizado.
* No se puede cambiar el estado de un ticket cerrado o cancelado.
* No se puede asignar responsable a un ticket cerrado o cancelado.
* No se pueden agregar mensajes a tickets cerrados o cancelados.
* Al cerrar o cancelar un ticket se registra `fechaCierre`.
* Al asignar responsable, el ticket pasa a estado `EN_REVISION`.
* Al agregar un mensaje válido, el ticket pasa a estado `RESPONDIDO`.

---

## 11. Endpoints principales

### Tickets

| Método | Endpoint                                    | Descripción                               |
| ------ | ------------------------------------------- | ----------------------------------------- |
| GET    | `/api/tickets`                              | Lista todos los tickets.                  |
| GET    | `/api/tickets/{idTicket}`                   | Busca un ticket por ID.                   |
| GET    | `/api/tickets/usuario/{idUsuario}`          | Lista tickets creados por un usuario.     |
| GET    | `/api/tickets/asignado/{idUsuarioAsignado}` | Lista tickets asignados a un responsable. |
| POST   | `/api/tickets`                              | Crea un nuevo ticket.                     |
| PUT    | `/api/tickets/{idTicket}`                   | Actualiza un ticket existente.            |
| PATCH  | `/api/tickets/{idTicket}/estado`            | Cambia el estado de un ticket.            |
| PATCH  | `/api/tickets/{idTicket}/asignar`           | Asigna un responsable al ticket.          |
| PATCH  | `/api/tickets/{idTicket}/cerrar`            | Cierra un ticket.                         |
| PATCH  | `/api/tickets/{idTicket}/cancelar`          | Cancela un ticket.                        |
| DELETE | `/api/tickets/{idTicket}`                   | Elimina un ticket.                        |

### Mensajes

| Método | Endpoint                                       | Descripción                      |
| ------ | ---------------------------------------------- | -------------------------------- |
| GET    | `/api/tickets/{idTicket}/mensajes`             | Lista los mensajes de un ticket. |
| POST   | `/api/tickets/{idTicket}/mensajes`             | Agrega un mensaje a un ticket.   |
| GET    | `/api/tickets/{idTicket}/mensajes/{idMensaje}` | Busca un mensaje por ID.         |
| DELETE | `/api/tickets/{idTicket}/mensajes/{idMensaje}` | Elimina un mensaje.              |

---

## 12. Ejemplos de uso

### Crear ticket

```http
POST /api/tickets
Content-Type: application/json
```

```json
{
  "idUsuario": 1,
  "asunto": "Problema con mi pedido",
  "descripcion": "Mi pedido aparece como entregado, pero no lo recibí.",
  "prioridad": "ALTA"
}
```

---

### Asignar responsable

```http
PATCH /api/tickets/1/asignar
Content-Type: application/json
```

```json
{
  "idUsuarioAsignado": 2
}
```

---

### Cambiar estado

```http
PATCH /api/tickets/1/estado
Content-Type: application/json
```

```json
{
  "estado": "EN_REVISION"
}
```

---

### Agregar mensaje

```http
POST /api/tickets/1/mensajes
Content-Type: application/json
```

```json
{
  "idUsuario": 1,
  "mensaje": "Hola, necesito ayuda con este problema.",
  "tipoAutor": "CLIENTE"
}
```

---

## 13. Manejo de errores

El microservicio cuenta con manejo global de excepciones mediante `GlobalExceptionHandler`.

Respuestas controladas:

| Error                       | Código HTTP |
| --------------------------- | ----------- |
| Recurso no encontrado       | 404         |
| Regla de negocio incumplida | 400         |
| Error de validación         | 400         |
| Error interno del servidor  | 500         |

Ejemplo de error:

```json
{
  "timestamp": "2026-06-24T10:30:00",
  "status": 404,
  "error": "Recurso no encontrado",
  "mensaje": "Ticket no encontrado con ID: 99",
  "path": "/api/tickets/99"
}
```

---

## 14. Ejecutar el proyecto

Desde la raíz del proyecto:

```powershell
.\mvnw.cmd spring-boot:run
```

O también:

```powershell
.\mvnw.cmd clean install
.\mvnw.cmd spring-boot:run
```

---

## 15. Ejecutar pruebas

Para ejecutar todas las pruebas:

```powershell
.\mvnw.cmd test
```

Para ejecutar pruebas con limpieza previa:

```powershell
.\mvnw.cmd clean test
```

Para ejecutar una clase específica:

```powershell
.\mvnw.cmd -Dtest=TicketSoporteServiceTest test
```

---

## 16. Cobertura con JaCoCo

El proyecto incluye configuración de JaCoCo para medir cobertura de pruebas.

Ejecutar pruebas y generar reporte:

```powershell
.\mvnw.cmd clean test
```

Abrir reporte:

```powershell
start target\site\jacoco\index.html
```

El reporte muestra cobertura por paquete, clase, método y línea.

---

## 17. Pruebas implementadas

El proyecto contiene pruebas para:

```txt
Controller
Service
Model
DTO
Exception
Integration Test
Application Test
```

Ejemplos:

```txt
TicketSoporteServiceTest
MensajeTicketServiceTest
TicketSoporteControllerTest
MensajeTicketControllerTest
TicketSoporteControllerIT
MensajeTicketControllerIT
GlobalExceptionHandlerTest
TicketSoporteTest
MensajeTicketTest
DtoTest
ExceptionTest
```

---

## 18. Relación con otros microservicios

Este servicio se relaciona conceptualmente con `usuario-service`, pero no almacena la entidad completa `Usuario`.

Campos usados:

```txt
idUsuario
idUsuarioAsignado
```

Esto permite mantener independencia entre microservicios y evitar acoplamiento directo de bases de datos.

---

## 19. Consideraciones para defensa técnica

Puntos importantes para explicar:

* El microservicio se encarga exclusivamente de la gestión de soporte.
* La lógica de negocio está centralizada en la capa service.
* Los controllers solo gestionan solicitudes HTTP y respuestas HATEOAS.
* Las entidades usan validaciones para evitar datos incompletos.
* Los errores se manejan de forma centralizada.
* Las pruebas cubren lógica de negocio, controllers, modelos, DTOs, excepciones e integración.
* El servicio no depende directamente de la base de datos de usuarios; solo usa IDs.
* JaCoCo permite evidenciar la cobertura de pruebas.

---

## 20. Autor

Proyecto desarrollado para **Perfulandia FullStack** como parte de una arquitectura basada en microservicios.

Microservicio: **Soporte Service**.
