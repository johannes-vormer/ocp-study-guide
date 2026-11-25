---
title: "Chapter 14: Practice Exam 3"
source: ocp17-829
topic: exam
---

# Chapter 14: Practice Exam 3

> Source: *OCP Oracle Certified Professional Java SE 17 Developer Practice Tests (Exam 1Z0-829)*. Extracted from your PDF for personal study. Questions with figures/tables include a scan of the original page.

## 829-14-001

Which of the following are true about Java operators and statements? (Choose three.)

- **A.** Both right-hand sides of the ternary expression are evaluated at runtime.
- **B.** A `switch` statement may contain at most one `default` statement.
- **C.** The post-increment operator (++) returns the value of the variable before the addition is applied.
- **D.** The logical operators (`|`) and (`||`) are interchangeable, producing the same results at runtime.
- **E.** The complement operator (`!`) operator may be applied to numeric expressions.
- **F.** An assignment operator returns a value that is equal to the value of the expression being assigned.

**Answer:** B, C, F

**Explanation:** The ternary operator (`? :`) evaluates only one of the two right-hand expressions at runtime, so option A is incorrect. A `switch` statement may contain at most one optional `default` statement, making option B correct. The post-increment operator increments the variable and returns the original value, making option C correct. The logical operator (`|`) operator will evaluate both operands, while the disjunctive short-circuit (`||`) operator will only evaluate the right-hand side of the expression if the left-hand side evaluates to `false`. Therefore, they may produce different results if the left operand is true, and option D is incorrect. Option E is incorrect as the complement operator (`!`) is applied to `boolean` values. Finally, option F is correct and allows the assignment operator to be used in a conditional expression, such as part of a loop condition.

## 829-14-002

What is the output of the following?

```java
public class Legos {
   public static void main(String[] args) {
     var ok = true;
     if (ok) {
        var sb = new StringBuilder();
        sb.append("red");
        sb.deleteCharAt(0);
        sb.delete(1, 1);
     }
     System.out.print(sb);
   } }
```

- **A.** `r`
- **B.** `e`
- **C.** `ed`
- **D.** `red`
- **E.** The code does not compile.
- **F.** The code compiles but throws an exception at runtime.

**Answer:** E

**Explanation:** This code does not compile because `print()` attempts to reference the `sb` variable. However, that variable is only in scope for the `if` statement. Since the code does not compile, option E is correct. If `println()` were inside the `if` statement, option C would be correct.

## 829-14-003

Which line of code belongs in a service locator?

- **A.** `ServiceLoader<Mouse> sl = ServiceLoader.load(Mouse.class);`
- **B.** `ServiceLoader<Mouse> sl = ServiceLoader.loader(Mouse.class);`
- **C.** `ServiceLoader<Mouse> sl = ServiceLoader.lookup(Mouse.class);`
- **D.** `ServiceLocator<Mouse> sl = ServiceLoader.load(Mouse.class);`
- **E.** `ServiceLocator<Mouse> sl = ServiceLoader.loader(Mouse.class);`
- **F.** `ServiceLocator<Mouse> sl = ServiceLoader.lookup(Mouse.class);`

**Answer:** A

**Explanation:** The service locator contains a `ServiceLoader` call to the `load()` method to look up, which is option A.

## 829-14-004

What is the result of the following?

```java
1: import java.util.function.*;
2: public class Ready {
3:     private static double getNumber() {
4:       return .007;
5:     }
6:     public static void main(String[] args) {
7:       Supplier<double> s = Ready::getNumber;
8:       double d = s.get();
9:       System.out.println(d);
10:    } }
```

- **A.** `0`
- **B.** `0.007`
- **C.** The code does not compile due to line 7.
- **D.** The code does not compile due to line 8.
- **E.** The code does not compile for another reason.

**Answer:** C

**Explanation:** You can’t use generics with a primitive, so it should be `Supplier<Double>`. This makes option C the answer.

## 829-14-005

What is the output of executing the following code snippet?

```java
var e = Executors.newSingleThreadExecutor();
Runnable r1 = () -> Stream.of(1,2,3).parallel();
Callable r2 = () -> Stream.of(4,5,6).parallel();
Future<Stream<Integer>> f1 = e.submit(r1);     // x1
Future<Stream<Integer>> f2 = e.submit(r2);     // x2
var r = Stream.of(f1.get(),f2.get())
   .flatMap(p -> p)                      // x3
   .parallelStream()                      // x4
   .collect(
     Collectors.groupingByConcurrent(i -> i%2==0));
System.out.print(r.get(false).size()
   +" "+r.get(true).size());
```

- **A.** `3 3`
- **B.** `2 4`
- **C.** One of the marked lines (`x1`, `x2`, `x3`, `x4`) does not compile.
- **D.** Two of the marked lines (`x1`, `x2`, `x3`, `x4`) do not compile.
- **E.** Three of the marked lines (`x1`, `x2`, `x3`, `x4`) do not compile.
- **F.** None of the above.

**Answer:** D

**Explanation:** Line `x1` does not compile because of an assignment and value mismatch. The `r1` variable is a `Runnable` expression. While there is an `ExecutorService.submit()` that takes a `Runnable` expression, it returns `Future<?>`, since the return type is `void`. This type is incompatible with the `Future<Stream>` assignment without an explicit cast, leading to a compiler error. Next, line `x4` does not compile. The `parallelStream()` method is found in the `Collection` interface, not the `Stream` interface. Due to these two compilation errors, option D is the correct answer.

## 829-14-006

Which can fill in the blank so this code outputs `true`?

```java
import java.util.function.*;
import java.util.stream.*;
public class HideAndSeek {
   public static void main(String[] args) {
     var hide = Stream.of(true, false, true);
     Predicate<Boolean> pred = b -> b;
     var found = hide.filter(pred). ______________ (pred);
     System.out.println(found);
   } }
```

- **A.** Only `anyMatch`
- **B.** Only `allMatch`
- **C.** Both `anyMatch` and `allMatch`
- **D.** Only `noneMatch`
- **E.** Both `noneMatch` and `anyMatch`
- **F.** The code does not compile with any of these options.

**Answer:** C

**Explanation:** The `filter()` method passes two of the three elements of the stream through to the terminal operation. This is redundant, since the terminal operation checks the same `Predicate`. There are two matches with the same value, so both `anyMatch()` and `allMatch()` return `true`, and option C is correct.

## 829-14-007

Suppose you have a consumer that calls the `lion()` method within a `Lion` service. You have four distinct modules: consumer, service locator, service provider, and service provider interface. If you add a parameter to the `lion()` method, how many of the modules require recompilation?

- **A.** Zero
- **B.** One
- **C.** Two
- **D.** Three
- **E.** Four

**Answer:** D

**Explanation:** The service provider interface clearly needs to be recompiled, since that’s where the change occurs. The service provider also needs to be recompiled because it implements the interface. Finally, the consumer needs to be recompiled because it calls the interface. The service locator does not need to be recompiled, as it only knows the service provider interface name rather than its method signature. Since three require recompilation, option D is correct.

## 829-14-008

What is the output of the following application?

```java
package music;
interface DoubleBass {
   void strum();
   default int getVolume() {return 5;}
}
interface BassGuitar {
   void strum();
   default int getVolume() {return 10;}
}
abstract class ElectricBass implements DoubleBass, BassGuitar {
   @Override public void strum() {System.out.print("X");}
}
 public class RockBand {
    public static void main(String[] strings) {
       final class MyElectricBass extends ElectricBass {
         public int getVolume() {return 30;}
         public void strum() {System.out.print("Y");}
       } } }
```

- **A.** `X`
- **B.** `Y`
- **C.** The application completes without printing anything.
- **D.** `ElectricBass` is the first class to not compile.
- **E.** `RockBand` is the first class to not compile.
- **F.** None of the above.

**Answer:** D

**Explanation:** The application does not compile, so options A, B, and C are incorrect. The `ElectricBass` class does not compile, since it inherits two `default` methods with the same signature. Even though the class is marked `abstract`, it still must override this `default` method. Since `ElectricBass` fails to do so, option D is correct.

## 829-14-009

Which condition is most likely to result in invalid data entering the system?

- **A.** Deadlock
- **B.** Livelock
- **C.** Resource not found
- **D.** Out of memory error
- **E.** Race condition
- **F.** Starvation

**Answer:** E

**Explanation:** A race condition occurs when two threads attempt to execute the same process. If not properly handled, it can lead to an undesirable result, such as two users both being able to create an account with the same login. For this reason, option E is correct.

## 829-14-010

Which statements are correct? (Choose two.)

- **A.** A `Comparable` implementation is often implemented by a lambda.
- **B.** A `Comparable` object has a `compare()` method.
- **C.** The `compare()` and `compareTo()` methods have the same contract for the return value.
- **D.** It is possible to sort the same `List` using different `Comparator` implementations.
- **E.** Two objects that return `true` for `equals()` will always return `0` when passed to `compareTo()`.

**Answer:** C, D

**Explanation:** Option A is incorrect because `Comparable` is implemented in the class being compared. To be useful, such a class must have instance variables to compare, ruling out a lambda. By contrast, a `Comparator` is often implemented with a lambda. Option B is incorrect because `compareTo()` is the method in `Comparable`. Option C is correct because these methods have different parameters but the same return type, with the same rules for ordering elements. Option D is correct because a `Comparator` doesn’t need to be implemented by the class being compared. It can be passed to the `sort()` method as a parameter. Option E is incorrect because comparators are not required to be consistent with the `equals()` method. For example, two objects that are equivalent in terms of `equals()` may be sorted differently.

## 829-14-011

Which lines fail to compile?

```java
 package armory;
 import java.util.function.*;
 interface Shield {
    void protect();
 }
 class Dragon {
    int addDragon(Integer count) {
      return ++count;
    } }
public class Sword {
   public static void main(String[] knight) {
     var dragon = new Dragon();
     Function<Shield, Sword> func = Shield::protect; // line x
     UnaryOperator<Integer> op = dragon::addDragon;     // line y
   } }
```

- **A.** Only line `x`
- **B.** Only line `y`
- **C.** Both lines `x` and `y`
- **D.** The code compiles.

**Answer:** A

**Explanation:** The method reference on line `x` is supposed to define a `Function`. The `Shield` interface does define a single abstract method. However, that method has a `void` return type, which is not compatible with `Function`. Line `y` does compile since `addDragon()` has both a parameter and return type. Option A is the answer, since only line `x` fails to compile.

## 829-14-012

What option names are equivalent to `-p` and `-cp` on the `javac` command? (Choose two.)

- **A.** `--module-path` and `-classpath`
- **B.** `--module-path` and `-class-path`
- **C.** `--module-path` and `--class-path`
- **D.** `--path` and `-classpath`
- **E.** `--path` and `-class-path`
- **F.** `--path` and `--class-path`

**Answer:** A, C

**Explanation:** The `javac` command uses `-p` and `--module-path` to supply the module path. There are two valid long forms of the classpath option: `-classpath` and `--class-path`. Options A and C match these.

## 829-14-013

What is the minimum number of lines that need to be changed or removed to make this method compile?

```java
11: public void colors() {
12:    var yellow = "";
13:    yellow = null;
14:
15:    var red = null;
16:
17:    var blue = "";
18:    blue = 1;
19:
20:    var var = "";
21:    var = "";
22:
23:    var pink = 1;
24: }
```

- **A.** Zero
- **B.** One
- **C.** Two
- **D.** Three
- **E.** Four
- **F.** Five

**Answer:** C

**Explanation:** Line 15 does not compile because the type of a variable cannot be inferred when the value is `null`. Line 18 does not compile because the type cannot be changed once the variable is declared. In this case, an `int` cannot be assigned to a `String` variable. Since the change or removal of these two lines allows the code to compile, option C is correct.

## 829-14-014

What is the output of the following application?

```java
package ballroom;
public class Dance {
   public static void swing(int... beats)
        throws ClassCastException {
     try {
        System.out.print("1"+beats[2]);     // p1
     } catch (RuntimeException e) {
        System.out.print("2");
     } catch (Exception e) {
        System.out.print("3");
     } finally {
        System.out.print("4");
     } }
   public static void main(String[] music) {
     new Dance().swing(0,0);            // p2
     System.out.print("5");
   } }
```

- **A.** `145`
- **B.** `1045`
- **C.** `24`, followed by a stack trace
- **D.** `245`
- **E.** The code does not compile because of line `p1`.
- **F.** The code does not compile because of line `p2`.

**Answer:** D

**Explanation:** The code compiles without issue, so options E and F are incorrect. Note that line `p2` accesses a `static` method using an instance reference, which is discouraged but permitted in Java. First, a varargs `int` array of `[0,0]` is passed to the `swing()` method. The `try` block throws `ArrayIndexOutOfBoundsException`, since the third element is requested, and the size of the array is two. For this reason, the `print()` statement in the `try` block is not executed. Next, since `ArrayIndexOutOfBoundsException` is a subclass of `RuntimeException`, the `RuntimeException catch` block is executed and `2` is printed. The rest of the `catch` blocks are skipped, since the first one was selected. The `finally` block then executes and prints `4`. Finally, control is returned to the `main()` method without an exception being thrown, and `5` is printed. Since `245` is printed, option D is the correct answer.

## 829-14-015

What is the output of the following application?

```java
1: interface HasHue {String getHue();}
2: enum COLORS implements HasHue {
3:     red {
4:       public String getHue() {return "FF0000";}
5:     }, green {
6:       public String getHue() {return "00FF00";}
7:     }, blue {
8:       public String getHue() {return "0000FF";}
9:     }
10:    private COLORS() {}
11: }
12: class Book {
13:    static void main(String[] pencils) {}
14: }
15: final public class ColoringBook extends Book {
16:    final void paint(COLORS c) {
17:      System.out.print("Painting: " + c.getHue());
18:    }
19:    final public static void main(String[] crayons) {
20:      new ColoringBook().paint(green);
21:    } }
```

