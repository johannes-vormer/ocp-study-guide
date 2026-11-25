---
title: "D5 — Arrays and Collections"
topic: d05-collections
order: 5
---

# D5 — Arrays and Collections

## What the exam tests

- Declaring, creating and initialising arrays (one- and multi-dimensional), default values, `length`.
- `Arrays` helpers: `sort`, `binarySearch`, `equals`, `compare`, `mismatch`, `asList`, `fill`, `copyOf`.
- Which `List` / `Set` / `Queue` / `Deque` / `Map` implementation allows `null`, keeps order, sorts, or rejects duplicates.
- Unmodifiable collections: `List.of` / `Set.of` / `Map.of` / `copyOf`, `Arrays.asList` (fixed-size) and `Collections.unmodifiable*`, plus the exception each one throws.
- `Map` default methods: `putIfAbsent`, `merge`, `compute*`, `getOrDefault`, `replace*`, iteration with `entrySet()`.
- Sorting with `Comparable` and `Comparator` (`comparing`, `thenComparing`, `reversed`, `nullsFirst`), `Collections.sort` and `binarySearch`.
- Generics: diamond, wildcards (`?`, `? extends`, `? super`), bounded type parameters, type erasure, and the compile errors they cause.
- 🆕 **Sequenced collections** (Java 21) and 🆕 `HashMap.newHashMap` and related factories (Java 19).

Many collection questions are really "does it compile?" or "which exception?" questions. When you read any snippet, check three things: (1) does the static type have this method, (2) is the collection modifiable, (3) are nulls allowed.

---

## 1. Arrays

### Declarations

| Code | Compiles? |
|---|---|
| `int[] a = new int[3];` | ✅ |
| `int a[] = new int[3];` / `int []a` | ✅ brackets can go after the type or after the name |
| `int[] a = {1, 2, 3};` | ✅ array initializer (only allowed in a declaration) |
| `int[] a = new int[]{1, 2,};` | ✅ trailing comma is fine, length 2 |
| `int[] a = new int[];` | ❌ dimension missing |
| `int[] a = new int[2]{1, 2};` | ❌ size and initializer together |
| `int[] a; a = {1, 2};` | ❌ a bare `{}` is only allowed in a declaration (use `new int[]{1,2}`) |
| `var a = {1, 2};` | ❌ `var` cannot infer from a bare initializer |
| `var a = new int[]{1, 2};` | ✅ |
| `int a[] = {}, b = 3;` | ✅ `a` is `int[]`, `b` is `int` |
| `int[] c, d;` | both are `int[]` |
| `int[] x, y[];` | `x` is `int[]`, `y` is `int[][]` |
| `new int[2L]` | ❌ the size must be `int` (a `char`/`short`/`byte` size is fine) |
| `new int[-1]` | compiles, throws `NegativeArraySizeException` |

### Default values

Array elements are always initialised, even for local arrays:
`0` for numeric types, `'\u0000'` for `char`, `false` for `boolean`, `null` for references.

### Multi-dimensional arrays

```java
int[][] a = new int[3][];       // OK: only the first dimension is needed
int[] b[] = {{1}, {2, 3}};      // ragged ("jagged") array, b[1].length == 2
int[][] c = new int[][3];       // DOES NOT COMPILE
int[][] m = new int[2][];
System.out.println(m[0]);       // null: the inner arrays do not exist yet
```

### Array facts that show up in traps

- `length` is a **field** on arrays (`arr.length`), a **method** on `String` (`s.length()`), and collections use `size()`.
- Arrays are **covariant**: `Object[] o = new String[1];` compiles, but `o[0] = 1;` throws `ArrayStoreException` at runtime.
- A primitive array is not an `Object[]`: `Object[] q = new int[1];` does not compile, but `Object p = new int[1];` does.
- No widening between array types: `long[] l = new int[1];` does not compile. No auto-boxing either: `int[] p = new Integer[1];` does not compile.
- `arr.equals(other)` is the `Object` reference comparison. Use `Arrays.equals` for one level and `Arrays.deepEquals` for nested arrays.
- `System.out.println(arr)` prints something like `[I@1b6d3586`. Use `Arrays.toString` or `Arrays.deepToString` instead.

### `java.util.Arrays`

