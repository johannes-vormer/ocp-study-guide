---
title: "D6 — Streams and Lambdas"
topic: d06-streams
order: 6
---

# D6 — Streams and Lambdas

## What the exam tests

- Lambda syntax: what compiles and what does not, `var` parameters, effectively final variables.
- The `java.util.function` interfaces: names, abstract method names, and the primitive specializations.
- The four kinds of method references.
- Stream pipelines: sources, intermediate and terminal operations, laziness, short-circuiting, single use.
- `Optional` and `OptionalInt`/`OptionalLong`/`OptionalDouble`.
- Primitive streams (`IntStream`, …) and how to convert between object and primitive streams.
- `reduce`, `collect`, and `Collectors` (`toMap`, `groupingBy`, `partitioningBy`, `joining`, `mapping`, `teeing`, …).
- `flatMap`, `mapMulti`, `takeWhile`/`dropWhile`, `iterate`, `concat`.
- Parallel streams: ordering, `findAny`, `forEachOrdered`, and the `reduce` identity/combiner rules.
- 🆕 **Stream gatherers** (Java 24): `Stream::gather` and `java.util.stream.Gatherers`.

Typical question shapes: "What is printed?" (laziness, `peek`, short-circuiting), "Which line does not compile?" (wrong functional interface, `Optional` vs value, wrong collector return type), and "Which exception?" (reused stream, `toMap` duplicate key, `Optional.get` on empty).

---

## 1. Lambdas

### Syntax

```java
s -> s.isEmpty()                    // one param, no type → parentheses optional
(s) -> s.isEmpty()
(String s) -> s.isEmpty()           // typed → parentheses required
(var s) -> s.isEmpty()              // var allowed (Java 11)
(a, b) -> a + b
() -> 42                            // no params → () required
x -> { return x + 1; }              // block body: return and ; required
() -> {}                            // empty block: fine for Runnable
```

| Code | Compiles? | Why |
|---|---|---|
| `String s -> s.isEmpty()` | ❌ | typed parameter needs parentheses |
| `(var a, b) -> 1` | ❌ | all parameters must be `var`, or all typed, or all untyped |
| `(String a, b) -> 1` | ❌ | same rule |
| `() -> { "x"; }` | ❌ | `"x"` is not a statement; you need `return "x";` |
| `() -> { return "x" }` | ❌ | missing `;` |
| `Runnable r = () -> 5;` | ❌ | a value is not a statement, and `Runnable` returns `void` |
| `Runnable r = () -> "x".length();` | ✅ | a method call can be used as a statement, so the result is discarded |
| `Supplier<Integer> s = () -> System.out.println();` | ❌ | a `void` call cannot supply a value |
| `var r = () -> {};` | ❌ | a lambda needs a target type |
| `Object o = () -> {};` | ❌ | `Object` is not a functional interface. `(Runnable) () -> {}` is OK |
| `String s = "a"; Predicate<String> p = s -> s.isEmpty();` | ❌ | a lambda parameter cannot reuse the name of a local variable in scope |
| 🆕 `(_, b) -> b` | ✅ | **Java 22** unnamed variable (JEP 456) for an unused parameter |

### Effectively final

A lambda can read local variables (and parameters) of the enclosing method only if they are `final` or **effectively final**, meaning never reassigned, **before or after** the lambda.

```java
int x = 1; x++; Runnable r = () -> System.out.println(x);   // ❌ (modified before)
int y = 1; Runnable q = () -> System.out.println(y); y++;   // ❌ (modified after)
int z = 1; Runnable p = () -> z++;                          // ❌ (modified inside)
for (int i = 0; i < 2; i++) { Runnable t = () -> System.out.println(i); }  // ❌
for (String s : list) { Runnable t = () -> System.out.println(s); }        // ✅ new variable per iteration
```

**Instance and static fields** have no such restriction: `() -> field++` compiles.

### Functional interfaces

A functional interface has **exactly one abstract method**. `default` and `static` methods do not count, and neither do abstract methods that redeclare a public `Object` method (`equals`, `hashCode`, `toString`). With two abstract methods, `@FunctionalInterface` is a compile error.

---

## 2. `java.util.function` — the table to memorise

