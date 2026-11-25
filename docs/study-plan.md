---
title: "4-week study plan"
order: 1
---

# 4-week study plan (20 workdays, 2–4 h/day)

**Goal:** pass 1Z0-831 (Java SE 25 Developer): 50 questions, 120 minutes, pass mark about 68%.

**How each day works**

1. **Read** the topic summary in *Study* (it marks 🆕 what changed since Java 17). Set its status to *reading* or *done* and rate your confidence.
2. **Practise** in *Practice* → choose the set, filter **Not mastered yet**, set **How many**, mode **Practice**. Progress is saved per question, so "Continue" always picks up where you stopped.
3. **Review mistakes** at the end of the day: Dashboard → *Mistakes (n)* for that domain. For each wrong answer, read the explanation and write a one-line rule in the topic notes.
4. Tick the day off below.

Aim for **≥ 75%** accuracy per domain before week 4. Anything under 68% shows in red on the dashboard. Those domains get the extra time in week 4.

> The JDBC chapter (Java 17 book, chapter 10) is **optional**: JDBC does not appear in the 1Z0-831 objectives. See [exam objectives](exam-objectives.md).

## Week 1 — Foundations

### Day 1 — Orientation + D1 values
- Read **Java 17 → 25 gap** (skim, 30 min). It shows which parts of the Java 17 material changed.
- Read **D1 summary** (numbers, String/StringBuilder, text blocks, Date-Time).
- Practise: *Ch 1 Handling Date, Time…*, **35 questions**.

### Day 2 — D1 finish + D2 flow
- Practise: *Ch 1*, the remaining **35 questions**.
- Practise: *Gap: Smaller API additions*, the D1 items (Math.clamp, String/StringBuilder additions).
- Read **D2 summary**, up to and including switch expressions.

### Day 3 — Pattern matching
- Read **D2 summary**: pattern matching for switch, record patterns, `_`.
- Practise: *Ch 2 Controlling Program Flow*, **all 40**.
- Practise: *Gap: Pattern matching for switch* + *Gap: Record patterns*.
- Review: mistakes for D1 + D2.

### Day 4 — D3 OOP part 1
- Read **D3 summary**: classes, initialization order, constructors (🆕 flexible constructor bodies), nested classes, overloading.
- Practise: *Ch 3 Object-Oriented*, **50 questions**.
- Practise: *Gap: Flexible constructor bodies*.

### Day 5 — D3 OOP part 2 + weekly review
- Read **D3 summary**: inheritance, overriding, abstract, sealed, records, 🆕 unnamed variables.
- Practise: *Ch 3*, **50 questions** + *Gap: Unnamed variables and patterns*.
- Weekly review: mistakes for all domains so far. Re-read the "Exam traps" of D1–D3.

## Week 2 — OOP, exceptions, collections, streams

### Day 6 — D3 OOP part 3
- Read **D3 summary**: interfaces, functional interfaces, enums, polymorphism and casting.
- Practise: *Ch 3*, **65 questions**.

### Day 7 — D3 finish + D4 exceptions
- Practise: *Ch 3*, the remaining **65 questions**.
- Read **D4 summary** (exceptions).

### Day 8 — D4 + start D5
- Practise: *Ch 4 Handling Exceptions*, **all 60**.
- Read **D5 summary**: arrays, collections, generics, 🆕 sequenced collections.

### Day 9 — D5 collections
- Practise: *Ch 5 Arrays and Collections*, **all 80**.
- Practise: *Gap: Sequenced collections*.

### Day 10 — D6 streams part 1 + weekly review
- Read **D6 summary**: lambdas, functional interfaces, method references, stream basics.
- Practise: *Ch 6 Streams and Lambdas*, **50 questions**.
- Weekly review: mistakes for D3–D5.

## Week 3 — Streams, modules, concurrency, I/O

### Day 11 — D6 streams part 2
- Read **D6 summary**: collectors, grouping/partitioning, primitive streams, 🆕 gatherers.
- Practise: *Ch 6*, **50 questions** + *Gap: Stream gatherers*.

### Day 12 — D6 finish + D7 modules
- Practise: *Ch 6*, the remaining **50 questions**.
- Read **D7 summary**: modules, services, CLI, 🆕 compact source files and module imports.

### Day 13 — D7 modules
- Practise: *Ch 7 Packaging and Modules*, **45 questions**.
- Practise: *Gap: Compact source files…*.
- Try it: write a tiny 2-module project and run `javac`/`java -p`/`jdeps` yourself.

### Day 14 — D7 finish + D8 concurrency
- Practise: *Ch 7*, the remaining **45 questions**.
- Read **D8 summary**: threads, 🆕 virtual threads, executors, locks, 🆕 scoped values.
- Practise: *Ch 8 Concurrency*, **20 questions**.

### Day 15 — D8 + D9 + weekly review
- Practise: *Ch 8*, the remaining **20 questions** + *Gap: Virtual threads* + *Gap: Scoped values*.
- Read **D9 summary** (I/O, NIO.2, serialization).
- Weekly review: mistakes for D6–D8.

## Week 4 — Finish the material, then mock exams

### Day 16 — D9 I/O + D10 localization
- Practise: *Ch 9 Java I/O*, **all 40**.
- Read **D10 summary**. Practise: *Ch 11 Localization*, **all 30**.
- Optional: *Ch 10 JDBC* (not on the Java 25 exam).

### Day 17 — Mock exam 1
- *Practice → Mock exams → Practice Exam 1* (exam mode, timed). No notes.
- Review every wrong answer in the result screen. Note the weak domains.
- Practise: *Mistakes* for your weakest domain.

### Day 18 — Mock exam 2 + Java 25 mock
- *Practice Exam 2* (timed).
- *Random Java 25 mock* (50 questions weighted by domain, includes gap questions).
- Review the mistakes from both.

### Day 19 — Mock exam 3 + targeted repair
- *Practice Exam 3* (timed).
- Re-read every **🆕 section** and every **Exam traps** list.
- Practise: *Mistakes only* across all questions.

### Day 20 — Final check
- One more *Random Java 25 mock*. Target: ≥ 75%.
- Practise only mistakes and low-confidence domains. Stop early and rest.
- Exam-day tips: read the last line of each question first ("Which **two**…"). Check for compile errors **before** you trace the output. Flag long questions and come back to them.
