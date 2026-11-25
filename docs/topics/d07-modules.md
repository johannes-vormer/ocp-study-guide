---
title: "D7 — Packaging, Deploying and Modules"
topic: d07-modules
order: 7
---

# D7 — Packaging, Deploying and Modules

## What the exam tests

- Writing and reading `module-info.java`: every directive, which are legal together, and what each one allows at **compile time** versus **run time (reflection)**.
- Services: who declares `uses`, who declares `provides ... with`, and how `ServiceLoader` finds implementations.
- The three kinds of module (named, automatic, unnamed): what each one can read, what it exports, and how an automatic module gets its name.
- Migration strategies (bottom-up versus top-down).
- Command-line tools and their exact options: `javac`, `java`, `jar`, `jdeps`, `jlink`, `jmod`. Many questions turn on short-form options such as `-p`, `-m` and `-d`, which mean **different things in different tools**.
- Launching code: the single-file source launcher, 🆕 multi-file source programs, 🆕 compact source files and instance `main` methods, and 🆕 `import module`.

All CLI output and behaviour on this page was checked on JDK 25 with a small three-module project (`zoo.api`, `zoo.impl`, `zoo.app`).

---

## 1. Module basics

A **module** is a named group of packages plus a descriptor, `module-info.java`, which sits at the **root** of the module's source folder (not inside a package).

```text
src/
  zoo.api/
    module-info.java          <- module zoo.api { exports zoo.api; }
    zoo/api/Animal.java
    zoo/api/internal/Secret.java
  zoo.impl/
    module-info.java
    zoo/impl/Lion.java
```

Rules:

- Module names follow package-name rules: dot-separated Java identifiers (`zoo.api`, `com.example.app`). `module m.2x {}` does not compile.
- Every module implicitly `requires java.base`. `jar --describe-module` and `java --describe-module` show it as `requires java.base mandated`. Writing `requires java.base;` yourself is legal but redundant. `requires static java.base;` does **not** compile.
- **Cyclic dependencies are not allowed.** If `a requires b` and `b requires a`, javac reports `cyclic dependence involving b`.
- **Split packages are not allowed.** A module may not read the same package from two modules (compile error: `module z reads package p from both x and y`). At run time, two modules on the module path that contain the same package fail to start even when neither exports it (`LayerInstantiationException: Package p in both module x and module y`).
- A package can be exported only if it exists and is not empty. `exports q;` for a missing package gives `package is empty or does not exist: q`.
- Duplicate directives are errors: `requires java.sql;` twice, or `exports p;` plus `exports p to x;`, give `duplicate or conflicting exports`.
- Directives may appear in any order. The module declaration may be preceded by `import` statements, but not by a `package` statement.

---

## 2. `module-info.java` directives

| Directive | Meaning |
|---|---|
| `requires M;` | This module reads M at compile time and run time. |
| `requires transitive M;` | As above, and every module that reads *this* module also reads M ("implied readability"). |
| `requires static M;` | M is required at **compile time only**. At run time it is optional and is not resolved unless something else pulls it in (for example `--add-modules M`). |
| `exports p;` | Public types in `p`, and their public/protected members, are accessible to every module, at compile time and through normal reflection. |
| `exports p to M1, M2;` | Qualified export: only M1 and M2 get that access. |
| `opens p;` | At **run time only**, every module may use **deep reflection** on `p` (private members, `setAccessible(true)`). This gives no compile-time access. |
| `opens p to M1;` | Qualified opens. |
| `open module m { ... }` | Every package in the module is opened. Inside an open module the `opens` directive is not allowed (`'opens' only allowed in strong modules`), but `exports` is. |
| `uses S;` | This module is a **consumer** of service type S and may call `ServiceLoader.load(S.class)`. |
| `provides S with Impl1, Impl2;` | This module supplies implementations of service S. |

A package can be both exported and opened (`exports p; opens p;` compiles).

`exports p to nobody;` where module `nobody` is not observable produces only a **warning** (`module not found: nobody`), not an error.

### What reaches another module: exports vs opens

