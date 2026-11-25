---
title: "Gap: Sequenced collections"
source: java25-gap
topic: d05-collections
---

# Gap: Sequenced collections (🆕 Java 21, JEP 431)

## gap-seq-001

What is the output of the following code fragment? (Assume `java.util.*` is imported.)

```java
List<Integer> list = new ArrayList<>(List.of(1, 2, 3));
List<Integer> rev = list.reversed();
rev.addFirst(0);
list.add(4);
System.out.println(list + " " + rev);
```

- **A.** `[0, 1, 2, 3, 4] [4, 3, 2, 1, 0]`
- **B.** `[1, 2, 3, 0, 4] [4, 0, 3, 2, 1]`
- **C.** `[1, 2, 3, 4] [0, 3, 2, 1]`
- **D.** `[1, 2, 3, 4] [3, 2, 1]`
- **E.** An `UnsupportedOperationException` is thrown.
- **F.** The code does not compile.

**Answer:** B

**Explanation:** 🆕 Java 21: `List.reversed()` returns a reverse-ordered **view** of the list, not a copy. Changes made through the view write through to the original list, and changes to the original are visible in the view.

`rev.addFirst(0)` adds at the front of the reversed view, which is the **end** of the original: `list` becomes `[1, 2, 3, 0]`. Then `list.add(4)` appends to the original, giving `[1, 2, 3, 0, 4]`. Reading the view back to front gives `[4, 0, 3, 2, 1]`. Option B is correct.

Option A would be right if `addFirst` on the view added at the front of the original. Options C and D assume `reversed()` makes an independent copy. The `ArrayList` is modifiable, so no `UnsupportedOperationException` occurs.

## gap-seq-002

Which of the following declarations compile? (Choose three.) (Assume `java.util.*` is imported.)

- **A.** `SequencedSet<String> s = new HashSet<>();`
- **B.** `SequencedCollection<Integer> c = new ArrayDeque<>();`
- **C.** `SequencedMap<String, Integer> m = new HashMap<>();`
- **D.** `SequencedSet<Integer> t = new TreeSet<>();`
- **E.** `SequencedCollection<String> q = new PriorityQueue<>();`
- **F.** `SequencedMap<String, Integer> m = new TreeMap<>();`

**Answer:** B, D, F

**Explanation:** 🆕 Java 21 retrofitted three interfaces into the collections hierarchy:

- `SequencedCollection` — superinterface of `List` and `Deque` (so `ArrayList`, `LinkedList`, `ArrayDeque` are sequenced).
- `SequencedSet` — extends `SequencedCollection` and `Set`; implemented by `LinkedHashSet` and extended by `SortedSet` (so `TreeSet` is sequenced).
- `SequencedMap` — extends `Map`; implemented by `LinkedHashMap` and extended by `SortedMap` (so `TreeMap` is sequenced).

Only collections with a **well-defined encounter order** are sequenced. `HashSet` and `HashMap` have no defined order, so A and C do not compile. `PriorityQueue` is a `Queue` but not a `Deque`; its iteration order is unspecified, so E does not compile. B, D and F compile.

## gap-seq-003

What is the output of the following code fragment? (Assume `java.util.*` is imported.)

```java
List<String> list = new ArrayList<>();
try { list.getFirst(); }
catch (RuntimeException e) { System.out.print(e.getClass().getSimpleName() + " "); }
try { list.get(0); }
catch (RuntimeException e) { System.out.print(e.getClass().getSimpleName()); }
```

- **A.** `IndexOutOfBoundsException IndexOutOfBoundsException`
- **B.** `NoSuchElementException NoSuchElementException`
- **C.** `NoSuchElementException IndexOutOfBoundsException`
- **D.** `IndexOutOfBoundsException` only, because `getFirst()` returns `null`
- **E.** The code does not compile because `getFirst()` is not defined on `List`.

**Answer:** C

**Explanation:** 🆕 Java 21: `getFirst()`, `getLast()`, `removeFirst()` and `removeLast()` are declared in `SequencedCollection` and therefore available on every `List`. On an empty collection they throw `java.util.NoSuchElementException` — the same behaviour as the identically named `Deque` methods.

`get(0)` is the old index-based method and still throws `IndexOutOfBoundsException` on an empty list. So the output is `NoSuchElementException IndexOutOfBoundsException`.

Note that none of the sequenced "get/remove" methods return `null` for an empty collection; if you want a `null` instead of an exception you still need `Deque.peekFirst()`/`pollFirst()` and friends.

## gap-seq-004

What is the output of the following code fragment? (Assume `java.util.*` is imported.)