| Interface | Abstract method | Signature | Useful extras |
|---|---|---|---|
| `Supplier<T>` | `get()` | `() → T` | |
| `Consumer<T>` | `accept(T)` | `T → void` | `andThen` |
| `BiConsumer<T,U>` | `accept(T,U)` | `(T,U) → void` | `andThen` |
| `Predicate<T>` | `test(T)` | `T → boolean` | `and`, `or`, `negate`, `Predicate.not`, `isEqual` |
| `BiPredicate<T,U>` | `test(T,U)` | `(T,U) → boolean` | `and`, `or`, `negate` |
| `Function<T,R>` | `apply(T)` | `T → R` | `andThen`, `compose`, `Function.identity()` |
| `BiFunction<T,U,R>` | `apply(T,U)` | `(T,U) → R` | `andThen` |
| `UnaryOperator<T>` | `apply(T)` | `T → T` (extends `Function<T,T>`) | `UnaryOperator.identity()` |
| `BinaryOperator<T>` | `apply(T,T)` | `(T,T) → T` (extends `BiFunction<T,T,T>`) | `minBy`, `maxBy` |

`f.andThen(g)` = `g(f(x))`, while `f.compose(g)` = `f(g(x))`.
`UnaryOperator` is a `Function`, but a `Function<Integer,Integer>` variable **cannot** be assigned to a `UnaryOperator<Integer>` variable.

### Primitive specializations (method names change!)

| Shape | int | long | double | Method |
|---|---|---|---|---|
| `() → prim` | `IntSupplier` | `LongSupplier` | `DoubleSupplier` | `getAsInt()` / `getAsLong()` / `getAsDouble()` |
| `() → boolean` | `BooleanSupplier` (the only boolean specialization) | | | `getAsBoolean()` |
| `prim → void` | `IntConsumer` | `LongConsumer` | `DoubleConsumer` | `accept` |
| `prim → boolean` | `IntPredicate` | `LongPredicate` | `DoublePredicate` | `test` |
| `prim → R` | `IntFunction<R>` | `LongFunction<R>` | `DoubleFunction<R>` | `apply` |
| `T → prim` | `ToIntFunction<T>` | `ToLongFunction<T>` | `ToDoubleFunction<T>` | `applyAsInt` / `applyAsLong` / `applyAsDouble` |
| `(T,U) → prim` | `ToIntBiFunction` | `ToLongBiFunction` | `ToDoubleBiFunction` | `applyAsXxx` |
| `prim → same prim` | `IntUnaryOperator` | `LongUnaryOperator` | `DoubleUnaryOperator` | `applyAsXxx` |
| `(prim,prim) → same prim` | `IntBinaryOperator` | `LongBinaryOperator` | `DoubleBinaryOperator` | `applyAsXxx` |
| `prim → other prim` | `IntToLongFunction`, `IntToDoubleFunction` | `LongToIntFunction`, `LongToDoubleFunction` | `DoubleToIntFunction`, `DoubleToLongFunction` | `applyAsXxx` |
| `(T, prim) → void` | `ObjIntConsumer<T>` | `ObjLongConsumer<T>` | `ObjDoubleConsumer<T>` | `accept` |

A few trick names: `IntFunction` **takes** an int and returns R (`apply`), while `ToIntFunction` **returns** an int (`applyAsInt`). There is no `IntBiFunction` and no `BooleanPredicate`.

### Method references

| Kind | Syntax | Equivalent lambda | Example target |
|---|---|---|---|
| Static method | `Class::staticMethod` | `x -> Integer.parseInt(x)` | `Function<String,Integer> f = Integer::parseInt;` |
| Instance method of a particular object | `obj::method` | `x -> System.out.println(x)` | `Consumer<String> c = System.out::println;` |
| Instance method of an arbitrary object (the first parameter is the receiver) | `Class::instanceMethod` | `(s, p) -> s.startsWith(p)` | `BiFunction<String,String,Boolean> b = String::startsWith;` |
| Constructor | `Class::new` | `() -> new ArrayList<>()` | `Supplier<List<String>> s = ArrayList::new;` / `Function<Integer,List<String>> f = ArrayList::new;` (capacity) |