I tested this with module `lib`, which has `exports lib.pub; opens lib.op;` and a third package, `lib.hid`, with neither directive. Module `app` requires `lib` and reflects on a class in each package. The class has a public field `open` and a private field `secret`.

| Package in `lib` | Compile-time use from `app` | Reflect on public field | `setAccessible(true)` on private field |
|---|---|---|---|
| `exports lib.pub` | yes | yes | **`InaccessibleObjectException`** |
| `opens lib.op` | **no** (compile error) | yes | yes |
| neither (`lib.hid`) | no | `IllegalAccessException` | no |

The command-line overrides, `--add-exports` and `--add-opens`, have the form `<module>/<package>=<target-module>`:

```bash
java -p out --add-opens lib/lib.pub=app --add-exports lib/lib.hid=app -m app/app.Main
```

`--add-reads <module>=<target>` adds a readability edge in the same way. Code on the class path is named with the target `ALL-UNNAMED`, for example `javac --add-exports java.base/sun.security.util=ALL-UNNAMED ...`.

### `requires transitive`: implied readability

```java
module a { exports pa; }
module b { requires transitive a; exports pb; }   // pb's API returns pa.A
module c { requires b; }                         // c can use pa.A without "requires a"
```

With a plain `requires a;` in `b`, code in `c` that names `pa.A` fails with `package pa is not visible`. `c` can still *call* `pb.B.make()` and hold the result as an `Object`.

### `requires static`

```java
module c { requires b; requires static opt; }
```

`c` compiles against `opt`. At run time with `java -p out -m c/pc.C`, `opt` is **not resolved**: `ModuleLayer.boot().findModule("opt")` is empty and `Class.forName("po.O")` throws `ClassNotFoundException`. With `--add-modules opt` the module is resolved and the class loads. The code must therefore check whether the optional module is present before using it.

---

## 3. Services

A service involves four parts:

| Part | Lives in | `module-info` |
|---|---|---|
| **Service provider interface** (the interface or abstract class) | API module | `exports zoo.api;` |
| **Service provider** (implementation) | provider module | `requires zoo.api; provides zoo.api.Animal with zoo.impl.Lion;` |
| **Service locator** (code that calls `ServiceLoader`) | often the API module | `uses zoo.api.Animal;` |
| **Consumer** (code that uses the service) | consumer module | `requires zoo.api;` (plus `uses` if it calls `ServiceLoader` itself) |

On the exam, "part of the service" usually means the **service provider interface plus the service locator**. The consumer and the provider are outside the service.

```java
module zoo.impl {
    requires transitive zoo.api;
    provides zoo.api.Animal with zoo.impl.Lion, zoo.impl.Tiger;
}
module zoo.app {
    requires zoo.api;          // NOT zoo.impl, because consumers never depend on the provider
    uses zoo.api.Animal;
}
```

```java
ServiceLoader<Animal> sl = ServiceLoader.load(Animal.class);   // ServiceLoader<S> is Iterable<S>
for (Animal x : sl) System.out.println(x.name());              // Lion, Tiger(provider)

sl.stream()                                  // Stream<ServiceLoader.Provider<Animal>>
  .map(ServiceLoader.Provider::get)          // Provider.get() creates the instance
  .map(Animal::name)
  .forEach(System.out::println);

Optional<Animal> first = ServiceLoader.load(Animal.class).findFirst();
```

Rules (all verified):

- `ServiceLoader.load(Class)` is lazy and returns `ServiceLoader<S>`, which implements `Iterable<S>`. `stream()` returns `Stream<Provider<S>>`, so you call `Provider::get` (or inspect `type()`) to get instances. `findFirst()` returns `Optional<S>`.
- The implementation class does **not** need to be in an exported package.
- An implementation must be `public` and non-abstract, with a `public` no-arg constructor **or** a `public static provider()` method that returns the service type. If both exist, `provider()` wins. In my test, `Tiger` had a private constructor plus `provider()`, and the loader printed `Tiger(provider)`.
- Compile errors: `X is not public in p`, `the service implementation is an abstract class`, `does not have a default constructor`, and "must be a subtype of the service interface type, or have a public static no-args method named "provider"".
- If the calling module has no `uses` directive, the call fails at run time with `ServiceConfigurationError: ... module zoo.app does not declare 'uses'`. It still compiles.
- **Service binding:** the consumer does not `requires zoo.impl`, yet the provider is found because resolution "binds" providers that are on the module path. `--show-module-resolution` prints `zoo.app binds zoo.impl`. With `--limit-modules zoo.app`, the provider is not observable and `findFirst()` is empty.
- Adding a new provider needs **no recompilation** of the consumer or the API. You only put the new provider module on the module path.
- On the class path, providers are registered through `META-INF/services/<fully.qualified.Interface>` files instead of `provides`.

