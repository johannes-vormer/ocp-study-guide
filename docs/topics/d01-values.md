---
title: "D1 — Date, Time, Text, Numeric and Boolean Values"
topic: d01-values
order: 1
---

# D1 — Date, Time, Text, Numeric and Boolean Values

## What the exam tests

- Primitive types, literals, wrapper classes, autoboxing/unboxing and the `Integer` cache
- Operator precedence, numeric promotion, casting (implicit and explicit), compound assignment
- `Math` API (rounding, exact arithmetic, `floorDiv`/`floorMod`, 🆕 `clamp`, `ceilDiv`)
- `String` immutability, the string pool, the core `String` and `StringBuilder` methods, text blocks
- Date-Time API: `LocalDate`, `LocalTime`, `LocalDateTime`, `ZonedDateTime`, `Instant`, `Period`, `Duration`, time zones and daylight saving time (DST)
- Reading code and predicting the output exactly (spacing, formatting of `toString()`)

---

## 1. Primitives and literals

| Type | Bits | Default (field) | Literal examples |
|------|------|-----------------|------------------|
| `byte` | 8 | `0` | `(byte) 10` (an int literal in range is allowed in assignment) |
| `short` | 16 | `0` | |
| `char` | 16, unsigned | `'\u0000'` | `'a'`, `65`, `'\n'` |
| `int` | 32 | `0` | `42`, `0b101`, `017` (octal), `0x1F`, `1_000` |
| `long` | 64 | `0L` | `3_000_000_000L` (without `L` it is a compile error) |
| `float` | 32 | `0.0f` | `1.1f` (without `f`, `1.1` is a `double` → compile error) |
| `double` | 64 | `0.0` | `1.1`, `1e3`, `1.1d` |
| `boolean` | – | `false` | `true`, `false` only (no `0`/`1`) |

- Underscores: allowed only **between digits**. Not at the start/end, not next to `.`, `L`, `f`, `0x`/`0b` prefix.
- Local variables have **no** default value — reading one before assignment is a compile error.
- `0b101 + 017 + 0x1F + 1_000` is `5 + 15 + 31 + 1000 = 1051`.

## 2. Numeric promotion and casting

Binary numeric promotion rules (applied to `+ - * / % < > == & | ^` etc.):

1. If either operand is `double` → `double`; else `float` → `float`; else `long` → `long`.
2. Otherwise **both are promoted to `int`** — even `byte + byte`, `short * short`, `char + char`.
3. Result has the promoted type.

```java
byte a = 1, b = 2;
byte c = a + b;          // DOES NOT COMPILE: int → byte needs a cast
byte d = (byte) (a + b); // OK
System.out.println('a' + 'b');      // 195  (char + char = int)
System.out.println("" + 'a' + 'b'); // ab
```

- Widening (implicit): `byte → short → int → long → float → double`, `char → int`. `long → float` is allowed although precision can be lost.
- Narrowing needs an explicit cast, **except**: a compile-time constant `int` that fits can be assigned to `byte`/`short`/`char` (`byte b = 10;` OK, `byte b = 200;` error).
- Cast results: `(byte) 200` → `-56`, `(int) 3.99` → `3`, `(int) -3.99` → `-3` (truncation toward zero), `(int) 1e20` → `Integer.MAX_VALUE`, `(int) Double.NaN` → `0`, `(int) 3_000_000_000L` → `-1294967296`.
- `float f = 1.1f; double d = f;` → `d` prints `1.100000023841858`.

### Compound assignment hides a cast

`x op= y` means `x = (T)(x op y)` where `T` is the type of `x`.

```java
byte b = 10;
b += 300;          // compiles! (byte)(10 + 300) = 54
b = b + 1;         // DOES NOT COMPILE
char c = 'a'; c += 1;   // 'b'
```

### Integer arithmetic