The same reference can fit several shapes: `String::new` works as both `Supplier<String>` and `Function<String,String>`. `String::length` works as `Function<String,Integer>`, `ToIntFunction<String>`, and as a `Consumer<String>` (the result is discarded).

---

## 3. Stream pipeline

```
source  →  0..n intermediate operations (lazy)  →  1 terminal operation (triggers the work)
```

- **Lazy**: nothing runs until a terminal operation is called. Without a terminal operation, `peek` prints nothing.
- **One element at a time**: each element passes through the whole pipeline before the next one starts. *Stateful* operations (`sorted`, `distinct`) are the exception: `sorted` must see every element first.
- **Single use**: running a second terminal operation on the same stream object → `IllegalStateException`.

```java
Stream.of("a","b","c").peek(s -> System.out.print("p"+s+" "))
      .map(String::toUpperCase).forEach(s -> System.out.print(s+" "));
// pa A pb B pc C

Stream.of(3,1,2).peek(s -> System.out.print("p"+s+" "))
      .sorted().forEach(s -> System.out.print(s+" "));
// p3 p1 p2 1 2 3
```

### Sources

| Source | Notes |
|---|---|
| `coll.stream()`, `coll.parallelStream()` | |
| `Stream.of(a, b, …)`, `Stream.empty()`, `Stream.ofNullable(x)` | `ofNullable(null)` → an empty stream |
| `Arrays.stream(arr)`, `Arrays.stream(arr, from, toExcl)` | for an `int[]` → `IntStream` |
| `Stream.of(intArray)` | a `Stream<int[]>` with **1** element |
| `Stream.generate(supplier)` | infinite |
| `Stream.iterate(seed, unaryOp)` | infinite |
| `Stream.iterate(seed, hasNext, next)` | finite, like a for loop: `iterate(1, x -> x < 20, x -> x * 3)` → `[1, 3, 9]` |
| `IntStream.range(1, 4)` / `rangeClosed(1, 3)` | `1,2,3` both |
| `Stream.concat(a, b)` | both inputs must be the same kind of stream (`IntStream.concat` for ints) |

### Intermediate operations

| Operation | Notes |
|---|---|
| `filter(Predicate)` | |
| `map(Function)` | `Stream<T>` → `Stream<R>` |
| `mapToInt/Long/Double`, `mapToObj` (primitive → object), `boxed()` | change the stream kind |
| `flatMap(T → Stream<R>)` | the function **must return a stream**: `flatMap(x -> List.of(x))` does not compile; `flatMap(List::stream)` does |
| `mapMulti((T, Consumer<R>) → void)` | push 0..n results per element |
| `distinct()`, `sorted()`, `sorted(Comparator)` | stateful. `sorted()` on non-`Comparable` elements → `ClassCastException` (at the terminal operation) |
| `limit(n)`, `skip(n)` | `limit` short-circuits |
| `takeWhile(p)` / `dropWhile(p)` | stop or start at the **first** element that fails `p` (not a filter!) |
| `peek(Consumer)` | for debugging |
| `parallel()`, `sequential()`, `unordered()` | the **last** call wins for the whole pipeline |
| 🆕 `gather(Gatherer)` | Java 24, see the end of this page |

```java
Stream.of(1,2,5,1,7).takeWhile(x -> x < 3).toList();   // [1, 2]
Stream.of(1,2,5,1,7).dropWhile(x -> x < 3).toList();   // [5, 1, 7]
Stream.of(1,2,3).<Integer>mapMulti((x, sink) -> {
    if (x != 2) { sink.accept(x); sink.accept(x * 10); }
}).toList();                                           // [1, 10, 3, 30]
```

Without the `<Integer>` type witness, `mapMulti` produces a `Stream<Object>`.

### Terminal operations

| Operation | Returns | Short-circuits? | Empty stream |
|---|---|---|---|
| `forEach`, `forEachOrdered` | `void` | no | |
| `count()` | **`long`** | no | `0` |
| `min(Comparator)`, `max(Comparator)` | `Optional<T>` | no | `Optional.empty` |
| `findFirst()`, `findAny()` | `Optional<T>` | **yes** | `Optional.empty` |
| `anyMatch`, `allMatch`, `noneMatch` | `boolean` | **yes** | `false`, **`true`**, **`true`** |
| `reduce(...)` | see below | no | |
| `collect(...)` | see below | no | |
| `toList()` | unmodifiable `List` | no | `[]` |
| `toArray()`, `toArray(String[]::new)` | array | no | |
| `iterator()` | | | |

