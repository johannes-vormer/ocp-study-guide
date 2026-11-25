---
title: "Gap: Flexible constructor bodies"
source: java25-gap
topic: d03-oop
---

# Gap: Flexible constructor bodies (🆕 Java 25, JEP 513)

## gap-flexctor-001

What is the output?

```java
public class F01 {
    static class Base {
        Base() { show(); }
        void show() { System.out.print("base "); }
    }
    static class Child extends Base {
        private final String name;
        Child(String n) {
            name = n.toUpperCase();
            super();
            System.out.print("child ");
        }
        @Override void show() { System.out.print(name + " "); }
    }
    public static void main(String[] args) { new Child("x"); }
}
```

- **A.** `base child`
- **B.** `null child`
- **C.** `x child`
- **D.** `X child`
- **E.** The code does not compile because `super()` must be the first statement of the constructor.
- **F.** The code does not compile because a `final` field cannot be assigned before `super()`.

**Answer:** D

**Explanation:** Since Java 25, statements may precede an explicit `super(...)` or `this(...)` call. These statements form the constructor's **prologue**. In the prologue you may assign a field of the class being constructed, provided that field has no initializer — `name` qualifies.

The assignment runs *before* the superclass constructor. `Base()` calls the overridden `show()`, which already sees `name == "X"`. With the classic Java 17 layout (`super()` first, then assignment) this program would have printed `null child`.

## gap-flexctor-002

Given:

```java
static class Shape { Shape(int size) { } }
static class Square extends Shape {
    int side;
    int area = 0;
    Square(int side) {
        // INSERT
        super(side);
    }
    void log(String s) { }
    static int helper(int s) { return s; }
}
```

Which statements, each inserted on its own at `// INSERT`, allow the code to compile? (Choose three.)

- **A.** `area = side * side;`
- **B.** `if (side < 0) throw new IllegalArgumentException();`
- **C.** `log("init");`
- **D.** `this.side = side;`
- **E.** `return;`
- **F.** `helper(side);`
- **G.** `super.toString();`

**Answer:** B, D, F

**Explanation:** The prologue is an *early construction context*: the object under construction may not be used yet. Allowed: argument validation and `throw` (B), assigning a field that has **no initializer** (D — `side` has none), and calling **static** methods (F).

Not allowed: assigning a field that *has* an initializer (A: "cannot assign initialized field 'area' before supertype constructor has been called"), calling an instance method (C), `return` in the prologue (E: "'return' not allowed before explicit constructor invocation"), and any use of `super.` (G).

## gap-flexctor-003

Given:

```java
static class P { P(int v) { System.out.print(v); } }
static class C extends P {
    // constructor here
}
```

Which constructors compile when placed in class `C`? (Choose three.)

- **A.** `C(int x) { if (x > 0) super(1); else super(2); }`
- **B.** `C(int x) { super(x > 0 ? 1 : 2); }`
- **C.** `C(int x) { try { super(x); } catch (Exception e) { } }`
- **D.** `C(int x) { int v = Math.abs(x) * 2; super(v); }`
- **E.** `C(int x) { super(x); super(x); }`
- **F.** `C(int x) { for (int i = 0; i < x; i++) System.out.print(i); super(x); }`

**Answer:** B, D, F

**Explanation:** Java 25 allows statements *before* the constructor invocation, including local variables (D) and loops (F), but the invocation itself is still special: there may be at most one, and it must be a top-level statement of the constructor body.

A places it inside `if`/`else` and C inside `try` → "explicit constructor invocation not allowed here". E has two invocations → "redundant explicit constructor invocation". B was already legal before Java 25 — the condition is part of the argument expression.

## gap-flexctor-004

What is the output?

```java
public class F05 {
    record Range(int lo, int hi) {
        Range {
            if (lo > hi) throw new IllegalArgumentException("lo > hi");
        }
        Range(int hi) {
            if (hi < 0) hi = 0;
            this(0, hi);
        }
    }
    public static void main(String[] args) {
        System.out.print(new Range(-5) + " ");
        try {
            System.out.print(new Range(3, 1));
        } catch (IllegalArgumentException e) {
            System.out.print(e.getMessage());
        }
    }
}
```

- **A.** `Range[lo=0, hi=-5] lo > hi`
- **B.** `Range[lo=0, hi=0] lo > hi`
- **C.** `Range[lo=0, hi=0] Range[lo=3, hi=1]`
- **D.** An `IllegalArgumentException` is thrown by `new Range(-5)`.
- **E.** The code does not compile because a non-canonical record constructor must start with `this(...)`.

**Answer:** B

**Explanation:** A non-canonical record constructor must still delegate with `this(...)`, but since Java 25 it may have a prologue before that call. Here the prologue reassigns the *parameter* `hi` from `-5` to `0`, then calls the canonical constructor with `(0, 0)`, which passes the check → `Range[lo=0, hi=0]`.

`new Range(3, 1)` goes straight to the compact canonical constructor, which throws with message `lo > hi`.

## gap-flexctor-005

What is the result of compiling this record?

```java
record Range(int lo, int hi) {
    Range {
        if (lo > hi) { int t = lo; lo = hi; hi = t; }
        this(lo, hi);
    }
}
```

- **A.** It compiles; the components are swapped when `lo > hi`.
- **B.** It compiles, but every instantiation throws `StackOverflowError`.
- **C.** It does not compile because a canonical constructor may not contain an explicit constructor invocation.
- **D.** It does not compile because the parameters of a compact constructor cannot be reassigned.
- **E.** It does not compile because a local variable cannot be declared before `this(...)`.

