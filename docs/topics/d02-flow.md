---
title: "D2 — Program Flow Control"
topic: d02-flow
order: 2
---

# D2 — Program Flow Control

## What the exam tests

- `if`/`else`, the ternary operator and **`instanceof` pattern matching** with flow scoping
- `switch` **statements** vs `switch` **expressions**, arrow (`->`) vs colon (`:`) labels, `yield`, fall-through
- Allowed selector types, `case` constants, exhaustiveness
- 🆕 **Pattern matching for `switch`** (Java 21): type patterns, guards (`when`), `case null`, dominance, sealed-type exhaustiveness
- 🆕 **Record patterns** (Java 21), including nested patterns and `var`
- 🆕 **Unnamed patterns and variables `_`** (Java 22)
- `while`, `do`/`while`, `for`, enhanced `for`, `break`/`continue` with labels
- Unreachable code and "does not compile" detection

---

## 1. `if` / `else` and conditions

- The condition must be a `boolean` (or `Boolean`). `while (1)` / `if (x)` with `int x` → compile error.
- `if (flag = true)` compiles for a `boolean flag` (assignment yields the value) — usually a trap.
- A dangling `else` belongs to the **nearest** `if`; indentation means nothing.
- Without braces, only **one** statement is the body.

## 2. `instanceof` and pattern matching

```java
Object obj = "hi";
if (obj instanceof String s && s.length() > 1) {
    System.out.println(s.length());   // s in scope: && only evaluates RHS when the match succeeded
}
if (!(obj instanceof String t)) return;
System.out.println(t.toUpperCase());  // t in scope after the if, because the if-branch cannot complete normally
int len = obj instanceof String s ? s.length() : 0;   // works in a ternary too
```

**Flow scoping** — a pattern variable is in scope wherever the compiler can prove the match succeeded:

| Code | Compiles? |
|---|---|
| `if (o instanceof String s && s.isEmpty())` | ✓ |
| `if (o instanceof String s \|\| s.isEmpty())` | ✗ `s` not in scope (RHS runs when match **failed**) |
| `if (!(o instanceof String s)) return; s.length();` | ✓ |
| `if (!(o instanceof String s)) { log(); } s.length();` | ✗ the `if` body completes normally |
| `if (!(o instanceof String s)) {} else { s.length(); }` | ✓ |
| `String s = ""; if (o instanceof String s) {}` | ✗ `s` already defined |
| `if (o instanceof String s) { s = "new"; }` | ✓ pattern variables are **not** implicitly final |

Type rules:

- `Integer i; i instanceof String s` → compile error (types can never be compatible).
- `String str; str instanceof String t` → ✓ since Java 21 (was an error in Java 17 for pattern `instanceof`).
- `null instanceof X` is always `false`, never an exception.
- `instanceof` with a primitive type (`x instanceof int`) is **preview (JEP 507) — not on the exam**.

## 3. `switch` — the four combinations

|  | **colon `case X:`** | **arrow `case X ->`** |
|---|---|---|
| **statement** | classic; falls through until `break` | no fall-through; body is an expression statement, a block or a `throw` |
| **expression** (produces a value) | needs `yield` in each path; falls through! | value after `->`, or `{ ... yield v; }`, or `throw` |

```java
Color c = Color.GREEN;
int n = switch (c) {
    case RED -> 1;
    case GREEN -> { System.out.println("g"); yield 2; }
    case BLUE -> 3;
};                                         // semicolon after a switch expression!

int m = switch (c) {
    case RED: yield 1;
    case GREEN:
    case BLUE: yield 9;                    // GREEN falls through to here → 9
};
```

Rules:

- Arrow and colon labels **cannot be mixed** in one switch ("different case kinds used").
- A `switch` expression arm with a block must `yield` or `throw` — completing normally is an error. `return`, `break` and `continue` cannot jump out of a switch expression.
- `yield` is a contextual keyword; it is only meaningful inside a switch expression.
- A switch expression **cannot** stand alone as a statement: `switch (n) { case 1 -> 1; default -> 2; }` does not compile.
- Multiple constants per case: `case "a", "b" ->` or `case 1, 2:`.
- Type of a switch expression: if all arms are numeric, numeric promotion applies (`case 1 -> 5; default -> 2.0;` gives `5.0`); mixed types like `5` and `"x"` make the arms reference types (`5` is boxed; `var r` then holds an `Integer` for that arm). Assigning to a target type (`int r = switch ...`) requires every arm to be compatible with it.