`Stream<T>.min/max` **require a `Comparator`**. `IntStream.min()/max()` take no argument and return `OptionalInt`.

```java
System.out.println(Stream.of(1,2,3,4).peek(x -> System.out.print("p"+x+" ")).anyMatch(x -> x == 2));
// p1 p2 true        (stops after 2)

Stream.iterate(1, x -> x + 1).filter(x -> x % 2 == 0).limit(3).toList();  // [2, 4, 6]
// but  Stream.iterate(1, x -> x + 1).sorted().limit(3)  → hangs forever (sorted needs every element)
```

**`count()` may skip the pipeline.** When the size is known from the source, the JDK may not run `peek`/`map` at all. `List.of(1,2,3).stream().peek(System.out::print).count()` prints only `3`. Do not rely on side effects in such a pipeline.

---

## 4. `Optional`

| Method | Behaviour |
|---|---|
| `Optional.of(v)` | NPE if `v` is `null` |
| `Optional.ofNullable(v)`, `Optional.empty()` | |
| `isPresent()`, `isEmpty()` | |
| `get()`, `orElseThrow()` | `NoSuchElementException` if empty |
| `orElseThrow(Supplier)` | throws the supplied exception |
| `orElse(v)`, `orElseGet(Supplier)` | default value. `orElse`'s argument is **always** evaluated |
| `ifPresent(Consumer)`, `ifPresentOrElse(Consumer, Runnable)` | |
| `map`, `flatMap` (the function returns an `Optional`), `filter` | an empty `Optional` stays empty |
| `or(Supplier<Optional>)` | the alternative `Optional` |
| `stream()` | 0 or 1 elements |

```java
Optional<Integer> o = Optional.of(1);
int i = o.orElse(null);    // compiles. Would throw NPE (unboxing) if o were empty
```

Primitive optionals: `OptionalInt.getAsInt()`, `OptionalLong.getAsLong()`, `OptionalDouble.getAsDouble()`. They also have `orElse`, `isPresent`, `ifPresent`, and so on, but **no `map`/`filter`/`flatMap`**. `IntStream.average()` returns `OptionalDouble`, so `double d = IntStream.of(1).average();` does not compile.

---

## 5. Primitive streams

| | `IntStream` | `LongStream` | `DoubleStream` |
|---|---|---|---|
| `sum()` | `int` | `long` | `double` |
| `average()` | `OptionalDouble` | `OptionalDouble` | `OptionalDouble` |
| `min()`/`max()` | `OptionalInt` | `OptionalLong` | `OptionalDouble` |
| `summaryStatistics()` | `IntSummaryStatistics` | `LongSummaryStatistics` | `DoubleSummaryStatistics` |
| object stream | `mapToObj`, `boxed()` | | |
| other primitive | `mapToLong`, `mapToDouble`, `asLongStream`, `asDoubleStream` | `mapToInt`, … | `mapToInt`, … |
| `range`/`rangeClosed` | ✅ | ✅ | ❌ |

- `Stream<Integer>` has **no** `sum()`, `average()` or `summaryStatistics()`. Call `mapToInt(x -> x)` first.
- An empty `IntStream` → `sum()` is `0`, and `average()` is `OptionalDouble.empty`.
- An empty `IntSummaryStatistics` → `min = Integer.MAX_VALUE`, `max = Integer.MIN_VALUE`, `average = 0.0`, `count = 0`.
- `IntStream.map` must return an `int`, giving an `IntStream`. To produce objects, use `mapToObj`. `Stream.mapToObj` does not exist.
- `IntStream.collect(Collectors.toList())` does not compile: the primitive `collect` takes three arguments. Call `boxed()` first.
- `IntStream.reduce(0, Integer::sum)` returns `int`. `IntStream.reduce(Integer::sum)` returns `OptionalInt`.

