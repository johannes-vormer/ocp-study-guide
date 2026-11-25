---
title: "Gap: Stream gatherers"
source: java25-gap
topic: d06-streams
---

# Gap: Stream gatherers (🆕 Java 24, JEP 485)

## gap-gather-001

What is the output of the following code fragment? (Assume `java.util.*` and `java.util.stream.*` are imported.)

```java
var result = Stream.of(1, 2, 3, 4, 5)
    .gather(Gatherers.windowFixed(2))
    .toList();
System.out.println(result);
```

- **A.** `[[1, 2], [3, 4]]`
- **B.** `[[1, 2], [3, 4], [5]]`
- **C.** `[[1, 2], [2, 3], [3, 4], [4, 5]]`
- **D.** `[[1, 2], [3, 4], [5, null]]`
- **E.** An `IllegalArgumentException` is thrown because 5 is not divisible by 2.

**Answer:** B

**Explanation:** 🆕 Java 24: `Stream.gather(Gatherer)` is an intermediate operation that applies a custom transformation. `Gatherers.windowFixed(n)` groups elements into consecutive, non-overlapping `List`s of size `n`. The **last window may be shorter** if the element count is not a multiple of `n`; it is not dropped and not padded. The result is `[[1, 2], [3, 4], [5]]`.

Option C describes `windowSliding(2)`.

## gap-gather-002

What is the output of the following code fragment? (Assume `java.util.*` and `java.util.stream.*` are imported.)

```java
var a = Stream.of(1, 2, 3, 4, 5).gather(Gatherers.windowSliding(3)).toList();
var b = Stream.of(1, 2).gather(Gatherers.windowSliding(3)).toList();
System.out.println(a.size() + " " + b);
```

- **A.** `3 []`
- **B.** `5 [[1, 2]]`
- **C.** `2 [[1, 2]]`
- **D.** `3 [[1, 2]]`
- **E.** An `IllegalArgumentException` is thrown.

**Answer:** D

**Explanation:** `Gatherers.windowSliding(n)` produces overlapping windows of size `n`, each shifted by one element. For five elements and `n = 3` the windows are `[1, 2, 3]`, `[2, 3, 4]`, `[3, 4, 5]` — three windows (in general `count - n + 1`).

Special case: if the stream is non-empty but has **fewer** than `n` elements, a **single** window containing all of them is emitted, so `b` is `[[1, 2]]`. (An empty stream produces no windows at all.) The output is `3 [[1, 2]]`.

## gap-gather-003

What is the output of the following code fragment? (Assume `java.util.*` and `java.util.stream.*` are imported.)

```java
var s = Stream.of(1, 2, 3, 4)
    .gather(Gatherers.scan(() -> 0, (acc, n) -> acc + n)).toList();
var f = Stream.of(1, 2, 3, 4)
    .gather(Gatherers.fold(() -> 10, (acc, n) -> acc + n)).toList();
System.out.println(s + " " + f);
```

- **A.** `[1, 3, 6, 10] [20]`
- **B.** `[0, 1, 3, 6, 10] [20]`
- **C.** `[1, 3, 6, 10] 20`
- **D.** `[1, 3, 6, 10] [11, 13, 16, 20]`
- **E.** The code does not compile.

**Answer:** A

**Explanation:** Both gatherers take a `Supplier` for the initial value and a `BiFunction` that combines the current state with the next element.

- `scan` is a running (prefix) accumulation: it emits the new state **after each element**. The initial value itself is **not** emitted: `[1, 3, 6, 10]`. B is wrong.
- `fold` is like a reduction but implemented as a gatherer: it emits **one** element, the final state, at the end. Because the result of `gather` is still a stream, `toList()` gives `[20]`, not `20` (C is wrong). D describes `scan` with initial value 10.

## gap-gather-004

What is the output of the following code fragment? (Assume `java.util.*` and `java.util.stream.*` are imported.)

```java
List<String> r = Stream.of(1, 2, 3)
    .filter(n -> n > 5)
    .gather(Gatherers.fold(() -> "start", (s, n) -> s + n))
    .toList();
System.out.println(r.size() + " " + r);
```

- **A.** `0 []`
- **B.** `1 [null]`
- **C.** `1 [start]`
- **D.** `1 [start123]`
- **E.** A `NoSuchElementException` is thrown.

**Answer:** C

**Explanation:** The filter removes every element, so `fold` receives an empty stream. `Gatherers.fold` **always** emits exactly one element — the final state — even when there were no input elements. For an empty input the final state is simply the initial value supplied by `() -> "start"`. The list therefore contains one element: `1 [start]`.

This mirrors `reduce(identity, accumulator)`, which also returns the identity for an empty stream. By contrast, `Gatherers.scan` emits nothing for an empty stream.

Watch out: with an initial value of `""` the output would be `1 []`, because a list containing one empty string prints as `[]`.

