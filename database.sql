-- ====================================================================
-- CAMPUS RECRUITMENT MANAGEMENT SYSTEM
-- Database Schema Script for MySQL
-- Database Name: campus_recruitment_db
-- ====================================================================

CREATE DATABASE IF NOT EXISTS campus_recruitment_db;
USE campus_recruitment_db;

-- -----------------------------------------------------
-- Table 1: users
-- Stores authentication and role information for all user types
-- -----------------------------------------------------
DROP TABLE IF EXISTS applications;
DROP TABLE IF EXISTS job_drives;
DROP TABLE IF EXISTS companies;
DROP TABLE IF EXISTS students;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('STUDENT', 'ADMIN', 'COMPANY')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- -----------------------------------------------------
-- Table 2: students
-- Stores academic and profile details of registered students
-- -----------------------------------------------------
CREATE TABLE students (
    student_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL UNIQUE,
    branch VARCHAR(50) NOT NULL,
    cgpa DECIMAL(3,2) NOT NULL,
    graduation_year INT NOT NULL,
    active_backlogs INT NOT NULL DEFAULT 0,
    skills TEXT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- -----------------------------------------------------
-- Table 3: companies
-- Stores company/recruiter organization profiles
-- -----------------------------------------------------
CREATE TABLE companies (
    company_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL UNIQUE,
    company_name VARCHAR(100) NOT NULL,
    company_description TEXT,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- -----------------------------------------------------
-- Table 4: job_drives
-- Stores job postings created by recruiters along with eligibility criteria
-- -----------------------------------------------------
CREATE TABLE job_drives (
    job_id INT PRIMARY KEY AUTO_INCREMENT,
    company_id INT NOT NULL,
    job_role VARCHAR(100) NOT NULL,
    package VARCHAR(50) NOT NULL,
    minimum_cgpa DECIMAL(3,2) NOT NULL,
    eligible_branches VARCHAR(255) NOT NULL,
    maximum_backlogs INT NOT NULL DEFAULT 0,
    required_skills TEXT NOT NULL,
    job_description TEXT NOT NULL,
    application_deadline DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES companies(company_id) ON DELETE CASCADE
);

-- -----------------------------------------------------
-- Table 5: applications
-- Tracks job applications submitted by students and hiring pipeline status
-- -----------------------------------------------------
CREATE TABLE applications (
    application_id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    job_id INT NOT NULL,
    application_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    application_status VARCHAR(30) NOT NULL DEFAULT 'Applied' 
        CHECK (application_status IN ('Applied', 'Shortlisted', 'Aptitude Test', 'Interview', 'Selected', 'Rejected')),
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (job_id) REFERENCES job_drives(job_id) ON DELETE CASCADE,
    CONSTRAINT unique_student_job UNIQUE (student_id, job_id)
);

-- ====================================================================
-- INITIAL SEED DATA FOR TESTING & PREVIEW
-- ====================================================================

-- 1. Admin Account (Default login: admin@campus.com / admin123)
INSERT INTO users (name, email, password, role) 
VALUES ('Head Placement Officer', 'admin@campus.com', 'admin123', 'ADMIN');

-- 2. Company Recruiter Accounts
INSERT INTO users (name, email, password, role) 
VALUES ('Google HR Team', 'hr@google.com', 'google123', 'COMPANY');
INSERT INTO companies (user_id, company_name, company_description)
VALUES (LAST_INSERT_ID(), 'Google', 'Google LLC is an American multinational technology company focusing on artificial intelligence, search engine technology, online advertising, cloud computing, computer software, quantum computing, e-commerce, and consumer electronics.');

INSERT INTO users (name, email, password, role) 
VALUES ('Microsoft University Relations', 'hr@microsoft.com', 'ms123', 'COMPANY');
INSERT INTO companies (user_id, company_name, company_description)
VALUES (LAST_INSERT_ID(), 'Microsoft', 'Microsoft Corporation is an American multinational corporation and technology company headquartered in Redmond, Washington.');

INSERT INTO users (name, email, password, role) 
VALUES ('TCS Early Careers', 'hr@tcs.com', 'tcs123', 'COMPANY');
INSERT INTO companies (user_id, company_name, company_description)
VALUES (LAST_INSERT_ID(), 'Tata Consultancy Services', 'Tata Consultancy Services is an Indian multinational information technology services and consulting company headquartered in Mumbai.');

-- 3. Job Drives
INSERT INTO job_drives (company_id, job_role, package, minimum_cgpa, eligible_branches, maximum_backlogs, required_skills, job_description, application_deadline)
VALUES 
(1, 'Software Engineer - Associate', '18.5 LPA', 7.50, 'CSE, IT, ECE', 0, 'Java, Data Structures, Algorithms, SQL, Git', 'We are looking for enthusiastic Computer Science and Electronics graduates to join our Core Engineering team. You will work on scalable backend services and cloud infrastructure.', '2026-10-31'),
(1, 'Cloud Support Engineer', '12.0 LPA', 7.00, 'CSE, IT, ECE, MECH', 1, 'Linux, Networking, Python, Java, Cloud Computing', 'Assist enterprise clients in troubleshooting complex distributed systems on Google Cloud Platform.', '2026-11-15'),
(2, 'Software Development Engineer I (SDE-1)', '16.0 LPA', 7.00, 'CSE, IT', 0, 'C++, Java, System Design, REST APIs', 'Build high-performance web and backend microservices powering Microsoft Azure and Office 365 services.', '2026-10-15'),
(3, 'Systems Engineer - Digital', '7.0 LPA', 6.00, 'CSE, IT, ECE, EEE, MECH, CIVIL', 2, 'Java, SQL, HTML, CSS, JavaScript', 'Join TCS Digital practice working on enterprise application development, data analytics, and software testing.', '2026-12-01');

-- 4. Sample Students
INSERT INTO users (name, email, password, role) 
VALUES ('Rahul Sharma', 'rahul.sharma@student.edu', 'student123', 'STUDENT');
INSERT INTO students (user_id, branch, cgpa, graduation_year, active_backlogs, skills)
VALUES (LAST_INSERT_ID(), 'CSE', 8.45, 2026, 0, 'Java, Python, Data Structures, SQL, HTML/CSS');

INSERT INTO users (name, email, password, role) 
VALUES ('Ananya Patel', 'ananya.patel@student.edu', 'student123', 'STUDENT');
INSERT INTO students (user_id, branch, cgpa, graduation_year, active_backlogs, skills)
VALUES (LAST_INSERT_ID(), 'IT', 7.20, 2026, 0, 'Java, Servlets, JavaScript, MySQL, Git');

INSERT INTO users (name, email, password, role) 
VALUES ('Rohan Verma', 'rohan.verma@student.edu', 'student123', 'STUDENT');
INSERT INTO students (user_id, branch, cgpa, graduation_year, active_backlogs, skills)
VALUES (LAST_INSERT_ID(), 'MECH', 6.40, 2026, 1, 'AutoCAD, SolidWorks, C Programming, Basic Java');

-- 5. Sample Applications
INSERT INTO applications (student_id, job_id, application_status) VALUES (1, 1, 'Interview');
INSERT INTO applications (student_id, job_id, application_status) VALUES (1, 3, 'Shortlisted');
INSERT INTO applications (student_id, job_id, application_status) VALUES (2, 2, 'Applied');
INSERT INTO applications (student_id, job_id, application_status) VALUES (2, 4, 'Selected');

-- ====================================================================
-- END OF DATABASE SCRIPT
-- ====================================================================