```java
IntSummaryStatistics s = IntStream.of(4, 2, 9).summaryStatistics();
s.getMin(); s.getMax(); s.getAverage(); s.getSum(); s.getCount();   // 2 9 5.0 15 3
```

---

## 6. Reduction

```java
T           reduce(T identity, BinaryOperator<T> acc)
Optional<T> reduce(BinaryOperator<T> acc)
<U> U       reduce(U identity, BiFunction<U,? super T,U> acc, BinaryOperator<U> combiner)
```

```java
Stream.of(1,2,3).reduce(0, Integer::sum);         // 6
Stream.of(1,2,3).reduce(Integer::sum);            // Optional[6]
Stream.<Integer>empty().reduce(0, Integer::sum);  // 0
Stream.<Integer>empty().reduce(Integer::sum);     // Optional.empty
Stream.of("a","bb","ccc").reduce(0, (n, s) -> n + s.length(), Integer::sum);  // 6
```

Rules, needed for parallel streams: the identity must truly be an identity (`acc(identity, x) == x`), and the accumulator and combiner must be **associative** and **stateless**. Break these rules and a parallel result can differ from the sequential one:

```java
Stream.of(1,2,3).reduce(10, Integer::sum);             // 16
Stream.of(1,2,3).parallel().reduce(10, Integer::sum);  // 36 on this JDK: 10 is added once per chunk
```

---

## 7. `collect` and `Collectors`

### Basic collectors

| Collector | Result |
|---|---|
| `toList()`, `toSet()` | no guarantee about the type or mutability (in practice `ArrayList`/`HashSet`) |
| `toUnmodifiableList/Set/Map()` | unmodifiable. `null` elements → NPE |
| `toCollection(TreeSet::new)` | the collection you choose |
| `joining()`, `joining(", ")`, `joining(", ", "[", "]")` | `String`. Elements must be `CharSequence` |
| `counting()` | **`Long`** |
| `summingInt`, `averagingInt` (→ **`Double`**), `minBy`, `maxBy` (→ `Optional`) | |
| `mapping(f, downstream)`, `filtering(p, downstream)`, `flatMapping(f, downstream)` | adapt the elements for a downstream collector |
| `collectingAndThen(c, finisher)` | post-processing |
| `reducing(identity, op)` | |
| `teeing(c1, c2, merger)` | runs two collectors and merges their results (Java 12) |

### `toMap`

```java
List<String> w = List.of("apple", "bob", "cat", "avocado", "bee");
w.stream().collect(Collectors.toMap(String::length, s -> s));
// IllegalStateException: Duplicate key 3 (attempted merging values bob and cat)
w.stream().collect(Collectors.toMap(String::length, s -> s, (a, b) -> a + "|" + b));
// {3=bob|cat|bee, 5=apple, 7=avocado}
w.stream().collect(Collectors.toMap(String::length, s -> s, (a, b) -> a, TreeMap::new));  // a TreeMap
```

`toMap` with a `null` value → NPE.

### `groupingBy` and `partitioningBy`

```java
w.stream().collect(Collectors.groupingBy(String::length));
// {3=[bob, cat, bee], 5=[apple], 7=[avocado]}            Map<Integer, List<String>>

TreeMap<Character, Long> byFirst =
    w.stream().collect(Collectors.groupingBy(s -> s.charAt(0), TreeMap::new, Collectors.counting()));
// {a=2, b=2, c=1}

w.stream().collect(Collectors.groupingBy(String::length,
        Collectors.mapping(s -> s.substring(0, 1), Collectors.toSet())));
// {3=[b, c], 5=[a], 7=[a]}

w.stream().collect(Collectors.partitioningBy(s -> s.length() > 3));
// {false=[bob, cat, bee], true=[apple, avocado]}          Map<Boolean, List<String>>
w.stream().collect(Collectors.partitioningBy(s -> s.startsWith("a"), Collectors.counting()));
// {false=3, true=2}
```

| | `groupingBy` | `partitioningBy` |
|---|---|---|
| Key | any type from the classifier | `Boolean` |
| Missing groups | **absent** | **both keys always present**: `Stream.of("aa").collect(partitioningBy(s -> s.length() > 3))` → `{false=[aa], true=[]}` |
| Overloads | `(f)`, `(f, downstream)`, `(f, mapFactory, downstream)` | `(p)`, `(p, downstream)` |
| `null` key | NPE ("element cannot be mapped to a null key") | n/a |

