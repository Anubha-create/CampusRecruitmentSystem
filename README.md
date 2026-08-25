# Campus Recruitment Management System

A full-stack Java web application designed to manage college campus recruitment drives, student job applications, automatic eligibility evaluations, company recruiter postings, and placement officer administration.

Built strictly using **Core Java, Java Servlets, JDBC, MySQL, HTML5, Vanilla CSS3, and Vanilla JavaScript** without third-party frameworks like Spring, Hibernate, Bootstrap, or React.

---

## 🌟 Resume Highlights for Software Engineer / ASE Roles

- **Full-Stack Java Web Architecture**: Designed and implemented an MVC-structured web portal using Java Servlets (Jakarta EE) and raw JDBC for role-based campus recruitment automation.
- **Automatic Eligibility Engine**: Built an object-oriented business logic engine (`EligibilityChecker.java`) that evaluates student academic profiles (CGPA, branch, active backlogs) against job criteria and generates transparent eligibility feedback.
- **Role-Based Access Control (RBAC)**: Enforced session-based security across three distinct user personas: **Student**, **Recruiter/Company**, and **Placement Officer/Admin**.
- **Data Access & Security**: Handled standard MySQL relational database schemas using `PreparedStatement` to prevent SQL injection vulnerabilities and transactional SQL operations.
- **Zero-Dependency Frontend**: Engineered a modern, responsive UI using Vanilla CSS variables, flexbox/grid layouts, dynamic status badges, and asynchronous ES6 JavaScript `fetch` API.

---

## 🏗️ Project Architecture & Folder Structure

```
campus-recruitment-system/
├── pom.xml                               # Maven build & embedded Tomcat dependencies
├── database.sql                          # MySQL database creation & seed data script
├── README.md                             # Project setup & interview guide
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── campus/
        │           ├── model/            # Entity Beans
        │           │   ├── User.java
        │           │   ├── Student.java
        │           │   ├── Company.java
        │           │   ├── JobDrive.java
        │           │   └── Application.java
        │           ├── dao/              # Data Access Layer (JDBC PreparedStatements)
        │           │   ├── UserDAO.java
        │           │   ├── StudentDAO.java
        │           │   ├── CompanyDAO.java
        │           │   ├── JobDriveDAO.java
        │           │   └── ApplicationDAO.java
        │           ├── servlet/          # Controller Layer (Servlets & Session RBAC)
        │           │   ├── LoginServlet.java
        │           │   ├── RegisterServlet.java
        │           │   ├── LogoutServlet.java
        │           │   ├── StudentServlet.java
        │           │   ├── CompanyServlet.java
        │           │   ├── JobDriveServlet.java
        │           │   ├── ApplicationServlet.java
        │           │   └── AdminServlet.java
        │           └── util/             # Core Utilities & Eligibility Logic
        │               ├── DBConnection.java
        │               ├── EligibilityChecker.java
        │               └── AppServer.java
        └── webapp/
            ├── WEB-INF/
            │   └── web.xml               # Web application deployment descriptor
            ├── css/
            │   └── style.css             # Vanilla CSS design system
            ├── js/
            │   ├── main.js
            │   ├── student.js
            │   ├── company.js
            │   └── admin.js
            ├── index.html                # Landing page
            ├── login.html                # Unified portal login
            ├── register.html             # Student/Company registration
            ├── student-dashboard.html    # Student profile & job drives
            ├── company-dashboard.html    # Recruiter candidate tracking
            ├── admin-dashboard.html      # Placement statistics & filters
            ├── job-list.html             # Public job drives catalog
            ├── job-details.html          # Drive details & eligibility status
            └── application-history.html # Student application status history
```

---

## 🛠️ Software Requirements

- **Java JDK**: 21 or 17+
- **Apache Maven**: 3.8+
- **MySQL Database Server**: 8.0 / 9.0+
- **Apache Tomcat**: 10.1+ (or embedded Tomcat runner included in project)

---

