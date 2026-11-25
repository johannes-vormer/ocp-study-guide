---
title: "D3 — Object-Oriented Principles"
topic: d03-oop
order: 3
---

# D3 — Object-Oriented Principles

The biggest domain on the exam (roughly 18% of the questions; 230 practice questions in the Java 17 book). Almost every question here is a "does it compile, and if so what does it print?" puzzle. Learn the rules as *compiler rules*: most wrong answers are lines that look fine but do not compile.

All code below was compiled and run on JDK 25.

## What the exam tests

- Declaring classes, records, enums and interfaces; which modifiers go where.
- Access modifiers, especially `protected` across packages.
- Object creation: initialization order of static/instance initializers and constructors, `this()`/`super()` chaining, and **🆕 Java 25 flexible constructor bodies**.
- Object life cycle and garbage-collection eligibility.
- Nested classes (static nested, inner, local, anonymous) and what each may access.
- Overloading resolution, varargs, `var`, scope, effectively final, **🆕 Java 22 unnamed variables `_`**.
- Encapsulation and immutable classes.
- Inheritance, overriding vs hiding, abstract, `final`, `sealed`/`non-sealed`/`permits`.
- Records: canonical/compact constructors, accessors, what is forbidden.
- `Object` methods: `equals`/`hashCode`/`toString` contracts.
- Polymorphism: reference type vs object type; casts that fail at compile time vs `ClassCastException`.
- Interfaces: abstract/default/static/private methods, constants, default-method conflicts.
- Functional interfaces and `@FunctionalInterface`.
- Enums with fields, constructors, methods and constant-specific bodies.

---

## 1. Class structure and access modifiers

Order of elements in a file: `package` → `import`s → type declarations. At most **one public top-level type per file**, and its name must match the file name. Members (fields, methods, constructors, initializers, nested types) can appear in any order inside a class.

| Modifier | Same class | Same package | Subclass in other package | Everyone |
|---|---|---|---|---|
| `private` | yes | no | no | no |
| (package / default) | yes | yes | **no** | no |
| `protected` | yes | yes | yes, *through inheritance only* | no |
| `public` | yes | yes | yes | yes |

Top-level classes may only be `public` or package-private (no `private`/`protected`). Nested types may use all four.

### The `protected` trap

In a subclass in a *different* package, a protected member is accessible only through a reference whose type is that subclass (or a subclass of it) — i.e. via inheritance — not via a reference of the parent type.

```java
package zoo;
public class Animal { protected void eat() {} String pkg = "p"; protected Animal() {} }

package park;
import zoo.Animal;
public class Lion extends Animal {
    void ok()             { eat(); this.eat(); new Lion().eat(); }   // OK
    void bad(Animal a)    { a.eat(); }        // DOES NOT COMPILE: eat() has protected access
}
class Lion2 extends Animal { void x() { System.out.println(pkg); } }  // DOES NOT COMPILE: package access
class Lion3 { void x() { new Animal(); } }          // DOES NOT COMPILE: protected constructor
class Lion4 { void x() { new Animal() {}; } }       // OK: anonymous subclass calls super()
```

Static protected members (`protected static int count`) *are* accessible from a subclass in another package via the class name too.

### Static vs instance

- A static method cannot use `this`, instance fields or instance methods without an object: `inst();` inside `main` → *non-static method cannot be referenced from a static context*.
- A static method can be called through a reference, even a `null` one: `Fin f = null; f.hi();` runs fine if `hi()` is static (the compiler uses the reference *type*).
- Java is **pass-by-value**. A method can mutate the object a parameter refers to, but reassigning the parameter has no effect on the caller.

```java
static void change(StringBuilder sb, int n) { sb.append("!"); n++; sb = new StringBuilder("new"); }
// caller: sb = "x", n = 1  →  after call: sb = "x!", n = 1
```

### Methods that look like constructors

`void Foo() {}` inside class `Foo` is a **method**, not a constructor (it has a return type). It compiles and is never called by `new Foo()`.

---

## 2. Object creation and initialization order

### Order (verified)

1. **Class initialization** (once, when the class is first used): superclass first, then subclass. Static variable initializers and `static {}` blocks run in textual order.
2. **Instance creation**, for each class from `Object` down:
   1. Constructor arguments evaluated; constructor called.
   2. (🆕 Java 25) the constructor's **prologue** (statements before `super(...)`/`this(...)`) runs.
   3. `super(...)` is processed (recursively, all the way to `Object`).
   4. Instance variable initializers and instance `{}` blocks of *this* class, in textual order.
   5. The rest of the constructor body (the **epilogue**).
   - If the constructor starts with `this(...)`, step 4 is **not** run for that constructor; it runs in the constructor that (explicitly or implicitly) calls `super(...)`.

```java
class P { static { System.out.print("Ps "); } { System.out.print("Pi "); } P() { System.out.print("Pc "); } }
class C extends P {
    static { System.out.print("Cs "); }
    int x = f();
    { System.out.print("Ci "); }
    C() { this(1); System.out.print("Cc "); }
    C(int a) { System.out.print("Cc1 "); }
    int f() { System.out.print("Cf "); return 1; }
}
public class Init {
    static { System.out.print("Main "); }
    public static void main(String[] a) { new C(); System.out.println(); new C(); }
}
```

