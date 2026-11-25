---
title: "D4 — Exception Handling"
topic: d04-exceptions
order: 4
---

# D4 — Exception Handling

## What the exam tests

- The `Throwable` hierarchy: checked exceptions, unchecked exceptions (`RuntimeException`) and `Error`
- The "handle or declare" rule, `throw` vs `throws`, overriding methods and `throws` clauses
- `try`/`catch`/`finally` syntax, catch ordering, `finally` execution order and `return` in `finally`
- Multi-catch (`|`) rules
- try-with-resources: `AutoCloseable`, closing order, suppressed exceptions, effectively final resources
- Writing custom exceptions (constructors, cause chaining)
- Recognising common JDK exceptions and who throws them
- 🆕 Unnamed variables `_` in `catch` and try-with-resources (Java 22)

---

## 1. The hierarchy

```
Throwable
├── Error                      (unchecked – serious JVM problems, don't catch normally)
│   ├── StackOverflowError, OutOfMemoryError
│   └── ExceptionInInitializerError, NoClassDefFoundError, AssertionError
└── Exception                  (checked, unless it is a RuntimeException)
    ├── IOException → FileNotFoundException, NotSerializableException, …
    ├── SQLException, ParseException, InterruptedException, CloneNotSupportedException, …
    └── RuntimeException       (unchecked)
        ├── ArithmeticException, ClassCastException, NullPointerException
        ├── IllegalArgumentException → NumberFormatException
        ├── IllegalStateException, UnsupportedOperationException
        ├── IndexOutOfBoundsException → ArrayIndexOutOfBoundsException, StringIndexOutOfBoundsException
        ├── ArrayStoreException, NegativeArraySizeException
        ├── NoSuchElementException (java.util), ConcurrentModificationException
        ├── DateTimeException → UnsupportedTemporalTypeException (java.time)
        └── MatchException (🆕 Java 21, thrown by an exhaustive switch that unexpectedly matches nothing)
```

| Kind | Must be handled or declared? | Examples |
|---|---|---|
| **checked** (`Exception` but not `RuntimeException`) | **yes** | `IOException`, `FileNotFoundException`, `SQLException`, `Exception` itself |
| **unchecked** (`RuntimeException` and subclasses) | no | `NullPointerException`, `NumberFormatException` |
| **`Error`** and subclasses | no | `StackOverflowError`, `ExceptionInInitializerError` |

Which exception does the JDK throw?

| Code | Exception |
|---|---|
| `Object x = "s"; Integer i = (Integer) x;` | `ClassCastException` |
| `Object[] arr = new String[1]; arr[0] = 1;` | `ArrayStoreException` |
| `new int[-1]` | `NegativeArraySizeException` |
| `List.of(1).add(2)` | `UnsupportedOperationException` |
| `new ArrayList<>().iterator().next()` | `NoSuchElementException` |
| `Integer.parseInt("x")` | `NumberFormatException` (an `IllegalArgumentException`) |
| `1 / 0` | `ArithmeticException` |
| `throw null;` | `NullPointerException` |
| `static int v = 1 / 0;` when the class initialises | `ExceptionInInitializerError` (cause: `ArithmeticException`) |
| endless recursion | `StackOverflowError` |

## 2. `throw`, `throws` and handle-or-declare

- `throw` is a statement that throws **one** object: `throw new IOException("msg");`.
- `throws` is part of a method/constructor declaration: `void read() throws IOException, SQLException`.
- A checked exception that can be thrown must be **caught** or **declared** — otherwise "unreported exception … must be caught or declared to be thrown".
- A statement directly after `throw` in the same block is **unreachable** → compile error.
- A method may declare exceptions it never throws (`void m() throws IOException {}` is fine).
- `main` may declare `throws Exception`; an uncaught exception terminates the thread and prints the stack trace (`Exception in thread "main" ...`).

### Catching a checked exception that cannot be thrown

