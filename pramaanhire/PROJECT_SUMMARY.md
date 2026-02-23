# 📘 PramaanHire - Complete Project Documentation

## 🚀 Project Overview
PramaanHire is an **Evidence-Based AI-Powered Hiring Platform** designed to streamline the recruitment process. It features role-based access for HR and Candidates, automated resume parsing, intelligent AI evaluation, and automated communication workflows.

---

## 🌟 Key Differentiators
- **AI-Assisted Bulk Shortlisting**: Automatically ranks and shortlists top candidates while filtering out others.
- **Automated Constructive Feedback**: Rejected candidates receive personalized AI feedback (Strengths, Weaknesses, Tips) to help them improve.
- **Resume-to-JD Alignment Scoring**: AI evaluates how well a resume matches the specific job description.
- **Fairness Protection**: Critical job details (Location, Questions) are locked once applications are received to ensure a level playing field.
- **Async AI Processing**: Heavy AI tasks run in the background with a robust retry mechanism for reliability.
- **Idempotent Email Triggering**: Prevents duplicate notifications even if HR clicks buttons multiple times.

---

## 🏗️ Architecture Overview
The system follows a clean **Layered Architecture**:
`Controller` → `Service` → `Repository` → `Database`

- **Async Processing**: AI evaluation is triggered via `TransactionSynchronizationManager` only *after* the database transaction commits, ensuring data consistency.
- **Event-Driven Emails**: Notifications are decoupled from core logic and triggered based on status transitions.
- **Abstracted Storage**: File storage logic is isolated in a service, making it easy to swap local storage for Cloud (S3) in the future.

---

## 🛠️ Tech Stack & Dependencies
- **Framework**: Spring Boot 3.3.2 (Java 17)
- **Frontend**: React + Vite + Tailwind CSS (v3) + Recharts + Framer Motion
- **Database**: MySQL 8.0+ (Spring Data JPA)
- **Security**: Spring Security + JWT (Stateless) + CORS Config
- **AI Engine**: Spring AI + Groq API (`llama-3.3-70b-versatile`)
- **Resume Parsing**: Apache PDFBox (`2.0.29`)
- **Excel Export**: Apache POI (`poi-ooxml 5.2.5`)
- **Email**: Spring Boot Starter Mail (JavaMailSender)
- **File Storage**: Local File System (`D:/uploads`)
- **Documentation**: Swagger UI / OpenAPI (`springdoc-openapi-starter-webmvc-ui 2.5.0`)

---

## 🔐 Security Highlights
- **JWT Expiration Handling**: Tokens expire in 5 minutes; frontend handles 401 errors gracefully.
- **Role-Based Access Control (RBAC)**: Strict `@PreAuthorize` checks for HR vs Candidate endpoints.
- **Ownership Validation**: HR can only view/edit jobs and applications they created.
- **CORS Configuration**: Securely allows requests only from the frontend origin.
- **MIME Validation**: Ensures only PDF files are uploaded.
- **Input Validation**: Strict validation for screening answers and job details.

---

## 👔 2. HR Module (Job Management)
- **Dashboard**:
  - KPI Cards (Open Jobs, Total Apps, Shortlisted, Hired).
  - Charts: Application Status Donut Chart, 30-Day Trend Line Chart.
  - AI Snapshot: Average/High/Low scores.
  - Recent Activity Feed.
- **Create Job**: HR defines title, description, employment type, and deadline.
- **Screening Questions**: HR adds custom questions (Mandatory/Optional, Max Length).
- **Update Job**:
  - **Safe Fields**: Title, Description, Deadline (always editable).
  - **Restricted Fields**: Location, Questions (Locked if applications > 0 to ensure fairness).
- **My Jobs**: List view of all posted jobs with applicant counts.

---

## 📋 3. HR Action Module (Candidate Management)
- **View Applications**:
  - Advanced Filtering: Status, Search (Name/Email).
  - Pagination: Full support.
  - Sorting: By AI Score (High to Low) by default.
- **Full Application Details**:
  - Resume Download.
  - **AI Evaluation**: Score, Summary, Strengths, Weaknesses, Tips.
  - Candidate's Answers to Screening Questions.
  - HR Notes.
- **Manual Status Update**:
  - Transitions: `SUBMITTED` -> `UNDER_REVIEW` -> `SHORTLISTED` -> `HIRED`.
  - Rejection allowed from any stage.
  - **Idempotency**: Prevents duplicate emails if status is updated to the same value.
- **Bulk Action (AI Shortlist)**:
  - **Input**: Top N (e.g., 5).
  - **Logic**:
    - **Top N**: Moved to `UNDER_REVIEW` (Under Consideration).
    - **Others**: Moved to `REJECTED` (Auto-rejected).
- **Excel Export**: Downloads all application data (including AI feedback) as `.xlsx`.

---

## 🔍 4. Candidate Module
- **Dashboard**:
  - Welcome Banner.
  - KPI Cards (Total Applied, Status Breakdown).
  - Application Activity Chart.
  - AI Performance Snapshot (Avg/High/Latest Score).
  - Recent Updates Feed.
