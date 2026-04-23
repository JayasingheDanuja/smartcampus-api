# Smart Campus Sensor & Room Management API

**Module          :**        5COSC022W - Client Server Architectures 
**Student         :**        Danuja Jayasinghe  
**UoW No          :**        w2120262  
**Framework       :**        JAX-RS (Jersey 2.39) on Embedded Apache Tomcat 9  
**Persistence     :**        In-memory data structures (ConcurrentHashMap, ArrayList)  
**Java Version    :**        11 (OpenJDK 11+)  
**Build Tool      :**        Apache Maven 3.6+

---

## API Design Overview

A production-ready RESTful API for managing campus facilities and IoT sensors, built using JAX-RS without requiring external server installation.

```
/api/v1                              ← Discovery / entry point
/api/v1/rooms                        ← Room collection
/api/v1/rooms/{roomId}               ← Individual room
/api/v1/sensors                      ← Sensor collection (supports ?type= filter)
/api/v1/sensors/{sensorId}           ← Individual sensor
/api/v1/sensors/{sensorId}/readings  ← Reading history sub-resource
```

**Key Design Decisions:**
- In-memory storage only - `ConcurrentHashMap` and `ArrayList`, no database
- Versioned base path `/api/v1` configured via `@ApplicationPath` annotation
- Sub-resource locator pattern delegates reading history to a dedicated class
- Leak-proof error handling - every exception maps to a structured JSON response
- Cross-cutting request/response logging via a single JAX-RS filter

---

## Project Structure

```
src/
└── main/
    ├── java/
    │   └── com/smartcampus/
    │       ├── Main.java    ←------------------------------------ Entry point, starts embedded Tomcat
    │       ├── application/
    │       │   └── SmartCampusApplication.java   ←--------------- JAX-RS Application class (@ApplicationPath)
    │       ├── model/
    │       │   ├── Room.java
    │       │   ├── Sensor.java
    │       │   ├── SensorReading.java
    │       │   └── DataStore.java   ←---------------------------- Singleton ConcurrentHashMap store
    │       ├── resource/
    │       │   ├── DiscoveryResource.java       ←---------------- GET /api/v1
    │       │   ├── RoomResource.java            ←---------------- /api/v1/rooms
    │       │   ├── SensorResource.java          ←---------------- /api/v1/sensors
    │       │   └── SensorReadingResource.java   ←---------------- /api/v1/sensors/{id}/readings
    │       ├── exception/
    │       │   ├── RoomNotEmptyException.java
    │       │   ├── RoomNotEmptyExceptionMapper.java  ←----------- HTTP 409
    │       │   ├── LinkedResourceNotFoundException.java
    │       │   ├── LinkedResourceNotFoundExceptionMapper.java   ← HTTP 422
    │       │   ├── SensorUnavailableException.java
    │       │   ├── SensorUnavailableExceptionMapper.java  ←------ HTTP 403
    │       │   └── GlobalExceptionMapper.java    ←--------------- HTTP 500 catch-all
    │       └── filter/
    │           └── ApiLoggingFilter.java  ←---------------------- Request + response logging
    └── webapp/
        └── WEB-INF/
            └── web.xml
```

---

## How to Build and Run

### Prerequisites

| Tool             | Version           |
|------------------|-------------------|
| Java JDK         | 11 or higher      |
| Apache Maven     | 3.6+              |
| NetBeans IDE     | 12+ (recommended) |

No Tomcat installation needed - it is embedded inside the project.

### Option A - Run from NetBeans (easiest)

1. Clone the repository: `git clone https://github.com/JayasingheDanuja/smartcampus-api.git`
2. Open NetBeans → **File → Open Project** → select the `smartcampus` folder
3. NetBeans recognises it as a Maven project automatically
4. Right-click the project → **Run**
5. The API starts at: `http://localhost:8080/smart-campus-api/api/v1`

### Option B - Command Line

```bash
# 1. Clone the repo
git clone https://github.com/JayasingheDanuja/smartcampus-api.git
cd smartcampus-api

# 2. Build a fat executable JAR
mvn clean package

# 3. Run it
java -jar target/smart-campus-api.jar
```

The API will be available at: `http://localhost:8080/smart-campus-api/api/v1`

---

## Sample curl Commands

### 1. Discovery - GET /api/v1
```bash
curl -X GET http://localhost:8080/smart-campus-api/api/v1/
```

### 2. List all rooms
```bash
curl -X GET http://localhost:8080/smart-campus-api/api/v1/rooms
```

### 3. Create a new room
```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"ENG-201\",\"name\":\"Engineering Workshop\",\"capacity\":40}"
```

### 4. Get sensors filtered by type
```bash
curl -X GET "http://localhost:8080/smart-campus-api/api/v1/sensors?type=CO2"
```

### 5. Register a new sensor
```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"TEMP-002\",\"type\":\"Temperature\",\"status\":\"ACTIVE\",\"currentValue\":0.0,\"roomId\":\"ENG-201\"}"
```

### 6. Post a reading (also updates parent sensor currentValue)
```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors/TEMP-002/readings \
  -H "Content-Type: application/json" \
  -d "{\"value\":23.7}"
```

### 7. Attempt to delete a room with active sensors - expect 409 Conflict
```bash
curl -X DELETE http://localhost:8080/smart-campus-api/api/v1/rooms/LIB-301
```

### 8. Create sensor with non-existent roomId - expect 422
```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"CO2-999\",\"type\":\"CO2\",\"status\":\"ACTIVE\",\"currentValue\":0.0,\"roomId\":\"FAKE-999\"}"
```

### 9. Post reading to a MAINTENANCE sensor - expect 403 Forbidden
```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors/OCC-001/readings \
  -H "Content-Type: application/json" \
  -d "{\"value\":15}"
```

