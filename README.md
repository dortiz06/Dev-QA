# Microservicio de Productos

API REST para la gestión de productos (CRUD) construida con Java 17, Spring Boot 3 y MongoDB. El proyecto está pensado como práctica universitaria: incluye validaciones, manejo global de errores, documentación OpenAPI, pruebas unitarias e de integración con Testcontainers, cobertura JaCoCo y análisis estático con SonarQube.

## Tecnologías

- Java 17
- Spring Boot 3.3.5
- Spring Web, Validation y Spring Data MongoDB
- Lombok
- springdoc-openapi (Swagger UI)
- JUnit 5, Mockito, AssertJ
- Testcontainers (MongoDB)
- JaCoCo
- SonarQube Community
- Maven
- Docker Compose

## Requisitos previos

- JDK 17 (Maven debe usar Java 17; verifica con `mvn -v`)
- Maven 3.9+
- Docker y Docker Compose (para MongoDB, SonarQube y las pruebas de integración)

Si `mvn -v` muestra otra versión (por ejemplo Java 21 o 26), apunta `JAVA_HOME` a JDK 17:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -v
```

## Levantar MongoDB y SonarQube

Desde la raíz del proyecto:

```bash
docker compose up -d
```

Servicios:

| Servicio   | URL / puerto              | Notas                                      |
|------------|---------------------------|--------------------------------------------|
| MongoDB    | `mongodb://localhost:27017` | Base `productos_db` al arrancar la app   |
| SonarQube  | http://localhost:9000     | Usuario inicial `admin` / `admin`          |

El primer arranque de SonarQube puede tardar uno o dos minutos. Cuando pida cambiar la contraseña, asígnala y úsala en el comando de análisis.

Para detener los contenedores:

```bash
docker compose down
```

## Ejecutar la aplicación

Con MongoDB en marcha:

```bash
mvn spring-boot:run
```

La API queda en http://localhost:8080

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## Ejecutar las pruebas

Las pruebas de integración usan Testcontainers y requieren Docker. Maven debe correr con JDK 17 (`mvn -v`).

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH="$JAVA_HOME/bin:$PATH"
mvn clean verify
```

Este comando:

1. Compila el proyecto.
2. Ejecuta pruebas unitarias e de integración.
3. Genera el reporte JaCoCo.
4. Falla si la cobertura de líneas es menor al 80%.

Solo unitarias (sin Testcontainers):

```bash
mvn test -Dtest='!*IT'
```

## Reporte de JaCoCo

Tras `mvn clean verify`, el HTML queda en:

```text
target/site/jacoco/index.html
```

El XML que consume SonarQube:

```text
target/site/jacoco/jacoco.xml
```

Se excluyen de la cobertura las clases sin lógica: `MicroservicioApplication`, `config`, DTOs, modelos y `AppConstants`.

## Ejecutar SonarQube

1. Levanta SonarQube con Docker Compose y espera a que http://localhost:9000 cargue.
2. Inicia sesión (por defecto `admin` / `admin`) y genera un token en **My Account > Security**.
3. Genera cobertura y envía el análisis:

```bash
mvn clean verify sonar:sonar -Dsonar.login=<TU_TOKEN>
```

En SonarQube 10+ también sirve:

```bash
mvn clean verify sonar:sonar -Dsonar.token=<TU_TOKEN>
```

Propiedades ya definidas en el `pom.xml`:

- `sonar.projectKey=com.utch:microservicio-productos`
- `sonar.host.url=http://localhost:9000`
- `sonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml`

## Colección de Postman

Importa `postman/Productos-API.postman_collection.json` en Postman. La variable `baseUrl` apunta a `http://localhost:8080`. El request de creación guarda el `id` en `productoId` para los flujos siguientes.

## Endpoints

Base: `/api/productos`

| Método | Ruta            | Descripción        | Código éxito |
|--------|-----------------|--------------------|--------------|
| POST   | `/api/productos` | Crear producto    | 201          |
| GET    | `/api/productos` | Listar productos  | 200          |
| GET    | `/api/productos/{id}` | Obtener por id | 200          |
| PUT    | `/api/productos/{id}` | Actualizar     | 200          |
| DELETE | `/api/productos/{id}` | Eliminar       | 204          |