```java
var set = new LinkedHashSet<>(List.of("a", "b", "c"));
set.addFirst("c");
set.addLast("a");
set.add("b");
System.out.println(set);
```

- **A.** `[a, b, c]`
- **B.** `[c, a, b]`
- **C.** `[c, b, a]`
- **D.** `[b, c, a]`
- **E.** An `UnsupportedOperationException` is thrown.

**Answer:** C

**Explanation:** 🆕 Java 21: `LinkedHashSet` implements `SequencedSet` and supports `addFirst()` and `addLast()`. If the element is **already present**, these methods **move** it to the requested end (sets cannot contain duplicates).

- start: `[a, b, c]`
- `addFirst("c")` moves `c` to the front: `[c, a, b]`
- `addLast("a")` moves `a` to the end: `[c, b, a]`
- `add("b")` — plain `add` of an existing element does nothing and does **not** change its position: `[c, b, a]`

The output is `[c, b, a]`.

## gap-seq-005

What is the output of the following code fragment? (Assume `java.util.*` is imported.)

```java
var m = new LinkedHashMap<String, Integer>();
m.put("x", 1); m.put("y", 2); m.put("z", 3);
m.putFirst("z", 30);
m.putLast("x", 10);
m.put("y", 20);
System.out.println(m);
```

- **A.** `{x=10, y=20, z=30}`
- **B.** `{z=30, x=10, y=20}`
- **C.** `{y=20, z=30, x=10}`
- **D.** `{z=30, y=20, x=10}`
- **E.** An `UnsupportedOperationException` is thrown.

**Answer:** D

**Explanation:** 🆕 Java 21: `LinkedHashMap` implements `SequencedMap`, which adds `putFirst(k, v)` and `putLast(k, v)`. If the key already exists, its value is replaced **and** the entry is moved to the front/end.

- start: `{x=1, y=2, z=3}`
- `putFirst("z", 30)`: `{z=30, x=1, y=2}`
- `putLast("x", 10)`: `{z=30, y=2, x=10}`
- `put("y", 20)`: an ordinary `put` of an existing key only replaces the value; in an insertion-ordered `LinkedHashMap` the position does not change: `{z=30, y=20, x=10}`

`putFirst`/`putLast` throw `UnsupportedOperationException` only on maps that determine their own order, such as `TreeMap`.

## gap-seq-006

Given the following declarations (assume `java.util.*` is imported), which statements are true? (Choose two.)

```java
List<String> list = new ArrayList<>();
Deque<String> deque = new ArrayDeque<>();
SequencedMap<String, Integer> map = new LinkedHashMap<>();
```

- **A.** `list.getFirst()` returns `null`.
- **B.** `deque.peekFirst()` returns `null`.
- **C.** `map.firstEntry()` throws a `NoSuchElementException`.
- **D.** `map.pollLastEntry()` returns `null`.
- **E.** `deque.getFirst()` returns `null`.
- **F.** `list.removeLast()` throws an `IndexOutOfBoundsException`.

**Answer:** B, D

**Explanation:** The rules for empty sequenced types are inconsistent on purpose, matching the existing APIs they were modelled on:

- `SequencedCollection.getFirst()/getLast()/removeFirst()/removeLast()` throw `NoSuchElementException` when empty (A, E and F are false — F names the wrong exception).
- `Deque.peekFirst()` (an old Java 6 method) returns `null` when empty (B is true).
- `SequencedMap.firstEntry()/lastEntry()/pollFirstEntry()/pollLastEntry()` follow the `NavigableMap` style and return `null` when the map is empty (C is false, D is true).

## gap-seq-007

What is the result of the following code fragment? (Assume `java.util.*` is imported.)

```java
List<Integer> nums = List.of(3, 1, 2);
System.out.print(nums.getLast() + " ");
System.out.print(nums.reversed() + " ");
nums.reversed().addFirst(9);
System.out.print(nums);
```

- **A.** It prints `2 [2, 1, 3] ` and then throws an `UnsupportedOperationException`.
- **B.** It prints `2 [2, 1, 3] [3, 1, 2, 9]`.
- **C.** It prints `3 [3, 2, 1] ` and then throws an `UnsupportedOperationException`.
- **D.** It throws an `UnsupportedOperationException` before printing anything, because `List.of()` lists cannot be reversed.
- **E.** The code does not compile.

**Answer:** A

**Explanation:** `List.of()` returns an unmodifiable list, but the read-only sequenced methods still work: `getLast()` returns `2` (the last element in encounter order, not the largest), and `reversed()` returns a reverse-ordered view `[2, 1, 3]`.