Output:
```
Main Ps Cs Pi Pc Cf Ci Cc1 Cc
Pi Pc Cf Ci Cc1 Cc
```
Static blocks run once only; the class containing `main` is initialized before `main` runs.

### Constructor rules

- If you write **no** constructor, the compiler adds a no-arg default constructor with the same access as the class, containing `super();`.
- If a constructor has no explicit `this(...)`/`super(...)`, the compiler inserts `super();` → compile error if the parent has no accessible no-arg constructor:
  ```java
  class A { A(int x) {} }
  class B extends A { }   // DOES NOT COMPILE: constructor A cannot be applied (no-arg needed)
  ```
- At most one explicit constructor call per constructor (`super(); super();` → *redundant explicit constructor invocation*).
- Constructors cannot call each other in a cycle (`this(1)` ↔ `this()` → *recursive constructor invocation*).
- `super(...)`/`this(...)` cannot be inside `try`, `if`, a loop, etc. — it must be a top-level statement of the constructor body.

### Final fields

- `final` instance field: must be definitely assigned exactly once — at declaration, in an instance initializer, or **in every constructor** (a constructor that delegates with `this(...)` counts through the delegate).
- `static final` field: at declaration or in a `static {}` block. Assigning it in a constructor → *cannot assign a value to static final variable*.
- `final` local variable: may be declared without a value and assigned later once. A non-final local used before assignment → *might not have been initialized* (locals have **no default value**; fields do).
- `final` on a reference does not make the object immutable: `final StringBuilder sb = ...; sb.append("x");` is fine.

### Forward references

```java
int a = b + 1; int b = 2;       // DOES NOT COMPILE: illegal forward reference
int a = this.b + 1; int b = 2;  // compiles; a == 1 (b still 0)
static { b = 5; } static int b = 3;  // compiles (assignment, not read); b ends up 3
```

### Overridable calls during construction

A constructor calling an overridden method runs the **subclass** version before the subclass's field initializers have run, so it sees default values (`0`, `null`). Java 25 offers a fix: assign the field in the prologue (next section).

---

## 3. 🆕 Java 25 — Flexible constructor bodies (JEP 513)

Statements may now appear **before** the explicit `super(...)` or `this(...)` call. The JLS calls them the **prologue**; statements after the call are the **epilogue**. The prologue (and the arguments of the `super`/`this` call) form an *early construction context*: the object exists but is not yet initialized, so you may not *use* it.

| In the prologue you **may** | In the prologue you **may not** |
|---|---|
| declare and use local variables, use parameters | read any instance field (`x`, `this.x`) |
| call **static** methods, use static fields | call instance methods (`m()`, `this.m()`) |
| throw exceptions (validation!) | use `this` as a value (`println(this)`) |
| **assign** an instance field of this class that has **no initializer**: `x = v;` or `this.x = v;` | use `super.field` / `super.m()` |
| declare local classes that don't touch `this` | create an inner-class instance (`new Inner()` needs `this`) |
| | `return` |
| | assign a field that has an initializer (`int x = 1;`) |
| | assign an **inherited** field (it belongs to the superclass) |
| | compound assignment `x += 1` (reads `x`) |
| | assign a field inside a lambda or inner class |

```java
class Super {
    Super() { show(); }                         // calls the overriding method!
    void show() { System.out.println("super"); }
}
class Sub extends Super {
    final int x; int y; String s;
    Sub(int v) {
        if (v < 0) throw new IllegalArgumentException("neg"); // validate before super()
        int tmp = v * 2;
        x = tmp;                 // assigning a field: allowed
        this.y = v;              // also allowed
        s = helper(v);           // static method: allowed
        super();
        System.out.println("after super " + x);
    }
    static String helper(int v) { return "h" + v; }
    @Override void show() { System.out.println("show x=" + x + " y=" + y + " s=" + s); }
}
// new Sub(5) prints:
// show x=10 y=5 s=h5      ← fields already set when Super() calls show()
// after super 10
```

Compiler messages you will meet: *cannot reference x before supertype constructor has been called*, *cannot assign initialized field 'x' before supertype constructor has been called*, *'return' not allowed before explicit constructor invocation*.

More facts:
- Works with `this(...)` too: `A(int i) { String s = "#" + i; this(s); }`. Locals declared in the prologue stay in scope in the epilogue.
- With `this(...)`, a `final` field assigned in the prologue *and* in the delegated constructor → *variable x might already have been assigned*.
- Works in records' non-canonical constructors: `R(int a) { if (a < 0) throw ...; this(a, a * 2); }`.
- Still at most one `super(...)`/`this(...)`, still not inside `try`/`if`.
- A constructor **without** an explicit call has an empty prologue; all its statements are the epilogue (implicit `super()` comes first) — so old code behaves exactly as before.
- Order of execution: prologue → `super(...)` → this class's field initializers / instance blocks → epilogue.

---

## 4. Object life cycle and garbage collection

