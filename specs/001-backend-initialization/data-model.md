# Data Model: Backend Core

## Entity Relationship Diagram (Conceptual)

```mermaid
erDiagram
    User {
        UUID id PK
        String email UK
        String password_hash
        String name
        Role role "ADMIN|TEACHER|STUDENT"
    }
    Class {
        Long id PK
        String name
        String section
    }
    Student {
        Long id PK
        String name
        String admission_no UK
        Long class_id FK
    }
    Teacher {
        Long id PK
        String name
        String subject
    }

    Class ||--o{ Student : "has"
```

## Schema Definitions

### 1. `users`
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | UUID | PK, Default gen_random_uuid() | Unique User ID |
| `email` | VARCHAR(255) | UNIQUE, NOT NULL | Login email |
| `password_hash` | VARCHAR(255) | NOT NULL | BCrypt encoded password |
| `name` | VARCHAR(100) | NOT NULL | Full name |
| `role` | VARCHAR(20) | NOT NULL | Enum: ADMIN, TEACHER, STUDENT |
| `created_at` | TIMESTAMP | DEFAULT NOW() | Audit |

### 2. `classes`
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | BIGSERIAL | PK | Auto-inc ID |
| `name` | VARCHAR(50) | NOT NULL | e.g. "10" |
| `section` | VARCHAR(10) | NOT NULL | e.g. "A" |

### 3. `students`
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | BIGSERIAL | PK | Auto-inc ID |
| `name` | VARCHAR(100) | NOT NULL | Student Name |
| `admission_no` | VARCHAR(50) | UNIQUE, NOT NULL | School ID |
| `class_id` | BIGINT | FK -> classes(id) | Current Class |

### 4. `teachers`
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | BIGSERIAL | PK | Auto-inc ID |
| `name` | VARCHAR(100) | NOT NULL | Teacher Name |
| `subject` | VARCHAR(50) | | Primary Subject |

## Validation Rules
- **Email**: Must be valid email format.
- **Role**: Must be one of valid enum values.