**Answer:** C

**Explanation:** Flexible constructor bodies did not change this record rule: the **canonical** constructor (compact or not) must never call `this(...)` or `super(...)` — it is the constructor that actually assigns the fields. javac reports "invalid canonical constructor in record Range (canonical constructor must not contain explicit constructor invocation)" (plus a recursive-invocation error).

Without the `this(lo, hi);` line the record compiles: reassigning the parameters in a compact constructor is allowed and is exactly how you normalize values (D is wrong).

## gap-flexctor-006

What is the output?

```java
public class F06 {
    static int print(String s) { System.out.print(s); return 1; }
    static class A {
        A() { System.out.print("A "); }
    }
    static class B extends A {
        { System.out.print("init "); }
        int x = print("field ");
        B() {
            System.out.print("pro ");
            super();
            System.out.print("epi ");
        }
    }
    public static void main(String[] args) { new B(); }
}
```

- **A.** `A init field pro epi`
- **B.** `pro A init field epi`
- **C.** `init field pro A epi`
- **D.** `pro init field A epi`
- **E.** `A pro init field epi`

**Answer:** B

**Explanation:** Order of execution for `new B()`:

1. the **prologue** of `B()` — `pro`
2. the superclass constructor `A()` — `A`
3. `B`'s instance initializers and field initializers, in textual order — `init`, then `field`
4. the **epilogue** of `B()` — `epi`

Instance initializers still run right after the superclass constructor returns, so they run *after* the prologue.

## gap-flexctor-007

Given:

```java
public class F07 {
    int count = 0;
    class Inner {
        int value;
        Inner() {
            // INSERT
            super();
        }
    }
}
```

Which statements, each inserted on its own at `// INSERT`, compile? (Choose three.)

- **A.** `F07.this.count++;`
- **B.** `System.out.print(this.hashCode());`
- **C.** `count++;`
- **D.** `Runnable r = () -> value = 5;`
- **E.** `System.out.print(hashCode());`
- **F.** `value = 7;`

**Answer:** A, C, F

**Explanation:** The early construction context restricts only the object *being constructed* (the `Inner` instance). The enclosing `F07` instance already exists, so its fields can be used: explicitly via `F07.this.count` (A) or by the simple name `count`, which refers to the outer field (C). F assigns `Inner`'s own field, which has no initializer — allowed.

B and E use the `Inner` instance (`this.hashCode()` / implicit `this.hashCode()`) → "cannot reference … before supertype constructor has been called". D: assigning a field of the instance under construction is allowed only directly in the prologue, not inside a lambda or inner class.

## gap-flexctor-008

Given:

```java
static class Temp {
    final double c;
    int calls;
    Temp(double c) { this.c = c; }
    Temp(String s) {
        // INSERT
        this(Double.parseDouble(s.strip()));
    }
}
```

Which statements, each inserted on its own at `// INSERT`, compile? (Choose three.)

- **A.** `if (s == null) throw new NullPointerException("s");`
- **B.** `c = 1.0;`
- **C.** `this.calls = 2;`
- **D.** `calls++;`
- **E.** `var t = s.strip();`
- **F.** `System.out.print(c);`

**Answer:** A, C, E

**Explanation:** A prologue may precede `this(...)` just like `super(...)`. A and E only use the parameter. C assigns a field without initializer, which is allowed in the prologue.

B: the delegated constructor `Temp(double)` assigns the `final` field `c`, so assigning it here too gives "variable c might already have been assigned". D (`calls++` reads the field) and F (reads `c`) both *read* instance fields, which is never allowed in the prologue.

## gap-flexctor-009

What is the output?

```java
public class F10 {
    enum Level {
        LOW(1), HIGH("9");
        final int v;
        Level(int v) { this.v = v; }
        Level(String s) {
            int parsed = Integer.parseInt(s);
            this(parsed * 10);
        }
    }
    public static void main(String[] args) { System.out.print(Level.LOW.v + Level.HIGH.v); }
}
```

- **A.** `19`
- **B.** `91`
- **C.** `190`
- **D.** `10`
- **E.** The code does not compile because enum constructors cannot call other constructors.
- **F.** The code does not compile because statements cannot precede `this(...)`.

**Answer:** B

**Explanation:** Enum constructors may delegate with `this(...)` (that was always allowed), and since Java 25 they may also have a prologue. `HIGH("9")` parses `9`, then calls `Level(90)`. `LOW.v + HIGH.v` is `int` addition: `1 + 90 = 91`.

## gap-flexctor-010

Which statements about flexible constructor bodies in Java 25 are true? (Choose two.)

- **A.** In the prologue, a field of the class being constructed may be read once it has been assigned.
- **B.** The prologue may contain a `throw` statement but not a `return` statement.
- **C.** Instance initializer blocks run before the prologue.
- **D.** In the prologue, a field without an initializer may be assigned so that the superclass constructor already sees its value through an overridden method.
- **E.** The explicit constructor invocation may now appear inside an `if` statement.
- **F.** A record's compact canonical constructor may now call `this(...)` at its end.

**Answer:** B, D

**Explanation:** B: `throw` is fine (validation is the main use case); `return` is forbidden in the prologue (it is allowed in the epilogue). D: that is exactly the "safe initialization" benefit of JEP 513.

A: fields may only be *assigned*, never read, in the prologue. C: initializers run after the superclass constructor, i.e. after the prologue. E: the invocation must still be a top-level statement of the constructor body. F: canonical record constructors can never contain `this(...)` or `super(...)`.
