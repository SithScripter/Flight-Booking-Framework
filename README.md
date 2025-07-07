# ✈️ Flight Booking Automation Framework

This is a hybrid Selenium + TestNG automation framework for the BlazeDemo website, designed with best practices for CI/CD, reporting, and parallel execution.

---

## ✅ CURRENT STAGE (FROZEN)

🔒 This version is **frozen and tagged as `jenkins-ci-v1`**. It includes:

- ✅ Jenkins CI jobs for:
  - Smoke Tests (on every Git push)
  - Regression Suite (nightly trigger)
- ✅ Dockerized Selenium Grid (hub + Chrome + Firefox)
- ✅ Parallel execution with TestNG (multi-browser)
- ✅ Dynamic Extent Reports
- ✅ Failure summaries (for Jenkins email reports)
- ✅ Qase.io test result uploads
- ✅ Email Notifications (via `emailext` plugin)

---

## 🧪 Test Suites Overview

| Suite       | Trigger         | Runs In      | TestNG XML                          | Profile ID |
|-------------|------------------|--------------|-------------------------------------|------------|
| Smoke       | GitHub push      | Jenkins CI   | `testng-smoke.xml`                  | `smoke`    |
| Regression  | Nightly (12 AM)  | Jenkins CI   | `testng-regression.xml`             | `regression` |

> All test classes extend `BaseTest.java` and support parallel browser execution.

---

## 🧩 Technology Stack

- Java 21
- Selenium 4.26
- TestNG 7.10.2
- Maven 3.9.9
- Docker + Docker Compose
- Jenkins Declarative Pipelines
- Qase.io (test case management)
- ExtentReports 5.1.2
- Email Notifications (HTML + attachments)

---

## 🧪 Run Tests Locally (No CI)

Start Selenium Grid locally:

```bash
docker-compose -f docker-compose-grid.yml up -d


Run smoke tests:

bash
Copy
Edit
mvn clean test -P smoke -Denv=QA -Dtest.suite=smoke
Run regression suite:

bash
Copy
Edit
mvn clean test -P regression -Denv=QA -Dtest.suite=regression
Stop Selenium Grid after tests:

bash
Copy
Edit
docker-compose -f docker-compose-grid.yml down
🚀 Jenkins Job Details
1. CI Smoke Tests (Git-triggered)
Location: Jenkins > Job: CI-Smoke-Tests

Triggers on GitHub push

Uses testng-smoke.xml

Executes in parallel across Chrome + Firefox

Grid is spun up using Docker Compose

Test reports and summaries published post build

2. Nightly Regression Suite
Location: Jenkins > Job: Nightly-Regression-Suite

Triggered nightly at a scheduled time

Uses testng-regression.xml

Grid started + stopped automatically

ExtentReports and failure summaries archived

Qase.io integration and email notifications

📂 Reports and Logs
After every Jenkins build:

File	Description
reports/index.html	Full Extent HTML Report
reports/smoke-report.html	Smoke suite report
reports/regression-report.html	Regression suite report
reports/smoke-failure-summary.txt	Only failed tests summary (for email)
reports/regression-failure-summary.txt	Failed regression tests
screenshots/	Screenshots on test failures
logs/	Log4j logs per run

🔌 Docker Grid Info
File: docker-compose-grid.yml

yaml
Copy
Edit
services:
  selenium-hub:
    image: selenium/hub:4.27
    ports:
      - "4444:4444"
    environment:
      - GRID_TIMEOUT=60
      - GRID_CLEAN_UP_CYCLE=60

  chrome:
    image: selenium/node-chromium:4.27
    shm_size: "2g"
    depends_on:
      - selenium-hub
    environment:
      - SE_EVENT_BUS_HOST=selenium-hub
      - SE_NODE_MAX_SESSIONS=2

  firefox:
    image: selenium/node-firefox:4.27
    shm_size: "2g"
    depends_on:
      - selenium-hub
    environment:
      - SE_EVENT_BUS_HOST=selenium-hub
      - SE_NODE_MAX_SESSIONS=2
Both nodes support up to 2 sessions each (for parallelism).

📦 Framework Architecture
css
Copy
Edit
.
├── Jenkinsfile
├── docker-compose-grid.yml
├── pom.xml
├── reports/
├── screenshots/
├── src/
│   ├── main/
│   └── test/
│       ├── java/com/demo/flightbooking/
│       │   ├── base/
│       │   ├── pages/
│       │   ├── tests/
│       │   ├── utils/
│       │   └── listeners/
│       └── resources/
│           └── test-suites/
│               ├── testng-smoke.xml
│               └── testng-regression.xml
☁️ To Do Later (Optional Enhancements)
Feature	Purpose
GitHub Actions CI/CD	Migrate CI to GitHub-hosted runners
AWS EC2 Integration	Run tests in cloud-managed infra
Kubernetes (K8s)	Scale browser nodes dynamically
Allure Reporting	Alternative report integration
Sauce Labs / BrowserStack	Cloud cross-browser testing

👤 Author
Tester: Sithscripter (GitHub)

Framework: Java + Selenium + Jenkins + Docker

Contact: [Your email / LinkedIn]

🏁 You are currently on a stable CI checkpoint. Tag: jenkins-ci-v1

Happy Testing! 🧪🐞🚀