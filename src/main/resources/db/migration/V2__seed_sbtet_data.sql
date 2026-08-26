-- V2__seed_sbtet_data.sql
-- Seed historical SBTET reference data, master branches, schemes, exam types, exam month-year pairs, colleges, and badges

-- 1. SBTET Schemes
INSERT INTO sbtet_scheme (scheme_code, sbtet_scheme_id, is_active) VALUES
('C26', 12, TRUE),
('C24', 11, TRUE),
('ER2020', 10, TRUE),
('C21', 9, TRUE),
('C09', 8, FALSE),
('C08', 7, FALSE),
('C05', 6, FALSE),
('C18', 5, TRUE),
('C16S', 4, FALSE),
('C16', 3, FALSE),
('ER91', 2, TRUE),
('C14', 1, FALSE);

-- 2. SBTET Exam Types
INSERT INTO sbtet_exam_type (sbtet_exam_type_id, exam_name, is_active) VALUES
(1, 'Mid 1', TRUE),
(2, 'Mid 2', TRUE),
(5, 'Semester', TRUE);

-- 3. SBTET Semesters
INSERT INTO sbtet_semester (sem_id, sequence_id, is_active) VALUES
('1SEM', 1, TRUE),
('2SEM',2, TRUE),
('3SEM',3, TRUE),
('4SEM',4, TRUE),
('5SEM',5, TRUE),
('6SEM',6, TRUE);

-- 4. Confirmed SBTET Exam Month / Year Historical Table (Complete confirmed list)
INSERT INTO sbtet_exam_month_year (sbtet_id, exam_year_month, is_active) VALUES
(103, 'OCT-2026', TRUE),
(102, 'SEP-2026', TRUE),
(101, 'JUL-2026', TRUE),
(100, 'JUN-2026', TRUE),
(99,  'APR-2026', TRUE),
(98,  'MAR-2026', TRUE),
(97,  'JAN-2026', TRUE),
(96,  'DEC-2025-SUPPLY', TRUE),
(95,  'DEC-2025', TRUE),
(94,  'OCT-2025', TRUE),
(93,  'SEP-2025', TRUE),
(92,  'JUN-2025', TRUE),
(91,  'APR-2025', TRUE),
(90,  'MAR-2025', TRUE),
(89,  'JAN-2025', TRUE),
(88,  'DEC-2024', TRUE),
(87,  'C24-DEC-2024', TRUE),
(86,  'C24-NOV-2024', TRUE),
(85,  'C21-NOV-2024', TRUE),
(84,  'SEP-2024-C24', TRUE),
(82,  'SEP-2024', TRUE),
(80,  'AUG-2024', TRUE),
(79,  'JUN-2024', TRUE),
(78,  'APR-2024', TRUE),
(77,  'MAR-2024', TRUE),
(76,  'JAN-2024', TRUE),
(75,  'DEC-2023', TRUE),
(74,  'OCT-2023', TRUE),
(73,  'AUG-2023', TRUE),
(72,  'JUN-2023', TRUE),
(71,  'APR-2023', TRUE),
(70,  'MAR-2023', TRUE),
(69,  'FEB-2023', TRUE),
(68,  'NOV-2022', TRUE),
(67,  'OCT-2022', TRUE),
(66,  'DEC-2022', TRUE),
(65,  'AUG-2022', TRUE),
(64,  'JULY-2022', TRUE),
(63,  'JUN-2022', TRUE),
(62,  'MAY-2022', TRUE),
(61,  'APR-2022', TRUE),
(60,  'MAR-2022', TRUE),
(59,  'JAN-2022', TRUE),
(58,  'NOV-2021', TRUE),
(57,  'OCT-2021', TRUE),
(56,  'Aug-2021', TRUE),
(55,  'JULY-2021', TRUE),
(53,  'MAY-2021', TRUE),
(52,  'APRIL-2021', TRUE),
(51,  'JUNE 2021', TRUE),
(50,  'March 2021', TRUE),
(49,  'February 2021', TRUE),
(48,  'DECEMBER-2020', TRUE),
(45,  'OCT-2020', TRUE),
(44,  'Sept-2020', TRUE),
(43,  'July-2020', TRUE),
(41,  'June 2020', TRUE),
(5,   'April-2020', TRUE),
(4,   'Nov-Dec 2019', TRUE),
(6,   'Nov-2019', TRUE),
(3,   'June 2019', TRUE),
(2,   'Mar-Apr 2019', TRUE),
(1,   'Oct-Nov 2018', TRUE);