- **A.** `Painting: 00FF00`
- **B.** Exactly one line of code does not compile.
- **C.** Exactly two lines of code do not compile.
- **D.** Three or more lines of code do not compile.
- **E.** The code compiles but prints an exception at runtime.
- **F.** None of the above.

**Answer:** C

**Explanation:** Line 10 does not compile because line 9 is missing a semicolon (`;`) at the end of it. A semicolon is required after a list of enum values if the enum contains anything besides the list of values. Line 20 does not compile because the enum name is missing before the enum value `green`. For these two reasons, option C is correct.

## 829-14-016

Which of the following methods can run without error for a SQL query that returns a count of matching rows?

```java
private static void choices(PreparedStatement ps,
     String sql) throws SQLException {
   try (var rs = ps.executeQuery()) {
     System.out.println(rs.getInt(1));
   } }
private static void moreChoices(PreparedStatement ps,
     String sql) throws SQLException {
   try (var rs = ps.executeQuery()) {
     rs.next();
     System.out.println(rs.getInt(1));
   } }
private static void stillMoreChoices(PreparedStatement ps,
     String sql) throws SQLException {
   try (var rs = ps.executeQuery()) {
     if (rs.next())
        System.out.println(rs.getInt(1));
     }
   } }
```

- **A.** `moreChoices()`
- **B.** `stillMoreChoices()`
- **C.** `choices()` and `stillMoreChoices()`
- **D.** `moreChoices()` and `stillMoreChoices()`
- **E.** All three methods
- **F.** None of the above

**Answer:** D

**Explanation:** The `stillMoreChoices()` method is a common approach to retrieve zero or one records, which works for any `SELECT` statement that has an `int` in the first column. If the `SELECT` statement has a function like `count(*)` or `sum(*)` in the first column, there will always be a row in the `ResultSet`, so `moreChoices()` works as well. The method `choices()` produces an exception at runtime regardless of the query, because `rs.next()` is not called before any values are read. Therefore, option D is the answer.

## 829-14-017

In most of the United States, daylight saving time ends on November 6 at 02:00 and we repeat that hour. What is the output of the following?

```java
var localDate = LocalDate.of(2022, Month.NOVEMBER, 6);
var localTime = LocalTime.of(1, 0);
var zone = ZoneId.of("America/New_York");
var z = ZonedDateTime.of(localDate, localTime, zone);
var offset = z.getOffset();
for (int i = 0; i < 6; i++)
   z = z.plusHours(1);
System.out.print(z.getHour() + " "
   + offset.equals(z.getOffset()));
```

- **A.** `5 false`
- **B.** `5 true`
- **C.** `6 false`
- **D.** `6 true`
- **E.** `7 false`
- **F.** `7 true`
- **G.** The code does not compile.
- **H.** The code compiles but throws an exception at runtime.

**Answer:** C

**Explanation:** This code runs the loop six times, adding an hour to `z` each time. However, the first time is the repeated hour from daylight saving time. The time zone offset changes, but not the hour. This means the hour only increments five times. Adding that to 01:00 gives us 06:00. Since the time zone changes as well, option C is correct.

## 829-14-018

What is the output of the `Light` program?

```java
  package physics;
  class Wave {
    public int size = 7;
  }
  public class Light extends Wave {
    public int size = 5;
    public static void main(String... emc2) {
       Light v1 = new Light();
       var v2 = new Light();
   Wave v3 = new Light();
   System.out.println(v1.size+","+v2.size+","+v3.size);
} }
```

- **A.** `5,5,5`
- **B.** `5,5,7`
- **C.** `5,7,7`
- **D.** `7,7,7`
- **E.** The code does not compile.
- **F.** None of the above.

**Answer:** B

**Explanation:** When using a variable in a subclass, Java uses the reference type to determine the variable to use. Option A would be correct if the `size` variables were treated like method overriding, since all of the objects in the `main()` method are instances of `Light`. Instead, the reference type is used. The variables `v1` and `v2` are of reference type `Light`, so `5` is selected. Likewise, the variable `v3` is of type `Wave`, so `7` is used. The output is `5,5,7`, making option B correct.

## 829-14-019

Suppose we have a stored procedure named `update_data` that has one `IN` parameter named `data`. Which fills in the blank so the code runs without error?

```java
String sql = "{call update_data(?)}";
try (Connection conn = DriverManager.getConnection(url);
   Statement cs = conn.prepareCall(sql)) {
   cs. _______ (1, 6);
   var rs = cs.execute();
}
```

- **A.** `registerInParameter`
- **B.** `registerIntParameter`
- **C.** `registerInputParameter`
- **D.** `setIn`
- **E.** `setInt`
- **F.** `setInteger`
- **G.** None of the above

**Answer:** G

**Explanation:** Options A, B, C, D, and F are decoys. However, this question is tricky because `prepareCall()` returns a `CallableStatement`. While it can be stored in a `Statement`, the `setInt()` method is no longer available, making option G the answer.

## 829-14-020

What are possible outputs of this code? (Choose two.)

```java
import java.time.LocalDate;
public class Animal {
   private record Baby(String name, LocalDate birth) { }
   public static void main(String[] args) {
     LocalDate now = LocalDate.now();
     var b1 = new Baby("Teddy", now);
     var b2 = new Baby("Teddy", now);
     System.out.println((b1 == b2) + " " + b1.equals(b2));
     System.out.println(b1);
   } }
```

- **A.** `false false`
- **B.** `false true`
- **C.** `true false`
- **D.** `true true`
- **E.** `Baby[name=Teddy, birth=2022-05-21]`
- **F.** `Baby@1d81eb93`

**Answer:** B, E

**Explanation:** The `==` operator checks if two references point to the same object, which is not the case here. By contrast, records provide a default `equals()` method to compare the values of all fields. The former is `false` here, and the latter is `true`, making option B one answer. Also, a record provides a useful `toString()` implementation, making option E the other answer.

## 829-14-021

What is the output of calling the following method?

```java
public void countPets() {
   record Pet(int age) {}
   record PetSummary(long count, int sum) {}
   var summary = Stream.of(new Pet(2), new Pet(5), new Pet(8))
     .collect(Collectors.teeing(
        Collectors.counting(),
        Collectors.summingInt(Pet::age)));
   System.out.println(summary);
}
```

- **A.** `PetSummary[count=1, sum=3]`
- **B.** `PetSummary[count=1, sum=15]`
- **C.** `PetSummary[count=3, sum=3]`
- **D.** `PetSummary[count=3, sum=15]`
- **E.** The code does not compile due to the `teeing()` call.
- **F.** The code does not compile since records are defined inside a method.
- **G.** The code does not compile for another reason.

**Answer:** E

**Explanation:** A `teeing()` collector takes three parameters. Since one is missing, the code does not compile because of the `teeing()` call and option E is the answer.

## 829-14-022

Given the following, what can we infer about subtypes of `First`, called `Chicken` and `Egg`? (Choose two.)

```java
public sealed class First { }
```

- **A.** `Chicken` and `Egg` must be classes.
- **B.** `Chicken` and `Egg` can be classes, interfaces, enums, or records.
- **C.** `Chicken` and `Egg` must be located in a file other than `First`.
- **D.** `Chicken` and `Egg` must be located in the same file as `First`.
- **E.** `Chicken` and `Egg` must be located as nested classes of `First`.
- **F.** `Chicken` and `Egg` may be located in the same file or a different file as `First`.

**Answer:** A, D

**Explanation:** `Chicken` and `Egg` must be subclasses of `First` since interfaces, enums, and records can’t extend a class. This gives us option A as one of the answers. Since the `permits` clause is not specified, `Chicken` and `Egg` may or may not be nested classes, but they must be in the same file as `First`. Therefore, option D is the other answer.

## 829-14-023

Which of the following statements about `try`/`catch` blocks are correct? (Choose two.)

- **A.** A `catch` block can never appear after a `finally` block.
- **B.** A `try` block must be followed by a `catch` block.
- **C.** A `finally` block can never appear after a `catch` block.
- **D.** A `try` block must be followed by a `finally` block.
- **E.** A `try` block can have zero or more `catch` blocks.
- **F.** A `try` block can have more than one `finally` block.

**Answer:** A, E

**Explanation:** A `try` block can have zero or more `catch` blocks and zero or one `finally` blocks, but must be accompanied by at least one of these blocks. For these reasons, options B, D, and F are incorrect, and option E is correct. A `finally` block must appear after the last `catch` block, if there are any, making option C incorrect and option A correct.

## 829-14-024

What is the result of compiling and executing the following application?

```java
package reptile;
public class Alligator {
   static int teeth;
   double scaleToughness;
   public Alligator() {
     this.teeth++;
   }
   public void snap(int teeth) {
     System.out.print(teeth+" ");
     teeth--;
   }
   public static void main(String[] unused) {
     new Alligator().snap(teeth);
     new Alligator().snap(teeth);
   } }
```

- **A.** `0 1`
- **B.** `1 1`
- **C.** `1 2`
- **D.** `2 2`
- **E.** The code does not compile.
- **F.** The code compiles but produces an exception at runtime.
- **G.** The answer cannot be determined ahead of time.

**Answer:** C

**Explanation:** The code compiles and runs without exception, making options E and F incorrect. The question is testing your knowledge of variable scope. The `teeth` variable is `static` in the `Alligator` class, meaning the same value is accessible from all instances of the class, including the `static main()` method. The `static` variable `teeth` is incremented each time the constructor is called. Note that the constructor uses `this` to access a `static` variable, which is bad practice but allowed. Since `teeth` is a local variable within the `snap()` method, the argument value is used, but changes to the local variable do not affect the `static` variable `teeth`. The local variable `teeth` is not used after it is decremented, so the decrement operation has no meaningful effect on the program flflow or the `static` variable `teeth`. Since the constructor is called twice, with `snap()` executed after each constructor call, the output printed is `1 2`, making option C the correct answer.

## 829-14-025

Fill in the blank to make this code compile:

```java
String cheese = ServiceLoader.stream(Mouse.class)
   .map( ______________ )
   .map(Mouse::favoriteFood)
   .findFirst()
   .orElse("");
```

- **A.** `Mouse.get()`
- **B.** `Mouse::get`
- **C.** `Provider.get()`
- **D.** `Provider::get`
- **E.** None of the above

**Answer:** E

**Explanation:** The `ServiceLoader.load()` method needs to be called to get an instance before calling `stream()`. Since this call is missing, option E is correct.

## 829-14-026

Which lines can fill in the blank that would allow the code to compile? (Choose two.)

```java
abstract public class Exam {
   boolean pass;
   protected abstract boolean passed();
   class JavaProgrammerCert extends Exam {
     private Exam part1;
     private Exam part2;
   }
}
```

- **A.** 

  ```java
  boolean passed() {
     return part1.pass && part2.pass;
  }
  ```
- **B.** 

  ```java
  boolean passed() {
     return part1.passed() && part2.passed();
  }
  ```
- **C.** 

  ```java
  private boolean passed() {
     return super.passed();
  }
  ```
- **D.** 

  ```java
  public boolean passed() {
     return part1.passed() && part2.passed();
  }
  ```
- **E.** 

  ```java
  public boolean passed() {
     return part1.pass && part2.pass;
  }
  ```
- **F.** 

  ```java
  public boolean passed() {
     return super.passed();
  }
  ```

**Answer:** D, E

**Explanation:** Each option presents a potential override of the `passed()` method, since the method signature is the same. Options A, B, and C are incorrect because the method reduces the visibility of the `protected` method version declared in the parent class. Option F is incorrect, as the parent version of `passed()` is `abstract` and cannot be invoked. That leaves options D and E as valid overrides of this method.

## 829-14-027

How many of these lines print `false`?

```java
23: var smart = """
24:    barn owl\n\n
25:    wise
26:    """;
27: var clever = """
28:     barn owl\n\n
29:    wise
30:    """;
31: var sly = """
32:    barn owl\n\n
33:    wise""";
34:
35: System.out.println(smart.equals(smart.indent(0)));
36: System.out.println(smart.equals(smart.strip()));
37: System.out.println(smart.equals(smart.stripIndent()));
38:
39: System.out.println(clever.equals(clever.indent(0)));
40: System.out.println(clever.equals(clever.strip()));
41: System.out.println(clever.equals(clever.stripIndent()));
42:
43: System.out.println(sly.equals(sly.indent(0)));
44: System.out.println(sly.equals(sly.strip()));
45: System.out.println(sly.equals(sly.stripIndent()));
```

- **A.** Zero
- **B.** One
- **C.** Two
- **D.** Three
- **E.** Four
- **F.** Five
- **G.** Six or more
- **H.** The code does not compile.

**Answer:** D

**Explanation:** The first thing to notice is that `indent(0)` matches the indentation of a text block and then normalizes line breaks. Since `sly` has the closing triple quotes (`"""`) on the same line as text, it does not have a trailing line break. Therefore, lines 35 and 39 print `true` while line 43 prints `false`. Next, `strip()` gets rid of any leading or trailing whitespace. The last text block does not have either, which means lines 36 and 40 print `false`, while line 44 prints `true`. Finally, `stripIndent()` only removes incidental whitespace. None of these text blocks has any, making lines 37, 41, and 45 print `true`. Since three lines print `false`, option D is the answer.

## 829-14-028

Which are true statements about interfaces and abstract classes? (Choose three.)

- **A.** Abstract classes offer support for single inheritance, while interfaces offer support for multiple inheritance.
- **B.** All methods in abstract classes are implicitly `public`, while interfaces can use various access modifiers for their methods and variables, including `private` in some cases.
- **C.** Both abstract classes and interfaces can have `abstract` methods.
- **D.** Both abstract classes and interfaces can have `public` constructors.
- **E.** Interfaces can only extend other interfaces, while abstract classes can extend both abstract and concrete classes.
- **F.** Unlike abstract classes, interfaces can be marked `final`.