- Objects live on the heap; variables hold **references**. An object is **eligible for GC** when no live thread can reach it through any chain of references (a reference cycle that is unreachable — an "island" — is eligible).
- A reference stops pointing to an object when it is reassigned, set to `null`, or goes out of scope.
- `System.gc()` is only a *request*; you can never know *when* (or whether) an object is collected. Exam questions ask "how many objects are eligible at line X", never "has it been collected".
- `finalize()` is deprecated for removal; don't rely on it (Java 25 even allows finalization to be disabled).
- String literals live in the string pool — exam questions use `new String(...)` or other objects when counting eligibility.

```java
String one = new String("1"), two = new String("2");   // objects O1, O2
one = two;          // O1 is now eligible
String three = one; // O2 has two, three, one pointing at it
one = null;         // O2 still reachable via two and three → 0 more eligible
```

---

## 5. Nested classes

| Kind | Declared | Needs outer instance? | Can access outer instance members? | Static members allowed? | Modifiers |
|---|---|---|---|---|---|
| static nested | member, `static` | no | only via an outer object | yes | all access modifiers, `abstract`, `final` |
| inner (member) | member, non-static | **yes** | yes, directly (also `private`) | yes (since Java 16) | all access modifiers, `abstract`, `final` |
| local class | inside a method/block | yes if in instance context | yes + **effectively final** locals | yes (since Java 16) | only `abstract` or `final`, **no access modifier, no `static`** |
| anonymous | `new Type(...) { ... }` | as local | as local | yes (since Java 16) | none; extends **one** class *or* implements **one** interface |

```java
public class Nest {
    private int x = 10; private static int s = 20;
    class Inner {
        int x = 5;
        void show() { int x = 1; System.out.println(x + " " + this.x + " " + Nest.this.x + " " + s); }
    }
    static class SNested { void show() { System.out.println(s + " " + new Nest().x); } }
    public static void main(String[] a) {
        Nest o = new Nest();
        Nest.Inner in = o.new Inner();      // inner needs an outer instance
        in.show();                          // 1 5 10 20
        new SNested().show();               // 20 10
        Nest.Inner in2 = new Nest().new Inner();
        // Inner bad = new Inner();         // DOES NOT COMPILE in static main (no 'this')
    }
}
```

Rules:
- Outer and nested classes can access each other's **private** members.
- `Outer.this.field` reaches a shadowed outer field from an inner class.
- A static nested class cannot use outer instance fields directly → *non-static variable x cannot be referenced from a static context*.
- Local and anonymous classes (and lambdas) may only use local variables/parameters that are **final or effectively final** → *local variables referenced from an inner class must be final or effectively final*. Fields are not restricted (a lambda may do `f++` on a field).
- An anonymous class can pass arguments to a superclass constructor: `new Base("arg") { void m() {} }`. It cannot declare a constructor (it has no name).
- `new Runnable, Comparable() {}` does not compile — one supertype only.
- Local records, enums and interfaces are implicitly **static**: they **cannot** capture enclosing locals (`record R(int v) { int g() { return n; } }` → *non-static variable n cannot be referenced from a static context*).
- Records and enums nested as members are implicitly static.
- Members of an interface (including nested classes) are implicitly `public static`.

### Effectively final

A local variable or parameter is effectively final if it is never assigned after initialization (no `++`, no `=`). Assigning it **anywhere** in its scope — even after the lambda — breaks it:

```java
int n = 1; Runnable r = () -> System.out.println(n); n = 2;   // DOES NOT COMPILE
```

---

## 6. Overloading and varargs

Overloads differ in **parameter types/number/order**. Return type, parameter names, exceptions and access modifiers do not count:
- `int m(int x)` + `long m(int y)` → *already defined*.
- `void m(int[] x)` + `void m(int... y)` → *cannot declare both* (same signature).
- `m(List<String>)` + `m(List<Integer>)` → *name clash: same erasure*.

### Resolution order

The compiler picks the most specific method in the first phase that finds any match:

1. **Exact match** (or subtype for references).
2. **Widening** of primitives (`byte→short→int→long→float→double`, `char→int`).
3. **Boxing/unboxing** (then reference widening, e.g. `int → Integer → Object`).
4. **Varargs**.

Java never widens **then** boxes: `int` → `Long` fails.

```java
static void a(long x)    {}  static void a(Integer x) {}   a(5);  // long     (widening beats boxing)
static void b(Object x)  {}  static void b(int... x)  {}   b(5);  // Object   (boxing beats varargs)
static void c(double x)  {}  static void c(Long x)    {}   c(5);  // double
static void e(Object o)  {}  static void e(String s)  {}   e(null); // String (most specific)
static void g(char c)    {}  static void g(int i)     {}   g((short)1); // int ; g(byte) → int
static void m(Long x) {}  m(5);                           // DOES NOT COMPILE
static void f(StringBuilder o) {} static void f(String s) {}  f(null);   // DOES NOT COMPILE: ambiguous
static void d(int... x) {} static void d(Integer... x) {}     d(1);      // DOES NOT COMPILE: ambiguous
```

Overloading is decided at **compile time** using the **reference type** of arguments. Overriding is decided at **run time** (see §11).

### Varargs

