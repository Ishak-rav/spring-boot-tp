# 🎫 Ticket Management API

Une API REST moderne de gestion de tickets de support développée avec Spring Boot 3, offrant une authentification JWT et un contrôle d'accès basé sur les rôles.

## 📋 Table des matières

- [Fonctionnalités](#-fonctionnalités)
- [Technologies utilisées](#-technologies-utilisées)
- [Prérequis](#-prérequis)
- [Installation](#-installation)
- [Configuration](#-configuration)
- [Utilisation](#-utilisation)
- [API Documentation](#-api-documentation)
- [Tests](#-tests)
- [Déploiement](#-déploiement)
- [Structure du projet](#-structure-du-projet)
- [Contribution](#-contribution)

## ✨ Fonctionnalités

### 🔐 Authentification et Autorisation
- **Authentification JWT** sécurisée
- **Contrôle d'accès basé sur les rôles** (Admin/Utilisateur)
- **Endpoints publics** pour la consultation des tickets non résolus

### 🎫 Gestion des Tickets
- **Création de tickets** par tous les utilisateurs connectés
- **Modification des tickets** par les créateurs et administrateurs
- **Résolution de tickets** par les administrateurs uniquement
- **Consultation publique** des tickets non résolus
- **Système de priorités** et **catégories**

### 📊 Fonctionnalités avancées
- **Pagination** et **filtrage** des tickets
- **Recherche avancée** par statut, priorité, catégorie
- **Audit trail** avec timestamps automatiques
- **Validation des données** côté serveur

## 🛠 Technologies utilisées

- **Java 17** - Langage de programmation
- **Spring Boot 3.2.0** - Framework principal
- **Spring Security** - Authentification et autorisation
- **Spring Data JPA** - Persistance des données
- **JWT (JSON Web Tokens)** - Authentification stateless
- **H2 Database** - Base de données en mémoire (développement)
- **MySQL** - Base de données de production
- **Lombok** - Réduction du code boilerplate
- **OpenAPI/Swagger** - Documentation API
- **Testcontainers** - Tests d'intégration
- **Maven** - Gestion des dépendances
- **Docker** - Containerisation

## 📋 Prérequis

- **Java 17** ou supérieur
- **Maven 3.6+**
- **Docker** (optionnel, pour la base de données)
- **Git**

## 🚀 Installation

### 1. Cloner le repository

```bash
git clone https://github.com/Ishak-rav/spring-boot-tp.git
cd spring-boot-tp
```

### 2. Installation rapide avec Make

```bash
# Installation complète
make install

# Démarrage de l'application
make run
```

### 3. Installation manuelle

```bash
# Compilation du projet
./mvnw clean compile

# Démarrage de la base de données (optionnel)
docker-compose up -d db

# Lancement de l'application
./mvnw spring-boot:run
```

## ⚙️ Configuration

### Configuration de base

L'application utilise des profils Spring pour différents environnements :

- **Développement** : Base de données H2 en mémoire
- **Production** : Base de données MySQL

### Variables d'environnement

| Variable | Description | Valeur par défaut |
|----------|-------------|-------------------|
| `SERVER_PORT` | Port du serveur | `8080` |
| `DB_URL` | URL de la base de données | `jdbc:h2:mem:ticketdb` |
| `DB_USERNAME` | Nom d'utilisateur BDD | `sa` |
| `DB_PASSWORD` | Mot de passe BDD | `` |
| `JWT_SECRET` | Clé secrète JWT | (générée automatiquement) |

### Configuration Docker

```bash
# Démarrer la base de données et PhpMyAdmin
docker-compose up -d

# Démarrer uniquement la base de données
docker-compose up -d db
```

## 📖 Utilisation

### Démarrage rapide

1. **Démarrer l'application** :
   ```bash
   make run
   # ou
   ./mvnw spring-boot:run
   ```

2. **Accéder à l'application** :
   - API : http://localhost:8080
   - Console H2 : http://localhost:8080/h2-console
   - Documentation Swagger : http://localhost:8080/swagger-ui.html

3. **Utilisateurs par défaut** :
   - **Admin** : `admin@example.com` / `admin`
   - **Utilisateur** : `user@example.com` / `user`

### Exemples d'utilisation

#### Authentification

```bash
# Connexion
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"admin"}'

# Réponse
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "email": "admin@example.com",
    "role": "ADMIN"
  }
}
```

#### Gestion des tickets

```bash
# Créer un ticket
curl -X POST http://localhost:8080/api/tickets \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "titre": "Problème de connexion",
    "description": "Impossible de se connecter à l application",
    "prioriteId": 1,
    "categorieId": 1
  }'

# Lister tous les tickets (public)
curl http://localhost:8080/api/tickets/public

# Rechercher des tickets
curl "http://localhost:8080/api/tickets?statut=OUVERT&priorite=HAUTE&page=0&size=10"
```

## 📚 API Documentation

### Endpoints principaux

#### 🔐 Authentification (`/api/auth`)
- `POST /api/auth/register` - Inscription
- `POST /api/auth/login` - Connexion
- `POST /api/auth/refresh` - Rafraîchir le token

#### 🎫 Tickets (`/api/tickets`)
- `GET /api/tickets/public` - Liste publique (tickets non résolus)
- `GET /api/tickets` - Liste complète (authentifié)
- `POST /api/tickets` - Créer un ticket (authentifié)
- `GET /api/tickets/{id}` - Détails d'un ticket
- `PUT /api/tickets/{id}` - Modifier un ticket
- `PATCH /api/tickets/{id}/resolve` - Résoudre un ticket (admin)

#### 🏷️ Catégories (`/api/categories`)
- `GET /api/categories` - Liste des catégories
- `POST /api/categories` - Créer une catégorie (admin)

#### ⚡ Priorités (`/api/priorites`)
- `GET /api/priorites` - Liste des priorités
- `POST /api/priorites` - Créer une priorité (admin)

### Documentation Swagger

Une documentation interactive complète est disponible à l'adresse :
**http://localhost:8080/swagger-ui.html**

## 🧪 Tests

### Exécution des tests

```bash
# Tous les tests
make test
# ou
./mvnw test

# Tests d'intégration uniquement
./mvnw test -Dtest="*IntegrationTest"

# Tests unitaires uniquement
./mvnw test -Dtest="*Test" -Dtest="!*IntegrationTest"

# Tests avec couverture
./mvnw test jacoco:report
```

### Types de tests

- **Tests unitaires** : Services, utilitaires
- **Tests d'intégration** : Contrôleurs, repositories
- **Tests avec Testcontainers** : Base de données réelle

## 🚢 Déploiement

### Avec Docker

```bash
# Build de l'image
make docker-build

# Lancement complet
docker-compose up -d

# Production
docker-compose -f docker-compose.prod.yml up -d
```

### Déploiement manuel

```bash
# Package de l'application
./mvnw clean package -DskipTests

# Exécution du JAR
java -jar target/ticket-management.jar
```

### Variables d'environnement de production

```bash
export SPRING_PROFILES_ACTIVE=prod
export DB_URL=jdbc:mysql://localhost:3306/ticketdb
export DB_USERNAME=ticket_user
export DB_PASSWORD=your_secure_password
export JWT_SECRET=your_super_secret_jwt_key
```

## 📁 Structure du projet

```
src/
├── main/
│   ├── java/com/example/
│   │   ├── TicketManagementApplication.java
│   │   ├── config/           # Configuration Spring
│   │   ├── controller/       # Contrôleurs REST
│   │   ├── dao/             # Repositories
│   │   ├── dto/             # Objets de transfert
│   │   ├── filter/          # Filtres de sécurité
│   │   ├── model/           # Entités JPA
│   │   ├── service/         # Logique métier
│   │   ├── util/            # Utilitaires
│   │   └── view/            # Vues JSON
│   └── resources/
│       ├── application.properties
│       ├── application-prod.properties
│       └── data.sql
└── test/                    # Tests unitaires et d'intégration
```

### Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Controllers   │────│    Services     │────│   Repositories  │
│   (REST API)    │    │ (Business Logic)│    │   (Data Access) │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         │                       │                       │
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   DTOs/Views    │    │   Models/JPA    │    │    Database     │
│  (Data Transfer)│    │   (Entities)    │    │  (H2/MySQL)     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 🤝 Contribution

1. **Fork** le projet
2. Créer une **branche feature** (`git checkout -b feature/AmazingFeature`)
3. **Commit** les changements (`git commit -m 'Add some AmazingFeature'`)
4. **Push** vers la branche (`git push origin feature/AmazingFeature`)
5. Ouvrir une **Pull Request**

### Standards de code

- Suivre les conventions Java standard
- Utiliser Lombok pour réduire le boilerplate
- Documenter les APIs avec OpenAPI
- Maintenir une couverture de tests > 80%

## 📝 Licence

Ce projet est sous licence MIT. Voir le fichier `LICENSE` pour plus de détails.

## 📞 Support

- **Issues** : [GitHub Issues](https://github.com/Ishak-rav/spring-boot-tp/issues)
- **Email** : support@ticket-management.com
- **Documentation** : [Wiki du projet](https://github.com/Ishak-rav/spring-boot-tp/wiki)

---

**Développé avec ❤️ par l'équipe Ticket Management**
