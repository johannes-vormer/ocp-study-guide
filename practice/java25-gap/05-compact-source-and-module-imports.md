---
title: "Gap: Compact source files, instance main, module imports, source launcher"
source: java25-gap
topic: d07-modules
---

# Gap: Compact source files & instance main (🆕 Java 25, JEP 512), module imports (🆕 Java 25, JEP 511), multi-file source launcher (🆕 Java 22, JEP 458)

## gap-compact-001

The file `Hello.java` contains exactly the following:

```java
void main() {
    println("hi");
}
```

What is the result of running `java Hello.java`?

- **A.** `hi`
- **B.** Nothing is printed, because the instance method `main` is never called.
- **C.** Compilation fails.
- **D.** A runtime error occurs because `Hello` has no `static` main method.
- **E.** A runtime error occurs because the file contains no class declaration.

**Answer:** C

**Explanation:** This is a valid *compact source file* (no class declaration, an instance `void main()`), and such a `main` is a legal entry point in Java 25. But `println` is not a method of the implicitly declared class `Hello`: javac reports "cannot find symbol: method println(String)".

During the preview phase, compact source files implicitly imported the static methods of `java.io.IO`. In the final Java 25 version the class moved to `java.lang.IO` and its methods are **not** imported statically — you must write `IO.println("hi")` (or `System.out.println`).

## gap-compact-002

The file `Hello.java` contains exactly the following:

```java
List<String> names = new ArrayList<>();

void main() {
    names.add("b");
    names.add("a");
    Collections.sort(names);
    IO.println(names);
}
```

What is the result of running `java Hello.java`?

- **A.** Compilation fails because `List`, `ArrayList` and `Collections` are not imported.
- **B.** Compilation fails because a field cannot be declared outside a class.
- **C.** `[b, a]`
- **D.** A runtime error occurs because `names` is not initialized.
- **E.** `[a, b]`

**Answer:** E

**Explanation:** A compact source file implicitly declares a final top-level class (named `Hello` here) whose members are the declared fields and methods; `names` is an ordinary **instance** field, initialized when the launcher instantiates the class to call the instance `main`.

Every compact source file behaves as if it started with `import module java.base;`, so all public types of all packages exported by `java.base` — including `java.util.List`, `ArrayList` and `Collections` — are available without imports. The list is sorted → `[a, b]`.

## gap-compact-003

The file `Counter.java` contains exactly the following:

```java
int counter = 5;

static void main() {
    IO.println(counter);
}
```

What is the result of running `java Counter.java`?

- **A.** `5`
- **B.** `0`
- **C.** Compilation fails.
- **D.** A runtime error occurs because no instance of `Counter` can be created.
- **E.** A runtime error occurs because `main` must declare a `String[]` parameter.

**Answer:** C

**Explanation:** Fields in a compact source file are instance fields of the implicit class unless they are declared `static`. A `static` method cannot use an instance field without a reference: "non-static variable counter cannot be referenced from a static context". Either make `main` an instance method (`void main()`) or declare `static int counter = 5;`.

`static void main()` with no parameters is itself a perfectly legal entry point in Java 25 (E is wrong).

## gap-compact-004

Given the file `Main.java`:

```java
public class Main {
    static void main() { System.out.println("static no-arg"); }
    void main(String[] args) { System.out.println("instance args " + args.length); }
}
```

What is the result of running `java Main.java a b`?

- **A.** `static no-arg`
- **B.** `instance args 2`
- **C.** `static no-arg` followed by `instance args 2`
- **D.** Compilation fails because a class cannot declare two `main` methods.
- **E.** The launcher reports an error because the choice of `main` is ambiguous.

**Answer:** B

**Explanation:** Both methods are *candidate* main methods: they are named `main`, return `void`, are not `private`, and have either a single `String[]` parameter or no parameters. Overloading `main` was always legal, and having several candidates is not an error.

The launch protocol (JLS 12.1.4) prefers the candidate **with the `String[]` parameter**, regardless of whether it is `static` or not. Since it is an instance method, the launcher first creates `new Main()` and then calls `main(args)` with the two arguments.

## gap-compact-005

