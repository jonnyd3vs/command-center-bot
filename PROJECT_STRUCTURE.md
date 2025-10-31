# Command Center Bot - Multi-Module Project Structure

This project has been restructured as a multi-module Maven project with two separate modules:

## Project Structure

```
command-center-bot/
├── pom.xml                    # Parent POM (aggregator)
├── game-api-skeleton/         # Module 1: API Skeleton Library
│   ├── pom.xml
│   └── src/
│       └── main/
│           └── java/
│               └── com/
│                   └── rsps/
│                       └── api/
│                           ├── ApiConfig.java
│                           ├── ApiEndpoint.java
│                           ├── ApiRequest.java
│                           ├── ApiResponse.java
│                           └── GameApiServer.java
└── bot/                       # Module 2: Discord Bot
    ├── pom.xml
    └── src/
        └── main/
            └── java/
                └── com/
                    └── rsps/
                        └── discordbot/
                            └── ... (bot code)
```

## Modules

### 1. game-api-skeleton
**Purpose**: Reusable API server library for game servers

**Contents**:
- HTTP server infrastructure
- API key authentication
- Request/response handling
- Reflection-based endpoint registration

**Output**: `game-api-skeleton-1.0.0.jar`

**Used by**:
- voidx-game
- legion-game (Fantasy)
- Any other RSPS projects
- The Discord bot (for understanding the API structure)

### 2. bot
**Purpose**: Discord bot for managing game servers

**Dependencies**:
- game-api-skeleton (internal)
- JDA (Discord API)
- HttpClient (for API calls)
- Gson (JSON processing)

**Output**: `command-center-bot-1.0.0.jar` (executable)

## Building

### Build Everything
```bash
mvn clean package
```

This builds both modules in the correct order:
1. game-api-skeleton (dependency)
2. bot (depends on skeleton)

### Build Individual Modules

**Build only the skeleton:**
```bash
cd game-api-skeleton
mvn clean package
```

**Build only the bot:**
```bash
cd bot
mvn clean package
```

### Install Skeleton to Local Maven
To make the skeleton available for other projects:
```bash
cd game-api-skeleton
mvn install
```

This installs to `~/.m2/repository/com/rsps/game-api-skeleton/1.0.0/`

## Output Artifacts

After building:
```
game-api-skeleton/target/game-api-skeleton-1.0.0.jar  # Library JAR
bot/target/command-center-bot-1.0.0.jar               # Executable bot JAR
```

## Benefits of This Structure

### 1. **Separation of Concerns**
- Skeleton library is completely independent
- Bot can be developed separately
- Clear module boundaries

### 2. **Reusability**
- Skeleton can be used by any game server
- Update skeleton once, all projects benefit
- Proper versioning (1.0.0, 1.0.1, etc.)

### 3. **Development Workflow**
- Work on skeleton without affecting bot
- Work on bot without rebuilding skeleton
- Test modules independently

### 4. **Version Management**
- Each module can have its own version
- Track changes separately
- Maven handles dependency resolution

## Updating the Skeleton

When you make changes to the skeleton:

1. **Edit skeleton code**:
   ```bash
   # Edit files in game-api-skeleton/src/main/java/com/rsps/api/
   ```

2. **Build and install**:
   ```bash
   cd game-api-skeleton
   mvn clean install
   ```

3. **Update version (optional)**:
   In `game-api-skeleton/pom.xml`:
   ```xml
   <version>1.0.1</version>  <!-- was 1.0.0 -->
   ```

4. **Rebuild bot** (if skeleton changed):
   ```bash
   cd bot
   mvn clean package
   ```

5. **Update game servers**:
   ```bash
   cd ~/game-api-skeleton
   mvn clean install

   # Then rebuild game projects
   cd ~/voidx-game && mvn clean package
   cd ~/legion-game && mvn clean package
   ```

## CI/CD Integration

The build scripts and GitHub Actions have been configured to work with this multi-module structure:

- `build.sh` / `build.bat`: Build all modules
- `run.sh` / `run.bat`: Run the bot module
- GitHub Actions: Build and deploy both modules

## Migration Notes

**From**: Single module project with separate skeleton on server
**To**: Multi-module project with skeleton included

**Changes**:
- Original `pom.xml` → `pom.xml.backup` (saved)
- New `pom.xml` is parent aggregator
- Bot code moved to `bot/` module
- Skeleton code moved to `game-api-skeleton/` module

**Backwards Compatibility**:
- Build scripts still work (`build.sh`, `run.sh`)
- Output JAR is still `bot/target/command-center-bot-1.0.0.jar`
- No changes to deployment process
