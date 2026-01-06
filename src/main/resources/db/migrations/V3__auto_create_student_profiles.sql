-- V3: Auto-create student profiles for existing STUDENT/USER role users without profiles

-- Insert student profiles for STUDENT users who don't have one
INSERT INTO student_profiles (user_id, student_code, full_name)
SELECT 
    u.id,
    CONCAT('STU-', LPAD(u.id, 5, '0')),
    u.username
FROM users u
LEFT JOIN student_profiles sp ON u.id = sp.user_id
WHERE (u.role = 'STUDENT' OR u.role = 'USER')
  AND sp.id IS NULL;
