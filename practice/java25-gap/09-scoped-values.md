---
title: "Gap: Scoped values"
source: java25-gap
topic: d08-concurrency
---

# Gap: Scoped values (🆕 Java 25, JEP 506)

## gap-scoped-001

What is the result of running the following program?

```java
public class Session {
    static final ScopedValue<String> USER = ScopedValue.newInstance();

    public static void main(String[] args) {
        System.out.print(USER.isBound() + " " + USER.orElse("guest") + " ");
        System.out.print(USER.get());
    }
}
```

- **A.** It prints `false guest null`.
- **B.** It prints `false null null`.
- **C.** It prints `false guest ` and then throws a `NoSuchElementException`.
- **D.** It prints `false guest ` and then throws an `IllegalStateException`.
- **E.** The code does not compile because `ScopedValue` must be imported.

**Answer:** C

**Explanation:** 🆕 Java 25 (JEP 506): `ScopedValue` is final and lives in **`java.lang`**, so no import is needed. A scoped value is created with the static factory `ScopedValue.newInstance()` and is usually stored in a `static final` field.

Outside of any binding the value is **unbound**: `isBound()` returns `false`, `orElse("guest")` returns the fallback, and `get()` throws `java.util.NoSuchElementException` — it never returns `null` for an unbound value.

## gap-scoped-002

What is the output of the following program?

```java
public class Nested {
    static final ScopedValue<String> USER = ScopedValue.newInstance();

    public static void main(String[] args) {
        ScopedValue.where(USER, "alice").run(() -> {
            System.out.print(USER.get());
            ScopedValue.where(USER, "bob").run(() -> System.out.print(" " + USER.get()));
            System.out.print(" " + USER.get());
        });
        System.out.println(" " + USER.isBound());
    }
}
```

- **A.** `alice bob alice false`
- **B.** `alice bob bob false`
- **C.** `alice bob alice true`
- **D.** `alice alice alice false`
- **E.** An `IllegalStateException` is thrown because `USER` is already bound.

**Answer:** A

**Explanation:** `ScopedValue.where(key, value)` returns a `ScopedValue.Carrier`; `run(Runnable)` executes the code with the binding in effect. Bindings can be **nested**: the inner `where(USER, "bob")` **rebinds** (shadows) `USER` only for the duration of the inner `run`. When the inner `run` returns, the outer binding `alice` is visible again.

When the outer `run` returns, the binding is removed automatically, so `isBound()` is `false` afterwards. Output: `alice bob alice false`.

## gap-scoped-003

What is the output of the following program?

```java
import java.io.IOException;

public class Loader {
    static final ScopedValue<Integer> N = ScopedValue.newInstance();

    static int read() throws IOException {
        if (N.get() < 0) throw new IOException("neg");
        return N.get() * 2;
    }

    public static void main(String[] args) {
        try {
            int r = ScopedValue.where(N, 21).call(Loader::read);
            System.out.println(r);
            ScopedValue.where(N, -1).call(Loader::read);
        } catch (IOException e) {
            System.out.println("IOE " + e.getMessage());
        }
    }
}
```

- **A.** `42` only
- **B.** `42` followed by `IOE neg`
- **C.** The code does not compile because `call()` cannot propagate a checked `IOException`.
- **D.** The code does not compile because `call()` returns `void`.
- **E.** `21` followed by `IOE neg`

**Answer:** B

**Explanation:** `Carrier.call(op)` runs an operation that **returns a value**. In Java 25 its parameter type is `ScopedValue.CallableOp<T, X extends Throwable>`, whose `call()` method declares `throws X`. The compiler infers `X` as `IOException` from the method reference, so `call()` itself throws `IOException` — which is caught by the surrounding `catch (IOException e)`. (In earlier previews `call` took a `Callable` and threw `Exception`; on the exam, use the final Java 25 API.)

The first call returns 21 × 2 = 42. The second call binds -1, so `read()` throws `IOException("neg")`, which propagates out of `call()`. Output: `42` then `IOE neg`.

## gap-scoped-004

What is the output of the following program?

```java
public class Chain {
    static final ScopedValue<String> NAME = ScopedValue.newInstance();
    static final ScopedValue<Integer> LEVEL = ScopedValue.newInstance();

    public static void main(String[] args) {
        ScopedValue.where(NAME, "x")
                   .where(LEVEL, 1)
                   .where(NAME, "y")
                   .run(() -> System.out.println(NAME.get() + LEVEL.get()));
    }
}
```