```java
try { System.out.println(); } catch (IOException e) {}   // ✗ IOException is never thrown in body
try { System.out.println(); } catch (Exception e) {}     // ✓ Exception also covers runtime exceptions
try { } catch (Throwable t) {}                           // ✓
try { } catch (MyCheckedException e) {}                  // ✗ never thrown
```

### Overriding and `throws`

An overriding method may **not** declare new or broader **checked** exceptions. It may declare fewer, narrower or any unchecked ones.

```java
class P { void go() throws IOException {} }
class C1 extends P { void go() throws FileNotFoundException, RuntimeException {} }  // ✓
class C2 extends P { void go() throws Exception {} }                                // ✗ broader
class Q { void go() {} }
class C3 extends Q { void go() throws MyCheckedException {} }                       // ✗ new checked
```

The same applies to implementing interface methods: a class implementing `Closeable` cannot declare `close() throws Exception` (`Closeable.close()` throws only `IOException`).

**Constructors**: if a superclass constructor declares a checked exception, the subclass constructor must declare it (it cannot catch it, because `super(...)` is implicit or must not be wrapped in `try`).

```java
class W { W() throws IOException {} }
class W2 extends W { W2() {} }   // ✗ unreported exception IOException
```

## 3. `try` / `catch` / `finally`

Syntax rules:

- `try` needs at least one `catch` or a `finally` — except try-with-resources, which may stand alone.
- Order is fixed: `try` → `catch`es → `finally`. Braces are mandatory even for one statement.
- At most one `finally`.

### Catch ordering

A `catch` for a subclass placed **after** its superclass is unreachable → compile error.

```java
try { new FileReader("x"); }
catch (IOException e) {}
catch (FileNotFoundException e) {}  // ✗ already caught

try { new FileReader("nope.txt"); }
catch (FileNotFoundException e) { System.out.println("FNFE"); }  // ✓ specific first
catch (IOException e) {}
```

Only one `catch` block runs. An exception thrown **inside** a `catch` block is **not** handled by sibling `catch` blocks of the same `try` — it propagates (after `finally` runs).

```java
try {
    try { throw new IllegalArgumentException(); }
    catch (IllegalArgumentException e) { throw new IllegalStateException(); }
    catch (IllegalStateException e) { System.out.println("sibling"); }   // not reached
} catch (IllegalStateException e) { System.out.println("outer"); }       // prints "outer"
```

### `finally`

- Runs whether or not an exception was thrown or caught, and also after `return`, `break` or `continue` in `try`/`catch`.
- Does **not** run if `System.exit()` is called (or the JVM crashes).

```java
try {
    try { System.out.print("A"); throw new RuntimeException(); }
    catch (RuntimeException e) { System.out.print("B"); throw new IllegalStateException(); }
    finally { System.out.print("C"); }
} catch (IllegalStateException e) { System.out.print("D"); }
finally { System.out.println("E"); }                  // ABCDE
```

### `return` and `finally`

```java
static int f() {
    int x = 1;
    try { return x; }               // value 1 is saved now
    finally { x = 2; }              // changing the variable does not change the saved value
}                                   // f() == 1

static StringBuilder g() {
    var sb = new StringBuilder("a");
    try { return sb; }
    finally { sb.append("b"); }     // the saved value is a reference → object is mutated
}                                   // g() prints "ab"

static int h() {
    try { throw new RuntimeException("lost"); }
    finally { return 42; }          // return in finally discards the exception!
}                                   // h() == 42, no exception
```

- If `finally` throws, that exception **replaces** the one from `try`/`catch`; the original is lost (not added as suppressed).

## 4. Multi-catch

```java
catch (FileNotFoundException | SQLException e) { ... }
```