**Watch the declared type**: `groupingBy(f, counting())` gives `Map<K, Long>`. Assigning it to `Map<Integer, Integer>` does not compile. `groupingBy(f, averagingInt(...))` gives `Map<K, Double>`. A `mapFactory` is only allowed in the **three**-argument form, in the middle position.

```java
String r = w.stream().collect(Collectors.teeing(
        Collectors.counting(), Collectors.joining(), (c, j) -> c + ":" + j));
// 5:applebobcatavocadobee
```

### `Stream.toList()` vs `collect(Collectors.toList())`

| | `stream.toList()` (Java 16) | `collect(Collectors.toList())` | `collect(Collectors.toUnmodifiableList())` |
|---|---|---|---|
| Modifiable? | **no** (UOE) | not guaranteed (an `ArrayList` in practice) | no |
| `null` elements | allowed | allowed | **NPE** |
| Type inference | `List<T>`. `List<Number> n = Stream.of(1).toList();` ❌ | the target guides inference. `List<Number> n = Stream.of(1).collect(Collectors.toList());` ✅ | |

---

## 8. Parallel streams

- `coll.parallelStream()` or `stream.parallel()`. `isParallel()` checks the mode. In `parallel().sequential()`, the last call wins.
- `forEach` on a parallel stream has **no order guarantee**. `forEachOrdered` keeps the encounter order (and gives up some of the speed gain).
- `findAny()` can return **any** element in parallel. `findFirst()` always returns the first element in encounter order (slower in parallel).
- Order-respecting operations keep encounter order on an ordered source even in parallel: `map`, `toList()`, `collect(toList())` and `reduce` with a correct identity. `List.of(1,2,3,4,5).parallelStream().map(x -> x*2).toList()` → `[2, 4, 6, 8, 10]`.
- `reduce` needs an identity, an associative accumulator and a compatible combiner (section 6). `Stream.of("a","b","c").parallel().reduce("", String::concat)` → `"abc"` (correct identity, associative operation).
- Do not mutate shared state from lambdas, for example `parallelStream().forEach(list::add)` on an `ArrayList`. Use `collect` instead.
- `Collectors.groupingByConcurrent` and `toConcurrentMap` exist for unordered parallel collection.

---

## 🆕 Since Java 17

### 🆕 Java 24 — Stream Gatherers (JEP 485)

`gather` is a general-purpose, user-definable **intermediate** operation. It does for intermediate steps what `collect` does for terminal ones.

```java
<R> Stream<R> gather(Gatherer<? super T, ?, R> gatherer)   // on Stream only
```

- It exists only on `Stream<T>`. `IntStream.gather(...)` does not compile (call `boxed()` first).
- It is lazy like every intermediate operation. You can chain several `gather` calls, and compose gatherers with `g1.andThen(g2)`.

**Built-in gatherers in `java.util.stream.Gatherers`:**

| Gatherer | Output | Example → result |
|---|---|---|
| `windowFixed(n)` | non-overlapping `List`s of size `n`. The **last one may be smaller** | `1..5`, `windowFixed(2)` → `[[1, 2], [3, 4], [5]]` |
| `windowSliding(n)` | overlapping windows, moving by 1 | `1..5`, `windowSliding(3)` → `[[1, 2, 3], [2, 3, 4], [3, 4, 5]]`. Fewer than `n` elements → one short window: `Stream.of(1,2)` → `[[1, 2]]` |
| `fold(initialSupplier, folder)` | **one** element: the final accumulated value (it can be a different type) | `Stream.of(1,2,3).gather(fold(() -> "", (acc, x) -> acc + x))` → `["123"]`. Empty stream → `[initial]`, so `fold(() -> 0, …)` gives `[0]` |
| `scan(initialSupplier, scanner)` | a running (prefix) accumulation: one output **per input** (the initial value is not emitted) | `Stream.of(1,2,3).gather(scan(() -> 0, (a, x) -> a + x))` → `[1, 3, 6]`. Empty stream → `[]` |
| `mapConcurrent(maxConcurrency, mapper)` | maps elements concurrently using **virtual threads**, at most `maxConcurrency` at once, **keeping the encounter order** | `Stream.of(1,2,3,4).gather(mapConcurrent(2, x -> x*10))` → `[10, 20, 30, 40]` |