---

## 4. Named, automatic and unnamed modules

| | Named module | Automatic module | Unnamed module |
|---|---|---|---|
| How you get one | JAR or directory with `module-info.class` on the **module path** | JAR **without** `module-info` on the **module path** | Any code on the **class path** (a `module-info` there is ignored) |
| Name | from `module-info` | `Automatic-Module-Name` in `MANIFEST.MF`, else derived from the file name | none, so it cannot be `requires`-d |
| Exports | only what it `exports` | **all** packages (and opens all) | all packages, but only other class-path code and automatic modules can read them |
| Reads | only what it `requires` (+ `java.base`) | **every** module, including the unnamed module | every resolved module |
| Can a named module use it? | yes, with `requires` | yes, with `requires <derived name>` | **no**. Named modules cannot read the class path (`package cat is not visible`). |

**Automatic modules are the bridge.** A named module `requires` the automatic module, and the automatic module can in turn use class-path code. I verified a named module calling into automatic module `bridge`, which calls `cat.Cat` on the class path; it printed `cat unnamed module @...`.

**Class-path code using a module-path JAR:** an automatic module is resolved only if something requires it or it is added as a root. Without `--add-modules dog.hair.util`, `javac` reports `package dog.hair is not visible` and `java` throws `NoClassDefFoundError`.

### Automatic module name derivation

When the manifest has `Automatic-Module-Name: com.example.hair`, that value is used and the file name is ignored. Otherwise the name is derived from the file name:

1. Remove `.jar`.
2. Drop the version: everything from the first `-` that is followed by a digit (`-1.2.3-SNAPSHOT`).
3. Replace every non-alphanumeric character with `.`.
4. Collapse repeated dots and remove leading and trailing dots.

| File | Module name (verified with `jar --describe-module`) |
|---|---|
| `dog-hair_util-1.2.3-SNAPSHOT.jar` | `dog.hair.util@1.2.3-SNAPSHOT` |
| `commons_lang3-3.12.0.jar` | `commons.lang3@3.12.0` |
| `a..b--c.jar` | `a.b.c` |
| `my-lib2.jar` | `my.lib2` |
| `foo.bar-1.0.jar` | `foo.bar@1.0` |
| `1abc-x.jar` | error: *Unable to derive module descriptor* (`1abc` is not an identifier) |
| `lib-2-x.jar` | error (no version match, so the name would be `lib.2.x`, which is invalid) |

---

## 5. Migration strategies

| | **Bottom-up** | **Top-down** |
|---|---|---|
| Idea | Migrate the projects with **no dependencies** first, then work upward. | Move **all** JARs to the module path first, then migrate starting at the **top** (the application). |
| During migration | Migrated JARs are **named modules** on the module path. Unmigrated JARs stay on the **class path** (unnamed module). | Unmigrated JARs are **automatic modules** on the module path. |
| Requirement | Each lower-level library must be modularized before the code that depends on it. | Works when you do not control (or cannot yet change) the libraries. |

To migrate a single JAR, generate a starting descriptor with `jdeps --generate-module-info <dir> lib.jar` (see section 7). Remember that cycles are illegal: break a cycle by **extracting the shared code into a new module** that both modules require.

---

## 6. Compiling, packaging and running