| Rule | Example | Result |
|---|---|---|
| Alternatives must **not** be related by subclassing | `FileNotFoundException \| IOException` | ✗ |
| Only one variable name, at the end | `catch (IOException e1 \| SQLException e2)` | ✗ |
| Parameter is implicitly **final** | `e = new ...;` inside multi-catch | ✗ |
| Single-type catch parameter may be reassigned | `catch (NumberFormatException e) { e = ...; }` | ✓ (bad style) |
| Static type of `e` | the closest common supertype | `e.getClass()` still gives the runtime type |
| `var` | `catch (var e)` | ✗ |

### Precise rethrow

If a catch parameter is effectively final, `throw e;` rethrows only the checked exceptions the `try` body can actually throw:

```java
static void p() throws IOException {
    try { throw new IOException(); }
    catch (Exception e) { throw e; }        // ✓ compiler knows it can only be IOException
}
// adding  e = new Exception();  before throw e  → ✗ unreported exception Exception
```

## 5. try-with-resources

```java
try (var in = Files.newBufferedReader(path);
     var out = Files.newBufferedWriter(target)) {
    ...
}   // out.close(), then in.close() — automatically, even on exception
```

Rules:

- Resources must implement `AutoCloseable` (`close() throws Exception`) — `Closeable` (`close() throws IOException`) extends it. `try (String s = "x")` → ✗.
- Resources are **closed in reverse order** of declaration, **before** any `catch` or `finally` block runs.
- Resource variables are implicitly **final** — `a = null;` inside the block → ✗.
- Resource variables are scoped to the `try` block — not visible in `catch` or `finally`.
- An existing variable may be used if it is **final or effectively final** (Java 9): `try (r) { ... }` / `try (r1; r2)`.
- `var` is allowed: `try (var r = new MyRes())`.
- A `null` resource is allowed — `close()` is simply not called.
- If the `close()` of the resource type declares a checked exception, it must be caught or declared — the `try` body need not throw anything for this to apply.

```java
class R implements AutoCloseable {
    public void close() throws Exception { ... }
}
static void q() { try (R a = new R()) { } }                         // ✗ unreported Exception from close()
static void q() { try (R a = new R()) { } catch (Exception e) { } } // ✓
```

An implementation may narrow `close()` (e.g. no `throws`) — then no handler is needed.

### Execution order

```java
try (R a = new R("a"); R b = new R("b")) {
    System.out.println("body");
    throw new IllegalStateException("x");
} catch (Exception e) {
    System.out.println("catch " + e.getMessage());
} finally {
    System.out.println("finally");
}
// open a, open b, body, close b, close a, catch x, finally
```

If initialising the 2nd resource throws, the already-opened 1st resource is closed, then the `catch` runs; the body never runs.

### Suppressed exceptions

- If the body throws and `close()` throws too, the body's exception is the **primary** one; the `close()` exception is attached via `addSuppressed` and readable with `e.getSuppressed()`.
- If only `close()` throws, that exception is the primary one (no suppressed).
- If several `close()` calls throw, all but the primary are suppressed.

```java
class Bad implements AutoCloseable {
    public void close() { throw new IllegalStateException("close"); }
}
try (Bad b = new Bad()) {
    throw new RuntimeException("body");
} catch (RuntimeException e) {
    System.out.println(e.getMessage());                    // body
    System.out.println(e.getSuppressed()[0].getMessage()); // close
}
```

Contrast: a `finally` that throws **replaces** the exception; try-with-resources **suppresses** close failures.

## 6. Custom exceptions

```java
class MyException extends Exception {                       // checked
    public MyException() { super(); }
    public MyException(String message) { super(message); }
    public MyException(String message, Throwable cause) { super(message, cause); }
    public MyException(Throwable cause) { super(cause); }
}
class MyRuntime extends RuntimeException { }               // unchecked
```