## gap-gather-005

What is the result of the following code fragment? (Assume `java.util.*` and `java.util.stream.*` are imported.)

```java
var g = Gatherers.<String>windowFixed(0);
System.out.print("created ");
System.out.println(Stream.of("a").gather(g).toList());
```

- **A.** It prints `created []`.
- **B.** It prints `created [[a]]`.
- **C.** It prints `created ` and then throws an `IllegalArgumentException`.
- **D.** It throws an `IllegalArgumentException` without printing anything.
- **E.** The code does not compile.

**Answer:** D

**Explanation:** `windowFixed(int windowSize)` and `windowSliding(int windowSize)` validate their argument eagerly: a window size less than 1 throws `IllegalArgumentException` **when the gatherer is created**, i.e. on the first line, before anything is printed.

The explicit type witness `Gatherers.<String>windowFixed(0)` is legal syntax, so the code compiles.

## gap-gather-006

What is the result of the following code fragment? (Assume `java.util.*` and `java.util.stream.*` are imported.)

```java
var r = IntStream.rangeClosed(1, 4)
    .gather(Gatherers.windowFixed(2))
    .toList();
System.out.println(r);
```

- **A.** `[[1, 2], [3, 4]]`
- **B.** `[1, 2, 3, 4]`
- **C.** `[[1, 2, 3, 4]]`
- **D.** An `UnsupportedOperationException` is thrown.
- **E.** The code does not compile.

**Answer:** E

**Explanation:** `gather()` is defined only on `Stream<T>`. The primitive streams `IntStream`, `LongStream` and `DoubleStream` have no `gather()` method (and no `toList()` either), so the code does not compile. Inserting `.boxed()` after `rangeClosed(1, 4)` would make it compile and print `[[1, 2], [3, 4]]`.

## gap-gather-007

What is the result of the following code fragment? (Assume `java.util.*` and `java.util.stream.*` are imported.)

```java
var w = Stream.of(1, 2, 3)
    .gather(Gatherers.windowFixed(2))
    .toList();
w.get(1).add(4);
System.out.println(w);
```

- **A.** `[[1, 2], [3, 4]]`
- **B.** `[[1, 2], [3], [4]]`
- **C.** `[[1, 2], [3]]`
- **D.** An `UnsupportedOperationException` is thrown.
- **E.** An `IndexOutOfBoundsException` is thrown.

**Answer:** D

**Explanation:** The windows emitted by `windowFixed` and `windowSliding` are **unmodifiable** lists. `w.get(1)` is the window `[3]`; calling `add(4)` on it throws `UnsupportedOperationException`. (The outer list returned by `Stream.toList()` is unmodifiable too.)

## gap-gather-008

What is the output of the following code fragment? (Assume `java.util.*` and `java.util.stream.*` are imported.)

```java
var s = Stream.of("a", "b", "c", "d", "e")
    .peek(System.out::print)
    .gather(Gatherers.windowFixed(2));
System.out.print("|");
System.out.println(s.map(List::size).reduce(0, Integer::sum));
```

- **A.** `abcde|5`
- **B.** `abcde|3`
- **C.** `|abcde3`
- **D.** `|5`
- **E.** `|abcde5`

**Answer:** E

**Explanation:** `gather()` is an **intermediate** operation and, like `peek()` and `map()`, is lazy: nothing happens until the terminal operation `reduce` runs. So `|` is printed first. When `reduce` runs, `peek` prints every element `abcde`. The windows are `[a, b]`, `[c, d]`, `[e]`; mapping to sizes gives 2, 2, 1, and the sum is 5. Output: `|abcde5`. Option C sums the number of windows (3) instead of their sizes.

## gap-gather-009

Consider the following code fragment. (Assume `java.util.*` and `java.util.stream.*` are imported.)

```java
List<Integer> out = Stream.of(4, 1, 3, 2)
    .gather(Gatherers.mapConcurrent(4, x -> {
        try { Thread.sleep(x * 50); } catch (InterruptedException e) { }
        return x * 10;
    }))
    .toList();
System.out.println(out);
```

Which statement is true?

- **A.** It always prints `[10, 20, 30, 40]`, because results are emitted in completion order.
- **B.** The order of the printed elements is unpredictable.
- **C.** It always prints `[40, 10, 30, 20]`.
- **D.** The code does not compile because `Thread.sleep()` cannot be called in a lambda.
- **E.** An `IllegalArgumentException` is thrown because the concurrency level must be smaller than the number of elements.

**Answer:** C

**Explanation:** `Gatherers.mapConcurrent(maxConcurrency, mapper)` runs the mapping function concurrently on **virtual threads**, with at most `maxConcurrency` invocations in flight. Even though the element `1` finishes first, the gatherer **preserves encounter order** in its output, so the result is always `[40, 10, 30, 20]`.

