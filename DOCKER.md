# Docker Setup for Movies Application

This document provides instructions for running the Movies application using Docker.

## Prerequisites

- Docker
- Docker Compose

## Quick Start

1. **Clone the repository** (if not already done)
   ```bash
   git clone <repository-url>
   cd movies
   ```

2. **Create environment file** (optional)
   ```bash
   cp env.example .env
   ```
   Edit `.env` file with your MongoDB configuration if needed.

3. **Build and start the application**
   ```bash
   docker-compose up --build
   ```

4. **Access the application**
   - Frontend: http://localhost:3000
   - Backend API: http://localhost:8080/api/v1
   - MongoDB: localhost:27017

## Services

### Backend (Spring Boot)
- **Port**: 8080
- **Health Check**: Available at `/api/v1/movies`
- **Database**: MongoDB connection configured via environment variables

### Frontend (React + Nginx)
- **Port**: 3000
- **Proxy**: API calls are proxied to backend container

### Database (MongoDB)
- **Port**: 27017
- **Database**: moviesdb
- **Collections**: movies, reviews

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| MONGO_DATABASE | moviesdb | MongoDB database name |
| MONGO_USER | movieuser | MongoDB username |
| MONGO_PASSWORD | moviepass | MongoDB password |
| MONGO_CLUSTER | localhost | MongoDB cluster (localhost for local MongoDB) |

## Development Commands

### Start all services
```bash
docker-compose up
```

### Start in background
```bash
docker-compose up -d
```

### Stop all services
```bash
docker-compose down
```

### Rebuild and start
```bash
docker-compose up --build
```

### View logs
```bash
docker-compose logs -f [service-name]
```

### Access container shell
```bash
docker-compose exec [service-name] /bin/bash
```

## Production Considerations

1. **MongoDB Atlas**: For production, update `.env` file with your MongoDB Atlas cluster URL
2. **Security**: Change default MongoDB credentials
3. **Volumes**: Database data persists in Docker volume `mongodb_data`

## Troubleshooting

### Backend not starting
- Check MongoDB connection
- Verify environment variables
- Check logs: `docker-compose logs backend`

### Frontend not connecting to backend
- Ensure backend is healthy
- Check nginx proxy configuration
- Verify network connectivity between containers

### Database connection issues
- Verify MongoDB is running: `docker-compose logs mongodb`
- Check connection string in backend logs
- Ensure proper authentication credentials