- Only one varargs parameter, and it must be **last** (`m(int... a, String s)` → error).
- Inside the method it is an array. Callers may pass zero args, a list of args, or an array:

```java
static int n(String... s) { return s == null ? -1 : s.length; }
n();                      // 0  (empty array, not null)
n("a", "b");              // 2
n(new String[]{"x"});     // 1
n((String[]) null);       // -1
```
- A method declared with an **array** parameter (`int[]`) cannot be called with a list of values `m(1, 2)`.
- `main(String... args)` is a legal `main`.

---

## 7. Scope, `var` and unnamed variables

### Scope

- Local variables: from declaration to the end of the enclosing block. Loop variables: only inside the loop.
- A local cannot redeclare another local/parameter still in scope (no shadowing between locals), but a local **may** shadow a field (`this.x` reaches the field).
- Instance variables: from object creation to GC. Class variables: whole program lifetime.

### `var` (local variable type inference)

| Allowed | Not allowed |
|---|---|
| local variables with an initializer | fields, method parameters, return types, constructor params |
| `for` and enhanced-`for` variables | without initializer (`var x;`) |
| try-with-resources variables | `var x = null;` |
| lambda parameters `(var x, var y) ->` (all-or-nothing) | array initializer `var x = {1, 2};` |
| `var` as a **variable** or **method** name | compound `var a = 1, b = 2;` |
|  | `var[] x`, `var x[]` |
|  | lambda / method reference initializer `var f = () -> 1;` |
|  | mixing `(var x, y) ->` |
|  | `var` as a class/interface/type name |

- The type is fixed at compile time: `var n = 1; n = "s";` → error; `n = 'c';` is fine (char fits in int; `n` becomes 99).
- `var list = new ArrayList<>();` infers `ArrayList<Object>`.
- `var s = (String) null;` compiles (type String).

### 🆕 Java 22 — Unnamed variables `_` (JEP 456)

`_` declares a variable you don't intend to use. It can never be read; several `_` may coexist in the same scope.

**Allowed** where a *local* variable is declared:
```java
int _ = sideEffect();                          // local variable (needs an initializer)
int _ = 1; int _ = 2;                          // several in one scope: OK
var _ = "x";
for (int i = 0, _ = 0; i < 1; i++) {}          // basic for
for (var _ : list) count++;                    // enhanced for
try (var _ = new StringReader("s")) {}          // try-with-resources
catch (NumberFormatException _) {}              // exception parameter
(a, _) -> a                                     // lambda parameter
map.computeIfAbsent("k", _ -> 1);
if (o instanceof Point(int x, _)) {}            // unnamed pattern inside a record pattern
if (o instanceof Point _) {}                    // unnamed type pattern
case Integer _, String _ -> ...                 // multiple patterns in one case label
```

**Not allowed**: fields, method/constructor parameters, `int _;` without initializer, reading it (`println(_)` → *underscore not allowed here*), bare `_` as a top-level pattern (`o instanceof _`). In Java 17, `_` was already a keyword, so it could not be used as an identifier at all.

---

## 8. Encapsulation and immutability

**Encapsulation:** `private` fields, access via methods (getters `getX`/`isX` for boolean, setters `setX`). The point is controlling *how* state changes.

**Immutable class recipe:**
1. Make the class `final` (or all constructors private) so no subclass can add mutable state or override behaviour.
2. All fields `private final`.
3. No setters / no methods that mutate fields.
4. **Defensive copy** mutable inputs in the constructor.
5. Never hand out a mutable field directly — return a copy or an unmodifiable view.

```java
public final class Immut {
    private final List<String> tags;
    public Immut(List<String> tags) { this.tags = new ArrayList<>(tags); }        // copy in
    public List<String> getTags() { return Collections.unmodifiableList(tags); }  // read-only out
}
// caller's later src.add("b") does not affect the object; getTags().add(..) → UnsupportedOperationException
```

Records give 1–3 automatically but **not** 4–5: `record Box(List<String> items)` stores whatever list you pass. Use `Box { items = List.copyOf(items); }`.

---

## 9. Inheritance basics

- A class `extends` exactly one class (default `Object`) and `implements` any number of interfaces. Interfaces `extend` any number of interfaces.
- Not inherited: constructors, `private` members, and **static methods of interfaces**.
- `this.x` / `this.m()` start lookup in the current class; `super.x` / `super.m()` start in the parent.
- A `final` class cannot be extended; a `final` method cannot be overridden or hidden.

```java
class A { int v = 1; String who() { return "A"; } }
class B extends A { int v = 2; String who() { return "B"; }
    String all() { return v + " " + this.v + " " + super.v + " " + who() + " " + super.who(); } }
// new B().all() → "2 2 1 B A"
```

---

## 10. Overriding, hiding and abstract classes

### Overriding rules (instance method, same signature in subclass)