- **Find Jobs**:
  - **Search**: By Title/Skill.
  - **Filters**: Location, Date Posted (24h, 7d, 30d).
  - **Status Indicators**: "Applied" badge on jobs already applied to.
- **Application Flow**:
  1. **View Job**: See description (Questions hidden).
  2. **Apply**: Upload PDF Resume + Answer Questions.
  3. **Validation**: Checks mandatory answers, word limits, and file type.
- **My Applications**:
  - List of all submissions with status.
  - **View Details**: See submitted resume, answers, and **AI Feedback** (if Rejected/Shortlisted).
  - **Withdraw**: Allowed only if status is `SUBMITTED`.

---

## 🧠 5. AI Engine & Resume Parsing
This is the core intelligence layer.

### **A. Resume Parsing**
- **Tool**: Apache PDFBox.
- **Process**: Extracts raw text from the uploaded PDF immediately upon submission.

### **B. AI Evaluation (Async)**
- **Provider**: **Groq** (Model: `llama-3.3-70b-versatile`).
- **Trigger**: Runs asynchronously (`@Async`) after the application transaction commits.
- **Input Prompt**: Job Description + Candidate Resume Text + Answers.
- **Output**:
  - **Score**: 0-100
  - **Summary**: Brief overview.
  - **Strengths & Weaknesses**: Key points.
  - **Improvement Tips**: Actionable advice.
- **Storage**: Results saved in DB.

### **C. Resilience (Background Scheduler)**
- **Component**: `AiRetryScheduler`
- **Frequency**: Runs every **5 minutes**.
- **Logic**: Retries evaluation for any application where `isAiProcessed = false`.

---

## 📧 6. Automated Email Notifications
Powered by `JavaMailSender` (SMTP).

| Trigger Event | Email Sent? | Content |
|:--- |:--- |:--- |
| **Application Submitted** | ✅ Yes | Confirmation of receipt. |
| **Status -> UNDER_REVIEW** | ❌ No | Silent update. |
| **Status -> SHORTLISTED** | ✅ Yes | "Good News! You've been shortlisted." |
| **Status -> HIRED** | ✅ Yes | "Congratulations! Offer inside." |
| **Status -> REJECTED** | ✅ Yes | **AI Feedback Included** (Strengths, Weaknesses, Tips). |
| **Bulk AI Rejection** | ✅ Yes | Same as manual rejection. |

---

## 🚀 Future Improvements
- **Cloud File Storage**: Migrate from local disk to AWS S3 or Google Cloud Storage.
- **Docker Containerization**: Dockerize the app for easy deployment.
- **CI/CD Pipeline**: Automate testing and deployment via GitHub Actions.
- **AI Bias Monitoring**: Implement checks to ensure AI fairness across demographics.
- **Interview Scheduling**: Integrate Calendly or internal scheduler.
- **In-App Notifications**: Real-time bell notifications using WebSockets.

---

## 📡 API Endpoints Reference

### **Authentication**
- `POST /api/auth/signup` - Register
- `POST /api/auth/login` - Login

### **Public**
- `GET /api/jobs` - List Jobs (Filters: title, location, datePosted)
- `GET /api/jobs/{id}` - View Job (No questions)
- `GET /api/files/{name}` - Download File

### **HR Operations**
- `POST /api/hr/jobs` - Create Job
- `PUT /api/hr/jobs/{id}` - Update Job (Restricted)
- `GET /api/hr/jobs` - List My Jobs
- `GET /api/hr/dashboard` - HR Dashboard Stats
- `GET /api/hr/jobs/{id}` - Job Details
- `GET /api/hr/jobs/{id}/applications` - List Applications (Filter/Search)
- `GET /api/hr/jobs/{id}/applications/export` - **Download Excel**
- `POST /api/hr/jobs/{id}/shortlist-top` - **Bulk AI Shortlist**
- `GET /api/hr/applications/{id}` - Full App Details
- `PATCH /api/hr/applications/{id}/status` - Update Status
- `GET /api/hr/applications/{id}/actions` - Get Allowed Transitions

### **Candidate Operations**
- `GET /api/candidate/jobs/dashboard` - **Candidate Dashboard**
- `GET /api/candidate/jobs/{id}/prepare-application` - View Questions
- `POST /api/candidate/jobs/{id}/apply` - Submit App
- `GET /api/candidate/jobs/my-applications` - List Apps
- `GET /api/candidate/jobs/my-applications/{id}` - View App
- `POST /api/candidate/jobs/my-applications/{id}/withdraw` - Withdraw

---

## ⚙️ Configuration (`application.properties`)
- **Groq API**: Configured for free, high-speed inference.
- **JWT**: 5-minute expiration.
- **File Storage**: `D:/uploads`.
- **Email**: SMTP (Gmail).
- **Async**: Enabled.
- **Scheduling**: Enabled.