Facts and edge cases (all verified):

- `windowFixed(0)`, `windowSliding(0)` and `mapConcurrent(0, f)` → `IllegalArgumentException`.
- The window lists are **unmodifiable** (`add` → UOE).
- An empty stream with `windowFixed` → `[]`.
- `fold` vs `reduce`: `reduce` is terminal and needs an associative operation. `fold` is intermediate, sequential and ordered, and its result type can differ from the element type.
- `fold` vs `scan`: `fold` emits only the final value. `scan` emits every intermediate value.

**The `Gatherer<T, A, R>` interface** (T = input type, A = private state type, R = output type) has four parts:

| Part | Type | Role |
|---|---|---|
| `initializer()` | `Supplier<A>` | creates the state (optional) |
| `integrator()` | `Gatherer.Integrator<A,T,R>` | **required**. `boolean integrate(A state, T element, Downstream<? super R> downstream)`. Return `false` to stop consuming (short-circuit) |
| `combiner()` | `BinaryOperator<A>` | merges states in parallel (optional; without one the gatherer runs sequentially) |
| `finisher()` | `BiConsumer<A, Downstream<? super R>>` | runs at the end and can push more elements (optional) |

`downstream.push(r)` sends an element on and returns `false` if the downstream no longer wants elements.

**Factory methods:**

- `Gatherer.of(integrator)` (+ optional initializer/combiner/finisher overloads): a **parallelizable** gatherer.
- `Gatherer.ofSequential(integrator)`, `ofSequential(initializer, integrator)`, `ofSequential(initializer, integrator, finisher)`: a **sequential** gatherer.
- `Gatherer.Integrator.ofGreedy(...)` marks an integrator that never short-circuits itself (an optimisation hint).

```java
// stateless 1:1 mapping
Gatherer<Integer, ?, Integer> doubler = Gatherer.of((state, el, down) -> down.push(el * 2));
Stream.of(1,2,3).gather(doubler).toList();                    // [2, 4, 6]

// stateful + short-circuit: works on an infinite stream
Gatherer<Integer, ?, Integer> first2 = Gatherer.ofSequential(
        () -> new int[1],
        (st, el, down) -> st[0]++ < 2 && down.push(el));
Stream.iterate(1, x -> x + 1).gather(first2).toList();        // [1, 2]

// finisher emits at the end
Gatherer<Integer, List<Integer>, List<Integer>> all = Gatherer.ofSequential(
        ArrayList::new,
        (st, el, down) -> { st.add(el); return true; },
        (st, down) -> down.push(st));
Stream.of(3,1,2).gather(all).toList();                        // [[3, 1, 2]]

Stream.of(1,2,3,4).gather(doubler.andThen(Gatherers.windowFixed(3))).toList(); // [[2, 4, 6], [8]]
```

### 🆕 Java 22 — Unnamed variables in lambdas (JEP 456)

`(_, v) -> v` or `map.forEach((_, v) -> …)`: `_` can be used for any number of unused lambda parameters. See D3 for the full rules.

### Not new, but often confused

`Stream.toList()` (Java 16), `mapMulti` (16), `Collectors.teeing` (12), `takeWhile`/`dropWhile`/3-argument `iterate`/`ofNullable` (9) are all already in the Java 17 exam scope.
Structured concurrency, which relates to `mapConcurrent`'s virtual threads, is still **preview** in Java 25 and is not on the exam.

---

## Exam traps