| Expression | Result | Note |
|---|---|---|
| `10 / 4` | `2` | integer division truncates toward zero |
| `10 / 4.0` | `2.5` | |
| `-10 % 3` | `-1` | sign of `%` follows the **left** operand |
| `10 % -3` | `1` | |
| `5.5 % 2` | `1.5` | `%` works on floating point too |
| `1 / 0` | `ArithmeticException` | integer only |
| `1.0 / 0` | `Infinity` | no exception for floating point |
| `0.0 / 0` | `NaN` | `Double.NaN == Double.NaN` is `false` |
| `Integer.MAX_VALUE + 1` | `-2147483648` | silent overflow |
| `0.1 + 0.2` | `0.30000000000000004` | |
| `1 << 33` | `2` | int shift distance is taken mod 32 |
| `1L << 33` | `8589934592` | |
| `~5` | `-6` | `~x == -x - 1` |
| `-5 >> 1` / `-5 >>> 28` | `-3` / `15` | `>>` keeps sign, `>>>` fills with zeros |

## 3. Operators: precedence and evaluation

Precedence (high → low): postfix `x++ x--` → prefix/unary `++x --x + - ! ~ (cast)` → `* / %` → `+ -` → `<< >> >>>` → `< > <= >= instanceof` → `== !=` → `&` → `^` → `|` → `&&` → `||` → `?:` → assignment `= += -=` …

- Operands are evaluated **left to right**, even if precedence groups them differently.
- Assignment is right-associative and is itself an expression with a value.

```java
int x = 5;
x = x++ + ++x;     // 5 + 7 → x = 12
int y = 3;
y += y++ * 2;      // y = 3 + (3 * 2) = 9   (left operand y is saved first)
System.out.println(1 + 2 + "3" + 4 + 5); // "3345"
```

### Boolean operators

- `&&` and `||` **short-circuit**; `&`, `|`, `^` on booleans always evaluate both sides.
- `if (f = true)` compiles when `f` is `boolean` (assignment yields `true`); with an `int` it does not compile.
- `true ^ true` → `false`.

```java
int k = 0;
boolean r = (k > 0) && (k++ > 0);  // k stays 0
r = (k >= 0) | (k++ > 0);          // k becomes 1
```

### Ternary operator

- Only one of the two branches is evaluated.
- The result type follows numeric promotion: `true ? 1 : 2.0` prints `1.0`.
- `false ? 'a' : 0` has type `char` (the constant `0` fits in `char`) → prints the NUL character, not `0`.
- `true ? null : 0` is fine (type `Integer`) and prints `null`; unboxing it into an `int` would throw `NullPointerException`.

## 4. Wrapper classes and autoboxing

- `Integer.parseInt("12")` → `int`; `Integer.valueOf("12")` → `Integer`. `parseInt("+12")` works, `parseInt("1.0")` throws `NumberFormatException`; `parseInt("ff", 16)` → `255`.
- `Boolean.parseBoolean(s)` is `true` only for `"true"` ignoring case; anything else (also `null`, `"yes"`) → `false`.
- **Integer cache**: `Integer.valueOf`/autoboxing returns cached objects for `-128..127`.

```java
Integer i1 = 127, i2 = 127, i3 = 128, i4 = 128;
System.out.println(i1 == i2);      // true  (cached)
System.out.println(i3 == i4);      // false (different objects)
System.out.println(i3.equals(i4)); // true
Long L = 1L;
System.out.println(L.equals(1));   // false: 1 is boxed to Integer
System.out.println(L == 1);        // true: L is unboxed
```

- Unboxing `null` → `NullPointerException`.
- Overload resolution order: exact match → widening → boxing → varargs. Java widens before it boxes, and never widens-then-boxes (`long` → `Integer` impossible).
- The wrapper constructors (`new Integer(5)`) are deprecated – use `valueOf` or autoboxing.

## 5. The `Math` API

| Method | Result type | Notes |
|---|---|---|
| `Math.max(1, 2L)` | `long` (`2`) | overloads for int/long/float/double |
| `Math.round(2.5)` / `Math.round(-2.5)` | `long` `3` / `-2` | round half **up** (toward +∞); `round(float)` returns `int` |
| `Math.rint(2.5)` | `2.0` | half-even, returns `double` |
| `Math.ceil(-0.5)` / `Math.floor(-0.5)` | `-0.0` / `-1.0` | return `double` |
| `Math.pow(2, 3)` | `8.0` | always `double` |
| `Math.abs(Integer.MIN_VALUE)` | `-2147483648` | overflows silently |
| `Math.absExact(Integer.MIN_VALUE)` | `ArithmeticException` | also `addExact`, `multiplyExact`, `toIntExact`… |
| `Math.floorDiv(-7, 2)` / `Math.floorMod(-7, 2)` | `-4` / `1` | compare `-7 / 2 = -3`, `-7 % 2 = -1` |
| `Math.random()` | `double` in `[0.0, 1.0)` | |

