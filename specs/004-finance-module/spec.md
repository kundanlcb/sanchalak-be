# Specification: Finance Module (Backend)

## 1. Overview
The Finance Module enables fee management, structure configuration, and payment tracking. This specification aligns with the Frontend "Phase 3 Financial & Admin" specification (P1-P3), focusing on Fee Structures, Online Payments, and Digital Receipts.

**Reference:** Frontend Spec `003-financial-admin/spec.md`

## 2. Goals
*   **Flexible Configuration:** Support Tuition, Transport, etc., with monthly/annual frequencies (Matches FE FR-001).
*   **Advanced Logic:** Handle Late Fees (FR-004) and Discounts (FR-005).
*   **Payment Integrity:** Support Partial Payments (FR-016) and prevent duplicates (FR-015).
*   **Receipts:** Generate unique sequential IDs `RCP-YYYY-NNNNN` (FR-022).

## 3. Functional Requirements (Backend)

### 3.1 Fee Structure & Categories (P1)
*   **FR-3.1.1:** CRUD for `FeeCategory` (Name: Tuition, Transport, etc., Default Amount). *Aligns with FE `FeeCategory`*.
*   **FR-3.1.2:** CRUD for `FeeStructure`. Must support:
    *   `frequency` (Monthly/Annual).
    *   `lateFeeRule` (Amount/Percentage + Grace Period).
*   **FR-3.1.3:** Assign Structure to Student/Class. Support `discounts` (e.g., Sibling Discount) at assignment time.

### 3.2 Ledger & Dues Management
*   **FR-3.2.1:** Calculate Total Dues dynamically:
    *   `Total Due` = `Base Amount` - `Discounts` + `Late Fees` - `Paid Amount`.
*   **FR-3.2.2:** API must return dues broken down by fee category for the frontend.

### 3.3 Transaction Processing (P2)
*   **FR-3.3.1:** Record `PaymentTransaction`.
*   **FR-3.3.2:** Support **Partial Payments**. Validates that `Amount <= Pending Amount` (unless Advance is enabled).
*   **FR-3.3.3:** **Idempotency**: Prevent duplicate charging using `gatewayTransactionId`.
*   **FR-3.3.4:** Record `paymentMethod` (UPI, Card, Cash) and `gatewayStatus`.

### 3.4 Receipt Generation (P3)
*   **FR-3.4.1:** Generate `Receipt` on successful transaction.
*   **FR-3.4.2:** **Format**: `RCP-{YYYY}-{Sequence}` (e.g., `RCP-2026-00123`).
*   **FR-3.4.3:** Persist Receipt URL/Data for retrieval.

## 4. Database Schema (Aligned with FE)

### 4.1 FeeCategory (was FeeHead)
*   `id`: Long (PK)
*   `name`: String (Unique)
*   `description`: String
*   `isMandatory`: Boolean

### 4.2 FeeStructure
*   `id`: Long (PK)
*   `name`: String
*   `academicYear`: String
*   `frequency`: ENUM (MONTHLY, QUARTERLY, ANNUAL)
*   `lateFeeAmount`: BigDecimal
*   `gracePeriodDays`: Integer

### 4.3 FeeStructureItem
*   `structure_id`: FK
*   `category_id`: FK (FeeCategory)
*   `amount`: BigDecimal

### 4.4 StudentFeeMap
*   `student_id`: FK
*   `structure_id`: FK
*   `discountAmount`: BigDecimal (Flat discount stored here)
*   `discountReason`: String (e.g., "Sibling")

### 4.5 PaymentTransaction
*   `id`: Long (PK)
*   `student_id`: FK
*   `transactionType`: ENUM (FEE_PAYMENT, REFUND)
*   `amount`: BigDecimal
*   `paymentDate`: LocalDateTime
*   `paymentMethod`: ENUM (CASH, UPI, CARD, CHEQUE)
*   `gatewayTransactionId`: String (Unique, for Idempotency)
*   `status`: ENUM (SUCCESS, PENDING, FAILED)
*   `receiptNo`: String (Unique, Format: RCP-YYYY-NNNNN)

## 5. API Endpoints

### Configuration
*   `GET/POST /api/finance/categories` (Fee Categories)
*   `GET/POST /api/finance/structures` (Fee Structures)
*   `POST /api/finance/structures/{id}/assign` (Assign to Student/Class)

### Ledger & Payments
*   `GET /api/finance/students/{studentId}/ledger` (Returns Dues, Late Fees, History)
*   `POST /api/finance/transactions` (Process Payment)
    *   Input: `studentId`, `amount`, `method`, `gatewayId`.
*   `GET /api/finance/receipts/{receiptNo}` (Fetch Receipt Data)

## 6. Out of Scope (For this Backend Branch)
*   **Phase 2:** Payroll Management (FE User Story 4).
*   **Phase 2:** Advanced Analytics Dashboards (FE User Story 5).
*   *Rational:* These are P4/P5 in the FE spec. We focus on enabling Fee Collection first.
