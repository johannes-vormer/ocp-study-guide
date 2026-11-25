---
title: "Exam objectives 1Z0-831 (Java SE 25)"
order: 2
---

# Exam 1Z0-831 — Oracle Certified Professional: Java SE 25 Developer

> ⚠️ **Verify against Oracle.** The official Oracle exam page could not be fetched when this file was written, and the 1Z0-831 PDF you mentioned is not in the project folder (only `jls25.pdf` and the 1Z0-829 practice book are). The domains below come from unofficial study guides ([savemycert/1z0-831-study-guide](https://github.com/savemycert/1z0-831-study-guide), [MyExamCloud cheat sheet](https://www.myexamcloud.com/blog/java-25-certification-preparation-cheat-sheet-1z0-831.article)). Check the "Exam topics" tab on [Oracle University](https://mylearn.oracle.com/ou/exam/java-se-25-developer-professional-1z0-831/105037/161532/270890). If you add the official PDF to `resources/`, update this file.

| Fact | Value (unofficial) |
|---|---|
| Questions | 50, multiple choice (single and "choose N") |
| Duration | 120 minutes |
| Passing score | 68% |

Weights are estimates from the unofficial guide. Oracle does not publish weights.

| # | Domain | App topic | Weight | Java 17 practice chapter |
|---|---|---|---|---|
| D1 | Handling date, time, text, numeric and boolean values | `d01-values` | ~11% | Ch 1 |
| D2 | Program flow control with decision and looping constructs | `d02-flow` | ~8% | Ch 2 |
| D3 | Object-oriented principles | `d03-oop` | ~18% | Ch 3 |
| D4 | Exception handling | `d04-exceptions` | ~8% | Ch 4 |
| D5 | Arrays and collections | `d05-collections` | ~8% | Ch 5 |
| D6 | Streams and lambda expressions | `d06-streams` | ~9% | Ch 6 |
| D7 | Packaging and deploying Java code (modules) | `d07-modules` | ~9% | Ch 7 |
| D8 | Multithreading and concurrency | `d08-concurrency` | ~11% | Ch 8 |
| D9 | Java I/O API | `d09-io` | ~11% | Ch 9 |
| D10 | Localization | `d10-localization` | ~7% | Ch 11 |
| — | JDBC: on 1Z0-829, **not listed for 1Z0-831** | `x-jdbc` | — | Ch 10 (optional) |

## Objectives (paraphrased)

**D1 — Values**
- Use primitives and wrapper classes. Evaluate arithmetic and boolean expressions: Math API, operator precedence, type conversion and casting.
- Manipulate text, including text blocks, with `String` and `StringBuilder`.
- Manipulate date, time, duration, period, instant and time-zone objects, including daylight saving time, with the Date-Time API.

**D2 — Program flow**
- Create flow control: if/else, switch statements and expressions (including 🆕 pattern matching), loops, break and continue.

**D3 — Object-oriented principles**
- Declare and instantiate objects, including nested classes. Explain the object life cycle: creation, reassigning references, garbage collection.
- Create classes and records. Define instance and static fields and methods, constructors (🆕 including flexible constructor bodies), and instance and static initializers.
- Implement overloaded methods, including varargs.
- Understand variable scope, apply encapsulation and create immutable objects. Use local variable type inference and 🆕 unnamed variables.
- Implement inheritance: abstract and sealed types, records, overriding (including `Object` methods), polymorphism, object type vs reference type.
- Create and use interfaces and identify functional interfaces. Use private, static and default methods.
- Create and use enums with fields, methods and constructors.

**D4 — Exceptions**
- Handle exceptions with try/catch/finally, try-with-resources and multi-catch, including custom exceptions.

**D5 — Arrays and collections**
- Create arrays, `List`, `Set`, `Map`, `Deque` and 🆕 sequenced collections. Add, remove, update, retrieve and sort their elements.

**D6 — Streams and lambdas**
- Use object and primitive streams, with lambdas implementing functional interfaces, to create, filter, transform, process and sort data.
- Perform decomposition, concatenation, reduction, grouping, partitioning and 🆕 gather operations on sequential and parallel streams.

**D7 — Packaging and deploying**
- Define modules and expose their content, including by reflection. Declare module dependencies. Define services, providers and consumers. Use 🆕 module import declarations.
- Compile and run Java code (🆕 compact source files, instance main methods, multi-file programs). Create modular and non-modular JARs and runtime images. Migrate to modules with unnamed and automatic modules.

**D8 — Concurrency**
- Create platform and 🆕 virtual threads. Use `Runnable` and `Callable`, manage the thread life cycle, use executor services and the concurrent API, and use 🆕 scoped values to share immutable context.
- Develop thread-safe code with locking and the concurrent API.
- Process collections concurrently and use parallel streams.

**D9 — I/O**
- Read and write console and file data with I/O streams.
- Serialize and deserialize objects.
- Construct, traverse, create, read and write `Path` objects and their properties with `java.nio.file`.

**D10 — Localization**
- Implement localization with locales and resource bundles. Parse and format messages, dates, times, numbers, currency and percentages.
