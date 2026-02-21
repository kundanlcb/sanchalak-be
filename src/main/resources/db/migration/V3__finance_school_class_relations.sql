-- V3__finance_school_class_relations.sql
-- Aligns finance-school-class relations with current entity mappings.

-- Sentinel UUID used only for legacy backfill when school context is unknown.
SET @default_school_uuid_bin := UNHEX(REPLACE('00000000-0000-0000-0000-000000000000', '-', ''));

-- ------------------------------------------------------------
-- 1) Ensure class table carries school context used by finance joins
-- ------------------------------------------------------------
ALTER TABLE classes
    ADD COLUMN IF NOT EXISTS school_id BINARY(16) NULL,
    ADD COLUMN IF NOT EXISTS class_id VARCHAR(50) NULL,
    ADD COLUMN IF NOT EXISTS grade INT NULL,
    ADD COLUMN IF NOT EXISTS section VARCHAR(10) NULL,
    ADD COLUMN IF NOT EXISTS room VARCHAR(20) NULL;

SET @has_idx := (
    SELECT COUNT(*)
    FROM (
        SELECT s.index_name
        FROM information_schema.statistics s
        WHERE s.table_schema = DATABASE()
          AND s.table_name = 'classes'
          AND s.non_unique = 0
        GROUP BY s.index_name
        HAVING COUNT(*) = 1
           AND MAX(CASE WHEN s.column_name = 'class_id' THEN 1 ELSE 0 END) = 1
    ) t
);
SET @sql := IF(@has_idx = 0, 'CREATE UNIQUE INDEX uk_classes_class_id ON classes(class_id)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ------------------------------------------------------------
-- 2) Finance master tables: school scoping + audit columns
-- ------------------------------------------------------------
ALTER TABLE fee_categories
    ADD COLUMN IF NOT EXISTS school_id BINARY(16) NULL,
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

UPDATE fee_categories
SET school_id = @default_school_uuid_bin
WHERE school_id IS NULL;

ALTER TABLE fee_categories
    MODIFY COLUMN school_id BINARY(16) NOT NULL;

