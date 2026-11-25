---
title: "D10 — Localization"
topic: d10-localization
order: 10
---

# D10 — Localization

All outputs below were verified on the local JDK 25 (CLDR locale data). Formatting output depends on the CLDR version shipped with the JDK, so some strings differ from older books — notably JDK 20+ puts a **narrow no-break space (U+202F)** before `AM`/`PM` in localized `en_US` times. In this document `⍽` marks U+202F and `·` marks a normal no-break space (U+00A0) where it matters. The exam will normally use a plain space; don't let it worry you, but don't be surprised if your local output doesn't match an old answer key.

The machine's default locale influences every un-parameterized call, so all examples pass an explicit `Locale` (the check machine had default `en_NL`, which uses `,` as decimal separator).

## What the exam tests

- Building `Locale` objects (`Locale.of`, constants, `Builder`, `forLanguageTag`), their `toString` format, and default-locale categories.
- The `ResourceBundle` search order (requested locale → default locale → base bundle), parent chain, and `MissingResourceException`.
- `MessageFormat` placeholders and single-quote rules.
- `NumberFormat` factories, their default fraction digits, **`HALF_EVEN` rounding**, and how `parse` stops at the first bad character.
- `CompactNumberFormat` SHORT vs LONG and rounding.
- `DecimalFormat` patterns `0` vs `#`.
- `DateTimeFormatter` pattern letters, quoting, `ofLocalized*` with `FormatStyle`, and the exceptions when the temporal lacks a field.

---

## 1. `Locale`

A locale = **language** (lowercase, ISO 639, required in practice) + optional **country/region** (uppercase, ISO 3166) [+ variant/script]. `toString()` → `language_COUNTRY`, e.g. `en_US`, `fr`, `de_DE`. The language tag form (`toLanguageTag()`) uses a hyphen: `en-US`.

| Way to get one | Example → `toString()` |
|---|---|
| Constants | `Locale.US` → `en_US`, `Locale.UK` → `en_GB`, `Locale.GERMANY` → `de_DE`, `Locale.FRANCE` → `fr_FR`, `Locale.CANADA_FRENCH` → `fr_CA`; language-only: `Locale.ENGLISH` → `en`, `Locale.GERMAN` → `de`; `Locale.ROOT` → empty |
| 🆕 **Java 19** `Locale.of(lang)` / `of(lang, country)` / `of(lang, country, variant)` | `Locale.of("en","US")` → `en_US`; `Locale.of("EN","us")` → `en_US` (case normalized) |
| `new Locale("en","US")` | still works but **deprecated since Java 19** (`javac -Xlint` warns) — the exam may still show it |
| `new Locale.Builder().setLanguage("en").setRegion("US").build()` | `en_US`; setter order doesn't matter; **validates** — `setRegion("USA")` → `IllformedLocaleException` |
| `Locale.forLanguageTag("en-US")` | `en_US`; `forLanguageTag("en_US")` (underscore = ill-formed) → **empty locale**, no exception |
| `Locale.getDefault()` | JVM default |

Traps:
- `Locale.of("US")` is the **language** `us` (prints `us`), not the country. `Locale.of("", "US")` → `_US`.
- `Locale.of` and the constructors don't validate: `Locale.of("xx","YY")` → `xx_YY` happily.
- There is no `Locale.Builder().setCountry(...)` — it's `setRegion`. Locales are immutable; there are no setters on `Locale`.
- Compare with `equals`, not `==`.

Display methods: `Locale.GERMANY.getDisplayCountry(Locale.US)` → `Germany`; `Locale.GERMANY.getDisplayLanguage(Locale.GERMANY)` → `Deutsch`; `getLanguage()` → `de`, `getCountry()` → `DE`.

### Default locale and categories

```java
Locale.setDefault(Locale.US);                                  // sets DISPLAY and FORMAT too
Locale.setDefault(Locale.Category.FORMAT, Locale.GERMANY);     // only FORMAT
Locale.getDefault();                                           // en_US
Locale.getDefault(Locale.Category.FORMAT);                     // de_DE
Locale.getDefault(Locale.Category.DISPLAY);                    // en_US
NumberFormat.getInstance().format(1234.5);                     // 1.234,5   (uses FORMAT)
String.format("%,.2f", 1234.567);                              // 1.234,57  (uses FORMAT)
DateTimeFormatter.ofPattern("MMMM").format(march);             // März      (uses FORMAT)
Locale.GERMANY.getDisplayCountry();                            // Germany   (uses DISPLAY)
```

