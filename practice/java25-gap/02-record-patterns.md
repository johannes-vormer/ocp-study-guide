---
title: "Gap: Record patterns"
source: java25-gap
topic: d02-flow
---

# Gap: Record patterns (🆕 Java 21, JEP 440)

## gap-recpat-001

What is the output?

```java
public class R01 {
    record Point(int x, int y) {}
    record Line(Point a, Point b) {}
    public static void main(String[] args) {
        Object o = new Line(new Point(1, 2), new Point(3, 4));
        if (o instanceof Line(Point(var x1, var y1), Point p2) && x1 < p2.x()) {
            System.out.println(x1 + y1 + p2.y());
        }
    }
}
```

- **A.** `124`
- **B.** `7`
- **C.** `6`
- **D.** Nothing is printed.
- **E.** The code does not compile because `var` and explicit types cannot be mixed in one record pattern.
- **F.** The code does not compile because `x1` is not in scope in the `&&` condition.

**Answer:** B

**Explanation:** The nested record pattern deconstructs the `Line`: `x1 = 1`, `y1 = 2`, and `p2` is bound to the second `Point`. Mixing `var` components, explicitly typed components, type patterns and nested record patterns is allowed. Bindings introduced on the left of `&&` are in scope on its right, and `1 < 3` is true.

`x1 + y1 + p2.y()` is pure `int` arithmetic: `1 + 2 + 4 = 7`. There is no `String` operand, so no concatenation happens.

## gap-recpat-002

What is the output?

```java
public class R02 {
    record Point(int x, int y) {}
    record Line(Point a, Point b) {}
    public static void main(String[] args) {
        Object o = new Line(null, new Point(1, 1));
        System.out.print(o instanceof Line(Point a, Point b) ? "A" : "-");
        System.out.print(o instanceof Line(Point(var x, var y), var b) ? "B" : "-");
        System.out.print(o instanceof Line(var a, Point(int x, int y)) ? "C" : "-");
        System.out.print(o instanceof Line(Object a, Object b) ? "D" : "-");
    }
}
```

- **A.** `ABCD`
- **B.** `--C-`
- **C.** `-BC-`
- **D.** `A-C-`
- **E.** A `NullPointerException` is thrown.
- **F.** `A-CD`

**Answer:** F

**Explanation:** A record pattern itself never matches `null`, but a *nested type pattern* that is unconditional for the component's type does match a `null` component. `Point a` (A), `var a` (C) and `Object a` (D) are all unconditional for the component type `Point`, so the null first component is accepted.

In B, the first component must match the nested *record* pattern `Point(var x, var y)`, which cannot match `null` → no match. No `NullPointerException` occurs; the test simply fails.

## gap-recpat-003

Given:

```java
record Point(int x, int y) {}
Object o = new Point(1, 2);
```

Which statements compile? (Choose two.)

- **A.** `if (o instanceof Point(int x, int y) p) System.out.print(p);`
- **B.** `if (o instanceof Point(int x)) System.out.print(x);`
- **C.** `if (o instanceof Point(var x, var y)) System.out.print(x + y);`
- **D.** `if (o instanceof Point(long x, long y)) System.out.print(x + y);`
- **E.** `if (o instanceof Point(final int x, var y)) System.out.print(x + y);`
- **F.** `if (o instanceof Point(Integer x, var y)) System.out.print(x + y);`

**Answer:** C, E

**Explanation:** C is the canonical form. E is legal because a pattern variable may be declared `final`.

A: a record pattern cannot have its own binding name (this was possible only in early previews). B: the number of nested patterns must match the number of record components ("incorrect number of nested patterns"). D and F: for an `int` component, the only allowed patterns are `int x`, `var x`, or `_`; `long x` or `Integer x` would need *primitive patterns*, which are only a preview feature in Java 25 (JEP 507), so javac reports "primitive patterns are a preview feature and are disabled by default".

## gap-recpat-004

What is the output?

```java
public class R04 {
    sealed interface Animal permits Dog, Cat {}
    record Dog(String name) implements Animal {}
    record Cat(String name, int lives) implements Animal {}
    record Pair(Animal a, Animal b) {}

    static String m(Pair p) {
        return switch (p) {
            case Pair(Dog d, Animal a) -> "1";
            case Pair(Cat c, Dog d) -> "2";
            case Pair(Animal a, Cat c) -> "3";
        };
    }
    public static void main(String[] args) {
        var dog = new Dog("Rex");
        var cat = new Cat("Tom", 9);
        System.out.println(m(new Pair(cat, cat)) + m(new Pair(dog, cat)) + m(new Pair(cat, dog)));
    }
}
```

- **A.** `312`
- **B.** `332`
- **C.** `313`
- **D.** The code does not compile because the switch is not exhaustive.
- **E.** The code does not compile because `case Pair(Animal a, Cat c)` is dominated.

**Answer:** A

**Explanation:** Exhaustiveness: first component `Dog` → covered by case 1. First component `Cat` → second component `Dog` is covered by case 2 and `Cat` by case 3. So all combinations are covered without `default`. Case 3 is not dominated, because no earlier case covers `Pair(Cat, Cat)`.