### Crear — request

```json
{
  "nombre": "Laptop Gamer",
  "codigo": "LAP001",
  "descripcion": "Equipo de alto rendimiento",
  "precio": 18999.50,
  "stock": 10
}
```

### Crear — response 201

```json
{
  "id": "671f0c2e8a1b2c3d4e5f6789",
  "nombre": "Laptop Gamer",
  "codigo": "LAP001",
  "descripcion": "Equipo de alto rendimiento",
  "precio": 18999.50,
  "stock": 10,
  "fechaCreacion": "2026-11-12T18:40:00Z"
}
```

### Listar — response 200

```json
[
  {
    "id": "671f0c2e8a1b2c3d4e5f6789",
    "nombre": "Laptop Gamer",
    "codigo": "LAP001",
    "descripcion": "Equipo de alto rendimiento",
    "precio": 18999.50,
    "stock": 10,
    "fechaCreacion": "2026-11-12T18:40:00Z"
  }
]
```

### Obtener / actualizar — response 200

Mismo JSON de producto que en la creación. El `PUT` usa el mismo cuerpo que el `POST`.

### Eliminar — response 204

Sin cuerpo.

### Error 400 — validación

```json
{
  "timestamp": "2026-11-12T18:41:00Z",
  "status": 400,
  "error": "Bad Request",
  "mensaje": "Error de validación",
  "path": "/api/productos",
  "errores": [
    {
      "campo": "nombre",
      "mensaje": "El nombre debe tener entre 3 y 100 caracteres"
    },
    {
      "campo": "codigo",
      "mensaje": "El código solo puede contener caracteres alfanuméricos"
    },
    {
      "campo": "precio",
      "mensaje": "El precio debe ser mayor a cero"
    },
    {
      "campo": "stock",
      "mensaje": "El stock no puede ser negativo"
    }
  ]
}
```

Reglas de validación:

- `nombre`: obligatorio, 3 a 100 caracteres
- `codigo`: obligatorio, solo alfanumérico (`A-Z`, `a-z`, `0-9`)
- `precio`: obligatorio y positivo
- `stock`: obligatorio y mayor o igual a 0

### Error 404 — no encontrado

```json
{
  "timestamp": "2026-11-12T18:42:00Z",
  "status": 404,
  "error": "Not Found",
  "mensaje": "No se encontró el producto con id: inexistente",
  "path": "/api/productos/inexistente"
}
```

### Error 409 — código duplicado

```json
{
  "timestamp": "2026-11-12T18:43:00Z",
  "status": 409,
  "error": "Conflict",
  "mensaje": "Ya existe un producto con el código indicado",
  "path": "/api/productos"
}
```

### Error 500 — no controlado

```json
{
  "timestamp": "2026-11-12T18:44:00Z",
  "status": 500,
  "error": "Internal Server Error",
  "mensaje": "Error interno del servidor",
  "path": "/api/productos"
}
```

## Estructura del proyecto

```text
.
├── docker-compose.yml
├── pom.xml
├── README.md
├── postman/
│   └── Productos-API.postman_collection.json
└── src/
    ├── main/
    │   ├── java/com/utch/microservicio/
    │   │   ├── MicroservicioApplication.java
    │   │   ├── config/          # OpenAPI
    │   │   ├── controller/      # REST
    │   │   ├── dto/             # Request, response y errores
    │   │   ├── exception/       # Excepciones y @RestControllerAdvice
    │   │   ├── mapper/          # Mapper manual
    │   │   ├── model/           # Documento MongoDB
    │   │   ├── repository/
    │   │   ├── service/         # Interfaz + implementación
    │   │   └── util/            # Constantes
    │   └── resources/
    │       └── application.yml
    └── test/
        └── java/com/utch/microservicio/
            ├── controller/      # @WebMvcTest
            ├── exception/
            ├── integration/     # Testcontainers + MockMvc
            ├── mapper/
            ├── service/         # Mockito + AssertJ
            └── support/
```