`Thread.sleep()` is fine because the checked `InterruptedException` is caught inside the lambda. `maxConcurrency` only has to be at least 1; it may be larger than the number of elements.

## gap-gather-010

What is the output of the following code fragment? (Assume `java.util.*` and `java.util.stream.*` are imported.)

```java
var m = Stream.of(1, 2, 3, 4, 5, 6, 7)
    .gather(Gatherers.windowFixed(3))
    .collect(Collectors.partitioningBy(w -> w.size() == 3,
                                       Collectors.counting()));
System.out.println(m);
```

- **A.** `{false=1, true=2}`
- **B.** `{true=2, false=1}`
- **C.** `{false=0, true=2}`
- **D.** `{true=[[1, 2, 3], [4, 5, 6]], false=[[7]]}`
- **E.** The code does not compile because a `Stream<List<Integer>>` cannot be partitioned.

**Answer:** A

**Explanation:** `windowFixed(3)` turns the seven elements into the stream `[1, 2, 3]`, `[4, 5, 6]`, `[7]`. Gatherers combine freely with collectors: the element type is now `List<Integer>`, so the predicate `w -> w.size() == 3` is valid. Two windows are full and one is partial; with the downstream collector `counting()` the map values are `Long` counts.

`partitioningBy` always contains both keys, and its map prints `false` before `true`: `{false=1, true=2}`. D would be the result without the downstream collector.

## gap-gather-011

What is the output of the following code fragment? (Assume `java.util.*` and `java.util.stream.*` are imported.)

```java
int result = Stream.of(1, 2, 3, 4, 5)
    .gather(Gatherers.windowFixed(2))
    .gather(Gatherers.fold(() -> 0,
            (Integer acc, List<Integer> w) -> acc + w.get(0)))
    .findFirst().get();
System.out.println(result);
```

- **A.** `15`
- **B.** `4`
- **C.** `6`
- **D.** `9`
- **E.** The code does not compile because `gather()` cannot be called twice.

**Answer:** D

**Explanation:** Several `gather()` calls can be chained like any intermediate operation (you could also compose them first with `Gatherer.andThen`). The first gather produces the windows `[1, 2]`, `[3, 4]`, `[5]`. The `fold` adds up the **first element** of every window: 1 + 3 + 5 = 9. `fold` emits exactly one element, so `findFirst().get()` returns 9.

Option A sums all elements; option C ignores the last partial window.

## gap-gather-012

What is the output of the following code fragment? (Assume `java.util.*` and `java.util.stream.*` are imported.)

```java
Gatherer<Integer, ?, Integer> g = Gatherer.ofSequential(
    (Void state, Integer el, Gatherer.Downstream<? super Integer> down)
        -> el < 3 && down.push(el));
System.out.println(Stream.of(1, 2, 3, 1).gather(g).toList());
```

- **A.** `[1, 2, 1]`
- **B.** `[1, 2]`
- **C.** `[3]`
- **D.** `[1, 2, 3, 1]`
- **E.** The code does not compile.

**Answer:** B

**Explanation:** A custom gatherer is built from up to four functions: initializer, **integrator**, combiner and finisher. `Gatherer.ofSequential(integrator)` creates a stateless, sequential gatherer from just an integrator. The integrator receives the state (here unused, typed `Void`), the element and a `Downstream`; `down.push(el)` emits an element. The integrator's `boolean` return value tells the stream whether to **continue**.

For 1 and 2, `el < 3` is true and `push` returns true, so both are emitted. For 3, `el < 3` is false; because of `&&` nothing is pushed and the integrator returns `false`, which **short-circuits** the stream — the final `1` is never processed. This gatherer behaves like `takeWhile(el -> el < 3)`. Output: `[1, 2]`.

## gap-gather-013

Which statements are true? (Choose two.)

- **A.** `Stream.gather()` is a terminal operation.
- **B.** `Gatherers.scan()` emits the initial value as its first element.
- **C.** `Gatherers.windowSliding(4)` applied to a stream of two elements emits one window containing both elements.
- **D.** `Gatherers.fold()` emits exactly one element, even for an empty stream.
- **E.** `IntStream`, `LongStream` and `DoubleStream` also have a `gather()` method.
- **F.** Stream gatherers became a final (non-preview) feature in Java 22.

**Answer:** C, D

**Explanation:** C is true: a non-empty stream with fewer elements than the window size yields one window with all elements. D is true: `fold` always emits its final state, which is the initial value when there are no elements.

A is false: `gather()` is an intermediate operation and returns a `Stream`. B is false: `scan` emits the state after each element, never the initial value alone. E is false: only `Stream<T>` has `gather()`. F is false: gatherers were previewed in Java 22 and 23 (JEP 461, JEP 473) and finalized in **Java 24** (JEP 485).
