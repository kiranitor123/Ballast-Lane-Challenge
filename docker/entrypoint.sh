#!/bin/sh
set -e

echo "=========================================================="
echo " Starting PokeDex Full-Stack Application (Backend + Front)"
echo "=========================================================="

mkdir -p /app/data

cd /app
echo "Starting Spring Boot backend..."
java -jar app.jar &
BACKEND_PID=$!

cleanup() {
    echo "Stopping PokeDex services..."
    kill -TERM "$BACKEND_PID" 2>/dev/null || true
    nginx -s quit 2>/dev/null || true
    wait "$BACKEND_PID" 2>/dev/null || true
    exit 0
}
trap cleanup SIGTERM SIGINT

echo "Waiting for Spring Boot backend to initialize and seed database..."
RETRIES=40
until curl -s -f http://127.0.0.1:8080/api/pokemon > /dev/null 2>&1 || [ "$RETRIES" -le 0 ]; do
    echo "Waiting for backend API... ($RETRIES attempts remaining)"
    RETRIES=$((RETRIES - 1))
    sleep 2
done

if [ "$RETRIES" -le 0 ]; then
    echo "Notice: Backend took longer to respond, starting web server anyway..."
else
    echo "Backend is ready and database is initialized with seed data!"
fi

echo "Starting Nginx web server on port 80..."
nginx -g "daemon off;" &
NGINX_PID=$!

wait "$NGINX_PID"
cleanup
