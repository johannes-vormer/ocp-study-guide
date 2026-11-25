---
title: "D9 — Java I/O, NIO.2 and Serialization"
topic: d09-io
order: 9
---

# D9 — Java I/O, NIO.2 and Serialization

All outputs below were verified on JDK 25 (macOS, a POSIX file system). Paths print with `/`; on Windows the separator and root (`C:\`) differ, but the rules are the same.

## What the exam tests

- Picking the right `java.io` class: byte vs character, low-level vs wrapper, and which wrappings **compile**.
- Reading/writing loops (`read()` returns `-1`, `readLine()` returns `null`), append mode, flushing, `PrintWriter`/`PrintStream` never throwing `IOException`.
- `System.in/out/err` and `Console` (`System.console()` can be `null`, `readPassword()` returns `char[]`).
- Serialization: what is (not) saved, which constructors and initializers run on deserialization, records, `serialVersionUID`, `NotSerializableException`.
- `Path` arithmetic: `getName`/`subpath` indexes, `resolve`, `relativize`, `normalize`, `startsWith`/`endsWith`. These methods are **pure string operations** (except `toRealPath`); they don't touch the disk.
- `Files`: which methods throw which exception, `createDirectory` vs `createDirectories`, `copy`/`move` options, `delete` vs `deleteIfExists`, `list` vs `walk` vs `find`, eager vs lazy reading, attributes.

---

## 1. The `java.io` stream family

Four abstract roots — the name suffix tells you the data type:

| | Input | Output |
|---|---|---|
| **Bytes** (binary) | `InputStream` | `OutputStream` |
| **Characters** (text) | `Reader` | `Writer` |

**Low-level** streams connect directly to a source (file, byte array). **High-level** (wrapper) streams take another stream in their constructor and add features (buffering, objects, formatting, conversion).

| Class | Level | Wraps / opens | Notes |
|---|---|---|---|
| `FileInputStream` / `FileOutputStream` | low | `String` path or `File` | bytes; `new FileOutputStream(name, true)` = append |
| `FileReader` / `FileWriter` | low | `String` path or `File` (+ optional `Charset`, Java 11) | chars; `new FileWriter(name, true)` = append |
| `ByteArrayInputStream`, `StringReader`, … | low | in-memory | |
| `BufferedInputStream` / `BufferedOutputStream` | high | `InputStream` / `OutputStream` | |
| `BufferedReader` / `BufferedWriter` | high | `Reader` / `Writer` | `readLine()`, `lines()`; `newLine()` |
| `ObjectInputStream` / `ObjectOutputStream` | high | `InputStream` / `OutputStream` | serialization |
| `InputStreamReader` / `OutputStreamWriter` | high | `InputStream` / `OutputStream` | **bridge** bytes → chars |
| `PrintStream` | high (also opens files) | `OutputStream`, `String`, `File` | bytes; type of `System.out`/`System.err` |
| `PrintWriter` | high (also opens files) | `Writer`, **`OutputStream`**, `String`, `File` | chars |

Wrapping rule: a byte wrapper needs a byte stream, a char wrapper needs a char stream. Only the bridge classes (and `PrintWriter`, which accepts an `OutputStream`) cross the line. Verified with `javac`:

```java
new BufferedReader(new FileInputStream("f"));                             // DOES NOT COMPILE
new BufferedReader(new InputStreamReader(new FileInputStream("f")));      // OK (bridge)
new ObjectInputStream(new FileReader("f"));                               // DOES NOT COMPILE
new BufferedInputStream(new FileReader("f"));                             // DOES NOT COMPILE
new BufferedWriter(new FileOutputStream("f"));                            // DOES NOT COMPILE
new PrintStream(new FileWriter("f"));                                     // DOES NOT COMPILE
new PrintWriter(new FileOutputStream("f"));                               // OK
new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream("f"))); // OK
new BufferedReader("f");                                                  // DOES NOT COMPILE (no String ctor)
```

Most constructors and methods throw the checked `IOException` (`FileNotFoundException` is a subclass). Closing the outermost wrapper closes the wrapped streams too.

### Core methods

| Method | Returns / behaviour |
|---|---|
| `InputStream.read()` | next byte as `int` 0–255, or **`-1`** at end |
| `Reader.read()` | next char as `int`, or **`-1`** at end |
| `read(byte[]/char[])` | number read, or `-1` |
| `OutputStream.write(int)` | writes only the **low 8 bits** (`write(321)` writes byte 65) |
| `Writer.write(String)` / `write(int)` | writes chars |
| `BufferedReader.readLine()` | line **without** terminator, **`null`** at end (repeatable) |
| `flush()` | pushes buffered data to the target; `close()` flushes first |
| `mark(limit)` / `reset()` / `markSupported()` | `BufferedReader`/`BufferedInputStream` support it; `FileInputStream.markSupported()` is `false` and `reset()` throws `IOException` |
| `skip(n)` | returns how many were actually skipped |
| `InputStream.readAllBytes()` (9), `transferTo(out)` (9), `Reader.transferTo` (10) | convenience |

Classic copy loops:

```java
try (var in = new FileInputStream(src); var out = new FileOutputStream(dst)) {
    int b;
    while ((b = in.read()) != -1) out.write(b);
}
try (var r = new BufferedReader(new FileReader(src));
     var w = new BufferedWriter(new FileWriter(dst, true))) {   // true = append
    String line;
    while ((line = r.readLine()) != null) { w.write(line); w.newLine(); }
}
```

A `BufferedWriter` that is never flushed/closed may write nothing: after `bw.write("hello")` the file length was still 0 until `flush()`.

### `PrintStream` / `PrintWriter`

- `print`, `println`, `printf`/`format` (returns the stream, so calls chain). `println(char[])` prints the characters, other arrays print `[I@1b6d…`.
- They **never throw `IOException`** — errors set an internal flag read with **`checkError()`**. Verified: a `PrintWriter` over a `Writer` whose `write` throws, and printing after `close()`, both just make `checkError()` return `true`.
- `System.out` and `System.err` are `PrintStream`s; `System.in` is an `InputStream` (a `BufferedInputStream` at runtime).
- Trap: `try (var w = new PrintWriter(System.out)) {...}` closes **`System.out`**; later `System.out.println` prints nothing (no exception) and `System.out.checkError()` is `true`.

Reading a line from standard input without `Console`:

```java
var in = new BufferedReader(new InputStreamReader(System.in));
String s = in.readLine();       // null at end of input
```

### `java.io.File` (legacy)

`File` represents a path (the file need not exist). Methods return `boolean`/`null` instead of throwing:

| Call | Missing target / failure |
|---|---|
| `exists()`, `isDirectory()`, `isFile()` | `false` |
| `length()` | `0` |
| `delete()` | `false` (also `false` for a non-empty directory) |
| `mkdir()` | `false` if the parent is missing; `mkdirs()` creates parents |
| `list()` / `listFiles()` | `null` if not a directory |
| `renameTo(File)` | `boolean` |
| `getParent()` on `"a.txt"` | `null` |

Converting: `file.toPath()` and `path.toFile()`.

---

## 2. `Console`

```java
Console c = System.console();
if (c != null) {
    String user = c.readLine("User %s: ", "name");   // prompt is a format string
    char[] pw   = c.readPassword("Password: ");      // echo off; char[] can be wiped
    c.format("Hello %s%n", user).flush();            // format/printf return Console
    PrintWriter w = c.writer(); Reader r = c.reader();
}
```

- `System.console()` returns **`null`** when there is no console device. Verified on JDK 25: it was `null` when output was piped/redirected and non-`null` (with `isTerminal() == true`) when run in a terminal. Always null-check.
- `readLine()` / `readPassword()` return `null` at end of input. `readPassword` returns **`char[]`**, not `String` (so it can be cleared and doesn't linger in the string pool).
- `Console` has **no** `println`/`print`/`readln` methods in Java 25 (verified by reflection) — those live in `java.lang.IO`. `Console` has no public constructor.
- 🆕 **Java 22**: `Console.isTerminal()`. 🆕 **Java 23**: `Locale` overloads `format(Locale, …)`, `printf(Locale, …)`, `readLine(Locale, …)`, `readPassword(Locale, …)`.

### 🆕 Java 25 — `java.lang.IO` (JEP 512)

A small final class for beginners: `IO.println(Object)`, `IO.println()`, `IO.print(Object)`, `IO.readln()`, `IO.readln(String prompt)`. It wraps `System.out` / `System.in`, works whether or not a console exists, and is in `java.lang`, so no import is needed:

```java
void main() {                        // compact source file (Java 25)
    String n = IO.readln("Name? ");  // reads a line from System.in; null at end of input
    IO.println("Hi " + n);
}
```

Piping `Bob` into it prints `Name? Hi Bob`; a second `IO.readln()` returned `null`. Trap: the methods are **not** statically imported into compact source files — a bare `println("x")` does **not compile** (`cannot find symbol`); write `IO.println`.

---

## 3. Serialization

Writes an object graph to bytes with `ObjectOutputStream.writeObject(Object)` and rebuilds it with `ObjectInputStream.readObject()` (returns `Object`, throws `IOException` **and** `ClassNotFoundException`).

Rules:

1. The class must implement the marker interface `java.io.Serializable`, and **every object reachable** through non-transient instance fields must be serializable too — otherwise `NotSerializableException` (a subclass of `IOException`) at write time. Verified: a `Serializable` holder with a field of a non-serializable class fails with `java.io.NotSerializableException: …NoSer`.
2. **`transient`** fields and **`static`** fields are not written. After deserialization a transient field has its **type's default value** (`0`, `false`, `null`) — **not** its initializer value.
3. On deserialization, **constructors and instance initializers of serializable classes do not run**. Java calls the **no-arg constructor of the first non-serializable superclass** (e.g. `Object`) — and that constructor plus initializers of all non-serializable ancestors **do** run. If that class has no accessible no-arg constructor → `InvalidClassException: …; no valid constructor`.
4. `serialVersionUID` (`private static final long`) identifies the class version. A mismatch between the stream and the local class → `InvalidClassException` (verified: `stream classdesc serialVersionUID = 1, local class serialVersionUID = 2`). Adding a field while keeping the same UID still deserializes (new field gets its default). Without an explicit UID one is computed from the class structure, so any change can break compatibility.
5. **Records** (`record R(...) implements Serializable`) are deserialized by calling the **canonical constructor**, so its validation runs again. A record can't mark components `transient` (`record R(transient int x)` → "record components cannot have modifiers") and can't declare other instance fields, so all components are serialized.
6. Reading past the last object throws **`EOFException`** — `readObject()` doesn't return `null` at end (it returns `null` only if `null` was written). Loop with `while (true)` + `catch (EOFException e)`.
7. The same object written twice to one stream is written once; reading gives the **same reference** back twice, with the state from the first write.

Verified example:

```java
class Animal { String kind = "animal";
    Animal() { System.out.println("Animal()"); kind = "set-by-ctor"; } }
class Mammal extends Animal implements Serializable { String m = "m-init";
    Mammal() { System.out.println("Mammal()"); } }
class Cat extends Mammal {
    private static final long serialVersionUID = 1L;
    String name; transient int lives = 9; transient String tag = "t";
    static int count = 0; int age = 5;
    { System.out.println("Cat init block"); }
    Cat(String n) { System.out.println("Cat(String)"); name = n; }
}
// new Cat("Tom") prints Animal(), Mammal(), Cat init block, Cat(String)
// then set kind="changed", m="changed-m", lives=3, count=42, age=7, serialize, deserialize:
// prints only:  Animal()
// fields:       name=Tom lives=0 tag=null age=7 kind=set-by-ctor m=changed-m count=42
```

- `kind` belongs to the non-serializable `Animal`, so it is re-initialized by `Animal()`.
- `m` (serializable `Mammal`) keeps the stored value; `Mammal()` did not run.
- `count == 42` only because static state lives in the current JVM — it wasn't read from the stream.

Customization hooks (rarely tested): `private void writeObject(ObjectOutputStream)` / `private void readObject(ObjectInputStream)`, `readResolve`, `writeReplace`; `Externalizable` needs a public no-arg constructor.

---

## 4. NIO.2 `Path`

Creating: `Path.of("a", "b", "c")` (Java 11) or `Paths.get("a", "b", "c")` → `a/b/c`; `FileSystems.getDefault().getPath(...)`. Duplicate/trailing separators are dropped: `Path.of("a//b/")` → `a/b`. `Path` is an interface; it is immutable and does not need the file to exist.

### Navigation (example `p = /zoo/./animals/../birds/parrot.txt`)

| Call | Result |
|---|---|
| `getFileName()` | `parrot.txt` (a `Path`) |
| `getParent()` | `/zoo/./animals/../birds` (not normalized) |
| `getRoot()` | `/` — `null` for a relative path |
| `getNameCount()` | `6` (root is not a name; `.` and `..` are) |
| `getName(0)` | `zoo` (0-based) |
| `subpath(1, 3)` | `./animals` — start inclusive, end exclusive, always relative |
| `subpath(0, 0)` / `subpath(0, 9)` | `IllegalArgumentException` |
| `normalize()` | `/zoo/birds/parrot.txt` |

More edge cases: `Path.of("a").getParent()` → `null`; `Path.of("/").getFileName()` → `null`, `getNameCount()` → `0`; `Path.of("../../a/./b/..").normalize()` → `../../a` (leading `..` of a relative path stay).

### `resolve`, `relativize`, `startsWith`/`endsWith`

| Expression | Result |
|---|---|
| `Path.of("a/b").resolve("c")` | `a/b/c` |
| `Path.of("a/b").resolve("/x/y")` | `/x/y` (absolute argument wins) |
| `Path.of("/x").resolve("../y")` | `/x/../y` (no normalization!) |
| `Path.of("/a/b").relativize(Path.of("/a/c/d"))` | `../c/d` |
| `Path.of("fish.txt").relativize(Path.of("friendly/birds.txt"))` | `../friendly/birds.txt` |
| `Path.of("a/b/c/d").relativize(Path.of("a/b"))` | `../..` |
| `Path.of("/a").relativize(Path.of("b"))` | **`IllegalArgumentException`** (absolute vs relative) |
| `p.relativize(p)` | empty path |
| `Path.of("/a/bc/d").startsWith("/a/b")` | **`false`** — compares whole name elements, not characters |
| `Path.of("/a/bc/d").startsWith("/a/bc")` | `true` |
| `Path.of("/a/bc/d").endsWith("d")` | `true` |
| `Path.of("/a/bc/d").endsWith("/d")` | `false` (an absolute path only matches from the root) |
| `Path.of("a/b").startsWith("/a")` | `false` |
| `Path.of("a/../b").equals(Path.of("b"))` | `false` — `equals` doesn't normalize or touch the disk |

Rule: `p.resolve(p.relativize(q))` gets you to `q`. On Windows `relativize` also throws `IllegalArgumentException` when the roots (drive letters) differ.

### Absolute and real paths

- `toAbsolutePath()` — prefixes the current working directory; no disk access; never fails for a missing file.
- `toRealPath(LinkOption...)` — **checks the file system**: resolves symlinks, removes `.`/`..`, throws **`NoSuchFileException`** if missing (it declares `IOException`). It is the only `Path` method above that throws `IOException`.

---

## 5. NIO.2 `Files`

All methods are `static`. Nearly all throw `IOException` — except the test methods `exists`, `notExists`, `isDirectory`, `isRegularFile`, `isSymbolicLink`, `isReadable`, `isWritable`, `isExecutable`, which declare no checked exception and simply return `false` when the file is missing. Exception: `Files.isHidden(p)` **does** declare `IOException` (calling it without handling doesn't compile).

### Creating, copying, moving, deleting (all verified)

| Call | Result |
|---|---|
| `createDirectory(x/y)` when `x` is missing | `NoSuchFileException` |
| `createDirectory(x)` when `x` exists | `FileAlreadyExistsException` |
| `createDirectories(x/y)` | creates parents; **no exception if it already exists** |
| `createFile(f)` when `f` exists | `FileAlreadyExistsException` |
| `copy(src, existingTarget)` | `FileAlreadyExistsException` |
| `copy(src, target, StandardCopyOption.REPLACE_EXISTING)` | overwrites |
| `copy(f, f)` (same file) | no-op, no exception |
| `copy(src, missingDir/f)` | `NoSuchFileException` |
| `copy(dir, dir2)` on a non-empty directory | creates an **empty** `dir2` — copy is shallow |
| `copy(InputStream, Path)` / `copy(Path, OutputStream)` | returns bytes copied (`long`) |
| `move(missing, t)` | `NoSuchFileException` |
| `move(a, existing)` | `FileAlreadyExistsException` (unless `REPLACE_EXISTING`) |
| `move(dir, nonEmptyDir, REPLACE_EXISTING)` | `DirectoryNotEmptyException` |
| `move(a, b, StandardCopyOption.ATOMIC_MOVE)` | atomic rename (or `AtomicMoveNotSupportedException`) |
| `delete(missing)` | **`NoSuchFileException`** |
| `deleteIfExists(missing)` | returns **`false`** |
| `delete(nonEmptyDir)` / `deleteIfExists(nonEmptyDir)` | **`DirectoryNotEmptyException`** (both!) |

`move` also renames; moving a whole directory works when it is a rename on the same file store. `StandardCopyOption.COPY_ATTRIBUTES` copies timestamps etc.; `LinkOption.NOFOLLOW_LINKS` copies/moves the link itself.

### Comparing

- `Files.isSameFile(a, b)`: if `a.equals(b)` it returns `true` **without checking existence** (verified with a missing path). Otherwise it checks the file system (follows links, ignores `.`/`..`) and throws `NoSuchFileException` if a file is missing. It never compares content.
- `Files.mismatch(a, b)` (Java 12) compares content: `-1` if identical, else the first differing byte index.

### Reading and writing

| Method | Notes |
|---|---|
| `readAllLines(p)` → `List<String>` | eager; whole file in memory |
| `lines(p)` → `Stream<String>` | **lazy**; close it (try-with-resources) |
| `readString(p)` → `String` (11) | eager |
| `readAllBytes(p)` → `byte[]` | |
| `writeString(p, cs, opts…)` (11), `write(p, byte[])`, `write(p, Iterable<String>)` | `write(p, List.of("a","b"))` writes `a\nb\n` |
| `newBufferedReader(p)` / `newBufferedWriter(p, opts…)` | UTF-8 by default |

Default open options for writing are `CREATE`, `TRUNCATE_EXISTING`, `WRITE` — `newBufferedWriter(p)` overwrites existing content. Other `StandardOpenOption` traps (verified):

- `APPEND` alone on a missing file → `NoSuchFileException` (add `CREATE`).
- `CREATE_NEW` on an existing file → `FileAlreadyExistsException`.
- Reading a missing file (`readAllLines`, `newBufferedReader`, …) → `NoSuchFileException` (not `FileNotFoundException`, which is the `java.io` version).

### Listing and searching

| Method | Depth | Notes |
|---|---|---|
| `Files.list(dir)` | 1 level, **excludes** `dir` itself | not recursive; on a file → `NotDirectoryException` |
| `Files.walk(start)` | unlimited (`Integer.MAX_VALUE`) | depth-first, **includes** `start` |
| `Files.walk(start, maxDepth)` | `0` = only `start`; `1` = `start` + children | |
| `Files.find(start, maxDepth, BiPredicate<Path, BasicFileAttributes>)` | `maxDepth` **required** | predicate gets attributes for free |

All return a lazy `Stream<Path>` holding open directory handles → use try-with-resources. They throw the checked `IOException` when called (e.g. `NoSuchFileException` for a missing start), but errors **during** traversal surface as `UncheckedIOException`.

Symbolic links: `walk`/`find` do **not** follow links by default (a link to a directory is reported, not entered). With `FileVisitOption.FOLLOW_LINKS`, a link cycle causes `FileSystemLoopException` (verified: thrown wrapped in `UncheckedIOException`).

```java
try (Stream<Path> s = Files.find(Path.of("x"), 10,
        (p, attrs) -> attrs.isRegularFile() && p.toString().endsWith(".txt"))) {
    s.forEach(System.out::println);
}
```

### Attributes

| Call | Notes |
|---|---|
| `Files.size(p)` | bytes (`long`); missing → `NoSuchFileException` |
| `Files.getLastModifiedTime(p)` | returns `FileTime` (not `Instant`/`long`; use `toMillis()`/`toInstant()`) |
| `Files.setLastModifiedTime(p, FileTime)` | |
| `Files.isDirectory(p, LinkOption.NOFOLLOW_LINKS)` | link to a dir → `false` with NOFOLLOW, `true` without |
| `Files.readAttributes(p, BasicFileAttributes.class)` | read-only snapshot: `size()`, `isDirectory()`, `isRegularFile()`, `isSymbolicLink()`, `isOther()`, `creationTime()`, `lastModifiedTime()`, `lastAccessTime()`, `fileKey()` |
| `Files.readAttributes(p, "size,isDirectory")` | `Map<String,Object>` |
| `Files.getFileAttributeView(p, BasicFileAttributeView.class)` | **modifiable** view: `readAttributes()`, `setTimes(lastModified, lastAccess, create)` (`null` = unchanged) |

Also `DosFileAttributes` / `PosixFileAttributes` subtypes exist. `LinkOption.NOFOLLOW_LINKS` is accepted by `exists`, `isDirectory`, `isRegularFile`, `readAttributes`, `getLastModifiedTime`, `toRealPath`; `FileVisitOption.FOLLOW_LINKS` is for `walk`/`find`.

### `java.io.File` vs NIO.2 at a glance

| | `File` | `Files` |
|---|---|---|
| delete missing | `false` | `delete` throws / `deleteIfExists` → `false` |
| mkdir with missing parent | `false` | `NoSuchFileException` |
| list a non-directory | `null` | `NotDirectoryException` |
| missing file on open | `FileNotFoundException` (`FileInputStream`) | `NoSuchFileException` |

---

## 🆕 Since Java 17

| Version | Change |
|---|---|
| **🆕 Java 18** (JEP 400) | **UTF-8 is the default charset** everywhere (`Charset.defaultCharset()`), so `FileReader`/`FileWriter`/`PrintStream` without a charset now use UTF-8 regardless of OS. `PrintStream.charset()` added. |
| **🆕 Java 22** | `Console.isTerminal()`. |
| **🆕 Java 23** | `Console` methods with an explicit `Locale` (`format`, `printf`, `readLine`, `readPassword`). |
| **🆕 Java 24** | `Reader.of(CharSequence)` — a `Reader` over a string/`StringBuilder`. |
| **🆕 Java 25** | `Reader.readAllLines()` → `List<String>` and `Reader.readAllAsString()` → `String` (reads the rest of the reader; verified `[a, b, c]` for `"a\nb\r\nc"`). |
| **🆕 Java 25** (JEP 512) | `java.lang.IO` with `println`/`print`/`readln` (see §2) — used with compact source files. |

Serialization, `Path` and `Files` APIs are otherwise unchanged since 17.

---

## Exam traps

- `readLine()` ends with `null`, `read()` ends with `-1`, `readObject()` ends with **`EOFException`**.
- Byte wrapper around a char stream (or vice versa) → **does not compile**. `BufferedReader` has no `String`/`File` constructor.
- `new FileWriter(f)` truncates; `new FileWriter(f, true)` appends. Same for `FileOutputStream`.
- `PrintWriter`/`PrintStream` swallow `IOException` → `checkError()`. Wrapping `System.out` in try-with-resources closes it.
- `System.console()` may be `null` → `NullPointerException` on `readLine`. `readPassword()` → `char[]`.
- Deserialization: serializable classes' constructors, initializers and field initializers **don't run**; transient → default value (not the initializer value); the first non-serializable superclass's no-arg constructor **does** run. Records → canonical constructor runs.
- `static` fields are not serialized; a value seen after deserialization comes from the current JVM.
- A non-serializable field object → `NotSerializableException` at runtime (it compiles fine).
- `getName`/`subpath` are 0-based and exclude the root; `subpath` end index is exclusive; out of range → `IllegalArgumentException`.
- `resolve` with an absolute argument returns the argument. `resolve`/`relativize`/`equals` do not normalize.
- `relativize` mixing absolute and relative → `IllegalArgumentException`.
- `startsWith("/a/b")` on `/a/bc` is `false`; `endsWith("/d")` on `/a/bc/d` is `false`.
- `toAbsolutePath()` never touches the disk; `toRealPath()` does and throws `NoSuchFileException`.
- `createDirectory` vs `createDirectories`; `delete` vs `deleteIfExists` (both fail on a non-empty directory).
- `Files.copy` of a directory copies only the directory, not its contents.
- `Files.list` is not recursive and excludes the start; `walk` includes the start; `find` requires `maxDepth`; `walk(p, 0)` returns only `p`.
- `Files.lines`/`list`/`walk`/`find` streams should be closed; `readAllLines` is eager.
- `isSameFile(p, p)` is `true` even if `p` doesn't exist.
- `getLastModifiedTime` returns `FileTime`.

---

## Self-check before practice

- [ ] I can sort any `java.io` class into byte/char and low/high level and spot a wrapping that doesn't compile.
- [ ] I know what `read()`, `readLine()`, `readObject()` do at end of data.
- [ ] I know `PrintWriter` never throws `IOException` and what `checkError()` is for.
- [ ] I know `System.console()` can be `null` and that `readPassword()` returns `char[]`.
- [ ] I can predict field values after deserialization (transient, static, initializers, non-serializable superclass).
- [ ] I know which constructor runs on deserialization of a class vs a record, and when `InvalidClassException` / `NotSerializableException` happen.
- [ ] I can compute `getName`, `subpath`, `getParent`, `getRoot`, `normalize`, `resolve`, `relativize` by hand.
- [ ] I know the `startsWith`/`endsWith` element-wise semantics.
- [ ] I can name the exception for: missing file, existing target, non-empty directory, mixed relativize.
- [ ] I know the differences between `list`, `walk`, `find` and what `maxDepth` 0/1 mean.
- [ ] I know `BasicFileAttributes` vs `BasicFileAttributeView` and `NOFOLLOW_LINKS` vs `FOLLOW_LINKS`.
- [ ] I know the Java 18–25 additions: UTF-8 default, `Console.isTerminal`, `Reader.of`, `Reader.readAllLines/readAllAsString`, `IO.println/readln`.

**Practice:** `ocp17-829` → *Chapter 9: Using Java I/O API* (40 questions), then the `java25-gap` set for D9.
