# Skeleton Distribution Without PAT

This guide shows how to distribute the skeleton JAR using **GitHub Releases** and **GitHub Actions** - **NO Personal Access Token required!**

## How It Works

### 1. Automatic Publishing (No PAT!)

When you push skeleton changes to `main` branch:

1. GitHub Actions automatically triggers
2. Uses built-in `GITHUB_TOKEN` (provided automatically)
3. Builds the skeleton JAR
4. Creates a GitHub Release
5. Uploads JAR to the release

**No PAT needed** - GitHub provides the token automatically!

### 2. Downloading (No Auth!)

GitHub Releases are **public** - anyone can download without authentication:

```bash
# Download latest skeleton JAR
curl -L -o game-api-skeleton.jar \
  https://github.com/YOUR_USERNAME/command-center-bot/releases/latest/download/game-api-skeleton-1.0.0.jar
```

## Setup Steps

### 1. Enable GitHub Actions

The workflow is already created at `.github/workflows/publish-skeleton.yml`

Just push to GitHub:
```bash
git add .github/workflows/publish-skeleton.yml
git add game-api-skeleton/pom.xml
git commit -m "Add automated skeleton publishing"
git push
```

### 2. Configure Repository Permissions

Go to GitHub Repository → Settings → Actions → General

Under "Workflow permissions":
- ✅ Select "Read and write permissions"
- ✅ Check "Allow GitHub Actions to create and approve pull requests"

Click "Save"

### 3. Test It

Make a change to skeleton code:
```bash
cd game-api-skeleton/src/main/java/com/rsps/api
# Edit any file
git commit -am "Update skeleton"
git push
```

GitHub Actions will:
- Automatically build the JAR
- Create a release
- Upload the JAR

Check: Repository → Releases

## Using in Game Projects

### Option 1: Download Script (Recommended)

Create `update-skeleton.sh` in each game project:

```bash
#!/bin/bash

REPO="YOUR_USERNAME/command-center-bot"
VERSION="1.0.0"
JAR_NAME="game-api-skeleton-${VERSION}.jar"
DOWNLOAD_URL="https://github.com/${REPO}/releases/download/skeleton-v${VERSION}/${JAR_NAME}"

echo "Downloading skeleton JAR from GitHub..."
curl -L -o "server/libs/${JAR_NAME}" "${DOWNLOAD_URL}"

if [ $? -eq 0 ]; then
    echo "✅ Downloaded ${JAR_NAME}"
    echo "Rebuilding project..."
    mvn clean package
else
    echo "❌ Failed to download skeleton JAR"
    exit 1
fi
```

Make it executable:
```bash
chmod +x update-skeleton.sh
```

Run it:
```bash
./update-skeleton.sh
```

### Option 2: Manual Download

1. Go to: `https://github.com/YOUR_USERNAME/command-center-bot/releases`
2. Download `game-api-skeleton-1.0.0.jar`
3. Copy to `your-game/server/libs/`
4. Rebuild: `mvn clean package`

### Option 3: GitHub Actions Auto-Update

Create `.github/workflows/update-skeleton.yml` in game projects:

```yaml
name: Update Skeleton JAR

on:
  schedule:
    - cron: '0 0 * * *'  # Daily at midnight
  workflow_dispatch:  # Manual trigger

jobs:
  update:
    runs-on: ubuntu-latest

    steps:
    - uses: actions/checkout@v3

    - name: Download latest skeleton
      run: |
        curl -L -o server/libs/game-api-skeleton-1.0.0.jar \
          https://github.com/YOUR_USERNAME/command-center-bot/releases/latest/download/game-api-skeleton-1.0.0.jar

    - name: Check if updated
      run: |
        git diff --quiet server/libs/ || echo "UPDATED=true" >> $GITHUB_ENV

    - name: Commit if changed
      if: env.UPDATED == 'true'
      run: |
        git config user.name "GitHub Actions"
        git config user.email "actions@github.com"
        git add server/libs/
        git commit -m "Update skeleton JAR from upstream"
        git push
```

## Security Model

### What's Secure

✅ **No PAT exposure** - Uses GitHub's built-in token
✅ **Token auto-expires** - Each workflow run gets a fresh token
✅ **Limited scope** - Token only works for this repository
✅ **Public downloads** - JAR is public, no credentials needed
✅ **Access control** - Only repo collaborators can trigger builds

### What's Different from PAT

| Feature | GitHub Token | Personal Access Token |
|---------|--------------|----------------------|
| Scope | Single repo | All repos you have access to |
| Expiration | Per-workflow | Manual/long-lived |
| Storage | Automatic | Manual (risky) |
| Security | ✅ High | ⚠️ Medium (if leaked) |
| Setup | ✅ None | ❌ Manual creation |