Evaluation is top-down: `(cat, cat)` → only case 3 matches → `3`. `(dog, cat)` → case 1 matches first (even though case 3 would also match) → `1`. `(cat, dog)` → case 2 → `2`. Output `312`.

## gap-recpat-005

Given:

```java
record Box<T>(T value) {}

Box<String> b = new Box<>("hi");
Object o = b;
```

Which statements compile? (Choose three.)

- **A.** `if (o instanceof Box(var v)) System.out.print(v.length());`
- **B.** `if (b instanceof Box(var v)) System.out.print(v.length());`
- **C.** `if (o instanceof Box(String s)) System.out.print(s.length());`
- **D.** `if (o instanceof Box<String>(var v)) System.out.print(v.length());`
- **E.** `if (b instanceof Box<String>(Object v)) System.out.print(v);`

**Answer:** B, C, E

**Explanation:** In a record pattern without type arguments, the compiler *infers* them from the type being matched. For `b` (a `Box<String>`), `Box(var v)` is inferred as `Box<String>(var v)`, so `v` is a `String` (B compiles). From `Object`, the inferred type is effectively `Box<?>`, so `var v` has type `Object` and `v.length()` does not compile (A).

C compiles: the nested `String s` pattern is checked at run time against the component value. D does not compile: casting an `Object` to `Box<String>` is unchecked, which is not allowed in a pattern ("Object cannot be safely cast to Box<String>"). E compiles: an explicit type argument matches the static type, and `Object v` is an unconditional pattern for a `String` component.

## gap-recpat-006

What is the output?

```java
public class R06 {
    record Item(String name, double price, int qty) {}
    static String label(Object o) {
        return switch (o) {
            case Item(var n, var p, var q) when q == 0 -> n + " sold out";
            case Item(String n, double p, int q) when p * q > 100 -> n + " bulk";
            case Item(var n, var p, var q) -> n + " x" + q;
            default -> "?";
        };
    }
    public static void main(String[] args) {
        System.out.println(label(new Item("pen", 2.5, 20)) + ", "
                + label(new Item("ink", 40.0, 3)) + ", "
                + label(new Item("cap", 999.0, 0)));
    }
}
```

- **A.** `pen x20, ink x3, cap bulk`
- **B.** `pen x20, ink bulk, cap bulk`
- **C.** `pen bulk, ink bulk, cap sold out`
- **D.** `pen x20, ink bulk, cap sold out`
- **E.** The code does not compile because the third case dominates the `default` label.

**Answer:** D

**Explanation:** `pen`: `qty` is 20, and `2.5 * 20 = 50.0`, not over 100 → third case → `pen x20`. `ink`: `40.0 * 3 = 120.0` → `ink bulk`. `cap`: the first guard (`q == 0`) is checked first and is true → `cap sold out`; the bulk case is never reached.

`default` is required because the selector is `Object`, and `Item(var n, var p, var q)` covers only `Item`s — it does not dominate `default`.

## gap-recpat-007

What is the output?

```java
public class R07 {
    record Account(int balance) {
        public int balance() {
            if (balance < 0) throw new IllegalStateException("overdrawn");
            return balance;
        }
    }
    public static void main(String[] args) {
        Object o = new Account(-5);
        try {
            if (o instanceof Account a) System.out.print("A ");
            if (o instanceof Account(int b)) System.out.print("B ");
        } catch (IllegalStateException e) {
            System.out.print("ISE ");
        } catch (RuntimeException e) {
            System.out.print(e.getClass().getSimpleName() + " ");
        }
        System.out.print("done");
    }
}
```

- **A.** `A B done`
- **B.** `A ISE done`
- **C.** `A MatchException done`
- **D.** `ISE done`
- **E.** `A done`

**Answer:** C

**Explanation:** The type pattern `Account a` only checks the type; no accessor is called, so `A` is printed. The record pattern `Account(int b)` deconstructs the record by calling the accessor `balance()`, which throws `IllegalStateException`.

An exception thrown by an accessor during pattern matching is wrapped in a `java.lang.MatchException` (new in Java 21, a `RuntimeException`), with the original exception as its cause. The `IllegalStateException` handler therefore does not catch it; the `RuntimeException` handler prints `MatchException`.

## gap-recpat-008

What is the output?

```java
public class R08 {
    record Temp(double c) {
        Temp {
            if (c < -273.15) c = -273.15;
        }
        public double c() { return Math.round(c); }
    }
    public static void main(String[] args) {
        Object o = new Temp(-300.0);
        if (o instanceof Temp(var c) && c < 0) System.out.println(c);
        else System.out.println("none");
    }
}
```

- **A.** `-300.0`
- **B.** `-273.15`
- **C.** `-273`
- **D.** `none`
- **E.** `-273.0`

**Answer:** E