## 6. `String`

- `String` is **immutable** and `final`. Methods like `toUpperCase()`, `concat()`, `replace()`, `strip()` return a **new** string; ignoring the result is a classic trap.

```java
String s = "abc";
s.toUpperCase();
s.concat("d");
System.out.println(s); // abc
```

### String pool and `==`

```java
String s1 = "Java";
String s2 = "Ja" + "va";          // compile-time constant → same pooled object
String p = "Ja";
String s3 = p + "va";             // computed at runtime → new object
final String fp = "Ja";
String s4 = fp + "va";            // fp is a constant variable → constant expression
s1 == s2;            // true
s1 == s3;            // false
s1 == s4;            // true
s1 == s3.intern();   // true
s1 == new String("Java"); // false
```

### Key methods (all indexes are 0-based, end index exclusive)

| Call | Result |
|---|---|
| `"abcdef".substring(2, 4)` | `"cd"` |
| `"abc".substring(3)` | `""` (index == length is OK) |
| `"abc".substring(4)` / `substring(2, 1)` | `StringIndexOutOfBoundsException` |
| `"abc".charAt(1)` | `'b'` |
| `"abc".indexOf('z')` | `-1` |
| `"abcabc".lastIndexOf("b")` | `4` |
| `"aaa".replace("aa", "b")` | `"ba"` (left to right, non-overlapping) |
| `"a,b,,c,,".split(",").length` | `4` (trailing empty strings removed) |
| `"b".compareTo("a")` / `"ab".compareTo("abcd")` | `1` / `-2` (length difference when one is a prefix) |
| `"abc".contains("")` | `true` |
| `" ".isEmpty()` / `" ".isBlank()` | `false` / `true` |
| `" x".trim().length()` / `.strip().length()` | `2` / `1` (`strip` knows Unicode whitespace, `trim` only `<= ' '`) |
| `"a".repeat(3)` | `"aaa"` |
| `String.join("-", "a", "b")` | `"a-b"` |
| `"%d-%s".formatted(1, "x")` | `"1-x"` |
| `"abc".transform(String::length)` | `3` |
| `"a\nb".indent(2)` | `"  a\n  b\n"` (normalises line endings and **adds** a trailing `\n`) |
| `"a\\tb".translateEscapes()` | `a<TAB>b` |
| `String.valueOf((Object) null)` | `"null"` |

`equals` vs `equalsIgnoreCase` vs `contentEquals(CharSequence)` — only `contentEquals` compares a `String` with a `StringBuilder` by content.

## 7. `StringBuilder`

- **Mutable**; most methods return `this`, so chained calls all modify the same object.
- `StringBuilder` does **not** override `equals` (identity comparison) but implements `Comparable<StringBuilder>` (`compareTo` compares content).

```java
StringBuilder sb = new StringBuilder("abcdef");
sb.insert(1, "X")       // aXbcdef
  .delete(2, 4)         // aXdef   (end exclusive)
  .deleteCharAt(0)      // Xdef
  .reverse();           // fedX
StringBuilder x = new StringBuilder("a"), y = x.append("b");
System.out.println(x == y);  // true, x and y are "ab"
new StringBuilder("abc").replace(1, 10, "Z"); // "aZ" — end may exceed length
```

- `length()` vs `capacity()`: `new StringBuilder(10)` has length 0, capacity 10.
- `setLength(3)` on an empty builder pads with `'\u0000'`.
- `substring()` returns a `String` and does **not** change the builder.
- `delete(start, end)` tolerates `end > length()`; `deleteCharAt`/`charAt`/`insert` with a bad index throw `StringIndexOutOfBoundsException`.

## 8. Text blocks