### Fall-through (colon form)

```java
int day = 2;
switch (day) {
    case 1: System.out.print("one ");
    case 2: System.out.print("two ");      // entry point
    case 3: System.out.print("three "); break;
    default: System.out.print("def");
}                                          // prints "two three "

switch (5) {
    default: System.out.print("def ");     // default can be anywhere; checked last
    case 1: System.out.print("one ");      // falls through from default
}                                          // prints "def one "
```

- In the colon form, the whole switch block is **one scope**: `case 1: int y = 1; break; case 2: y = 2;` compiles. In the arrow form each block is its own scope.

## 4. Selector types and `case` labels

**Allowed selector types**:

- `char`, `byte`, `short`, `int` and their wrappers `Character`, `Byte`, `Short`, `Integer`
- `String`, enum types
- 🆕 **Any reference type** when the cases use patterns (`Object`, sealed interfaces, records…) — Java 21

**Not allowed**: `long`, `float`, `double`, `boolean` (switching on them needs primitive patterns — preview in Java 25, not on the exam).

`case` constants must be:

- compile-time constants (literals, `final` variables initialised with a constant, enum constants) — a non-`final` local `int k` → "constant expression required";
- assignable to the selector type (`byte b; case 200:` → error);
- unique (duplicate label → error).

Enum constants:

- Classic form: unqualified `case RED`.
- 🆕 Java 21: the qualified name `case Color.RED` is also allowed (needed when the selector is a sealed interface implemented by an enum).

`String` cases are case-sensitive and use `equals`. A `null` selector without `case null` throws **`NullPointerException`** (for strings, enums, wrappers *and* pattern switches).

## 5. Exhaustiveness

| Switch | Must be exhaustive? |
|---|---|
| switch **expression** (any type) | **yes** — `default`, or all enum constants, or all permitted subtypes of a sealed type |
| old-style **statement** on `int`/`String`/enum, no patterns, no `case null` | no |
| statement using **patterns** or **`case null`** ("enhanced" switch) | **yes** |

```java
int r = switch (n) { case 1 -> 10; case 2 -> 20; };  // ✗ not exhaustive: int needs default
int r = switch (c) { case RED -> 1; case GREEN -> 2; case BLUE -> 3; }; // ✓ all enum constants
switch (c) { case RED: break; }                       // ✓ old statement, no check
switch (c) { case null -> {} case RED -> {} }         // ✗ enhanced statement must be exhaustive
```

## 6. 🆕 Pattern matching for `switch` (Java 21, JEP 441)

```java
static String desc(Object o) {
    return switch (o) {
        case null                 -> "null!";
        case Integer i when i > 10 -> "big int " + i;
        case Integer i            -> "int " + i;
        case String s when s.isEmpty() -> "empty";
        case String s             -> "str " + s.length();
        default                   -> "other";
    };
}
```

### Guards (`when`)

- `case Type t when <boolean expression>` — only allowed after a **pattern**; `case 1 when n > 0` does not compile.
- The guard can use the pattern variables; variables from outside must be effectively final.
- A guarded case does **not** count for exhaustiveness.

### `case null`

- `case null ->` or `case null, default ->` (the only combination of `null` with another label).
- Without `case null`, a `null` selector throws `NullPointerException` even if there is a `default`.
- `default` never matches `null` on its own.

### Dominance (order matters)

A label is an error if an earlier label matches everything it would match:

| Order | Result |
|---|---|
| `case CharSequence cs` → `case String s` | ✗ dominated |
| `case Integer i` → `case Integer i when i > 0` | ✗ dominated |
| `case Integer i when i > 0` → `case Integer i` | ✓ |
| `case Integer i` → `case 1` | ✗ constant dominated by pattern |
| `case 1` → `case Integer i` | ✓ |
| `case Integer i when i > 5` → `case 1` → `case Integer i` | ✓ guarded patterns don't dominate constants |
| `default` → `case String s` | ✗ default dominates later pattern labels |
| `default` → `case 1` (int selector) | ✓ old rule: default may precede constants |
| `case Object x` + `default` | ✗ "both an unconditional pattern and a default label" |

### Fall-through and patterns

- Colon form: falling into a pattern label is illegal (`case String s: ...` without `break` before `case Integer i:` → "illegal fall-through to a pattern"), because the binding would not be initialised.
- One case cannot declare two bindings: `case String s, Integer i ->` → error. With unnamed patterns it is allowed: `case String _, Integer _ ->` (Java 22).

