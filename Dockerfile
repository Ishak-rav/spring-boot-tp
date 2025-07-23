# Dockerfile pour l'application Ticket Management
FROM maven:3.9-eclipse-temurin-17 AS build

# Définir le répertoire de travail pour le build
WORKDIR /app

# Copier les fichiers de configuration Maven
COPY pom.xml .

# Télécharger les dépendances (mise en cache Docker)
RUN mvn dependency:go-offline -B

# Copier le code source
COPY src ./src

# Construire l'application
RUN mvn clean package -DskipTests

# Étape finale avec JRE
FROM eclipse-temurin:17-jre

# Métadonnées de l'image
LABEL maintainer="Ticket Management Team"
LABEL version="1.0.0"
LABEL description="API de gestion de tickets de support"

# Créer un utilisateur non-root
RUN addgroup --system springboot && adduser --system --ingroup springboot springboot

# Créer le répertoire de travail
WORKDIR /app

# Copier le JAR depuis l'étape de build
COPY --from=build /app/target/ticket-management.jar app.jar

# Changer le propriétaire
RUN chown springboot:springboot app.jar

# Passer à l'utilisateur non-root
USER springboot

# Variables d'environnement par défaut
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8080

# Exposer le port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Point d'entrée
ENTRYPOINT ["java", "-jar", "app.jar"]