| Method | Behaviour |
|---|---|
| `sort(arr)` | ascending. Strings sort by Unicode: **digits < uppercase < lowercase**: `{"b","A","a","B","10","9"}` → `[10, 9, A, B, a, b]` |
| `sort(T[], Comparator)` | object arrays only. `Arrays.sort(intArr, Collections.reverseOrder())` does not compile; you need `Integer[]` |
| `binarySearch(arr, key)` | the array **must already be sorted** (otherwise the result is undefined). Found → the index. Not found → `-(insertionPoint) - 1` |
| `equals(a, b)` | same length and equal elements in order |
| `compare(a, b)` | lexicographic. Negative if `a` is "smaller", `0` if equal, positive otherwise. A proper prefix is smaller |
| `mismatch(a, b)` | the first index where they differ. `-1` if they are equal. If one is a prefix of the other, the shorter length |
| `fill`, `copyOf(arr, newLen)`, `copyOfRange(arr, from, toExcl)` | `copyOf` pads with default values |
| `asList(T...)` | a **fixed-size** `List` backed by the array (see section 3) |

```java
int[] bs = {2, 4, 6, 8};
Arrays.binarySearch(bs, 4);   //  1
Arrays.binarySearch(bs, 5);   // -3   (would be inserted at index 2 → -2-1)
Arrays.binarySearch(bs, 1);   // -1   (insertion point 0)
Arrays.binarySearch(bs, 9);   // -5   (insertion point 4)

Arrays.compare(new int[]{1,2}, new int[]{1,2,3});  // -1  (prefix is smaller)
Arrays.compare(new int[]{1,3}, new int[]{1,2,3});  //  1  (3 > 2 at index 1)
Arrays.mismatch(new int[]{1,2}, new int[]{1,2});   // -1
Arrays.mismatch(new int[]{1,2}, new int[]{1,3});   //  1
Arrays.mismatch(new int[]{1,2}, new int[]{1,2,3}); //  2
```

`compare` and `mismatch` give **different** answers for equal arrays: `compare` returns `0`, `mismatch` returns `-1`. Remember the rule as **compare equal = 0, mismatch equal = -1**.

`Arrays.compare` on `String[]` uses `String.compareTo`, so only the sign is reliable: `Arrays.compare(new String[]{"a"}, new String[]{"B"})` is positive because `'a'` (97) > `'B'` (66).

---

## 2. The Collections Framework at a glance

```
Iterable
 └─ Collection
     ├─ SequencedCollection (21)
     │   ├─ List ............ ArrayList, LinkedList
     │   ├─ Deque (also a Queue) ArrayDeque, LinkedList
     │   └─ SequencedSet (21)
     │       ├─ LinkedHashSet
     │       └─ SortedSet → NavigableSet ... TreeSet
     ├─ Set ................ HashSet (not sequenced)
     └─ Queue .............. PriorityQueue (not sequenced)
Map (not a Collection!)
 ├─ HashMap, Hashtable
 └─ SequencedMap (21)
     ├─ LinkedHashMap
     └─ SortedMap → NavigableMap ... TreeMap
```

### Implementation behaviour

| Class | Order | Duplicates | `null` | Notes |
|---|---|---|---|---|
| `ArrayList` | insertion / index | yes | yes | fast random access |
| `LinkedList` | insertion / index | yes | yes | implements **both** `List` and `Deque` |
| `ArrayDeque` | head → tail | yes | **no** (NPE) | best stack/queue |
| `PriorityQueue` | head is the smallest (natural/comparator); iteration order not sorted | yes | **no** (NPE) | |
| `HashSet` | none guaranteed | no | one `null` | uses `hashCode`/`equals` |
| `LinkedHashSet` | insertion | no | one `null` | |
| `TreeSet` | sorted | no | **no** (NPE) | elements must be `Comparable` or you must pass a `Comparator`. Otherwise `ClassCastException` on `add` |
| `HashMap` | none guaranteed | unique keys | one `null` key, `null` values | |
| `LinkedHashMap` | insertion (or access order) | unique keys | `null` key/values | |
| `TreeMap` | sorted by key | unique keys | **no `null` key** (NPE); `null` values OK | |
| `Hashtable`, `ConcurrentHashMap` | none | unique keys | **no nulls at all** | |

Adding a non-`Comparable` object to a `TreeSet` compiles (`TreeSet<Object>`) but throws `ClassCastException`. This happens even for the **first** element, because the set compares it with itself.

### Common `Collection`/`List` methods and their return values

| Call | Returns |
|---|---|
| `add(e)` | `boolean`. `Set.add` returns `false` for a duplicate |
| `remove(Object)` | `boolean` |
| `List.remove(int index)` | the removed element |
| `List.set(i, e)` | the **old** element |
| `Map.put(k, v)` | the previous value or `null` |
| `isEmpty()`, `size()`, `contains`, `clear()` | |
| `removeIf(Predicate)`, `replaceAll(UnaryOperator)` (List), `forEach(Consumer)` | |

