# Reqstrata — Enterprise Software Lifecycle & Bidirectional Traceability Platform

[![Android Platform](https://img.shields.io/badge/Platform-Android%2024%2B%20(API%2036)-3DDC84.svg?style=flat&logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF.svg?style=flat&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20(Material3)-4285F4.svg?style=flat&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Database](https://img.shields.io/badge/Database-Room%20(SQLite)%20%2B%20MongoDB%20Atlas-47A248.svg?style=flat&logo=mongodb&logoColor=white)](https://www.mongodb.com/atlas)
[![AI Integration](https://img.shields.io/badge/AI-Google%20Gemini%20(1.5%20%2F%203.x)-4285F4.svg?style=flat&logo=googlegemini&logoColor=white)](https://ai.google.dev)
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20Architecture%20%2B%20MVI%2FMVVM-orange.svg?style=flat)](https://developer.android.com/topic/architecture)
[![Automated Tests](https://img.shields.io/badge/Tests-48%20Passed%20(Robolectric%20SDK%2034)-brightgreen.svg?style=flat)](https://robolectric.org)

**Reqstrata** (`com.theoriongd.reqstrata`, version `1.0.1`, build `2`) is an enterprise-grade Android software engineering platform designed to eliminate the architectural disconnect between business requirements, system specifications, database schemas, REST APIs, engineering task tracking, and quality assurance testing.

By unifying **Role-Based Access Control (RBAC)** across five engineering personas, **Google Gemini Generative AI synthesis**, a **Dual-Engine Persistence Layer** (Local Room SQLite + MongoDB Atlas Stitch Cloud Sync), a **Mathematical 3D Cyberpunk Möbius Ribbon Visualizer**, and **Automated Bidirectional Traceability (RTM)**, Reqstrata provides an end-to-end environment where changes to a single requirement instantly propagate across architectural diagrams, database entities, API contracts, task queues, and automated test suites.

---

## Table of Contents

1. [Executive Summary & Problem Statement](#executive-summary--problem-statement)
2. [System Architecture & Design Paradigms](#system-architecture--design-paradigms)
3. [Role-Based Access Control (RBAC) & Governance Matrix](#role-based-access-control-rbac--governance-matrix)
4. [Master Application Screen Catalog (All 42 Screens)](#master-application-screen-catalog-all-42-screens)
5. [Continuous 3D Cyberpunk Möbius Visual & Motion Engine](#continuous-3d-cyberpunk-m%C3%B6bius-visual--motion-engine)
6. [Artificial Intelligence Engine (Google Gemini Multi-Model Cascade)](#artificial-intelligence-engine-google-gemini-multi-model-cascade)
7. [Local Database Schema & Data Access Layer (Room SQLite)](#local-database-schema--data-access-layer-room-sqlite)
8. [Cloud Database & Real-Time Background Synchronization (MongoDB Atlas)](#cloud-database--real-time-background-synchronization-mongodb-atlas)
9. [Bidirectional Traceability Matrix & Change Impact Analysis Engine](#bidirectional-traceability-matrix--change-impact-analysis-engine)
10. [End-to-End Persona Lifecycle Walkthrough](#end-to-end-persona-lifecycle-walkthrough)
11. [High-Volume Concurrency & Stress Testing Analysis](#high-volume-concurrency--stress-testing-analysis)
12. [Global State Management & ViewModel Architecture](#global-state-management--viewmodel-architecture)
13. [Type Converters & Serialization Infrastructure](#type-converters--serialization-infrastructure)
14. [ProGuard, R8 & Build Optimization Rules](#proguard-r8--build-optimization-rules)
15. [Complete Technology Stack & Dependency Catalog](#complete-technology-stack--dependency-catalog)
16. [Repository Directory Layout](#repository-directory-layout)
17. [Automated Testing & Verification Suite](#automated-testing--verification-suite)
18. [Build, Configuration & Environment Setup](#build-configuration--environment-setup)
19. [Security, Governance & Multi-Tenant Data Isolation](#security-governance--multi-tenant-data-isolation)

---

## Executive Summary & Problem Statement

Modern software delivery regularly suffers from an engineering disconnect between functional requirements and production implementations. Specifications authored by business analysts often languish in word documents, while developers build API routes and database schemas that drift from original stakeholder intent. Concurrently, QA engineers write test suites without direct linkage to acceptance criteria, making it impossible to determine test coverage or the blast radius of a change request.

**Reqstrata** solves this fragmentation through an integrated software lifecycle pipeline:

1. **Structured Synthesis**: Product concepts are transformed into formal requirements with user stories, acceptance criteria, and quality metrics using Google Gemini AI models.
2. **Architectural Derivation**: Requirements automatically feed Clean Architecture component decompositions, interactive UML diagrams, relational/document database schemas, and RESTful API contracts.
3. **Task & Implementation Linking**: Developers receive implementation tasks pre-linked to specific architectural components, database tables, and API schemas.
4. **Verification & Quality Assurance**: QA testers generate test suites directly from acceptance criteria, execute test runs, and record pass/fail statuses.
5. **Bidirectional Traceability**: The Requirements Traceability Matrix (RTM) maintains forward and backward links across the entire lifecycle, powering automated change impact analysis when requirements are updated.

---

## System Architecture & Design Paradigms

Reqstrata is engineered following strict **Clean Architecture** and **Unidirectional Data Flow (UDF / MVI-MVVM)** principles. The codebase is decoupled into four primary layers:

```mermaid
graph TD
    subgraph UI_Layer [Presentation Layer — Jetpack Compose & Material 3]
        Screens[42 Application Screens & Dynamic Graph]
        VisualEngine[3D Cyberpunk Möbius Visual Engine]
        NavGuards[Route Authorization Interceptor / AuthorizedRoute]
        VM[MainViewModel — StateFlow & UDF Dispatcher]
    end

    subgraph Domain_Layer [Domain & Governance Layer]
        RBAC[ProjectAccessPolicy — 5 Roles x 16 Modules]
        AuthManager[CentralizedAuthorizationManager]
        DomainModels[Domain Entities, Enums & Status Models]
        Exceptions[Security Violations & Boundary Guards]
    end

    subgraph Data_Layer [Data & Persistence Layer]
        Repos[12 Core Repositories]
        RoomDB[Local Room SQLite DB — 20 Entities / 14 DAOs]
        MongoStitch[MongoDB Stitch HTTP Client — 16 Collections]
        SyncService[MongoBackgroundSyncService — FIFO Queue & ChangeStream]
        GeminiService[GeminiApiClient & GeminiRetrofitClient — Multi-Model Cascade]
    end

    subgraph Cloud_Infrastructure [External Cloud & Hardware Infrastructure]
        Atlas[(MongoDB Atlas Cloud Cluster)]
        GeminiCloud[Google Generative Language API v1beta]
        AndroidOS[Android Runtime API 24–36]
    end

    Screens --> NavGuards
    NavGuards --> VM
    VisualEngine --> Screens
    VM --> Repos
    VM --> RBAC
    VM --> AuthManager
    Repos --> RoomDB
    Repos --> MongoStitch
    Repos --> GeminiService
    MongoStitch --> SyncService
    SyncService --> Atlas
    GeminiService --> GeminiCloud
    RoomDB --> AndroidOS
```

### Architectural Principles

1. **Unidirectional Data Flow (UDF)**: The presentation layer observes read-only `StateFlow` instances (`currentUser`, `currentProject`, `currentRole`, `requirements`, `components`, `tasks`, `testSuites`). Mutations are dispatched through suspend functions that execute persistence operations before updating in-memory state.
2. **Offline-First Persistence**: Mutations are written immediately to the local Room database (`reqstrata.db`), guaranteeing responsive interactions regardless of network quality. An asynchronous queue in `MongoBackgroundSyncService` mirrors updates to the MongoDB Atlas cluster.
3. **Decoupled Governance & Security**: Business logic and navigation are guarded by `CentralizedAuthorizationManager` and `ProjectAccessPolicy`. Data manipulations that breach privilege boundaries throw a typed `UnauthorizedDataAccessException`, while unauthorized navigation triggers `UnauthorizedNavigationException` and presents a dedicated boundary screen.
4. **Deterministic Fallbacks**: External dependencies (Gemini AI generation, MongoDB Atlas cluster ping) implement deterministic, rule-based offline synthetic engines, ensuring full functionality and passing tests even in air-gapped environments.

---

## Role-Based Access Control (RBAC) & Governance Matrix

Access control in Reqstrata is **project-scoped**. A user can hold the `ARCHITECT` role in one project while acting as a `DEVELOPER` in another.

### The 5 Project Roles

1. **`ADMIN` (Project Owner & Administrator)**: Full project governance, member invitations, role management, approval center sign-offs, and activity audit inspection.
2. **`BUSINESS_ANALYST` (Business Analyst)**: Requirements authoring, acceptance criteria formalization, AI ambiguity analysis, use case specification, and backlog refinement.
3. **`ARCHITECT` (System Architect)**: System component hierarchy, UML diagramming, database schema design (ERD), REST API specification, and Architecture Decision Records (ADRs).
4. **`DEVELOPER` (Software Developer)**: Implementation task execution (Kanban), code artifact linking, API schema consumption, and Developer AI Copilot assistance.
5. **`TESTER` (QA Engineer / Tester)**: Test suite and test case design, execution recording (Passed/Failed/Blocked), coverage analysis, and Change Impact analysis.

### The 16 Functional Modules

* `PROJECT_MANAGEMENT`: Project creation, metadata modification, and workspace archiving.
* `TEAM_MANAGEMENT`: Project member directory, invitations, and member removal.
* `REQUIREMENTS`: Functional and non-functional requirement authoring and version control.
* `USE_CASES`: Actor workflows, preconditions, main success scenarios, and extensions.
* `ARCHITECTURE`: Clean architecture module decomposition and component registry.
* `UML`: Interactive UML diagrams (Class, Sequence, Component, ERD, Activity, Use Case).
* `DATABASE`: Relational and document entity modeling, column definitions, and foreign keys.
* `APIS`: RESTful endpoint contracts, HTTP methods, request/response JSON schemas, and auth flags.
* `DEVELOPMENT_TASKS`: Kanban task tracking (`BACKLOG`, `READY`, `IN_PROGRESS`, `BLOCKED`, `IN_REVIEW`, `COMPLETED`).
* `TEST_SUITES`: Logical test collections categorized by type (Unit, Integration, E2E, Regression).
* `TEST_EXECUTION`: Interactive test runners recording actual results against expected outcomes.
* `TRACEABILITY`: Bidirectional Requirements Traceability Matrix (RTM) generation.
* `CHANGE_IMPACT`: Automated blast-radius analysis evaluating downstream ripple effects of requirement modifications.
* `DOCUMENTATION`: Live Markdown document synthesis (SRS, SDD, API contracts, Test Plans, RTM).
* `AUDIT_LOGS`: Immutable chronological ledger of every mutation across the workspace.
* `ROLE_MANAGEMENT`: Administrative permission assignment and member role reassignment.

### Master RBAC Permission Matrix

The table below reflects the exact permission boundaries implemented in `ProjectAccessPolicy.kt`:

| Project Module | `ADMIN` | `BUSINESS_ANALYST` | `ARCHITECT` | `DEVELOPER` | `TESTER` |
| :--- | :---: | :---: | :---: | :---: | :---: |
| **`PROJECT_MANAGEMENT`** | **FULL** | VIEW | VIEW | VIEW | VIEW |
| **`TEAM_MANAGEMENT`** | **FULL** | NONE | NONE | NONE | NONE |
| **`REQUIREMENTS`** | **FULL** | **FULL** | VIEW | VIEW | VIEW |
| **`USE_CASES`** | **FULL** | **FULL** | VIEW | VIEW | VIEW |
| **`ARCHITECTURE`** | **FULL** | VIEW | **FULL** | VIEW | VIEW |
| **`UML`** | **FULL** | VIEW | **FULL** | VIEW | VIEW |
| **`DATABASE`** | **FULL** | VIEW | **FULL** | VIEW | VIEW |
| **`APIS`** | **FULL** | VIEW | **FULL** | VIEW | VIEW |
| **`DEVELOPMENT_TASKS`** | **FULL** | VIEW | VIEW | **FULL** | VIEW |
| **`TEST_SUITES`** | **FULL** | VIEW | VIEW | VIEW | **FULL** |
| **`TEST_EXECUTION`** | **FULL** | NONE | NONE | VIEW | **FULL** |
| **`TRACEABILITY`** | **FULL** | VIEW | **FULL** | VIEW | **FULL** |
| **`CHANGE_IMPACT`** | **FULL** | VIEW | **FULL** | VIEW | **FULL** |
| **`DOCUMENTATION`** | **FULL** | **FULL** | **FULL** | **FULL** | **FULL** |
| **`AUDIT_LOGS`** | **FULL** | NONE | NONE | NONE | NONE |
| **`ROLE_MANAGEMENT`** | **FULL** | NONE | NONE | NONE | NONE |

* **`FULL`**: Unrestricted creation, editing, status transition, and deletion rights.
* **`VIEW`**: Read-only collaborative visibility into specifications and diagrams.
* **`NONE`**: Absolute access restriction. Route navigation is intercepted by `AuthorizedRoute` and displays the `RoleRestrictedAccessScreen`.

---

## Master Application Screen Catalog (All 42 Screens)

Reqstrata includes **42 dedicated screen destinations**, interconnected via centralized route constants in `AppRoutes.kt` and assembled at runtime in `DynamicNavGraphBuilder.kt`.

```
========================================================================================
REQSTRATA SCREEN TOPOLOGY MAP
========================================================================================

[ Public & Auth Ecosystem ]
  ├── Splash Screen                      (Route: "splash")
  ├── Onboarding Screen                  (Route: "onboarding")
  ├── Login Screen                       (Route: "login")
  ├── Register Screen                    (Route: "register")
  ├── Tenant Separation Register         (Route: "tenant_separation_register")
  ├── Forgot Password Screen             (Route: "forgot_password")
  ├── Email Verification Screen          (Route: "email_verification?email={email}")
  └── Reset Password Screen              (Route: "reset_password?email={email}")

[ Universal Collaborative Workspace ]
  ├── Project Selection Screen           (Route: "project_selection")
  ├── Project Dashboard Screen           (Route: "project_dashboard")
  ├── Project Overview Screen            (Route: "project_overview")
  ├── Requirements List Screen           (Route: "requirements_list")
  ├── Requirement Detail Screen          (Route: "requirement_detail/{reqId}")
  ├── Requirement Comparison Screen      (Route: "requirement_comparison/{reqId}?v1={v1}&v2={v2}")
  ├── Requirement AI Analysis Screen     (Route: "requirement_ai_analysis/{reqId}")
  ├── Use Cases Screen                   (Route: "use_cases")
  ├── Use Case Detail Screen             (Route: "use_case_detail/{useCaseId}")
  ├── Traceability Matrix Screen (RTM)   (Route: "traceability_matrix")
  ├── Engineering Documentation Screen   (Route: "document_viewer")
  ├── Global Search Screen               (Route: "global_search")
  ├── Notifications Center Screen        (Route: "notifications")
  ├── Activity Log Screen                (Route: "activity_log")
  ├── User Profile Screen                (Route: "profile")
  └── App Settings Screen                (Route: "settings")

[ Administrator Governance Hub ]
  ├── Admin Dashboard Screen             (Route: "admin_dashboard")
  ├── Create Project Screen              (Route: "create_project")
  ├── Project Settings Screen            (Route: "project_settings")
  ├── Role Governance Screen             (Route: "role_management")
  ├── Invite Member Screen               (Route: "invite_member")
  ├── Team Management Screen             (Route: "team_management")
  └── Member Detail Screen               (Route: "member_detail/{memberId}")

[ Business Analyst Engineering Hub ]
  ├── Business Analyst Dashboard Screen  (Route: "ba_dashboard")
  ├── New Requirement Form Screen        (Route: "new_requirement_form")
  ├── Create / Edit Requirement Screen   (Route: "create_edit_requirement?reqId={reqId}")
  ├── AI Requirements Generator Screen   (Route: "generate_requirements?projectId={projectId}")
  └── Approval Center Screen             (Route: "approval_center")

[ System Architect Studio Hub ]
  ├── Architect Dashboard Screen         (Route: "architect_dashboard")
  ├── Architecture Workspace Screen      (Route: "architecture_workspace")
  ├── Component Detail Screen            (Route: "architecture_component_detail/{componentId}")
  ├── Architecture Decisions Screen(ADR) (Route: "architecture_decisions")
  ├── UML Studio Screen                  (Route: "uml_studio")
  ├── Database Designer Screen (ERD)     (Route: "database_designer")
  ├── API Designer Screen                (Route: "api_designer")
  ├── AI Architecture Synthesizer Screen (Route: "suggest_architecture?projectId={projectId}")
  └── System Design AI Chat Screen       (Route: "project_ai_assistant")

[ Software Developer Workspace Hub ]
  ├── Developer Dashboard Screen         (Route: "developer_dashboard")
  ├── Task Management Screen (Kanban)    (Route: "task_management")
  └── Developer AI Assistant Screen      (Route: "developer_ai_assistant")

[ QA Engineer / Tester Workspace Hub ]
  ├── Tester Dashboard Screen            (Route: "tester_dashboard")
  ├── Testing Workspace Screen           (Route: "testing_workspace")
  ├── Test Execution Workspace Screen    (Route: "test_execution_workspace")
  ├── Coverage Dashboard Screen          (Route: "coverage_dashboard")
  ├── AI Test Suite Generator Screen     (Route: "generate_test_suite?projectId={projectId}")
  └── Change Impact Dashboard Screen     (Route: "change_impact?reqId={reqId}")
========================================================================================
```

### Detailed Screen Catalog & Route Specifications

| # | Screen Name | Source File | Route Constant | Access Level | Description |
|---|---|---|---|---|---|
| 1 | **Splash Screen** | `ui/screens/auth/SplashScreen.kt` | `SPLASH` | Public | Bootstrapper, tenant detector, and animated 3D Möbius space background. |
| 2 | **Onboarding Screen** | `ui/screens/auth/AuthScreens.kt` | `ONBOARDING` | Public | Features the interactive 3D Continuous Möbius Ribbon Hero visual. |
| 3 | **Login Screen** | `ui/screens/auth/AuthScreens.kt` | `LOGIN` | Public | Dual-engine authentication verifying against Room and MongoDB Atlas. |
| 4 | **Register Screen** | `ui/screens/auth/AuthScreens.kt` | `REGISTER` | Public | Account creation, default tenant provisioning, and initial role configuration. |
| 5 | **Tenant Separation Register** | `ui/screens/auth/AuthScreens.kt` | `TENANT_SEPARATION_REGISTER` | Public | Enterprise onboarding isolating organization-specific workspaces. |
| 6 | **Forgot Password Screen** | `ui/screens/auth/AuthScreens.kt` | `FORGOT_PASSWORD` | Public | Self-service password recovery flow with email verification triggering. |
| 7 | **Email Verification Screen** | `ui/screens/auth/EmailVerificationScreen.kt` | `EMAIL_VERIFICATION` | Public | Verification token entry enforcing identity confirmation before access. |
| 8 | **Reset Password Screen** | `ui/screens/auth/ResetPasswordScreen.kt` | `RESET_PASSWORD` | Public | Secure credential update and reset token confirmation. |
| 9 | **Project Selection Screen** | `ui/screens/project/MainDashboardProjectsScreen.kt` | `PROJECT_SELECTION` | All Authenticated | Multi-project workspace selector with search and active tenant context. |
| 10 | **Project Dashboard Screen** | `ui/screens/project/ProjectDashboardScreen.kt` | `PROJECT_DASHBOARD` | All Authenticated | Executive KPI metrics, role-specific action tiles, and health status. |
| 11 | **Project Overview Screen** | `ui/screens/project/ProjectOverviewScreen.kt` | `PROJECT_OVERVIEW` | All Authenticated | Project metadata, cluster connection state, live ping tests, and spec export. |
| 12 | **Requirements List Screen** | `ui/screens/requirements/RequirementsScreens.kt` | `REQUIREMENTS_LIST` | All Authenticated | Filterable requirement catalog by priority, status, and category. |
| 13 | **Requirement Detail Screen** | `ui/screens/requirements/RequirementsScreens.kt` | `REQUIREMENT_DETAIL` | All Authenticated | Specification inspector with acceptance criteria and revision history. |
| 14 | **Requirement Comparison Screen** | `ui/screens/requirements/RequirementComparisonScreen.kt` | `REQUIREMENT_COMPARISON` | All Authenticated | Visual side-by-side diff comparing two versions of a requirement. |
| 15 | **Requirement AI Analysis Screen** | `ui/screens/requirements/RequirementAiAnalysisScreen.kt` | `REQUIREMENT_AI_ANALYSIS` | All Authenticated | Gemini-driven audit for ambiguity, completeness, and testability. |
| 16 | **Use Cases Screen** | `ui/screens/requirements/RequirementsScreens.kt` | `USE_CASES` | All Authenticated | Catalog of business use cases, actor interactions, and trigger conditions. |
| 17 | **Use Case Detail Screen** | `ui/screens/requirements/UseCaseDetailScreen.kt` | `USE_CASE_DETAIL` | All Authenticated | Step-by-step main success scenario, alternative branches, and preconditions. |
| 18 | **Traceability Matrix Screen (RTM)**| `ui/screens/traceability/TraceabilityScreens.kt` | `TRACEABILITY_MATRIX` | All Authenticated | Full RTM mapping Requirements $\to$ Components $\to$ Endpoints $\to$ Tasks $\to$ Tests. |
| 19 | **Engineering Documentation Screen**| `ui/screens/documents/DocumentScreens.kt` | `DOCUMENT_VIEWER` | All Authenticated | Live Markdown generator for SRS, SDD, API Contracts, and Test Plans. |
| 20 | **Global Search Screen** | `ui/screens/team/TeamAndActivityScreens.kt` | `GLOBAL_SEARCH` | All Authenticated | Full-text cross-module indexing spanning requirements, architecture, and tests. |
| 21 | **Notifications Center Screen** | `ui/screens/team/TeamAndActivityScreens.kt` | `NOTIFICATIONS` | All Authenticated | In-app real-time notification inbox with read state and priority badges. |
| 22 | **Activity Log Screen** | `ui/screens/team/TeamAndActivityScreens.kt` | `ACTIVITY_LOG` | All Authenticated | Chronological audit ledger tracking user actions and timestamped mutations. |
| 23 | **User Profile Screen** | `ui/screens/profile/ProfileAndSettingsScreens.kt` | `PROFILE` | All Authenticated | User account details, active tenant card, and project role permissions. |
| 24 | **App Settings Screen** | `ui/screens/profile/ProfileAndSettingsScreens.kt` | `SETTINGS` | All Authenticated | Dark mode toggle, reduce-motion switch, haptics, and cluster diagnostics. |
| 25 | **Admin Dashboard Screen** | `ui/screens/project/AdminDashboardScreen.kt` | `ADMIN_DASHBOARD` | `ADMIN` | Executive project health, cluster sync status, member count, and velocity. |
| 26 | **Create Project Screen** | `ui/screens/project/ProjectScreens.kt` | `CREATE_PROJECT` | `ADMIN` | New project wizard setting domain, architecture style, stack, and methodology. |
| 27 | **Project Settings Screen** | `ui/screens/project/ProjectSettingsScreen.kt` | `PROJECT_SETTINGS` | `ADMIN` | General workspace configuration, archiving, and permanent deletion. |
| 28 | **Role Governance Screen** | `ui/screens/team/RoleManagementScreen.kt` | `ROLE_MANAGEMENT` | `ADMIN` | Member authority audit, permission modification, and administrative assignment. |
| 29 | **Invite Member Screen** | `ui/screens/team/InviteMemberScreen.kt` | `INVITE_MEMBER` | `ADMIN` | Send project invitations with predefined role scoping. |
| 30 | **Team Management Screen** | `ui/screens/team/TeamAndActivityScreens.kt` | `TEAM_MANAGEMENT` | `ADMIN`, `BA` | Team member directory, activity status, and member administrative actions. |
| 31 | **Member Detail Screen** | `ui/screens/team/MemberDetailScreen.kt` | `MEMBER_DETAIL` | `ADMIN` (Write) / All | Member profile inspection, assigned role reassignment, and project removal. |
| 32 | **Business Analyst Dashboard** | `ui/screens/requirements/BusinessAnalystDashboardScreen.kt` | `BA_DASHBOARD` | `BA`, `ADMIN` | Requirements velocity, backlog breakdown by priority, and approval status. |
| 33 | **New Requirement Form Screen** | `ui/screens/requirements/NewRequirementFormScreen.kt` | `NEW_REQUIREMENT_FORM` | `BA`, `ADMIN` | Streamlined requirement creation with acceptance criteria builder. |
| 34 | **Create / Edit Requirement Screen**| `ui/screens/requirements/RequirementsScreens.kt` | `CREATE_EDIT_REQUIREMENT` | `BA`, `ADMIN` | Full version-controlled requirement authoring form with revision tracking. |
| 35 | **AI Requirements Generator** | `ui/screens/requirements/GenerateRequirementsScreen.kt` | `GENERATE_REQUIREMENTS` | `BA`, `ADMIN` | AI specification synthesizer transforming raw ideas into structured user stories. |
| 36 | **Approval Center Screen** | `ui/screens/project/ApprovalCenterScreen.kt` | `APPROVAL_CENTER` | `BA`, `ARCH`, `ADMIN`| Formal sign-off queue for approving, rejecting, or revising specifications. |
| 37 | **Architect Dashboard Screen** | `ui/screens/architecture/ArchitectDashboardScreen.kt` | `ARCHITECT_DASHBOARD` | `ARCHITECT`, `ADMIN` | Architectural completeness score, layer distribution, and component warnings. |
| 38 | **Architecture Workspace Screen** | `ui/screens/architecture/ArchitectureScreens.kt` | `ARCHITECTURE_WORKSPACE` | `ARCHITECT`, `ADMIN` | Clean Architecture layer explorer (Presentation, Domain, Data, Infrastructure).|
| 39 | **Component Detail Screen** | `ui/screens/architecture/ArchitectureComponentDetailScreen.kt` | `ARCHITECTURE_COMPONENT_DETAIL`| `ARCHITECT`, `ADMIN` | Component specifications, dependencies, linked requirements, and tech stack. |
| 40 | **Architecture Decisions (ADR)** | `ui/screens/architecture/ArchitectureDecisionsScreen.kt` | `ARCHITECTURE_DECISIONS` | `ARCHITECT`, `ADMIN` | Architecture Decision Records (Context, Decision, Consequences, Status). |
| 41 | **UML Studio Screen** | `ui/screens/architecture/ArchitectureScreens.kt` | `UML_STUDIO` | `ARCHITECT`, `ADMIN` | Interactive diagram visualizer (Class, Sequence, Component, ERD, Activity). |
| 42 | **Database Designer Screen (ERD)** | `ui/screens/design/DesignersScreens.kt` | `DATABASE_DESIGNER` | `ARCHITECT`, `ADMIN` | Entity modeling, schema attributes, and SQL/MongoDB migration generator. |
| 43 | **API Designer Screen** | `ui/screens/design/DesignersScreens.kt` | `API_DESIGNER` | `ARCHITECT`, `ADMIN` | RESTful endpoint designer with HTTP methods, paths, and JSON schemas. |
| 44 | **AI Architecture Synthesizer** | `ui/screens/architecture/SuggestArchitectureScreen.kt` | `SUGGEST_ARCHITECTURE` | `ARCHITECT`, `ADMIN` | Gemini synthesizer recommending layers, components, and trade-offs. |
| 45 | **System Design AI Chat Screen** | `ui/screens/ai/RequirementSystemDesignChatScreen.kt` | `PROJECT_AI_ASSISTANT` | `ARCHITECT`, `ADMIN` | Conversational system design assistant for scaling, caching, and trade-offs. |
| 46 | **Developer Dashboard Screen** | `ui/screens/tasks/DeveloperDashboardScreen.kt` | `DEVELOPER_DASHBOARD` | `DEVELOPER`, `ADMIN` | Personal task queue, assigned API endpoints, sprint deadlines, and req links. |
| 47 | **Task Management Screen** | `ui/screens/tasks/TaskManagementScreen.kt` | `TASK_MANAGEMENT` | `DEVELOPER`, `ADMIN` | Kanban board with state transitions (`TODO` $\to$ `IN_PROGRESS` $\to$ `DONE`). |
| 48 | **Developer AI Assistant Screen** | `ui/screens/ai/DeveloperAIAssistantScreen.kt` | `DEVELOPER_AI_ASSISTANT`| `DEVELOPER`, `ADMIN` | Gemini code generator producing Kotlin/SQL implementations from specs. |
| 49 | **Tester Dashboard Screen** | `ui/screens/testing/TesterDashboardScreen.kt` | `TESTER_DASHBOARD` | `TESTER`, `ADMIN` | Pass/Fail execution ratios, QA velocity, defect count, and run status. |
| 50 | **Testing Workspace Screen** | `ui/screens/testing/TestingWorkspaceScreen.kt` | `TESTING_WORKSPACE` | `TESTER`, `ADMIN` | Test suite and test case authoring with severity, preconditions, and steps. |
| 51 | **Test Execution Workspace Screen** | `ui/screens/testing/TestExecutionWorkspaceScreen.kt` | `TEST_EXECUTION_WORKSPACE`| `TESTER`, `ADMIN`| Step-by-step test runner recording actual results and status. |
| 52 | **Coverage Dashboard Screen** | `ui/screens/testing/CoverageDashboardScreen.kt` | `COVERAGE_DASHBOARD` | `TESTER`, `ADMIN` | Real-time requirement test coverage meter and uncovered test gap tracker. |
| 53 | **AI Test Suite Generator** | `ui/screens/testing/GenerateTestSuiteScreen.kt` | `GENERATE_TEST_SUITE` | `TESTER`, `ADMIN` | Automated synthesis of Unit and Integration test suites via Gemini. |
| 54 | **Change Impact Dashboard Screen** | `ui/screens/traceability/ChangeImpactDashboardScreen.kt` | `CHANGE_IMPACT` | `TESTER`, `ARCH`, `ADMIN`| AI-driven blast radius analyzer evaluating regression risks across artifacts. |

---

## Continuous 3D Cyberpunk Möbius Visual & Motion Engine

Reqstrata incorporates a hardware-accelerated 3D vector graphics rendering engine implemented directly in Jetpack Compose (`ui/components/mobius/CyberpunkMobiusRenderer.kt`, `MobiusRibbonHeroVisual.kt`, and `MobiusSpaceBackground.kt`).

```
                +------------------------------------------------+
                |        3D PARAMETRIC TOPOLOGY PIPELINE         |
                +------------------------------------------------+
                                        |
       Parametric Evaluation: u ∈ [0, 2π], t ∈ [-1, 1], Twist = u / 2
                                        |
       3D Coordinates: (x, y, z) = f_mobius(u, t, R, W)
                                        |
       3D Rotation Matrix: R_euler(pitch, yaw, roll + continuous_spin)
                                        |
       Perspective Projection: scrX, scrY, scrZ with d / (d + z)
                                        |
       Painter's Algorithm: Depth Sorting of 1,200 Quads (Z-Order)
                                        |
  +-------------------------------------+------------------------------------+
  |                                                                          |
[ Quad Rasterization ]                                              [ Particle & FX Pass ]
  ├── Translucent Neon Shading                                        ├── 48 Lifecycle Data Particles
  ├── Dynamic Surface Normal Calc                                     ├── 7 Orbital Milestone Nodes
  ├── 3-Pass Luminous Bloom (Cyan/Violet/Pink)                        ├── Milestone Beacons & Badges
  └── Cybernetic Tech Lattice Grid                                    └── Touch Inertia & Drag Tracking
```

### Mathematical & Engineering Specifications

* **Topology Model**: Single continuous topological surface with a half-twist ($180^\circ$):
  $$\vec{R}(u, t) = \left( \left( R_0 + t \cdot W \cdot \cos\left(\frac{u}{2}\right) \right) \cos(u), \left( R_0 + t \cdot W \cdot \cos\left(\frac{u}{2}\right) \right) \sin(u), t \cdot W \cdot \sin\left(\frac{u}{2}\right) \right)$$
  where $u \in [0, 2\pi]$ represents the longitudinal angle and $t \in [-1, 1]$ represents the lateral ribbon width.
* **Grid Resolution**:
  * $U$-Steps (Longitudinal Loop): 120 subdivisions.
  * $T$-Steps (Lateral Width): 10 subdivisions.
  * Total Calculated Vertices: $(120 + 1) \times (10 + 1) = 1,331$ vertices per frame.
  * Total Quads Evaluated: $120 \times 10 = 1,200$ quads.
* **Depth Sorting (Painter's Algorithm)**: Quads are sorted dynamically along the camera's Z-axis every frame to guarantee accurate rendering of self-intersecting and twisted geometry.
* **Surface Lighting**: Facing normals are calculated from cross products $(\vec{v}_1 - \vec{v}_0) \times (\vec{v}_2 - \vec{v}_0)$ to calculate realistic back-face darkening and dynamic surface sheen.
* **Multi-Pass Luminous Bloom**: Triple-layered bloom pass using blend modes and Gaussian stroke widths for Electric Neon Cyan (`#00F0FF`), Neon Violet (`#A855F7`), Cyberpunk Magenta (`#EC4899`), and Electric Indigo (`#6366F1`).
* **Dynamic Data Flow Simulation**: 48 independent data particles traverse the ribbon's surface coordinates with distinct speed multipliers and colors, symbolizing continuous requirements traceability.
* **Interactive Physics**: Gesture detectors support dual-axis drag rotation, angular velocity tracking, and inertia-based continuous motion dampening.

---

## Artificial Intelligence Engine (Google Gemini Multi-Model Cascade)

Reqstrata integrates Google Generative Language APIs (`v1beta`) through `GeminiApiClient.kt` and `GeminiRetrofitClient.kt`. The system uses structured JSON schemas and a multi-model cascade to balance speed and reasoning depth.

### Model Cascade Hierarchy

```mermaid
graph LR
    Req[AI Generation Request] --> Primary[Primary Model Target]
    Primary -->|Complex Reasoning| MPro[gemini-3.1-pro-preview]
    Primary -->|High-Speed Parsing| MFlashLite[gemini-3.1-flash-lite]
    Primary -->|Advanced Flash| MFlashAdv[gemini-3.8-flash]
    MPro -->|HTTP 429 / 503 Fallback| MFlashLite
    MFlashLite -->|Retry Fail| MFlashAdv
    MFlashAdv -->|Offline / No Key| SyntheticEngine[Deterministic Offline Synthetic Engine]
```

1. **`gemini-3.1-pro-preview`**: Reserved for deep architectural reasoning, Architecture Decision Records (ADRs), and complex Change Impact blast radius evaluations.
2. **`gemini-3.1-flash-lite`**: High-speed endpoint used for structured requirement generation, acceptance criteria extraction, and test suite synthesis.
3. **`gemini-3.8-flash`**: Alternative high-capacity flash model for conversational system design.
4. **Deterministic Offline Synthetic Engine**: If an API key is missing or the device is offline, Reqstrata invokes domain-specific rule engines to synthesize complete requirements, architecture components, and test suites with zero downtime.

### Core Generative Pipelines

* **`generateStructuredRequirements(goals, domain, targetUsers, constraints, promptTemplate)`**:
  * Deconstructs raw product briefs into structured functional and non-functional requirements with Gherkin-formatted acceptance criteria (`Given-When-Then`) and verification metrics.
  * System Instruction:
    ```
    You are a Principal Software Requirements Engineer and Systems Architect.
    Your role is to translate high-level business goals and constraints into formal, structured, verifiable software requirements according to IEEE 830 and ISO/IEC/IEEE 29148 standards.
    Always provide crisp Functional Requirements (FR) and Non-Functional Requirements (NFR) in the exact JSON format specified.
    ```
* **`analyzeProjectIdea(rawConcept, domain)`**:
  * Evaluates product viability, detects technical feasibility risks, proposes software architectures, and suggests target tech stacks.
* **`analyzeRequirementQuality(title, description, acceptanceCriteria)`**:
  * Audits requirement clarity, yielding normalized percentage scores for Completeness, Ambiguity, Consistency, and Testability alongside actionable suggestions.
* **`generateUseCase(requirementTitle, requirementDescription)`**:
  * Generates actor profiles, trigger events, preconditions, main success scenarios, alternative branches, and postconditions.
* **`suggestSystemArchitecture(projectId, projectName, requirementsSummary)`**:
  * Recommends Clean Architecture layer mappings, component responsibilities, database schemas (ERD tables and columns), and RESTful API endpoints.
* **`analyzeChangeImpact(requirementId, proposedChange, affectedModules)`**:
  * Evaluates the ripple effects of requirement modifications, scoring risk levels and highlighting impacted database tables, API routes, and test suites.
* **`generateTestSuiteFromRequirements(requirements, testType)`**:
  * Produces end-to-end Unit and Integration test cases, complete with preconditions, input test fixtures, execution steps, and expected outcomes.
* **`askProjectAssistant(query, projectContext)`**:
  * Conversational architectural copilot providing context-aware guidance based on the current workspace.

---

## Local Database Schema & Data Access Layer (Room SQLite)

Reqstrata's local database (`reqstrata.db`) is built on **AndroidX Room v2.6.1**, featuring 20 tables and 14 Data Access Objects (DAOs).

### Database Entity Models

#### 1. Core Platform Entities
* **`users` (`UserEntity`)**:
  * Fields: `id` (PK, String), `fullName` (String), `email` (String, Unique Index), `passwordHash` (String), `titleOrRole` (String), `avatarColor` (Long), `status` (String), `tenantId` (String), `tenantName` (String), `createdAt` (Long).
* **`projects` (`ProjectEntity`)**:
  * Fields: `id` (PK, String), `name` (String), `description` (String), `domain` (String), `projectType` (String), `techStack` (String), `methodology` (String), `visibility` (String), `status` (String), `ownerId` (String), `tenantId` (String), `createdAt` (Long), `updatedAt` (Long).
* **`project_members` (`ProjectMemberEntity`)**:
  * Fields: `id` (PK, String), `projectId` (String), `userId` (String), `userName` (String), `userEmail` (String), `role` (String), `joinedAt` (Long). Unique Index on `[projectId, userId]`.

#### 2. Requirements & Use Case Entities
* **`requirements` (`RequirementEntity`)**:
  * Fields: `id` (PK, String), `projectId` (String), `code` (String), `title` (String), `description` (String), `type` (String), `priority` (String), `status` (String), `version` (Int), `authorId` (String), `authorName` (String), `acceptanceCriteriaJson` (String), `aiQualityJson` (String), `dependenciesJson` (String), `approvalNotes` (String), `createdAt` (Long), `updatedAt` (Long). Unique Index on `[projectId, code]`.
* **`requirement_versions` (`RequirementVersionEntity`)**:
  * Fields: `id` (PK, String), `requirementId` (String), `versionNumber` (Int), `title` (String), `description` (String), `acceptanceCriteriaJson` (String), `changedBy` (String), `changeReason` (String), `timestamp` (Long).
* **`use_cases` (`UseCaseEntity`)**:
  * Fields: `id` (PK, String), `projectId` (String), `code` (String), `requirementId` (String), `name` (String), `actor` (String), `goal` (String), `preconditions` (String), `postconditions` (String), `mainFlowJson` (String), `altFlowsJson` (String), `exceptionFlowsJson` (String), `status` (String), `createdAt` (Long). Unique Index on `[projectId, code]`.

#### 3. Architecture & Design Entities
* **`architecture_components` (`ArchitectureComponentEntity`)**:
  * Fields: `id` (PK, String), `projectId` (String), `code` (String), `name` (String), `style` (String), `layerOrModule` (String), `responsibilities` (String), `techStack` (String), `dependenciesJson` (String), `linkedRequirementIdsJson` (String), `status` (String), `createdAt` (Long). Unique Index on `[projectId, code]`.
* **`architecture_decisions` (`ArchitectureDecisionEntity`)**:
  * Fields: `id` (PK, String), `projectId` (String), `code` (String), `title` (String), `status` (String), `context` (String), `decision` (String), `consequences` (String), `timestamp` (Long).
* **`database_entities` (`DatabaseEntityRecord`)**:
  * Fields: `id` (PK, String), `projectId` (String), `code` (String), `name` (String), `description` (String), `fieldsJson` (String), `relationshipsJson` (String), `indexesJson` (String), `status` (String), `createdAt` (Long). Unique Index on `[projectId, name]`.
* **`api_endpoints` (`ApiEndpointEntity`)**:
  * Fields: `id` (PK, String), `projectId` (String), `code` (String), `method` (String), `path` (String), `description` (String), `authRequired` (Boolean), `requestSchema` (String), `responseSchema` (String), `validationRulesJson` (String), `errorResponsesJson` (String), `linkedRequirementId` (String), `status` (String), `createdAt` (Long). Unique Index on `[projectId, method, path]`.

#### 4. Execution & Testing Entities
* **`development_tasks` (`TaskEntity`)**:
  * Fields: `id` (PK, String), `projectId` (String), `code` (String), `title` (String), `description` (String), `priority` (String), `status` (String), `assigneeId` (String), `assigneeName` (String), `linkedRequirementId` (String), `linkedComponentId` (String), `linkedApiId` (String), `estimatedHours` (Int), `technicalNotes` (String), `createdAt` (Long), `updatedAt` (Long). Unique Index on `[projectId, code]`.
* **`test_suites` (`TestSuiteEntity`)**:
  * Fields: `id` (PK, String), `projectId` (String), `code` (String), `name` (String), `type` (String), `description` (String), `createdAt` (Long).
* **`test_cases` (`TestCaseEntity`)**:
  * Fields: `id` (PK, String), `projectId` (String), `code` (String), `suiteId` (String), `linkedRequirementId` (String), `title` (String), `preconditions` (String), `testData` (String), `stepsJson` (String), `expectedResult` (String), `priority` (String), `severity` (String), `status` (String), `createdAt` (Long). Unique Index on `[projectId, code]`.
* **`test_executions` (`TestExecutionEntity`)**:
  * Fields: `id` (PK, String), `projectId` (String), `testCaseId` (String), `status` (String), `actualResult` (String), `failureNotes` (String), `evidence` (String), `executedBy` (String), `executedAt` (Long).
* **`traceability_links` (`TraceabilityLinkEntity`)**:
  * Fields: `id` (PK, String), `projectId` (String), `sourceType` (String), `sourceId` (String), `targetType` (String), `targetId` (String), `description` (String), `createdAt` (Long).

#### 5. Documentation, Governance & Settings Entities
* **`documents` (`DocumentEntity`)**:
  * Fields: `id` (PK, String), `projectId` (String), `docType` (String), `title` (String), `contentMarkdown` (String), `generatedBy` (String), `version` (Int), `createdAt` (Long).
* **`notifications` (`NotificationEntity`)**:
  * Fields: `id` (PK, String), `projectId` (String), `recipientUserId` (String), `title` (String), `message` (String), `targetType` (String), `targetId` (String), `isRead` (Boolean), `createdAt` (Long).
* **`activity_logs` (`ActivityLogEntity`)**:
  * Fields: `id` (PK, String), `projectId` (String), `actorName` (String), `action` (String), `details` (String), `targetType` (String), `targetId` (String), `timestamp` (Long).
* **`ai_generations` (`AiGenerationEntity`)**:
  * Fields: `id` (PK, String), `projectId` (String), `generationType` (String), `promptUsed` (String), `resultSummary` (String), `status` (String), `initiatedBy` (String), `timestamp` (Long).
* **`app_settings` (`AppSettingEntity`)**:
  * Fields: `key` (PK, String), `value` (String).

### Complete Room DAO Interfaces (`Daos.kt`)

The 14 DAOs expose reactive Kotlin `Flow` queries for Compose state observation and `suspend` methods for transactional writes:

```kotlin
@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?
    @Query("SELECT * FROM users ORDER BY fullName ASC")
    fun getAllUsers(): Flow<List<UserEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)
    @Update
    suspend fun updateUser(user: UserEntity)
}

@Dao
interface RequirementDao {
    @Query("SELECT * FROM requirements WHERE projectId = :projectId ORDER BY code ASC")
    fun getRequirements(projectId: String): Flow<List<RequirementEntity>>
    @Query("SELECT * FROM requirements WHERE id = :id LIMIT 1")
    fun getRequirementById(id: String): Flow<RequirementEntity?>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequirement(req: RequirementEntity)
    @Update
    suspend fun updateRequirement(req: RequirementEntity)
    @Query("DELETE FROM requirements WHERE id = :id")
    suspend fun deleteRequirement(id: String)
}

@Dao
interface ArchitectureDao {
    @Query("SELECT * FROM architecture_components WHERE projectId = :projectId ORDER BY code ASC")
    fun getComponents(projectId: String): Flow<List<ArchitectureComponentEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComponent(comp: ArchitectureComponentEntity)
    @Update
    suspend fun updateComponent(comp: ArchitectureComponentEntity)
    @Query("DELETE FROM architecture_components WHERE id = :id")
    suspend fun deleteComponent(id: String)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM development_tasks WHERE projectId = :projectId ORDER BY updatedAt DESC")
    fun getTasks(projectId: String): Flow<List<TaskEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)
    @Update
    suspend fun updateTask(task: TaskEntity)
    @Query("DELETE FROM development_tasks WHERE id = :id")
    suspend fun deleteTask(id: String)
}

@Dao
interface TestDao {
    @Query("SELECT * FROM test_suites WHERE projectId = :projectId ORDER BY code ASC")
    fun getSuites(projectId: String): Flow<List<TestSuiteEntity>>
    @Query("SELECT * FROM test_cases WHERE projectId = :projectId ORDER BY code ASC")
    fun getTestCases(projectId: String): Flow<List<TestCaseEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTestCase(testCase: TestCaseEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExecution(execution: TestExecutionEntity)
}

@Dao
interface TraceabilityDao {
    @Query("SELECT * FROM traceability_links WHERE projectId = :projectId")
    fun getLinks(projectId: String): Flow<List<TraceabilityLinkEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLink(link: TraceabilityLinkEntity)
    @Query("DELETE FROM traceability_links WHERE sourceId = :id OR targetId = :id")
    suspend fun deleteLinksForArtifact(id: String)
}
```

---

## Cloud Database & Real-Time Background Synchronization (MongoDB Atlas)

Reqstrata connects to MongoDB Atlas using the MongoDB Stitch / Realm HTTP protocol managed through `MongoStitchClient.kt`.

```mermaid
graph TD
    AppMutation[User Mutation in Compose UI] --> Repo[Domain Repository]
    Repo -->|Immediate Write| Room[Room Local Database — reqstrata.db]
    Room -->|StateFlow Update| UI[UI Updates Reactively]
    Repo -->|Enqueue SyncTask| Queue[ConcurrentLinkedQueue in MongoSyncService]
    Queue -->|Push Asynchronously| Stitch[MongoDB Stitch HTTP Client]
    Stitch -->|Insert / Update / Delete| Atlas[(MongoDB Atlas Cloud Database)]
    Atlas -->|Real-time ChangeStream| StreamListener[ChangeStream Observers]
    StreamListener -->|Cross-Device Update| Room
```

### The 16 MongoDB Collections

All collections reside within the `requirement2system` database:
1. `projects`: Project workspaces, domains, stacks, and visibility metadata.
2. `requirements`: Primary requirement specifications with acceptance criteria and AI scores.
3. `requirement_versions`: Immutable revision history for requirements diffing.
4. `use_cases`: Actor workflows, success flows, and alternative branches.
5. `architecture_components`: Clean Architecture module definitions.
6. `architecture_decisions`: Architecture Decision Records (ADRs).
7. `database_tables`: Relational and document entity schemas, columns, and relationships.
8. `api_endpoints`: REST API contracts, methods, JSON schemas, and authorization flags.
9. `test_suites`: QA test suite configurations.
10. `test_cases`: Test steps, fixtures, and verification assertions.
11. `users`: Multi-tenant user identities and project role mappings.
12. `project_members`: Project-level user role assignments.
13. `activity_logs`: Immutable chronological mutation records.
14. `notifications`: Real-time user notifications.
15. `traceability_links`: Bidirectional relationship graph edges.
16. `settings`: Workspace and user preferences.

### Synchronization Pipeline (`MongoBackgroundSyncService`)

* **FIFO Queue**: Thread-safe task management using `ConcurrentLinkedQueue<SyncTask>`.
* **Non-Blocking Operation**: Mutations push immediately to local Room tables, with cloud synchronization executing asynchronously on `Dispatchers.IO`.
* **Change Stream Observers**: Active listeners observe changes across collections (`requirements`, `components`, `database_tables`, `api_endpoints`, `projects`) to synchronize updates across devices.
* **Observable Health Metrics**: Emits a reactive `StateFlow<BackgroundSyncMetrics>` tracking `syncState` (`IDLE`, `SYNCING`, `SYNCED`, `FAILED`), pending queue size, total synced count, last sync timestamp, and active cluster URI.

---

## Bidirectional Traceability Matrix & Change Impact Analysis Engine

Reqstrata's core capability is **Bidirectional Requirements Traceability**. Every artifact links back to an upstream requirement and forward to downstream implementations and tests.

```
+-------------------------------------------------------------------------------------------------------------------------+
|                                        BIDIRECTIONAL TRACEABILITY LIFECYCLE                                             |
+-------------------------------------------------------------------------------------------------------------------------+
  [ Requirement: REQ-001 ]
        │
        ├──► [ Use Case: UC-001 ] ──► Actor: Mobile User ──► Precondition: Authenticated
        │
        ├──► [ Architecture Component: CMP-AUTH ] ──► Layer: Domain / Security
        │          │
        │          ├──► [ Database Table: users ] ──► Columns: id, email, password_hash, role
        │          │
        │          └──► [ REST API: POST /api/v1/auth/login ] ──► Request/Response Schema
        │                     │
        │                     └──► [ Dev Task: TSK-101 ] ──► Assigned: Developer ──► Status: DONE
        │
        └──► [ Test Suite: TS-SECURITY ]
                   │
                   └──► [ Test Case: TC-AUTH-01 ] ──► Status: PASSED (Verified by Tester)
```

### Traceability Features

1. **RTM Matrix Visualization (`TraceabilityMatrixScreen`)**: Displays a real-time table linking Requirements $\to$ Use Cases $\to$ Components $\to$ Endpoints $\to$ Tasks $\to$ Test Cases. Unlinked or orphan artifacts are highlighted in red.
2. **AI Change Impact Analysis (`ChangeImpactDashboardScreen`)**: When a requirement is modified, the AI Change Impact engine analyzes the proposed change, calculates a confidence score, and identifies affected database schemas, API routes, tasks, and test cases that require re-verification.
3. **Automated Documentation Engine (`DocumentViewerScreen`)**: Generates live, exportable Markdown engineering documents on demand:
   * **SRS** (Software Requirements Specification)
   * **SDD** (Software Design Document)
   * **API Contract Specification** (OpenAPI-aligned REST documentation)
   * **Test Plan & QA Verification Report**
   * **Requirements Traceability Matrix (RTM) Summary**

---

## End-to-End Persona Lifecycle Walkthrough

The following sequence reflects the complete 7-stage workflow verified in `EndToEndSoftwareLifecycleIntegrationTest.kt`:

### Stage 1: Workspace Provisioning (Admin)
The Administrator creates an isolated project workspace, configuring domain attributes and methodology settings.
```kotlin
val project = projectRepo.createProject(
    name = "Enterprise Logistics System",
    description = "Cloud-native real-time routing platform",
    domain = "Supply Chain",
    projectType = "Cloud Native",
    techStack = "Kotlin, Jetpack Compose, MongoDB Atlas",
    methodology = "Scrum",
    ownerId = adminUser.id
)
```

### Stage 2: Requirement Specification (Business Analyst)
The Business Analyst authors a functional requirement containing explicit acceptance criteria.
```kotlin
val requirement = reqRepo.createRequirement(
    projectId = project.id,
    code = "REQ-001",
    title = "Carrier Geo-Tracking Ingress",
    description = "Ingest carrier GPS coordinates every 5 seconds with sub-second latency.",
    type = "Functional",
    priority = "Critical",
    authorId = baUser.id,
    authorName = baUser.fullName,
    acceptanceCriteria = listOf(
        "Given mobile transmitter, coordinates are submitted within 500ms.",
        "Given dropped connection, client retries with exponential backoff."
    )
)
```

### Stage 3: Formal Review & Sign-Off (Admin / Approval Center)
The requirement is submitted for review, transitioning from `UNDER_REVIEW` to `APPROVED`.
```kotlin
reqRepo.setApprovalStatus(
    reqId = requirement.id,
    status = RequirementStatus.APPROVED,
    notes = "Verified against Q3 compliance standards."
)
```

### Stage 4: Component Decomposition & Design (System Architect)
The Architect designs a backend ingress component and explicitly links it to `REQ-001`.
```kotlin
val component = archRepo.createComponent(
    projectId = project.id,
    code = "ARCH-001",
    name = "GeoIngressGateway",
    layerOrModule = "Infrastructure / Ingress",
    responsibilities = "High-throughput telemetry ingestion and WebSocket push.",
    techStack = "Kotlin Coroutines / Ktor / Room",
    linkedRequirementIds = listOf(requirement.id)
)
```

### Stage 5: Task Execution (Software Developer)
The Developer pulls the linked task from the Kanban board, transitions it to `IN_PROGRESS`, and completes the implementation.
```kotlin
val task = taskRepo.createTask(
    projectId = project.id,
    code = "DEV-001",
    title = "Implement GeoIngressGateway Telemetry Service",
    description = "Construct Ktor routes and MongoDB persistence buffers.",
    assigneeId = devUser.id,
    assigneeName = devUser.fullName,
    linkedRequirementId = requirement.id,
    linkedComponentId = component.id
)
taskRepo.updateTaskStatus(task.id, TaskStatus.COMPLETED)
```

### Stage 6: QA Verification & Test Run (QA Tester)
The Tester synthesizes a verification test case and records a successful test execution.
```kotlin
val testCase = testRepo.createTestCase(
    projectId = project.id,
    code = "TC-001",
    suiteId = testSuite.id,
    linkedRequirementId = requirement.id,
    title = "Verify 500ms Ingestion Latency",
    preconditions = "Transmitter active and calibrated.",
    testData = "{ lat: 37.7749, lon: -122.4194 }",
    steps = listOf("1. Transmit payload", "2. Monitor acknowledgment receipt"),
    expectedResult = "HTTP 200 returned in < 500ms."
)
testRepo.recordExecution(
    testCaseId = testCase.id,
    status = TestExecutionStatus.PASSED,
    actualResult = "Average response time: 142ms.",
    executedBy = testerUser.fullName
)
```

### Stage 7: Bidirectional Traceability Validation (RTM)
The Traceability engine binds the artifacts together, validating that no orphan links exist in the release chain.
```kotlin
traceRepo.createLink(
    projectId = project.id,
    sourceType = "REQUIREMENT",
    sourceId = requirement.id,
    targetType = "ARCHITECTURE",
    targetId = component.id
)
```

---

## High-Volume Concurrency & Stress Testing Analysis

Reqstrata's access policies and database layers are validated through a dedicated high-volume stress testing suite in `RoleSecurityAndLoadStressTest.kt`:

1. **500 Concurrent Permission Evaluations**:
   Evaluates 500 concurrent permission requests dispatched via `async(Dispatchers.Default)` across random role and module combinations. Verifies thread-safety, zero race conditions, and an execution threshold of $< 3000\text{ms}$.
2. **Bulk Data Mutation Under Load**:
   Executes batch requirement inserts ($25\times$) sequentially to preserve ordering, paired with concurrent asynchronous development task creations across worker threads on `Dispatchers.IO`. Verifies complete database persistence without locked tables.
3. **Rapid Role-Switching Stress Simulation**:
   Alternates user roles 200 consecutive times across sensitive administrative modules (`TEAM_MANAGEMENT`, `ROLE_MANAGEMENT`, `AUDIT_LOGS`). Asserts that non-admin identities are consistently rejected with zero permission leakage.
4. **Extreme Payloads & Injection Resistance**:
   Validates system stability when processing 8,000-character descriptions, unicode strings (`システム要件仕様書 — 🚀 Enterprise Traceability`), and cross-site scripting strings (`<script>alert('XSS')</script>`), verifying sanitized storage in SQLite and MongoDB without buffer overflow or execution vulnerabilities.

---

## Global State Management & ViewModel Architecture

The application's global presentation state is orchestrated by `MainViewModel.kt`, which manages reactive `StateFlow` streams exposed to Compose:

```kotlin
// Authentication & Session Context
val currentUser: StateFlow<UserEntity?>
val currentRole: StateFlow<ProjectRole>
val currentProject: StateFlow<ProjectEntity?>

// Workspace Entity State Streams
val requirements: StateFlow<List<RequirementEntity>>
val components: StateFlow<List<ArchitectureComponentEntity>>
val databaseEntities: StateFlow<List<DatabaseEntityRecord>>
val apiEndpoints: StateFlow<List<ApiEndpointEntity>>
val tasks: StateFlow<List<TaskEntity>>
val testSuites: StateFlow<List<TestSuiteEntity>>
val traceabilityLinks: StateFlow<List<TraceabilityLinkEntity>>
val activityLogs: StateFlow<List<ActivityLogEntity>>
val notifications: StateFlow<List<NotificationEntity>>

// Real-Time In-App Alert Dispatcher
CentralizedNotificationService.showInApp(
    title = "Atlas Push Succeeded",
    message = "Requirement REQ-001 synchronized with MongoDB Atlas cluster",
    type = NotificationType.SUCCESS
)
```

Navigation is driven by the internal `screenBackStack`, allowing forward and backward movement while automatically syncing the active screen state with the top app bar and bottom navigation tabs.

---

## Type Converters & Serialization Infrastructure

Because Room SQLite stores primitive values, complex collections are serialized using `Converters.kt`:

```kotlin
class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        if (value == null) return "[]"
        val array = JSONArray()
        value.forEach { array.put(it) }
        return array.toString()
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return try {
            val array = JSONArray(value)
            val list = mutableListOf<String>()
            for (i in 0 until array.length()) {
                list.add(array.getString(i))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }
}
```

This ensures acceptance criteria, dependency arrays, and test steps are stored consistently as JSON text arrays in SQLite and seamlessly converted to Kotlin `List<String>`.

---

## ProGuard, R8 & Build Optimization Rules

The project enforces aggressive shrinking, optimization, and obfuscation in production release builds via `app/proguard-rules.pro`:

```proguard
# 1. Room Database
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keep class androidx.room.** { *; }
-keep class com.theoriongd.reqstrata.data.local.entity.** { *; }
-keep class com.theoriongd.reqstrata.data.local.dao.** { *; }

# 2. Retrofit & OkHttp
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn org.bouncycastle.jsse.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**
-keep class retrofit2.** { *; }
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# 3. Moshi & JSON Serialization
-keepclasseswithmembers class * {
    @com.squareup.moshi.* <methods>;
}
-keepclasseswithmembers class * {
    @com.squareup.moshi.* <fields>;
}
-keep class com.theoriongd.reqstrata.data.remote.gemini.** { *; }
-keep class com.theoriongd.reqstrata.domain.model.** { *; }

# 4. Kotlin Coroutines & Jetpack Compose
-dontwarn kotlinx.coroutines.**
-keep class kotlinx.coroutines.** { *; }
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# 5. Platform Optimization
-keepattributes *Annotation*
-repackageclasses ''
-allowaccessmodification
```

---

## Complete Technology Stack & Dependency Catalog

All libraries and versions are sourced directly from `gradle/libs.versions.toml` and `app/build.gradle.kts`:

| Category | Technology / Library | Version | Purpose in Codebase |
| :--- | :--- | :--- | :--- |
| **Operating System** | Android OS | API 24–36 | Minimum SDK 24, Target SDK 36 (Android 16 'Baklava') |
| **Language** | Kotlin | `2.0.21` | Core language across all presentation, domain, and data layers |
| **Build System** | Gradle / AGP | `8.8.2` | Android Gradle Plugin with KSP and Secrets Gradle Plugin |
| **UI Framework** | Jetpack Compose BOM | `2024.09.00` | Declarative UI rendering, canvas vector graphics, and animations |
| **Design System** | Material 3 | Compose BOM | Material You components, adaptive colors, and typography |
| **Navigation** | Compose Navigation | `2.8.5` | Dynamic graph generation, route parsing, and back-stack handling |
| **Local Persistence** | AndroidX Room | `2.6.1` | Local SQLite database, 20 entities, 14 DAOs, KSP annotation processing |
| **Cloud Networking** | Square Retrofit | `2.11.0` | REST API networking for Gemini and MongoDB endpoints |
| **HTTP Engine** | Square OkHttp | `4.12.0` | Connection pooling, timeouts, and request interception |
| **JSON Serialization** | Square Moshi | `1.15.1` | Kotlin JSON parsing with KSP codegen |
| **Concurrency** | Kotlinx Coroutines | `1.8.1` | Asynchronous operations, `Dispatchers.IO`, and reactive flows |
| **State Management** | AndroidX Lifecycle | `2.8.7` | `ViewModel`, `StateFlow`, and `LifecycleOwner` integration |
| **Secrets Engine** | MapsPlatform Secrets | `2.0.1` | Injects `.env` keys securely into `BuildConfig` at compile-time |
| **Unit Testing** | JUnit 4 | `4.13.2` | Core JVM test execution runner |
| **Android Sandbox** | Robolectric | `4.13` | Simulates Android SDK 34 runtime environment for JVM tests |
| **Coroutines Testing**| Kotlinx Coroutines Test | `1.8.1` | Virtual time execution and dispatcher control in unit tests |

---

## Repository Directory Layout

```
o:\REQUIREMENT2SYSTEM\
├── .env                              # Active environment configuration (API keys, cluster URIs)
├── .env.example                      # Reference template for configuration variables
├── build.gradle.kts                  # Root Gradle build script
├── settings.gradle.kts               # Module and plugin repository resolution settings
├── gradle/
│   ├── libs.versions.toml            # Centralized version catalog
│   └── wrapper/                      # Gradle wrapper distribution binaries
├── PAGE_LIST_AND_ROLES.txt           # Verified 42-screen topology and RBAC specification
└── app/
    ├── build.gradle.kts              # Application build configuration, SDK 36, Room, ProGuard
    ├── proguard-rules.pro            # R8 shrinking and code obfuscation rules
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml   # Manifest declaring application identity and permissions
        │   ├── res/                  # Drawable assets, mipmap launcher icons, color values
        │   └── java/com/theoriongd/reqstrata/
        │       ├── MainActivity.kt   # Single-Activity host rendering the Compose NavHost
        │       ├── data/
        │       │   ├── local/        # Room Database, 20 Entities, 14 DAOs, Converters
        │       │   ├── remote/       # GeminiApiClient, GeminiRetrofitClient, OkHttp client
        │       │   │   └── mongo/    # MongoDB Stitch client, repositories, sync service
        │       │   └── repository/   # 12 Repository implementations handling data orchestration
        │       ├── domain/
        │       │   ├── auth/         # CentralizedAuthorizationManager, Security Exceptions
        │       │   └── model/        # DomainModels.kt, ProjectAccessPolicy.kt (5 Roles x 16 Modules)
        │       └── ui/
        │           ├── Screen.kt     # Sealed class defining the screen destination hierarchy
        │           ├── MainViewModel.kt # Primary application ViewModel managing global state
        │           ├── components/   # Reusable UI widgets, badges, buttons, cards
        │           │   ├── background/ # MobiusSpaceBackground.kt (Cosmic 3D Starfield Canvas)
        │           │   └── mobius/   # CyberpunkMobiusRenderer.kt, MobiusRibbonHeroVisual.kt
        │           ├── navigation/   # AppRoutes.kt, RouteAuthorization.kt, DynamicNavGraphBuilder.kt
        │           ├── notifications/# CentralizedNotificationService, in-app notification manager
        │           ├── screens/      # All 42 Compose Screen implementations
        │           │   ├── ai/       # Gemini Copilot, System Design Chat
        │           │   ├── architecture/ # Architecture Workspace, UML Studio, ADRs
        │           │   ├── auth/     # Login, Register, Splash, Onboarding, Password Recovery
        │           │   ├── design/   # Database Designer (ERD), API Designer
        │           │   ├── documents/# Markdown Document Generator & Viewer
        │           │   ├── profile/  # User Profile, Tenant Isolation Card, Settings
        │           │   ├── project/  # Selection, Dashboards, Overview, Approval Center
        │           │   ├── requirements/ # Requirements Catalog, AI Synthesizer, Use Cases
        │           │   ├── tasks/    # Developer Kanban Board, Task Queue
        │           │   ├── team/     # Team Directory, Member Details, Role Governance
        │           │   └── testing/  # Test Suites, Execution Runner, Coverage Dashboard
        │           └── theme/        # Color.kt, Theme.kt, Typography.kt (Material 3 Dark Palette)
        └── test/java/com/theoriongd/reqstrata/
            ├── ArchitectureAndTestingSuiteTest.kt            # Component linking and test suite tests
            ├── EndToEndSoftwareLifecycleIntegrationTest.kt    # Full 7-stage persona lifecycle integration
            ├── NavigationAndMongoArchitectureTest.kt         # Route mappings & MongoDB serialization
            ├── RoleBasedAccessControlAndScopingTest.kt       # 5 roles x 16 modules RBAC boundary tests
            ├── RoleSecurityAndLoadStressTest.kt              # 500-thread concurrent stress testing
            ├── ExampleUnitTest.kt                            # JVM arithmetic validation
            └── ExampleRobolectricTest.kt                     # Robolectric context verification
```

---

## Automated Testing & Verification Suite

Reqstrata includes an automated test suite containing **48 tests** executed via JUnit 4 and Robolectric on Android SDK 34.

```
========================================================================================
AUTOMATED TEST SUITE SUMMARY (48 TESTS — 0 FAILURES)
========================================================================================
[√] RoleBasedAccessControlAndScopingTest
    ├── testAdminRoleHasFullAccessToEverySingleModule
    ├── testAdminNavigationItemsContainAllGovernanceDestinations
    ├── testBusinessAnalystHasWriteAccessOnlyToRequirementsAndUseCases
    ├── testArchitectHasWriteAccessToArchitectureUmlDatabaseAndApis
    ├── testDeveloperHasWriteAccessOnlyToDevelopmentTasks
    ├── testTesterHasWriteAccessOnlyToTestingAndTraceability
    ├── testNegativeRejectionDeveloperCannotAccessRoleGovernance
    ├── testNegativeRejectionTesterCannotCreateRequirements
    ├── testNegativeRejectionBusinessAnalystCannotModifyDatabaseSchema
    ├── testBlankAndInvalidInputHandlingInProjectAccessPolicy
    └── testAll16ModulesHaveConsistentPermissionMappingsAcrossRoles

[√] EndToEndSoftwareLifecycleIntegrationTest
    ├── testCompleteSevenStageSoftwareLifecycleAcrossPersonas
    │   ├── Stage 1: Admin provisions project workspace
    │   ├── Stage 2: Business Analyst authors requirement specification
    │   ├── Stage 3: Admin reviews and approves requirement
    │   ├── Stage 4: System Architect creates component & links to requirement
    │   ├── Stage 5: Developer creates task, links to component, and completes work
    │   ├── Stage 6: Tester designs test case, executes verification, records PASS
    │   └── Stage 7: Traceability Matrix validates bidirectional link chain
    ├── testNegativeRejectionUnauthorizedUserCannotApproveRequirement
    └── testBlankAndInvalidFieldsAreRejectedAcrossAllRepositories

[√] RoleSecurityAndLoadStressTest
    ├── testConcurrentRoleEvaluationUnderHighConcurrency (500 threads)
    ├── testRapidRoleSwitchingStressSimulation
    └── testBulkDataPersistenceAndEntityCreationUnderStress

[√] NavigationAndMongoArchitectureTest
    ├── testRoleBasedDashboardRouting
    ├── testMongoProjectDocumentCompatibility
    ├── testMongoRequirementDocumentCompatibility
    ├── testMongoArchitectureComponentDocumentCompatibility
    ├── testMongoDatabaseEntityDocumentCompatibility
    └── testMongoApiEndpointDocumentCompatibility

[√] ArchitectureAndTestingSuiteTest
    ├── testArchitectureComponentHierarchyAndDependencyResolution
    ├── testTestCaseExecutionStatusTransitions
    └── testTraceabilityMatrixGraphIntegrity
========================================================================================
```

### Running Tests via Command Line

Run all unit and integration tests using Gradle:

```bash
# Execute the complete test suite
./gradlew testDebugUnitTest

# Execute a specific test suite
./gradlew testDebugUnitTest --tests "com.theoriongd.reqstrata.RoleBasedAccessControlAndScopingTest"
```

---

## Build, Configuration & Environment Setup

### Prerequisites

* **Java Development Kit (JDK)**: Version 17 or Version 21 (configured as `JAVA_HOME`).
* **Android Studio**: Ladybug (2024.2.1+) or Meerkat (2024.3.1+).
* **Android SDK**: Build Tools `36.0.0` or higher, Platform SDK `API 36`.

### Environment Variables (`.env`)

Reqstrata utilizes the **Secrets Gradle Plugin** to read configuration keys from a `.env` file at the root of the project and compile them into `BuildConfig`:

```env
# Google Gemini API Key for Generative Synthesis
GEMINI_API_KEY=AIzaSyYourGeminiApiKeyHere

# MongoDB Atlas Cluster Credentials
MONGODB_USERNAME=your_atlas_username
MONGODB_PASSWORD=your_atlas_password
MONGODB_URI=mongodb+srv://your_username:your_password@yourcluster.mongodb.net/requirement2system?retryWrites=true&w=majority
MONGODB_APP_ID=your_mongodb_stitch_app_id
MONGODB_CLUSTER_NAME=your_cluster_name
MONGODB_DATABASE_NAME=requirement2system
```

> **Note**: An `.env.example` template is provided in the repository root. Ensure `.env` is never committed to public source control.

### Build Commands

```bash
# Clean the project workspace
./gradlew clean

# Compile debug Kotlin sources and KSP annotations
./gradlew compileDebugSources

# Assemble debug APK
./gradlew assembleDebug

# Run unit and Robolectric integration test suites
./gradlew testDebugUnitTest

# Assemble production-optimized release APK (with ProGuard/R8 minification and resource shrinking)
./gradlew assembleRelease
```

The resulting signed release APK is generated at:
`app/build/outputs/apk/release/app-release.apk`

---

## Security, Governance & Multi-Tenant Data Isolation

1. **Project-Scoped RBAC**: Roles are tied directly to specific projects. A user with administrative rights in one workspace cannot execute operations in another unless explicitly granted membership.
2. **Dual-Layer Security Enforcement**:
   * **Route-Level**: `AuthorizedRoute` and `RouteAuthorization.kt` intercept unauthorized navigation attempts and render the `RoleRestrictedAccessScreen` instead of exposing sensitive UI.
   * **Data-Level**: `CentralizedAuthorizationManager` evaluates write and delete calls, throwing typed `UnauthorizedDataAccessException` errors if the calling user lacks the necessary role.
3. **Audit Trail Logging**: All mutations (requirement edits, component additions, test executions, member invitations) are logged as immutable events in the local `activity_logs` Room table and synchronized with MongoDB Atlas.
4. **Secret Protection**: API keys and database credentials are excluded from source code via the Secrets Gradle Plugin, keeping them out of version control and embedding them securely into compiled bytecode.
5. **Robust Exception Handling**: Cloud and network failures trigger graceful fallbacks to the local Room database, ensuring the application remains functional without crashing or data loss.

---

## License

This codebase and all associated modules are proprietary and confidential.  
Copyright &copy; 2026 TheOrionGD / Reqstrata. All rights reserved.
