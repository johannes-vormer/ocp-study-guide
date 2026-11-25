---
title: "Gap: Smaller API additions (Java 18–25)"
source: java25-gap
topic: d01-values
---

# Gap: Smaller API additions (Java 18–25)

## gap-api-001

**Topic:** d01-values

What is the output of the following statement?

```java
System.out.println(Math.clamp(15, 0, 10) + " " + Math.clamp(-3L, 0, 10) + " "
                   + Math.clamp(2.5, 1.0, 2.0));
```

- **A.** `10 0 2.5`
- **B.** `10 -3 2.0`
- **C.** `15 0 2.0`
- **D.** `10 0 2.0`
- **E.** The code does not compile because the arguments of the second call have different types.

**Answer:** D

**Explanation:** 🆕 Java 21: `Math.clamp(value, min, max)` returns `value` limited to the range `[min, max]`: `min` if it is smaller, `max` if it is larger, otherwise `value` itself.

- `clamp(15, 0, 10)` → 10
- `clamp(-3L, 0, 10)` → 0. This uses the overload `int clamp(long value, int min, int max)`, which exists precisely so a `long` can be clamped into an `int` range — mixed types are fine.
- `clamp(2.5, 1.0, 2.0)` → 2.0 (`double` overload)

## gap-api-002

**Topic:** d01-values

Which statements compile? (Choose two.)

- **A.** `int a = Math.clamp(20L, 0L, 10L);`
- **B.** `int b = Math.clamp(5_000_000_000L, 0, 100);`
- **C.** `long c = Math.clamp(20, 0, 10);`
- **D.** `int d = Math.clamp(20, 0, 10L);`
- **E.** `int e = Math.clamp(2.0, 0, 10);`

**Answer:** B, C

**Explanation:** `Math.clamp` has four overloads:

```java
int    clamp(long value, int min, int max)
long   clamp(long value, long min, long max)
double clamp(double value, double min, double max)
float  clamp(float value, float min, float max)
```

There is **no** `clamp(int, int, int)`; `int` arguments use `clamp(long, int, int)` and the result is an `int`.

- A: all three are `long`, so `clamp(long, long, long)` is chosen and returns `long` → lossy assignment to `int`, does not compile.
- B: `clamp(long, int, int)` returns `int` → compiles (the result is 100).
- C: returns `int`, which widens to `long` → compiles.
- D: the `long` max forces `clamp(long, long, long)` → returns `long`, does not compile.
- E: the `double` value forces the `double` overload → returns `double`, does not compile.

## gap-api-003

**Topic:** d01-values

What is the result of the following code fragment?

```java
int lo = 10, hi = 0;
System.out.println(Math.clamp(5, lo, hi));
```

- **A.** It prints `5`.
- **B.** It prints `10`.
- **C.** It prints `0`.
- **D.** An `IllegalArgumentException` is thrown.
- **E.** An `ArithmeticException` is thrown.

**Answer:** D

**Explanation:** `Math.clamp` requires `min <= max`. If `min` is greater than `max`, it throws `IllegalArgumentException` rather than swapping the bounds or returning one of them.

## gap-api-004

**Topic:** d01-values

What is the output of the following statement?

```java
System.out.println(Math.ceilDiv(7, 2) + " " + Math.ceilDiv(-7, 2) + " "
                   + Math.floorDiv(-7, 2) + " " + (-7 / 2));
```

- **A.** `4 -3 -4 -3`
- **B.** `4 -4 -4 -3`
- **C.** `3 -3 -4 -4`
- **D.** `4 -3 -3 -3`
- **E.** The code does not compile.

**Answer:** A

**Explanation:** 🆕 Java 18 added `Math.ceilDiv`, `ceilMod` and `ceilDivExact` as counterparts of the older `floorDiv`/`floorMod`.

- `ceilDiv(7, 2)`: 3.5 rounded toward **positive infinity** → 4
- `ceilDiv(-7, 2)`: -3.5 rounded toward positive infinity → -3
- `floorDiv(-7, 2)`: -3.5 rounded toward **negative infinity** → -4
- `-7 / 2`: integer division truncates toward **zero** → -3

## gap-api-005

**Topic:** d01-values

What is the output of the following code fragment?

```java
var sb = new StringBuilder("x");
sb.repeat("ab", 2).repeat('-', 3).repeat(65, 2);
System.out.println(sb);
```

- **A.** `xabab---6565`
- **B.** `xabab---AA`
- **C.** `xab2-365`
- **D.** `abab---AA`
- **E.** The code does not compile.

**Answer:** B

**Explanation:** 🆕 Java 21: `StringBuilder` (and `StringBuffer`) gained `repeat` methods that **append** repeated content and return the builder, so they can be chained:

- `repeat(CharSequence cs, int count)` appends `cs` `count` times → `xabab`
- `repeat(int codePoint, int count)` appends a **code point** `count` times. The `char` `'-'` is widened to `int`, so → `xabab---`
- `repeat(65, 2)`: `65` is interpreted as a code point, which is the character `A` → `xabab---AA`

Note that `String.repeat(int)` (Java 11) returns a new `String`, while `StringBuilder.repeat` modifies the builder. A negative count throws `IllegalArgumentException`.

## gap-api-006

**Topic:** d01-values

What is the output of the following code fragment?

```java
String s = "banana";
System.out.println(s.indexOf('a', 2, 4) + " " + s.indexOf('b', 1, 6) + " "
                   + s.indexOf("na", 0, 4) + " " + s.indexOf("na", 0, 3));
```

- **A.** `3 0 2 2`
- **B.** `1 -1 2 -1`
- **C.** `3 -1 2 2`
- **D.** `3 -1 4 -1`
- **E.** `3 -1 2 -1`

**Answer:** E

**Explanation:** 🆕 Java 21: `String.indexOf(int ch, int beginIndex, int endIndex)` and `indexOf(String str, int beginIndex, int endIndex)` search only in the range from `beginIndex` (inclusive) to `endIndex` (exclusive). The returned index is still relative to the **whole** string.

- `indexOf('a', 2, 4)`: search `"na"` (indexes 2–3) → `a` at index 3
- `indexOf('b', 1, 6)`: `b` is at index 0, outside the range → -1
- `indexOf("na", 0, 4)`: search `"bana"` → `"na"` at index 2
- `indexOf("na", 0, 3)`: search `"ban"` → the match at 2–3 does not fit completely in the range → -1

## gap-api-007

**Topic:** d01-values

What is the result of the following code fragment?

```java
String s = "banana";
System.out.print(s.indexOf('a', 10) + " ");
System.out.print(s.indexOf('a', -5) + " ");
System.out.print(s.indexOf('a', 0, 10));
```

- **A.** It prints `-1 1 -1`.
- **B.** It prints `-1 1 1`.
- **C.** It prints `-1 1 ` and then throws a `StringIndexOutOfBoundsException`.
- **D.** It throws a `StringIndexOutOfBoundsException` without printing anything.
- **E.** It prints `-1 -1 ` and then throws a `StringIndexOutOfBoundsException`.

**Answer:** C

**Explanation:** The old two-argument `indexOf(ch, fromIndex)` is **lenient**: a `fromIndex` past the end simply returns -1, and a negative `fromIndex` is treated as 0 (so the first `a` at index 1 is found).

The Java 21 range version `indexOf(ch, beginIndex, endIndex)` is **strict**: it throws `StringIndexOutOfBoundsException` if `beginIndex < 0`, `beginIndex > endIndex` or `endIndex > length()`. Here `endIndex` 10 exceeds the length 6.

## gap-api-008

**Topic:** d01-values

What is the output of the following code fragment? (Assume `java.util.*` is imported.)

```java
System.out.println(Arrays.toString("a1b22c".splitWithDelimiters("\\d+", 0)));
System.out.println(Arrays.toString("a1b22c".split("\\d+")));
```

- **A.** `[a, 1, b, 22, c]` followed by `[a, b, c]`
- **B.** `[a, b, c]` followed by `[a, b, c]`
- **C.** `[a, 1, b, 2, 2, c]` followed by `[a, b, c]`
- **D.** `[a1, b22, c]` followed by `[a, b, c]`
- **E.** The code does not compile.

**Answer:** A

**Explanation:** 🆕 Java 21: `String.splitWithDelimiters(String regex, int limit)` works like `split`, but the result **also contains the delimiters** — the text matched by the regex — as separate elements, alternating with the pieces in between. The regex `\d+` matches `1` and `22` as whole runs of digits, so the result is `[a, 1, b, 22, c]`. Plain `split` drops the delimiters: `[a, b, c]`.

Note that `splitWithDelimiters` has no one-argument overload; the `limit` is required.

## gap-api-009

**Topic:** d01-values

What is the output of the following code fragment?

```java
String[] p1 = "a,b,,c,".splitWithDelimiters(",", 0);
String[] p2 = "a,b,c".splitWithDelimiters(",", 2);
System.out.println(p1.length + " " + p2.length + " " + p2[2]);
```

- **A.** `9 3 b`
- **B.** `8 3 b,c`
- **C.** `7 2 b,c`
- **D.** `8 4 b`
- **E.** `9 3 b,c`

**Answer:** B

**Explanation:** `splitWithDelimiters` uses the same `limit` rules as `split`:

- `limit == 0`: split as often as possible and **remove trailing empty strings**. `"a,b,,c,"` gives `a`, `,`, `b`, `,`, `""`, `,`, `c`, `,` and a trailing `""`, which is removed → 8 elements. (The empty string between the two commas is not trailing, so it stays.)
- `limit > 0`: the pattern is applied at most `limit - 1` times. With limit 2 there is one split: `a`, `,`, `b,c` → 3 elements, and `p2[2]` is `"b,c"`.

With a negative limit, the trailing empty string would be kept (9 elements).

## gap-api-010

**Topic:** d01-values

What is the output of the following code fragment? (The source file is saved in UTF-8, the default since Java 18.)

```java
String s = "Hi😀";
System.out.print(s.length() + " ");
System.out.print(s.codePoints().filter(Character::isEmoji).count() + " ");
System.out.print(s.chars().filter(Character::isEmoji).count() + " ");
System.out.print(Character.isEmoji('1'));
```

- **A.** `3 1 1 false`
- **B.** `4 1 1 false`
- **C.** `4 1 0 true`
- **D.** `3 1 0 true`
- **E.** `4 1 0 false`

**Answer:** C

**Explanation:** 🆕 Java 21 added emoji property methods to `Character`: `isEmoji`, `isEmojiPresentation`, `isEmojiModifier`, `isEmojiModifierBase`, `isEmojiComponent` and `isExtendedPictographic`. They all take an `int` **code point**.

- 😀 (U+1F600) is outside the Basic Multilingual Plane and is stored as **two** `char`s (a surrogate pair), so `length()` is 4.
- `codePoints()` yields whole code points; exactly one of them is an emoji → 1.
- `chars()` yields UTF-16 code units; the individual surrogate halves are not emoji → 0.
- Surprisingly, digits (and `#`, `*`) have the Unicode *Emoji* property because they can form keycap emoji, so `isEmoji('1')` is `true`. (`isEmojiPresentation('1')` would be `false`.)

## gap-api-011

**Topic:** d10-localization

What is the output of the following code fragment? (Assume `java.util.*` is imported.)

```java
Locale l1 = Locale.of("fr", "CA");
Locale l2 = Locale.of("EN");
Locale l3 = Locale.of("de", "ch", "x");
System.out.println(l1 + " " + l2 + " " + l3);
```

- **A.** `fr_CA EN de_ch_x`
- **B.** `fr_CA en de_CH_x`
- **C.** `fr-CA en de-CH-x`
- **D.** `fr_CA en de_CH`
- **E.** The code does not compile because `Locale` has no `of` method.

**Answer:** B

**Explanation:** 🆕 Java 19: the static factories `Locale.of(language)`, `Locale.of(language, country)` and `Locale.of(language, country, variant)` replace the `Locale` constructors, which are **deprecated** (they still compile, with a deprecation warning). There is no no-argument `Locale.of()`.

As with the constructors, the language is normalized to lower case and the country to upper case. `toString()` joins the parts with underscores: `fr_CA en de_CH_x`. Option C shows the format of `toLanguageTag()`-style tags, not `toString()`.

## gap-api-012

**Topic:** d05-collections

Which statements are true? (Choose two.) (Assume `java.util.*` is imported.)

- **A.** `HashMap.newHashMap(100)` returns a map that already contains 100 entries.
- **B.** `HashMap.newHashMap(n)` returns a map sized so that `n` mappings can be added without resizing.
- **C.** `TreeMap.newTreeMap(5)` compiles.
- **D.** `Set<String> s = HashSet.newHashSet(10);` compiles.
- **E.** `HashMap.newHashMap(-1)` returns an empty map.
- **F.** `new HashMap<>(100)` and `HashMap.newHashMap(100)` create maps that can hold the same number of mappings before resizing.

**Answer:** B, D

**Explanation:** 🆕 Java 19 added `HashMap.newHashMap(int numMappings)`, `LinkedHashMap.newLinkedHashMap`, `WeakHashMap.newWeakHashMap`, `HashSet.newHashSet` and `LinkedHashSet.newLinkedHashSet`. The argument is the **expected number of mappings**, and the capacity is calculated from it using the load factor 0.75 (B, D true).

That differs from the constructor `new HashMap<>(100)`, whose argument is the **initial capacity**: because of the load factor 0.75, such a map resizes before it reaches 100 entries (F false). The new map is empty (A false). Tree-based collections have no capacity, so there is no `TreeMap.newTreeMap` (C false). A negative argument throws `IllegalArgumentException` (E false).

## gap-api-013

**Topic:** d01-values

What is the output of the following code fragment? (Assume `java.time.*` is imported.)

```java
Instant start = Instant.parse("2025-01-01T10:00:00Z");
Instant end   = Instant.parse("2025-01-01T08:30:00Z");
System.out.println(start.until(end) + " " + Duration.between(end, start));
```