Given the file `Main.java`:

```java
public class Main {
    private static void main(String[] args) { System.out.println("private"); }
    void main() { System.out.println("instance"); }
}
```

What is the result of running `java Main.java`?

- **A.** `instance`
- **B.** `private`
- **C.** Compilation fails because `main` must be `public`.
- **D.** The launcher reports that no main method was found.
- **E.** `private` followed by `instance`

**Answer:** A

**Explanation:** Since Java 25, a main method may have `public`, `protected` or package access, but a **`private`** method is never a candidate main method. The only candidate is the instance `void main()`, so the launcher creates a `Main` object and calls it → `instance`.

## gap-compact-006

Given the file `Main.java`:

```java
public class Main {
    private final String name;
    Main(String name) { this.name = name; }
    void main() { System.out.println("hello " + name); }
}
```

What happens when you run `javac Main.java` followed by `java Main`?

- **A.** `hello null` is printed.
- **B.** `hello ` is printed.
- **C.** `javac` fails because an instance `main` method requires a no-argument constructor.
- **D.** `javac` succeeds, but `java Main` fails with an error because `Main` has no non-private zero-argument constructor.
- **E.** `javac` fails because `name` might not have been initialized.

**Answer:** D

**Explanation:** The class is a valid ordinary class, so it compiles. At launch, the only candidate is an *instance* main method, so the launcher must evaluate `new Main()` — which requires a non-private constructor without parameters. `Main` only has `Main(String)`, so the launch fails at run time with an error like "no non-private zero argument constructor found in class Main". The JLS states that execution fails in this case; it is not a compile-time error.

## gap-compact-007

Given the file `Main.java`:

```java
class Base {
    void main() { System.out.println("from " + getClass().getSimpleName()); }
}
public class Main extends Base { }
```

What happens when you run `javac Main.java` followed by `java Main`?

- **A.** `from Base`
- **B.** `from Main`
- **C.** The launcher reports that `Main` has no main method.
- **D.** `javac` fails because `Main` declares no members.
- **E.** The launcher runs `Base.main` without creating any object.

**Answer:** B

**Explanation:** A candidate main method may be declared in, **or inherited by**, the initial class. `Main` inherits the instance method `void main()` from `Base`. The launcher creates a `Main` instance (with the default constructor) and invokes `main()` on it; `getClass()` is therefore `Main`.

## gap-compact-008

Which statements about compact source files in Java 25 are true? (Choose three.)

- **A.** The implicitly declared class is `final` and belongs to the unnamed package.
- **B.** A compact source file may start with a `package` declaration.
- **C.** A compact source file may declare constructors and static initializers.
- **D.** All public top-level types of the packages exported by `java.base` can be used without import statements.
- **E.** A class declared in a compact source file `Hello.java`, such as `class Helper {}`, is compiled to `Hello$Helper.class`.
- **F.** The static methods of `IO` can be called without the `IO.` prefix.

**Answer:** A, D, E

**Explanation:** A: the implicit class is final, extends `Object`, and is a member of the unnamed package. D: compact source files behave as if they contain `import module java.base;`. E: everything declared in the file is a *member* of the implicit class, so `Helper` is a nested member class `Hello$Helper`.

B: "compact source file should not have package declaration". C: a compact source file cannot contain constructors or initializers (`Hello() {}` is parsed as a method missing its return type). F: `IO` must be used with its class name.

## gap-compact-009

Given:

```java
import module java.base;
import module java.desktop;

public class C09 {
    public static void main(String[] args) {
        List<String> l = new ArrayList<>();
        l.add("x");
        System.out.println(l);
    }
}
```

The class does not compile. Which changes, each applied on its own, make it compile and print `[x]`? (Choose two.)

- **A.** Swap the two `import module` lines.
- **B.** Add `import java.util.List;`
- **C.** Remove `import module java.base;`
- **D.** Add `import java.util.*;`
- **E.** Add `import java.util.ArrayList;`
- **F.** Add `import static java.util.List.of;`

**Answer:** B, D

**Explanation:** `java.base` exports `java.util` (with interface `List`) and `java.desktop` exports `java.awt` (with class `List`), so the simple name `List` is ambiguous: "reference to List is ambiguous". The order of module imports is irrelevant (A).