**Answer:** A, C, E

**Explanation:** Option B is incorrect as abstract classes allow any of the access modifiers. Option D is incorrect because interfaces do not have constructors. Option F is incorrect because neither abstract classes nor interfaces can be marked `final`. Options A, C, and E are true statements.

## 829-14-029

Fill in the blanks: The operators `+=`, \_\_\_\_\_\_, \_\_\_\_\_\_, \_\_\_\_\_\_\_, \_\_\_\_\_\_\_, and `--` are listed in increasing or the same level of operator precedence. (Choose two.)

- **A.** `^`, `*`, `=`, `++`
- **B.** `%`, `*`, `/`, `&&`
- **C.** `=`, `+`, `/`, `*`
- **D.** `^`, `*`, `==`, `++`
- **E.** `*`, `/`, `%`, ++
- **F.** `<=`, `>=`, `!=`, `!`

**Answer:** C, E

**Explanation:** In option A, the assignment operator (`=`) incorrectly comes after the multiplication (`*`) operator. In option B, the short-circuit logical operator (`&&`) incorrectly comes after the division (`/`) operator. In option D, the equality operator (`==`) incorrectly comes after the multiplication (`*`) operator. In option F, the not equals operator (`!=`) incorrectly comes after the relational operators, (`<=` and `>=`). This leaves options C and E as the correct answers. For these answers, it may help to remember that the modulus operator (`%`), multiplication operator (`*`), and division operator (`/`) have the same operator precedence.

## 829-14-030

Given the following two classes in the same package, which constructors contain compiler errors? (Choose three.)

```java
public class Big {
   public Big(boolean stillIn) {
     super();
   } }
public class Trouble extends Big {
   public Trouble() {}
   public Trouble(int deep) {
     super(false);
     this();
   }
   public Trouble(String now, int... deep) {
     this(3);
   }
   public Trouble(long deep) {
     this("check", deep);
   }
   public Trouble(double test) {
     super(test>5 ? true : false);
   } }
```

- **A.** `public Big(boolean stillIn)`
- **B.** `public Trouble()`
- **C.** `public Trouble(int deep)`
- **D.** `public Trouble(String now, int... deep)`
- **E.** `public Trouble(long deep)`
- **F.** `public Trouble(double test)`

**Answer:** B, C, E

**Explanation:** The constructors declared by options A, D, and F compile without issue. Option B does not compile. Since there is no call to a parent constructor or constructor in the same class, the compiler inserts a no-argument `super()` call as the first line of the constructor. Because `Big` does not have a no-argument constructor, the no-argument constructor `Trouble()` does not compile. Option C also does not compile because `super()` and `this()` cannot be called in the same constructor. Finally, option E does not compile. There is no matching constructor that can take a `String` followed by a `long` value.

## 829-14-031

What is the output of the following code snippet?

```java
11: Path x = Paths.get(".","song","..","/note");
12: Path y = Paths.get("/dance/move.txt");
13: x.normalize();
14: System.out.println(x.resolve(y));
15: System.out.println(y.resolve(x));
```

- **A.** 

  ```java
  /./song/../note/dance/move.txt
  /dance/move.txt
  ```
- **B.** 

  ```java
  /dance/move.txt
  /dance/move.txt/note
  ```
- **C.** 

  ```java
  /dance/move.txt
  /dance/move.txt/./song/../note
  ```
- **D.** 

  ```java
  /note/dance/move.txt
  ../dance/move.txt/song
  ```
- **E.** The code does not compile.
- **F.** The code compiles but an exception is thrown at runtime.

**Answer:** C

**Explanation:** Line 13 does not modify the value of `x` because `Path` is immutable and `x` is not reassigned to the new value. On line 14, the `resolve()` method is called using `y` as the input argument. If the parameter passed to the `resolve()` method is absolute, then that value is returned, causing the first `println()` method call to output `/dance/move.txt`. On line 15, the absolute path is concatenated with the relative path, printing `/dance/move.txt/./song/../note` at runtime. For these reasons, option C is correct.

## 829-14-032

How many lines does this code output?

```java
import java.util.*;
public class PrintNegative {
  public static void main(String[] args) {
    List<Integer> list = new ArrayList<>();
    list.add(-5);
    list.add(0);
    list.add(5);
    list.removeIf(e -> e < 0);
    list.forEach(x -> System.out.println(x));
  } }
```

- **A.** One
- **B.** Two
- **C.** Three
- **D.** None. It doesn’t compile.
- **E.** None. It throws an exception at runtime.

**Answer:** B

**Explanation:** This is a correct example of using lambdas. The code creates an `ArrayList` with three elements. Both lambdas are correct. The code removes any negative numbers and prints the remaining two numbers, `0` and `5`, making option B correct.

## 829-14-033

What is the result of the following?

```java
public static void main(String[] args) {
   var list = Arrays.asList("0", "1", "01", "10");
   Collections.reverse(list);
   Collections.sort(list);
   System.out.println(list);
}
```

- **A.** `[]`
- **B.** `[0, 01, 1, 10]`
- **C.** `[0, 01, 10, 1]`
- **D.** `[0, 1, 01, 10]`
- **E.** The code does not compile.
- **F.** The code compiles but throws an exception at runtime.

**Answer:** B

**Explanation:** The `reverse()` method is just there to mislead you since `sort()` will reorder regardless of the original state. Remember that `String` values sort alphabetically rather than by number. Therefore, `01` sorts before `1`, and option B is correct.

## 829-14-034

What is the result of the following?

```java
var dice = new TreeSet<Integer>();
dice.add(6);
dice.add(6);
dice.add(4);
dice.stream()
   .filter(n -> n != 4)
   .forEach(System.out::println)
   .count();
```

- **A.** It prints just one line.
- **B.** It prints one line and then the number `3`.
- **C.** There is no output.
- **D.** The code does not compile.
- **E.** The code compiles but throws an exception at runtime.

**Answer:** D

**Explanation:** This code attempts to use two terminal operations, `forEach()` and `count()`. Only one terminal operation is allowed, so the code does not compile, and option D is correct.

## 829-14-035

How many objects are eligible for garbage collection at the end of the `main()` method?

```java
package store;
public class Shoes {
   static String shoe1 = new String("sandal");
   static String shoe2 = new String("flip flop");
   public void shopping() {
     String shoe3 = new String("croc");
     shoe2 = shoe1;
     shoe1 = shoe3;
   }
   public static void main(String... args) {
     new Shoes().shopping();
   } }
```

- **A.** None
- **B.** One
- **C.** Two
- **D.** Three
- **E.** The code does not compile.
- **F.** None of the above.

**Answer:** C

**Explanation:** While `shoe3` goes out of scope after the `shopping()` method, the `"croc"` object is referenced by `shoe1`, and therefore cannot be garbage collected. Similarly, the `"sandal"` object is now referenced by `shoe2`. No variables reference the `"flip flop"` object, so it is eligible to be garbage collected. Finally, the `Shoes` object created in the `main()` method is also eligible for garbage collection, since there are no saved references to it. For these reasons, option C is correct.

## 829-14-036

Which of the following are true? (Choose two.)

```java
20: int[] crossword [] = new int[10][20];
21: for (int i = 0; i < crossword.length; i++)
22:    for (int j = 0; j < crossword.length; j++)
23:      crossword[i][j] = 'x';
24: System.out.println(crossword.size());
```

- **A.** One line needs to be changed for this code to compile.
- **B.** Two lines need to be changed for this code to compile.
- **C.** Three lines need to be changed for this code to compile.
- **D.** If the code is fixed to compile, none of the cells in the 2D array have a value of `0`.
- **E.** If the code is fixed to compile, half of the cells in the 2D array have a value of `0`.
- **F.** If the code is fixed to compile, all of the cells in the 2D array have a value of `0`.

**Answer:** A, E

**Explanation:** Line 24 does not compile because arrays use `length`. It is `ArrayList` that uses `size()`. All of the other lines compile, making option A correct. It is permitted to split up the brackets, `[]`, in the 2D array declaration on line 20. The code is also allowed to use `crossword.length` as the loop condition on line 22, although this is not a good idea for a nested array. Instead, the inner loop should reference `crossword[i].length`. The array starts out with all two hundred of the cells initialized to the default value for an `int` of `0`. Both loops iterate starting at `0` and stopping before `10`, which causes only half of the array to be set to `'x'`. The other half still has the initial default value of `0`, making option E correct.

## 829-14-037

Which of the following can fill in the blank to output `sea lion, bald eagle`?

```java
String names = Stream.of(
   "bald eagle", "pronghorn", "puma", "sea lion")
   .collect(Collectors.joining(", "));
System.out.println(names);
```

- **A.** 

  ```java
  .filter(s -> s.contains(" "))
  .collect(Collectors.toSet())
  .stream()
  .entrySet()
  .stream()
  .filter(e -> e.getKey())
  .map(Entry::getValue)
  .flatMap(List::stream)
  .sorted(Comparator.reverseOrder())
  ```
- **B.** 

  ```java
  .filter(s -> s.contains(" "))
  .collect(Collectors.toUnmodifiableSet())
  .stream()
  .entrySet()
  .stream()
  .filter(e -> e.getKey())
  .map(Entry::getValue)
  .flatMap(List::stream)
  .sorted(Comparator.reverseOrder())
  ```
- **C.** 

  ```java
  .collect(Collectors.toUnmodifiableSet())
  .stream()
  .collect(Collectors.groupingBy(s -> s.contains(" ")))
  .entrySet()
  .stream()
  .filter(e -> e.getKey())
  .map(Entry::getValue)
  .map(List::stream)
  .sorted(Comparator.reverseOrder())
  ```
- **D.** 

  ```java
  .collect(Collectors.toSet())
  .stream()
  .collect(Collectors.groupingBy(s -> s.contains(" ")))
  .entrySet()
  .stream()
  .filter(e -> e.getKey())
  .map(Entry::getValue)
  .flatMap(List::stream)
  .sorted(Comparator.reverseOrder())
  ```
- **E.** 

  ```java
  .filter(s -> s.contains(" "))
  .collect(Collectors.toUnmodifiableSet())
  .stream()
  .collect(Collectors.groupingBy(s -> s.contains(" ")))
  .entrySet()
  .stream()
  .filter(e -> e.getKey())
  .map(Entry::getValue)
  .map(List::stream)
  .sorted(Comparator.reverseOrder())
  ```
- **F.** 

  ```java
  .collect(Collectors.toUnmodifiableSet())
  .stream()
  .collect(Collectors.groupingBy(s -> s.contains(" ")))
  .entrySet()
  .stream()
  .filter(e -> e.getKey())
  .map(Entry::getValue)
  .map(List::stream)
  .sorted(Comparator.reverseOrder())
  ```

**Answer:** D

**Explanation:** In options A and B, the stream pipeline attempts to call `entrySet()` on it. However this method is only defined on `Map`, which makes these two options incorrect. Options C, E, and F are incorrect because the second-to-last line is `map()`, instead of `flatMap()`. It doesn’t make sense to sort the `Stream` generated by `map()` and the code does not compile. By contrast, option D is correct. It creates a `Set` of matching `String` objects. It then builds a map where the key represents whether they match. Next, the code gets the list of all matching values, turns it into a `Stream`, and uses `flatMap()` to turn it back into individual objects. Finally, the code sorts in descending order and turns the result back into a `String`.

## 829-14-038

What is the output of the following code snippet?

```java
LocalDate dogDay = LocalDate.of(2022,8,26);
var x = DateTimeFormatter.ISO_DATE;
System.out.println(x.format(dogDay));
var y = DateTimeFormatter.ofPattern("Dog Day: dd/MM/yy");
System.out.println(y.format(dogDay));
var z = DateTimeFormatter.ofPattern("Dog Day: dd/MM/yy");
System.out.println(dogDay.format(z));
```

- **A.** `2022-07-26` and `Dog Day: 26/07/22`
- **B.** `2022-07-25` and `Dog Day: 25/07/22`
- **C.** `2022-08-26` and `Dog Day: 26/08/22`
- **D.** The code does not compile.
- **E.** An exception is thrown at runtime.
- **F.** None of the above.

**Answer:** E

**Explanation:** The code compiles without issue. The text `Dog Day:` is not properly escaped and includes letters, such as `o`, which are invalid patterns. Therefore, the code prints an exception at runtime, making option E correct. If the values were properly escaped, then option C would be correct.

## 829-14-039

How many of the following lines contain a compiler error?

```java
long min1 = 123.0, max1 = 987L;
final long min2 = 1_2_3, max2 = 9 _________ 8 _________ 7;
long min3 = 123, int max3 = 987;
long min4 = 123L, max4 = 987;
long min5 = 123_, max5 = _987;
```

- **A.** Zero
- **B.** One
- **C.** Two
- **D.** Three
- **E.** Four
- **F.** Five

**Answer:** D

**Explanation:** A `long` cannot contain a number with decimal points, preventing `min1` from compiling. When declaring multiple variables in the same statement, the type is only declared once. Therefore, `max3` does not compile. Underscores in numeric expressions are allowed as long as they are between two digits, making the line with `min5` and `max5` incorrect. Since three lines have compiler errors, the answer is option D. The `L` suffix is valid, as is having multiple underscores in a row.

## 829-14-040

Fill in the blanks: Using \_\_\_\_\_\_\_\_ and \_\_\_\_\_\_\_\_ together allows a variable to be accessed from any class, without requiring an instance variable.

- **A.** `class`, `static`
- **B.** `default`, `public`
- **C.** `final`, package (no modifier)
- **D.** `protected`, `instance`
- **E.** `public`, `static`
- **F.** None of the above

**Answer:** E