```bash
# compile several modules at once
javac -d out --module-source-path src -m zoo.api,zoo.impl,zoo.app

# compile one module against already-built modules
javac -p mods -d out src/app/module-info.java src/app/app/M.java

# package as modular JARs
jar --create --file mods/zoo.api.jar -C out/zoo.api .
jar -cf mods/zoo.impl.jar -C out/zoo.impl .
jar --create --file mods/zoo.app.jar --main-class zoo.app.Main -C out/zoo.app .

# run
java -p mods -m zoo.app/zoo.app.Main           # module/fully.qualified.MainClass
java --module-path mods --module zoo.app       # OK because the JAR records a main class
```

### The overloaded short options

| Option | `javac` | `java` | `jar` | `jdeps` | `jlink` |
|---|---|---|---|---|---|
| `-p` | `--module-path` | `--module-path` | `--module-path` | **`--package`** (filter!) | `--module-path` |
| `-m` | `--module` (modules to compile) | `--module` (module to run) | **`--manifest`** | `--module` (root to analyse) | n/a |
| `-d` | **output directory** | **`--describe-module`** | **`--describe-module`** | n/a (`-dotoutput`) | n/a |
| `-cp` / `--class-path` | yes | yes | n/a | yes | n/a |
| `-e` | n/a | n/a | `--main-class` | `--regex` | n/a |

### `java` module options

| Option | Purpose |
|---|---|
| `-p` / `--module-path <path>` | Where to find modules |
| `-m` / `--module <module>[/<mainclass>]` | Module to run |
| `--add-modules m1,m2` | Extra root modules to resolve (also `ALL-MODULE-PATH`) |
| `--list-modules` | Lists observable modules (`java.base@25...`, plus your module path if given) and **exits** |
| `-d` / `--describe-module <module>` | Prints that module's descriptor (`requires ... mandated`, `exports`, `contains` for non-exported packages, `provides`, `uses`) and **exits** |
| `--show-module-resolution` | Prints `root ...`, `X requires Y`, `X binds Z` lines **and then runs** the program |
| `--add-exports`, `--add-opens`, `--add-reads` | Override encapsulation (see section 2) |
| `--limit-modules` | Restrict the set of observable modules |

### `jar` options

`-c/--create`, `-f/--file`, `-t/--list`, `-x/--extract`, `-u/--update`, `-e/--main-class`, `-m/--manifest` (include a manifest file), `-d/--describe-module`, `-C dir` (change directory before adding files), `-v`. Sample `jar -d -f mods/zoo.app.jar` output:

```text
zoo.app jar:file:///.../mods/zoo.app.jar!/module-info.class
requires java.base mandated
requires zoo.api
uses zoo.api.Animal
contains zoo.app
main-class zoo.app.Main
```

---

## 7. `jdeps`: dependency analysis

```bash
jdeps --module-path mods mods/zoo.impl.jar     # full package-level report
jdeps -s legacy/cookie.jar                     # summary
jdeps -summary legacy/cookie.jar               # same thing
jdeps --jdk-internals legacy/cookie.jar        # use of internal JDK APIs
jdeps -jdkinternals legacy/cookie.jar          # old single-dash form, still accepted
jdeps --module-path mods -m zoo.app            # analyse a module by name
jdeps --list-deps legacy/cookie.jar
jdeps --generate-module-info gen legacy/cookie.jar
```

Sample output (verified):

```text
$ jdeps -s legacy/cookie.jar
cookie.jar -> java.base
cookie.jar -> java.logging

$ jdeps --jdk-internals legacy/cookie.jar
cookie.jar -> java.base
   cookie.Cookie  -> sun.security.util.HexDumpEncoder   JDK internal API (java.base)
Warning: JDK internal APIs are unsupported and private to JDK implementation ...
```

Points to remember:

- `-s` and `-summary` are valid. **`--summary` is not** (`Error: unknown option: --summary`).
- `--jdk-internals` and `-jdkinternals` are valid. `--jdk-unsupported`, `-jdk-unsupported` and `-jdkunsupported` do **not** exist.
- `--module-path` has no `-p` short form in jdeps, because `-p` means `--package`.
- `--list-deps` prints `java.base/sun.security.util` for internal packages. `--generate-module-info` writes a suggested `module-info.java` (`requires java.logging; exports cookie;`).
- Code that uses internal JDK packages compiles only with `--add-exports java.base/sun.security.util=ALL-UNNAMED`. Without that flag at run time it throws `IllegalAccessError ... does not export package sun/security/util to unnamed module`.