SET @legacy_unique_name_idx := (
    SELECT INDEX_NAME
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'fee_categories'
      AND non_unique = 0
      AND index_name <> 'PRIMARY'
    GROUP BY INDEX_NAME
    HAVING COUNT(*) = 1 AND MAX(CASE WHEN column_name = 'name' THEN 1 ELSE 0 END) = 1
    LIMIT 1
);
SET @sql := IF(
    @legacy_unique_name_idx IS NULL,
    'SELECT 1',
    CONCAT('ALTER TABLE fee_categories DROP INDEX `', @legacy_unique_name_idx, '`')
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_idx := (
    SELECT COUNT(*)
    FROM (
        SELECT s.index_name
        FROM information_schema.statistics s
        WHERE s.table_schema = DATABASE()
          AND s.table_name = 'fee_categories'
          AND s.non_unique = 0
        GROUP BY s.index_name
        HAVING GROUP_CONCAT(s.column_name ORDER BY s.seq_in_index) = 'school_id,name'
    ) t
);
SET @sql := IF(@has_idx = 0, 'CREATE UNIQUE INDEX uk_fee_categories_school_name ON fee_categories(school_id, name)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE fee_structures
    ADD COLUMN IF NOT EXISTS school_id BINARY(16) NULL,
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

UPDATE fee_structures
SET school_id = @default_school_uuid_bin
WHERE school_id IS NULL;

ALTER TABLE fee_structures
    MODIFY COLUMN school_id BINARY(16) NOT NULL;

SET @has_idx := (
    SELECT COUNT(*)
    FROM (
        SELECT s.index_name
        FROM information_schema.statistics s
        WHERE s.table_schema = DATABASE()
          AND s.table_name = 'fee_structures'
          AND s.non_unique = 0
        GROUP BY s.index_name
        HAVING GROUP_CONCAT(s.column_name ORDER BY s.seq_in_index) = 'school_id,name,academic_year'
    ) t
);
SET @sql := IF(
    @has_idx = 0,
    'CREATE UNIQUE INDEX uk_fee_structures_school_name_year ON fee_structures(school_id, name, academic_year)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE fee_structure_items
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- ------------------------------------------------------------
-- 3) Class fee assignment (source of truth for class-structure linkage)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS class_fee_assignments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    school_id BINARY(16) NOT NULL,
    class_id BIGINT NOT NULL,
    structure_id BIGINT NOT NULL,
    academic_year VARCHAR(20) NOT NULL,
    effective_from DATE NOT NULL,
    effective_to DATE NULL,
    due_day_of_month INT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_class_fee_assignment_class FOREIGN KEY (class_id) REFERENCES classes(id),
    CONSTRAINT fk_class_fee_assignment_structure FOREIGN KEY (structure_id) REFERENCES fee_structures(id),
    CONSTRAINT uk_class_fee_assignment UNIQUE (school_id, class_id, structure_id, academic_year, effective_from)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 4) Student obligations scoped by school + assignment
-- ------------------------------------------------------------
ALTER TABLE student_fee_maps
    ADD COLUMN IF NOT EXISTS school_id BINARY(16) NULL,
    ADD COLUMN IF NOT EXISTS class_fee_assignment_id BIGINT NULL,
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

UPDATE student_fee_maps sfm
JOIN fee_structures fs ON fs.id = sfm.structure_id
SET sfm.school_id = fs.school_id
WHERE sfm.school_id IS NULL;

SET @has_idx := (
    SELECT COUNT(*)
    FROM (
        SELECT s.index_name
        FROM information_schema.statistics s
        WHERE s.table_schema = DATABASE()
          AND s.table_name = 'student_fee_maps'
          AND s.non_unique = 0
        GROUP BY s.index_name
        HAVING GROUP_CONCAT(s.column_name ORDER BY s.seq_in_index) = 'student_id,class_fee_assignment_id'
    ) t
);
SET @sql := IF(
    @has_idx = 0,
    'CREATE UNIQUE INDEX uk_student_fee_map_student_assignment ON student_fee_maps(student_id, class_fee_assignment_id)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_idx := (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'student_fee_maps'
      AND index_name = 'idx_student_fee_maps_school_id'
);
SET @sql := IF(@has_idx = 0, 'CREATE INDEX idx_student_fee_maps_school_id ON student_fee_maps(school_id)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_fk := (
    SELECT COUNT(*)
    FROM information_schema.key_column_usage k
    WHERE k.table_schema = DATABASE()
      AND k.table_name = 'student_fee_maps'
      AND k.column_name = 'class_fee_assignment_id'
      AND k.referenced_table_name = 'class_fee_assignments'
      AND k.referenced_column_name = 'id'
);
SET @sql := IF(
    @has_fk = 0,
    'ALTER TABLE student_fee_maps ADD CONSTRAINT fk_student_fee_map_assignment FOREIGN KEY (class_fee_assignment_id) REFERENCES class_fee_assignments(id)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ------------------------------------------------------------
-- 5) Payment transactions: school scope + student fee map linkage
-- ------------------------------------------------------------
ALTER TABLE payment_transactions
    ADD COLUMN IF NOT EXISTS school_id BINARY(16) NULL,
    ADD COLUMN IF NOT EXISTS student_fee_map_id BIGINT NULL,
    ADD COLUMN IF NOT EXISTS transaction_reference VARCHAR(255) NULL,
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

SET @has_gateway_col := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'payment_transactions'
      AND column_name = 'gateway_txn_id'
);
SET @sql := IF(
    @has_gateway_col = 1,
    'UPDATE payment_transactions SET transaction_reference = gateway_txn_id WHERE transaction_reference IS NULL AND gateway_txn_id IS NOT NULL',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE payment_transactions pt
JOIN students s ON s.id = pt.student_id
LEFT JOIN classes c ON c.id = s.class_id
SET pt.school_id = c.school_id
WHERE pt.school_id IS NULL
  AND c.school_id IS NOT NULL;

SET @has_idx := (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'payment_transactions'
      AND index_name = 'idx_payment_transactions_school_id'
);
SET @sql := IF(@has_idx = 0, 'CREATE INDEX idx_payment_transactions_school_id ON payment_transactions(school_id)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_idx := (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'payment_transactions'
      AND index_name = 'idx_payment_transactions_student_fee_map_id'
);
SET @sql := IF(
    @has_idx = 0,
    'CREATE INDEX idx_payment_transactions_student_fee_map_id ON payment_transactions(student_fee_map_id)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_fk := (
    SELECT COUNT(*)
    FROM information_schema.key_column_usage k
    WHERE k.table_schema = DATABASE()
      AND k.table_name = 'payment_transactions'
      AND k.column_name = 'student_fee_map_id'
      AND k.referenced_table_name = 'student_fee_maps'
      AND k.referenced_column_name = 'id'
);
SET @sql := IF(
    @has_fk = 0,
    'ALTER TABLE payment_transactions ADD CONSTRAINT fk_payment_txn_student_fee_map FOREIGN KEY (student_fee_map_id) REFERENCES student_fee_maps(id)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ------------------------------------------------------------
-- 6) Receipts: school scope + canonical column names
-- ------------------------------------------------------------
ALTER TABLE receipts
    ADD COLUMN IF NOT EXISTS school_id BINARY(16) NULL,
    ADD COLUMN IF NOT EXISTS receipt_number VARCHAR(50) NULL,
    ADD COLUMN IF NOT EXISTS pdf_url VARCHAR(255) NULL,
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

SET @has_receipt_no_col := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'receipts'
      AND column_name = 'receipt_no'
);
SET @sql := IF(
    @has_receipt_no_col = 1,
    'UPDATE receipts SET receipt_number = receipt_no WHERE receipt_number IS NULL AND receipt_no IS NOT NULL',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE receipts
SET receipt_number = CONCAT('REC-LEGACY-', id)
WHERE receipt_number IS NULL;

SET @has_file_url_col := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'receipts'
      AND column_name = 'file_url'
);
SET @sql := IF(
    @has_file_url_col = 1,
    'UPDATE receipts SET pdf_url = file_url WHERE pdf_url IS NULL AND file_url IS NOT NULL',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE receipts r
JOIN payment_transactions pt ON pt.id = r.transaction_id
SET r.school_id = pt.school_id
WHERE r.school_id IS NULL;

ALTER TABLE receipts
    MODIFY COLUMN receipt_number VARCHAR(50) NOT NULL;

SET @has_idx := (
    SELECT COUNT(*)
    FROM (
        SELECT s.index_name
        FROM information_schema.statistics s
        WHERE s.table_schema = DATABASE()
          AND s.table_name = 'receipts'
          AND s.non_unique = 0
        GROUP BY s.index_name
        HAVING COUNT(*) = 1
           AND MAX(CASE WHEN s.column_name = 'receipt_number' THEN 1 ELSE 0 END) = 1
    ) t
);
SET @sql := IF(@has_idx = 0, 'CREATE UNIQUE INDEX uk_receipts_receipt_number ON receipts(receipt_number)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_idx := (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'receipts'
      AND index_name = 'idx_receipts_school_id'
);
SET @sql := IF(@has_idx = 0, 'CREATE INDEX idx_receipts_school_id ON receipts(school_id)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