**Explanation:** The `public` modifier allows access from the same class, package, subclass, or even classes in other packages, while the `static` modifier allows access without an instance of the class. For these reasons, option E is the correct answer. Option A is incorrect because `class` is not a modifier; it is a keyword. Option B is incorrect because the `default` keyword is for interface methods and `switch` statements, not class variables. Option C is incorrect because `final` is not related to access, and package access limits to the package. Finally, option D is incorrect because `instance` is not a Java keyword or modifier. Further, `protected` prevents all classes outside the package other than subclasses from accessing the variable.

## 829-14-041

Which of the following sequences can fill in the blanks so the code prints `-1 0 2`?

```java
char[][] letters = new char[][] {
   new char[] { 'a', 'e', 'i', 'o', 'u'},
   new char[] { 'a', 'e', 'o', 'u'} };
var x = Arrays. _______ (letters[0], letters[0]);
var y = Arrays. _______ (letters[0], letters[0]);
var z = Arrays. _______ (letters[0], letters[1]);
System.out.print(x + " " + y + " " + z);
```

- **A.** `compare`, `mismatch`, `compare`
- **B.** `compare`, `mismatch`, `mismatch`
- **C.** `mismatch`, `compare`, `compare`
- **D.** `mismatch`, `compare`, `mismatch`
- **E.** None of the above

**Answer:** D

**Explanation:** When the arrays are the same, the `compare()` method returns `0`, while the `mismatch()` method returns `-1`. This narrows it down to option C or option D. When the arrays are different, `mismatch()` returns the index of the first element that is different. In our case, this is index `2`, making option D correct. By contrast, the `compare()` method would return a negative number if filling in the third blank since `'i'` is smaller than `'o'`.

## 829-14-042

What is the output of the following application? Assume the file system is available and able to be written to and read from.

```java
package boat;
import java.io.*;
public class Cruise {
   private int numPassengers = 1;
   private transient String schedule = "NONE";
   { numPassengers = 2; }
   public Cruise() {
     this.numPassengers = 3;
     this.schedule = "Tropical Island";
   }
   public static void main(String... p) throws Exception {
     final String f = "ship.txt";
     try (var o = new ObjectOutputStream(
           new FileOutputStream(f))) {
        Cruise c = new Cruise();
        c.numPassengers = 4;
        c.schedule = "Casino";
        o.writeObject(c);
     }
     try (var i = new ObjectInputStream(
           new FileInputStream(f))) {
        Cruise c = i.readObject();
        System.out.print(c.numPassengers + "," + c.schedule);
     } } }
```

- **A.** `2,NONE`
- **B.** `3,null`
- **C.** `4,Casino`
- **D.** `4,null`
- **E.** One line would need to be fixed for this code to run without throwing an exception.
- **F.** Two lines would need to be fixed for this code to run without throwing an exception.

**Answer:** F

**Explanation:** The `readObject()` method returns an `Object` instance, which must be explicitly cast to `Cruise` in the second try-with-resources statement. For this reason, the code does not compile. If the explicit cast were added, the code would compile but throw a `NotSerializableException` at runtime, since `Cruise` does not implement the `Serializable` interface. For this reason, option F is correct. If both of these issues were fixed, then the code would run and print `4,null`. The `schedule` variable is marked `transient`, so it defaults to `null` when deserialized, while `numPassengers` is assigned the value it had when it was serialized. Remember that on deserialization, the constructors and instance initializers are not executed.

## 829-14-043

How many lines does the following program output?

```java
class Coin {
   enum Side { HEADS, TAILS };
   public static void main(String[] args) {
     var sides = Side.values();
     for (var s : sides)
         for (int i=sides.length; i>0 ; i-=2)
            System.out.print(s+" "+sides[i]);
            System.out.println();
    }
 }
```

- **A.** One
- **B.** Two
- **C.** Four
- **D.** The code does not compile.
- **E.** The code compiles but throws an exception at runtime.
- **F.** The code compiles but enters an infinite loop at runtime.

**Answer:** E

**Explanation:** The first time the inner `for` loop is executed, it accesses `sides[i]` where `i` is equal to `sides.length`. Since the maximum element is indexed as `sides.length-1`, this results in an `IndexOutOfBoundsException`, making option E correct. Also, notice that there are no braces `{}` around the inner `for` loop; therefore, even without the exception, the most lines this code would print would be one, since there’s only one `println()` statement executed.

## 829-14-044

What is the minimum number of lines that need to be removed to make this code compile and be able to be implemented as a lambda expression?

```java
@FunctionalInterface
public interface Play {
   public static void baseball() {}
   private static void soccer() {}
   default void play() {}
   void fun();
   void game();
   void toy();
}
```

- **A.** 1
- **B.** 2
- **C.** 3
- **D.** 4
- **E.** The code compiles as is.

**Answer:** B

**Explanation:** This code is not a functional interface because it has three abstract methods: `fun()`, and `game()`, and `toy()`. Removing two of these three methods would cause the code to compile, leaving it with a single abstract method. Therefore, option B is the answer.

## 829-14-045

What is the output of this code?

```java
 10: var m = new TreeMap<Integer, Integer>();
 11: m.put(1, 4);
 12: m.put(2, 8);
 13:
 14: m.putIfAbsent(2, 10);
 15: m.putIfAbsent(3, 9);
 16:
 17: m.replaceAll((k, v) -> k + 1);
 18:
 19: m.entrySet().stream()
20:    .sorted(Comparator.comparing(Entry::getKey))
21:    .limit(1)
22:    .map(Entry::getValue)
23:    .forEach(System.out::println);
```

- **A.** `1`
- **B.** `2`
- **C.** `3`
- **D.** `4`
- **E.** The code does not compile.
- **F.** The code compiles but prints something else.

**Answer:** B

**Explanation:** Lines 10–12 created a map with two key/value pairs. Line 14 does not add to the map, since the key `2` is present. Line 15 adds a third key/value to the map. At this point, the map contains `{1=4, 2=8, 3=9}`. Line 17 replaces the values with one higher than the key, and the map contains `{1=2, 2=3, 3=4}`. The stream on lines 19–23 goes through the map and sorts ascendingly by key. It gets the lowest key from that sort, which is `1`. Then it prints the value that goes with that key, which is `2`. This makes option B the answer.

## 829-14-046

Given the following three property files, what does the following method output?

```java
toothbrush.properties
color=purple
type=generic
toothbrush_es.properties
color=morado
type=lujoso
toothbrush_fr.properties
color=violette
void brush() {
   Locale.setDefault(new Locale.Builder()
     .setLanguage("es")
     .setRegion("MX").build());
   var rb = ResourceBundle.getBundle("toothbrush",
     new Locale("fr"));
   var a = rb.getString("color");
   var b = rb.getString("type");
   System.out.print(a + " " + b);
}
```

- **A.** `morado null`
- **B.** `violette generic`
- **C.** `morado lujoso`
- **D.** `violette null`
- **E.** The code does not compile.
- **F.** An exception is thrown at runtime.

**Answer:** B

**Explanation:** Java starts out by looking for a properties file with the requested locale. In this case, the requested locale is the `fr` language, which it finds with `toothbrush_fr.properties`. For this reason, the default locale of `es_MX` is ignored, and the first value for `color` printed is `violette`. Next, it looks for a value for `type` property. Since it doesn’t find it in the first properties file, it checks the default `toothbrush.properties`, where it does find it, and prints `generic`. For these reasons, option B is correct.

## 829-14-047

Which options, when inserted into the blank, allow this code to compile? (Choose three.)

```java
import java.io.*;
class Music {
   void make() throws IOException {
     throw new UnsupportedOperationException();
   } }
public class Sing extends Music {
   public void make() _________________ {
     System.out.println("do-re-mi-fa-so-la-ti-do");
   } }
```

- **A.** `throws FileNotFoundException`
- **B.** `throws NumberFormatException`
- **C.** `throws Exception`
- **D.** `throws SQLException`
- **E.** `throws Throwable`
- **F.** Placing nothing in the blank

**Answer:** A, B, F

**Explanation:** An overriding method cannot declare any new or broader checked exceptions than the overridden method. For this reason, options C, D, and E are incorrect. Option A is correct because `FileNotFoundException` is a narrower exception than `IOException`. Option B is also correct because new unchecked exceptions are allowed. Option F is correct because not throwing any exceptions is also permitted in overridden methods.

## 829-14-048

Given the application shown here, which lines do not compile? (Choose three.)

```java
package furryfriends;
interface Friend {
   protected String getName();                  // h1
}
class Cat implements Friend {
   String getName() {                          // h2
     return "Kitty";
   } }
public class Dog implements Friend {
   String getName() throws RuntimeException {     // h3
     return "Doggy";
   }
   public static void main(String[] adoption) {
     Friend friend = new Dog();                // h4
     System.out.print(((Cat)friend).getName());     // h5
     System.out.print(((Dog)null).getName());     // h6
   } }
```

- **A.** Line `h1`
- **B.** Line `h2`
- **C.** Line `h3`
- **D.** Line `h4`
- **E.** Line `h5`
- **F.** Line `h6`

**Answer:** A, B, C

**Explanation:** All of the compilation issues with this code involve access modifiers. First, interface methods cannot be marked `protected`. This causes a compilation error on line `h1`, making option A correct. Next, lines `h2` and `h3` both override the interface method to be package access. Since this reduces the implied visibility of `protected`, the overrides are invalid and neither line compiles. Therefore, options B and C are also correct. Line `h4` is valid. An object can be implicitly cast to a superclass or inherited interface. Finally, lines `h5` and `h6` will compile without issue but independently throw a `ClassCastException` and a `NullPointerException` at runtime, respectively.

## 829-14-049

What is the output of the following?

```java
1: package reader;
2: import java.util.stream.*;
3: public class Books {
4:    public static void main(String[] args) {
5:      IntStream pages = IntStream.of(200, 300);
6:      long total = pages.sum();
7:      long count = pages.count();
8:      System.out.println(total + "-" + count);
9:    } }
```

- **A.** `2-2`
- **B.** `200-1`
- **C.** `500-0`
- **D.** `500-2`
- **E.** The code does not compile.
- **F.** The code compiles but throws an exception at runtime.

**Answer:** F

**Explanation:** When summing `int` primitives, the return type is also an `int`. Since a `long` is larger, you can assign the result to it, so line 6 is correct. All the primitive stream types use `long` as the return type for `count()`. Therefore, the code compiles, and option E is incorrect. When actually running the code, line 7 throws an `IllegalStateException` because the stream has already been used. Both `sum()` and `count()` are terminal operations, and only one terminal operation is allowed on the same stream. Therefore, option F is the answer.

## 829-14-050

\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_ modules are on the classpath, while \_\_\_\_\_\_\_\_\_\_\_\_ modules never contain a `module-info` file.

- **A.** Automatic, named
- **B.** Automatic, unnamed
- **C.** Named, automatic
- **D.** Named, unnamed
- **E.** Unnamed, automatic
- **F.** Unnamed, named

**Answer:** E