- **`FORMAT`** → formatting numbers, currencies, percentages, dates.
- **`DISPLAY`** → names shown to the user (display names of locales, etc.).
- `setDefault(Locale)` (one argument) resets **both** categories; `setDefault(Category, Locale)` only one. `setDefault` affects only the running JVM.

---

## 2. Resource bundles

A resource bundle is a set of key/value pairs for one locale, found by **base name + locale suffix**:
`Zoo.properties` (base / default bundle), `Zoo_en.properties`, `Zoo_en_US.properties`, `Zoo_fr_CA.properties`, or Java classes `Zoo_en extends ListResourceBundle`.

```java
ResourceBundle rb = ResourceBundle.getBundle("Zoo", Locale.of("fr", "CA"));
String s = rb.getString("hello");                     // MissingResourceException if absent
rb.keySet();                                          // includes keys of parents
rb.containsKey("x");
ResourceBundle.getBundle("Zoo");                      // uses Locale.getDefault()
```

### Lookup order — memorize

Requested `fr_CA`, default locale `en_US`:

1. `Zoo_fr_CA` (Java class first, then `.properties`)
2. `Zoo_fr`
3. `Zoo_en_US` ← default locale
4. `Zoo_en`
5. `Zoo` ← base bundle
6. none → `MissingResourceException`

Java drops the most specific part first (variant → country), then switches to the **default locale** only if it found nothing for the requested locale except the base bundle, and finally takes the base bundle. A `.class` bundle beats a `.properties` bundle of the same name (verified: `Mixed_en.class` won over `Mixed_en.properties`).

Verified with files `Zoo`, `Zoo_en`, `Zoo_en_US`, `Zoo_fr`, `Zoo_fr_CA` and default `en_US`:

| Requested | Bundle chosen |
|---|---|
| `fr_CA` | `Zoo_fr_CA` |
| `fr_FR` | `Zoo_fr` |
| `en_GB` | `Zoo_en` |
| `de_DE` | `Zoo_en_US` (fell back to default locale) |
| `Locale.ROOT`, or no-arg `getBundle` with default `ja_JP` | `Zoo` |

More verified cases: only `OnlyEn_en.properties` exists, request `fr` → found via default `en_US`; but with default `de_DE` → `MissingResourceException: Can't find bundle for base name OnlyEn, locale fr_FR`.

### Parent chain — where keys come from

Once a bundle is selected, keys missing in it are looked up in its **parents**: the less specific bundles of the **same** locale, then the base bundle — **never** the default locale's bundles.

- Chosen `Zoo_fr_CA` → may use keys from `Zoo_fr_CA`, `Zoo_fr`, `Zoo`.
- Key only in `Zoo_en` while using `fr_CA` → `MissingResourceException` (verified), even though default is `en_US`.
- `keySet()` of `Zoo_fr` = own keys + `Zoo` keys.

### Properties file syntax

```properties
# comment       ! also a comment
hello=Hello
name:Ann
city Paris
spaced   :   value with spaces  
multi=one \
     two
```

`key=value`, `key:value` or `key value`. Whitespace around the separator and leading whitespace of a value are ignored; **trailing spaces are kept** (`spaced` → `"value with spaces  "`). A trailing `\` continues the line, the next line's leading whitespace is dropped (`multi` → `"one two"`).

Other API notes: values are `String`s for properties bundles; Java class bundles can hold any object (`getObject("n")` → `42`; `getString("n")` on an `Integer` → `ClassCastException`). `getBundle` caches: two calls with the same name/locale returned the same instance. `java.util.Properties` (not a bundle): `getProperty("b")` → `null`, `getProperty("b", "dflt")` → `dflt`.

---

## 3. `MessageFormat`

Placeholders `{index}` (0-based), optionally `{index,type,style}` with types `number`, `date`, `time`, `choice`.

```java
MessageFormat.format("Hello {0}, you are {1}", "Ann", 42);   // Hello Ann, you are 42
MessageFormat.format("{1} {0} {1}", "a", "b");               // b a b     (any order, repeatable)
MessageFormat.format("{0} and {1}", "only");                 // only and {1}   (missing arg stays literal)
MessageFormat.format("{0}", "a", "b");                       // a         (extra args ignored)
MessageFormat.format("{0}", 1234567.891);                    // 1,234,567.891 (default FORMAT locale, here US)
MessageFormat.format("{0,number,integer}", 2.5);             // 2         (HALF_EVEN)
MessageFormat.format("{0,number,percent}", 0.5);             // 50%
MessageFormat.format("{0}", (Object) null);                  // null
new MessageFormat("{0,number}", Locale.GERMANY).format(new Object[]{1234.5});  // 1.234,5
```

**Single quotes** start a quoted (literal) section:

| Pattern | Result |
|---|---|
| `"It's {0}"` with `"x"` | `Its {0}` — the lone `'` opens a quote, swallowing `{0}` |
| `"It''s {0}"` | `It's x` — `''` is one literal quote |
| `"'{0}' is {0}"` | `{0} is x` |