A single-type import (B) **and** a type-import-on-demand (D) both shadow types imported by module import declarations, so either resolves `List` to `java.util.List`. C leaves only `java.awt.List`, which is not generic, and `ArrayList` would no longer be found. E and F do not affect the name `List` as a type.

## gap-compact-010

Given:

```java
import module java.sql;

public class Main {
    public static void main(String[] args) {
        Logger log = Logger.getLogger("x");
        Connection c = null;
        System.out.println(log.getName());
    }
}
```

What is the result of running `java Main.java`?

- **A.** `x`
- **B.** Compilation fails because `Logger` is not imported.
- **C.** Compilation fails because `Logger` is ambiguous.
- **D.** Compilation fails because `import module` can only be used inside a named module.
- **E.** A `NullPointerException` is thrown.

**Answer:** A

**Explanation:** `import module M;` imports all public top-level types from the packages exported by `M` **and** from the packages exported by the modules that `M` requires transitively. The module `java.sql` declares `requires transitive java.logging` (and `java.xml`), so `java.util.logging.Logger` is imported along with `java.sql.Connection`.

Module import declarations can be used in any source file, including classes on the class path (D is wrong). `c` is never dereferenced, so there is no NPE.

## gap-compact-011

A directory contains three files:

```java
// Main.java
public class Main {
    public static void main(String[] args) { System.out.println(Helper.greet()); }
}
```

```java
// Helper.java
class Helper { static String greet() { return "hi from Helper"; } }
```

```java
// Broken.java
class Broken { int x = "oops"; }
```

What is the result of running `java Main.java` in that directory?

- **A.** Compilation fails because `Helper` cannot be found; only `Main.java` is compiled.
- **B.** Compilation fails because of the error in `Broken.java`.
- **C.** A `NoClassDefFoundError` is thrown for `Helper`.
- **D.** `hi from Helper`
- **E.** You must first run `javac Helper.java`, otherwise nothing is printed.

**Answer:** D

**Explanation:** Since Java 22 (JEP 458), the source launcher can run programs consisting of several source files. It compiles the launched file and then compiles other source files **on demand**, when a class they declare is needed. `Helper.java` is found in the same directory and compiled automatically.

`Broken.java` is never referenced, so it is never compiled and its error does not matter. If `Main` did reference `Broken`, the launch would fail with the compilation error.

## gap-compact-012

The file `Main.java` contains:

```java
class First {
    public static void main(String[] args) { System.out.println("First"); }
}
public class Main {
    public static void main(String[] args) { System.out.println("Main"); }
}
```

What is the result of running `java Main.java`?

- **A.** `Main`
- **B.** `First`
- **C.** `First` followed by `Main`
- **D.** Compilation fails because the file contains two classes with a `main` method.
- **E.** The launcher reports that the main class is ambiguous.

**Answer:** B

**Explanation:** In source-file mode the launcher starts with the **first top-level class** declared in the file. If that class has a main method, it is used — even if another class in the file matches the file name. Only if the first class has no main method does the launcher look for a class whose name matches the file name (`Main`), and if there is none it fails with "can't find class".

Compiling with `javac Main.java` and running `java Main` would print `Main`, because there you name the class explicitly.

## gap-compact-013

Which statements compile in a Java 25 program? (Choose three.)

- **A.** `IO.printf("%d%n", 1);`
- **B.** `IO.println();`
- **C.** `String s = IO.readln("Name: ");`
- **D.** `int n = IO.readInt();`
- **E.** `IO.print(42);`
- **F.** `IO io = new IO();`

**Answer:** B, C, E

**Explanation:** `java.lang.IO` (final in Java 25, implicitly available like every `java.lang` type) is a small utility class for beginners with exactly five static methods: `print(Object)`, `println(Object)`, `println()`, `readln()` and `readln(String prompt)`. `print(42)` autoboxes the `int`.

There is no `printf` (A) and no `readInt` (D) — `readln` always returns a `String` (or `null` at end of input). The constructor is private, so `new IO()` does not compile (F).