## 🗄️ Step 1: Database Setup

1. Open your MySQL client (MySQL Workbench, phpMyAdmin, or MySQL CLI).
2. Execute the provided `database.sql` script:

```bash
mysql -u root -p < database.sql
```

3. The script creates the `campus_recruitment_db` database, 5 tables (`users`, `students`, `companies`, `job_drives`, `applications`), constraints, and populates seed test accounts:
   - **Admin**: `admin@campus.com` / `admin123`
   - **Google HR**: `hr@google.com` / `google123`
   - **Microsoft HR**: `hr@microsoft.com` / `ms123`
   - **Student Rahul**: `rahul.sharma@student.edu` / `student123`

---

## 🚀 Step 2: How to Run the Application

### Option A: Run directly with Maven (Embedded Apache Tomcat)

Execute the following command in the project directory:

```bash
mvn exec:java
```

Once started, open your browser and navigate to:
[http://localhost:8080](http://localhost:8080)

### Option B: Deploy `.war` to Standalone Apache Tomcat

1. Build the production WAR package:
   ```bash
   mvn clean package
   ```
2. Copy `target/campus-recruitment-system.war` to your Apache Tomcat `webapps/` directory.
3. Start Tomcat (`bin/startup.bat` or `bin/startup.sh`).
4. Access at: [http://localhost:8080/campus-recruitment-system](http://localhost:8080/campus-recruitment-system)

---

## 🧪 Step 3: Testing User Flows

1. **Student Workflow**:
   - Log in as `rahul.sharma@student.edu` / `student123`.
   - View active job drives. Notice the **Eligible** / **Not Eligible** badges automatically calculated by `EligibilityChecker.java`.
   - Click **Apply Now** for an eligible job.
   - Go to **My Applications** to verify status tracking.
   - Edit your student profile (e.g. change CGPA or backlogs) and observe job eligibility updates in real time.

2. **Recruiter Workflow**:
   - Log in as `hr@google.com` / `google123`.
   - Post a new Job Drive with custom CGPA/branch criteria.
   - Review applicants in the **Applicant Candidates Pipeline**.
   - Change applicant status (e.g., move candidate to *Shortlisted*, *Interview*, or *Selected*).

3. **Admin / Placement Officer Workflow**:
   - Log in as `admin@campus.com` / `admin123`.
   - View overall metrics (Total Students, Companies, Job Drives, Applications, Placed Students).
   - Search student registry, manage recruiters, and filter application reports by company, role, or candidate name.

---

## 🐙 Step 4: Push Project to GitHub

To publish this project to your GitHub profile:

```bash
# 1. Initialize git repository
git init

# 2. Add files and make initial commit
git add .
git commit -m "Initial commit: Campus Recruitment Management System (Java Servlets, JDBC, MySQL)"

# 3. Create a new repository on GitHub (e.g. campus-recruitment-system)
# 4. Link remote and push
git remote add origin https://github.com/YOUR_GITHUB_USERNAME/campus-recruitment-system.git
git branch -M main
git push -u origin main
```

---

## 💡 Technical Interview Q&A Summary

- **Q: How does the Automatic Eligibility Engine work?**
  *A: `EligibilityChecker.java` compares student attributes (`Student` bean) against job thresholds (`JobDrive` bean). It performs precision decimal comparison for CGPA, integer check for active backlogs, and normalized string token matching for eligible academic branches.*

- **Q: How are database operations secured against SQL Injection?**
  *A: All DAO classes (`UserDAO`, `StudentDAO`, `JobDriveDAO`, `ApplicationDAO`) use JDBC `PreparedStatement` objects with parameter binding (`?`) instead of string concatenation.*

- **Q: How is role-based session management handled without Spring Security?**
  *A: Upon successful login, `LoginServlet` creates an `HttpSession` containing user details and role (`STUDENT`, `COMPANY`, `ADMIN`). Servlets validate `session.getAttribute("user")` and role privileges before executing actions.*
