# Deployment Guide for Ubuntu Server

This guide will help you deploy the RSPS Command Center Discord Bot to an Ubuntu server.

## Prerequisites

- Ubuntu Server 20.04 or newer
- Java 11 or higher installed
- Git installed (optional)
- Sudo access

## Option 1: Manual Deployment with systemd

### Step 1: Install Java

```bash
# Update package list
sudo apt update

# Install Java 11
sudo apt install openjdk-11-jre-headless -y

# Verify installation
java -version
```

### Step 2: Create application directory

```bash
# Create directory
sudo mkdir -p /opt/rsps-bot

# Change ownership to your user
sudo chown $USER:$USER /opt/rsps-bot
```

### Step 3: Build and upload the application

On your local machine:
```bash
# Build the project
mvn clean package

# Upload to server (replace USER and SERVER_IP)
scp target/command-center-bot-1.0.0.jar USER@SERVER_IP:/opt/rsps-bot/
scp src/main/resources/servers.xml USER@SERVER_IP:/opt/rsps-bot/
```

### Step 4: Configure environment

On the server:
```bash
# Create environment file
sudo nano /opt/rsps-bot/.env
```

Add the following:
```
DISCORD_BOT_TOKEN=your_discord_bot_token
RSPS_API_KEY=your_api_key
ADMIN_ROLE_ID=your_admin_role_id
MODERATOR_ROLE_ID=your_moderator_role_id
```

### Step 5: Install systemd service

```bash
# Copy service file to systemd
sudo cp rsps-bot.service /etc/systemd/system/

# Edit the service file to update YOUR_USER
sudo nano /etc/systemd/system/rsps-bot.service
```

Update these lines:
- Change `YOUR_USER` to your actual username
- Update environment variables with your actual values

### Step 6: Enable and start the service

```bash
# Reload systemd
sudo systemctl daemon-reload

# Enable service to start on boot
sudo systemctl enable rsps-bot

# Start the service
sudo systemctl start rsps-bot

# Check status
sudo systemctl status rsps-bot
```

### Step 7: Monitor logs

```bash
# View real-time logs
sudo journalctl -u rsps-bot -f

# View last 100 lines
sudo journalctl -u rsps-bot -n 100
```

### Managing the service

```bash
# Start
sudo systemctl start rsps-bot

# Stop
sudo systemctl stop rsps-bot

# Restart
sudo systemctl restart rsps-bot

# Status
sudo systemctl status rsps-bot

# Disable auto-start
sudo systemctl disable rsps-bot
```

## Option 2: Docker Deployment

### Step 1: Install Docker

```bash
# Update package list
sudo apt update

# Install prerequisites
sudo apt install apt-transport-https ca-certificates curl software-properties-common -y

# Add Docker GPG key
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg

# Add Docker repository
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# Install Docker
sudo apt update
sudo apt install docker-ce docker-ce-cli containerd.io docker-compose-plugin -y

# Add your user to docker group
sudo usermod -aG docker $USER

# Verify installation
docker --version
```

### Step 2: Upload project files

```bash
# Create project directory
mkdir -p ~/rsps-bot
cd ~/rsps-bot

# Upload files from local machine
scp -r * USER@SERVER_IP:~/rsps-bot/
```

Or clone from git:
```bash
git clone <repository-url> ~/rsps-bot
cd ~/rsps-bot
```

### Step 3: Build the application

```bash
# Build with Maven (if not already built)
mvn clean package
```

### Step 4: Configure environment

```bash
# Create .env file for Docker
nano .env
```

Add:
```
DISCORD_BOT_TOKEN=your_bot_token
RSPS_API_KEY=your_api_key
ADMIN_ROLE_ID=your_admin_role_id
MODERATOR_ROLE_ID=your_moderator_role_id
```

### Step 5: Build and run with Docker Compose

```bash
# Build the image
docker compose build

# Start the container
docker compose up -d

# View logs
docker compose logs -f

# Check status
docker compose ps
```

### Managing Docker container

```bash
# Start
docker compose start

# Stop
docker compose stop

# Restart
docker compose restart

# Stop and remove container
docker compose down

# Rebuild and restart
docker compose up -d --build
```