**`remove(int)` vs `remove(Object)` on `List<Integer>`:**

```java
List<Integer> li = new ArrayList<>(List.of(1, 2, 3));
li.remove(1);                    // removes the element at INDEX 1 → [1, 3]
li.remove(Integer.valueOf(1));   // removes the VALUE 1 → [3]
```

- `ArrayList` index errors (`get(0)` on an empty list, `add(5, x)` past the end) throw `IndexOutOfBoundsException`.
- `new ArrayList<>(-1)` throws `IllegalArgumentException`. The initial capacity is **not** the size: `new ArrayList<>(1)` happily grows.
- Modifying a list inside a for-each loop over that same list typically throws `ConcurrentModificationException`. Use `Iterator.remove()` or `removeIf` instead.
- `subList(from, toExcl)` is a **view**: `list.subList(1,3).clear()` removes those elements from the original list.
- `equals` between collections follows the interface contract: `new ArrayList<>(List.of(1)).equals(new LinkedList<>(List.of(1)))` is `true`, and so is `HashSet.equals(TreeSet)` with the same elements.

### Queue and Deque method families

| Operation | Throws on failure | Returns special value |
|---|---|---|
| insert at tail | `add(e)` | `offer(e)` → `false` |
| remove head | `remove()` → `NoSuchElementException` | `poll()` → `null` |
| look at head | `element()` → `NoSuchElementException` | `peek()` → `null` |

`Deque` adds `First`/`Last` variants (`addFirst`, `offerLast`, `pollFirst`, `peekLast`, …). Its **stack** methods all work on the **front**: `push` = `addFirst`, `pop` = `removeFirst` (throws when the deque is empty), `peek` = `peekFirst`.

```java
Deque<Integer> dq = new ArrayDeque<>();
dq.push(1); dq.push(2); dq.offer(3);   // [2, 1, 3]   push → front, offer → back
dq.peek();  // 2
dq.pop();   // 2  → [1, 3]
```

### `TreeSet` / `TreeMap` navigation

```java
TreeSet<Integer> ts = new TreeSet<>(List.of(10, 20, 30));
ts.lower(20)   // 10   strictly less
ts.floor(20)   // 20   ≤
ts.ceiling(21) // 30   ≥
ts.higher(30)  // null strictly greater, none → null
ts.headSet(20) // [10]       exclusive by default
ts.tailSet(20) // [20, 30]   inclusive by default
ts.descendingSet() // [30, 20, 10]
```

`TreeMap` has the same family: `floorKey`, `ceilingEntry`, `headMap(k)` (exclusive), `headMap(k, true)`, `tailMap(k)` (inclusive), `descendingMap()`.
`first()`/`firstKey()` on an empty tree throw `NoSuchElementException`. `pollFirst()`/`firstEntry()` return `null`.

---

## 3. Unmodifiable and fixed-size collections

| Created by | `add`/`remove` | `set`/`sort`/`replaceAll` | `null` elements | Reflects source changes? |
|---|---|---|---|---|
| `Arrays.asList(arr)` | **UOE** (fixed size) | ✅ allowed (writes through to the array) | allowed | **yes**, both directions |
| `List.of(...)` / `Set.of` / `Map.of` | UOE | UOE | **NPE** on creation | n/a (a copy of the arguments) |
| `List.copyOf(c)` / `Set.copyOf` / `Map.copyOf` | UOE | UOE | **NPE** on creation | **no**: a snapshot |
| `Collections.unmodifiableList(l)` | UOE | UOE | allowed | **yes**: a read-only *view* |
| `stream.toList()` | UOE | UOE | allowed | no |
| `Collections.emptyList()`, `singletonList(x)` | UOE | UOE | | |

UOE = `UnsupportedOperationException`.

```java
String[] sa = {"b", "a"};
List<String> as = Arrays.asList(sa);
sa[0] = "z";        // as  → [z, a]
as.set(1, "y");     // sa  → [z, y]
as.add("q");        // UnsupportedOperationException
Collections.sort(as);   // fine: sort only uses set()
```

```java
List<String> src = new ArrayList<>(List.of("a"));
List<String> copy = List.copyOf(src);
List<String> view = Collections.unmodifiableList(src);
src.add("b");
System.out.println(copy + " " + view);   // [a] [a, b]
```

Rules for the `of`/`copyOf` factories:

- `List.of("a", null)` → `NullPointerException`. Even `List.of("a").contains(null)` throws NPE, and so do `Map.of(1,1).get(null)` and `Set.of(1).contains(null)`.
- `Set.of("a", "a")` → `IllegalArgumentException` (duplicate element). `Map.of("a",1,"a",2)` → IAE (duplicate key).
- But `Set.copyOf(List.of(1, 1, 2))` is fine: duplicates are silently removed.
- `Map.of` takes at most **10** key/value pairs. For more, use `Map.ofEntries(Map.entry(k, v), ...)`. `Map.entry(null, 1)` throws NPE.
- Mutators throw UOE **even when they would not change anything**: `List.of(1,2).sort(null)` (already sorted), `List.of().clear()`, and `List.of("a").remove("zz")` all throw.
- `List.of(array)` copies the array: later changes to the array are **not** visible (unlike `Arrays.asList`).
- Iteration order of `Set.of`/`Map.of` is unspecified and may change between runs.
- `Arrays.asList(intArray)` gives a `List<int[]>` of size **1**. `Arrays.asList(1, 2)` gives `List<Integer>` of size 2.

---

## 4. The `Map` API

```java
Map<String,Integer> m = new HashMap<>();
m.put("a", null);
m.putIfAbsent("a", 1);       // returns null; "a" had a null value → now a=1
```

| Method | Key absent (or value `null`) | Key present (non-null value) | Function returns `null` |
|---|---|---|---|
| `putIfAbsent(k, v)` | puts `v`, returns `null` | no change, returns the current value | — |
| `getOrDefault(k, d)` | absent → `d`. **Present with a `null` value → `null`, not `d`** | the value | — |
| `merge(k, v, (old,v)->…)` | puts `v` (the function is **not** called) | puts `f(old, v)` | **removes** the key |
| `computeIfAbsent(k, k->…)` | puts the result | no change, returns the current value | nothing is stored |
| `computeIfPresent(k, (k,v)->…)` | nothing, returns `null` | puts the result | **removes** the key |
| `compute(k, (k,v)->…)` | called with `v == null` | called with the current `v` | removes the key / does not add it |
| `replace(k, v)` | nothing, returns `null` | replaces, returns the old value | — |
| `replaceAll((k,v)->…)` | applies to every entry | | |

```java
Map<String,Integer> m = new HashMap<>();
m.merge("b", 5, Integer::sum);     // 5     (absent → just put)
m.merge("b", 5, Integer::sum);     // 10
m.merge("b", 5, (o, n) -> null);   // null  → "b" removed
m.computeIfAbsent("d", k -> 4);    // 4
m.computeIfAbsent("d", k -> 99);   // 4     (already present)
m.computeIfPresent("d", (k, v) -> null);  // "d" removed
m.compute("f", (k, v) -> v == null ? 1 : v + 1);  // 1, then 2 on the next call
```

Iteration:

```java
for (Map.Entry<String,Integer> e : map.entrySet())
    e.setValue(e.getValue() * 2);          // allowed on HashMap/TreeMap entries
map.forEach((k, v) -> System.out.println(k + v));   // BiConsumer
map.keySet(); map.values();                // views: keySet().remove(k) removes the mapping
map.keySet().add("x");                     // UnsupportedOperationException
```

- `Map` is **not** a `Collection`. It has no `add`, no `iterator()`, and no `stream()`. Use `map.entrySet().stream()`.
- `containsKey`/`containsValue` exist. `contains` does **not**.
- Entries from `Map.of(...)`, `Map.entry(...)` and `firstEntry()` are immutable: `setValue` → UOE.

---

## 5. Sorting: `Comparable` vs `Comparator`

| | `Comparable<T>` | `Comparator<T>` |
|---|---|---|
| Package | `java.lang` | `java.util` |
| Method | `int compareTo(T o)` | `int compare(T a, T b)` |
| Where | implemented by the class itself ("natural order") | separate object/lambda |
| Used by | `Collections.sort(list)`, `TreeSet()`, `list.sort(null)`, `sorted()` | `list.sort(c)`, `new TreeSet<>(c)`, `sorted(c)` |

The contract: negative means "this/a comes first", 0 means equal, positive means "comes after". Keep `compareTo` consistent with `equals`. Otherwise a `TreeSet` treats objects that compare as 0 as duplicates.

`String.compareTo` returns the difference of the first differing chars (or of the lengths): `"A".compareTo("a")` = `-32`. Only rely on the sign unless the question asks for the exact number.

### Building comparators

