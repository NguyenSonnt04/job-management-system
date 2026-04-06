# VPS Deployment Guide

This repository already contains a Docker-based production deployment flow for a single VPS.

## Files used in production

- `Dockerfile`
- `docker-compose.yml`
- `.env.example`
- `deploy.sh`
- `nginx/default.conf`

## 1. Copy the project to the VPS

Recommended path:

```bash
/opt/job-management-system
```

Then move into the project directory.

## 2. Prepare the environment file

```bash
cp .env.example .env
```

Update at least these variables:

- `MYSQL_PASSWORD`
- `MYSQL_ROOT_PASSWORD`
- `APP_PUBLIC_BASE_URL`
- `APP_DEFAULT_ADMIN_EMAIL`
- `APP_DEFAULT_ADMIN_PASSWORD`

Notes:

- `APP_PUBLIC_BASE_URL` should be your real public URL, for example `https://jobs.example.com`
- the production profile disables demo users, demo employers, demo jobs, and demo applications by default
- the default admin account is only created when both admin email and password are provided

## 2.1 Keep your existing application-local.properties

If all your API, SMTP, OAuth, or Firebase keys already live in `application-local.properties`, keep that approach.

On the VPS, place the file here:

```bash
config/application-local.properties
```

The Docker stack mounts that file to:

```bash
/app/config/application-local.properties
```

and Spring Boot imports it automatically.

Important:

- do not copy secrets into `src/main/resources/application-local.properties` for production builds
- keep the file outside the image and mount it at runtime

## 3. Optional secrets

If you use Firebase, put the service account file in:

```bash
.secrets/
```

and point `FIREBASE_SERVICE_ACCOUNT_PATH` to the matching container path.

## 4. Build and start the stack

```bash
bash deploy.sh
```

This starts:

- `mysql`
- `app`
- `nginx`

Optional tools:

- phpMyAdmin is behind the `admin-tools` profile
- certbot is behind the `ssl` profile

## 5. Check logs

```bash
docker compose logs -f app
```

## 6. Enable HTTPS

After your DNS points to the VPS:

```bash
docker compose --profile ssl run --rm certbot certonly --webroot -w /var/www/certbot -d your-domain.com
```

Then update `nginx/default.conf` to enable the HTTPS server block and restart nginx:

```bash
docker compose restart nginx
```

## Useful commands

Start phpMyAdmin only when needed:

```bash
docker compose --profile admin-tools up -d phpmyadmin
```

Rebuild after code changes:

```bash
docker compose build app
docker compose up -d --remove-orphans
```

Stop everything:

```bash
docker compose down
```

## Production behavior included

- `SPRING_PROFILES_ACTIVE=prod`
- reduced SQL logging
- Thymeleaf cache enabled
- forwarded headers enabled for reverse proxy / HTTPS
- chatbot links use `APP_PUBLIC_BASE_URL` instead of `localhost`
- demo seed data disabled by default
- external `config/application-local.properties` is loaded automatically when present

## Current limitation

CSRF is still disabled in the current application security configuration, because the frontend is not yet wired for CSRF tokens.
