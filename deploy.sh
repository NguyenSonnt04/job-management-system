#!/bin/bash
set -e

echo "=== QLTD - Production Deployment ==="

# Check .env file exists
if [ ! -f .env ]; then
    echo "ERROR: .env file not found!"
    echo "Copy .env.example to .env and fill in your values:"
    echo "  cp .env.example .env"
    echo "  nano .env"
    exit 1
fi

# Create required directories
mkdir -p certbot/conf certbot/www .secrets config

if [ ! -f config/application-local.properties ]; then
    echo ">> config/application-local.properties not found."
    echo ">> If your API/OAuth/SMTP keys are in application-local.properties, copy that file here before starting production."
fi

# Validate compose file
docker compose config >/dev/null

# Build and start services
echo ">> Building and starting containers..."
docker compose build app
docker compose up -d --remove-orphans

echo ""
echo ">> Waiting for services to start..."
sleep 10

# Check status
echo ""
echo ">> Service status:"
docker compose ps

echo ""
echo "=== Deployment complete! ==="
echo "App:        http://$(hostname -I | awk '{print $1}')"
echo "If you need phpMyAdmin locally, start it with:"
echo "  docker compose --profile admin-tools up -d phpmyadmin"
echo ""
echo ">> To set up SSL, run:"
echo "   docker compose --profile ssl run --rm certbot certonly --webroot -w /var/www/certbot -d your-domain.com"
echo "   Then update nginx/default.conf to enable HTTPS and restart nginx."