**Explanation:** Unnamed modules are always on the classpath. Both automatic and named modules are on the module path. Named modules always contain a `module-info` file, but automatic modules never do. Option E is correct, as it meets both these criteria. Index % (modulus operator), 431, 558 Symbols /\* \*/ (multiline comments), 367, 542 + or += (addition operator), 8, 98, 446, 469 \* (multiplication operator), 8, 431, 446, 558 & (and operator), 5, 445 ! (not operator), 395, 549 ^ (and/or operator), 5, 445 != (not equals operator), 8, 431, 446, 558 —> (arrow operator), 212, 235, 370–371, ( ) (parentheses), 17, 150, 212, 448, 451, 454, 498, 507, 543 482, 498 = (assignment operator), 418–419, 431, . (period), 270, 292, 332, 517, 523, 534 554–555, 558 ++ (pre/post-increment operator), 299–300, @ (at symbol), 5, 445 310, 525, 528 \\ (backslashes), 20, 378, 449, 545 + (plus sign), 5, 445 ? (bind variables), 351, 537 -- (post-decrement operator), 3, 310, 444, ~ (bitwise complement operator), 5, 7, 14, 453, 528 408, 445, 448, 552 ++ (pre/post-increment operator), 418, 554 [ ] (brackets), 186, 197, 200, 434, 490, ? (question mark), 182, 199, 201, 401, 489, 494–495, 558 495, 550 {} (braces), 41–42, 150, 212, 451–452, &lt; or &lt;= or >= (relational operator), 8, 431, 454–455, 482, 498 446, 558 && (short-circuit logical operator), 8, 30, : (colons), 340, 454, 536 431, 446, 452, 558 += or -= (compound assignment operators), / / (single-line comments), 367, 542 314–315, 529 ""+ (string concatenation), 140, 480 -- (decrement operator), 8, 446 ?: (ternary operator), 401–402, 418, / (division operator), 8, 431, 446, 558 550, 554 :: (double colon operator), 222, 228, """ (triple quotes), 2, 4, 13, 19, 22, 430, 503, 504 444–446, 447, 449–450, 557 $ (dollar sign), 102, 369, 384, 444, 470, \_ (underscore), 11, 102, 133, 163–164, 369, 542, 547 384, 414, 437, 444, 447, 470, 478, 485, == (equality operator), 426–427, 431, 542, 547, 554, 559 556, 558 ++ (increment operator), 8, 408, 446 /\*\* \*\*/ (Javadoc comments), 367, 542 A ! (logical complement operator), 8, 418, 446, 554 `abs()` method, 386, 547 | (logical operator), 20, 449, 454 `abstract` class || (logical or operator), 5, 30, 418, 445, abstract methods in, 138–139, 479 452, 554 access modifiers, 431, 557 compared to interfaces, 431, 557 `another-Line` object, 6, 445 constructors, 100, 470 `anyMatch()` method, 258, 420, 514, 555 extending, 55, 131, 322, 457, 478, 531 `apply()` method, 230–231, 506 limitations, 85, 133, 459, 466, 478 `args` variable, 456 `abstract` methods `ArrayIndexOutOfBoundsException`, access modifiers, 431, 458, 462, 557 74, 152, 165, 174, 188, 203, 206, 376, within functional interfaces, 63, 452, 454, 463, 482, 486, 488, 491, 112–113, 121–122, 134, 459, 473, 496–497, 544 475, 478 `ArrayList` class, 34, 182, 405, 551 implementing, 406, 552 arrays. See also collections inheritance, 69, 461, 465 adding elements, 182, 183, 203, 489, limitations, 98–100, 110–111, 137–139, 490, 496 381, 410–411, 439, 467, 469–470, array parameters, 62, 459 472, 479, 546, 552, 560 brackets in, 197, 494 overriding, 128, 131, 465, 477, 478 comparing elements, 196–197, 437, `private` modifier, 47, 455, 458, 462 494, 559 `protected` modifier, 455 converting to `List`, 411, 552 `abstract` modifier, 110, 472 creating, 182, 190, 192, 194, 206, 370, `abstract read()` method, 108–109, 472 403, 489, 492, 493, 497, 543, 551 `accept()` method, 213, 230–231, declaring, 434, 558 499, 506 exceptions, 300, 452, 454, 525 access modifiers indexing, 204, 206, 496–497 abstract methods, 431, 458, 462, 557 `int` in, 185–186, 490 for classes, 469 loops, 206, 376, 496–497, 544 commenting out, 135, 479 multidimensional, 183, 186, 188, 196, leaving blank, 415, 554 200, 202, 490, 491, 494, 495, 496 limitations of, 48, 78, 455, 464 removing elements, 202, 496 package, 119, 474 sorting elements, 185, 190, 191, 194, private (See `private` modifier) 195, 198, 199, 200, 202, 208–209, protected (See `protected` modifier) 412, 432–433, 490, 492, 493, 494, public (See `public` modifier) 495, 497, 552, 558 accessor, defined, 81, 465 `String` in, 184, 185–186, 199–200, `addFirst()` method, 206, 497 209, 224–225, 490, 495, 497, 504 addition operator (+ or +=), 8, 98, 446, 469 threads in, 315, 529 `addLast()` method, 206, 497 two-dimensional, 183, 193–194, 195, `addStationName()` method, 203, 496 199–200, 209, 490, 493, 495, 498 `allMatch()` method, 258, 420, 514, 555 unsorted, 371, 543 and operator (&), 5, 445 `Arrays.compare()` method, and/or operator (^), 5, 445 196–197, 494 anonymous classes, 67–68, 108, 127, `ArrayStoreException`, 199–200, 136–139, 461, 472, 476, 479 495 arrow operator (—>), 212, 235, 370–371, `break` statement, 31, 32, 35, 43, 452, 454, 498, 507, 543 453, 455 assignment operator (=), 418–419, 431, `BufferedReader` class, 334–335, 534 554–555, 558 `byte` variable, 26, 450–451 at symbol (@), 5, 445 `byteValue()` method, 21, 450 `AutoCloseable` interface, 158, 160, 367, 377, 484, 542, 545 autocommit mode, 351, 538 C `average()` method, 218, 226, 235, 501, `Callable` parameter, 296–297, 524 504, 507 `case` keyword, 452 `case` statement, 39–40, 398, 452–455, 457, 550 B catch blocks, 150, 154, 160, 167–168, backslashes (\\), 20, 378, 449, 545 170–171, 395, 482–487, 549 `Bend` interface, 56–57, 458 `ceil()` method, 386, 547 `BiConsumer`, 216, 500 chaining, 448 `BiFunction` interface, 212, 216, 242, 246, `char` arrays, 324, 531 498, 500, 509, 511 `char` type, 26, 449–451 `binarySearch()` method, 191, 198, 202, `Character` class, 450–451 492, 494, 495 `charAt()` method, 7, 232, 446, 506 bind variables, 341–342, 347–348, checked exceptions 536, 537 handling, 156, 164–165, 301–302, `BiPredicate` interface, 233, 506 327–328, 410, 483, 485–486, 526, bitwise complement operator (~), 5, 7, 14, 532, 553 408, 445, 448, 552 method signatures, 170, 487 `blocks` variable, 193–194, 493 overriding, 96, 119–120, 161–162, `boolean` expressions, 36, 38, 453 171–172, 175–176, 366–367, `boolean/Boolean` data types 385–386, 441, 469, 475, 485, class, 451 487–488, 541, 547, 560 default initialization, 65, 460 try/catch/finally, 157, 483 operators, 5, 8, 9, 395, 431, 445–446, `ChronoUnit enum` parameter, 10, 447 448, 549, 558 `ClassCastException`, 138–139, 164, required conditions, 451 165, 179, 188, 441, 479, 485, 486, `BooleanSupplier` interface, 243, 489, 491, 560 263–264, 509, 516 classes. See also subclasses bottom-up migration, 278, 286–287, 289, accessibility, 68, 461 411, 519, 522–523, 552 anonymous, 67–68, 108, 127, 136–139, braces ({}), 41–42, 150, 212, 451–452, 461, 472, 476, 479 454–455, 482, 498 assigning, 467 brackets ([ ]), 186, 197, 200, 434, 490, concrete, 100, 121, 470, 475 494–495, 558 creating, 47, 455, 456 declaring, 102, 117, 118, 182, 382–383, migration, 286, 289, 520, 523 427, 455, 456, 460, 469, 470, 474, modules on, 268, 279, 281, 291, 442, 489, 546, 557 517, 519–523, 560 defining as local types, 378, 545 `climb()` method, 178–179, 489 exception, 159, 484 `close()` method, 158, 160, 166, 170, 176, extending, 72, 100, 101, 116–117, 394, 377, 484, 486, 487, 488, 545 462, 470, 474, 548 `Closeable` interface, 160, 176, 367, 484, `final`, 67, 100, 128, 461, 470, 477 488, 542 immutable, 6–7, 13, 17, 67, 87–88, 113, `collect()` method, 220, 242, 427, 502, 122, 445, 447, 449, 459, 461, 467, 509, 557 473, 475 `Collection` interface, 203, 256, 260, 405, implementing, 46–47, 455, 456 496, 514, 515, 551 importing methods, 106, 471 collections. See also arrays; `List` inheritance, 58–59, 88, 458, 467 collections; `Map` collections inner, 105, 126, 142, 373, 469, 471, concurrent, 306, 312, 476, 480, 543 526–527, 528–529 instantiation, 101, 409–410, 470, 552 `Deque`, 189, 196, 206, 409, 491, 494, interfaces, compared to, 146–147, 431, 497, 552 481, 557 immutable, 195, 197, 493–494 interfaces, multiple, 95, 120–121, `null` values in, 200, 495 468, 475 parallel streams with, 236, 256, interfaces and, 48, 55, 146–147, 456, 310–311, 507, 514, 528 457, 481 `Set`, 188, 189, 190, 195, 200, 491, local, 64, 460 493, 495 member variables, 115, 473 sorting elements, 198, 494 modifiers, 100, 470 wildcards, 401, 550 mutable, 446 `Collector` argument, 220, 264, 502, 516 nested, 59–60, 64–65, 108, 411–412, `Collectors`, 427, 557 459, 460, 472, 552 colons (:), 340, 454, 536 overriding methods, 128, 457, 477 command-line arguments, 199, 495 polymorphism, 456 commas, 454 sealed, 72, 107, 129–130, 462, 472, 477 comments, 135, 367, 462, 479, 542 `static`, 59–60, 64–65, 140, 459, `CompactNumberFormat`, 359, 392, 460, 480 540, 548 time zone, concept of, 2, 444 `Comparable` interface, 205, 496 top-level, 469 `Comparator`, 184–185, 254, 257, 421, wrapper, 1, 12, 17, 26, 447, 490, 513, 514, 555 449, 450–451 `compare()` method, 421, 437, 555, 559 classpath `compareTo()` method, 192, 421, 492, 555 accessing from module path, 287 compound assignment operators (+= or -=), long forms, 422, 555 314–315, 529 `compute()` method, 169, 486 currency, formatting, 356–357, 361, 392, `concat()` method, 214, 449, 499 539, 540, 548 concrete classes, 100, 121, 470, 475 currency symbols, 102, 133, 444, concurrency, 300, 306, 308–310, 312, 470, 478 314–315, 395, 404, 525–529, cyclic dependencies 549, 551 breaking, 375, 544 `Concurrent` class, 300, 404, 525, 551 constructors, 134–135, 479 `Connection` interface, 340, 351, 536, 538 JARs, 271, 274, 518 `conn.rollback()` method, 343–344, modules, 272–273, 285, 293, 375, 518, 346, 536, 537 521, 524, 544 constructors `CyclicBarrier`, 302–303, 311–312, calling, 71, 82–83, 111, 157–158, 462, 376–377, 526, 528, 544 465, 473, 484 compact, 76, 123–124, 145–146, 378, 396, 463, 476, 481, 545, 549 D compared to methods, 68, 461 cyclic dependency, 134–135, 479 `-d` option, 285, 521 deferred execution, 216, 500 dashes (-), 269–270, 517 exceptions, 153, 157–158, 173, 176, databases, 340, 341, 347–352, 424–425, 482, 484, 487–488 536–538, 556 ignoring input to, 447 dates implementing, 110, 141, 464, 472, 480 adding, 5, 14, 16–17, 20, 445, 448–449 lambda expressions, 217, 501 formatting, 354–357, 359–361, missing, 390, 548 363–364, 372, 538–541, 543 multiple, 460 subtracting, 5–6, 11–12, 20, 445, no-argument, 68, 73, 111, 118, 126, 447, 449 130, 153, 366–367, 379, 431–432, types, 448 461, 463, 473, 474, 476, 477, 482, `DateTimeException`, 12, 447 541, 545, 558 `DateTimeFormatter`, 355, 357, overloaded, 76, 145–146, 463, 481 359–360, 538–540 values in, 124, 476 daylight savings time, 2–4, 6–7, 10–11, 13, `Consumer` interface, 227, 504 22, 385, 425, 444–445, 447–448, 450, consumers 547, 556 creating, 284, 521 deadlock, 312, 376–377, 528, 544 defined, 270, 517 decrement operator (--), 8, 446 dependencies, 274, 291–292, 518, 523 `default` interface, 380, 546 recompiling, 420, 555 `default` keyword, 129, 455, 477 `count(*)` function, 349, 537 `default` method `count()` method, 218, 501 inheritance, 69, 457, 462, 465 country codes, 360, 411, 540, 552 overriding, 48, 380, 456, 546 `-cp` option, 422, 555 using, 101, 470 `createStatement()` method, 352, 538 `default` modifier functional interfaces, 467 proper, 64, 73–74, 86–87, 130, 145, incorrect usage, 71, 462 460, 463, 466, 477, 481 methods within an interface, 75–76, 89, using, 460 463, 467 `enum`, 3, 26, 78, 444, 451, 457, 464 `default` statements, 26, 28, 418, 451, 554 enums defensive copies, 67, 461 changing value of, 88–89, 467 `Deque` collections, 189, 196, 206, 409, constructors, 98–99, 398, 469, 550 491, 494, 497, 552 defined, 59–60, 459 deserialization, 323–324, 336, 438, 531, properties, 465 534, 559 punctuation, 132, 423–424, 478, 556 diamond operator (&lt;>), 196, 198, 395, valid, 423–424, 556 494–495, 549 equality operator (==), 426–427, 431, direct types, 205, 496 556, 558 `distinct()` method, 246, 510 `equals()` method, 401, 550 division operator (/), 8, 431, 446, 558 `Error` case, 154, 173–174, 177, 483, 488 dollar sign ($), 102, 369, 384, 444, 470, escaping text, 364, 436–437, 541, 559 542, 547 essential whitespace, 4, 445 dot notation, 465 `Exception`, 153, 167–168, 210, 482, `Double`, 182, 212, 216, 489, 498, 500 486, 498 double colon operator (::), 222, 228, `Exception` constructor, 153, 482 503, 504 exception handling `double` type, 5, 6, 26, 216, 226–227, 445, catch blocks, 150, 154, 160, 167–168, 450–451, 500, 504 170–171, 395, 482–487, 549 `DoubleBinaryOperator` interface, custom, 155, 176, 483, 488 371–372, 543 lambda expressions, 327–328, 532 `DoublePredicate` interface, 253, 513 method overrides, 119–120, 161–163, `DoubleStream`, 218, 501 166, 171–172, 175, 475, 485, 486, `DoubleSupplier` interface, 215, 499 487, 488 `DoubleUnaryOperator` interface, by methods, 164–165, 170, 410–411, 246, 511 485–487, 552 `do/while` loop, 36, 40, 373–374, 453, multi-catch blocks, 160, 167–168, 454, 543 170–171, 395, 484, 486–487, 549 `DriverManager`, 341, 536 passing to methods, 176–177, 488 `Duration` class, 2, 5–6, 444, 445 try/catch/finally, 151–154, 156–157, 161, 163–168, 170, 172–175, 178–179, 241, 321, 413–414, 423, 428, 482–483, 485–489, 509, 530, E 553, 556–557 `else` statement, 34, 36–37, 452, 453 try-with-resources, 150, 152–154, 156, encapsulation 162, 166, 177, 321, 326, 333–334, broad access, 458 367, 377, 388–389, 438, 482–483, poor/improper, 70–71, 91–92, 485–486, 488, 530–532, 534, 542, 462, 468 545, 547, 559 exceptions limitations, 46, 53, 98–99, 101, 139, checked (See checked exceptions) 394, 455, 457, 458, 462, 465, 469, constructors in, 153, 157–158, 173, 470, 479, 548 176, 482, 484, 487–488 try-with-resources statements, 154, inheritance, 156, 167–169, 175, 179, 388–389, 483, 547 410, 483, 486, 488, 489, 552 `findAny()` method, 218, 231, 378, 501, naming, 163–164, 485 506, 545 unchecked, 86, 155, 162–163, 171, 172, `findFast()` method, 378, 545 190, 466, 483, 485, 487, 492 `findFirst()` method, 248, 378, 511, 545 `execute()` method, 311, 341, 345, 407, `findSlow()` method, 378, 545 528, 536, 537, 552 `flatMap()` method, 219, 260, 261, `executeQuery()` method, 341, 347–348, 434–436, 501, 515, 559 536, 537 `float` parameter, 6, 98, 445, 469 `executeUpdate()` method, 341, 536 `float` type, 26, 451, 467 `Executors` class, 302, 526 floating-point types, 26, 451 `ExecutorService`, 297, 302–303, 314, `floor()` method, 23, 450 392, 524, 526, 529, 548 flow scoping, 401, 452, 550 `exports` directive, 269, 276, 279–280, `for` loops 517, 519–520 commas, 454 compared to for-each, 29, 452 creating, 376, 544 executing, 38, 425, 438–439, 454, F 556, 560 `FileAlreadyExistsException`, 531 for-each loops, 29, 31–32, 36, 451–453 `FileNotFoundException`, 119–120, `forEach()` method, 203, 221–222, 228, 170–171, 413–414, 475, 487, 553 260, 264, 331, 402, 496, 502, 504, `Files` class, 321–322, 530 515–516, 533, 551 `Files` methods, 330–331, 533 `forEachOrdered()` method, 239, 316, `Files.find()` method, 330, 533 508, 530 `Files.isSameFile()` method, 330, 533 formal types, 191, 193, 205, 369–370, 492, `Files.lines()` method, 333, 337, 534 493, 496, 542 `Files.list()` method, 337, 534 `for-reversed` loops, 31–32, 452 `Files.readAllLines()` method, `Function`, 232, 421–422, 506, 555 333, 534 functional interfaces `Files.walk()` method, 330–331, 533 `BooleanSupplier`, 243, 509 `filter()` method, 202, 237, 246, creating, 56–57, 69, 224, 458, 461, 503 408–409, 496, 507, 510, 552 `default` modifier, 467 `final` modifier defined, 112–113, 473 classes, 67, 100, 122, 128, 461, 470, generic arguments, 219–220, 242, 475, 477 502, 509 implementing, 84, 379, 466, 545 input arguments, 252–253, 255, implicit application of, 53, 77, 457, 464 513, 514 in `java.util.function` package, `get` prefix, 81, 465 218, 253, 501, 513 `getAsDouble()` method, 226, 504 lambda expressions in, 210–213, `getBundle()` method, 358, 539 215–218, 223, 225–226, 228–229, `getColor()` method, 237, 507 236, 239, 244–245, 246, 250, 254, `getName()` method, 332–333, 386–387, 263, 402, 439, 498–501, 503–505, 406, 534, 547, 552 507–508, 510–513, 516, 551, 560 `getNumRakes()` method, 60, 459 limitations, 69, 82, 100, 461, 465, 470 `getOrDefault()` method, 209–210, 498 methods in, 63, 110–113, 121–122, `getSeconds()` method, 2, 444 134, 232, 246, 459, 472–473, 475, `GetSet` instance, 139–140, 479 478, 506, 511 `getTime()` method, 382–383, 546 parameters, 226–227, 258, 371–372, `getYear()` method, 446 504, 514, 543 `getYears()` method, 446 primitive values, 221, 502 `groupingbyConcurrent()` method, `private` modifier, 467 227–228, 504 requirements, 121–122, 134, 475, 478 return types, 227, 504 `Supplier`, 225–226, 254, 263–264, H 504, 513, 516 `HOURS` constant, 10, 447 valid, 63, 127, 397, 459, 477, 549 hyphen (-), 133, 478 `@FunctionalInterface` annotation, 69, 112–113, 134, 461, 473, 478 I idempotent, 158, 484 G identifier names, 163–164, 374, 485, 544. garbage collection See also variables eligible objects, 92, 140–141, 400, 407, `if` statement 434, 468, 469, 480, 550, 552, 558 `boolean` expressions in, 38, 453 forcing, 466 compared to `switch` statements, generic types 39–40, 454 changing, 246, 510 executing, 33, 114–115, 452, 473 `extends` keyword, 369–370, 542 loops, 38–39, 454 functional interfaces, 219–220, 242, variables declared in, 70, 418, 462, 554 502, 509 `if-else` statement, 34, 401–402, 452, instantiating, 395, 549 453, 550 limitations, 205, 419, 496, 555 `ifPresent()` method, 221–222, 502 missing, 220–221, 502 `if-then` statement, 21, 450 reference, 193, 493 `IllegalArgumentException`, 74, 86, unspecified, 219, 502 172, 175, 179, 241, 332–333, 395, `get()` method, 232, 262, 263–264, 307, 463, 466, 487–489, 509, 534, 549 506, 516, 527 immutability classes, 6–7, 13, 17, 67, 87–88, 113, instance variables 122, 445, 447, 449, 459, 461, 467, calling based on reference, 58–59, 458 473, 475 changing, 459 collections, 195, 197, 493, 494 defined, 460 defined, 67, 73–74, 458, 461, 463 initializing, 171–172, 487 implementing, 87–88, 122, 467, 475 limitations, 60, 79, 437, 458, 459, 464, `Path` instances, 534, 558 467, 559 `import` statement, 229, 455, 505 `instanceof` operators, 109, 405, `IN` parameters, 426, 556 472, 551 `IncidentReportException` case, `Instant` class, 14, 21, 448, 450 173, 487 `int` types increment operator (++), 8, 408, 446 compatible values, 143, 480 `incrementBy10()` method, 307, 527 primitive, 442, 560 indentation, 449 variables, 3, 5, 26, 89–90, 226–227, indexing, 204, 206, 344–345, 447, 453, 444–445, 450–451, 467, 504 496–497, 536–537 `Integer` class, 77, 204, 238, 260, inheritance 450–451, 464, 496, 508, 515 abstract, 69, 461, 465 interfaces classes and subclasses, 58–59, 88, abstract, 133, 458, 478 458, 467 access levels, 62, 75–76, 459, 463 `default` methods, 69, 457, 462, 465 built-in, 340, 535 exceptions, 156, 167–169, 175, 179, classes and, 48, 55, 146–147, 431, 456, 410, 483, 486, 488, 489, 552 457, 481, 557 methods, 58, 128, 458, 465, 477 declaring, 69, 462 models, 158–159, 484 extending, 55, 457, 461 multiple, 457, 461 implementing, 158, 460, 484 no-argument constructors, 111, 473 implementing multiple, 95, 97–98, overriding signatures, 380, 546 120–121, 411–412, 468, 469, `init()` method, 127–128, 136–137, 475, 552 367–368, 477, 479, 542 inheritance, 128, 465, 477 initializers JDBC, 340, 535 instance, 116, 455, 467, 474 limitations, 100, 470 `static`, 80, 107–108, 115–116, methods within, 75–76, 89, 93, 100, 409–410, 455–456, 465, 472, 392, 441, 463, 467, 468, 470, 474, 552 548, 560 input parameters, 349, 537 methods without bodies, 121, 475 `InputStream.` See `I/O Streams` modifiers, 110, 472 `insert()` method, 7, 446, 448 `private` modifier, 110, 472 instance initializers, 116, 455, 467, 474 static, 51–52, 456 instance keywords, 54, 457 `static` methods, 89, 100, 110, 119, instance methods, 117–118, 138, 373, 381, 467, 470, 472, 474, 546 460–461, 474, 479, 543 sub-interfaces, 370, 542 using, 79, 464 `invokeAll()` method, 305–306, 526 variables within, 123, 475 `invokeAny()` method, 305–306, 526 interfaces, functional `I/O Streams`, 324–326, 328–329, 331, `BooleanSupplier`, 243, 509 334–335, 336, 531, 532, 533, 534 creating, 56–57, 69, 224, 458, 461, 503 `IOException`, 170–171, 173, 176, `default` modifier, 467 327–328, 487, 488, 532 defined, 112–113, 473 `isBlank()` method, 16, 448 generic arguments, 219–220, 242, `isEmpty()` method, 16 502, 509 `isNotNull()` method, 221–222, 502 input arguments, 252–253, 255, `isPresent()` method, 221–222, 502 513, 514 iterations, 28, 37–38, 451, 453 in `java.util.function` package, 218, 253, 501, 513 lambda expressions in, 210–213, J 215–218, 223, 225–226, 228–229, JARs 236, 239, 244–245, 246, 250, 254, cyclic dependencies, 271, 274, 518 263, 402, 439, 498–501, 503–505, modular, 287, 291, 522, 523 507–508, 510–513, 516, 551, 560 non-modular, 274, 518 limitations, 69, 82, 100, 461, 465, 470 using internal APIs in, 404, 551 methods in, 63, 110–113, 121–122, `java` command, 270, 517 134, 232, 246, 459, 472–473, 475, Java runtime images, 276, 519 478, 506, 511 `javac` command, 273, 285–286, 412, 422, parameters, 226–227, 258, 371–372, 518, 521, 552, 555 504, 514, 543 Javadoc comments (/\*\* \*\*/), 367, 462, primitive values, 221, 502 542 `private` modifier, 467 `java.lang.Error`, 177, 488 requirements, 121–122, 134, 475, 478 JDBC interfaces, built-in, 340, 535 return types, 227, 504 `jdbc` protocol, 340, 536 `Supplier`, 225–226, 254, 263–264, JDBC URL, 340, 536 504, 513, 516 `jdeps` command, 275, 280, 282, 284, 293, valid, 63, 127, 397, 459, 477, 549 404, 519–521, 524, 551 interfaces, service provider, 270–271, 277, `jdeps` output, 286, 522 282–283, 291, 371, 517, 519, 521, JDK, 340, 535 523, 543 `jlink` command, 276, 282, 397, 519, `InterruptedException`, 301–302, 526 521, 549 `IntFunction` interface, 227, 260–261, `jmod` command, 275, 286, 519, 522 504, 515 `IntStream` interface, 252, 400, 512, 550 `IntSummaryStatistics` class, 239–240, L 255, 508, 513 `IntSupplier` functional interface, 236, `LackOfInformationException`, 371–372, 507, 543 366–367, 541 `IntUnaryOperator`, 219–220, 368, lambda expressions 502, 542 accumulators, 221, 502 asynchronous task definition, 298, 525 creating, 182–183, 186–187, 193, 201, compared to method references, 208, 210, 403, 489, 491, 492–493, 240, 508 495, 497, 498, 551 deferred execution, 216, 246, 500, 511 immutable, 197, 494 defining local variables within, 145, 481 lambda expressions with, 260, 515 exception handling, 327–328, 532 `null` values in, 200, 495 functional interface implementation, removing elements, 202, 208, 496, 497 210–213, 215–218, 223, 225–226, semicolons in, 78, 464 228–229, 236, 239, 244–245, 246, sorting elements, 194, 197, 207, 412, 250, 254, 263, 402, 439, 498–501, 432–433, 493, 494, 497, 552, 558 503–505, 507–508, 510–513, 516, updating elements, 193, 492–493 551, 560 `List.of()` method, 197, 494 in methods, 223–224, 375–376, 380, livelock, 301, 525 503, 544, 546 `load()` method, 268, 271–272, 278, 289, parameters, 143, 249, 380, 480, 292, 371, 516, 518, 520, 523, 543 512, 546 `loadPod()` method, 361–362, 540 passing to methods, 243, 368, 509, 542 `LocalDate` class, 3, 5–6, 8, 355, 444, 445, replacing with method references, 233, 446, 539 245, 248–249, 252, 258, 506, `LocalDateTime` class, 2, 21, 444, 450 510–512, 514 `Locale` class, 411, 552 stateful, 245, 312, 395, 510, locales 528–529, 549 defining, 354, 358, 538, 540 in streams, 202, 234, 496, 506 formats, 356, 360, 392, 411, 539, 540, syntax, 234, 235, 370–371, 507, 543 548, 552 using method references, 222, 503 properties files, 356, 357–358, 359, using with `list`, 260, 515 361–363, 440, 539, 540, 560 valid, 212, 214, 253, 256, 262–263, localization 415, 498, 499, 513, 514, 516, 554 currency formatting, 356–357, 361, variables, 220–221, 250, 467, 502, 512 392, 539, 540, 548 language codes, 360, 411, 540, 552 date formatting, 354–357, 359–361, lazy evaluation, 245, 249, 510, 512 363–364, 372, 538–541 `length()` method, 7, 26, 238, 446, number formatting, 354–355, 359–360, 450, 508 538, 540 `limit()` method, 214, 499 resource bundles, 359, 361–363, 540 line breaks, 378, 430, 545, 557 time formatting, 355, 360–361, 372, `lines()` method, 333, 534 396, 539–540, 543, 549 `LinkedList` data structure, 183, 406, using resource bundles, 354, 538 490, 552 `LocalTime` class, 2, 8–9, 444, 446, 448 `List` collections `lock()` method, 298–299, 313, 379–380, changing elements, 206–207, 497 525, 529, 545 converting arrays to, 411, 552 `lockUp()` method, 298–299, 525 logical complement operator (!), 8, 418, `main()` method 446, 554 accessing variables, 456 logical operator ( | ), 20, 418, 449, creating threads, 376–377, 544 454, 554 defining, 59–60, 459 logical or operator ( || ), 5, 30, 445, 452 defining classes with, 94, 136, 468, 479 logical or operator/disjunctive short-circuit exception handling, 164–165, 170, operator ( || ), 418, 554 485–486, 487 `Long` wrapper class, 12, 447 instantiating objects, 93, 107–108, `LongConsumer` interface, 213, 499 468, 472 `LongFunction` interface, 213, 499 limitations, 67–68, 79–80, 137–138, `LongPredicate` interface, 213, 499 143, 155, 457, 461, 465, `LongSupplier` interface, 213, 227, 479–480, 483 499, 504 passing changes, 103–104, 471 `LongToIntFunction` interface, `MajorProblemException`, 157–158, 484 371–372, 543 `Map` collections `LongUnaryOperator` interface, 253, 513 converting streams to, 235, 507 loops creating, 183, 187, 195–196, 203, 204, arrays, 206, 376, 496–497, 544 439–440, 490, 491, 493, 496, 560 breaking, 31, 32, 452 sorting elements, 190, 204, 491, 496 commas, 454 working with streams, 218, 434–436, `do/while`, 36, 40, 373–374, 453, 501, 559 454, 543 `map()` method, 234, 434–436, 506, 559 `for` (See `for` loops) `Matrix.` prefix, 84, 466 for-each, 28, 29, 31–32, 36, 451–453 `max()` method, 5, 218, 445, 501 `for-reversed` loops, 31–32, 452 member variables, 115, 473 `if` statement, 38–39, 454 method chaining, 448 infinite, 36, 313–314, 451, 453, 529 method references initialization types, 454 compared to lambda expressions, inner/outer, 373–374, 453, 455, 543 240, 508 iterations, 37–38, 453 lambda expression utilization, 222, 503 ordering elements, 31–32, 452 replacing lambda expressions, 233, requirements of, 40, 454 245, 248–249, 252, 258, 506, `while`, 29, 36, 40, 373–374, 398, 451, 510–512, 514 453–454, 543, 550 method signatures checked exceptions, 170, 487 completing, 201, 495 M incorrect, 184–185, 490 `m()` method, 36–37, 453 multiple same, 75, 97–98, 133, `-m` option, 288, 522 143–144, 420–421, 429–430, 463, `Magazine` class, 188, 491 469, 478, 481, 555, 557 `magic()` method, 34–35, 243, 509 overloading, 384–385, 547 parameters, 226–227, 371–372, checked exceptions, 96, 119–120, 504, 543 171–172, 175, 385–386, 441, 469, signature matching, 457 475, 487–488, 547, 560 methods. See also specific methods with classes, 128, 457, 477 abstract (See `abstract` method) covariant return types, 85, 463, 466 accessing, 136–137, 367–368, 479, 542 with covariant return types, 124, 131, calling, 177, 388, 390, 488, 547, 548 476, 477 case sensitive, 387, 547 declaring exceptions, 162–163, 485 compared to constructors, 68, 461 `default` methods, 48, 69, 97–98, 456, declaring, 150, 201, 482, 495 462, 469 encapsulation, 458 implementing multiple interfaces, exception handling by, 164–165, 170, 120–121, 475 410–411, 485–487, 552 inherited `default` methods, 79–80, in functional interfaces, 110–113, 106, 465, 471 121–122, 134, 232, 246, 459, instance methods, 460 472–473, 475, 478, 506, 511 limitations, 119–120, 162–163, 166, inheritance, 58, 128, 458, 465, 477 171–172, 175, 386–387, 429–430, instance, 117–118, 138, 373, 460–461, 475, 485–488, 547, 557 474, 479, 543 `static` and `non-static` within interfaces, 75–76, 89, 93, 100, methods, 99, 470 392, 441, 463, 467, 468, 470, throwing exceptions, 161–162, 485 548, 560 migration lambda expressions in, 223–224, automatic modules, 278–279, 411, 375–376, 380, 503, 544, 546 519–520, 552 naming, 133, 369, 478, 542 bottom-up, 278, 286–287, 289, 411, overloading, 341, 457, 536 519, 522–523, 552 passed to lambda expressions, 243, 368, named, 287, 522 509, 542 top-down, 274, 289, 411, 518, 523, 552 passing exceptions to, 176–177, 488 unnamed modules, 279, 287, 289, 411, `private` interface, 75–76, 147, 520, 522–523, 552 463, 481 `min()` method, 23, 450 side effects, 230, 505 `minus()` method, 5–6, 445 static, 48, 448, 456, 457 `mismatch()` method, 329, 532 `static` (See static methods) `MissedCallException` case, 174, 488 synchronous operation, 305–306, 526 `MissingResourceException` case, 539 virtual invocation, 58–59, 458 modifiers. See specific types. without bodies, 121, 475 module path option, 412, 422, 552, 555 methods, overriding `module-info` file, 268–270, 272–273, `abstract` methods, 128, 131, 465, 275–276, 279–280, 291, 442, 477, 478 517–520, 523, 560 modules nanoseconds, 446 automatic, 269, 278–279, 281, 291, nested classes, 59–60, 64–65, 108, 411, 442, 517, 519–521, 523, 411–412, 459, 460, 472, 552 552, 560 `nested()` method, 143, 480 built-in, 273, 277, 291, 518, 519, 523 `new` keyword, 82–83, 140–141, 150–151, cyclic dependencies, 272–273, 285, 293, 459, 465, 480, 482 375, 518, 521, 524, 544 newline, 449 declaring, 268, 277, 278, 369, 374, 516, `newValue` variable, 250, 512 519, 520, 542, 544 NIO.2 attribute interfaces, 330–331, 533 default, 269, 517 no-argument constructors defined, 269, 288, 517, 522 assigning value, 379, 545 dependencies, 271, 274, 275, 281–282, default automatically inserted, 68, 118, 284, 290–292, 517, 518, 519, 130, 366–367, 431–432, 461, 474, 521, 523 477, 541, 558 directives (See specific directives) `Exception` class, 153, 482 exposing content, 277, 282, 519, 521 inheritance, 111, 473 file formats, 292, 523 limitations, 73, 126, 431–432, 463, `jlink` command, 276, 397, 519, 549 476, 558 listing, 270, 284, 517, 521 `noneMatch()` method, 258, 514 migration implementation, 274, `non-sealed` modifier, 46, 58–59, 72, 84, 278–279, 286–287, 289, 411, 100, 455, 458, 462, 466, 470 518–520, 522–523, 552 `non-static` methods, 99, 470 named, 268, 275, 276, 281, 517, `NoSuchElementException`, 409, 552 519, 521 `NoSuchFileException`, 329–330, 533 naming, 270, 275, 292, 517, 519, 523 not equals operator (!=), 8, 431, 446, 558 recompiling, 420, 555 not operator (!), 395, 549 service locators (See service locators) `NotSerializableException`, 335–336, shared, 281–282, 521 438, 534, 559 unnamed, 269, 276, 279, 281, 287,

```java
null
```

289, 291, 411, 442, 517, 519–520, in collections, 200, 495 522–523, 552, 560 initializing to, 403–404, 551 modulus operator (%), 431, 558 limitations, 18, 449, 468 multi-catch blocks, 160, 167–168, passing, 137–138, 479 170–171, 395, 484, 486–487, 549 using with `instanceof`, 109, 405, multiline comments (/\* \*/), 367, 462, 542 472, 551 multiplication operator (\*), 8, 431, `NullPointerException`, 151, 165–166, 446, 558 169, 175, 179, 200, 214, 328, mutability, 458 333–334, 374–375, 403–404, 441, 482, 486, 488, 489, 495, 499, 532, 534, 544, 551, 560 N `num` variable, 262–263, 516 `n()` method, 36–37, 453 numbers. See also dates `name` parameter, 457 in class declarations, 102, 470 currency, 356–357, 361, 392, 539, operators. See specific operators 540, 548 operators, levels of precedence, 8, 446 formatting, 354–355, 359–360, `Optional`, 262, 389–390, 516, 547 538, 540 `OptionalDouble`, 218, 226, 501, 504 in identifier names, 163–164, 485 `order()` method, 176–177, 488 rounding, 359, 386, 540, 547 `ordinal()` method, 89–90, 467 in variable names, 444 `OutputStream.` See `I/O Streams` numeric expressions, 437, 559 overloading numeric types, 21, 448, 450 compared to overriding, 457 `num_pages`, 344, 536 constructors, 76, 145–146, 463, 481 `LocalTime` class, 8, 446 method signatures, 384–385, 547 O methods, 341, 457, 536 valid forms, 133, 478 `o()` method, 36–37, 453 `@Override` annotation, 81, 465 `Object` class, 26, 451 overriding. See also methods, overriding objects. See also `Path` objects; access levels, 58, 458 specific objects checked exceptions, 119–120, 171–172, casting, 141–142, 480 175–176, 366–367, 385–386, 441, chaining, 448 475, 487–488, 541, 547, 560 declaring, 46, 455 compared to overloading, 457 deserialization, 323–324, 336, 438, 531, including in records, 105, 471 534, 559 instance variables, 79, 464 eligible for garbage collection, 92, interface signatures, 380, 546 140–141, 400, 407, 434, 468, 469, limitations, 98–99, 102–103, 107, 480, 550, 552, 558 469, 471 initialization, 101, 470 objects, 72, 462 manipulating, 1 scope, 130–131, 477 overriding, 72, 462 unchecked exceptions, 86, 466 polymorphism, 102–103, 107, 141–142, 470, 471, 480 references to, 125, 476 P serialization, 326, 335–336, 438, 531–532, 534, 559 `p()` method, 36–37, 453 `ObjLongConsumer` interface, `-p` option, 412, 422, 552, 555 371–372, 543 package access `ofDays()` method, 5–6, 445 on module paths, 287, 522 `offer()` method, 183, 189, 490, 491 `private`, 48, 455 offset changes, 2–3, 444 `protected`, 119, 132, 367–368, 388, `ofNullable()` method, 389–390, 547 474, 478, 542, 547 `opens` directive, 519 `public`, 48, 455 `operate()` method, 161–162, 485 restricting, 415, 554 package sharing, 279–280, 520 `Period` class, 2, 16–17, 444–446, 448 `package` statement, 455 `permits` clause, 58–59, 72, 116–117, 147, `parallel()` method, 229, 232, 256, 505, 370, 456, 458, 462, 474, 481, 542 506, 514 `play()` method, 78, 85, 99, 134, 161, 464, parallel streams 466, 470, 479, 485 forcing to behave as serial, 243, 312, plus sign (+), 5, 445 510, 528–529 `plusMonths()` method, 448 limitations, 261, 301, 515, 525 polymorphism, 79, 102–103, 107, methods executed on, 218, 232, 239, 141–142, 456, 464, 470, 471, 480 317, 501, 506, 508, 530 post-decrement operator (--), 3, 310, 444, obtaining equivalent of existing, 214, 453, 528 217, 229, 252, 256, 499, 501, 505, pound (#), 133, 478 512, 514 Practice Exam 1 reduction, 395, 549 answers and explanations, 541–548 working with collections, 236, 256, questions, 365–390 310–311, 507, 514, 528 Practice Exam 2 parentheses, 17, 150, 212, 448, 451, answers and explanations, 548–554 482, 498 questions, 391–415 `parse()` method, 355, 538 Practice Exam 3 `parseLong()` method, 12, 447 answers and explanations, 554–560 pass-by-value, 103–104, 405–406, questions, 417–442 471, 551 `Predicate` interface, 241, 254, 258, 259, `Path` objects 509, 513, 515 creating, 381, 546 `PreparedStatement` class, 340, 341–342, immutable instances, 534, 558 348, 351, 536, 537 normalizing, 329, 432, 532, 558 pre/post-increment operator (++), 299–300, reading, 327, 330–331, 332, 333–334, 310, 418, 525, 528, 554 335–336, 532, 533, 534 primitive types, 221, 383–384, 419, 449, traversing, 330, 331, 408–409, 533, 502, 546, 555. See also specific types 534, 552 `print()`, 95–96, 469 writing, 321–322, 334–335, 337, `println()` method, 17, 35, 448, 453 530–531, 534 `private` interface method, 75–76, 147, `Path.of()` method, 320, 323, 530, 531 463, 481 `Paths.get()` method, 320, 530 `private` modifier pattern matching, 450, 452 abstract methods, 47, 455, 458, 462 `peek()` method, 183, 225, 230–231, cannot be overridden, 386–387, 547 246, 389, 409, 490, 504–506, 510, compared to `protected`, 86–87, 466 547, 552 enum constructors, 98–99, 469 `perform()` method, 31–32, 452 functional interfaces, 467 period (.), 270, 292, 332, 517, 523, 534 in immutable classes, 67, 461 implementation, 64, 73–74, 320, 460, Q 463, 530 incorrect usage, 71, 462 `q()` method, 36–37, 453 inheritance, 465 queries, 347, 424–425, 537, 556 instance-based methods, 117–118, 474 question mark (?), 182, 199, 201, 401, 489, interfaces, 110, 472 495, 550 limitations, 48, 114, 455, 459, 473 queues, 183, 196–197, 206, 409, 490, 494, methods within an interface, 89, 100, 497, 552 467, 470 static methods, 130, 477 R using, 101, 470 `private static` modifier, 86, 459, `r()` method, 36–37, 453 466, 467 race conditions, 309, 421, 525, 527, `ProblemException`, 157–158, 484 555 `protected` modifier `r.apply()` method, 458 `abstract` methods, 455 `read()` method, 332, 534 calling methods with, 112, 132–133, `readAllLines()` method, 333, 534 388, 473, 478, 547 `Reader` class, 327, 332, 532, 534 compared to package, 132, 478 records compared to `private`, 86–87, 466 declaring, 46, 455, 460 defined, 461 defining as local types, 378, 545 implied visibility, 441, 560 `equals()`, 401, 550 limitations, 458 including overrides in, 105, 471 subclass access, 459 limitations, 467 using, 48, 99, 455, 468, 470 unable to extend, 457 `provides` directives, 280, 283, 519, `reduce()` method, 223–224, 503 520, 521 reference types. See specific types `provides` statement, 275, 519 reference types, implementing, 81, 465 `public` modifier `registerOutParameter()` method, abstract interface methods, 458 345, 537 `abstract` methods, 455 relational operator (&lt; or &lt;= or >=), 8, 431, immutable objects, 67, 461 446, 558 implicit application of, 62, 77, `relativize()` method, 332, 534 459, 464 `remove()` method, 208, 497 interface methods, 110, 472 `RemoveMe`, 383–384, 546 limitations, 398, 550 `requires` directive, 283, 290–291, unsafe implementation, 70–71, 462 371, 375, 406, 519, 521, 523, using, 48, 64, 79, 83, 437, 455, 460, 543–544, 552 464, 466, 559 `requires mandated java.base` output, `push()` method, 189, 491 293, 524 `requires` statement, 274, 275, 276, serial streams 518, 519 grouping, 227–228, 504 `requires transitive` directive, 519 infinite sources, 225, 504 resource bundles, 354, 359, 361–363, limitations, 261, 515 538, 540 methods executed on, 218, 501 resource leaks, 331, 533 ordered, 250, 512 resource starvation, 301, 525 reduction, 317, 530 `ResultSet` interface, 393, 548 `Serializable`, 73–74, 145, 322, 329, `return` statement, 40, 398–399, 454, 550 331–332, 463, 481, 530, 533 `reverse()` method, 3, 194, 432–433, 444, serialization, 322, 326, 335–336, 438, 530, 493, 558 531–532, 534, 559 `round()` method, 23, 386, 450, 547 service locators `run()` method, 60, 459 combining with service provider `Runnable` expression, 298, 306, 419, 525, interface, 270, 517 527, 555 creating, 283–284, 521 `Runnable` functional interface, 415, defined, 268, 516 554 directives, 289, 371, 522–523, 543 `RuntimeException`, 153–154, 156, methods on, 278, 371, 520, 543 164, 167–169, 174–175, 179, 190, module dependencies, 271, 517 482–483, 485–486, 488–489, 492 `ServiceLoader` call, 281, 520 valid, 418, 554 service provider interface, 270–271, 277, S 282–283, 291, 371, 517, 519, 521, `-s` option, 288, 522 523, 543 `Sand()` method, 60, 459 service providers `sb.length()` method, 448 adding, 288, 522 `ScheduledExecutorService`, 308, 311, calling, 292–293, 524 527, 528 creating, 275, 282–283, 519, 521 `sealed` modifier dependencies, 291–292, 523 classes, 72, 100, 107, 129–130, 462, `ServiceLoader` class, 271–272, 278, 281, 470, 472, 477 289, 292, 418, 428–429, 518, 520, subclasses, 46, 58–59, 84, 455, 458, 466 523, 554, 557 `SELECT` statement, 351, 538 `ServiceLocator` class, 371, 543 `sell()` method, 457 services semicolons (;) calling, 271–272, 289, 518, 523 enums, 132, 423–424, 478, 556 defined, 281, 288, 520, 522 in lambda expressions, 214, 499 parts of, 268, 516 in `List`, 78, 464 `Set` collections optional, 166, 486 creating, 188, 189, 491 use of, 28, 451 immutable, 195, 493 `sendHome()` method, 456 `null` values in, 200, 495 `sequential()` method, 214, 499 sorting elements, 190, 491 `setContents()` method, 101, 470 threads, 302–303, 526 `setNull()` method, 340, 535 using, 80, 101, 465, 470 `setObject()` method, 344–345, 536–537 `static` modifier, 77, 122, 437, 464, 465, `Short` class, 450–451 467, 475, 559 `short` variable, 26, 450–451 `static` variable, 47, 60, 77, 108, 139–140, short-circuit logical operator (&&), 8, 30, 455, 459, 464, 472, 479 431, 446, 452, 558 stored procedures, 345, 537 `shutdown()` method, 314, 529 `Stream` interface signature matching, 457 changing generic types, 246, 510 single-line comments (/ /), 367, 462, 542 creating, 419, 555 `sorted()` method, 243–244, 246, filtering data, 215, 237, 242, 324–325, 510, 511 389–390, 500, 507, 509, 531, 547 `speak()` method, 114, 473 mapping data, 217, 233, 238–239, `Spliterator`, 247, 412–413, 511, 552 243–244, 366, 400, 500, 506, 508, SQL statements, 341, 349, 351, 536, 510, 541, 550 537, 538 sorting data, 242, 246, 509, 511 `SQLException`, 372–373, 543 source operation, 229, 505 stack traces, 153–154, 168, 241, 482, supplying data, 230, 236, 237, 247, 413, 486, 509 505, 507, 511, 553 `StackOverflowError`, 488 working with data, 231, 259, statements 506, 515 callable, 344–345, 346, 536–537 stream pipelines order of, 458 avoiding multiple trips through, prepared, 343, 344, 536 239–240, 508 `static` classes, 59–60, 64–65, 108, 140, creating, 212–213, 242, 262, 420, 499, 459, 460, 472, 480 509, 516, 555 `static` imports, 106, 147–148, 471, 481 intermediate operations, 222, 240, 257, `static` initializers, 80, 107–108, 115–116, 503, 509, 514 409–410, 455–456, 465, 472, lazy evaluation, 245, 249, 510, 512 474, 552 methods, 263, 516 `static` methods multiple, 393–394, 548 hiding, 460 order of operations, 212, 498 immutable classes, 449 terminal operations, 218, 406, 433, 442, interface methods, 89, 100, 110, 119, 501, 552, 558, 560 381, 467, 470, 472, 474, 546 streams. See also parallel streams; keywords in, 54, 457 serial streams limitations, 57, 60, 138, 458, 459, concatenation, 214, 265, 499, 516 462, 479 decomposition, 261, 515 marking `private`, 130, 477 empty, 235, 507 in nested classes, 411–412, 552 grouping, 235, 250–251, 507, 512 overriding, 99, 470 infinite, 237, 507 lambda expressions in, 202, 234, `substring()` method, 13, 447 496, 506 `sum`, 23, 450 limitations, 219, 240, 502, 508 `sum`() method, 218, 501 mapping data, 229, 366, 393–394, `Super` class, 139, 479 434–436, 439–440, 505, 541, 548, `super .getWeight()` method, 79, 464 559, 560 `super` keyword, 54, 71, 73, 106, 457, ordered, 378, 545 462–463, 471 partitioning, 235, 238, 250–251, 507, `super()` method, 95, 111, 141, 464, 468, 508, 512 473, 480 primitive, 226, 442, 504, 560 superclass constructors, 157–158, 484 reduction, 216, 220–221, 224, 234, superclasses, 88, 467 238, 242, 246, 256–257, 395, 500, `Supplier` functional interface, 225–226, 502–503, 507–510, 514, 549 254, 263–264, 504, 513, 516 sorting data, 225, 243–244, 247, 413, `Supplier<Double>`, 215, 499–500 439–440, 504, 510, 511, 533, 560 `switch` expressions supplying data, 257, 514 compared to `switch` statements, 39, valid, 222, 228, 503, 504 398–399, 452, 454, 550 `String` class compiling, 28, 382, 451, 546 in arrays, 199–200, 209, 224–225, 495, `default` statements in, 26, 28, 451 497, 504 variables in, 89–90, 467 comparing with method, 414, 554 `switch` statements errors, 41, 454 braces, 454 immutable, 6, 13, 445, 447, 449 `case` keyword, 452 modifying data in, 405–406, 551 compared to `switch` expressions, 39, supported in `switch` statements, 398–399, 452, 454, 550 26, 450–451 `default` statements in, 28, 418, string concatenation (""+), 140, 480 451, 554 `String` data type, 7, 73, 153, 446–447, enum types, 457 463, 482 fall through, 39–40, 454 `StringBuilder` class, 3–4, 22, 444, limitations, 129–130, 418, 477, 554 446, 450 supported types, 26, 450–451, 453 `StringBuilder` data type, 7, 446 `var` type in, 26, 451, 453 `StringIndexOutOfBounds ______________ synchronized` method, 307–309, 527 `Exception`, 13, 447 `System.console()` method, 328, 532 `strip()` method, 10, 16, 430, 447, 448, 557 `stripIndent()` method, 10, 447 T `stripTrailing()` method, 10, 447 `stroke()` method, 57, 458 tables, 343–344, 346, 350–351, 372–373, subclasses 536–537, 543 non-sealed, 58–59, 458 `teams` variable, 449 `permits` clause, 147, 481 `teeing()` collector, 427, 557 sealed, 46, 58–59, 84, 455, 458, 466 ternary expressions, 3, 418, 444, 448, 554 ternary operator (?:), 401–402, 418, top-down migration, 274, 289, 411, 518, 550, 554 523, 552 text, escaping, 364, 436–437, 541, 559 `toString()` method, 105, 426–427, 457, `think()` method, 150–151, 482 471, 556 `this()` constructors, 95, 111, 117, `transform()` declaration, 137–138, 479 145–146, 464, 468, 473, 474, 481 `transformation()` method, 407, 552 `this` keyword, 54, 71, 73–74, 457, `transitive` directive, 290, 523 462, 463 `travel()` method, 42, 454 `Thread.run()` method, 296, 524 `trim()` method, 16, 447 threads triple quotes ("""), 2, 4, 8, 13, 19, 22, 430, concurrency, 308–310, 312, 314–315, 444–446, 447, 449–450, 557 404, 527–529, 551 try/catch/finally, 151–154, 156–157, cyclic barriers, 302–303, 311–312, 161, 163–168, 170, 172–175, 376–377, 526, 528, 544 178–179, 241, 321, 413–414, 423, multithreaded programs, 315, 529 428, 482–483, 485–489, 509, 530, order of operations, 303–304, 526 553, 556–557 race conditions, 309, 421, 525, `tryLock()` method, 298–299, 379–380, 527, 555 525, 545 stop running tasks, 314, 529 try-with-resources, 150, 152–154, 156, worker, 296–297, 302–303, 305–306, 162, 166, 177, 321, 326, 333–334, 307, 524, 526, 527 367, 377, 388–389, 438, 482–483, thread-safe code, 297–300, 304–305, 485–486, 488, 530–532, 534, 542, 307–310, 312, 314–317, 395, 404, 545, 547, 559 524–530, 549, 551 `turnOn()` method, 461 `throw` keyword, 150–151, 482 two-dimensional arrays, 183, 490 `Throwable` case, 154, 156, 165, 173–174, type casting, 141–142, 480 483, 486, 488 type parameters, 191, 492 `throws` keyword, 150, 482 types. See specific types time daylight savings time, 2–4, 6–7, 10–11, U 13, 22, 385, 425, 444–445, 447–448, 450, 547, 556 `UnaryOperator`, 212, 498 formatting, 355, 360–361, 372, 396, unchecked exceptions, 86, 155, 162–163, 538–540, 543, 549 171, 172, 190, 466, 483, 485, time zones, 2, 16, 396, 425, 444, 450, 487, 492 549, 556 underscore (\_), 11, 102, 133, 163–164, `TimeException`, 170, 487 369, 384, 414, 437, 444, 447, 470, `ToDoubleBiFunction` interface, 478, 485, 542, 547, 554, 559 217, 500–501 `UnsupportedOperationException`, `ToIntBiFunction` interface, 253, 513 169, 486 `ToIntFunction` interface, 215, 500 `UnsupportedTemporalTypeException`, `ToLongBiFunction` interface, 5–6, 445 371–372, 543 `uses` directive, 277, 519 scope, 77, 95–96, 111, 130–131, 418, V 428, 434, 464, 469, 473, 477, 554, `valueOf()` method, 12, 447 557, 558 `var` type static, 60, 459 inserting without a value, 74, 463 synchronized access, 303–304, 526 limitations, 140, 414, 480, 554 virtual method invocation, 58–59, 458 in `switch` statements, 26, 451, 453 `void` type, 127, 143, 227, 477, 480, 504 as variable name, 82–83, 465 `volatile` attribute, 316–317, 530 variables with, 468 varargs parameter, 84, 187, 466, 491 variable type inference, 422–423, W 467, 556 `walk()` method, 330–331, 533 variables `while` loops, 29, 36, 40, 373–374, 398, accessing, 437, 456, 559 451, 453–454, 543, 550 bind, 341–342, 536 whitespace, 4, 22, 378, 430, 445, 447–448, declaring multiple, 61, 459 450, 545, 557 effectively final, 53, 457 wildcards, 182, 190, 199, 201, 203, 210, encapsulation, 458 401, 489, 492, 495, 496, 498, 550 initializing, 384, 403–404, 547, 551 `woof()` method, 457 instance, 60, 455, 459 wrapper classes, 1, 12, 17, 26, 447, within interfaces, 123, 475 449, 450–451 lambda expressions, 220–221, 250, 467, `Writer` class, 321–322, 530–531 502, 512 local, 145, 481 naming, 2, 77, 155, 384, 414, 444, 464, Z 483, 547, 554 nested classes, 411–412, 552 `ZonedDateTime` class, 6–7, 8, 445, replacing in a subclass, 425–426, 556 446, 449 Online Test Bank To help you study for your OCP Java SE 17 Developer certifification exam, register to gain one year of FREE access after activation to the online interactive test bank—included with your purchase of this book! All of the practice questions in this book are included in the online test bank so you can study in a timed and graded setting. Register and Access the Online Test Bank To register your book and get access to the online test bank, follow these steps: 1. Go to `www.wiley.com/go/sybextestprep`. You’ll see the “How to Register Your Book for Online Access” instructions. 2. Click “here to register” and then select your book from the list. 3. Complete the required registration information, including answering the security verifification to prove book ownership. You will be emailed a pin code. 4. Follow the directions in the email or go to `www.wiley.com/go/sybextestprep`. 5. Find your book on that page and click the “Register or Login” link with it. Then enter the pin code you received and click the “Activate PIN” button. 6. On the Create an Account or Login page, enter your username and password, and click Login or, if you don’t have an account already, create a new account. 7. At this point, you should be in the test bank site with your new test bank listed at the top of the page. If you do not see it there, please refresh the page or log out and log back in. WILEY END USER LICENSE AGREEMENT Go to www.wiley.com/go/eula to access Wiley’s ebook EULA.