**Explanation:** The compact constructor replaces the parameter value with `-273.15` before the field is assigned. A record pattern obtains its component values by calling the **accessor methods**, not by reading fields, so `c` is the result of the overridden `c()`: `Math.round(-273.15)` = `-273L`, which is converted to the return type `double` → `-273.0`. Since `var c` has the component type `double`, printing gives `-273.0`.

## gap-recpat-009

Given:

```java
record Point(int x, int y) {}
static void m(Object o) {
    // INSERT
}
```

Which code fragments compile when inserted at `// INSERT`? (Choose two.)

- **A.** `if (o instanceof Point(var x, var y) || x > y) System.out.print(x);`
- **B.** `if (o instanceof Point(var x, var y) && x > y) System.out.print(x);`
- **C.** `if (o instanceof Point(var x, var x)) System.out.print(x);`
- **D.** `if (!(o instanceof Point(var x, var y))) return;`  
  `System.out.print(x + y);`
- **E.** `boolean ok = o instanceof Point(var x, var y) ? x > 0 : y > 0;`

**Answer:** B, D

**Explanation:** Record pattern bindings follow the same flow-scoping rules as `instanceof` type patterns. B: bindings are definitely matched on the right of `&&`. D: if the negated test is true the method returns, so after the `if` the pattern must have matched and `x`, `y` are in scope.

A: on the right of `||` the pattern did *not* match → "cannot find symbol". E: in the false branch of `?:` the pattern did not match, so `y` is not in scope. C: the same name cannot be bound twice in one pattern ("illegal attempt to redefine an existing match binding").

## gap-recpat-010

What is the output?

```java
public class R10 {
    sealed interface Result<T> permits Ok, Err {}
    record Ok<T>(T value) implements Result<T> {}
    record Err<T>(String msg) implements Result<T> {}

    static int value(Result<Integer> r) {
        return switch (r) {
            case Ok(var v) -> v + 1;
            case Err(var m) -> m.length();
        };
    }
    public static void main(String[] args) {
        System.out.println(value(new Ok<>(41)) + value(new Err<>("bad")));
    }
}
```

- **A.** `423`
- **B.** `45`
- **C.** `44`
- **D.** The code does not compile because `v + 1` is applied to an `Object`.
- **E.** The code does not compile because raw types cannot be used in record patterns.

**Answer:** B

**Explanation:** `case Ok(var v)` does not use a raw type: the type argument of the record pattern is inferred from the selector type `Result<Integer>`, giving `Ok<Integer>(var v)`, so `v` is an `Integer` and `v + 1` compiles (unboxing). The switch is exhaustive because `Ok` and `Err` are the only permitted subtypes.

`value(new Ok<>(41))` = 42 and `value(new Err<>("bad"))` = 3; both are `int`s, so the sum `45` is printed.

## gap-recpat-011

What is the result?

```java
import java.util.List;
public class R11 {
    record Point(int x, int y) {}
    public static void main(String[] args) {
        List<Point> pts = List.of(new Point(1, 2), new Point(3, 4));
        int sum = 0;
        for (Point(var x, var y) : pts) {
            sum += x * y;
        }
        System.out.println(sum);
    }
}
```

- **A.** `14`
- **B.** `10`
- **C.** `0`
- **D.** The code does not compile.
- **E.** A `MatchException` is thrown.

**Answer:** D

**Explanation:** Record patterns in the header of an enhanced `for` loop were tried as a preview in Java 20 but were **dropped** before record patterns became final in Java 21. In Java 25, record patterns can be used only with `instanceof` and in `switch` case labels; this `for` header is a syntax error. Use `for (Point p : pts)` and deconstruct inside the loop, e.g. `if (p instanceof Point(var x, var y)) …`.

## gap-recpat-012

Given:

```java
sealed interface Animal permits Dog, Cat {}
record Dog(String name) implements Animal {}
record Cat(String name) implements Animal {}
record Pair(Animal a, Animal b) {}

static int m(Pair p) {
    return switch (p) {
        case Pair(Dog d, Dog e) -> 1;
        case Pair(Cat c, Cat d) -> 2;
        case Pair(Dog d, Cat c) -> 3;
        // INSERT
    };
}
```

Which case labels, inserted at `// INSERT`, make the method compile? (Choose three.)

- **A.** `case Pair(Dog d, Animal a) -> 4;`
- **B.** `case Pair(Cat c, Dog d) -> 4;`
- **C.** `case Pair(Animal a, Dog d) -> 4;`
- **D.** `case Pair(Cat c, Cat d) -> 4;`
- **E.** `case Pair q -> 4;`

**Answer:** B, C, E

**Explanation:** The three existing cases cover `(Dog, Dog)`, `(Cat, Cat)` and `(Dog, Cat)`; only `(Cat, Dog)` is missing. B covers it exactly, C covers it (and `(Dog, Dog)`, but that is not a problem because C is not *dominated* by any earlier single case), and E is an unconditional type pattern that covers everything.

A still leaves `(Cat, Dog)` uncovered → "the switch expression does not cover all possible input values". D repeats case 2 and is dominated by it → compile error.
