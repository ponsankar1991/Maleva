#!/bin/sh
set -e

# Default values to fall back on
: "${SPRING_DATASOURCE_URL:=jdbc:sqlserver://host.docker.internal:1433;databaseName=Maleva}"
: "${SPRING_DATASOURCE_USERNAME:=sa}"
: "${SPRING_DATASOURCE_PASSWORD:=YourStrong!Passw0rd}"
: "${SPRING_REDIS_HOST:=redis}"
: "${SPRING_REDIS_PORT:=6379}"

# Function to parse host and port from JDBC SQL Server URL
# Example URL: jdbc:sqlserver://hostname:1433;databaseName=Maleva
parse_sqlserver_host_port() {
  url="$1"
  # remove prefix
  hostport=$(echo "$url" | sed -E 's#jdbc:sqlserver://([^;]+).*#\1#')
  # split host:port
  host=$(echo "$hostport" | cut -d: -f1)
  port=$(echo "$hostport" | cut -s -d: -f2)
  if [ -z "$port" ]; then
    port=1433
  fi
  echo "$host" "$port"
}

# Wait for TCP port to be open
wait_for_port() {
  host="$1"
  port="$2"
  echo "Waiting for $host:$port..."
  retries=30
  count=0
  while ! nc -z "$host" "$port" 2>/dev/null; do
    count=$((count+1))
    if [ "$count" -ge "$retries" ]; then
      echo "Timed out waiting for $host:$port" >&2
      exit 1
    fi
    sleep 2
  done
  echo "$host:$port is available"
}

# Parse SQL Server host/port from URL
read db_host db_port <<EOF
$(parse_sqlserver_host_port "$SPRING_DATASOURCE_URL")
EOF

# Wait for DB
wait_for_port "$db_host" "$db_port"

# Wait for Redis
wait_for_port "$SPRING_REDIS_HOST" "$SPRING_REDIS_PORT"

# If a mounted config exists at /app/config/application.yaml, use it by setting spring config location
SPRING_CONFIG_OPTS=""
if [ -f /app/config/application.yaml ]; then
  SPRING_CONFIG_OPTS="--spring.config.location=file:/app/config/application.yaml"
  echo "Using mounted application.yaml at /app/config/application.yaml"
fi

# Start the application with any passed JVM_OPTS
exec java ${JVM_OPTS} -jar /app/app.war ${SPRING_CONFIG_OPTS} "$@"