A reversed view of an unmodifiable list is itself unmodifiable. `addFirst(9)` on the view would modify the underlying list, so it throws `UnsupportedOperationException`. Option C confuses "reverse order" with "sorted descending".

## gap-seq-008

What is the result of the following code fragment? (Assume `java.util.*` is imported.)

```java
TreeSet<Integer> ts = new TreeSet<>(List.of(5, 1, 3));
System.out.print(ts.reversed().first() + " ");
System.out.print(ts.removeFirst() + " ");
ts.addLast(9);
System.out.print(ts);
```

- **A.** It prints `5 1 [3, 5, 9]`.
- **B.** It prints `1 1 [3, 5, 9]`.
- **C.** It prints `5 5 ` and then throws an `UnsupportedOperationException`.
- **D.** The code does not compile because `reversed()` returns a `SequencedSet`, which has no `first()` method.
- **E.** It prints `5 1 ` and then throws an `UnsupportedOperationException`.

**Answer:** E

**Explanation:** `TreeSet` is a `SortedSet`/`NavigableSet`, and since Java 21 `SortedSet` extends `SequencedSet`. `NavigableSet` overrides `reversed()` with the covariant return type `NavigableSet`, so `ts.reversed().first()` compiles and returns `5` (the first element of the descending view). D is wrong.

`removeFirst()` is supported on sorted sets (it behaves like `pollFirst()` but throws when empty) and removes and returns `1`.

`addFirst()` and `addLast()` are **not** supported on sorted collections, because the position of an element is determined by the comparator, not by the caller. `ts.addLast(9)` throws `UnsupportedOperationException`. The output is `5 1 ` followed by the exception.

## gap-seq-009

What is the result of the following code fragment? (Assume `java.util.*` is imported.)

```java
Map<String, Integer> m = new LinkedHashMap<>();
m.put("a", 1);
m.put("b", 2);
System.out.println(m.firstEntry());
```

- **A.** `a=1`
- **B.** `b=2`
- **C.** `null`
- **D.** The code does not compile.
- **E.** An `UnsupportedOperationException` is thrown at runtime.

**Answer:** D

**Explanation:** `firstEntry()` is declared in `SequencedMap` (and `NavigableMap`), not in `Map`. The compiler only looks at the **reference type**, which is `Map<String, Integer>`, so `m.firstEntry()` does not compile even though the object is a `LinkedHashMap`. Declaring the variable as `SequencedMap<String, Integer>`, `LinkedHashMap<String, Integer>` or using `var` would make it compile and print `a=1`.

## gap-seq-010

What is the output of the following code fragment? (Assume `java.util.*` is imported.)

```java
Deque<Integer> dq = new ArrayDeque<>(List.of(1, 2, 3));
Deque<Integer> rd = dq.reversed();
rd.push(0);
rd.offerFirst(-1);
System.out.println(dq + " " + rd.peek());
```

- **A.** `[-1, 0, 1, 2, 3] -1`
- **B.** `[1, 2, 3] -1`
- **C.** `[1, 2, 3, 0, -1] -1`
- **D.** `[1, 2, 3, 0, -1] 1`
- **E.** `[1, 2, 3, -1, 0] 0`

**Answer:** C

**Explanation:** `Deque.reversed()` (🆕 Java 21) returns a `Deque` view in reverse order. All operations on the view are mapped to the opposite end of the original:

- `rd.push(0)` is `rd.addFirst(0)`, which is `dq.addLast(0)`: `[1, 2, 3, 0]`
- `rd.offerFirst(-1)` is `dq.offerLast(-1)`: `[1, 2, 3, 0, -1]`
- `rd.peek()` is `rd.peekFirst()`, which is `dq.peekLast()`: `-1`

The view is not a copy, so B is wrong.

## gap-seq-011

What is the output of the following code fragment? (Assume `java.util.*` is imported.)

```java
List<String> base = new ArrayList<>(List.of("a", "b", "c", "d"));
List<String> view = base.reversed();
view.set(0, "X");
view.remove("b");
base.removeFirst();
System.out.println(base + " " + view);
```

- **A.** `[c, X] [X, c]`
- **B.** `[X, c] [c, X]`
- **C.** `[b, c, d] [X, c, b]`
- **D.** `[c, d] [X, c, b]`
- **E.** A `ConcurrentModificationException` is thrown.

**Answer:** A

**Explanation:** The reversed view is fully read-write for a modifiable list.

- `view` is `[d, c, b, a]`; `view.set(0, "X")` replaces index 0 **of the view**, which is the last element of `base`: `base = [a, b, c, X]`.
- `view.remove("b")` removes `b` from the underlying list: `base = [a, c, X]`.
- `base.removeFirst()` removes `a`: `base = [c, X]`.

