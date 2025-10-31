# GitHub Actions Setup Guide

## Required Secrets

To enable automated deployments with GitHub Actions, you need to configure the following secrets in your GitHub repository:

### How to Add Secrets
1. Go to your GitHub repository
2. Click on **Settings** → **Secrets and variables** → **Actions**
3. Click **New repository secret**
4. Add each of the following secrets:

### Required Secrets:

#### 1. `SSH_PRIVATE_KEY`
Your private SSH key for server access.

**Value:** Copy the contents of `C:\Users\Jonat\.ssh\id_rsa`

```bash
# To view the private key:
cat C:\Users\Jonat\.ssh\id_rsa
```

**Important:** Copy the ENTIRE key including the `-----BEGIN ... -----` and `-----END ... -----` lines.

#### 2. `SERVER_HOST`
**Value:** `148.113.171.18`

#### 3. `SERVER_USER`
**Value:** `ubuntu`

#### 4. `DISCORD_BOT_TOKEN`
**Value:** Your Discord bot token from the Discord Developer Portal

## How the Workflow Works

The GitHub Actions workflow (`.github/workflows/deploy.yml`) will:

1. **Trigger automatically** when you push to `main` or `master` branch
2. **Build** the bot using Maven with Java 11
3. **Deploy** the JAR file to your server via SSH
4. **Create/update** the config file with your bot token
5. **Restart** the systemd service
6. **Check** the service status

## Manual Trigger

You can also manually trigger the deployment:
1. Go to **Actions** tab in your GitHub repository
2. Click on **Deploy Command Center Bot** workflow
3. Click **Run workflow**
4. Select the branch and click **Run workflow**

## Verifying Deployment

After the workflow runs:
- Check the **Actions** tab for the workflow run status
- SSH into your server and run: `sudo systemctl status command-center-bot`
- View logs with: `sudo journalctl -u command-center-bot -f`

## Server Requirements

The workflow assumes:
- The server has Java installed
- The systemd service `command-center-bot` is already created (from initial manual deployment)
- The ubuntu user has sudo permissions (or passwordless sudo for systemctl commands)
