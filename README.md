# 📬 Web Mail Application

A full-stack web-based email management system built with **Angular 20** (frontend) and **Spring Boot 4** (backend). This application provides a modern, feature-rich email experience with support for composing, organizing, and managing emails with an intuitive user interface.

---

## 📋 Table of Contents

- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Prerequisites](#-prerequisites)
- [Installation](#-installation)
- [Running the Application](#-running-the-application)
- [API Endpoints](#-api-endpoints)
- [Frontend Routes](#-frontend-routes)
- [Data Models](#-data-models)
- [License](#-license)

---

## ✨ Features

### 📧 Email Management
- **Compose emails** with support for multiple recipients, subject, body, and attachments
- **Priority levels** (1-5) for email categorization
- **Priority Inbox** - Automatically sorts emails by priority for focused email management
- **Drafts** - Save and edit email drafts before sending
- **Sent emails** - View all sent email history
- **Trash** - Soft delete with automatic cleanup scheduler
- **Mark as read/unread** - Track email read status with visual indicators

### 📁 Folder Management
- **Custom folders** - Create, rename, and delete personal folders
- **Move emails** - Organize emails by moving them between folders
- **Return to original folder** - Restore emails to their original location

### 🔍 Search & Filter
- **Full-text search** across inbox, sent, drafts, and folders
- **Advanced filtering** with multiple criteria support
- **Sorting options** (Date newest/oldest, Priority, Sender, Subject)
- **Pagination** for efficient data loading

### 👥 Contacts Management
- **Contact list** - Manage contacts with multiple email addresses per contact
- **Add/Edit/Delete contacts** - Full CRUD operations
- **Email validation** - Automatic email format validation

### 🔐 Authentication & Security
- **User registration** with security question (favorite movie)
- **User login/logout** with token-based session management
- **Password reset** - Secure password recovery via security question verification
- **Protected routes** - Authorization required for all email operations

### 🎨 User Interface
- **Modern dark/light mode theming** with CSS variables
- **Responsive design** for desktop and mobile
- **Toast notifications** for user feedback
- **Real-time UI updates** for email status changes

---

## 🛠 Tech Stack

### Frontend
| Technology | Version | Purpose |
|------------|---------|---------|
| Angular | 20.3.x | Frontend framework |
| TypeScript | 5.9.x | Typed JavaScript |
| RxJS | 7.8.x | Reactive programming |
| CSS Variables | - | Dynamic theming |

### Backend
| Technology | Version | Purpose |
|------------|---------|---------|
| Spring Boot | 4.0.0 | Backend framework |
| Java | 21+ | Programming language |
| Lombok | - | Boilerplate reduction |
| Jackson | 2.15.2 | JSON processing |
| Spring Security | - | Authentication & authorization |
| Maven | - | Dependency management |

### Data Storage
- **File-based storage** using JSON files (no database required)
- Separate data files per user for email, folders, and contacts

---

## 📁 Project Structure

```
web-mail/
├── backend/                          # Spring Boot backend
│   ├── src/main/java/com/mailSystem/demo/
│   │   ├── config/                   # Configuration classes
│   │   ├── controller/               # REST API controllers
│   │   │   ├── AccountController     # Authentication endpoints
│   │   │   ├── AttachmentController  # File attachment handling
│   │   │   ├── ContactController     # Contact management
│   │   │   ├── DraftController       # Draft email operations
│   │   │   ├── FilterController      # Email filtering
│   │   │   ├── FolderController      # Folder management
│   │   │   ├── InboxController       # Inbox operations
│   │   │   ├── PriorityInboxController # Priority-based inbox
│   │   │   ├── SearchController      # Email search
│   │   │   ├── SendController        # Send email operations
│   │   │   └── TrashController       # Trash management
│   │   ├── dal/                      # Data Access Layer
│   │   ├── dto/                      # Data Transfer Objects
│   │   ├── model/                    # Domain models
│   │   │   ├── Contact.java          # Contact entity
│   │   │   ├── EmailFilter.java      # Filter configuration
│   │   │   ├── FilterType.java       # Filter type enum
│   │   │   ├── Mail.java             # Email entity
│   │   │   └── User.java             # User entity
│   │   ├── service/                  # Business logic services
│   │   │   ├── Filter/               # Filter strategies
│   │   │   ├── search/               # Search implementations
│   │   │   └── sort/                 # Sort implementations
│   │   └── utils/                    # Utility classes
│   └── pom.xml                       # Maven dependencies
│
├── frontend/                         # Angular frontend
│   ├── src/app/
│   │   ├── core/                     # Core module
│   │   │   ├── guards/               # Route guards
│   │   │   ├── interceptors/         # HTTP interceptors
│   │   │   └── services/             # Core services (API, Auth, Storage, Toast)
│   │   ├── features/                 # Feature modules
│   │   │   ├── auth/                 # Authentication
│   │   │   │   ├── Signin/           # Login component
│   │   │   │   ├── Signup/           # Registration component
│   │   │   │   └── ForgotPassword/   # Password reset component
│   │   │   ├── dashboard/            # Dashboard views
│   │   │   └── mail/                 # Mail feature module
│   │   │       └── components/
│   │   │           ├── compose/      # Email composer
│   │   │           ├── contacts/     # Contacts manager
│   │   │           ├── drafts/       # Drafts view
│   │   │           ├── EmailDisplay/ # Email reader
│   │   │           ├── folders/      # Folder list
│   │   │           ├── inbox/        # Inbox view
│   │   │           ├── PriorityInbox/ # Priority inbox view
│   │   │           ├── sent/         # Sent emails view
│   │   │           ├── sidebar/      # Navigation sidebar
│   │   │           ├── toast/        # Toast notifications
│   │   │           ├── trash/        # Trash view
│   │   │           └── userfoldersview/ # Custom folder view
│   │   ├── services/                 # Feature services
│   │   └── shared/                   # Shared components
│   ├── angular.json                  # Angular configuration
│   ├── package.json                  # NPM dependencies
│   └── tsconfig.json                 # TypeScript configuration
│
├── package.json                      # Root package.json
└── README.md                         # This file
```

---

## 📋 Prerequisites

Before running the application, ensure you have the following installed:

- **Node.js** (v18 or higher) - [Download](https://nodejs.org/)
- **npm** (comes with Node.js)
- **Java JDK 21** or higher - [Download](https://adoptium.net/)
- **Maven** (or use the included Maven wrapper) - [Download](https://maven.apache.org/)

---

## 🚀 Installation

### 1. Clone the Repository

```bash
git clone https://github.com/Mohamedahmed716/web-mail.git
cd web-mail
```

### 2. Backend Setup

```bash
cd backend

# Using Maven wrapper (recommended)
./mvnw clean install

# Or using installed Maven
mvn clean install
```

### 3. Frontend Setup

```bash
cd frontend

# Install dependencies
npm install
```

---

## ▶️ Running the Application

### Start the Backend Server

```bash
cd backend

# Using Maven wrapper
./mvnw spring-boot:run

# Or using Maven
mvn spring-boot:run
```

The backend server will start at: `http://localhost:8080`

### Start the Frontend Development Server

```bash
cd frontend

# Start Angular dev server
npm start
# or
ng serve
```

The frontend will be available at: `http://localhost:4200`

---

## 🔌 API Endpoints

### Authentication (`/api/auth`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/login` | User login |
| POST | `/signup` | User registration |
| POST | `/logout` | User logout |
| GET | `/users` | Get all users |
| POST | `/verify-email` | Verify email exists (password reset step 1) |
| POST | `/verify-security-question` | Verify security answer (password reset step 2) |
| POST | `/reset-password` | Reset password (password reset step 3) |

### Inbox (`/api/inbox`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Get inbox emails with pagination & sorting |
| GET | `/search` | Search inbox emails |
| POST | `/filter` | Filter inbox with criteria |
| PUT | `/{mailId}/read` | Mark email as read |

### Priority Inbox (`/api/priority-inbox`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Get priority-sorted inbox |
| GET | `/search` | Search priority inbox |
| POST | `/filter` | Filter priority inbox |
| PUT | `/{mailId}/read` | Mark email as read |

### Send (`/api/send`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/sendEmail` | Send an email (multipart/form-data) |
| GET | `/` | Get sent emails with pagination |
| GET | `/loadSent` | Load all sent emails |
| GET | `/search` | Search sent emails |
| POST | `/filter` | Filter sent emails |

### Drafts (`/api/draft`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/saveDraft` | Save a draft (multipart/form-data) |
| GET | `/` | Get drafts with pagination |
| GET | `/loadDrafts` | Load all drafts |
| GET | `/search` | Search drafts |
| POST | `/filter` | Filter drafts |

### Folders (`/api/folders`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Get all user folders |
| GET | `/{folderName}` | Get emails in a folder |
| POST | `/` | Create a new folder |
| PUT | `/{oldName}` | Rename a folder |
| DELETE | `/{folderName}` | Delete a folder |
| POST | `/move/{mailId}/{targetFolder}` | Move email to folder |
| POST | `/return/{mailId}` | Return email to original folder |

### Trash (`/api/trash`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Get trash emails with pagination |
| POST | `/restore/{mailId}` | Restore email from trash |
| DELETE | `/{mailId}` | Permanently delete email |
| DELETE | `/empty` | Empty trash |

### Contacts (`/api/contacts`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Get all contacts |
| POST | `/` | Create a contact |
| PUT | `/{contactId}` | Update a contact |
| DELETE | `/{contactId}` | Delete a contact |

### Attachments (`/api/attachments`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/download/{filename}` | Download an attachment |

---

## 🗺 Frontend Routes

| Path | Component | Description |
|------|-----------|-------------|
| `/` | - | Redirects to `/signin` |
| `/signin` | SigninComponent | User login page |
| `/signup` | SignupComponent | User registration page |
| `/forgot-password` | ForgotPasswordComponent | Password reset page |
| `/mail` | Mail | Mail shell with sidebar |
| `/mail/inbox` | Inbox | Regular inbox view |
| `/mail/priority-inbox` | PriorityInboxComponent | Priority-sorted inbox |
| `/mail/drafts` | Drafts | Draft emails view |
| `/mail/sent` | Sent | Sent emails view |
| `/mail/trash` | Trash | Deleted emails view |
| `/mail/folders` | FolderListComponent | Custom folders list |
| `/mail/userfoldersview` | UserFoldersView | View emails in custom folder |
| `/mail/contacts` | ContactsComponent | Contacts management |

---

## 📊 Data Models

### User
```java
{
  "id": "UUID",
  "name": "string",
  "email": "string",
  "password": "string",
  "favoriteMovie": "string"  // Security question answer
}
```

### Mail
```java
{
  "id": "string",
  "sender": "string",
  "receivers": ["string"],
  "subject": "string",
  "body": "string",
  "timestamp": "Date",
  "priority": 1-5,           // 1=Low, 5=Critical
  "attachmentNames": ["string"],
  "folder": "string",        // Inbox, Sent, Trash, etc.
  "trashEntryDate": "Date",
  "parentFolder": "string",
  "isRead": boolean,
  "firstFolder": "string"
}
```

### Contact
```java
{
  "id": "string",
  "userId": "string",
  "name": "string",
  "emails": ["string"]       // Set of email addresses
}
```

---

## 🔧 Configuration

### Backend Configuration

The backend uses Spring Boot's default configuration. To customize:

1. Create/edit `backend/src/main/resources/application.properties`
2. Common configurations:
   ```properties
   server.port=8080
   spring.servlet.multipart.max-file-size=10MB
   spring.servlet.multipart.max-request-size=10MB
   ```

### Frontend Configuration

The frontend can be configured via:

1. `frontend/angular.json` - Angular build configuration
2. Environment files in `frontend/src/environments/`

---

## 🧪 Testing

### Backend Tests
```bash
cd backend
./mvnw test
```

### Frontend Tests
```bash
cd frontend
npm test
```

---

## 📝 Scripts

### Frontend (`frontend/package.json`)

| Script | Command | Description |
|--------|---------|-------------|
| `start` | `ng serve` | Start development server |
| `build` | `ng build` | Build for production |
| `watch` | `ng build --watch` | Build in watch mode |
| `test` | `ng test` | Run unit tests |

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).

---

## � Contributors

Meet the team behind this project:

| Contributor | GitHub Profile |
|-------------|----------------|
| **Mohamed Ahmed** | [![GitHub](https://img.shields.io/badge/-@Mohamedahmed716-181717?style=flat&logo=github)](https://github.com/Mohamedahmed716) |
| **Nour Eldin** | [![GitHub](https://img.shields.io/badge/-@noureldin75-181717?style=flat&logo=github)](https://github.com/noureldin75) |
| **Ali Emr** | [![GitHub](https://img.shields.io/badge/-@3li--3mr-181717?style=flat&logo=github)](https://github.com/3li-3mr) |
| **Omar Assem** | [![GitHub](https://img.shields.io/badge/-@omarassem--1-181717?style=flat&logo=github)](https://github.com/omarassem-1) |
| **Abdelrhman Abouf** | [![GitHub](https://img.shields.io/badge/-@abdelrhmanaboouf-181717?style=flat&logo=github)](https://github.com/abdelrhmanaboouf) |

---

## 🙏 Acknowledgments

- Angular team for the amazing framework
- Spring Boot team for the robust backend framework
- All contributors who have helped improve this project
