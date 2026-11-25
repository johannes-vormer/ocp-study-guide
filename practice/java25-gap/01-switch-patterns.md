---
title: "Gap: Pattern matching for switch"
source: java25-gap
topic: d02-flow
---

# Gap: Pattern matching for switch (🆕 Java 21, JEP 441)

## gap-switch-001

What is the output of the following code?

```java
public class S01 {
    static String test(Object o) {
        return switch (o) {
            case Integer i when i > 10 -> "big";
            case Integer i -> "int";
            case String s when s.isEmpty() -> "empty";
            case String s -> "str" + s.length();
            default -> "other";
        };
    }
    public static void main(String[] args) {
        System.out.println(test(42) + " " + test(7) + " " + test("") + " " + test(3.0));
    }
}
```

- **A.** `big big empty other`
- **B.** `big int str0 other`
- **C.** `big int empty other`
- **D.** `int int empty other`
- **E.** The code does not compile because two case labels declare a pattern variable named `i`.
- **F.** The code does not compile because the second `Integer` case is dominated by the first.

**Answer:** C

**Explanation:** Case labels are tested from top to bottom and the first one that matches (including its guard) wins. `42` matches `Integer i when i > 10` → `big`. `7` fails the guard and falls to the unguarded `case Integer i` → `int`. `""` matches `String s when s.isEmpty()` → `empty`. `3.0` is a `Double`, so only `default` matches → `other`.

Each pattern variable's scope is only its own case, so reusing `i` and `s` is fine (E is wrong). A *guarded* pattern never dominates a later unguarded pattern of the same type — only the reverse order would be an error (F is wrong).

## gap-switch-002

Which line causes a compilation error?

```java
static String kind(Object o) {
    return switch (o) {
        case CharSequence cs -> "cs";        // line 1
        case String s -> "s";                // line 2
        case Integer i when i > 0 -> "pos";  // line 3
        case Integer i -> "int";             // line 4
        case Number n -> "num";              // line 5
        default -> "?";                      // line 6
    };
}
```

- **A.** line 1
- **B.** line 2
- **C.** line 4
- **D.** line 5
- **E.** line 6
- **F.** The code compiles without errors.

**Answer:** B

**Explanation:** `String` implements `CharSequence`, so every `String` is already matched by `case CharSequence cs`. The label on line 2 can never be selected and javac reports "this case label is dominated by a preceding case label".

Line 4 is fine: the guarded label on line 3 does not dominate the unguarded `Integer` label. Line 5 is fine because `Number` is more general than `Integer` and comes *after* it. `default` at the end is allowed because `Number n` is not unconditional for `Object`.

## gap-switch-003

What is the output?

```java
public class S03 {
    static void print(String s) {
        switch (s) {
            case "a" -> System.out.print("A");
            case null, default -> System.out.print("N");
        }
    }
    public static void main(String[] args) {
        print(null); print("a"); print("b");
    }
}
```

- **A.** `NA` followed by a `NullPointerException`
- **B.** A `NullPointerException` is thrown before anything is printed.
- **C.** `NAB`
- **D.** `NAN`
- **E.** The code does not compile because `case null` cannot be combined with `default`.
- **F.** The code does not compile because a switch with `case null` must be a switch expression.

**Answer:** D

**Explanation:** Since Java 21 a switch may have a `case null` label, and `null` may be combined with `default` as `case null, default`. With that label a null selector no longer throws `NullPointerException`; it executes the combined branch. So `print(null)` → `N`, `print("a")` → `A`, `print("b")` → `N` (default).

Without `case null`, *every* switch (old or new style, with or without `default`) throws `NullPointerException` for a null selector.

## gap-switch-004

Given:

```java
sealed interface Shape permits Circle, Square, Rect {}
record Circle(double r) implements Shape {}
final class Square implements Shape {}
non-sealed class Rect implements Shape {}

static int code(Shape s) {
    return switch (s) {
        case Circle c -> 1;
        case Square q -> 2;
    };
}
```

The method does not compile. Which two changes, each applied on its own, make it compile? (Choose two.)

- **A.** Add `case Rect r -> 3;` as the last case.
- **B.** Turn the switch expression into a switch statement that prints the values instead of returning them.
- **C.** Add `case null -> 0;` as the last case.
- **D.** Add `default -> 0;` as the last case.
- **E.** Declare `Rect` as `final` instead of `non-sealed`.
- **F.** Remove the `sealed` and `permits` keywords from `Shape`.

**Answer:** A, D

**Explanation:** A switch expression must be exhaustive. The permitted subtypes of `Shape` are `Circle`, `Square` and `Rect`; `Rect` is missing, so javac reports "the switch expression does not cover all possible input values". Covering `Rect` (A) or adding `default` (D) fixes it.