```java
Comparator.comparing(String::length)                 // key extractor (natural order of the key)
Comparator.comparing(String::length, Comparator.reverseOrder())   // key + key comparator
Comparator.comparingInt(String::length)              // avoids boxing
    .thenComparing(Comparator.reverseOrder())        // tie-breaker
    .thenComparing(s -> s)                           // thenComparing(Function) also exists
Comparator.<String>naturalOrder(); Comparator.reverseOrder();
Comparator.nullsFirst(Comparator.naturalOrder());    // nulls before everything else
```

```java
List<String> l = new ArrayList<>(List.of("bb", "a", "ccc", "dd"));
l.sort(Comparator.comparing(String::length).thenComparing(Comparator.reverseOrder()));
// [a, dd, bb, ccc]
l.sort(Comparator.comparing(String::length).reversed());
// [ccc, dd, bb, a]   reversed() reverses the WHOLE chain built so far
```

Traps:

- `Comparator.comparing(s -> s.length()).reversed()` **does not compile** when the target is `Comparator<String>`. Once `.reversed()` is chained, the lambda's parameter type cannot be inferred, so it becomes `Object`. Fix it with `String::length`, `(String s) -> s.length()`, or `Comparator.<String,Integer>comparing(...)`. Without the chained call, `Comparator<String> c = Comparator.comparing(s -> s.length());` compiles.
- `nullsLast(...).reversed()` puts the nulls **first**, because `reversed()` flips the null handling too.
- Sorting a list that contains `null` with natural order → `NullPointerException`. Comparing on a key that is `null` → NPE unless you wrap the key comparator in `nullsFirst`/`nullsLast`.
- `list.sort(null)` means "natural order".

### `Collections` utilities

```java
List<String> l = new ArrayList<>(List.of("c", "a", "b"));
Collections.sort(l);                    // [a, b, c]
Collections.binarySearch(l, "b");       // 1
Collections.binarySearch(l, "bb");      // -3
Collections.sort(l, Comparator.reverseOrder());          // [c, b, a]
Collections.binarySearch(l, "a", Comparator.reverseOrder()); // 2
Collections.binarySearch(l, "a");       // UNDEFINED: wrong ordering for a natural search
```

- `binarySearch` must be given a list sorted in the **same order** it searches with. When the orders differ, the exam's answer is "the result is undefined / unpredictable".
- Also: `reverse`, `shuffle`, `max`, `min` (both throw `NoSuchElementException` on an empty collection), `frequency`, `nCopies`, `addAll(coll, e...)`, `emptyList`, `singletonList`, `unmodifiableList/Set/Map`, and 🆕 `unmodifiableSequencedCollection/Set/Map` (Java 21).
- `Collections.sort(List.of(2, 1))` → UOE.

---

## 6. Generics

### Basics

- Generics are compile-time only. **Type erasure** replaces `T` with its bound (`Object` by default) in the bytecode.
- Type arguments must be reference types: `List<int>` does not compile.
- Diamond: `new ArrayList<>()` infers the arguments from the target. `var list = new ArrayList<>();` infers `ArrayList<Object>`, so `String s = list.get(0);` does not compile. Diamond also works with anonymous classes: `new ArrayList<>(){}` compiles.
- Generic methods declare the type parameter before the return type: `static <T> void s(T t)`. Call them with an explicit witness as `ClassName.<String>s("x")` or `this.<String>s("x")`. A bare `<String>s("x")` does not compile.
- Naming conventions: `E` element, `K` key, `V` value, `T`/`U` types, `N` number.

### What erasure forbids

| Code | Result |
|---|---|
| `void m(List<String> a)` + `void m(List<Integer> a)` | ❌ name clash: same erasure |
| `new T()`, `new T[1]` | ❌ |
| `new List<String>[2]` | ❌ generic array creation. `new List<?>[2]` ✅ |
| `static T field;` in `class X<T>` | ❌ a static member cannot use the class's type parameter |
| `o instanceof List<String>` (with `o` an `Object`) | ❌ cannot be checked safely. `instanceof List<?>` ✅. `List<Integer> l; l instanceof ArrayList<Integer>` ✅ (the cast is checkable) |
| `class E<T> extends Exception` | ❌ a generic class may not extend `Throwable` |
| Raw types (`List r = new ArrayList<String>(); r.add(1);`) | ✅ compiles with an unchecked warning |

### Wildcards and PECS

Generic types are **invariant**: `List<Number> l = new ArrayList<Integer>();` does **not** compile, even though `Integer` is a `Number`. The same goes for `List<Object> = new ArrayList<String>()`. (Arrays are covariant, generics are not.)