## Option 3: Build and Run with Maven

For testing or development:

```bash
# Navigate to project directory
cd /path/to/command-center-bot

# Build the project
mvn clean package

# Run directly
java -jar target/command-center-bot-1.0.0.jar
```

## Post-Deployment

### 1. Verify bot is online

Check your Discord server to see if the bot appears online.

### 2. Test slash commands

In Discord, type `/` to see if the bot's commands appear:
- `/giveitem`
- `/massban`
- `/giveoffers`
- `/setpassword`

### 3. Set up log rotation

If using systemd (logs go to journald):
```bash
# Edit journald config
sudo nano /etc/systemd/journald.conf
```

Set:
```
SystemMaxUse=100M
SystemMaxFileSize=10M
```

Then restart:
```bash
sudo systemctl restart systemd-journald
```

### 4. Firewall configuration

If you have a firewall enabled:
```bash
# Check firewall status
sudo ufw status

# Allow SSH (if not already allowed)
sudo ufw allow 22/tcp

# Note: Discord bot doesn't need incoming ports
# It only makes outbound connections to Discord and your game servers
```

## Updating the Bot

### systemd deployment:

```bash
# On local machine, build new version
mvn clean package

# Upload to server
scp target/command-center-bot-1.0.0.jar USER@SERVER_IP:/opt/rsps-bot/

# On server, restart service
sudo systemctl restart rsps-bot
```

### Docker deployment:

```bash
# Pull latest code (if using git)
git pull

# Rebuild
mvn clean package

# Rebuild and restart container
docker compose up -d --build
```

## Troubleshooting

### Bot won't start

1. Check logs:
   ```bash
   # systemd
   sudo journalctl -u rsps-bot -n 100

   # Docker
   docker compose logs
   ```

2. Verify Java is installed:
   ```bash
   java -version
   ```

3. Check environment variables are set correctly

### Commands not appearing in Discord

1. Wait up to 1 hour for global commands to register
2. Check bot has correct permissions in Discord
3. Try kicking and re-inviting the bot with the correct OAuth URL

### Permission errors

1. Verify role IDs in configuration
2. Check users have the correct roles
3. Make sure bot can see member list (requires "Server Members Intent")

### Cannot connect to game servers

1. Verify servers.xml has correct IPs and ports
2. Check game servers are running and accessible from the Ubuntu server
3. Test connectivity: `curl http://SERVER_IP:PORT`
4. Verify API key is correct

### High memory usage

Add JVM memory limits:

systemd:
```bash
# Edit service file
sudo nano /etc/systemd/system/rsps-bot.service

# Change ExecStart line to:
ExecStart=/usr/bin/java -Xmx512m -jar /opt/rsps-bot/command-center-bot-1.0.0.jar
```

Docker:
```yaml
# In docker-compose.yml, add under rsps-bot service:
deploy:
  resources:
    limits:
      memory: 512M
```

## Security Best Practices

1. **Never commit secrets to git**
   - Keep bot.properties and .env files out of version control
   - Use .gitignore to exclude sensitive files

2. **Use environment variables for production**
   - Don't hardcode tokens in configuration files
   - Use systemd environment or Docker secrets

3. **Restrict file permissions**
   ```bash
   chmod 600 /opt/rsps-bot/.env
   chmod 600 bot.properties
   ```

4. **Keep Java updated**
   ```bash
   sudo apt update && sudo apt upgrade openjdk-11-jre-headless
   ```

5. **Monitor logs for suspicious activity**
   - Check for unauthorized command attempts
   - Monitor API errors

## Backup and Recovery

### Backup configuration:

```bash
# Backup configuration files
tar -czf rsps-bot-config-backup.tar.gz \
  /opt/rsps-bot/.env \
  /opt/rsps-bot/servers.xml \
  /etc/systemd/system/rsps-bot.service
```

### Restore:

```bash
# Extract backup
tar -xzf rsps-bot-config-backup.tar.gz -C /

# Reload systemd
sudo systemctl daemon-reload

# Restart service
sudo systemctl restart rsps-bot
```

## Support

For issues or questions, contact the development team or check the main README.md file.
