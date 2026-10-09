# PokeDex Full-Stack Application

Web application to explore, search, and synchronize Pokémon data using the public PokeAPI service. The system consists of a Spring Boot (Java 25) backend and a React (Vite, TypeScript, Tailwind CSS) frontend.

## Table of Contents

1. [Architecture](#architecture)
2. [Prerequisites](#prerequisites)
3. [Environment Configuration](#environment-configuration)
4. [Running the Application](#running-the-application)
   - [Backend](#backend)
   - [Frontend](#frontend)
   - [Docker](#docker)
5. [API Documentation](#api-documentation)
6. [Predefined Test Data](#predefined-test-data)
7. [Unit Test Suite](#unit-test-suite)
   - [Backend Tests](#backend-tests)
   - [Frontend Tests](#frontend-tests)

## Architecture

### Backend (Spring Boot 4.x / Java 25)

The backend structure uses a layered design:
- `domain`: core entities (`Pokemon`, `User`, `Role`) and repository contracts.
- `application`: application services (`PokemonService`, `AuthService`, DTOs, mappers).
- `infrastructure`: HTTP client for PokeAPI (`PokeApiClient`), H2 database configuration, Spring Security with JWT, and data initialization (`DataSeeder`).
- `presentation`: REST controllers (`PokemonController`, `AuthController`) and global exception handling.

Data access and resilience mechanisms:
- File-persisted relational H2 database at `./data/pokemondb`.
- Stateless authentication using 256-bit HMAC-SHA256 JWTs and BCrypt password hashing.
- Cache-aside search: when a queried name is absent from the local database, the backend queries PokeAPI, persists the entry, and returns it.
- Batch pagination: requests for incomplete or empty pages trigger concurrent batch fetches of 20 items from PokeAPI using `CompletableFuture`.
- On-demand evolution chains: viewing a Pokémon detail view automatically checks and imports any missing members of its evolutionary line.

### Frontend (React 19 / Vite / TypeScript / Tailwind CSS)

The client is split into reusable components and views:
- `src/components`: UI elements including cards, stat bars, elemental badges, sticky navigation bar, and a scroll-to-top floating button.
- `src/pages`: views for infinite-scroll listings via `IntersectionObserver`, detailed view, login, and registration.
- `src/context`: session management through `AuthContext` with defensive `localStorage` parsing.
- `src/services`: Axios client with interceptors to automatically attach `Authorization: Bearer <token>` headers.

## Prerequisites

- Java Development Kit (JDK) 21 or higher (tested with Java 25).
- Node.js 18 or higher.
- `pnpm` package manager 9 or higher (`npm install -g pnpm`).
- Git for version control.

## Environment Configuration

### Backend (`backend/src/main/resources/application.yaml`)

| Property | Default Value | Description |
|---|---|---|
| `server.port` | `8080` | HTTP port for the backend server |
| `spring.datasource.url` | `jdbc:h2:file:./data/pokemondb` | File path for H2 database storage |
| `spring.h2.console.enabled` | `true` | Enables the administrative web console for H2 |
| `spring.h2.console.path` | `/h2-console` | Web path to access the H2 console |
| `pokeapi.base-url` | `https://pokeapi.co/api/v2` | Upstream PokeAPI endpoint |
| `jwt.secret` | `404E635266556A586E327235...` | 256-bit key used to sign and verify JWT tokens |
| `jwt.expiration` | `86400000` | Token expiration period (24 hours in milliseconds) |

### Frontend (`frontend/src/services/api.ts`)

- API Base URL: points to `http://localhost:8080/api`.
- Interceptors: attaches the bearer token on outgoing requests when an active user session exists.

## Running the Application

### Backend

1. Navigate to the `backend` directory:
   ```bash
   cd backend
   ```
2. Start the server using the Maven wrapper:
   - On Windows:
     ```powershell
     .\mvnw.cmd spring-boot:run
     ```
   - On Linux or macOS:
     ```bash
     ./mvnw spring-boot:run
     ```
3. The server runs at `http://localhost:8080`. On first run, `DataSeeder` seeds the default admin user and imports the initial 20 Pokémon.

### Frontend

1. Navigate to the `frontend` directory:
   ```bash
   cd frontend
   ```
2. Install dependencies:
   ```bash
   pnpm install
   ```
3. Start the development server:
   ```bash
   pnpm dev
   ```
4. Access the web application at `http://localhost:5173` or `http://localhost:5174`.

### Docker

Both `backend` and `frontend` include a `Dockerfile` for containerized compilation and execution.

## API Documentation

- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI Specification: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
- H2 Console: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
  - JDBC URL: `jdbc:h2:file:./data/pokemondb`
  - User Name: `sa`
  - Password: (leave empty)

### Key Endpoints

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/auth/register` | Public | Registers a new user with username, email, and password |
| `POST` | `/api/auth/login` | Public | Authenticates credentials and returns a JWT token |
| `GET` | `/api/pokemon` | Public | Paginated Pokémon list with optional search query |
| `GET` | `/api/pokemon/{id}` | Public | Detailed Pokémon stats and evolutionary tree |
| `GET` | `/api/pokemon/filter/type?type={type}` | Public | Filters entries by elemental type |
| `GET` | `/api/pokemon/filter/ability?ability={ability}` | Public | Filters entries by ability |
| `POST` | `/api/pokemon/sync/{idOrName}` | Authenticated | Imports or updates a specific Pokémon from PokeAPI |
| `POST` | `/api/pokemon/sync/batch?startId=X&endId=Y` | Authenticated | Imports a range of Pokémon by ID from PokeAPI |

## Predefined Test Data

### Initial Administrator User (`DataSeeder`)

| Username | Password | Role | Usage |
|---|---|---|---|
| `admin` | `admin123` | `ADMIN` | Used to log in and access protected sync endpoints or authenticated frontend views. |

### Seeded Pokémon

On fresh database startup, the first 20 Pokémon from the National Pokédex are imported:

1. Bulbasaur (#1)
2. Ivysaur (#2)
3. Venusaur (#3)
4. Charmander (#4)
5. Charmeleon (#5)
6. Charizard (#6)
7. Squirtle (#7)
8. Wartortle (#8)
9. Blastoise (#9)
10. Caterpie (#10)
11. Metapod (#11)
12. Butterfree (#12)
13. Weedle (#13)
14. Kakuna (#14)
15. Beedrill (#15)
16. Pidgey (#16)
17. Pidgeotto (#17)
18. Pidgeot (#18)
19. Rattata (#19)
20. Raticate (#20)

Upstream fallback behavior: searching for or navigating to any Pokémon outside this initial batch (such as Pikachu or Gengar) fetches the data directly from PokeAPI, saves it to H2, and returns it immediately.

## Unit Test Suite

### Backend Tests

Run command:
```powershell
cd backend
.\mvnw.cmd test
```

Result: 41 tests executed, 0 errors, 0 failures.

#### `AuthControllerTest` (3 tests)
- `registerUser_Success`: posts valid user data and expects HTTP 201 with an access token.
- `registerUser_ValidationError`: sends blank fields or invalid emails and verifies HTTP 400 Bad Request.
- `loginUser_Success`: provides valid credentials and expects HTTP 200 with an access token.

#### `PokemonControllerTest` (9 tests)
- `listPokemon_DefaultPaging`: requests the default page and confirms HTTP 200 with page metadata.
- `listPokemon_WithSearch`: passes a query parameter and confirms matching entries.
- `getPokemonDetail_Success`: requests an existing identifier and confirms complete stats and species payload.
- `getPokemonDetail_NotFound`: requests a non-existent identifier and confirms HTTP 404.
- `filterByType_Success`: filters by elemental type and checks the resulting list.
- `filterByAbility_Success`: filters by ability and checks the resulting list.
- `syncSinglePokemon_Success`: triggers a single synchronization under authentication.
- `syncBatchPokemon_Success`: triggers range-based synchronization with valid boundaries.
- `syncBatchPokemon_InvalidRange`: sends inverted range boundaries and verifies HTTP 400.

#### `PokemonServiceImplTest` (10 tests)
- `listPokemon_ReturnPageFromDb`: reads existing entries directly from the local repository.
- `listPokemon_FallbackToPokeApiOnSearchNotFound`: triggers PokeAPI lookup and local persistence when a search misses the database.
- `listPokemon_FetchBatchOnIncompletePage`: fetches a batch of 20 items concurrently when the requested page lacks cached entries.
- `getPokemonDetail_ResolvesEvolutionChain`: walks and persists missing evolution chain members on detail lookup.
- `filterByType_Success`: delegates elemental type queries to the repository.
- `filterByAbility_Success`: delegates ability queries to the repository.
- `syncPokemonById_Success`: parses and stores a Pokémon by numerical ID.
- `syncPokemonByName_Success`: parses and stores a Pokémon by name string.
- `syncBatch_Success`: iterates over an ID interval and persists every item.
- `syncBatch_InvalidRangeThrowsException`: asserts a business exception when start ID exceeds end ID.

#### `AuthServiceImplTest` (5 tests)
- `register_Success`: creates an account, hashes password with BCrypt, and returns a token.
- `register_ThrowsWhenUsernameTaken`: prevents duplicate username registrations.
- `register_ThrowsWhenEmailTaken`: prevents duplicate email registrations.
- `login_Success`: verifies authentication through `AuthenticationManager`.
- `login_InvalidCredentialsThrows`: asserts authentication failure on invalid passwords.

#### `JwtServiceTest` (5 tests)
- `generateToken_Valid`: creates an HMAC-SHA256 signed token containing custom claims.
- `extractUsername_Matches`: reads the subject claim from a valid token.
- `isTokenValid_Success`: validates token integrity and signature against user details.
- `isTokenExpired_False`: confirms a newly created token is not flagged as expired.
- `extractRole_Matches`: reads the user role claim from the token payload.

#### `PokeApiClientTest` (8 tests)
- `getPokemon_ById`: deserializes the response payload for an ID request.
- `getPokemon_ByName`: deserializes the response payload for a name request.
- `getPokemon_NotFoundReturnsNull`: handles upstream 404 status codes cleanly.
- `getPokemonSpecies`: retrieves and processes species details.
- `getEvolutionChain`: retrieves and processes evolution chain trees.
- `getPokemonList_Paging`: sends paginated index requests with offset and limit parameters.
- `extractEvolutionNames`: traverses the nested `evolves_to` tree to return ordered species names.

#### `PokemonApiApplicationTests` (1 test)
- `contextLoads`: ensures the Spring Boot ApplicationContext loads cleanly.

### Frontend Tests

Run command:
```bash
cd frontend
pnpm test
```

Result: 18 tests across 6 suites, 0 failures.

#### `authService.test.ts` (4 tests)
- Reads token from storage and checks `isAuthenticated()` boolean return.
- Stores and retrieves parsed user session objects.
- Handles corrupted storage strings by returning `null` instead of raising an uncaught exception.
- Clears tokens and resets user state on `logout()`.

#### `Navbar.test.tsx` (2 tests)
- Displays login and registration options while hiding protected features when unauthenticated.
- Displays username, sync button, and triggers `logout()` when authenticated.

#### `PokemonCard.test.tsx` (2 tests)
- Formats `#025` ID, converts weight to kilograms, renders abilities, and applies type badges.
- Confirms the navigation link points to `/pokemon/:id`.

#### `PokemonStats.test.tsx` (2 tests)
- Renders stat labels and values for all six core attributes.
- Renders six progress bars scaled against the 255 maximum stat threshold.

#### `TypeBadge.test.tsx` (5 tests)
- Renders type names in text.
- Applies the designated background color for electric (`#F8D030`).
- Applies the designated background color for fire (`#F08030`).
- Applies the designated background color for water (`#6890F0`).
- Falls back to `#A8A878` for unrecognized types.

#### `ScrollToTop.test.tsx` (3 tests)
- Remains hidden when scroll position is at the top.
- Appears once vertical scroll exceeds 300 pixels.
- Invokes `window.scrollTo({ top: 0, behavior: 'smooth' })` on click.