| Declaration | Can assign | Read as | Can `add` |
|---|---|---|---|
| `List<?>` | any `List` | `Object` | only `null` |
| `List<? extends Number>` | `List<Integer>`, `List<Double>`, `List<Number>` | `Number` | only `null` |
| `List<? super Integer>` | `List<Integer>`, `List<Number>`, `List<Object>` | `Object` | `Integer` (and subtypes) |

**PECS — Producer `extends`, Consumer `super`.** Use `? extends T` when you only read from the collection, and `? super T` when you only write to it.

```java
List<? extends Number> ext = new ArrayList<Integer>();
ext.add(1);              // DOES NOT COMPILE
Number n = ext.get(0);   // OK

List<? super Integer> sup = new ArrayList<Number>();
sup.add(1);              // OK
Integer i = sup.get(0);  // DOES NOT COMPILE: you only get Object back
```

### Bounded type parameters

```java
class B<T extends Number & Comparable<T>> {}   // class bound first, then interfaces with &
class C<T extends Comparable<T> & Number> {}   // DOES NOT COMPILE: the class must come first
<T extends Number> double sum(List<T> l)       // bounded method type parameter
```

Wildcards (`?`) are only allowed in **type uses** (variables, parameters, return types). You cannot write `class X<?>` or `new ArrayList<?>()`. Type parameters (`T`) are declared on classes, interfaces and methods.

---

## 🆕 Since Java 17

### 🆕 Java 21 — Sequenced collections (JEP 431)

Three new interfaces give every collection with a **defined encounter order** one uniform API for its first and last elements.

```java
interface SequencedCollection<E> extends Collection<E> {
    SequencedCollection<E> reversed();
    void addFirst(E e);  void addLast(E e);
    E getFirst();        E getLast();
    E removeFirst();     E removeLast();
}
interface SequencedSet<E> extends SequencedCollection<E>, Set<E> {
    SequencedSet<E> reversed();
}
interface SequencedMap<K,V> extends Map<K,V> {
    SequencedMap<K,V> reversed();
    Map.Entry<K,V> firstEntry();      Map.Entry<K,V> lastEntry();
    Map.Entry<K,V> pollFirstEntry();  Map.Entry<K,V> pollLastEntry();
    V putFirst(K k, V v);             V putLast(K k, V v);
    SequencedSet<K> sequencedKeySet();
    SequencedCollection<V> sequencedValues();
    SequencedSet<Map.Entry<K,V>> sequencedEntrySet();
}
```

**Who implements what** (verified with `instanceof`):

| Type | SequencedCollection | SequencedSet | SequencedMap |
|---|---|---|---|
| `List` (`ArrayList`, `LinkedList`, `List.of`, `Arrays.asList`, `CopyOnWriteArrayList`) | ✅ | | |
| `Deque` (`ArrayDeque`, `LinkedList`) | ✅ | | |
| `LinkedHashSet`, `SortedSet`/`NavigableSet` (`TreeSet`, `ConcurrentSkipListSet`) | ✅ | ✅ | |
| `LinkedHashMap`, `SortedMap`/`NavigableMap` (`TreeMap`, `ConcurrentSkipListMap`) | | | ✅ |
| `HashSet`, `Set.of`, `PriorityQueue`, `HashMap`, `Map.of` | ❌ | ❌ | ❌ |

`Map` types are never `SequencedCollection`s, but `LinkedHashMap.keySet()` is a `SequencedSet`.
`SequencedCollection` has **no** `get(int)`: `SequencedCollection<Integer> s = new ArrayList<>(); s.get(0);` does not compile.

**`reversed()` returns a view with a covariant type:**

| Receiver | `reversed()` static type |
|---|---|
| `List` | `List` |
| `Deque` | `Deque` |
| `NavigableSet` / `TreeSet` | `NavigableSet` |
| `LinkedHashSet` | `SequencedSet` (not `LinkedHashSet`: `LinkedHashSet<Integer> x = lhs.reversed();` does not compile) |
| `NavigableMap` / `TreeMap` | `NavigableMap` |
| `LinkedHashMap` | `SequencedMap` |

The view is **live** in both directions:

```java
List<Integer> l = new ArrayList<>(List.of(1, 2, 3));
List<Integer> r = l.reversed();   // [3, 2, 1]
r.set(0, 99);                     // l → [1, 2, 99]
l.add(7);                         // r → [7, 99, 2, 1]
```

**Behaviour you must know:**