B does not help: a switch *statement* that uses pattern labels is an enhanced switch and must be exhaustive too. C does not cover `Rect`. E changes nothing about coverage. F makes it worse — without `sealed`, the compiler cannot know all subtypes, so only `default` or `case Shape x` would make it exhaustive.

## gap-switch-005

Given:

```java
enum Color { RED, GREEN }

static void a(Color c) {
    switch (c) { case RED -> System.out.print(1); }
}
static void b(Object o) {
    switch (o) { case String s -> System.out.print(2); }
}
static void c(Color c) {
    switch (c) { case RED -> System.out.print(3); case null -> System.out.print(4); }
}
static int d(Color c) {
    return switch (c) { case RED -> 5; case GREEN -> 6; };
}
```

Which methods compile? (Choose two.)

- **A.** `b`
- **B.** `a`
- **C.** `c`
- **D.** `d`

**Answer:** B, D

**Explanation:** `a` is an "old-style" switch statement on an enum that uses only constant labels; such statements are still allowed to be non-exhaustive. `d` is a switch expression that lists every enum constant, so it is exhaustive without `default`.

`b` uses a type pattern, which makes it an enhanced switch statement — it must be exhaustive, and `String` does not cover `Object`. `c` looks like the old style, but `case null` also turns the statement into an enhanced switch, so it must now be exhaustive and `GREEN` is missing. Both fail with "the switch statement does not cover all possible input values".

## gap-switch-006

What is the output?

```java
public class S06 {
    sealed interface Dir permits Compass, Turn {}
    enum Compass implements Dir { N, S }
    enum Turn implements Dir { LEFT, RIGHT }

    static String m(Dir d) {
        return switch (d) {
            case Compass.N -> "n";
            case Compass.S -> "s";
            case Turn.LEFT -> "l";
            case Turn t -> "t" + t.ordinal();
        };
    }
    public static void main(String[] args) {
        System.out.println(m(Turn.RIGHT) + m(Compass.S) + m(Turn.LEFT));
    }
}
```

- **A.** `t1st0`
- **B.** `t2sl`
- **C.** The code does not compile because enum constants in case labels must be unqualified.
- **D.** The code does not compile because the switch is not exhaustive.
- **E.** `t1sl`
- **F.** `t1sl` is printed only if a `default` label is added.

**Answer:** E

**Explanation:** Since Java 21 a case constant may be a *qualified* enum constant name such as `Compass.N`. This is what allows constants of two different enums in one switch whose selector type is the sealed interface `Dir`.

The switch is exhaustive: `Compass` is fully covered by `N` and `S`, and `Turn` by `case Turn t`. `m(Turn.RIGHT)` does not match `Turn.LEFT`, so it reaches `case Turn t` → `"t" + 1`. `m(Compass.S)` → `s`. `m(Turn.LEFT)` hits the constant label first → `l`. Result: `t1sl`.

## gap-switch-007

Given:

```java
static void m(Object o) {
    int limit = 3;
    int max = 10;
    max++;
    switch (o) {
        // INSERT
        default -> System.out.print("D");
    }
}
```

Which case labels can be inserted at `// INSERT` so that the code compiles? (Choose three.)

- **A.** `case Integer i when i > limit -> System.out.print("A");`
- **B.** `case Integer i when i < max -> System.out.print("B");`
- **C.** `case Integer i when false -> System.out.print("C");`
- **D.** `case String s when s.isBlank() || limit > 0 -> System.out.print("E");`
- **E.** `case 42 when limit > 0 -> System.out.print("F");`
- **F.** `case Integer i when (i > 0) == true -> System.out.print("G");`

**Answer:** A, D, F

**Explanation:** A guard (`when`) is any boolean expression, but local variables it uses must be final or effectively final. `limit` is never changed, so A and D compile; `max` is incremented, so B fails ("local variables referenced from a guard must be final or effectively final").

C fails because a guard that is a constant expression with the value `false` is explicitly forbidden. E fails because guards are only allowed on pattern labels, never on constant labels ("guards are only allowed for case with a pattern"). F is an ordinary (if silly) boolean expression and compiles.

## gap-switch-008

What is the output?

```java
public class S08 {
    static void m(Object o) {
        switch (o) {
            case String s:
                System.out.print("S");
                break;
            case Integer i:
                System.out.print("I");
            default:
                System.out.print("D");
        }
    }
    public static void main(String[] args) {
        m(5); m("x"); m(2.0);
    }
}
```