---

## 8. `jlink` and `jmod`

`jlink` builds a **custom runtime image** that contains only the modules you need (plus `java.base`).

```bash
jlink --module-path mods --add-modules zoo.app,zoo.impl --output zooimage --launcher zoo=zoo.app
zooimage/bin/java --list-modules     # java.base, zoo.api, zoo.app, zoo.impl
zooimage/bin/zoo                     # generated launcher script
```

- Required options: `--add-modules` (root modules) and `--output` (the directory, which must not already exist). `-p`/`--module-path` is needed for your own modules. The JDK's `jmods` directory is found automatically.
- The image directory contains `bin/`, `conf/`, `lib/`, `legal/`, `include/` and a `release` file.
- **Services are not bound by default.** `--add-modules zoo.app` alone produced an image **without** `zoo.impl`. Running it printed no animals. Add the provider explicitly or use `--bind-services`.
- Other options: `--strip-debug` (`-G`), `--compress`, `--no-header-files`, `--no-man-pages`, `--launcher name=module[/mainclass]`, `--limit-modules`, `--suggest-providers`.

`jmod` creates **JMOD files**, a format that can hold native code and configuration files, for use at **compile time and link time only, not at run time**:

```bash
jmod create --class-path out/zoo.api jm/zoo.api.jmod
jmod list jm/zoo.api.jmod        # classes/module-info.class, classes/zoo/api/Animal.class ...
jmod describe jm/zoo.api.jmod
```

`jmod` modes: `create`, `extract`, `list`, `describe`, `hash`. A `.jmod` can be fed to `jlink -p jm:mods ...`, but `java -p jm` does not load it.

---

## 9. Single-file and 🆕 multi-file source launcher

`java Prog.java args...` compiles in memory and runs; no `.class` file is written.

- **Single file (Java 11):** the file may declare several top-level classes. **The first top-level class is launched**, so put the class with `main` first. `class A {main} class B {main}` runs `A`.
- The file name need not match a class name. A `package` statement is allowed only when the path ends with the matching directories (`error: end of path to source file does not match its package name foo`).
- A file without the `.java` extension needs `--source N`.

**🆕 Java 22 (JEP 458) multi-file source programs:** the launched file can use classes in **other `.java` files** in the same directory tree. Package `pkg` maps to the directory `pkg/`.

```text
multi/Prog.java       public class Prog { main -> Util.x() + pkg.Lib.y() }
multi/Util.java
multi/pkg/Lib.java
$ java multi/Prog.java      ->  util lib
```

Other files are compiled **on demand**. An unused file with a compile error (`Broken.java`) is ignored, but once a used class references it, the launch fails with that compile error. Libraries can be added with `--class-path`.

---

## 10. 🆕 Compact source files and instance `main` methods (Java 25, JEP 512)

A source file whose top-level code is just **fields and methods** (no enclosing class) is a **compact source file**. It **implicitly declares a class**:

```java
// Hello.java
String greeting = "Hi";
void main() {
    IO.println(greeting + " " + helper());
    List<String> l = List.of("a", "b");        // java.base is auto-imported
    IO.println(l + " " + getClass().getName()); // [a, b] Hello
}
String helper() { return "there"; }
```

`java Hello.java` prints `Hi there` and then `[a, b] Hello`. `javac Hello.java` produces `Hello.class`. `javap -p` shows `final class Hello { java.lang.String greeting; Hello(); void main(); java.lang.String helper(); }`.

Rules for the implicitly declared class:

- It is **`final`**, extends `Object`, lives in the **unnamed package**, and has a default constructor. Its name comes from the file name, but **your code cannot refer to it by name** (`Hello h;` gives `cannot find symbol`). Use `this` or `getClass()`.
- It **must have a launchable `main`** (`compact source file does not have main method in the form of void main() or void main(String[] args)`).
- **No `package` declaration** is allowed (`compact source file should not have package declaration`).
- Classes, records and interfaces declared in the file become **member classes** of the implicit class (`Point.class.getName()` gives `Nested$Point`).
- It **automatically imports every public top-level type from every package exported by `java.base`**, as if `import module java.base;` appeared at the top. `List`, `Map`, `Path` and `Files` need no imports. Ordinary classes do **not** get this; `List` in a normal class still needs an import.

### `java.lang.IO` (🆕 Java 25)

`IO` is in `java.lang`, so it works in **every** class, not only compact ones. It has static methods `println(Object)`, `println()`, `print(Object)`, `readln()` and `readln(String prompt)`. `readln` returns `null` at end of input.

### Instance `main` and the launch protocol (for every class, not just compact ones)

`main` no longer has to be `public`, `static`, or take `String[]`. When launching class `C`, the JVM:

1. Looks for a **non-private** `main(String[])` (or `String...`), static or instance, declared or **inherited**.
2. If none exists, looks for a **non-private** `main()` with no parameters.
3. If the chosen method is `static`, calls it. If it is an instance method, instantiates `C` with its **non-private no-arg constructor** and then calls it.

| Class has | Runs (verified) |
|---|---|
| `static void main()` + instance `void main(String[])` | `main(String[])`, because the parameter version wins regardless of `static` |
| instance `void main()` + `static void main(String...)` | `main(String...)` |
| `private static void main(String[])` + instance `void main()` | `main()`, because private methods are not candidates |
| only instance `main()`, only constructor is `private Prio4(int)` | `error: can't find no argument constructor` |
| `protected void main()` | runs |
| `int main()` | `error: can't find main(String[]) or main() method` |
| `abstract class` with instance `main()` | `abstract class Abs can not be instantiated` |
| `class Kid extends Base` where `Base` has `void main()` | runs the inherited method |

---

## 11. 🆕 Module import declarations (Java 25, JEP 511)

```java
import module java.sql;
```

This imports, **on demand**, every public **top-level** type in

1. every package that the named module exports to the current module, and
2. every package exported by modules the current module reads **because of** reading that module, which means its `requires transitive` closure.

So `import module java.sql;` also gives `Logger` (java.logging) and `XMLConstants` (java.xml), because `java.sql` requires them transitively (verified).

Rules:

- It may be used in **any** source file, modular or not. Repeating the same module import is redundant, not an error.
- The current module must **read** the imported module. In a named module without `requires java.sql`, you get `module app does not read: java.sql`. On the class path, `import module java.se;` fails (`unnamed module does not read: java.se`) because `java.se` exports nothing itself and is not a default root module. It works with `--add-modules java.se`. `import module java.desktop;` works without flags.
- It imports **types only**. Static members are not imported (`max(1,2)` does not compile). Nested types are reachable only through their outer type: `Map.Entry` works, a bare `Entry` does not.
- **Ambiguity:** `import module java.base; import module java.desktop;` and then `List` gives `reference to List is ambiguous` (`java.util.List` versus `java.awt.List`).
- **Resolving it:** shadowing ranks the import forms as follows. A *single-type import* (`import java.util.List;`) shadows types imported by a type-import-on-demand, which in turn shadows module imports. A module import never shadows anything. So:
  - `import module java.base; import module java.desktop; import java.util.List;` makes `List` mean `java.util.List` (verified).
  - `import module java.base; import java.awt.*;` makes `List` mean `java.awt.List`. The on-demand package import wins over the module import (verified).
  - `java.lang.*` is implicitly on-demand imported, so it always shadows a same-named type from a module import.

---

## 🆕 Since Java 17

| Feature | Version | Exam point |
|---|---|---|
| Multi-file source-code programs | **Java 22**, JEP 458 | `java Main.java` compiles other source files in the tree on demand |
| Compact source files and instance `main` methods | **Java 25**, JEP 512 | Implicit final class, auto `import module java.base`, `java.lang.IO`, new launch protocol |
| Module import declarations | **Java 25**, JEP 511 | `import module M;`, transitive closure, ambiguity and shadowing |

