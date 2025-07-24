#!/bin/bash

echo "🌐 Active profile: ${SPRING_PROFILES_ACTIVE:=dev}"

# Nếu MAIL_LOG_LEVEL chưa được set từ ngoài thì tự động gán theo profile
if [ -z "$MAIL_LOG_LEVEL" ]; then
  if [ "$SPRING_PROFILES_ACTIVE" = "prod" ]; then
    export MAIL_LOG_LEVEL=INFO
    echo "🔒 Log level set to INFO (prod)"
  else
    export MAIL_LOG_LEVEL=DEBUG
    echo "🔧 Log level set to DEBUG (dev)"
  fi
fi

exec java -jar app.jar