- **A.** `x1`
- **B.** `xy1`
- **C.** `y1`
- **D.** An `IllegalArgumentException` is thrown because `NAME` is bound twice.
- **E.** The code does not compile because `where()` is static.

**Answer:** C

**Explanation:** `ScopedValue.where()` is static, but it returns a `Carrier`, and `Carrier` has an **instance** method `where(key, value)` that adds another binding. This is how several scoped values are bound for one `run`/`call`. If the same key appears more than once in the chain, the **last** binding wins, so `NAME` is `"y"` and the output is `y1`.

## gap-scoped-005

Given `static final ScopedValue<String> USER = ScopedValue.newInstance();` and `Runnable r = () -> {};`, which statements compile? (Choose two.)

- **A.** `USER.set("x");`
- **B.** `ScopedValue.runWhere(USER, "x", r);`
- **C.** `ScopedValue<String> s = new ScopedValue<>();`
- **D.** `ScopedValue.where(USER, "x").run(r);`
- **E.** `ScopedValue<Integer> id = ScopedValue.newInstance();`
- **F.** `USER.where("x").run(r);`

**Answer:** D, E

**Explanation:** A scoped value is **immutable** once bound: there is no `set()` method (A). The only way to give it a value is to bind it for a bounded scope with `ScopedValue.where(key, value)` followed by `run(...)` or `call(...)` (D).

B: static convenience methods such as `runWhere`/`callWhere` existed in early previews but were **removed**; they are not part of the final Java 25 API. C: the constructor is private; use the factory method `newInstance()` (E). F: there is no instance method `where` on `ScopedValue` itself — `where` is static on `ScopedValue` and an instance method only on `ScopedValue.Carrier`.

## gap-scoped-006

What is the output of the following program?

```java
public class Inherit {
    static final ScopedValue<String> CTX = ScopedValue.newInstance();

    public static void main(String[] args) {
        ScopedValue.where(CTX, "main").run(() -> {
            Thread t = Thread.ofVirtual()
                             .start(() -> System.out.println("child: " + CTX.isBound()));
            try { t.join(); } catch (InterruptedException e) { }
            System.out.println("parent: " + CTX.get());
        });
    }
}
```

- **A.** `child: true` followed by `parent: main`
- **B.** `child: false` followed by `parent: main`
- **C.** `child: false` followed by a `NoSuchElementException`
- **D.** `child: true` followed by a `NoSuchElementException`
- **E.** The code does not compile because a scoped value cannot be read inside a lambda.

**Answer:** B