```java
List<Integer> l = new ArrayList<>(List.of(1, 2, 3));
l.addFirst(0); l.addLast(4);         // [0, 1, 2, 3, 4]
l.getFirst();  l.getLast();          // 0, 4
l.removeFirst(); l.removeLast();     // returns 0, 4 → [1, 2, 3]

new ArrayList<>().getFirst();        // NoSuchElementException (NOT IndexOutOfBounds)
new ArrayList<>().removeFirst();     // NoSuchElementException
```

| Situation | Result |
|---|---|
| `getFirst/getLast/removeFirst/removeLast` on an empty collection | `NoSuchElementException` |
| `List.of(1).addFirst(0)`, `List.of(1).reversed().add(3)` | `UnsupportedOperationException` (immutable) |
| `List.of(1, 2).getFirst()`, `List.of(1,2).reversed()` | fine (reading is allowed) → `1`, `[2, 1]` |
| `Arrays.asList(1, 2).addFirst(0)` | UOE (fixed size). But `Arrays.asList(1,2).reversed().set(0, 9)` works and writes through → `[1, 9]` |
| `TreeSet.addFirst/addLast`, `TreeMap.putFirst/putLast` | **always** UOE: the position is decided by sorting, so it cannot be dictated |
| `Collections.unmodifiableSequencedCollection(...).addFirst(x)` | UOE |
| `firstEntry()` / `pollFirstEntry()` on an empty map | `null` (not an exception) |
| `firstEntry().setValue(...)` | UOE: it returns an immutable snapshot entry |

**`LinkedHashSet.addFirst/addLast` move an existing element:**

```java
LinkedHashSet<String> s = new LinkedHashSet<>(List.of("a", "b", "c"));
s.addFirst("c");   // [c, a, b]     "c" is relocated, not duplicated
s.addLast("a");    // [c, b, a]
s.addFirst("z");   // [z, c, b, a]
```

**`LinkedHashMap.putFirst/putLast` also relocate, but plain `put` keeps the position:**

```java
LinkedHashMap<String,Integer> m = new LinkedHashMap<>();
m.put("a",1); m.put("b",2); m.put("c",3);
m.firstEntry();            // a=1
m.putFirst("c", 30);       // {c=30, a=1, b=2}
m.putLast("a", 10);        // {c=30, b=2, a=10}
m.put("b", 22);            // {c=30, b=22, a=10}   existing key: position unchanged
m.pollFirstEntry();        // c=30 (removed)
m.reversed();              // reversed view
```

**Old method vs new method:**

| Old | New uniform name |
|---|---|
| `list.get(0)`, `list.get(list.size()-1)` | `getFirst()`, `getLast()` |
| `deque.peekFirst()` (returns `null` when empty) | `getFirst()` (throws when empty) |
| `sortedSet.first()` / `last()` | `getFirst()` / `getLast()` (same exception) |
| `treeSet.pollFirst()` (returns `null`) | `removeFirst()` (throws when empty) |
| `treeMap.firstEntry()` | unchanged, now declared in `SequencedMap` |
| `Collections.reverse(copy)` | `reversed()` (a view, not a copy) |

### 🆕 Java 19 — Pre-sized hash collections

```java
HashMap<String,Integer> m = HashMap.newHashMap(10);
HashSet<String> s          = HashSet.newHashSet(10);
LinkedHashMap<String,Integer> lm = LinkedHashMap.newLinkedHashMap(10);
LinkedHashSet<String> ls   = LinkedHashSet.newLinkedHashSet(10);
WeakHashMap<String,String> w = WeakHashMap.newWeakHashMap(10);
```

- The argument is the **expected number of elements**. The factory computes a capacity big enough that the map will not resize below that size. With `new HashMap<>(10)`, the 10 is the raw *initial capacity*, and the map resizes once it is 75 % full.
- The new collection is empty (`size() == 0`). A negative argument throws `IllegalArgumentException`.

### Other

- 🆕 Java 21: `Collections.shuffle(List, RandomGenerator)` overload.
- Not new but often thought to be: `List.of`/`Set.of`/`Map.of` (Java 9), `copyOf` (Java 10), `Stream.toList()` (Java 16, see D6).

---

## Exam traps