The view always reflects the current state of `base` in reverse: `[X, c]`. Modifying the list directly is not a problem here because no iterator is in use, so no `ConcurrentModificationException` occurs.

## gap-seq-012

What is the result of the following code fragment? (Assume `java.util.*` is imported.)

```java
var m = new LinkedHashMap<String, Integer>();
m.put("k1", 1);
m.put("k2", 2);
var e = m.firstEntry();
e.setValue(99);
System.out.println(m);
```

- **A.** `{k1=99, k2=2}`
- **B.** `{k1=1, k2=2}`
- **C.** `{k2=2, k1=99}`
- **D.** An `UnsupportedOperationException` is thrown.
- **E.** The code does not compile.

**Answer:** D

**Explanation:** `SequencedMap.firstEntry()`, `lastEntry()`, `pollFirstEntry()` and `pollLastEntry()` return **unmodifiable snapshot** entries (like the `NavigableMap` methods of the same names). Calling `setValue()` on such an entry throws `UnsupportedOperationException`. To change the value, call `m.put("k1", 99)` or modify the entry obtained from `m.entrySet()` instead.

## gap-seq-013

What is the output of the following code fragment? (Assume `java.util.*` is imported.)

```java
var map = new LinkedHashMap<String, Integer>();
map.put("one", 1); map.put("two", 2); map.put("three", 3);
var rev = map.reversed();
rev.putFirst("four", 4);
rev.remove("one");
System.out.println(map.keySet() + " " + rev.firstEntry().getKey());
```

- **A.** `[four, two, three] four`
- **B.** `[two, three, four] three`
- **C.** `[two, three] three`
- **D.** `[two, three, four] four`
- **E.** An `UnsupportedOperationException` is thrown.

**Answer:** D

**Explanation:** `SequencedMap.reversed()` returns a reverse-ordered **view** of the map. `rev.putFirst("four", 4)` puts the mapping at the front of the view, which is the **end** of `map`. `rev.remove("one")` removes from the underlying map. So `map` is `{two=2, three=3, four=4}` and its key set prints `[two, three, four]`.

The first entry of the reversed view is the last entry of `map`, whose key is `four`.

## gap-seq-014

Which statements are true? (Choose two.)

- **A.** `SequencedCollection` was added in Java 21 and is a superinterface of both `List` and `Deque`.
- **B.** `HashSet` implements `SequencedSet` because `LinkedHashSet` extends it.
- **C.** Since Java 21, `SortedSet` extends `SequencedSet`.
- **D.** `SequencedMap` extends `SequencedCollection`.
- **E.** `List.reversed()` returns a new, independent copy of the list.
- **F.** `LinkedHashMap.keySet()` has the return type `SequencedSet<K>`.

**Answer:** A, C

**Explanation:** A and C are true: `List` and `Deque` both extend `SequencedCollection`, and `SortedSet` extends `SequencedSet` (likewise `SortedMap` extends `SequencedMap`).

B reverses the inheritance: `LinkedHashSet extends HashSet implements SequencedSet` — the superclass `HashSet` is not sequenced. D is false: `SequencedMap` extends `Map`, and maps are not collections. E is false: `reversed()` returns a view. F is false: `keySet()` still returns `Set<K>` for compatibility; the new method `sequencedKeySet()` returns a `SequencedSet<K>` (similarly `sequencedValues()` and `sequencedEntrySet()`).

## gap-seq-015

Given the following declarations (assume `java.util.*` is imported), which statements throw an `UnsupportedOperationException`? (Choose two.)

```java
var lhs = new LinkedHashSet<>(List.of(1, 2, 3));
var lhm = new LinkedHashMap<>(Map.of("k", 1));
var tm  = new TreeMap<String, Integer>();
```

- **A.** `lhs.addFirst(0);`
- **B.** `lhs.reversed().addLast(0);`
- **C.** `lhm.sequencedKeySet().addFirst("z");`
- **D.** `lhm.putFirst("z", 0);`
- **E.** `tm.putLast("z", 0);`
- **F.** `tm.pollFirstEntry();`

**Answer:** C, E

**Explanation:** A, B and D are normal sequenced operations on modifiable insertion-ordered collections (B adds `0` at the **front** of `lhs`, through the reversed view).

C throws: the key set of a map is a view that supports removal but never **adding** — there is no value to put with a new key. This is true for `keySet()` as well as `sequencedKeySet()`.

E throws: `TreeMap` keeps its keys sorted, so the caller cannot choose a position; `putFirst`/`putLast` are unsupported on sorted maps.

F is fine: `pollFirstEntry()` on an empty `TreeMap` simply returns `null`.