1. Same name and parameter types (otherwise it's an overload).
2. Access **same or wider** (`protected` → `public` OK; `protected` → package → *weaker access privileges*).
3. Checked exceptions: **none new or broader**. May throw fewer, narrower, or any unchecked exceptions.
4. Return type **covariant**: same type or a subtype for references (`Number` → `Integer`, `Object` → `String`). Primitives must match exactly (`long` → `int` is an error).
5. Neither may be `static` (instance ↔ static mismatch is an error either way).
6. The parent method must not be `final`.

```java
class A { protected Object m() throws Exception { return null; } }
class B extends A { @Override public String m() { return "widened"; } }   // OK: wider, covariant, no exception

A x = new B();  x.m();   // DOES NOT COMPILE unless Exception handled: compiler checks A.m()'s throws clause
```

- `@Override` on a method that doesn't override → compile error. A common case: `public boolean equals(MyType o)` is an **overload**, not an override of `equals(Object)`.
- `private` methods are not inherited, so a same-named method in the subclass is a new, unrelated method (any return type allowed). Calls in the parent still go to the parent's private method.
- Calling an overridden method uses the **object's** runtime type (virtual dispatch), even from a parent constructor.

### Hiding

| Member | Same signature in subclass | Which one runs? |
|---|---|---|
| instance method | **overrides** | object type (runtime) |
| static method | **hides** (both must be static) | reference type (compile time) |
| field (static or instance) | **hides** (types may differ) | reference type (compile time) |

```java
class A { String name = "A"; static String s() { return "sA"; } String i() { return "iA"; } String n() { return name; } }
class B extends A { String name = "B"; static String s() { return "sB"; } String i() { return "iB"; } }
A x = new B();
x.name  // "A"   field → reference type
x.s()   // "sA"  static → reference type
x.i()   // "iB"  instance method → object type
x.n()   // "A"   code in A sees A's field
```

- `@Override` on a static method → *static methods cannot be annotated with @Override*.
- A `static final` method cannot be hidden.

### Abstract classes

- `abstract class` may have constructors, fields, concrete and abstract methods, and zero abstract methods. It cannot be instantiated (`new A()` → error), but its constructor runs when a subclass is created.
- An abstract method has no body (`abstract void m() {}` → error) and can only exist in an abstract class (or interface/enum constant bodies).
- Illegal combinations: `abstract` + `final`, `abstract` + `private`, `abstract` + `static` (methods); `abstract` + `final` (class).
- The **first concrete** subclass must implement all inherited abstract methods; an abstract subclass may leave them.

---

## 11. Polymorphism and casting

- **Reference type** decides which members you may *access* (compile time). **Object type** decides which *overridden method runs* (run time).

```java
class A {} class B extends A { void only() {} }
A x = new B();
x.only();          // DOES NOT COMPILE: A has no only()
((B) x).only();    // OK
```

- Overloads chosen by reference type, overrides by object type:
```java
class A { void m(Object o) { System.out.println("A.m(Object)"); } }
class B extends A { void m(String s) { System.out.println("B.m(String)"); } }
A x = new B();
x.m("hi");          // A.m(Object) — A only knows m(Object); B didn't override it
((B) x).m("hi");    // B.m(String)
```

### Casting rules

| Cast | Result |
|---|---|
| subtype → supertype (upcast) | implicit, always OK |
| supertype → subtype | needs explicit cast; `ClassCastException` at run time if the object isn't that type |
| between **unrelated classes** (neither extends the other) | **compile error** (*incompatible types*) |
| class → interface | compiles if class is **not final** (a subclass might implement it); CCE at run time if not |
| **final** class → interface it doesn't implement | **compile error** |
| `(String)(Object) integer` | compiles; CCE at run time |

`instanceof` follows the same logic: `Integer i; i instanceof String` → compile error (incompatible types). `x instanceof T` is `false` for `null`.

---

## 12. Sealed classes and interfaces (Java 17)

```java
sealed interface Vehicle permits Car, Truck, Bike {}
final class Car implements Vehicle {}
non-sealed class Truck implements Vehicle {}       // reopened: anyone may extend Truck
record Bike(int gears) implements Vehicle {}       // records are implicitly final
class BigTruck extends Truck {}                    // fine
```

Rules:
- Every **direct** permitted subtype must be `final`, `sealed`, or `non-sealed` (records and enums are implicitly final/sealed so they qualify). Missing → *sealed, non-sealed or final modifiers expected*. Interfaces can be `sealed` or `non-sealed` (not `final`).
- Each class in `permits` must actually extend/implement the sealed type (*invalid permits clause*). A class not listed cannot extend it.
- `permits` may be **omitted** only if all subtypes are declared in the **same source file** (including as nested classes). A sealed class with no subclass at all → *sealed class must have subclasses*.
- Location of permitted subtypes:
  - **Named module:** same **module** (may be different packages).
  - **Unnamed module** (classpath): same **package**. Different package → *class in unnamed module cannot extend a sealed class in a different package*.
- `non-sealed` on a class without a sealed supertype → error.
- Local classes and anonymous classes may **not** extend a sealed type.
- Abstract sealed classes are common: `sealed abstract class Shape {}`.
- A `switch` over a sealed type with all permitted subtypes covered is **exhaustive** — no `default` needed (see D2).
- Spelling: `non-sealed` (hyphen). Not `nonsealed`, `unsealed`, `closed`.

---

## 13. Records (Java 16)

```java
record Rect(double w, double h) implements Shape, Comparable<Rect> {
    static int count;                         // static fields OK
    static final Rect UNIT = new Rect(1, 1);
    Rect {                                    // compact canonical constructor
        if (w < 0) throw new IllegalArgumentException("w");
        w = Math.abs(w);                      // reassigns the PARAMETER, field set afterwards
        count++;
    }
    Rect(double side) { this(side, side); }   // non-canonical: must call this(...)
    public double area() { return w * h; }
    public double w() { return w; }          // explicit accessor: must be public
    public int compareTo(Rect o) { return Double.compare(area(), o.area()); }
    record Pair(Rect a, Rect b) {}            // nested record: implicitly static
}
new Rect(2, 3)  // toString: Rect[w=2.0, h=3.0]
```

What you get automatically: `private final` field per component, public accessor `w()` (not `getW()`), canonical constructor, `equals` (component-wise), `hashCode`, `toString` (`Name[a=1, b=2]`).

**Implicit modifiers:** a record is `final` (cannot be extended), its nested-member form is `static`, it implicitly extends `java.lang.Record` (so it **cannot `extends`** anything). It **can `implement`** interfaces.

### Constructors

| Form | Rules |
|---|---|
| **Long canonical** `R(int a) { this.a = a; }` | parameter names and types must match components exactly; must assign **every** field; access at least as wide as the record |
| **Compact** `R { ... }` | no parameter list; fields are assigned *after* the body from the (possibly reassigned) parameters; **`this.a = a` inside is an error** (*cannot assign a value to final variable*) |
| **Non-canonical** `R(String s) { this(...); }` | must start (after an optional 🆕 prologue) with `this(...)` |

- You cannot declare both a compact and a long canonical constructor (*already defined*).
- `public record R(int a) { R { } }` → error: canonical constructor must not have weaker access than the record (*attempting to assign stronger access privileges; was public*).
- Wrong parameter names in a long canonical constructor → *invalid parameter names in canonical constructor*.

### What a record cannot declare

- Instance fields (*field declaration must be static*) — only components.
- Instance initializers `{ }` (static initializers are fine).
- `abstract`, `native` methods; `extends` clause.
- Setters that assign fields (fields are final).
- Accessors that are not `public`, or that have a `throws` clause, or a different return type.

### Allowed

Static fields/methods/initializers, instance methods, nested types (incl. records, enums, interfaces), overriding `equals`/`hashCode`/`toString`, `implements`, generics `record Pair<A, B>(A a, B b)`, zero components `record R() {}`, local records (implicitly static — can't capture locals).

**Trap:** array components use array identity in `equals`: `new R(new int[]{1}).equals(new R(new int[]{1}))` → `false`.

---

## 14. `Object` methods

| Method | Contract / default |
|---|---|
| `boolean equals(Object o)` | default `==`. Must be **reflexive**, **symmetric**, **transitive**, **consistent**, and `x.equals(null)` is `false`. |
| `int hashCode()` | equal objects **must** have equal hash codes; unequal objects *may* share one. Must be consistent while relevant state is unchanged. Override `equals` ⇒ override `hashCode`. |
| `String toString()` | default `ClassName@hexHash`. Called implicitly by string concatenation / `println`. |
| `getClass()`, `wait`, `notify`, `notifyAll` | `final` — cannot override (*overridden method is final*) |

- `equals(MyType o)` is an overload, invisible through an `Object` reference:
```java
class Q { int id; public boolean equals(Q o) { return o.id == id; } }
Object q = new Q(1);
q.equals(new Q(1));        // false  (Object.equals runs)
new Q(1).equals(new Q(1)); // true   (overload chosen at compile time)
```
- Overriding `equals` but not `hashCode` breaks hash collections: two "equal" objects end up as separate entries in a `HashSet`.
- Pattern-matching style: `return o instanceof P p && p.id == id;`
- Interfaces cannot declare a `default` method that overrides `equals`/`hashCode`/`toString` (*default method equals overrides a member of java.lang.Object*).

---

## 15. Interfaces

| Member | Implicit modifiers | Body? | Notes |
|---|---|---|---|
| constant `int X = 1;` | `public static final` | must be initialized | `int X;` → error; can't be reassigned |
| abstract method `void m();` | `public abstract` | no | implementing method must be `public` |
| `default` method | `public` | yes | can be overridden; instance context |
| `static` method | `public` (or `private static`) | yes | **not inherited**; call only as `Iface.m()` |
| `private` method (Java 9) | — | yes | helper for default/private methods |
| `private static` method | — | yes | helper for static/default/private methods |

- Interface methods can't be `protected`, `final`, or package-private (no such keyword; omitted = `public`).
- `default` methods are only allowed in interfaces (`default void m(){}` in a class → error).
- Implementing an interface method without `public` → *attempting to assign weaker access privileges; was public*.
- A static interface method is **not** inherited by implementing classes or subinterfaces: `C.s()`, `B.s()` → *cannot find symbol*; `ref.s()` on an interface reference → *illegal static interface method call*. Use `A.s()`.
- An abstract class implementing an interface needn't implement its methods; the first concrete class must.
- A subinterface may **re-abstract** a default method (`interface Swim extends Walk { String move(); }`).

### Default method conflicts

1. **Class wins:** a method inherited from a superclass beats an interface default (`class Hybrid extends Base implements Run` uses `Base.move()`).
2. **More specific interface wins:** if `B extends A` and both define `m()`, a class implementing both gets `B.m()`.
3. Otherwise two unrelated defaults with the same signature → compile error (*class C inherits unrelated defaults for m() from types A and B*) unless the class overrides `m()`. Inside the override, call a specific one with **`Interface.super.m()`**:

```java
interface Walk { default String move() { return "walk" + helper(); } private String helper() { return "!"; } }
interface Run  { default String move() { return "run"; } }
class Robot implements Walk, Run {
    public String move() { return Walk.super.move() + "+" + Run.super.move(); }   // "walk!+run"
}
```
`X.super.m()` works only for a **direct** superinterface `X` of the class (`A.super.m()` when only `B extends A` is implemented → *not an enclosing class: A*).

---

## 16. Functional interfaces

- Exactly **one abstract method** (SAM). Default, static and private methods don't count.
- Abstract methods matching public `Object` methods (`equals(Object)`, `hashCode()`, `toString()`) **don't count**. `clone()` is protected in `Object`, so declaring `Object clone();` **does** count.
- `@FunctionalInterface` is optional; if present, the compiler checks: zero or two+ abstract methods, or annotating a class → error.
- A subinterface that adds no abstract methods is still functional.

```java
@FunctionalInterface
interface Calc {
    int apply(int x);
    boolean equals(Object o); String toString(); int hashCode();   // don't count
    default Calc twice() { return x -> apply(apply(x)); }
    static Calc id() { return x -> x; }
}
Calc c = x -> x + 1; c.twice().apply(1);   // 3
@FunctionalInterface interface None { boolean equals(Object o); }   // DOES NOT COMPILE: no abstract method
```

(Lambda syntax and the built-in `java.util.function` interfaces are covered in D6.)

---

## 17. Enums

```java
enum Planet {
    MERCURY(3.3), EARTH(5.9) { @Override String describe() { return "home"; } };   // ';' needed before members
    private final double mass;
    Planet(double m) { mass = m; System.out.print("ctor-" + name() + " "); }      // implicitly private
    double mass() { return mass; }
    String describe() { return "planet"; }
}
enum Op {
    PLUS  { int apply(int a, int b) { return a + b; } },
    TIMES { int apply(int a, int b) { return a * b; } };
    abstract int apply(int a, int b);      // every constant must implement it
}
```

- Constants come **first**; if anything follows, the list ends with `;`.
- Constructors are implicitly `private`; `public`/`protected` → error. You can't call `new` on an enum (*enum classes may not be instantiated*).
- All constructors run **once each**, when the enum class is first used (all constants together, in declaration order).
- An enum constructor/instance initializer cannot read a non-constant **static** field of the enum (*illegal reference to static field from initializer*).
- An enum cannot `extend` anything (it implicitly extends `java.lang.Enum`) and cannot be extended; it **can implement** interfaces (also sealed ones).
- Abstract method in the enum ⇒ **every** constant needs a body implementing it; otherwise error.
- A constant with a body is an anonymous subclass: `EARTH.getClass()` is `Planet$1`; `getDeclaringClass()` is `Planet`.

| Method | Notes |
|---|---|
| `values()` | static, compiler-generated; array in declaration order |
| `valueOf(String)` | exact, case-sensitive name; otherwise `IllegalArgumentException` |
| `name()`, `toString()` | constant name (`toString` overridable) |
| `ordinal()` | 0-based position |
| `compareTo` | by ordinal (enums are `Comparable`) |

- Compare with `==` (or `equals`). `Size.S == 0` → compile error (enum vs int).
- **switch:** in a classic switch, case labels use the simple name `case EARTH:`. 🆕 **Java 21**: the qualified name `case Planet.EARTH ->` is also allowed (it was an error in Java 17).
- A switch **expression** over an enum must be exhaustive: all constants or `default` (*the switch expression does not cover all possible input values*).

---

## 18. 🆕 Java 25 — Instance main methods and compact source files (JEP 512)

In brief (details in **D7**): `main` no longer needs to be `static`, `public` or take `String[]`. A class may have `void main()` as an instance method; the launcher creates an instance via the no-arg constructor and calls it. If several candidates exist, `static void main(String[])` / `main(String[])` is preferred over `main()`. A **compact source file** may declare fields and methods with no enclosing class: the compiler wraps them in an implicit `final` class, imports the `java.base` module automatically, and `IO.println(...)` is available:

```java
String msg = "compact";
void main() { IO.println(msg + " " + List.of(1, 2)); }   // java Compact.java → compact [1, 2]
```

---

## 🆕 Since Java 17

| Feature | Final in | Effect on D3 |
|---|---|---|
| Qualified enum constants as case labels (`case Color.RED`) | 21 (JEP 441) | §17 |
| Unconditional type patterns in `instanceof` (`str instanceof CharSequence cs` compiles) | 21 | was an error in 17 |
| Record patterns, pattern `switch` | 21 (JEP 440/441) | see D2; used with sealed types/records |
| Unnamed variables and patterns `_` | 22 (JEP 456) | §7 |
| Flexible constructor bodies (prologue before `super()`/`this()`) | 25 (JEP 513) | §3 |
| Instance main methods & compact source files | 25 (JEP 512) | §18, D7 |
| Module import declarations | 25 (JEP 511) | D7 |

Preview in 25 — **not on the exam**: primitive types in patterns (JEP 507).

---

## Exam traps

- A record declaring an instance field or instance initializer, or using `extends`.
- `this.x = x` inside a **compact** record constructor (only the parameter may be reassigned).
- A record accessor declared without `public` (package access is weaker than the implicit public).
- Non-canonical record constructor that doesn't delegate with `this(...)`.
- Enum: members before constants, missing `;`, `public` constructor, `new Color()`, comparing with `int`.
- An abstract method in an enum where one constant lacks a body.
- `protected` member accessed through a **parent-type** reference in a subclass in another package.
- Package-private member accessed from a subclass in another package.
- Subclass with no explicit constructor when the parent has **no no-arg constructor**.
- `final` field not assigned in **every** constructor; `static final` assigned in a constructor.
- Overriding with weaker access, a broader checked exception, a non-covariant return type, or static ↔ instance mismatch.
- Calling a method through a parent reference whose version declares a checked exception — the compiler uses the **reference** type's `throws` clause.
- Fields and static methods are **hidden** (reference type wins), instance methods **overridden** (object type wins).
- `equals(MyType)` overloads instead of overriding; `@Override` on it fails to compile.
- Cast between unrelated classes → compile error; cast to an interface → compiles unless the class is `final`.
- `int` argument to a `Long` parameter → compile error (no widen-then-box).
- `f(null)` with two unrelated reference overloads → ambiguous.
- `void ClassName()` is a method, not a constructor.
- Static method of an interface called via implementing class or instance.
- Two interfaces with the same default method and no override in the class.
- `X.super.m()` where `X` isn't a direct superinterface.
- `@FunctionalInterface` on an interface whose only abstract method is `equals(Object)`, or with `clone()` plus another method.
- Lambda/local/anonymous class capturing a local that is modified **anywhere**, even after the lambda.
- Local record or local enum referring to an enclosing method's local variable.
- `static` or an access modifier on a local class.
- `new Inner()` from a static method without an outer instance.
- `var` without initializer, with `null`, with `{...}`, in a compound declaration, as a field/parameter/return type.
- `_` used as a field or method parameter, or read after declaring.
- Sealed subclass without `final`/`sealed`/`non-sealed`; permitted subclass in another package on the classpath; omitting `permits` with subclasses in other files.
- 🆕 Reading a field, calling an instance method, `return`, or assigning an *initialized* or *inherited* field in a constructor prologue.
- 🆕 `super()` inside `try` or `if` is still illegal; only one constructor call per constructor.
- GC: an object is eligible as soon as it's unreachable; `System.gc()` guarantees nothing.

---

## Self-check before practice

- [ ] I can write the output of a parent/child class with static blocks, instance blocks, field initializers and `this()` chaining.
- [ ] I can list what is allowed and forbidden in a Java 25 constructor prologue, and explain why assigning a field there fixes the "overridable call in constructor sees 0" problem.
- [ ] I know when the compiler inserts `super()` and when that causes an error.
- [ ] I can fill in the access-modifier table, including the `protected`-via-parent-reference rule.
- [ ] I can count objects eligible for GC after a sequence of reassignments.
- [ ] I know the four nested-class kinds, which need an outer instance, and which modifiers each allows.
- [ ] I can resolve an overloaded call: exact → widening → boxing → varargs, and spot ambiguous calls.
- [ ] I know every place `var` is and isn't allowed.
- [ ] I know every place `_` is allowed (local, for, try resource, catch, lambda param, patterns) and where it isn't (fields, method params).
- [ ] I can state the 6 overriding rules and the difference between hiding and overriding.
- [ ] I know which casts fail at compile time and which throw `ClassCastException`.
- [ ] I know the sealed rules: modifiers on subclasses, `permits` omission, same package vs same module.
- [ ] I can write a record with compact, long canonical and non-canonical constructors, and list 5 things a record cannot declare.
- [ ] I can state the `equals`/`hashCode` contract and spot an `equals` overload.
- [ ] I know the implicit modifiers of every interface member and the three default-method conflict rules, including `X.super.m()`.
- [ ] I can decide whether an interface is functional (Object methods don't count).
- [ ] I can write an enum with a constructor, fields, an abstract method with constant bodies, and use `values()`/`valueOf()`/`ordinal()`.
- [ ] I can write a Java 25 compact source file with an instance `main()`.

**Practice:** `practice/ocp17-829/ch03-utilizing-java-object-oriented-approach.md` (Java 17 book, chapter 3), then the `practice/java25-gap/` files tagged `d03-oop` for flexible constructor bodies, unnamed variables and the other 🆕 items. Also mixed OOP questions in the practice exams (ch12–ch14).
