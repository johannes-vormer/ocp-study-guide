---
title: "Java 17 → 25 gap"
topic: gap
order: 0
---

# Java 17 → 25: what changed for the exam

Your 1Z0-829 question bank covers Java 17. Everything below became **final** between Java 18 and Java 25 and is fair game on 1Z0-831. Every code example in this page was compiled and run on JDK 25; outputs are shown in comments.

Rule of thumb for the exam: if a feature was still **preview** or **incubator** in Java 25, it is *not* exam material (see the last section). A program that uses a preview feature without `--enable-preview` does **not compile**, and that can itself be a trap.

## Version table

| Version | Feature (final) | JEP / API | Domain |
|---|---|---|---|
| 18 | UTF-8 is the default charset | JEP 400 | d09-io |
| 18 | `@snippet` tag in javadoc | JEP 413 | d07-modules |
| 18 | Simple web server `jwebserver` | JEP 408 | d07-modules (tooling, minor) |
| 18 | Finalization deprecated for removal | JEP 421 | d03-oop |
| 18 | `Math.ceilDiv/ceilMod/ceilDivExact/floorDivExact/divideExact`, `Duration.isPositive()` | API | d01-values |
| 19 | `Locale.of(...)` factories (constructors deprecated) | API | d10-localization |
| 19 | `DateTimeFormatter.ofLocalizedPattern(String)` | API | d10-localization |
| 19 | `HashMap.newHashMap(n)`, `HashSet.newHashSet(n)`, `LinkedHashMap/LinkedHashSet` equivalents | API | d05-collections |
| 19 | `ExecutorService` is `AutoCloseable` (`close()`) | API | d08-concurrency |
| 19 | `Future.state()`, `resultNow()`, `exceptionNow()` | API | d08-concurrency |
| 19 | `Thread.sleep(Duration)`, `join(Duration)`, `threadId()` (`getId()` deprecated) | API | d08-concurrency |
| 20 | `Thread.stop()` always throws `UnsupportedOperationException` | API change | d08-concurrency |
| 21 | Record patterns | JEP 440 | d02-flow |
| 21 | Pattern matching for `switch` | JEP 441 | d02-flow |
| 21 | Sequenced collections | JEP 431 | d05-collections |
| 21 | Virtual threads | JEP 444 | d08-concurrency |
| 21 | `Math.clamp`, `StringBuilder.repeat`, `String.indexOf(…, begin, end)`, `String.splitWithDelimiters`, `Character.isEmoji` & co., `Locale.availableLocales()` | API | d01-values / d10 |
| 22 | Unnamed variables & patterns (`_`) | JEP 456 | d03-oop (patterns: d02) |
| 22 | Launch multi-file source-code programs | JEP 458 | d07-modules |
| 22 | `java.text.ListFormat` | API | d10-localization |
| 23 | Markdown documentation comments (`///`) | JEP 467 | d07-modules (tooling) |
| 23 | `Instant.until(Instant)`, `NumberFormat.setStrict/isStrict` | API | d01 / d10 |
| 24 | Stream gatherers (`Stream.gather`) | JEP 485 | d06-streams |
| 24 | Virtual threads no longer pinned by `synchronized` | JEP 491 | d08-concurrency |
| 24 | Security Manager permanently disabled | JEP 486 | – (removal) |
| 24 | `Reader.of(CharSequence)` | API | d09-io |
| 25 | Scoped values | JEP 506 | d08-concurrency |
| 25 | Module import declarations | JEP 511 | d07-modules |
| 25 | Compact source files and instance main methods, `java.lang.IO` | JEP 512 | d07-modules |
| 25 | Flexible constructor bodies | JEP 513 | d03-oop |
| 25 | `Math.powExact`, `unsignedMultiplyExact`; `Reader.readAllLines/readAllAsString`; `Currency.availableCurrencies()` | API | d01 / d09 |

---

## 🆕 Java 21 — Pattern matching for switch (JEP 441) — d02

**Rules**