---

## Report Conceptual Question Answers

### Part 1.1 JAX-RS Resource Lifecycle & In-Memory Synchronisation

By default, JAX-RS creates a new instance of every resource class for each incoming HTTP request (request-scoped lifecycle). This is the safest default because each request gets its own isolated object, eliminating shared mutable state within a single resource instance.

The critical consequence for this project is that no data can be stored as instance fields inside a resource class it would vanish after the request completes. To solve this, all shared state lives in a singleton `DataStore` class, initialised once when the application starts and referenced by every resource via `DataStore.getInstance()`. The collections inside `DataStore` use `ConcurrentHashMap`, which provides thread-safe concurrent access without requiring manual `synchronized` blocks. Without this, two simultaneous POST requests could both read an empty map, both decide a key does not exist, and both insert the same record - a classic race condition leading to silent data corruption.

---

### Part 1.2 HATEOAS and Self-Documenting APIs

HATEOAS (Hypermedia as the Engine of Application State) means embedding navigational links directly inside API responses, so clients can discover what actions are available without consulting external documentation. This is considered a hallmark of mature RESTful design because it decouples the client from hardcoded URI knowledge. If the server restructures its paths in a new version, a well-behaved HATEOAS client follows the updated links from the discovery endpoint rather than breaking. For client developers, this reduces onboarding time - they start at `GET /api/v1`, read the resource map, and navigate from there without needing to study a separate API reference.

---

### Part 2.1 Returning IDs vs Full Objects in a List

Returning only IDs keeps the response payload small and fast to transfer, but forces the client to issue one additional GET request per room to retrieve usable data the N+1 problem. Returning full objects increases payload size proportionally with the collection size, but allows the client to render a complete list with a single round trip. For a campus management dashboard where a user might scroll through hundreds of rooms, returning full objects is the better trade-off. For embedded hardware with bandwidth constraints, returning IDs would be preferable.

---

### Part 2.2 Idempotency of DELETE

The DELETE operation is idempotent in this implementation. Idempotency means that sending the same request multiple times produces the same server state as sending it once. After the first successful DELETE, the room no longer exists. A second identical DELETE request finds nothing in the store and returns `404 Not Found` - the server state (room is absent) is identical after both calls. No data is created, modified, or duplicated by the second request, satisfying the idempotency contract defined in the HTTP specification.

---

### Part 3.1 @Consumes and Content-Type Mismatch

`@Consumes(MediaType.APPLICATION_JSON)` declares that the endpoint will only process requests with a `Content-Type: application/json` header. If a client sends `text/plain` or `application/xml`, the JAX-RS runtime rejects the request before the resource method is even called and automatically returns `HTTP 415 Unsupported Media Type`. This enforcement happens entirely at the framework level, so no application code needs to validate the format manually. It protects the Jackson deserializer from receiving unexpected data structures it was not designed to parse.

---

### Part 3.2 @QueryParam vs @PathParam for Filtering

Using a query parameter (`/sensors?type=CO2`) is semantically correct for filtering because the base resource being addressed is still `/sensors` - the `type` parameter simply narrows the result set. Path parameters are designed for identifying a specific, unique resource (e.g. `/sensors/TEMP-001`). Embedding a filter value in the path (`/sensors/type/CO2`) implies that this is a distinct, permanently addressable resource, which is architecturally misleading. Query parameters are optional by nature, so a single method handles both `GET /sensors` (return all) and `GET /sensors?type=CO2` (return filtered) without duplication.

---

### Part 4.1 Sub-Resource Locator Pattern

The sub-resource locator pattern delegates further path matching to a separate class at runtime. Rather than adding all `/sensors/{id}/readings` routes directly to `SensorResource`, a locator method returns an instance of `SensorReadingResource`, and JAX-RS hands all remaining path segments to that class. In a large API with many nested paths, this prevents individual controller classes from growing into unmanageable monoliths with hundreds of methods. Each class has one clear responsibility, making it independently testable and easier to extend without introducing regressions elsewhere.

---

### Part 5.2 Why 422 is More Accurate Than 404

`HTTP 404 Not Found` signals that the URL itself corresponds to no known resource on the server. `HTTP 422 Unprocessable Entity` signals that the URL is valid, the request was understood, and the JSON was parsed successfully, but the semantic content of the payload failed a business rule. When a client POSTs a sensor with a `roomId` that does not exist, the endpoint `/api/v1/sensors` is perfectly valid. The problem is a broken reference inside the request body. Using 404 would mislead the client into thinking the API endpoint is missing; 422 precisely communicates that the data itself was logically invalid.

---

### Part 5.4 Cybersecurity Risks of Exposed Stack Traces

A raw Java stack trace exposes multiple categories of sensitive information. It reveals internal package and class names, disclosing the application's architecture. It shows library names and versions (e.g. `org.glassfish.jersey 2.39`), allowing an attacker to look up known CVEs for those exact versions. It may include server file system paths useful for path traversal attempts. It also exposes application logic flow which method called which providing a roadmap for crafting targeted injection or logic-bypass attacks. A clean 500 response with a generic message eliminates all of this attack surface while server-side logs still allow developers to diagnose issues.

---

### Part 5.5 Why Filters Are Superior to Manual Logging

Inserting `Logger.info()` calls into every resource method violates the DRY (Don't Repeat Yourself) principle and tightly couples a cross-cutting concern to business logic. If the log format ever needs to change, every resource method must be modified. A JAX-RS filter registered with `@Provider` runs automatically for every request and response through a single class, regardless of how many endpoints exist. New endpoints added in the future are logged automatically with no extra effort, and resource methods remain clean and focused solely on their business responsibilities.