### Sealed types

```java
sealed interface Shape permits Circle, Square, Rect {}
record Circle(double r) implements Shape {}
record Square(double s) implements Shape {}
record Rect(double w, double h) implements Shape {}

static double area(Shape s) {
    return switch (s) {          // no default needed: all permitted subtypes covered
        case Circle c -> Math.PI * c.r() * c.r();
        case Square q -> q.s() * q.s();
        case Rect(var w, var h) -> w * h;
    };
}
```

- Missing a permitted subtype → "does not cover all possible input values".
- Enums implementing a sealed interface can be covered constant by constant: `case Code.ADD -> ...; case Code.SUB -> ...; case Bin b -> ...`.
- If a new subtype is added later, code compiled earlier throws `MatchException` at runtime.

## 7. 🆕 Record patterns (Java 21, JEP 440)

```java
record Point(int x, int y) {}
record Line(Point a, Point b) {}

Object o = new Line(new Point(1, 2), new Point(3, 4));
if (o instanceof Line(Point(var x1, var y1), Point(int x2, int y2)) && x1 < x2) { ... }
```

- Number of nested patterns must equal the number of record components ("incorrect number of nested patterns").
- Each component pattern can be a type pattern (`int x`, `Point p`), `var x`, a nested record pattern or `_`.
- Component types must match: for an `int` component use `int` or `var` (`long x` would need a primitive pattern → not on the exam).
- A record pattern **never matches `null`**: `null instanceof Point(int x, int y)` is `false`.
- Nested: a type pattern like `Point p` in a component position matches a `null` component, but a nested **record** pattern `Point(var x, var y)` does not:

```java
Object ln = new Line(null, new Point(1, 2));
ln instanceof Line(Point p, Point q)               // true  (p == null)
ln instanceof Line(Point(var x, var y), Point q)   // false
```

- Generic records: `Box(var v)` infers the type argument; `Box<?>(String s)` also works.
- Exhaustiveness looks into components:

```java
record Pair(Shape a, Shape b) {}   // Shape sealed: Circle, Square
switch (p) {
    case Pair(Circle a, Circle b) -> ...
    case Pair(Circle a, Square b) -> ...
    case Pair(Square a, Shape b)  -> ...   // exhaustive, no default
}
```

## 8. 🆕 Unnamed patterns and variables `_` (Java 22, JEP 456)

| Use | Example | OK? |
|---|---|---|
| unnamed pattern inside a record pattern | `case Line(Point(int x, _), _)` | ✓ |
| unnamed type pattern | `case Circle _ ->` | ✓ |
| `var _` | `o instanceof Point(var _, var _)` | ✓ |
| several patterns, no bindings, one case | `case String _, Integer _ -> ...` | ✓ (may have a guard) |
| bare `_` as top-level pattern | `case _ ->`, `o instanceof _` | ✗ "underscore not allowed here" |
| combining with `default` | `case String _, default ->` | ✗ |

`_` can also name unused locals, `catch` parameters, lambda parameters and try-with-resources variables (see D3/D4). `_` can never be read.

## 9. Loops

```java
for (int i = 0, j = 10; i < j; i += 3, j -= 3) System.out.print(i + ":" + j + " ");
// 0:10 3:7
int k = 0;
do { k++; } while (k < 0);   // body runs once → k == 1
for (var v : List.of(1, 2)) ...     // var allowed in enhanced for
for (final int v : arr) ...          // final allowed
```

- `for` init can declare several variables of the **same** type only: `for (int i = 0, long j = 0; ...)` → error.
- A loop variable may not shadow a local variable in the same method (`int i = 0; for (int i = 0; ...)` → error).
- Enhanced `for` works on arrays and `Iterable` — **not** on a `Map` directly (use `entrySet()`, `keySet()`, `values()`).
- `do { } while (cond);` — the semicolon is required, body runs at least once.
- All three `for` parts are optional: `for (;;)` is an infinite loop.

### `break`, `continue` and labels

```java
outer:
for (int i = 0; i < 3; i++) {
    for (int j = 0; j < 3; j++) {
        if (j == 1) continue outer;
        if (i == 2) break outer;
        System.out.print(i + "" + j + " ");
    }
}                                   // prints "00 10 "
```