- **A.** `PT1H30M PT1H30M`
- **B.** `PT-1H-30M PT-1H-30M`
- **C.** `PT-1H-30M PT1H30M`
- **D.** `PT-1.5H PT1.5H`
- **E.** The code does not compile because `until` requires a `TemporalUnit`.

**Answer:** C

**Explanation:** 🆕 Java 23: `Instant.until(Instant endExclusive)` returns the `Duration` from this instant to the other one; it is equivalent to `Duration.between(this, endExclusive)`. The older `until(Temporal, TemporalUnit)` overload, which returns a `long`, still exists, so the one-argument call compiles.

`end` is 90 minutes **before** `start`, so `start.until(end)` is negative: `PT-1H-30M` (each component of a negative `Duration` is printed with its own minus sign). `Duration.between(end, start)` is positive: `PT1H30M`.

## gap-api-014

**Topic:** d09-io

The following class is run and the user types `Bob` followed by Enter. What is the output (including the echoed prompt)?

```java
public class Greeter {
    public static void main(String[] args) {
        IO.println("x" + 1);
        IO.print(2);
        IO.println();
        IO.println(null);
        String in = IO.readln("Name? ");
        IO.println("[" + in + "]");
    }
}
```

- **A.**
  ```
  x1
  2
  null
  Name? [Bob]
  ```
- **B.**
  ```
  x1
  2
  Name? [Bob]
  ```
- **C.**
  ```
  x12
  null
  Name? [Bob]
  ```
- **D.** The code does not compile because `IO` is not imported.
- **E.** A `NullPointerException` is thrown.

**Answer:** A

**Explanation:** 🆕 Java 25 (JEP 512): the class `java.lang.IO` provides static console methods: `print(Object)`, `println(Object)`, `println()`, `readln()` and `readln(String prompt)`. Because it is in `java.lang`, it can be used in **any** class without an import, not only in compact source files.

- `IO.println("x" + 1)` prints `x1` and a newline.
- `IO.print(2)` prints `2` without a newline; `IO.println()` ends the line.
- `IO.println(null)` prints `null` — the parameter is `Object`, so there is no ambiguity and no exception.
- `readln("Name? ")` prints the prompt (without a newline), then reads one line. The typed input itself is echoed by the terminal, not by the program, so the program's output on that line is `Name? [Bob]`.

## gap-api-015

**Topic:** d09-io

Which statements about `java.lang.IO` in Java 25 are true? (Choose two.)

- **A.** `IO.printf("%d%n", 42);` compiles.
- **B.** `IO` can be used without an import statement in any class.
- **C.** In a compact source file (`void main() { ... }`), `println("hi");` compiles without the `IO.` prefix.
- **D.** `IO.readln()` declares `throws IOException`, so callers must handle it.
- **E.** `IO.readln()` returns `null` when the end of input has been reached.
- **F.** `IO.println()` cannot be called without an argument.

**Answer:** B, E

**Explanation:** `IO` is in `java.lang` (B true). `readln()` returns `null` at end of input, like `BufferedReader.readLine()` (E true).

A is false: `IO` has only `print`, `println` and `readln` — there is no `printf`. C is false: during the previews the `IO` methods were implicitly imported into compact source files, but in the final Java 25 version they are **not**; you must write `IO.println(...)`. D is false: `readln` does not declare any checked exception (I/O errors are reported as `IOError`). F is false: a no-argument `println()` exists and prints a line separator.

## gap-api-016

**Topic:** d09-io

What is the output of the following code fragment? (Assume `java.io.*` is imported; the enclosing method declares `throws IOException`.)

```java
Reader r = Reader.of("line1\nline2\n\nline4");
System.out.println(r.readAllLines());
System.out.println(Reader.of("ab\ncd").readAllAsString().length());
```

- **A.** `[line1, line2, line4]` followed by `4`
- **B.** `[line1, line2, , line4]` followed by `4`
- **C.** `[line1, line2, line4]` followed by `5`
- **D.** `[line1, line2, , line4]` followed by `5`
- **E.** The code does not compile because `Reader` is abstract.

**Answer:** D

**Explanation:** 🆕 Java 24 added the static factory `Reader.of(CharSequence)`, which returns a `Reader` over the characters of a string (like `new StringReader(...)`). 🆕 Java 25 added `Reader.readAllLines()` and `Reader.readAllAsString()`.

- `readAllLines()` splits on line terminators and returns a `List<String>` without the terminators. The empty line between `line2` and `line4` is kept as an empty string: `[line1, line2, , line4]`.
- `readAllAsString()` returns all remaining characters **including** line terminators: `"ab\ncd"` has length 5.

`Reader` is abstract, but calling a static factory method on it is fine.
