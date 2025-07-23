# ===============================================
# Makefile pour Ticket Management API
# ===============================================

# Configuration
JAVA_HOME := $(shell asdf where java 2>/dev/null || echo $$JAVA_HOME)
MAVEN := JAVA_HOME=$(JAVA_HOME) mvn
PROJECT_NAME := ticket-management
DOCKER_IMAGE := $(PROJECT_NAME)
DOCKER_TAG := latest

# Couleurs pour l'affichage
RED := \033[0;31m
GREEN := \033[0;32m
YELLOW := \033[0;33m
BLUE := \033[0;34m
PURPLE := \033[0;35m
CYAN := \033[0;36m
NC := \033[0m # No Color

# Configuration par défaut
.DEFAULT_GOAL := help
.PHONY: help clean compile test package install run docker-build docker-run docker-compose-up docker-compose-down dev watch lint format check security

# ===============================================
# Aide et documentation
# ===============================================
help: ## 📚 Affiche cette aide
	@echo "$(CYAN)🎫 Ticket Management API - Commandes disponibles:$(NC)"
	@echo ""
	@awk 'BEGIN {FS = ":.*##"; printf "Usage: make $(BLUE)<target>$(NC)\n\n"} /^[a-zA-Z_-]+:.*?##/ { printf "  $(GREEN)%-20s$(NC) %s\n", $$1, $$2 }' $(MAKEFILE_LIST)
	@echo ""
	@echo "$(YELLOW)Variables d'environnement:$(NC)"
	@echo "  JAVA_HOME = $(JAVA_HOME)"
	@echo "  PROJECT_NAME = $(PROJECT_NAME)"
	@echo "  DOCKER_IMAGE = $(DOCKER_IMAGE)"

# ===============================================
# Vérifications préalables
# ===============================================
check-java: ## 🔍 Vérifie la configuration Java
	@echo "$(BLUE)🔍 Vérification de Java...$(NC)"
	@if [ -z "$(JAVA_HOME)" ]; then \
		echo "$(RED)❌ JAVA_HOME n'est pas défini. Assurez-vous qu'asdf est configuré.$(NC)"; \
		exit 1; \
	fi
	@echo "$(GREEN)✅ Java trouvé: $(JAVA_HOME)$(NC)"
	@$(JAVA_HOME)/bin/java -version

check-maven: check-java ## 🔍 Vérifie la configuration Maven
	@echo "$(BLUE)🔍 Vérification de Maven...$(NC)"
	@$(MAVEN) -version

# ===============================================
# Build et compilation
# ===============================================
clean: check-maven ## 🧹 Nettoie le projet
	@echo "$(BLUE)🧹 Nettoyage du projet...$(NC)"
	@$(MAVEN) clean
	@echo "$(GREEN)✅ Nettoyage terminé$(NC)"

compile: check-maven ## 🔨 Compile le projet
	@echo "$(BLUE)🔨 Compilation en cours...$(NC)"
	@$(MAVEN) compile
	@echo "$(GREEN)✅ Compilation terminée$(NC)"

package: check-maven ## 📦 Package l'application
	@echo "$(BLUE)📦 Packaging en cours...$(NC)"
	@$(MAVEN) package -DskipTests
	@echo "$(GREEN)✅ Packaging terminé$(NC)"

install: check-maven ## 📥 Installe les dépendances et compile
	@echo "$(BLUE)📥 Installation et compilation...$(NC)"
	@$(MAVEN) clean install
	@echo "$(GREEN)✅ Installation terminée$(NC)"

# ===============================================
# Tests
# ===============================================
test: check-maven ## 🧪 Exécute les tests unitaires
	@echo "$(BLUE)🧪 Exécution des tests unitaires...$(NC)"
	@$(MAVEN) test
	@echo "$(GREEN)✅ Tests unitaires terminés$(NC)"

test-integration: check-maven ## 🔗 Exécute les tests d'intégration
	@echo "$(BLUE)🔗 Exécution des tests d'intégration...$(NC)"
	@$(MAVEN) verify
	@echo "$(GREEN)✅ Tests d'intégration terminés$(NC)"

test-all: check-maven ## 🧪 Exécute tous les tests avec couverture
	@echo "$(BLUE)🧪 Exécution de tous les tests...$(NC)"
	@$(MAVEN) clean verify jacoco:report
	@echo "$(GREEN)✅ Tous les tests terminés$(NC)"
	@echo "$(CYAN)📊 Rapport de couverture: target/site/jacoco/index.html$(NC)"

# ===============================================
# Développement
# ===============================================
run: check-maven ## 🚀 Lance l'application en mode développement
	@echo "$(BLUE)🚀 Démarrage de l'application...$(NC)"
	@echo "$(CYAN)📖 Documentation disponible sur: http://localhost:8080/swagger-ui.html$(NC)"
	@echo "$(CYAN)🗄️  Console H2 disponible sur: http://localhost:8080/h2-console$(NC)"
	@$(MAVEN) spring-boot:run -Dspring-boot.run.profiles=dev

dev: ## 🔄 Lance l'application avec rechargement automatique
	@echo "$(BLUE)🔄 Mode développement avec rechargement automatique...$(NC)"
	@$(MAVEN) spring-boot:run -Dspring-boot.run.profiles=dev -Dspring-boot.run.jvmArguments="-Dspring.devtools.restart.enabled=true"

watch: ## 👀 Surveille les changements et recompile automatiquement
	@echo "$(BLUE)👀 Surveillance des changements...$(NC)"
	@while true; do \
		$(MAVEN) compile -q; \
		sleep 2; \
	done

# ===============================================
# Qualité du code
# ===============================================
lint: check-maven ## 🔍 Vérifie la qualité du code
	@echo "$(BLUE)🔍 Vérification de la qualité du code...$(NC)"
	@$(MAVEN) checkstyle:check || true
	@echo "$(GREEN)✅ Vérification terminée$(NC)"

format: check-maven ## 🎨 Formate le code
	@echo "$(BLUE)🎨 Formatage du code...$(NC)"
	@$(MAVEN) fmt:format || echo "$(YELLOW)⚠️  Plugin de formatage non configuré$(NC)"

security: check-maven ## 🔒 Analyse de sécurité
	@echo "$(BLUE)🔒 Analyse de sécurité...$(NC)"
	@$(MAVEN) org.owasp:dependency-check-maven:check
	@echo "$(GREEN)✅ Analyse de sécurité terminée$(NC)"

# ===============================================
# Docker
# ===============================================
docker-build: ## 🐳 Construit l'image Docker
	@echo "$(BLUE)🐳 Construction de l'image Docker...$(NC)"
	@docker build -t $(DOCKER_IMAGE):$(DOCKER_TAG) .
	@echo "$(GREEN)✅ Image Docker construite: $(DOCKER_IMAGE):$(DOCKER_TAG)$(NC)"

docker-run: docker-build ## 🐳 Lance l'application dans Docker
	@echo "$(BLUE)🐳 Lancement du conteneur Docker...$(NC)"
	@docker run -p 8080:8080 --name $(PROJECT_NAME)-app -d $(DOCKER_IMAGE):$(DOCKER_TAG)
	@echo "$(GREEN)✅ Application lancée sur http://localhost:8080$(NC)"

docker-stop: ## 🛑 Arrête le conteneur Docker
	@echo "$(BLUE)🛑 Arrêt du conteneur Docker...$(NC)"
	@docker stop $(PROJECT_NAME)-app || true
	@docker rm $(PROJECT_NAME)-app || true
	@echo "$(GREEN)✅ Conteneur arrêté$(NC)"

docker-logs: ## 📋 Affiche les logs du conteneur
	@docker logs -f $(PROJECT_NAME)-app

# ===============================================
# Docker Compose
# ===============================================
docker-compose-up: ## 🐳 Lance tous les services avec Docker Compose
	@echo "$(BLUE)🐳 Lancement des services Docker Compose...$(NC)"
	@docker-compose up -d --build
	@echo "$(GREEN)✅ Services lancés:$(NC)"
	@echo "$(CYAN)  📖 Application: http://localhost:8080$(NC)"
	@echo "$(CYAN)  📖 API Docs: http://localhost:8080/swagger-ui.html$(NC)"
	@echo "$(CYAN)  🗄️  phpMyAdmin: http://localhost:8181$(NC)"

docker-compose-down: ## 🛑 Arrête tous les services Docker Compose
	@echo "$(BLUE)🛑 Arrêt des services Docker Compose...$(NC)"
	@docker-compose down
	@echo "$(GREEN)✅ Services arrêtés$(NC)"

docker-compose-logs: ## 📋 Affiche les logs Docker Compose
	@docker-compose logs -f

docker-compose-restart: docker-compose-down docker-compose-up ## 🔄 Redémarre tous les services

# ===============================================
# Base de données
# ===============================================
db-start: ## 🗄️ Lance uniquement la base de données
	@echo "$(BLUE)🗄️ Lancement de la base de données...$(NC)"
	@docker-compose up -d db phpmyadmin
	@echo "$(GREEN)✅ Base de données lancée$(NC)"
	@echo "$(CYAN)🗄️  phpMyAdmin: http://localhost:8181$(NC)"

db-stop: ## 🛑 Arrête la base de données
	@echo "$(BLUE)🛑 Arrêt de la base de données...$(NC)"
	@docker-compose stop db phpmyadmin
	@echo "$(GREEN)✅ Base de données arrêtée$(NC)"

# ===============================================
# Maintenance
# ===============================================
clean-all: clean ## 🧹 Nettoyage complet (Maven + Docker)
	@echo "$(BLUE)🧹 Nettoyage complet...$(NC)"
	@docker system prune -f || true
	@docker-compose down --volumes --rmi all || true
	@echo "$(GREEN)✅ Nettoyage complet terminé$(NC)"

status: ## 📊 Affiche le statut du projet
	@echo "$(CYAN)📊 Statut du projet:$(NC)"
	@echo "$(BLUE)Java:$(NC) $(shell $(JAVA_HOME)/bin/java -version 2>&1 | head -1)"
	@echo "$(BLUE)Maven:$(NC) $(shell $(MAVEN) -version 2>&1 | head -1)"
	@echo "$(BLUE)Docker:$(NC) $(shell docker --version 2>/dev/null || echo 'Non installé')"
	@echo "$(BLUE)Conteneurs actifs:$(NC)"
	@docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep $(PROJECT_NAME) || echo "  Aucun"

# ===============================================
# Raccourcis utiles
# ===============================================
quick-start: db-start compile run ## ⚡ Démarrage rapide (DB + App)

full-test: clean test-all ## 🧪 Tests complets avec nettoyage

release: clean test-all package docker-build ## 🚀 Build de release complet

deploy: docker-compose-up ## 🚀 Déploiement local complet

# ===============================================
# Informations
# ===============================================
info: ## ℹ️ Informations sur le projet
	@echo "$(CYAN)🎫 Ticket Management API$(NC)"
	@echo "$(BLUE)Version:$(NC) 1.0.0"
	@echo "$(BLUE)Java:$(NC) 17"
	@echo "$(BLUE)Spring Boot:$(NC) 3.2.0"
	@echo "$(BLUE)Base de données:$(NC) MySQL 8.0"
	@echo ""
	@echo "$(YELLOW)Endpoints principaux:$(NC)"
	@echo "  📖 Documentation: http://localhost:8080/swagger-ui.html"
	@echo "  🔐 Login: POST /api/auth/login"
	@echo "  🎫 Tickets publics: GET /api/tickets/unresolved"
	@echo ""
	@echo "$(YELLOW)Comptes par défaut:$(NC)"
	@echo "  👑 Admin: admin / admin123"
	@echo "  👤 User: user / user123"