**Explanation:** Scoped-value bindings belong to the thread that executes `run`/`call`. A thread started with `Thread.ofVirtual().start(...)` (or any other ordinary `Thread`/`ExecutorService`) does **not** inherit them, so `CTX.isBound()` is `false` in the child. (Unlike `InheritableThreadLocal`, inheritance into child threads happens only with structured concurrency's `StructuredTaskScope`, which is a **preview** API in Java 25 — not on the exam.)

The parent thread is still inside its `run`, so `CTX.get()` returns `main`.

## gap-scoped-007

What is the output of the following program?

```java
public class Views {
    static final ScopedValue<String> S = ScopedValue.newInstance();

    public static void main(String[] args) {
        Runnable r = () -> System.out.print(S.orElse("-"));
        r.run();
        ScopedValue.where(S, "A").run(r);
        r.run();
        ScopedValue.where(S, "B").run(() -> {
            r.run();
            ScopedValue.where(S, "C").run(r);
            r.run();
        });
        System.out.println();
    }
}
```

- **A.** `-A-BCC`
- **B.** `-AABCB`
- **C.** `-A-BCB`
- **D.** `AABCCB`
- **E.** `-A-B-B`

**Answer:** C

**Explanation:** A scoped value is looked up **dynamically** at the moment `get()`/`orElse()` executes, based on the bindings active in the current thread — it does not matter where the lambda was defined.

1. outside any binding: `-`
2. inside `where(S, "A")`: `A`
3. after that `run` returned: `-`
4. inside `where(S, "B")`: `B`
5. nested `where(S, "C")`: `C`
6. back in the `B` scope: `B`

Output: `-A-BCB`.

## gap-scoped-008

What is the result of running the following program?

```java
public class Nulls {
    static final ScopedValue<String> NAME = ScopedValue.newInstance();

    public static void main(String[] args) {
        ScopedValue.where(NAME, null)
                   .run(() -> System.out.print(NAME.isBound() + " " + NAME.get() + " "));
        System.out.print(NAME.orElse(null));
    }
}
```

- **A.** It prints `true null null`.
- **B.** It prints `false null null`.
- **C.** It throws a `NullPointerException` immediately, because a scoped value cannot be bound to `null`.
- **D.** It prints `true null ` and then throws a `NullPointerException`.
- **E.** It prints `false null ` and then throws a `NoSuchElementException`.

**Answer:** D

**Explanation:** A scoped value **may be bound to `null`**. Inside the `run`, `isBound()` is `true` and `get()` returns `null`, so `true null ` is printed.

In the final Java 25 API, `orElse(T other)` does **not** accept `null` as the fallback: it throws `NullPointerException` (the argument is checked even before testing whether the value is bound). Use `isBound()` if you need to distinguish the unbound case.

## gap-scoped-009

What is the output of the following program?

```java
public class Counter {
    static final ScopedValue<Integer> N = ScopedValue.newInstance();

    public static void main(String[] args) {
        ScopedValue.where(N, 1).run(() -> {
            int x = N.get();
            x++;
            System.out.print(x + " " + N.get() + " ");
        });
        ScopedValue.where(N, 7).run(() -> System.out.print(N.orElse(0) + N.get() + " "));
        System.out.println(N.orElse(0));
    }
}
```

- **A.** `2 2 14 0`
- **B.** `2 1 77 0`
- **C.** `2 1 14 7`
- **D.** `2 2 77 7`
- **E.** `2 1 14 0`

**Answer:** E

**Explanation:** `N.get()` returns the bound `Integer`; incrementing the local copy `x` does not change the binding, so the first lambda prints `2 1 `.

In the second lambda `N.orElse(0)` returns 7 because `N` is bound; `7 + 7` is evaluated as **integer addition** (both operands are `Integer`, unboxed), giving `14` before the string concatenation with `" "`.

After the second `run` returns, `N` is unbound again, so `orElse(0)` returns `0`. Output: `2 1 14 0`.

## gap-scoped-010

Which statements about scoped values are true? (Choose two.)

- **A.** `ScopedValue` is a final (non-preview) API in Java 25.
- **B.** Code running inside `run()` can change the bound value with `set()`.
- **C.** A binding created with `where(...).run(...)` is removed automatically when `run()` returns, even if it throws an exception.
- **D.** `get()` returns `null` when the scoped value is not bound.
- **E.** `ScopedValue` is in the `java.util.concurrent` package and must be imported.
- **F.** A thread started with `Thread.ofVirtual().start()` inside `run()` inherits the caller's bindings.

**Answer:** A, C

**Explanation:** A is true: scoped values were previewed in Java 21–24 and finalized in Java 25 (JEP 506). C is true: the binding has a strictly bounded lifetime — it exists only while `run`/`call` executes and is always cleaned up when it exits, normally or abruptly. This is the main advantage over `ThreadLocal`, which must be removed manually.

B is false: scoped values are immutable (rebinding is done with a nested `where`). D is false: `get()` throws `NoSuchElementException` when unbound. E is false: `ScopedValue` is in `java.lang`. F is false: only `StructuredTaskScope` (preview) forks inherit bindings.

## gap-scoped-011

What is the output of the following program?

```java
public class Carriers {
    static final ScopedValue<String> NAME = ScopedValue.newInstance();
    static final ScopedValue<Integer> LEVEL = ScopedValue.newInstance();

    public static void main(String[] args) {
        ScopedValue.Carrier c = ScopedValue.where(NAME, "n").where(LEVEL, 2);
        System.out.println(c.get(LEVEL) + " " + LEVEL.isBound());
    }
}
```

- **A.** `2 true`
- **B.** `2 false`
- **C.** `null false`
- **D.** A `NoSuchElementException` is thrown.
- **E.** The code does not compile.

**Answer:** B

**Explanation:** Creating a `Carrier` with `where(...)` does **not** bind anything yet; it only collects key/value pairs. Bindings take effect only while `run()` or `call()` executes. `Carrier.get(key)` reads the value **stored in the carrier**, so `c.get(LEVEL)` returns 2, while `LEVEL.isBound()` is still `false` in the current thread. (`c.get(key)` for a key that is not in the carrier throws `NoSuchElementException`.)