## Workflow Triggers

The workflow runs when:

1. **Push to main** (skeleton changes only):
   ```bash
   git add game-api-skeleton/
   git commit -m "Update skeleton"
   git push  # Triggers automatically
   ```

2. **Manual trigger** (from GitHub UI):
   - Go to Actions tab
   - Select "Publish Skeleton JAR"
   - Click "Run workflow"

## Version Management

### Incrementing Version

1. Edit `game-api-skeleton/pom.xml`:
   ```xml
   <version>1.0.1</version>  <!-- was 1.0.0 -->
   ```

2. Commit and push:
   ```bash
   git commit -am "Bump skeleton to 1.0.1"
   git push
   ```

3. GitHub Actions creates release `skeleton-v1.0.1`

4. Update game projects:
   ```bash
   # Update download script to use 1.0.1
   ./update-skeleton.sh
   ```

### Listing All Versions

View all published versions:
```bash
# Via GitHub CLI
gh release list

# Or visit
https://github.com/YOUR_USERNAME/command-center-bot/releases
```

## CI/CD Integration

### In Your Game Servers

Add to game server build:

```yaml
# .github/workflows/deploy.yml
jobs:
  build:
    steps:
    - name: Download skeleton JAR
      run: |
        mkdir -p server/libs
        curl -L -o server/libs/game-api-skeleton-1.0.0.jar \
          https://github.com/${{ secrets.SKELETON_REPO }}/releases/latest/download/game-api-skeleton-1.0.0.jar

    - name: Build game server
      run: mvn clean package
```

## Comparison: Distribution Methods

| Method | PAT Required | Setup | Security | Updates |
|--------|--------------|-------|----------|---------|
| **GitHub Releases** | ❌ No | Easy | ✅ High | Manual |
| GitHub Packages | ✅ Yes | Medium | ⚠️ Medium | Auto |
| Local Maven | ❌ No | Easy | ❌ None | Manual |
| Self-hosted Nexus | ❌ No | Hard | ✅ High | Auto |

**Recommendation**: GitHub Releases (what we just set up)

## Server Setup

### On Your Build Server

No special setup needed! Just use curl:

```bash
# In deployment script
curl -L -o libs/game-api-skeleton.jar \
  https://github.com/YOUR_USERNAME/command-center-bot/releases/latest/download/game-api-skeleton-1.0.0.jar
```

### On Multiple Servers

Create `install-skeleton.sh`:

```bash
#!/bin/bash
SERVERS=("server1.example.com" "server2.example.com")
VERSION="1.0.0"
JAR_URL="https://github.com/YOUR_USERNAME/command-center-bot/releases/download/skeleton-v${VERSION}/game-api-skeleton-${VERSION}.jar"

for server in "${SERVERS[@]}"; do
    echo "Updating $server..."
    ssh $server "cd /opt/game && curl -L -o libs/game-api-skeleton.jar '${JAR_URL}' && systemctl restart game-server"
done
```

## Rollback

To rollback to a previous version:

```bash
# Download specific version
curl -L -o server/libs/game-api-skeleton.jar \
  https://github.com/YOUR_USERNAME/command-center-bot/releases/download/skeleton-v1.0.0/game-api-skeleton-1.0.0.jar

# Rebuild
mvn clean package
```

## Monitoring

### Check for Updates

```bash
# Get latest release version
LATEST=$(curl -s https://api.github.com/repos/YOUR_USERNAME/command-center-bot/releases/latest | jq -r .tag_name)
echo "Latest skeleton version: $LATEST"
```

### Automated Checking

Add to cron:
```bash
# Check daily for new skeleton versions
0 0 * * * /path/to/check-skeleton-updates.sh
```

## Troubleshooting

### Workflow Not Running

**Check**: Repository → Settings → Actions
- Ensure Actions are enabled
- Check workflow permissions

**Fix**: Enable "Read and write permissions"

### Release Not Created

**Check**: Actions tab for errors

**Common issues**:
- Missing GITHUB_TOKEN permissions
- Tag already exists
- JAR file not found

**Fix**: Check workflow logs in Actions tab

### Download Fails

**Check**: Release exists
```bash
curl -I https://github.com/YOUR_USERNAME/command-center-bot/releases/latest
```

**Fix**:
- Verify repository name
- Check release tag name
- Ensure release is published (not draft)

## Summary

✅ **No PAT required**
✅ **GitHub Actions handles everything**
✅ **Public downloads (no auth)**
✅ **Automatic builds on push**
✅ **Version management via tags**
✅ **Secure by default**

Perfect for your setup with multiple game servers!