- **A.** `ISD`
- **B.** `IDSD`
- **C.** `IDSDD`
- **D.** The code does not compile because pattern labels cannot be used with colon-style (`:`) cases.
- **E.** The code does not compile because of illegal fall-through from `case Integer i`.

**Answer:** B

**Explanation:** Pattern labels work with both `->` and `:` forms. With the colon form, fall-through still happens. `m(5)` prints `I` and, because there is no `break`, falls through into `default` and prints `D`. `m("x")` prints `S` and breaks. `m(2.0)` prints `D`.

Falling through *into* `default` is legal. What is illegal is falling through *into* a pattern label that declares a binding variable (the variable would be unassigned) — that is not the case here.

## gap-switch-009

What is the result of compiling and running the following?

```java
public class S09 {
    static void m(Object o) {
        switch (o) {
            case String s:
                System.out.print("S");
            case Integer i:
                System.out.print("I");
                break;
            default:
                System.out.print("D");
        }
    }
    public static void main(String[] args) { m("x"); }
}
```

- **A.** `S`
- **B.** `SI`
- **C.** `SID`
- **D.** A `ClassCastException` is thrown.
- **E.** The code does not compile.

**Answer:** E

**Explanation:** The `case String s` group has no `break`, so control would fall through into `case Integer i`. That label declares the binding `i`, which would never have been initialized for a `String`. javac reports "illegal fall-through to a pattern". Either add `break` or use the arrow form, which never falls through.

## gap-switch-010

What is the output?

```java
public class S10 {
    public static void main(String[] args) {
        Object[] arr = { 'a', 'a' + 1, (char) ('a' + 1), 1L, 1.0f };
        for (Object o : arr) {
            System.out.print(switch (o) {
                case Character c -> "C";
                case Integer i -> "I";
                case Long l -> "L";
                case Number n -> "N";
                default -> "?";
            });
        }
    }
}
```

- **A.** `CCCLN`
- **B.** `CICL?`
- **C.** `CIILN`
- **D.** `CICLN`
- **E.** The code does not compile because `default` is dominated by `case Number n`.

**Answer:** D

**Explanation:** The array elements are boxed according to the type of each expression: `'a'` → `Character`; `'a' + 1` is an `int` expression (binary numeric promotion) → `Integer`; `(char)('a' + 1)` → `Character`; `1L` → `Long`; `1.0f` → `Float`, which is matched by `case Number n`.

`default` is not dominated: `Number` does not cover all `Object`s (a `Character` is not a `Number`), so `default` is still needed and legal.

## gap-switch-011

What is the output?

```java
public class S11 {
    static String f(Integer x) {
        return switch (x) {
            case Integer i when i > 5 -> "big";
            case 1 -> "one";
            case Integer i -> "other";
        };
    }
    public static void main(String[] args) {
        System.out.println(f(1) + f(9) + f(3));
    }
}
```

- **A.** `bigbigother`
- **B.** `oneotherother`
- **C.** `onebigother`
- **D.** The code does not compile because `case 1` is dominated by the guarded pattern.
- **E.** The code does not compile because a constant label cannot be mixed with pattern labels.

**Answer:** C

**Explanation:** Constant labels and pattern labels may be mixed when the selector type allows the constant (`Integer` selector, `int` constant). A *guarded* pattern does not dominate a constant label, so `case 1` after `case Integer i when i > 5` is legal. (An *unguarded* `case Integer i` before `case 1` would be a dominance error.)

`f(1)`: the guard `1 > 5` is false, then `case 1` matches → `one`. `f(9)` → `big`. `f(3)`: guard false, not `1`, so the unconditional `case Integer i` → `other`. The final unguarded `Integer` label also makes the switch exhaustive.

## gap-switch-012

What is the result of compiling the following method?

```java
static void m(Object o) {
    switch (o) {
        case String s -> System.out.print("S");
        case Object obj -> System.out.print("O");
        default -> System.out.print("D");
    }
}
```

- **A.** It compiles; `default` is simply never executed.
- **B.** It compiles; `default` handles `null` values.
- **C.** It does not compile because `case Object obj` dominates `case String s`.
- **D.** It does not compile because the switch has both an unconditional pattern and a `default` label.
- **E.** It does not compile because a switch statement on `Object` must be a switch expression.

**Answer:** D

**Explanation:** `case Object obj` is unconditional for the selector type `Object` — it matches everything (except `null`, which still causes an NPE unless there is `case null`). javac reports "switch has both an unconditional pattern and a default label". Remove one of them.

C is wrong: `Object obj` comes *after* `String s`, so it doesn't dominate it; the reverse order would. B is wrong: `default` never matches `null`; only `case null` does.