```java
String tb = """
    Hello,
      World!\
     same line
    tab\there\s
    quote: \""" and "" ok
    """;
```

Output (spaces shown as `.`):

```
Hello,
..World!.same.line
tab	here.
quote:.""".and."".ok
```

Rules:

- Opening `"""` must be followed by a **line terminator** — `"""abc"""` on one line does not compile.
- **Incidental whitespace** = the smallest indentation over all non-blank content lines **and the closing delimiter line**. It is removed. Moving the closing `"""` left adds indentation to every line.
- Trailing spaces on each line are **stripped**. Use `\s` (one space) or `\040` to keep one.
- `\` at the end of a line = line continuation (no newline inserted).
- If the closing `"""` is on its own line, the string ends with `\n`; if it follows the last content (`b"""`), there is no trailing newline.
- `"` and `""` need no escaping; `"""` inside must be escaped (`\"""`).
- Escape sequences are processed **after** incidental whitespace is stripped.
- A text block is a `String` literal → it is interned like any other literal.

```java
String tb3 = """
          x
        y
      """;      // closing delimiter is the least indented: "    x\n  y\n"
```

## 9. Date-Time API (`java.time`)

All classes are **immutable**; every `plusX`/`minusX`/`withX` returns a new object. No public constructors — use `of`, `now`, `parse`.

| Class | Contains | `toString` example |
|---|---|---|
| `LocalDate` | date | `2025-01-31` |
| `LocalTime` | time | `10:00` (seconds shown only if non-zero: `10:00:05`) |
| `LocalDateTime` | date + time | `2025-01-02T10:00` |
| `ZonedDateTime` | date + time + zone (+ offset) | `2025-03-09T03:30-04:00[America/New_York]` |
| `OffsetDateTime` | date + time + fixed offset | `2025-01-01T12:00+02:00` |
| `Instant` | point on the UTC timeline | `2025-01-01T00:00:00Z` |
| `Period` | date-based amount (y, m, d) | `P1Y2M3D`, `P14D` (weeks are converted to days) |
| `Duration` | time-based amount (s, ns) | `PT25H`, `PT1H30M`, `PT1M1.5S`, `PT-1.5S` |

- Months are **1-based** (`Month.JANUARY` or `1`). `LocalDate.of(2025, 13, 1)` and `LocalDate.of(2025, 2, 29)` throw `DateTimeException` at runtime (they compile).
- `plusMonths` clamps to the last valid day: `2025-01-31 plusMonths(1)` → `2025-02-28`; then another `plusMonths(1)` → `2025-03-28` (not 31).
- `2024-02-29 plusYears(1)` → `2025-02-28`.
- `Duration.ofDays(1)` prints `PT24H` (no `D` in `Duration`). `Duration.ofHours(25)` prints `PT25H`.
- `Period.of(1, 14, 40)` prints `P1Y14M40D` (not normalised); `normalized()` → `P2Y2M40D` (days are never normalised).

### Static factory chaining trap

```java
Period p = Period.ofYears(1).ofMonths(2); // P2M — ofMonths is static; the first call is discarded
```

### Which amount works with which type

| | `Period` | `Duration` |
|---|---|---|
| `LocalDate` | ✓ | `UnsupportedTemporalTypeException` |
| `LocalTime` | ✗ throws (unless the period is zero) | ✓ |
| `LocalDateTime`, `ZonedDateTime` | ✓ | ✓ |
| `Instant` | days only | ✓ (`plus(1, ChronoUnit.MONTHS)` throws) |

### Between

```java
Period.between(LocalDate.of(2025,1,31), LocalDate.of(2025,3,1));  // P1M1D
Duration.between(LocalTime.of(10,0), LocalTime.of(8,30));         // PT-1H-30M
ChronoUnit.DAYS.between(LocalDate.of(2025,1,1), LocalDate.of(2024,12,25)); // -7
ChronoUnit.MONTHS.between(LocalDate.of(2025,1,31), LocalDate.of(2025,2,28)); // 0 (not a full month)
```

`LocalTime.of(23, 30).plusHours(2)` → `01:30` (wraps around, no date change).

### Time zones and daylight saving time

The US switches on the second Sunday in March (02:00 → 03:00) and the first Sunday in November (02:00 → 01:00).

```java
ZoneId ny = ZoneId.of("America/New_York");
var spring = ZonedDateTime.of(2025, 3, 9, 1, 30, 0, 0, ny);  // 01:30-05:00
spring.plusHours(1);    // 2025-03-09T03:30-04:00  (02:30 doesn't exist)
ZonedDateTime.of(2025, 3, 9, 2, 30, 0, 0, ny); // gap → shifted to 03:30-04:00
var fall = ZonedDateTime.of(2025, 11, 2, 1, 30, 0, 0, ny);   // 01:30-04:00 (earlier offset)
fall.plusHours(1);      // 2025-11-02T01:30-05:00  (same local time, new offset)
Duration.between(ZonedDateTime.of(2025,3,9,0,0,0,0,ny),
                 ZonedDateTime.of(2025,3,10,0,0,0,0,ny));   // PT23H
```

- `plusHours`/`Duration` operate on the **instant timeline**; `plusDays`/`Period` keep the **local** time and adjust the offset.
- In an overlap (fall back), `of(...)` picks the **earlier** offset; `withLaterOffsetAtOverlap()` switches.
- Converting to UTC: subtract the offset. `2025-03-09T01:30-05:00` → `06:30Z`. `12:00+02:00` → `10:00Z`.

### Formatting (more in D10)

```java
DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").format(LocalDateTime.of(2025,3,4,5,6)); // 04.03.2025 05:06
DateTimeFormatter.ofPattern("HH:mm").format(LocalDate.of(2025,1,1)); // UnsupportedTemporalTypeException
```

`M` = month, `m` = minute, `H` = 0–23, `h` = 1–12 (with `a` for AM/PM), literal text in single quotes `'at'`.

---

## 10. 🆕 Since Java 17

| Feature | Version | Example | Result |
|---|---|---|---|
| `Math.clamp(value, min, max)` (int, long, float, double overloads) | 🆕 Java 21 | `Math.clamp(15, 0, 10)` | `10` |
| `Math.clamp(long, int, int)` returns **`int`** | 🆕 Java 21 | `int r = Math.clamp(15L, 0, 10);` | compiles, `10` |
| `clamp` with `min > max` | 🆕 Java 21 | `Math.clamp(5, 10, 0)` | `IllegalArgumentException` |
| `Math.ceilDiv`, `ceilMod`, `ceilDivExact`, `floorDivExact` | 🆕 Java 18 | `Math.ceilDiv(7, 2)` / `Math.ceilDiv(-7, 2)` / `Math.ceilMod(7, 2)` | `4` / `-3` / `-1` |
| `StringBuilder.repeat(CharSequence, int)` and `repeat(int codePoint, int)` (a `char` argument uses the code-point overload) | 🆕 Java 21 | `new StringBuilder("ab").repeat("xy", 2).repeat('-', 3)` | `abxyxy---` |
| `String.indexOf(ch, beginIndex, endIndex)` and `indexOf(String, begin, end)` | 🆕 Java 21 | `"hello world".indexOf('o', 5, 8)` | `7` (end exclusive) |
| `String.splitWithDelimiters(regex, limit)` | 🆕 Java 21 | `"a1b22c".splitWithDelimiters("\\d+", 0)` | `[a, 1, b, 22, c]` |
| `Character.isEmoji(int)`, `isEmojiPresentation`, `isEmojiModifier`, `isEmojiComponent`, `isExtendedPictographic`… | 🆕 Java 21 | `Character.isEmoji(0x1F600)` / `Character.isEmoji('A')` | `true` / `false` |

Notes:

- `StringBuilder.repeat` returns the builder (chainable), unlike `String.repeat` which returns a new `String` (Java 11).
- `repeat(0x1F600, 1)` appends the code point as a surrogate pair (😀, length 2).
- Primitive types in patterns (`instanceof int`, `switch` on `long` with patterns) — JEP 507 is **preview in Java 25, not on the exam**.

---

## Exam traps

- **Integer overflow** is silent: `Integer.MAX_VALUE + 1` is negative; `Math.abs(Integer.MIN_VALUE)` is still negative.
- `long big = 3000000000;` does not compile (int literal too large) — needs `L`. `float f = 1.0;` does not compile.
- `byte + byte` is `int`; `b = b + 1` fails but `b += 1` and `b++` compile (implicit cast).
- **Short-circuit**: side effects in the right operand of `&&`/`||` may never run. `&`/`|` always run both.
- **Ternary**: only the selected branch is evaluated; result type is promoted (`1` becomes `1.0` if the other branch is `double`).
- `x = x++` leaves `x` unchanged.
- `"" + 1 + 2` → `"12"` but `1 + 2 + ""` → `"3"`.
- `char` arithmetic: `'a' + 1` is `98` (int), `(char)('a' + 1)` is `'b'`.
- `Integer == Integer` outside −128..127 is `false`; `Long.equals(Integer)` is always `false`.
- **String immutability**: calling `s.trim()` / `s.concat(..)` without assigning does nothing.
- `==` on strings: only compile-time constants (literals, `final` constant variables) are pooled automatically.
- `StringBuilder` methods mutate and return `this`; `sb1.equals(sb2)` compares references.
- `substring`/`delete` end index is **exclusive**; `length()` is a method on `String`/`StringBuilder` but `length` is a field on arrays.
- **Text blocks**: closing-delimiter position changes indentation; trailing spaces stripped; `\s` keeps a space; `\<newline>` joins lines; text block ending `"""` on its own line adds a final `\n`.
- **`Period` vs `Duration`**: `Period` for dates (`LocalDate`), `Duration` for times; using the wrong one throws `UnsupportedTemporalTypeException` at **runtime**.
- `Period.ofYears(1).ofMonths(2)` — static method chaining, result is `P2M`.
- **`LocalDate` immutability**: `date.plusDays(1);` without assignment leaves `date` unchanged.
- **DST arithmetic**: on the spring-forward day a "day" has 23 hours; `plusHours(1)` from 01:30 gives 03:30; on fall-back `plusHours(1)` from 01:30 gives 01:30 with a different offset.
- `LocalDate.of(2025, 2, 30)` compiles but throws `DateTimeException`.
- `Duration` never prints days (`PT24H`), `Period` never prints hours.
- `Math.round(double)` returns `long` — `int r = Math.round(2.5);` does not compile. `Math.round(-2.5)` is `-2`.
- `Math.pow`, `ceil`, `floor`, `sqrt` return `double`.
- Integer `/ 0` throws, floating `/ 0` gives `Infinity` or `NaN`.

## Self-check before practice

- [ ] I can list the promotion rules and say why `short s = 1; s = s * 2;` fails but `s *= 2;` compiles.
- [ ] I can predict the result of `(byte) 200`, `(int) -3.99`, `-7 % 2`, `Math.floorMod(-7, 2)`, `Math.round(-2.5)`.
- [ ] I know when `&&` / `||` skip the right side and how `x++` vs `++x` evaluate inside one expression.
- [ ] I know the `Integer` cache range and when `==` on wrappers unboxes.
- [ ] I can tell which string comparisons with `==` are `true` (literals, constant expressions, `intern()`).
- [ ] I know `substring`, `indexOf`, `replace`, `strip`/`trim`, `isBlank`/`isEmpty`, `split`, `repeat`, `indent`.
- [ ] I can trace a chain of `StringBuilder` `insert`/`delete`/`reverse`/`replace` calls.
- [ ] I can write out the exact content of a text block, including indentation, `\`, `\s` and the final newline.
- [ ] I know which of `LocalDate`/`LocalTime`/`LocalDateTime`/`Instant` accept `Period` or `Duration`.
- [ ] I can predict `plusMonths` at month end and the `toString()` format of `Period` and `Duration`.
- [ ] I can compute DST spring-forward and fall-back results for `plusHours` vs `plusDays`.
- [ ] I know the Java 18–21 additions: `Math.clamp`, `Math.ceilDiv`, `StringBuilder.repeat`, ranged `indexOf`, `splitWithDelimiters`, `Character.isEmoji`.

---

**Practice:** `ocp17-829` chapter 1 (*Handling Date, Time, Text, Numeric and Boolean Values*) + the `java25-gap` files for `d01-values`.