- `break label` can exit any labeled statement (even a plain block); `continue label` needs a **loop** label (`not a loop label`).
- `break` without label exits the innermost loop **or switch**; `continue` without label applies to the innermost loop.
- A `break` inside an arrow-switch inside a loop exits the switch, not the loop.

## 10. Unreachable code

| Code | Result |
|---|---|
| statement after `break;`, `continue;`, `return;`, `throw ...;` in the same block | ✗ unreachable statement |
| `while (false) { ... }` | ✗ body unreachable |
| `for (;false;) { ... }` | ✗ |
| `if (false) { ... }` | ✓ (deliberate exception to allow "conditional compilation") |
| `while (true) { }` followed by a statement (no `break`) | ✗ |
| `for (;;) { if (x) break; }` followed by a statement | ✓ |
| `do { ... } while (false);` | ✓ runs once |

---

## 🆕 Since Java 17 (summary)

| Feature | Final in | Key points |
|---|---|---|
| Pattern matching for `switch` | 🆕 Java 21 (JEP 441) | any reference selector, type patterns, `when` guards, `case null`, dominance, exhaustiveness |
| Record patterns | 🆕 Java 21 (JEP 440) | deconstruct records in `instanceof` and `switch`, nesting, `var`, never match `null` |
| Qualified enum constants in `case` | 🆕 Java 21 | `case Color.RED` |
| `instanceof` with a pattern of the same/super type | 🆕 Java 21 | `str instanceof String s` compiles |
| Unnamed variables and patterns `_` | 🆕 Java 22 (JEP 456) | `case Point(int x, _)`, `case A _, B _ ->` |
| Primitive types in patterns, `instanceof`, `switch` | preview in 25 (JEP 507) | **not on the exam** |

---

## Exam traps

- Missing `;` after a switch **expression** used in an assignment or `return`.
- `yield` missing in a block arm, or `return` used inside a switch expression.
- Mixing `->` and `:` in one switch.
- Colon-form **fall-through**: execution continues into following labels (including `default`) until `break`.
- `default` placed first still runs only when no case matches — but then falls through in the colon form.
- `switch` on `long`, `double`, `float`, `boolean` does not compile.
- Non-constant `case` labels, duplicate labels, or constants out of range for the selector type (`byte` with `case 200`).
- `null` selector → `NullPointerException` unless there is a `case null`; `default` alone does not catch `null`.
- Switch expressions (and pattern switches) need exhaustiveness; an `int` switch expression without `default` fails.
- **Dominance**: more general patterns or unguarded patterns must come **after** the specific/guarded ones; a constant after a matching type pattern fails.
- `case Object o` together with `default` → error.
- Guards only on pattern labels; guarded labels don't help exhaustiveness.
- Pattern variable scope with `||` or negation without an exit.
- Record pattern with wrong arity or with a nested record pattern against a `null` component (no match).
- Bare `_` as a whole pattern is illegal; `_` cannot be combined with `default`.
- `continue` with a label that is not on a loop; label on the wrong loop.
- `while (false)` and code after `break`/`return` are compile errors; `if (false)` is not.
- `do-while` body always runs once.
- Loop variable modified inside the body changes the iteration count; trace carefully.

## Self-check before practice

- [ ] I can say for any `switch` whether it is a statement or expression and whether it must be exhaustive.
- [ ] I can trace colon-form fall-through including a `default` in the middle.
- [ ] I know the allowed selector types and that `long`/`boolean`/floating types are rejected.
- [ ] I know where `yield` is required and that `return` cannot leave a switch expression.
- [ ] I can order pattern labels correctly (specific before general, guarded before unguarded, constants before type patterns).
- [ ] I know `case null`, `case null, default`, and that `default` alone does not match `null`.
- [ ] I can write a switch over a sealed hierarchy with no `default` and spot a missing subtype.
- [ ] I can deconstruct nested records with `var`, explicit types and `_`, and know when a record pattern fails on `null`.
- [ ] I know the flow-scoping rules for pattern variables with `&&`, `||`, `!` and early `return`.
- [ ] I can trace labeled `break`/`continue` across nested loops.
- [ ] I can spot unreachable statements and the `if (false)` exception.

---

**Practice:** `ocp17-829` chapter 2 (*Controlling Program Flow*) + the `java25-gap` files for `d02-flow` (pattern matching for switch, record patterns, unnamed patterns are not in the Java 17 book).
