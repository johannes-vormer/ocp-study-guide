---
title: "Gap: Unnamed variables and patterns"
source: java25-gap
topic: d03-oop
---

# Gap: Unnamed variables and patterns (🆕 Java 22, JEP 456)

## gap-unnamed-001

Which statements compile when placed inside the body of `main`? (Choose three.)

- **A.** `int _;`
- **B.** `int _ = 5;`
- **C.** `int _ = 1; System.out.println(_);`
- **D.** `String _ = "a", _ = "b";`
- **E.** `for (int i = 0, _ = 10; i < 3; i++) { }`

**Answer:** B, D, E

**Explanation:** Since Java 22, `_` declares an *unnamed* local variable. An unnamed local variable must have an initializer (A fails with "= expected"), and it can never be read: `System.out.println(_)` fails with "underscore not allowed here" (C).

Several unnamed variables may be declared in the same scope, even in a single declaration (D), and `_` is allowed in the initialization part of a basic `for` loop (E).

## gap-unnamed-002

Which member declarations compile inside a class body? (Choose two.)

- **A.** `private int _ = 1;`
- **B.** `void m(int _) { }`
- **C.** `void m() { try { } catch (RuntimeException _) { } }`
- **D.** `java.util.function.Function<String, Integer> f = _ -> 42;`
- **E.** `record R(int _) { }`
- **F.** `static int _() { return 1; }`

**Answer:** C, D

**Explanation:** `_` is allowed only where a variable is declared but would never be used *by name*: local variables, exception parameters (C), lambda parameters (D), resources in try-with-resources, loop variables and pattern variables.

It is not allowed for fields (A), method or constructor parameters (B), record components (E), or as a method name (F) — all report "underscore not allowed here". Method parameters are part of an API, which is why they were excluded; lambda parameters are not.

## gap-unnamed-003

What is the output?

```java
import java.util.*;
import java.util.function.*;
public class U03 {
    public static void main(String[] args) {
        List<String> list = new ArrayList<>(List.of("a", "b", "c", "d"));
        int count = 0;
        for (var _ : list) count++;
        var _ = list.remove(0);
        BiFunction<Integer, Integer, Integer> f = (x, _) -> x * 2;
        System.out.println(count + list.size() + f.apply(5, 100));
    }
}
```

- **A.** `18`
- **B.** `43200`
- **C.** `17`
- **D.** `110`
- **E.** The code does not compile because `_` is declared twice in `main`.
- **F.** The code does not compile because `var` cannot be combined with `_`.

**Answer:** C

**Explanation:** The enhanced `for` loop with an unnamed variable still iterates over all four elements → `count = 4`. `var _ = list.remove(0);` still *executes* the initializer: the element is removed and only the result is discarded → `size()` is 3. The lambda ignores its second argument → `f.apply(5, 100)` = 10.

All three operands are `int`s, so the sum `4 + 3 + 10 = 17` is printed. Unnamed variables never clash with each other, so several `_` declarations in the same scope (or nested scopes) are fine.

## gap-unnamed-004

What is the output?

```java
public class U04 {
    sealed interface Vehicle permits Car, Bike, Truck {}
    record Car(int seats) implements Vehicle {}
    record Bike() implements Vehicle {}
    record Truck(int load) implements Vehicle {}

    static String m(Vehicle v) {
        return switch (v) {
            case Car _, Bike _ -> "light ";
            case Truck(int load) when load > 10 -> "heavy ";
            case Truck _ -> "truck ";
        };
    }
    public static void main(String[] args) {
        System.out.println(m(new Bike()) + m(new Truck(5)) + m(new Truck(20)) + m(new Car(4)));
    }
}
```

- **A.** `light heavy heavy light`
- **B.** `light truck heavy light`
- **C.** `light truck truck light`
- **D.** The code does not compile because a case label may contain only one pattern.
- **E.** The code does not compile because the switch is not exhaustive.

**Answer:** B

**Explanation:** Since Java 22, a case label may list several patterns, as long as none of them declares a named binding — here `Car _` and `Bike _` are unnamed type patterns. The switch is exhaustive: `Car`, `Bike` and `Truck` are all covered (the guarded `Truck` case does not count toward exhaustiveness, but `case Truck _` does).

`Bike` → `light`; `Truck(5)` fails the guard → `truck`; `Truck(20)` → `heavy`; `Car` → `light`.

## gap-unnamed-005

Given:

```java
record Point(int x, int y) {}
static void m(Object o) {
    switch (o) {
        // INSERT
        default -> System.out.print("D");
    }
}
```

Which case labels compile when inserted at `// INSERT`? (Choose four.)

- **A.** `case Integer i, String s -> System.out.print("A");`
- **B.** `case Integer _, String _ -> System.out.print("B");`
- **C.** `case _ -> System.out.print("C");`
- **D.** `case Point(var x, _) -> System.out.print(x);`
- **E.** `case Point(_, _) -> System.out.print("E");`
- **F.** `case Integer _, Long _ when o.hashCode() > 0 -> System.out.print("F");`
- **G.** `case Point(int x, _), Integer _ -> System.out.print("G");`

**Answer:** B, D, E, F

