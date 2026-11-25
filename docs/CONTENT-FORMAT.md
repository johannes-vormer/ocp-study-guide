# Content format

The app reads everything from markdown on startup (and on **Reload content**).
Edit or add files, then hit reload — no code changes needed.

## Topic IDs

| id | Domain (1Z0-831, Java SE 25) |
|----|------------------------------|
| `d01-values` | Handling date, time, text, numeric and boolean values |
| `d02-flow` | Program flow control (if/switch/loops, patterns in switch) |
| `d03-oop` | Object-oriented principles (classes, records, sealed, interfaces, enums, nested) |
| `d04-exceptions` | Exception handling |
| `d05-collections` | Arrays and collections (incl. sequenced collections) |
| `d06-streams` | Streams and lambdas (incl. gatherers) |
| `d07-modules` | Packaging, deploying, modules (incl. module imports, compact source files) |
| `d08-concurrency` | Concurrency (incl. virtual threads, scoped values) |
| `d09-io` | Java I/O, NIO.2, serialization |
| `d10-localization` | Localization |
| `x-jdbc` | JDBC — on the 1Z0-829 (Java 17) exam, **probably not on 1Z0-831** |
| `exam` | Mixed full mock exams |

## Topic summary files — `docs/topics/<topic-id>.md`

```markdown
---
title: "D1 — Date, Time, Text, Numeric and Boolean Values"
topic: d01-values
order: 1
---

# D1 — Date, Time, Text, Numeric and Boolean Values
...free markdown...
```

Sections marked with **🆕 Java 18–25** are new compared to the Java 17 exam.

## Question files — `practice/<set>/<file>.md`

Front matter is required. `topic` is the default for every question in the file.

````markdown
---
title: "Gap: Pattern matching"
source: java25-gap
topic: d02-flow
---

# Free text heading (ignored by the parser)

## gap-patterns-001

Question text (markdown). Code blocks allowed:

```java
Object o = 42;
```

Optional figure: ![scan](img/xyz.png)

- **A.** first option
- **B.** second option
- **C.** third option

**Answer:** A, C

**Explanation:** Why. May span several paragraphs and code blocks
until the next `## ` heading.
````

Rules:

- Every question starts with a level-2 heading whose text is a **unique id** (`## <id>`).
- Options are list items starting with `- **X.** ` (X = A–H). Extra paragraphs/code for an option are indented by two spaces.
- `**Answer:**` lists one or more letters separated by commas. More than one letter = "choose N".
- `**Explanation:**` runs until the next question heading.
- An optional line `**Topic:** d03-oop` inside a question overrides the file topic.
