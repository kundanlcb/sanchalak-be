-- V4__add_school_id_to_core_entities.sql
-- Adds multi-tenancy support (school_id) to core entities.

-- Backfill helper: Use a sentinel UUID for legacy platform data if needed, 
-- but generally we want to link existing students/teachers to their respective schools.

-- 1. Add school_id to tables
ALTER TABLE users ADD COLUMN school_id BINARY(16) NULL;
ALTER TABLE teachers ADD COLUMN school_id BINARY(16) NULL;
ALTER TABLE students ADD COLUMN school_id BINARY(16) NULL;
ALTER TABLE parents ADD COLUMN school_id BINARY(16) NULL;
ALTER TABLE subjects ADD COLUMN school_id BINARY(16) NULL;
ALTER TABLE exam_terms ADD COLUMN school_id BINARY(16) NULL;
ALTER TABLE exam_schedules ADD COLUMN school_id BINARY(16) NULL;
ALTER TABLE homework ADD COLUMN school_id BINARY(16) NULL;
ALTER TABLE notices ADD COLUMN school_id BINARY(16) NULL;
ALTER TABLE routes ADD COLUMN school_id BINARY(16) NULL;
ALTER TABLE vehicles ADD COLUMN school_id BINARY(16) NULL;

-- 2. Add Indexes
CREATE INDEX idx_users_school_id ON users(school_id);
CREATE INDEX idx_teachers_school_id ON teachers(school_id);
CREATE INDEX idx_students_school_id ON students(school_id);
CREATE INDEX idx_parents_school_id ON parents(school_id);
CREATE INDEX idx_subjects_school_id ON subjects(school_id);
CREATE INDEX idx_exam_terms_school_id ON exam_terms(school_id);
CREATE INDEX idx_exam_schedules_school_id ON exam_schedules(school_id);
CREATE INDEX idx_homework_school_id ON homework(school_id);
CREATE INDEX idx_notices_school_id ON notices(school_id);
CREATE INDEX idx_routes_school_id ON routes(school_id);
CREATE INDEX idx_vehicles_school_id ON vehicles(school_id);

-- 3. Backfill from linked entities where possible
-- Backfill students from classes
UPDATE students s 
JOIN classes c ON s.class_id = c.id 
SET s.school_id = c.school_id 
WHERE s.school_id IS NULL AND c.school_id IS NOT NULL;

-- Backfill teachers from class_subjects/classes
UPDATE teachers t
JOIN class_subjects cs ON t.id = cs.teacher_id
JOIN classes c ON cs.class_id = c.id
SET t.school_id = c.school_id
WHERE t.school_id IS NULL AND c.school_id IS NOT NULL;

-- Backfill parents from students
UPDATE parents p
JOIN parent_student_links psl ON p.id = psl.parent_id
JOIN students s ON psl.student_id = s.id
SET p.school_id = s.school_id
WHERE p.school_id IS NULL AND s.school_id IS NOT NULL;

-- 4. Set default school for remaining if necessary (Optional, based on business rules)
-- For now, we leave them NULL to indicate platform-level or unassigned.