1. `Arrays.asList(...).add(x)` → **UOE**. `set` and `sort` are fine and write through to the array.
2. `List.of(...)` with any `null` → **NPE** at creation. `List.of(...).contains(null)` → NPE too.
3. `Set.of` with a duplicate → **IAE**, but `Set.copyOf` with duplicates is fine.
4. `List.copyOf` is a snapshot. `Collections.unmodifiableList` is a live read-only view.
5. `list.remove(1)` on a `List<Integer>` removes **index** 1, not the value 1.
6. `TreeSet`/`TreeMap` (keys), `ArrayDeque`, `PriorityQueue`, `Hashtable` and `ConcurrentHashMap` reject `null` → NPE.
7. `Deque.push`/`pop`/`peek` work on the **front**. `offer`/`add` work on the **back**.
8. `Queue.remove()`/`element()` throw `NoSuchElementException` on an empty queue. `poll()`/`peek()` return `null`.
9. `getOrDefault` returns `null` (not the default) when the key **is** present with a `null` value.
10. `merge` with a remapping function returning `null` **removes** the entry. When the key is absent, the function is not called.
11. `binarySearch` on unsorted data, or with a different comparator → the answer is "undefined".
12. `Arrays.mismatch` of equal arrays is `-1`. `Arrays.compare` of equal arrays is `0`.
13. `Comparator.comparing(x -> ...).reversed()` with an implicitly typed lambda → **does not compile**.
14. `List<Number> = new ArrayList<Integer>()` → does not compile. `List<? extends Number>` → no `add` (except `null`).
15. Overloads that differ only in the generic type argument (`m(List<String>)` / `m(List<Integer>)`) → name clash.
16. `getFirst()` on an empty `ArrayList` throws **`NoSuchElementException`**, while `get(0)` throws `IndexOutOfBoundsException`.
17. `TreeSet.addFirst` / `TreeMap.putFirst` → always **UOE**.
18. `LinkedHashSet.addFirst(existing)` **moves** the element. `LinkedHashMap.put(existingKey, v)` does **not** move it, but `putFirst`/`putLast` do.
19. `HashSet`, `HashMap`, `PriorityQueue`, `Set.of` and `Map.of` are **not** sequenced. Calling `getFirst()` on a `HashSet` does not compile.
20. `int[] a; a = {1,2};` does not compile. `int[][] x = new int[][3];` does not compile.
21. `new TreeSet<Object>().add(new Object())` → `ClassCastException`, even for a single element.
22. `Arrays.sort(int[], Comparator)` does not exist. Comparators need object arrays.

---

## Self-check before practice

- [ ] I can tell at a glance which array declarations compile, including `int[] x, y[];` and initializer rules.
- [ ] I know the default value of every element type.
- [ ] I can compute `Arrays.binarySearch` results for missing keys (`-(insertion point) - 1`).
- [ ] I know the difference between `Arrays.compare`, `Arrays.mismatch` and `Arrays.equals`, including for equal arrays.
- [ ] For each of `ArrayList`, `LinkedList`, `ArrayDeque`, `PriorityQueue`, `HashSet`, `LinkedHashSet`, `TreeSet`, `HashMap`, `LinkedHashMap` and `TreeMap` I know the ordering, duplicates and null rules.
- [ ] I can predict UOE / NPE / IAE for `Arrays.asList`, `List.of`, `Set.of`, `Map.of`, `copyOf` and `unmodifiable*`.
- [ ] I know the throwing vs `null`-returning `Queue`/`Deque` methods and which end `push`/`pop` use.
- [ ] I can trace `merge`, `compute`, `computeIfAbsent`, `computeIfPresent`, `putIfAbsent` and `getOrDefault`, including the `null` cases.
- [ ] I can build and read `Comparator` chains with `comparing`, `thenComparing`, `reversed` and `nullsFirst`.
- [ ] I know when `Comparator.comparing(lambda).reversed()` fails to compile.
- [ ] I can apply PECS and spot illegal `add` calls on wildcard lists.
- [ ] I know the type-erasure compile errors (name clash, generic array creation, static `T`).
- [ ] 🆕 I know which classes are `SequencedCollection`, `SequencedSet` or `SequencedMap`, and which are none.
- [ ] 🆕 I know the static return type of `reversed()` for `List`, `Deque`, `LinkedHashSet`, `TreeSet`, `LinkedHashMap` and `TreeMap`.
- [ ] 🆕 I know when `addFirst`/`putFirst` throw UOE, and that `LinkedHashSet.addFirst` relocates existing elements.
- [ ] 🆕 I know that `getFirst()` on an empty collection throws `NoSuchElementException`, while `firstEntry()` on an empty map returns `null`.
- [ ] 🆕 I know that `HashMap.newHashMap(n)` takes an expected size, not a capacity.

## Practice

- `practice/ocp17-829/ch05-working-with-arrays-and-collections.md` (80 questions; everything except sequenced collections and `newHashMap`).
- `practice/java25-gap/`: the sequenced-collections and Java 19+ collection questions.