1. No terminal operation → nothing runs. `peek` output appears only when a terminal operation pulls elements.
2. A stream is single use: a second terminal call → `IllegalStateException`.
3. `count()` returns `long`, so `int n = stream.count();` does not compile.
4. `Stream<T>.max()` with no arguments does not compile (it needs a `Comparator`). `IntStream.max()` does.
5. `average()` returns `OptionalDouble`, not `double`. Calling `getAsDouble()` on an empty one → `NoSuchElementException`.
6. `allMatch`/`noneMatch` on an empty stream → `true`. `anyMatch` → `false`.
7. `anyMatch`, `findFirst` and `limit` short-circuit, which is how they can finish on infinite streams. `sorted()` or `count()` on an infinite stream hangs.
8. `takeWhile`/`dropWhile` are **not** filters: they stop or start at the first element that fails the predicate.
9. `toMap` with duplicate keys and no merge function → `IllegalStateException`.
10. `groupingBy(..., counting())` has `Long` values. `averagingX` gives `Double`.
11. `partitioningBy` always has both `true` and `false` keys. `groupingBy` only has keys that occurred.
12. `stream.toList()` is unmodifiable. `collect(Collectors.toList())` makes no promise either way. `toUnmodifiableList` rejects `null`.
13. `flatMap`'s function must return a `Stream`. `flatMap(List::stream)` works, `flatMap(x -> List.of(x))` does not compile.
14. Lambda parameter names cannot shadow local variables. Captured locals must be effectively final, and so must a classic `for` loop index.
15. `(var a, b) -> …` and `(String a, b) -> …` do not compile.
16. `Function<T,T>` is not assignable to a `UnaryOperator<T>` variable.
17. `IntFunction` = int → R. `ToIntFunction` = T → int. `IntUnaryOperator.applyAsInt` vs `Function.apply`.
18. A parallel `reduce` with a non-identity seed (e.g. `10`) or a non-associative operation gives a different result from the sequential one.
19. `forEach` on a parallel stream does not keep order. `forEachOrdered` does. `findAny` may return any element.
20. `Optional.of(null)` → NPE. `Optional.get()` on empty → `NoSuchElementException`. `orElse(expr)` always evaluates `expr`.
21. 🆕 `windowFixed` keeps a smaller final window. `windowSliding` on fewer than `n` elements produces one short window.
22. 🆕 `fold` on an empty stream emits the initial value. `scan` on an empty stream emits nothing.
23. 🆕 `gather` exists only on `Stream`, not on `IntStream`/`LongStream`/`DoubleStream`.
24. 🆕 Window sizes and `maxConcurrency` must be > 0 (`IllegalArgumentException`).
25. 🆕 `mapConcurrent` uses virtual threads but **preserves encounter order**.

---

## Self-check before practice

- [ ] I can say whether any lambda's syntax compiles (parentheses, braces, `return`, `var`).
- [ ] I can spot effectively-final violations, including modifications after the lambda.
- [ ] I can name the abstract method of every interface in the `java.util.function` table, including primitive versions (`applyAsInt`, `getAsBoolean`, …).
- [ ] I can rewrite each of the four kinds of method reference as a lambda and the other way round.
- [ ] I can trace `peek`/`map`/`sorted`/`anyMatch` output order, including lazy evaluation and short-circuiting.
- [ ] I know which operations are terminal, which short-circuit, and what each returns on an empty stream.
- [ ] I know every `Optional` method and which ones throw on empty.
- [ ] I can convert between `Stream<T>` and `IntStream`/`LongStream`/`DoubleStream` (`mapToInt`, `mapToObj`, `boxed`).
- [ ] I know the three `reduce` forms and their return types, and the parallel identity/associativity rules.
- [ ] I can work out the exact result *type* of `groupingBy` / `partitioningBy` with downstream collectors.
- [ ] I know `toMap` duplicate-key behaviour and the merge-function and map-factory overloads.
- [ ] I know the differences between `toList()`, `Collectors.toList()` and `toUnmodifiableList()`.
- [ ] I know `flatMap` vs `mapMulti`, `takeWhile` vs `filter`, and 2-argument vs 3-argument `iterate`.
- [ ] 🆕 I can predict the output of `windowFixed`, `windowSliding`, `fold`, `scan` and `mapConcurrent`, including on empty streams.
- [ ] 🆕 I know the four parts of a `Gatherer`, what the integrator's `boolean` result means, and `of` vs `ofSequential`.

## Practice

- `practice/ocp17-829/ch06-working-with-streams-and-lambda-expressions.md` (150 questions; everything except gatherers).
- `practice/java25-gap/`: the gatherer questions and the questions on unnamed lambda parameters.