- Extend `Exception` for checked, `RuntimeException` for unchecked.
- Constructors are not inherited: if the superclass of your exception has **no** no-arg constructor, a subclass without an explicit constructor does not compile.
- Wrap and chain: `throw new MyException("wrapped", ioe);` — `getCause()` returns the original.
- `toString()` = fully qualified class name + `": " + message` (if message non-null). `new RuntimeException()` prints `java.lang.RuntimeException`; its `getMessage()` is `null`.
- `printStackTrace()` writes to `System.err`.

---

## 🆕 Since Java 17

| Feature | Final in | Example |
|---|---|---|
| Unnamed variable as `catch` parameter | 🆕 Java 22 (JEP 456) | `catch (NumberFormatException _) { ... }` |
| Unnamed variable in multi-catch | 🆕 Java 22 | `catch (NumberFormatException \| ArithmeticException _)` |
| Unnamed try-with-resources variable | 🆕 Java 22 | `try (var _ = lock.acquire()) { ... }` — still closed automatically |
| `MatchException` | 🆕 Java 21 | thrown when an exhaustive pattern `switch` finds no match at runtime (e.g. a sealed subtype added after compilation) |

`_` can never be read: `catch (Exception _) { System.out.println(_); }` does not compile.

---

## Exam traps

- Catching a **checked** exception the `try` body cannot throw → compile error; catching `Exception`/`Throwable`/a `RuntimeException` is always allowed.
- Superclass `catch` before subclass `catch` → compile error ("has already been caught").
- Multi-catch with related types (`FileNotFoundException | IOException`) → compile error.
- Assigning to a multi-catch parameter or a try-with-resources variable → compile error.
- `try` alone (without `catch`/`finally`/resources) → compile error.
- try-with-resources: resources close **before** `catch`/`finally`, in **reverse** order.
- The resource's `close()` can throw a checked exception → must be handled even if the body is empty.
- Resource variable used in `catch`/`finally` → out of scope.
- External resource used in `try (r)` must be effectively final.
- `return` inside `finally` swallows exceptions and overrides earlier returns.
- A primitive returned from `try` is not affected by changes in `finally`; a mutable object is.
- `System.exit()` → `finally` does not run.
- Exception thrown from a `catch` block is not caught by the next `catch` of the same `try`.
- Overriding methods cannot add or broaden checked exceptions; unchecked ones are always allowed.
- Code after `throw` in the same block is unreachable.
- `throw null;` compiles and throws `NullPointerException`.
- `NumberFormatException` is an `IllegalArgumentException`; `FileNotFoundException` is an `IOException`; `ArrayIndexOutOfBoundsException` and `StringIndexOutOfBoundsException` are `IndexOutOfBoundsException`s.
- `Error`s can be caught with `catch (Error e)` or `catch (Throwable t)` — but not with `catch (Exception e)`.
- Static initialiser failure surfaces as `ExceptionInInitializerError`, not the original exception.

## Self-check before practice

- [ ] I can classify any common exception as checked, unchecked or `Error`, and name its superclass.
- [ ] I can tell whether a `catch` clause is legal (unreachable subclass, never-thrown checked exception).
- [ ] I know the multi-catch rules (unrelated types, one variable, implicitly final).
- [ ] I can trace output through nested `try`/`catch`/`finally` blocks, including exceptions thrown inside `catch` and `finally`.
- [ ] I know what `return` in `try` vs `finally` returns, for primitives and for mutable objects.
- [ ] I can write the exact order of open/body/close/catch/finally for a try-with-resources with several resources.
- [ ] I know when an exception is suppressed vs replaced, and how to read suppressed exceptions.
- [ ] I know the try-with-resources rules: `AutoCloseable`, implicit final, scope, effectively final external resources, `var`, `null`.
- [ ] I can check whether an overriding method's or constructor's `throws` clause is legal.
- [ ] I can write a custom checked exception with message and cause constructors.
- [ ] I know where `_` may replace an exception or resource variable name (Java 22).

---

**Practice:** `ocp17-829` chapter 4 (*Handling Exceptions*) + the `java25-gap` files for `d04-exceptions`.