## gap-switch-013

What is the output?

```java
import java.util.List;
public class S13 {
    static int count = 0;
    static boolean check(int i) { count++; return i > 2; }
    public static void main(String[] args) {
        for (Object o : List.of(1, "x", 5)) {
            switch (o) {
                case Integer i when check(i) -> System.out.print("A");
                case Integer i when check(-i) -> System.out.print("B");
                case Integer i -> System.out.print("C");
                default -> System.out.print("D");
            }
        }
        System.out.print(count);
    }
}
```

- **A.** `CDA2`
- **B.** `CDA4`
- **C.** `BDA3`
- **D.** `CDA6`
- **E.** `CDA3`

**Answer:** E

**Explanation:** Guards are evaluated lazily, top-down, and only when the type pattern in front of them matched.

- `1`: `check(1)` → count 1, false; `check(-1)` → count 2, false; unguarded `Integer i` → `C`.
- `"x"`: no `Integer` pattern matches, so no guard runs → `D`.
- `5`: `check(5)` → count 3, true → `A`; the remaining guards are not evaluated.

Output `CDA3`.

## gap-switch-014

What is the output?

```java
public class S14 {
    sealed interface Shape permits Circle, Square {}
    record Circle(double r) implements Shape {}
    record Square(double side) implements Shape {}
    static String describe(Shape s) {
        return switch (s) {
            case Circle c -> "circle";
            case Square q -> "square";
        };
    }
    public static void main(String[] args) {
        System.out.print("start ");
        try {
            System.out.print(describe(null));
        } catch (NullPointerException e) {
            System.out.print("NPE ");
        } catch (MatchException e) {
            System.out.print("ME ");
        }
        System.out.print("end");
    }
}
```

- **A.** `start null end`
- **B.** `start NPE end`
- **C.** `start ME end`
- **D.** `start ` followed by an uncaught exception
- **E.** The code does not compile because the switch does not handle `null`.

**Answer:** B

**Explanation:** The switch is exhaustive for the sealed interface (`Circle` and `Square` are the only permitted subtypes), so it compiles without `default` or `case null`. Exhaustiveness does not include `null`: a switch without `case null` throws `NullPointerException` when the selector is `null`. `MatchException` is used for other "impossible" situations (e.g. an accessor throwing during a record pattern match), not for `null`.

## gap-switch-015

Given:

```java
sealed interface Box<T> permits StrBox, AnyBox {}
final class StrBox implements Box<String> {}
final class AnyBox<T> implements Box<T> {}

static int size(Box<Integer> b) {
    return switch (b) {
        case AnyBox<Integer> a -> 1;
    };
}
```

Which two statements are true? (Choose two.)

- **A.** The method `size` does not compile because `StrBox` is not covered.
- **B.** The method `size` compiles; `size(new AnyBox<>())` returns `1`.
- **C.** If both occurrences of `Integer` are replaced by `String`, the method still compiles.
- **D.** If both occurrences of `Integer` are replaced by `String`, the method no longer compiles.
- **E.** The method compiles only if a `default` label is added, because generic sealed types are never exhaustive.

**Answer:** B, D

**Explanation:** For exhaustiveness the compiler only considers permitted subtypes that can actually be a `Box<Integer>`. `StrBox` implements `Box<String>`, so it can never be a `Box<Integer>`; covering `AnyBox<Integer>` is enough, and the method returns 1.

With `Box<String>` the situation changes: a `StrBox` *is* a `Box<String>`, so `case AnyBox<String> a` alone is not exhaustive and javac reports "the switch expression does not cover all possible input values".

## gap-switch-016

Which statements about pattern matching for `switch` in Java 25 are true? (Choose two.)

- **A.** Guards may be attached to constant labels, for example `case "a" when flag ->`.
- **B.** Without `case null`, a switch whose selector is `null` executes the `default` branch.
- **C.** A switch statement whose labels include a type pattern must be exhaustive.
- **D.** Pattern labels may appear in any order; the most specific matching label is chosen.
- **E.** `null` may be combined with `default` in a single label written `case null, default`.
- **F.** A switch on a sealed interface needs a `default` label to be exhaustive.

**Answer:** C, E

**Explanation:** C: any switch that uses patterns (or `case null`) is an enhanced switch and must be exhaustive, statement or expression. E: `case null, default ->` is the only legal way to combine `null` with another label.

A: guards are allowed only on pattern labels. B: without `case null`, a null selector always throws `NullPointerException`. D: labels are tested in order, and a label that can never be reached because of an earlier one is a compile-time dominance error. F: listing all permitted subtypes makes a switch on a sealed type exhaustive without `default`.
