# OCP Java SE 25 Trainer (exam 1Z0-831)

A local study app: topic summaries, 1000+ practice questions, a 4-week plan and progress tracking.
Built with Kotlin, Spring Boot 4, Thymeleaf and SQLite.

## Run

```bash
mvn spring-boot:run
```

Open <http://localhost:8025>. Run it from this folder. The app reads `docs/` and `practice/` from the working directory and stores progress in `data/progress.db`.

Requirements: JDK 25 and Maven.

## What's where

| Path | Content |
|---|---|
| `docs/study-plan.md` | 20-day plan. The *Study plan* page builds its daily checklist from the `### Day N — …` headings. |
| `docs/exam-objectives.md` | 1Z0-831 domains and how they map to the practice chapters |
| `docs/java17-to-25-gap.md` | Everything new in Java 18–25 that matters for the exam |
| `docs/topics/d01…d10-*.md` | Topic summaries. Read these before practising a domain. |
| `docs/CONTENT-FORMAT.md` | Markdown format for questions and summaries. Add your own here. |
| `practice/ocp17-829/` | 1010 questions extracted from the 1Z0-829 (Java 17) practice-test book, with answers and explanations. Questions with diagrams include a scan of the original page (`img/`). |
| `practice/java25-gap/` | Questions on Java 18–25 features, checked against JDK 25 |
| `resources/` | Source PDFs, plus `jls25.txt` (searchable text of the JLS) |
| `data/progress.db` | Your progress (SQLite). Back it up if you care about it. |

## Using the app

- **Dashboard**: days left until the exam (set the date there), accuracy and mastery per exam domain, recent sessions and 4 weeks of activity.
- **Study**: rendered topic summaries. Track a status (not started / reading / done), a 0–5 confidence rating and notes per domain.
- **Practice**: start a session by set, domain or source. Filter: *not mastered*, *unseen* or *mistakes*.
  - *Practice mode* shows feedback and the explanation after each answer.
  - *Exam mode* is timed (120 min per 50 questions) and shows results with a full review at the end.
  - *Random Java 25 mock* draws 50 questions weighted by domain and mixes in gap questions. JDBC is excluded.
- Keyboard: `A`–`H` select an option, `Enter` checks the answer or goes to the next question.
- **Reload content** (bottom of the dashboard) re-reads the markdown after you edit it. Parse errors show in red in the top bar.

"Mastered" means your **latest** attempt at a question was correct. Get a question wrong and it counts as a mistake again until you answer it correctly.

## Notes

- The extracted Java 17 questions come from your own PDF and are for personal study only. The extraction is automated. If a question looks garbled, its page scan or the PDF in `resources/` is the reference.
- Tests: `mvn test`. They check that every file in `practice/` parses and that question ids are unique.