- Any reference type can be the selector (`Object`, interfaces, sealed types…). Case labels may be **type patterns** (`case String s`), **record patterns** (`case Point(var x, var y)`), constants, `null`, and `default`.
- **Guards**: `case Integer i when i > 10 ->`. Guards are only allowed on pattern labels (`case "x" when …` does not compile). Local variables used in a guard must be final or effectively final. A guard that is the constant `false` is a compile error; a guard `when true` makes the label *unguarded*.
- **Labels are tested top-down**; the first match wins (not the "most specific").
- **Dominance**: a label that can never be reached because an earlier one covers it is a compile error (`case CharSequence cs` then `case String s`; `case Integer i` then `case 1`). A *guarded* pattern does not dominate later constants, so `case Integer i when i > 5` followed by `case 1` is fine.
- `default` (or `case null, default`) must not come before pattern labels it would dominate, and `case null` may not follow `default`.
- **Exhaustiveness**: a switch *expression* must always be exhaustive. A switch *statement* must be exhaustive **as soon as it uses a pattern or `case null`** (an "enhanced" switch). Old-style statements on an enum/String/int remain non-exhaustive-friendly.
- Sealed hierarchies are exhaustive without `default` if every permitted subtype is covered. `default` and an unconditional pattern (`case Object o`) together → compile error.
- **null**: without `case null`, any switch throws `NullPointerException` when the selector is null (even if there is `default`). `case null` can be combined only as `case null, default`.
- **Fall-through**: with colon-style labels, falling *into* a pattern label that declares bindings is illegal ("illegal fall-through to a pattern"); falling into `default` is fine.
- 🆕 Java 21: **qualified enum constants** may be used as case constants (`case Compass.N`), which lets you mix enum constants of several enums in a switch over a sealed interface.

```java
static String test(Object o) {
    return switch (o) {
        case Integer i when i > 10 -> "big";
        case Integer i             -> "int";
        case String s when s.isEmpty() -> "empty";
        case String s              -> "str" + s.length();
        case null                  -> "null";
        default                    -> "other";
    };
}
// test(42) big, test(7) int, test("") empty, test(3.0) other, test(null) null
```

```java
sealed interface Dir permits Compass, Turn {}
enum Compass implements Dir { N, S }
enum Turn implements Dir { LEFT, RIGHT }

static String m(Dir d) {
    return switch (d) {          // exhaustive: N, S, LEFT + Turn t
        case Compass.N -> "n";
        case Compass.S -> "s";
        case Turn.LEFT -> "l";
        case Turn t    -> "t" + t.ordinal();
    };
}
// m(Turn.RIGHT) + m(Compass.S) + m(Turn.LEFT)  ->  t1sl
```

**Exam traps**

- Boxing decides the type: `'a' + 1` is an `Integer`, `(char)('a'+1)` a `Character`.
- `case null` is the only way to avoid an NPE on a null selector.
- A switch statement over a sealed type using patterns that misses a subtype does **not** compile (it is not "just skipped").
- Generic sealed types: `sealed interface Box<T> permits StrBox, AnyBox` with `StrBox implements Box<String>` — a switch over `Box<Integer>` only needs `case AnyBox<Integer> a`, but a switch over `Box<String>` needs both.
- If a record accessor throws during matching, the exception is wrapped in `MatchException`.

## 🆕 Java 21 — Record patterns (JEP 440) — d02

**Rules**

