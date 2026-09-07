# Système de gestion de bibliothèque — Module 10

Ajout de deux microservices métier (`book-service`, `loan-service`) à l'infrastructure
existante (`eureka-server`, `config-server`, `api-gateway`).

## Architecture

| Service        | Port | Rôle                                              |
|----------------|------|----------------------------------------------------|
| eureka-server  | 8761 | Annuaire de service (service discovery)             |
| config-server  | 8888 | Configuration centralisée (sert `config-repo/`)     |
| api-gateway    | 8090 | Point d'entrée unique, routage vers les services    |
| book-service   | 8091 | Catalogue de livres (base H2 `bookdb`)              |
| loan-service   | 8092 | Emprunts (base H2 `loandb`), appelle book-service via Feign |

`loan-service` appelle `book-service` en **lecture** (`GET /api/books/{id}`) et en
**écriture** (`PATCH /api/books/{id}/decrement-stock` et `/increment-stock`) pour tenir
à jour le nombre d'exemplaires disponibles à chaque emprunt / retour.

### Règle métier "défense en profondeur"

`loan-service` vérifie `availableCopies > 0` avant d'appeler `book-service`, **et**
`book-service` revérifie cette même condition avant de décrémenter son propre stock.
Cela protège contre le cas de concurrence (TOCTOU) où deux emprunts concurrents
passeraient tous les deux la première vérification : le second appel à
`decrement-stock` renvoie alors `409 Conflict`, que `loan-service` propage tel quel
au client.

## Démarrer le projet

### Option A — en local (sans Docker)

Prérequis : JDK 17. Maven n'a pas besoin d'être installé : le projet embarque le
**Maven Wrapper** (`mvnw` / `mvnw.cmd`), qui télécharge Maven tout seul au premier
lancement.

Si `JAVA_HOME` n'est pas déjà défini sur ta machine (le wrapper en a besoin, il ne
se contente pas du `java` du PATH), configure-le une fois pour toutes :
- **Windows** : Panneau de configuration → Variables d'environnement → nouvelle
  variable système `JAVA_HOME` = chemin de ton JDK 17 (ex. `C:\Program Files\Java\jdk-17`),
  puis rouvrir le terminal. En dépannage ponctuel dans PowerShell :
  `$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"`.
- **macOS/Linux** : `export JAVA_HOME=$(/usr/libexec/java_home -v 17)` (macOS) ou le
  chemin de ton JDK 17 dans `~/.bashrc`/`~/.zshrc`.

Démarrer les services **dans cet ordre**, dans des terminaux séparés, depuis la racine
du projet :

```bash
# Windows (PowerShell / cmd)
.\mvnw.cmd -pl eureka-server spring-boot:run
.\mvnw.cmd -pl config-server spring-boot:run
.\mvnw.cmd -pl api-gateway spring-boot:run
.\mvnw.cmd -pl book-service spring-boot:run
.\mvnw.cmd -pl loan-service spring-boot:run

# macOS / Linux
./mvnw -pl eureka-server spring-boot:run
./mvnw -pl config-server spring-boot:run
./mvnw -pl api-gateway spring-boot:run
./mvnw -pl book-service spring-boot:run
./mvnw -pl loan-service spring-boot:run
```

(Si Maven est déjà installé globalement sur ta machine, `mvn` fonctionne aussi et
remplace `./mvnw` / `.\mvnw.cmd` dans toutes les commandes de ce README.)

Vérifier que les 5 services apparaissent dans Eureka : http://localhost:8761

### Option B — avec Docker Compose

Prérequis : Docker Desktop.

```bash
docker compose up --build
```

Démarre les 5 services. Les URLs Eureka/Config-server sont automatiquement
réécrites vers les noms de service Docker (`eureka-server`, `config-server`) via des
variables d'environnement dans `docker-compose.yml` — aucune modification des fichiers
`config-repo/*.yml` n'est nécessaire.

## Endpoints

Toutes les requêtes ci-dessous peuvent être envoyées directement aux services
(`localhost:8091` / `localhost:8092`) ou via la gateway (`localhost:8090`).

### book-service — `/api/books`

| Méthode | URL                              | Description                                                        |
|---------|-----------------------------------|----------------------------------------------------------------------|
| GET     | `/api/books`                     | Lister tous les livres                                              |
| GET     | `/api/books/{id}`                 | Récupérer un livre (404 si absent)                                  |
| POST    | `/api/books`                     | Créer un livre (`availableCopies` = `totalCopies`)                  |
| PUT     | `/api/books/{id}`                 | Mettre à jour un livre                                              |
| DELETE  | `/api/books/{id}`                 | Supprimer un livre                                                  |
| PATCH   | `/api/books/{id}/decrement-stock` | Décrémente `availableCopies` de 1 (409 si déjà à 0)                 |
| PATCH   | `/api/books/{id}/increment-stock` | Réincrémente `availableCopies` de 1 (plafonné à `totalCopies`)      |

### loan-service — `/api/loans`

| Méthode | URL                        | Description                                                    |
|---------|-----------------------------|------------------------------------------------------------------|
| GET     | `/api/loans`                | Lister tous les emprunts                                        |
| GET     | `/api/loans/{id}`            | Récupérer un emprunt (404 si absent)                             |
| POST    | `/api/loans`                | Créer un emprunt (400 si livre inexistant, 409 si stock épuisé) |
| PATCH   | `/api/loans/{id}/return`     | Marquer l'emprunt comme rendu (409 s'il est déjà `RETURNED`)     |

## Tests

```bash
./mvnw -pl book-service,loan-service -am test   # ou .\mvnw.cmd sous Windows
```

- `book-service` : tests unitaires (Mockito) sur `BookService`, tests d'intégration
  (MockMvc) sur `BookController`, couvrant en particulier le 409 lors de la
  décrémentation d'un stock déjà à 0.
- `loan-service` : tests unitaires (Mockito) sur `LoanService`, tests d'intégration
  (MockMvc) sur `LoanController`, couvrant le refus de création (livre inexistant → 400,
  stock épuisé → 409), le double retour d'un même emprunt (409), et le **cas de
  concurrence** : `book-service` renvoie 409 lors du `decrement-stock` alors que la
  vérification initiale (`GET`) avait indiqué un stock disponible.

## Collection de requêtes

Un fichier [`requests/library.http`](requests/library.http) (compatible avec
l'extension VS Code "REST Client" et le client HTTP d'IntelliJ) teste, via la
gateway : création d'un livre, création d'un emprunt réussi, création d'un emprunt
refusé (stock épuisé), création d'un emprunt sur un livre inexistant, retour d'un
emprunt, et tentative de rendre deux fois le même emprunt.