**Explanation:** B: multiple patterns in one label are fine when they are all unnamed. D and E: the unnamed pattern `_` may stand for any component inside a record pattern (it matches anything, including `null`). F: a label with multiple patterns may have a guard, as long as the guard does not need a binding from those patterns.

A and G: if a label has more than one pattern, none of them may declare a named binding (`i`, `s`, `x`) — javac reports this as "illegal fall-through from a pattern". C: the unnamed pattern cannot be used as a top-level pattern; use `default` (or a type pattern such as `Object _`) instead.

## gap-unnamed-006

What is the output?

```java
public class U06 {
    static class Res implements AutoCloseable {
        private final String name;
        Res(String name) { this.name = name; System.out.print("open-" + name + " "); }
        public void close() { System.out.print("close-" + name + " "); }
    }
    public static void main(String[] args) {
        try (var _ = new Res("a"); var _ = new Res("b")) {
            System.out.print("body ");
            throw new IllegalStateException();
        } catch (IllegalStateException _) {
            System.out.print("caught ");
        }
    }
}
```

- **A.** `open-a open-b body caught close-b close-a`
- **B.** `open-a open-b body close-a close-b caught`
- **C.** `open-a open-b body caught`
- **D.** `open-a open-b body close-b close-a caught`
- **E.** The code does not compile because a resource must have a name so it can be closed.

**Answer:** D

**Explanation:** Unnamed variables are allowed as try-with-resources resources and as catch parameters. The resources are still closed automatically — the name is not needed for that. The usual rules apply: resources are opened in order, closed in **reverse** order, and closed **before** the catch block runs. Output: `open-a open-b body close-b close-a caught`.

## gap-unnamed-007

What is the output?

```java
public class U07 {
    public static void main(String[] args) {
        String __ = "x";
        int _ = 1;
        int $ = 2;
        System.out.print(__ + $);
    }
}
```

- **A.** `x2`
- **B.** `x12`
- **C.** `3`
- **D.** The code does not compile because `__` is not a valid identifier.
- **E.** The code does not compile because `$` is not a valid identifier.

**Answer:** A

**Explanation:** Only the single underscore `_` is special (a keyword since Java 9, used for unnamed variables since Java 22). A double underscore `__` is an ordinary identifier, and so is `$`. The unnamed `int _ = 1` is simply ignored. `"x" + 2` → `x2`.

## gap-unnamed-008

Which lambda declarations compile? (Choose three.)

- **A.** `BiFunction<Integer, Integer, Integer> f = (Integer _, Integer y) -> y;`
- **B.** `BiFunction<Integer, Integer, Integer> f = (int _, var y) -> y;`
- **C.** `Function<String, Integer> f = _ -> _.length();`
- **D.** `BiFunction<Integer, Integer, Integer> f = (_, _) -> 0;`
- **E.** `Function<String, Integer> f = _ -> 1;`

**Answer:** A, D, E

**Explanation:** Lambda parameters may be unnamed, with explicit types (A), implicit types (D, E) or `var`. Two unnamed parameters in the same lambda are fine (D).

B mixes an explicitly typed parameter with a `var` parameter, which is never allowed ("invalid lambda parameter declaration"); besides, `int` does not match `Integer` in the functional interface. C tries to *use* `_` as a value → "underscore not allowed here".

## gap-unnamed-009

Which statements about unnamed variables and patterns in Java 25 are true? (Choose two.)

- **A.** An unnamed variable can be read with `_`, but not assigned.
- **B.** Several unnamed variables may be declared in the same scope.
- **C.** A method parameter may be declared as `_` if the method body does not use it.
- **D.** The unnamed pattern `_` may appear as a nested pattern inside a record pattern.
- **E.** `case _ ->` can be used instead of `default` in a switch on `Object`.
- **F.** The initializer of an unnamed local variable is not evaluated.

**Answer:** B, D

**Explanation:** B: unnamed variables don't introduce a name, so they can never clash. D: `Point(var x, _)` and `Point(_, _)` are legal nested uses.

A: `_` can never be used as an expression. C: method and constructor parameters cannot be unnamed (lambda parameters can). E: the unnamed pattern is only allowed nested inside a record pattern; a top-level `case _` does not compile. F: the initializer is evaluated normally (side effects happen); only the value is discarded.

## gap-unnamed-010

How many lines fail to compile?

```java
void process(List<String> data) {
    int _ = data.size();            // line 1
    for (String _ : data) { }       // line 2
    try { } catch (Exception _) { } // line 3
    var _;                          // line 4
    data.forEach(_ -> { });         // line 5
    String s = _;                   // line 6
}
```

- **A.** Zero
- **B.** One
- **C.** Two
- **D.** Three
- **E.** Four

**Answer:** C

**Explanation:** Lines 1, 2, 3 and 5 are valid uses of unnamed variables: a local variable with initializer, an enhanced-for variable, a catch parameter (catching `Exception` with an empty try is allowed, since `Exception` includes unchecked exceptions) and a lambda parameter.

Line 4 fails because an unnamed local variable needs an initializer (and `var` needs one anyway). Line 6 fails because `_` cannot be used as an expression. Two lines fail.