- `o instanceof Point(int x, int y)` deconstructs via the **accessor methods** (an overridden accessor's value is what you get).
- Nesting: `Line(Point(var x1, var y1), Point p2)`. `var` is allowed for components, **not** as a top-level pattern (`case var x` is illegal).
- The number of nested patterns must equal the number of components. Component pattern types must be compatible: `Point(long x, long y)` or `Point(Integer x, …)` for `int` components need *primitive patterns* (preview in 25) → compile error.
- A record pattern itself has **no binding name** (`Point(int x, int y) p` was dropped before Java 21 – does not compile).
- **null**: a record pattern never matches `null`. A *nested* type pattern that is unconditional for the component type (e.g. `Point a`, `Object a`, `var a`) **does** match a null component, but a nested record pattern does not.
- Generic records: type arguments are inferred — `case Ok(var v)` on a `Result<Integer>` gives `v` the type `Integer`. From `Object`, `Box(var v)` gives `v` type `Object`; `Box<String>(var v)` from `Object` is a compile error (unchecked cast).
- Record patterns in the enhanced `for` loop were a preview in Java 20 and were **removed** — `for (Point(var x, var y) : list)` does not compile.
- Pattern binding scope follows flow scoping: `a instanceof P(var x, var y) && x > y` ✔; `… || x > y` ✘.

```java
record Point(int x, int y) {}
record Line(Point a, Point b) {}

Object o = new Line(null, new Point(1, 1));
System.out.print(o instanceof Line(Point a, Point b) ? "A" : "-");              // A
System.out.print(o instanceof Line(Point(var x, var y), var b) ? "B" : "-");     // -
System.out.print(o instanceof Line(var a, Point(int x, int y)) ? "C" : "-");     // C
System.out.print(o instanceof Line(Object a, Object b) ? "D" : "-");             // D
```

**Exam traps**: exhaustiveness of nested patterns (`Pair(Dog, Animal)`, `Pair(Cat, Dog)`, `Pair(Animal, Cat)` is exhaustive), order of evaluation (first match wins), `MatchException` from throwing accessors.

## 🆕 Java 22 — Unnamed variables and patterns (JEP 456) — d03 / d02

`_` declares a variable or pattern that is never used. It can never be read (`System.out.println(_)` → "underscore not allowed here").

**Allowed**: local variables (with initializer: `int _;` does not compile), `var _ = …`, several `_` in the same scope, `for` init and enhanced-for variables, resources in try-with-resources, `catch (Exception _)`, lambda parameters (`(_, _) -> 0`, `_ -> 1`), type patterns (`case Car _`) and the unnamed pattern inside record patterns (`Point(var x, _)`, `Point(_, _)`).

**Not allowed**: fields, method/constructor parameters, record components, method names, top-level `case _` or `instanceof _`.

**Multiple patterns in one case** (only possible since 22): `case Car _, Bike _ ->` is fine, but a case with several patterns may not declare *any* named binding (`case Integer i, String s` → error). A guard is allowed after such a case.

```java
record Truck(int load) implements Vehicle {}
return switch (v) {
    case Car _, Bike _                  -> "light";
    case Truck(int load) when load > 10 -> "heavy";
    case Truck _                        -> "truck";
};

try (var _ = new Res("a"); var _ = new Res("b")) { … } catch (IllegalStateException _) { … }
```

**Exam traps**: `__` (two underscores) and `$` are still ordinary identifiers; `var _ = list.remove(0);` still executes the initializer; lambdas may not mix `var`/explicit/implicit parameter styles even with `_`.

## 🆕 Java 25 — Flexible constructor bodies (JEP 513) — d03

Statements may now appear **before** `super(…)` / `this(…)`. They form the **prologue**; statements after it form the **epilogue**. The prologue is an *early construction context*.

**In the prologue you may**: declare locals, compute and validate arguments, `throw`, use loops/ifs/blocks, call **static** methods, use parameters, use fields of an *enclosing* instance (`Outer.this.x`, or simply `x` if it is an outer field), and **assign** (not read!) fields of the class being constructed **that have no initializer** (`this.side = side;` or `side = …` when not shadowed).

**In the prologue you may not**: read instance fields, call instance methods (`log()`, `hashCode()`), use `this` as a value or `super.`anything, assign a field that has an initializer (`int area = 0;`), assign a field inside a lambda, use `return`.

Other rules that did not change: at most one explicit constructor call; it must be a top-level statement of the constructor body (not inside `if`, `try`, a loop or a block); record **canonical** constructors (including compact ones) still may not contain `this(…)`/`super(…)`; non-canonical record constructors must call `this(…)` and may now have a prologue. Enum constructors can also have a prologue before `this(…)`.

Execution order: **prologue → superclass constructor → instance initializers & field initializers (in textual order) → epilogue**.

```java
class Base {
    Base() { show(); }
    void show() { System.out.print("base "); }
}
class Child extends Base {
    private final String name;
    Child(String n) {
        name = n.toUpperCase();   // prologue: allowed, field has no initializer
        super();
        System.out.print("child ");
    }
    @Override void show() { System.out.print(name + " "); }
}
new Child("x");   // X child      (in Java 17 style it would print "null child")
```

**Exam traps**: `if (x > 0) super(1); else super(2);` still doesn't compile; a `final` field assigned in the prologue of a constructor that delegates with `this(…)` → "might already have been assigned"; printing `side` in the prologue prints the *parameter* if it shadows the field.

## 🆕 Java 25 — Compact source files & instance main methods (JEP 512) — d07

**Compact source file**: a `.java` file whose top level has fields and methods but no enclosing class declaration:

```java
// Hello.java
List<String> names = new ArrayList<>();   // instance field, no import needed

void main() {
    names.add("b"); names.add("a");
    Collections.sort(names);
    IO.println(names);                     // [a, b]
}
```

- The file implicitly declares a **final top-level class** in the **unnamed package**, named after the file (`Hello`), extending `Object`, with a default constructor. It may not have a `package` declaration ("compact source file should not have package declaration"), constructors, or initializer blocks. Classes declared in it are **member classes** (`Hello$Helper`).
- It must declare at least one method, and must have a launchable `main`.
- Every compact source file implicitly has `import module java.base;` — `List`, `Path`, `Files`, `LocalDate`… are available without imports.
- `IO`'s methods are **not** statically imported: `println("x")` does not compile; write `IO.println("x")`.
- Fields are instance fields: a `static void main()` cannot use them.
- Run with `java Hello.java` or compile with `javac Hello.java` → `Hello.class`, then `java Hello`.

**`java.lang.IO`** (final in 25): `print(Object)`, `println(Object)`, `println()`, `readln()`, `readln(String prompt)`. No `printf`, no `readInt`, no public constructor. It writes to `System.out` and reads from `System.in`.

**Main method selection (any class, not only compact files)** — a *candidate* main is non-private, `void`, named `main`, with either a single `String[]`/`String...` parameter or no parameters; it may be `static` or instance, and may be inherited.

1. If there is a candidate with a `String[]` parameter, it is invoked.
2. Otherwise the candidate with no parameters is invoked.
3. If it is an instance method, the launcher first does `new TheClass()`; this needs a non-private no-arg constructor, otherwise the launch fails at **run time** (it compiles fine).

```java
public class Main {
    static void main() { System.out.println("static no-arg"); }
    void main(String[] args) { System.out.println("instance args " + args.length); }
}
// java Main.java a b   ->  instance args 2
```

`private static void main(String[] a)` is not a candidate, so a `void main()` next to it is the one that runs.

## 🆕 Java 25 — Module import declarations (JEP 511) — d07

`import module M;` imports on demand all public top-level types of all packages **exported** by `M` to the current module, plus those of modules `M` **requires transitive**ly. Works in any source file, modular or not.

```java
import module java.sql;     // java.sql requires transitive java.logging, java.xml
...
Logger log = Logger.getLogger("x");   // java.util.logging.Logger — compiles
Connection c = null;                   // java.sql.Connection
```

**Ambiguity**: two module imports that bring the same simple name → error at the *use* of the name (not at the import):

```java
import module java.base;      // java.util.List, java.util.Date
import module java.desktop;   // java.awt.List
List<String> l = new ArrayList<>();   // error: reference to List is ambiguous
```

Fixes: a single-type import `import java.util.List;` or a package on-demand import `import java.util.*;` — both **shadow** module imports. Reordering module imports does not help. The same ambiguity exists for `Date` with `java.base` + `java.sql`.

Module imports never shadow anything; `java.lang.*` (implicit) wins over a module-imported type with the same name. In a named module you must `requires` the module you import.

## 🆕 Java 22 — Launch multi-file source-code programs (JEP 458) — d07

`java Main.java` compiles `Main.java` in memory and **compiles other source files on demand** when `Main` references their classes (looked up in the source tree, rooted by the package of the launched file). Unreferenced files with errors are ignored; a referenced file with an error fails the launch with a compile error. Libraries go on `--class-path`.

Which class runs: the **first top-level class** in the launched file, if it has a main method; otherwise the class whose name matches the file name; otherwise "error: can't find class".

```java
// Main.java
class First { public static void main(String[] a) { System.out.println("First"); } }
public class Main { public static void main(String[] a) { System.out.println("Main"); } }
// java Main.java  ->  First
```

## 🆕 Java 21 — Sequenced collections (JEP 431) — d05

New interfaces: `SequencedCollection<E>` (`addFirst/addLast/getFirst/getLast/removeFirst/removeLast/reversed`), `SequencedSet<E>` (`reversed()` returns `SequencedSet`), `SequencedMap<K,V>` (`firstEntry/lastEntry/pollFirstEntry/pollLastEntry/putFirst/putLast/reversed/sequencedKeySet/sequencedValues/sequencedEntrySet`).

Implemented by: `List` (all lists), `Deque`, `LinkedHashSet`, `SortedSet`/`NavigableSet` (`TreeSet`), `LinkedHashMap`, `SortedMap` (`TreeMap`). **Not** `HashSet`/`HashMap`.

```java
List<Integer> list = new ArrayList<>(List.of(1, 2, 3));
list.addFirst(0); list.addLast(4);
list.reversed();                       // [4, 3, 2, 1, 0] – a VIEW
list.reversed().removeFirst();         // removes 4 from list
LinkedHashSet<String> set = new LinkedHashSet<>(List.of("a", "b", "c"));
set.addFirst("c");                     // [c, a, b]  – moves an existing element
LinkedHashMap<String,Integer> m = new LinkedHashMap<>(); m.put("x",1); m.put("y",2);
m.putFirst("y", 3);                    // {y=3, x=1}
```

Traps: `reversed()` is a live view; `getFirst()` on an empty list throws `NoSuchElementException` (not `IndexOutOfBounds`); `TreeSet.addFirst` and anything on an unmodifiable list throw `UnsupportedOperationException`; `List.getFirst()` exists now, so code that used `list.get(0)` and `getFirst()` behaves the same except on empty lists (exception type).

## 🆕 Java 24 — Stream gatherers (JEP 485) — d06

`Stream.gather(Gatherer)` is an intermediate operation for custom transformations. Built-ins in `java.util.stream.Gatherers`:

```java
Stream.of(1,2,3,4,5).gather(Gatherers.windowFixed(2)).toList();    // [[1, 2], [3, 4], [5]]
Stream.of(1,2,3,4).gather(Gatherers.windowSliding(3)).toList();    // [[1, 2, 3], [2, 3, 4]]
Stream.of(1,2,3,4).gather(Gatherers.scan(() -> 0, Integer::sum)).toList();       // [1, 3, 6, 10]
Stream.of(1,2,3,4).gather(Gatherers.fold(() -> "", (a, b) -> a + b)).toList();   // [1234]
Stream.of(1,2,3).gather(Gatherers.mapConcurrent(2, x -> x * 10)).toList();       // [10, 20, 30]
```

`fold` emits a **single** element (still a stream, so `.toList()` gives a one-element list). `scan` emits every intermediate result. `windowFixed(0)` → `IllegalArgumentException`. `mapConcurrent` uses virtual threads and preserves encounter order.

Custom gatherers: `Gatherer.of(integrator)` (parallelizable) / `Gatherer.ofSequential(...)`; the integrator `(state, element, downstream) -> boolean` returns `false` to stop (short-circuit); `downstream.push(x)` emits.

```java
Gatherer<String, Void, String> takeUntilStop = Gatherer.ofSequential(
    (Void st, String el, Gatherer.Downstream<? super String> down) -> !el.equals("stop") && down.push(el));
Stream.of("a","b","stop","c").gather(takeUntilStop).toList();       // [a, b]
```

## 🆕 Java 21 — Virtual threads (JEP 444) — d08 (+ 🆕 Java 24 JEP 491)

```java
Thread v = Thread.ofVirtual().name("v1").start(() -> …);
Thread v2 = Thread.startVirtualThread(() -> {});
Thread u = Thread.ofVirtual().unstarted(() -> {});       // state NEW
Thread p = Thread.ofPlatform().name("p").start(() -> {});
try (ExecutorService ex = Executors.newVirtualThreadPerTaskExecutor()) {
    Future<Integer> f = ex.submit(() -> 21 * 2);
    f.get();                                              // 42
}                                                         // close() waits for tasks
```

Rules: virtual threads are always **daemon** (`setDaemon(false)` → `IllegalArgumentException`) and have normal priority (the builder has no `priority`); `isVirtual()` tells them apart; they are cheap — don't pool them, create one per task. They are for blocking I/O-bound work, not for CPU speed-ups. `ThreadLocal` works but is costly with millions of threads (→ scoped values). Since Java 24, blocking inside `synchronized` no longer pins the carrier thread.

**🆕 Java 19 related API**: `ExecutorService extends AutoCloseable`: `close()` = `shutdown()` + wait for termination (so try-with-resources waits for all tasks). `Future.state()` (`RUNNING, SUCCESS, FAILED, CANCELLED`), `resultNow()` (throws `IllegalStateException` if not successfully completed), `exceptionNow()`. `Thread.sleep(Duration)`, `join(Duration)`, `threadId()`.

## 🆕 Java 25 — Scoped values (JEP 506) — d08

Immutable, bounded-lifetime alternative to `ThreadLocal`.

```java
static final ScopedValue<String> USER = ScopedValue.newInstance();

USER.isBound();                       // false
USER.orElse("anon");                  // anon
ScopedValue.where(USER, "alice").run(() -> {
    USER.get();                                        // alice
    ScopedValue.where(USER, "bob").run(() -> USER.get());   // bob (rebinding, nested)
    USER.get();                                        // alice again
});
String r = ScopedValue.where(USER, "carol").call(() -> USER.get().toUpperCase());  // CAROL
USER.get();                           // NoSuchElementException (unbound)
```

Rules: there is no `set()`; a binding is visible only in the dynamic scope of `run`/`call`; `call` takes a `CallableOp` whose checked exception type is propagated; a plain `new Thread`/virtual thread started inside the scope does **not** inherit the binding (only `StructuredTaskScope` forks do — preview); `orElse(null)` throws NPE, but binding the value `null` with `where` is allowed in 25.

## Smaller API additions (verify these by heart)

| API | Since | Example → result |
|---|---|---|
| `Locale.of(lang[, country[, variant]])` | 19 | `Locale.of("de","CH")` → `de_CH`; `new Locale(...)` deprecated (still works) |
| `Math.clamp(value, min, max)` | 21 | `clamp(15,0,10)` → 10; `clamp(5_000_000_000L,0,100)` returns **int** 100; `min > max` → `IllegalArgumentException` |
| `Math.ceilDiv/ceilMod` | 18 | `ceilDiv(7,2)` 4, `ceilDiv(-7,2)` -3, `floorDiv(-7,2)` -4, `ceilMod(7,2)` -1 |
| `Math.powExact(int,int)` | 25 | `powExact(2,10)` 1024; overflow → `ArithmeticException` |
| `StringBuilder.repeat(CharSequence,int)` / `repeat(int codePoint,int)` | 21 | `new StringBuilder().repeat("ab",3)` → `ababab`; `repeat('-',3)` appends `---` (char → code point) |
| `String.indexOf(ch/str, begin, end)` | 21 | `"banana".indexOf("na",3,6)` → 4 |
| `String.splitWithDelimiters(regex, limit)` | 21 | `"a1b22c".splitWithDelimiters("\\d+",0)` → `[a, 1, b, 22, c]` |
| `Character.isEmoji(int)` etc. | 21 | `isEmoji(0x1F600)` true |
| `HashMap.newHashMap(n)` | 19 | sized for *n mappings* (unlike `new HashMap<>(n)`, where n is the table capacity) |
| `Duration.isPositive()` | 18 | `Duration.ZERO.isPositive()` false |
| `Instant.until(Instant)` | 23 | returns a `Duration`, e.g. `PT1M30S` |
| `DateTimeFormatter.ofLocalizedPattern("yMMM")` | 19 | US → `Sep 2025` |
| `ListFormat` | 22 | STANDARD → `a, b, and c`; OR → `a, b, or c` (US) |
| `NumberFormat.setStrict(true)` | 23 | default lenient: `parse("12abc")` → 12; strict → `ParseException` |
| `Reader.of(CharSequence)`, `Reader.readAllLines()` | 24 / 25 | `Reader.of("l1\nl2").readAllLines()` → `[l1, l2]` |
| `Locale.availableLocales()`, `Currency.availableCurrencies()` | 21 / 25 | return `Stream`s |

## 🆕 Java 23 — Markdown documentation comments (JEP 467)

Doc comments may be written as consecutive `///` lines using Markdown (CommonMark) instead of `/** … */` with HTML. Block tags (`@param`, `@return`) still work; links use `[Math#addExact(int, int)]`.

```java
/// Adds numbers. See [Math#addExact(int, int)].
///
/// - **fast**
/// - _simple_
public class Calc {
    /// @return `a + b`
    public static int add(int a, int b) { return a + b; }
}
```

To the compiler `///` is just an ordinary line comment.

## 🆕 Java 18 — UTF-8 by default (JEP 400) — d09

`Charset.defaultCharset()` is UTF-8 everywhere, so `FileReader`, `FileWriter`, `Files.newBufferedReader` without a charset, `Scanner(File)`, `PrintStream` etc. all use UTF-8 regardless of the OS locale. (Console output `System.out` still uses the platform console encoding.)

---

## Removed / changed since Java 17

- **Locale constructors** deprecated (19) — use `Locale.of(...)` or `Locale.forLanguageTag`. They still compile (with a warning) and work.
- **Finalization deprecated for removal** (18, JEP 421): `Object.finalize()` and `Runtime.runFinalization()` are `@Deprecated(forRemoval=true)`; overriding `finalize` gives a removal warning. Use try-with-resources/`Cleaner`.
- **`Thread.stop()`** always throws `UnsupportedOperationException` (20). `Thread.suspend()`/`resume()` no longer exist in JDK 25. `Thread.getId()` deprecated (19) → `threadId()`.
- **Security Manager permanently disabled** (24, JEP 486): `System.setSecurityManager(...)` throws `UnsupportedOperationException`.
- **URL constructors** deprecated (20) → `URI.create(...).toURL()`.
- **String templates** (`STR."…"`) were previews in 21–22 and were **withdrawn**; they are not part of Java 25.
- **Record patterns in enhanced `for`** (preview in 20) were dropped in 21.
- In compact source files, the static auto-import of `IO` methods (from earlier previews) was dropped: write `IO.println`.
- **JDBC** (`java.sql`) appears not to be an exam objective of 1Z0-831 any more.

## Preview / incubator in Java 25 — NOT on the exam

- Primitive types in patterns, `instanceof` and `switch` — JEP 507 (third preview). That is why `Point(long x, long y)` for `int` components does not compile.
- Structured concurrency (`StructuredTaskScope`) — JEP 505 (fifth preview).
- Stable values (`StableValue`) — JEP 502 (preview).
- PEM encodings of cryptographic objects — JEP 470 (preview).
- Vector API — JEP 508 (incubator).