Not on the exam: anything that is still preview in Java 25.

---

## Exam traps

- `opens` gives **no compile-time access**. `exports` gives **no deep reflection** (`setAccessible(true)` on a private member throws `InaccessibleObjectException`).
- `open module` plus an `opens` directive is a compile error. `open module` plus `exports` is fine.
- `requires transitive` is needed when **your exported API exposes types** from the other module. The consumer can then use those types without its own `requires`.
- `requires static`: the module is absent at run time unless something else resolves it.
- The consumer `requires` the **API** module, not the provider. The **locator module needs `uses`**, otherwise you get a runtime `ServiceConfigurationError` even though the code compiled.
- The provider package does not need to be exported. The provider class needs a public no-arg constructor or a `public static provider()` method.
- `ServiceLoader.stream()` gives `Provider<S>`, not `S`. Call `.get()`.
- Unnamed module code cannot be `requires`-d. Named modules **cannot read the class path**. Automatic modules **can**.
- An automatic module exports **all** its packages and reads all modules.
- Bottom-up puts unmigrated JARs on the **class path**. Top-down puts **everything** on the module path as automatic modules.
- `java -d` means describe-module, `javac -d` means output directory, `jar -d` means describe-module. `jar -m` means manifest, not module. `jdeps -p` means package.
- `jdeps --summary`, `--jdk-unsupported` and `--add-requires` are not real options. `-s`, `-summary`, `--jdk-internals` and `-jdkinternals` are real.
- `java --show-module-resolution` **still runs** the program. `--list-modules` and `--describe-module` do not run it.
- `jlink` does not include service providers unless you add them or use `--bind-services`.
- JMOD files are not runnable on the module path.
- Cyclic `requires` never compiles. Split packages never work.
- Single-file launcher: the **first** top-level class is launched.
- Compact source file: no `package`, a `main` is required, the class is `final` and cannot be named, and `java.base` is auto-imported. **Normal classes do not** get the auto-import, but they do get `IO`, because `IO` is in `java.lang`.
- Launch protocol: `main(String[])` beats `main()` even when the `main()` is static. Private `main` methods are ignored. An instance `main` needs a non-private no-arg constructor.
- `import module` with two modules exporting the same simple name is ambiguous **only when you use that name**. Fix it with a single-type import (or an on-demand package import).

---

## Self-check before practice

- [ ] I can state what each directive does at compile time and at run time: `requires`, `requires transitive`, `requires static`, `exports`, `exports ... to`, `opens`, `opens ... to`, `open module`, `uses`, `provides ... with`.
- [ ] I know which exception `setAccessible(true)` throws on an exported-but-not-opened package.
- [ ] I can name the four service parts and say which module needs `uses` and which needs `provides`.
- [ ] I know the provider requirements (public, non-abstract, public no-arg constructor or `provider()` method).
- [ ] I can use `ServiceLoader.load`, iterate it, `stream()` plus `Provider::get`, and `findFirst()`.
- [ ] I can fill in the named / automatic / unnamed table (name, exports, reads, who can read it).
- [ ] I can derive automatic module names from JAR file names.
- [ ] I can explain bottom-up versus top-down migration and where unmigrated JARs go.
- [ ] I know `-p`, `-m` and `-d` in each tool, and the real `jdeps` options (`-s`/`-summary`, `--jdk-internals`/`-jdkinternals`).
- [ ] I know what `--list-modules`, `--describe-module`, `--show-module-resolution` and `--add-modules` do.
- [ ] I can write a `jlink` command (`--module-path`, `--add-modules`, `--output`) and know that services are not bound by default.
- [ ] I know the single-file and multi-file launcher rules.
- [ ] I know the compact source file rules and the `main` launch priority.
- [ ] I can predict which `List` an `import module` combination resolves to.

**Practice:** `practice/ocp17-829` chapter 7 (*Packaging and Deploying Java Code and Use the Java Platform Module System*) for the module system and CLI tools, then the `java25-gap` set for compact source files, instance `main`, multi-file launch and `import module`.