Bad patterns: `"{a}"` → `IllegalArgumentException: can't parse argument number: a`; `"{0"` → `IllegalArgumentException: Unmatched braces in the pattern.`

---

## 4. `NumberFormat`

All are abstract-class factories; with no argument they use the default `FORMAT` locale. The returned object is a `DecimalFormat` (or `CompactNumberFormat`).

| Factory | Default fraction digits | `1234.5678` in `Locale.US` |
|---|---|---|
| `getInstance()` = `getNumberInstance()` | 0–3 | `1,234.568` |
| `getCurrencyInstance()` | 2 (per currency) | `$1,234.57` |
| `getPercentInstance()` | 0 (value × 100) | `0.5` → `50%`, `2.5` → `250%` |
| `getIntegerInstance()` | 0 | `1,235` |
| `getCompactNumberInstance(locale, style)` | 0 | see §5 |

Same value in other locales: `de_DE` number `1.234,568`, currency `1.234,57·€`; `fr_FR` number `1⍽234,568`; `en_GB` currency `£1,234.57`; negative US currency `-$3.50`; `de_DE` percent of 0.5 → `50·%`.

**Rounding is `HALF_EVEN`** (banker's rounding) — ties go to the even neighbor:

| `getIntegerInstance(Locale.US).format(x)` | 0.5 | 1.5 | 2.5 | -2.5 | 2.51 |
|---|---|---|---|---|---|
| result | `0` | `2` | `2` | `-2` | `3` |

`getCurrencyInstance(Locale.US).format(1.125)` → `$1.12`. Rounding works on the **exact binary value** of the `double`: `1.005` is really `1.00499…` → `$1.00`; `0.255` is really `0.25500…04` → `26%`. Exam questions use exactly representable values (x.5, x.25, x.125, …).

Change behaviour with `setMaximumFractionDigits`, `setMinimumFractionDigits`, `setRoundingMode(RoundingMode.HALF_UP)`, `setGroupingUsed(false)`, `setParseIntegerOnly`.

Contrast: **`String.format` uses `HALF_UP`**: `String.format(Locale.US, "%.1f %.1f", 0.25, 1.25)` → `0.3 1.3`.

### Parsing

`parse(String)` returns a `Number` and declares the checked **`ParseException`**.

| Call (`Locale.US` unless stated) | Result |
|---|---|
| `getInstance().parse("1,234.5x")` | `1234.5` — stops at first invalid char, rest ignored |
| `getInstance().parse("x12")` | `ParseException` (invalid at index 0) |
| `getInstance().parse(" 5")` | `ParseException` (leading space) |
| `getInstance().parse("1.234,5")` (German text, US format) | `1.234` — see note |
| `getInstance(Locale.GERMANY).parse("1.234,5")` | `1234.5` |
| `getInstance().parse("1,23")` | `123` (grouping separators aren't position-checked) |
| `getInstance().parse("1234")` / `"12.0"` / `"12.5"` | `Long` / `Long` / `Double` |
| `getCurrencyInstance().parse("$5.25")` | `5.25` |
| `getCurrencyInstance().parse("5.25")` | `ParseException` — symbol required |
| `getPercentInstance().parse("50%")` | `0.5` |
| `getPercentInstance().parse("50")` | `ParseException` |
| `getIntegerInstance().parse("3.7")` | `3` (integer-only parsing stops at `.`) |

Note on the German-text row: the US format reads `1.234` as a decimal number, then hits `,` after a decimal point and stops → `1.234`.

🆕 **Java 23**: `NumberFormat.setStrict(true)` / `isStrict()` — strict parsing rejects trailing garbage: `"1,234.5x"` → `ParseException`. Default is lenient.

---

## 5. `CompactNumberFormat` (Java 12)

```java
var s = NumberFormat.getCompactNumberInstance(Locale.US, NumberFormat.Style.SHORT);
var l = NumberFormat.getCompactNumberInstance(Locale.US, NumberFormat.Style.LONG);
```

| Value | SHORT | LONG |
|---|---|---|
| 999 | `999` | `999` |
| 1_000 | `1K` | `1 thousand` |
| 1_499 | `1K` | `1 thousand` |
| 1_500 | `2K` | `2 thousand` |
| 2_500 | `2K` | `2 thousand` (HALF_EVEN) |
| 3_500 | `4K` | `4 thousand` |
| 999_999 | `1M` | `1 million` (rounds into next unit) |
| 1_250_000 | `1M` | `1 million` |
| 314_159_265_358 | `314B` | `314 billion` |
| 10^15 | `1000T` | `1000 trillion` (no bigger unit) |
| -1_500 | `-2K` | |

- No-arg `getCompactNumberInstance()` = default locale + **SHORT** (`1_500_000` → `2M`).
- Default: **0 fraction digits, `HALF_EVEN`**. `setMaximumFractionDigits(2)` → `1_234_567` → `1.23M`.
- Locale-specific: `de_DE` SHORT `1_200_000` → `1·Mio.`, LONG → `1 Million`; `de_DE` SHORT `1200` → `1.200` (German has no short "thousand").
- It parses too: SHORT `parse("1K")` → `1000`; LONG `parse("2 million")` → `2000000`.

---

## 6. `DecimalFormat` patterns

`new DecimalFormat(pattern)` (uses the default `FORMAT` locale's symbols). `0` = always print a digit (pads with zeros); `#` = print only if needed. `,` grouping, `.` decimal point, `%` multiply by 100, `E` exponent, `'…'` literal text. Rounding: `HALF_EVEN`.

| Pattern | `1234.5` → |
|---|---|
| `###,###.##` | `1,234.5` |
| `000,000.00` | `001,234.50` |
| `#.#` / `0.0` | `1234.5` |
| `#` | `1234` (tie → even) |
| `$#,##0.00` | `$1,234.50` |
| `#,###,###.0#` | `1,234.5` |
| `0.###E0` | `1.234E3` |
| `00.##%` | `123450%` |
| `'#'0` | `#1234` |

Details: `#` formats 0 as `0`; `#.##` is treated as `#0.##`, so `0.5` → `0.5`, but `.##` gives `.5`. Ties: `#` of 2.5 → `2`, of 3.5 → `4`; `#.#` of 2.25 → `2.2`. Invalid pattern (`"#.0.0"`) → `IllegalArgumentException`. A `DecimalFormat` created under a German default locale prints `#,##0.00` of `1234.5` as `1.234,50` — the symbols are localized, the pattern letters are not.

---

## 7. `DateTimeFormatter`

`DateTimeFormatter.ofPattern(pattern[, locale])`. Without a locale it uses the default `FORMAT` locale. `date.format(f)` and `f.format(date)` are equivalent. Formatters are immutable and thread-safe; `withLocale(...)` returns a copy.

### Pattern letters (example `2025-03-07T14:05:09`, `Locale.US`)

| Letter | Meaning | Examples |
|---|---|---|
| `y` / `u` | year (of era) / proleptic year | `yy` → `25`, `y`/`yyy`/`yyyy` → `2025` |
| `M` | month | `M` → `3`, `MM` → `03`, `MMM` → `Mar`, `MMMM` → `March`, `MMMMM` → `M`; 6+ → `IllegalArgumentException: Too many pattern letters` |
| `d` / `D` | day of month / day of year | `d` → `7`, `dd` → `07`, `D` → `66` |
| `E` | day of week | `EEE` → `Fri`, `EEEE` → `Friday` |
| `h` / `K` | hour 1–12 / 0–11 | `hh` → `02` |
| `H` / `k` | hour 0–23 / 1–24 | `HH` → `14`; at 00:30: `HH`→`00`, `kk`→`24`, `K`→`0`, `h`→`12` |
| `m` / `s` | minute / second | `m` → `5`, `mm` → `05` |
| `S` | fraction of second | `SSS` → `000` |
| `a` | AM/PM | `PM` (locale-dependent text) |
| `z`, `VV`, `Z`, `XXX`, `O` | zone name / zone id / offsets | need a zone: `z` → `EST`, `VV` → `America/New_York` |
| `'…'` | literal text | `''` = one quote |

Uppercase `MM` = month; lowercase `mm` = minute — the classic trap. `Y` = week-based year, `w` = week of year — usually wrong for normal dates.

Quoting:

```java
DateTimeFormatter.ofPattern("h 'o''clock' a", Locale.US).format(dt);   // 2 o'clock PM
DateTimeFormatter.ofPattern("'Today is' D").format(dt);                 // Today is 66
DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm").format(dt);           // 2025-03-07T14:05
DateTimeFormatter.ofPattern("'abc");     // IllegalArgumentException: Pattern ends with an incomplete string literal
DateTimeFormatter.ofPattern("yyyy b");   // IllegalArgumentException: Unknown pattern letter: b
```

Bad pattern letters fail when **creating** the formatter (`ofPattern`), not when formatting. Unquoted letters are never printed literally: an unknown letter throws, and a known one (e.g. `q` = quarter) silently prints a field — so text must be quoted.

### Missing fields → runtime exception

| Code | Result |
|---|---|
| `ofPattern("hh:mm").format(localDate)` | `UnsupportedTemporalTypeException: Unsupported field: ClockHourOfAmPm` |
| `localDate.format(ofPattern("HH"))` | `UnsupportedTemporalTypeException: Unsupported field: HourOfDay` |
| `ofPattern("yyyy").format(localTime)` | `UnsupportedTemporalTypeException: Unsupported field: YearOfEra` |
| `ofPattern("z").format(localDateTime)` | `DateTimeException: Unable to extract ZoneId …` |
| `ofPattern("yyyy").format(Instant.now())` | `UnsupportedTemporalTypeException` (an `Instant` has no year field) |
| `ISO_LOCAL_DATE.format(localTime)` | `UnsupportedTemporalTypeException: Unsupported field: Year` |

`UnsupportedTemporalTypeException` extends `DateTimeException` (a `RuntimeException`). It compiles fine — the failure is at runtime.

### Localized styles — `FormatStyle` SHORT / MEDIUM / LONG / FULL

`ofLocalizedDate(style)`, `ofLocalizedTime(style)`, `ofLocalizedDateTime(style)` and `ofLocalizedDateTime(dateStyle, timeStyle)`. Verified output, `Locale.US`, `2025-03-07T14:05:09` as a `LocalDateTime`:

| Style | `ofLocalizedDate` | `ofLocalizedTime` | `ofLocalizedDateTime` |
|---|---|---|---|
| SHORT | `3/7/25` | `2:05⍽PM` | `3/7/25, 2:05⍽PM` |
| MEDIUM | `Mar 7, 2025` | `2:05:09⍽PM` | `Mar 7, 2025, 2:05:09⍽PM` |
| LONG | `March 7, 2025` | **`DateTimeException`** | **`DateTimeException`** |
| FULL | `Friday, March 7, 2025` | **`DateTimeException`** | **`DateTimeException`** |

- **LONG/FULL time styles need a time zone** → on a `LocalDateTime`/`LocalTime` they throw `DateTimeException: Unable to extract ZoneId`; use a `ZonedDateTime`.
- `ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.MEDIUM)` → `3/7/25, 2:05:09⍽PM`.
- `ofLocalizedTime(SHORT).format(localDate)` → `UnsupportedTemporalTypeException`.
- Other locales: SHORT date `de_DE` → `07.03.25`, `en_GB` → `07/03/2025`; MEDIUM `fr_FR` → `7 mars 2025`.
- Because of the U+202F, `LocalTime.parse("2:05 PM", ofLocalizedTime(SHORT).withLocale(Locale.US))` with a normal space **fails** (`could not be parsed at index 4`) on JDK 20+; `ofPattern("h:mm a", Locale.US)` parses it fine since its literal is a normal space.

### Parsing dates

```java
LocalDate.parse("03/07/2025", DateTimeFormatter.ofPattern("MM/dd/yyyy"));   // 2025-03-07
LocalDate.parse("3/7/2025",   DateTimeFormatter.ofPattern("MM/dd/yyyy"));   // DateTimeParseException
LocalDate.parse("3/7/2025",   DateTimeFormatter.ofPattern("M/d/yyyy"));     // 2025-03-07
LocalDateTime.parse("03/07/2025", DateTimeFormatter.ofPattern("MM/dd/yyyy")); // DateTimeParseException (no time)
```

---

## 8. `String.format` / `printf` and locales

`String.format(Locale, …)`; without a locale it uses the default `FORMAT` locale.

```java
String.format(Locale.US, "%,.2f", 1234.567);          // 1,234.57
String.format(Locale.GERMANY, "%s %.1f %,d", 1.5, 1.25, 1234567);  // 1.5 1,3 1.234.567
String.format(Locale.FRANCE, "%tB", dt);              // mars
```

`%s` uses `toString()` and is **not** localized (`1.5` stays `1.5`); `%f`/`%d` with `,` flag are localized; rounding is `HALF_UP`.

---

## 🆕 Since Java 17

| Version | Change |
|---|---|
| **🆕 Java 19** | `Locale.of(...)` factories; all `Locale` constructors **deprecated**. |
| **🆕 Java 19** | `DateTimeFormatter.ofLocalizedPattern(String template)` — a locale-appropriate pattern from a skeleton: `"yMMM"` → `Mar 2025` (US), `März 2025` (DE). |
| **🆕 Java 20** (CLDR 42) | Localized `en_US` times use U+202F before AM/PM; some other locale strings changed. Output-only, no API change. |
| **🆕 Java 21** | `Locale.availableLocales()` stream, `Locale.caseFoldLanguageTag`. |
| **🆕 Java 22** | `java.text.ListFormat`: `[a, b, c]` → `a, b, and c` (`Type.STANDARD`) / `a, b, or c` (`Type.OR`). Not on the listed exam objectives — awareness only. |
| **🆕 Java 23** | Strict parsing: `NumberFormat`/`DecimalFormat`/`CompactNumberFormat`/`ChoiceFormat` `setStrict(boolean)` / `isStrict()`. |

---

## Exam traps

- `Locale.of("US")` → language `us`. Lowercase language, uppercase country; `_` in `toString`, `-` in language tags; `forLanguageTag("en_US")` → empty locale.
- `new Locale(...)` compiles (deprecated) — not a compile error.
- `setDefault(Locale)` changes both categories; formatting uses **FORMAT**, display names use **DISPLAY**.
- ResourceBundle: requested locale → **default locale** → base. The default locale is only used if nothing more specific than the base was found for the requested locale. Parents of the chosen bundle never include default-locale bundles.
- `.class` bundle beats `.properties` with the same name.
- Missing key or missing bundle → `MissingResourceException` (unchecked), never `null`.
- `MessageFormat`: lone `'` swallows the rest as literal; missing args print `{n}`.
- `NumberFormat`/`DecimalFormat`/`CompactNumberFormat` round **HALF_EVEN**; `String.format` rounds **HALF_UP**.
- `getInstance()` keeps max **3** fraction digits; currency 2; percent and integer 0. Percent multiplies by 100.
- `parse` stops at the first invalid character without error, but an invalid **first** character → `ParseException` (checked — must be handled!). Currency/percent parsing requires the symbol.
- Compact: `1500` → `2K`, `2500` → `2K`, `999_999` → `1M`. SHORT is the default style.
- `DecimalFormat`: `0` pads, `#` doesn't. `000,000.00` → `001,234.50`.
- `MM` month vs `mm` minute; `hh` 12-hour vs `HH` 24-hour; `MMMMMM` → exception; letters must be quoted.
- Time pattern on a `LocalDate` / date pattern on a `LocalTime` → runtime `UnsupportedTemporalTypeException`; zone letter or LONG/FULL time style without a zone → `DateTimeException`.
- Pattern problems are found in `ofPattern` (`IllegalArgumentException`), field problems in `format` (`DateTimeException`).
- Local JDK output may contain U+202F/U+00A0 — the exam answer with a normal space is still the "correct" one.

---

## Self-check before practice

- [ ] I can create a `Locale` five ways and predict its `toString()`.
- [ ] I know which default-locale category affects `NumberFormat`, `DateTimeFormatter`, `String.format`, and display names.
- [ ] Given a list of bundle files, a requested locale and a default locale, I can name the bundle chosen and which files can supply each key.
- [ ] I know when `MissingResourceException` is thrown and that it's unchecked.
- [ ] I can evaluate `MessageFormat` patterns including `'` and `''`.
- [ ] I know the default fraction digits and HALF_EVEN rounding of each `NumberFormat` factory.
- [ ] I can predict `parse` results, the returned `Number` type, and when `ParseException` occurs.
- [ ] I can format numbers with `CompactNumberFormat` SHORT/LONG and `DecimalFormat` `0`/`#` patterns.
- [ ] I know the main `DateTimeFormatter` letters, quoting rules, and which temporal types each pattern supports.
- [ ] I know what the four `FormatStyle`s produce and that LONG/FULL times need a zone.
- [ ] I know the Java 19–23 additions (`Locale.of`, deprecated constructors, `ofLocalizedPattern`, strict parsing).

**Practice:** `ocp17-829` → *Chapter 11: Implementing Localization* (30 questions), then the `java25-gap` set for D10.