-- 5. Master Branches (All 35 official SBTET branch codes from the spec).
-- Only code + a standard display name are seeded — no descriptive blurb, since that text
-- was never sourced from anything confirmed and would just be invented filler.
INSERT INTO branches (code, name) VALUES
('CS', 'Computer Engineering'),
('AI', 'Artificial Intelligence & Machine Learning'),
('EC', 'Electronics & Communication Engineering'),
('EE', 'Electrical & Electronics Engineering'),
('ME', 'Mechanical Engineering'),
('CE', 'Civil Engineering'),
('CH', 'Chemical Engineering'),
('AA', 'Automobile Architecture & Engineering'),
('AU', 'Automobile Engineering'),
('BM', 'Bio-Medical Engineering'),
('CCB', 'Cloud Computing & Big Data'),
('CPS', 'Cyber Physical Systems & Security'),
('EI', 'Electronics & Instrumentation Engineering'),
('ES', 'Embedded Systems'),
('HS', 'Humanities & Sciences'),
('LF', 'Leather & Footwear Technology'),
('LG', 'Land Surveying & Geo-informatics'),
('MN', 'Mining Engineering'),
('MT', 'Metallurgical Engineering'),
('PK', 'Packaging Technology'),
('TF', 'Textile Technology'),
('AME', 'Applied Mechanical Engineering'),
('AMT', 'Applied Metallurgical Technology'),
('AR', 'Architectural Assistantship'),
('BT', 'Biotechnology'),
('CBM', 'Computer & Bio-Medical Engineering'),
('CBS', 'Cyber Security'),
('CRV', 'Ceramic Technology'),
('CTM', 'Construction Technology & Management'),
('EEV', 'Electric & Hybrid Vehicle Technology'),
('ID', 'Industrial Electronics'),
('LD', 'Leather Goods & Garments'),
('MEE', 'Mechanical (Energy & Environment)'),
('PDA', 'Plastics & Die Manufacturing'),
('SCT', 'Specialized Chemical Technology');

-- NOTE: No colleges, college_branches, or per-college communities are seeded here.
-- Colleges are real institutions with real 3-digit SBTET codes — inventing sample rows for
-- them here would be exactly the kind of unverified "real-looking" mock data the project
-- explicitly forbids. Per the approval-chain design, only an authenticated ADMIN can create a
-- college (POST /api/admin/colleges), and CollegeService already auto-creates that college's
-- community row at the same time — so there is nothing to pre-seed. Add real colleges through
-- the Admin UI/API once the app is running.

-- 8. Statewide Community (a real singleton — there is always exactly one, unlike a college)
INSERT INTO communities (name, slug, description, community_type) VALUES
('Telangana State SBTET Community', 'statewide', 'Official statewide community for all Telangana SBTET Polytechnic students, alumni, and faculty.', 'STATEWIDE');

-- 10. Badges Master
INSERT INTO badges (code, name, description, icon_name, points_required) VALUES
('PERFECT_ATTENDANCE', 'Attendance Champion', 'Maintained >= 95% attendance standing', 'Award', 100),
('ACADEMIC_EXCELLENCE', 'SGPA High Achiever', 'Achieved SGPA >= 9.0 in semester results', 'Star', 150),
('DOUBT_CRUSHER', 'Peer Helper', 'Answered 10+ doubts for junior peers', 'HelpCircle', 80),
('COMMUNITY_VOICE', 'Active Contributor', 'Created 5+ helpful posts in community', 'MessageSquare', 50),
('VERIFIED_MENTOR', 'Senior Mentor', 'Successfully guided 3+ diploma mentees', 'ShieldCheck', 200);
