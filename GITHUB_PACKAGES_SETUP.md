# GitHub Packages Setup for Secure Maven Distribution

This guide shows how to set up GitHub Packages to prevent unauthorized skeleton JAR overrides.

## Why GitHub Packages?

**Problems with Local Maven:**
- ❌ No access control - anyone can override
- ❌ No version history
- ❌ No audit trail
- ❌ Manual distribution required

**Benefits of GitHub Packages:**
- ✅ Access control via GitHub permissions
- ✅ Version history and rollback capability
- ✅ Automatic distribution to all projects
- ✅ Audit trail of all changes
- ✅ CI/CD integration

## Setup Steps

### 1. Create GitHub Personal Access Token

1. Go to GitHub → Settings → Developer settings → Personal access tokens → Tokens (classic)
2. Click "Generate new token (classic)"
3. Name it: `Maven Package Publishing`
4. Select scopes:
   - `write:packages` (upload packages)
   - `read:packages` (download packages)
   - `repo` (if private repo)
5. Generate and **save the token securely**

### 2. Configure Maven Settings

Create or edit `~/.m2/settings.xml`:

```xml
<settings>
  <servers>
    <server>
      <id>github</id>
      <username>YOUR_GITHUB_USERNAME</username>
      <password>YOUR_GITHUB_TOKEN</password>
    </server>
  </servers>
</settings>
```

### 3. Update Skeleton POM for Publishing

Add to `game-api-skeleton/pom.xml`:

```xml
<distributionManagement>
  <repository>
    <id>github</id>
    <name>GitHub Packages</name>
    <url>https://maven.pkg.github.com/YOUR_USERNAME/command-center-bot</url>
  </repository>
</distributionManagement>
```

### 4. Publish the Skeleton

```bash
cd command-center-bot/game-api-skeleton
mvn deploy
```

This publishes to GitHub Packages. Only users with write access to the repo can publish.

### 5. Configure Game Projects to Use GitHub Packages

**voidx-game/server/pom.xml** and **legion-game/server/pom.xml**:

```xml
<repositories>
  <repository>
    <id>github</id>
    <url>https://maven.pkg.github.com/YOUR_USERNAME/command-center-bot</url>
  </repository>
</repositories>

<dependency>
  <groupId>com.rsps</groupId>
  <artifactId>game-api-skeleton</artifactId>
  <version>1.0.0</version>
</dependency>
```

## Access Control

**Who can publish new versions:**
- Repository admins
- Users with write access
- GitHub Actions (with deploy key)

**Who can download:**
- Anyone with read access to the repo (if private)
- Anyone (if public repo)

**To revoke access:**
- Remove user from repository
- Revoke their GitHub token

## Version Management

**Publishing a new version:**

1. Update version in `game-api-skeleton/pom.xml`:
   ```xml
   <version>1.0.1</version>
   ```

2. Deploy:
   ```bash
   cd game-api-skeleton
   mvn clean deploy
   ```

3. Update game projects:
   ```xml
   <version>1.0.1</version>  <!-- in dependencies -->
   ```

4. Rebuild game projects:
   ```bash
   mvn clean package
   ```

## Rollback to Previous Version

If a bad version is published:

```xml
<!-- In game project pom.xml -->
<dependency>
  <groupId>com.rsps</groupId>
  <artifactId>game-api-skeleton</artifactId>
  <version>1.0.0</version>  <!-- rollback to previous version -->
</dependency>
```

## CI/CD Integration

### GitHub Actions Auto-Deploy

Create `.github/workflows/publish-skeleton.yml`:

```yaml
name: Publish Skeleton to GitHub Packages

on:
  push:
    branches: [ main ]
    paths:
      - 'game-api-skeleton/**'

jobs:
  publish:
    runs-on: ubuntu-latest

    steps:
    - uses: actions/checkout@v3

    - name: Set up JDK 11
      uses: actions/setup-java@v3
      with:
        java-version: '11'
        distribution: 'temurin'

    - name: Publish to GitHub Packages
      run: |
        cd game-api-skeleton
        mvn -B deploy
      env:
        GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

This automatically publishes when skeleton code changes are pushed.

## Audit Trail

View all published versions:
1. Go to repository on GitHub
2. Click "Packages" on right sidebar
3. Click on `game-api-skeleton`
4. See all versions with:
   - Who published
   - When published
   - Commit SHA
   - Download count

## Security Best Practices

1. **Never commit tokens to git**
2. **Use Personal Access Tokens (not passwords)**
3. **Limit token scope to only packages**
4. **Rotate tokens periodically**
5. **Use GitHub Actions for automated deploys**
6. **Review package permissions regularly**

## Comparison: Local Maven vs GitHub Packages

| Feature | Local Maven | GitHub Packages |
|---------|-------------|-----------------|
| Access Control | ❌ None | ✅ GitHub permissions |
| Version History | ❌ No | ✅ Yes |
| Audit Trail | ❌ No | ✅ Complete |
| Distribution | ❌ Manual | ✅ Automatic |
| Rollback | ❌ Manual | ✅ Version-based |
| CI/CD | ❌ Complex | ✅ Easy |
| Cost | ✅ Free | ✅ Free (public) |

## Current Status

- **Using**: Local Maven (no security)
- **Recommended**: GitHub Packages (with access control)

Would you like me to implement GitHub Packages setup?
