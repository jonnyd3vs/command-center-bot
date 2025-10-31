# Manual Deployment Instructions

## What's Already Done
✅ SSH public key has been copied to server (148.113.171.18)
✅ Project tarball created at `C:\Users\Jonat\Desktop\command-center-bot.tar.gz`

## Steps to Complete Deployment

### 1. Upload the project to the server
Open a PowerShell/Command Prompt and run:
```powershell
scp -o StrictHostKeyChecking=no C:\Users\Jonat\Desktop\command-center-bot.tar.gz ubuntu@148.113.171.18:~
```
(Accept the host key if prompted, use password: Fn@gk#uGvJd$g5q%Da9)

### 2. SSH into the server
```bash
ssh ubuntu@148.113.171.18
```

### 3. Extract and build the project
```bash
# Extract the project
tar -xzf command-center-bot.tar.gz
cd command-center-bot

# Build with Maven
mvn clean package -DskipTests

# Create config file with bot token
cat > config.properties << 'EOF'
DISCORD_TOKEN=YOUR_DISCORD_BOT_TOKEN_HERE
EOF
```

### 4. Create systemd service
```bash
sudo tee /etc/systemd/system/command-center-bot.service << 'EOF'
[Unit]
Description=Command Center Discord Bot
After=network.target

[Service]
Type=simple
User=ubuntu
WorkingDirectory=/home/ubuntu/command-center-bot
ExecStart=/usr/bin/java -jar /home/ubuntu/command-center-bot/target/command-center-bot-1.0.0.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF
```

### 5. Start the bot service
```bash
sudo systemctl daemon-reload
sudo systemctl enable command-center-bot
sudo systemctl start command-center-bot

# Check status
sudo systemctl status command-center-bot
```

### 6. Disable password authentication (IMPORTANT - do this last!)
```bash
sudo sed -i 's/^#\?PasswordAuthentication.*/PasswordAuthentication no/' /etc/ssh/sshd_config
sudo systemctl restart sshd
```

## Useful Commands
- View bot logs: `sudo journalctl -u command-center-bot -f`
- Restart bot: `sudo systemctl restart command-center-bot`
- Stop bot: `sudo systemctl stop command-center-bot`
