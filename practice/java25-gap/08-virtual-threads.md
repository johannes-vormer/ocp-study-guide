---
title: "Gap: Virtual threads"
source: java25-gap
topic: d08-concurrency
---

# Gap: Virtual threads and thread builders (🆕 Java 21, JEP 444)

## gap-vthread-001

What is the output of the following code fragment?

```java
Thread t = Thread.ofVirtual().unstarted(() -> {});
System.out.println(t.isVirtual() + " " + t.isDaemon() + " "
                   + t.getState() + " [" + t.getName() + "]");
```

- **A.** `true false NEW [Thread-0]`
- **B.** `true true NEW []`
- **C.** `true true RUNNABLE [VirtualThread-0]`
- **D.** `true false NEW []`
- **E.** `false true NEW [Thread-0]`

**Answer:** B

**Explanation:** 🆕 Java 21: `Thread.ofVirtual()` returns a `Thread.Builder.OfVirtual`; `unstarted(Runnable)` creates a thread without starting it, so its state is `NEW` (`start(Runnable)` would create and start it).

- `isVirtual()` is `true`.
- Virtual threads are **always daemon threads**, so `isDaemon()` is `true`.
- Unless a name is set on the builder, a virtual thread has **no name**: `getName()` returns the empty string `""`, not `Thread-N`.

## gap-vthread-002

What is the result of the following code fragment?

```java
Thread t = Thread.ofVirtual().unstarted(() -> {});
t.setPriority(Thread.MAX_PRIORITY);
System.out.print(t.getPriority() + " ");
t.setDaemon(false);
System.out.print(t.isDaemon());
```

- **A.** It prints `10 false`.
- **B.** It prints `5 false`.
- **C.** It prints `10 ` and then throws an `IllegalArgumentException`.
- **D.** It prints `5 ` and then throws an `IllegalArgumentException`.
- **E.** It prints `5 ` and then throws an `IllegalThreadStateException`.

**Answer:** D

**Explanation:** The priority of a virtual thread is always `Thread.NORM_PRIORITY` (5). `setPriority()` on a virtual thread is silently **ignored** — no exception — so `getPriority()` returns 5.

Virtual threads are always daemon threads. `setDaemon(true)` is allowed (it changes nothing), but `setDaemon(false)` throws **`IllegalArgumentException`**. Option E names the exception that `setDaemon` throws when called on an already started thread, which is not the case here.

## gap-vthread-003

What is the output of the following code fragment? (The enclosing method declares `throws InterruptedException`.)

```java
Thread.Builder b = Thread.ofVirtual().name("worker-", 1);
Thread t1 = b.unstarted(() -> {});
Thread t2 = b.start(() -> {});
Thread t3 = b.unstarted(() -> {});
t2.join();
System.out.println(t1.getName() + " " + t2.getName() + " " + t3.getName());
```

- **A.** `worker-1 worker-1 worker-2`
- **B.** `worker-0 worker-1 worker-2`
- **C.** `worker- worker- worker-`
- **D.** `worker-1 worker-2 worker-3`
- **E.** The code does not compile.

**Answer:** D

**Explanation:** `Thread.Builder.name(String prefix, long start)` names threads `prefix + counter`, starting at `start`. The counter is incremented **every time the builder creates a thread** — with `unstarted()`, `start()` or via its `factory()` — not only when a thread is started. So the names are `worker-1`, `worker-2`, `worker-3`.

With `name(String)` (one argument) every thread created by the builder gets the same name. `Thread.Builder` is the common superinterface of `OfVirtual` and `OfPlatform`, so assigning to `Thread.Builder` compiles.

## gap-vthread-004

Assume `Runnable r = () -> {};` is in scope. Which statements compile? (Choose three.)

- **A.** `Thread.ofVirtual().daemon(true).start(r);`
- **B.** `Thread.ofPlatform().daemon().priority(7).name("x").start(r);`
- **C.** `Thread.ofVirtual().priority(5).start(r);`
- **D.** `Thread t = new Thread.ofVirtual().start(r);`
- **E.** `ThreadFactory f = Thread.ofVirtual().name("v-", 0).factory();`
- **F.** `Thread t = Thread.startVirtualThread(r);`

**Answer:** B, E, F

**Explanation:** `Thread.Builder.OfPlatform` has methods for platform-only settings such as `daemon()`, `daemon(boolean)`, `priority(int)` and `stackSize(long)`. `Thread.Builder.OfVirtual` does **not** have `daemon` or `priority` methods, because virtual threads are always daemon and always have normal priority — so A and C do not compile.

D does not compile: `ofVirtual()` is a static method, not a nested class, so `new` cannot be used. E compiles: every builder can produce a `ThreadFactory` (`java.util.concurrent.ThreadFactory`). F compiles: `Thread.startVirtualThread(Runnable)` creates and starts a virtual thread and returns it.

## gap-vthread-005

What is the result of the following code fragment? (The enclosing method declares `throws InterruptedException`.)

```java
Thread t = Thread.startVirtualThread(() -> System.out.print("run "));
t.join();
System.out.print(t.getState() + " ");
t.start();
```

- **A.** It prints `run TERMINATED ` and then throws an `IllegalThreadStateException`.
- **B.** It prints `run TERMINATED run `.
- **C.** It prints `TERMINATED run `.
- **D.** It prints `run NEW run `.
- **E.** It prints `run TERMINATED ` and then completes normally, because starting a virtual thread twice is ignored.

**Answer:** A

**Explanation:** `Thread.startVirtualThread()` creates **and starts** the virtual thread. After `join()` returns, the thread has finished, so the state is `TERMINATED` and `run ` has already been printed.

A thread — platform or virtual — can be started only once. Calling `start()` again throws `IllegalThreadStateException`.

## gap-vthread-006

What is the result of the following code fragment? (Assume `java.util.concurrent.*` and `java.util.concurrent.atomic.*` are imported.)

```java
var count = new AtomicInteger();
try (ExecutorService es = Executors.newVirtualThreadPerTaskExecutor()) {
    for (int i = 0; i < 1000; i++)
        es.submit(() -> count.incrementAndGet());
}
System.out.println(count.get());
```

- **A.** It prints any value from 0 to 1000.
- **B.** It always prints `0`.
- **C.** The code does not compile because `ExecutorService` cannot be used in a try-with-resources statement.
- **D.** It throws a `RejectedExecutionException`.
- **E.** It always prints `1000`.

**Answer:** E

**Explanation:** 🆕 Java 19: `ExecutorService` extends `AutoCloseable`. Its default `close()` method calls `shutdown()` and then **waits until all submitted tasks have completed**. So when the try-with-resources block ends, all 1000 tasks have run and the output is always `1000`. (On Java 17, option C would have been correct.)

`Executors.newVirtualThreadPerTaskExecutor()` (🆕 Java 21) starts a **new virtual thread for every task** — there is no pool. The lambda returns a value, so it is a `Callable<Integer>`; this is fine for `submit`.

## gap-vthread-007

What is the output of the following code fragment? (Assume `java.util.concurrent.*` is imported; the enclosing method declares `throws Exception`.)

```java
ExecutorService es = Executors.newVirtualThreadPerTaskExecutor();
Future<Boolean> f = es.submit(() -> Thread.currentThread().isVirtual());
Future<String> g = es.submit(() -> "[" + Thread.currentThread().getName() + "]");
System.out.println(f.get() + " " + g.get());
es.close();
System.out.println(es.isShutdown() + " " + es.isTerminated());
```

- **A.** `true []` followed by `true true`
- **B.** `true [pool-1-thread-2]` followed by `true true`
- **C.** `true []` followed by `true false`
- **D.** `false []` followed by `true true`
- **E.** `true [VirtualThread-1]` followed by `false false`

**Answer:** A

**Explanation:** Every task of a `newVirtualThreadPerTaskExecutor()` runs in its own virtual thread, so `isVirtual()` is `true`. The threads are created by an unnamed virtual-thread factory, so their names are empty: `[]`.

`close()` shuts the executor down and waits for all tasks to finish, so afterwards both `isShutdown()` and `isTerminated()` return `true`. Any later `submit()` would throw `RejectedExecutionException`.

## gap-vthread-008

What is the output of the following code fragment? (Assume `java.util.concurrent.*` is imported.)

```java
try (var es = Executors.newFixedThreadPool(2)) {
    es.submit(() -> { Thread.sleep(200); System.out.print("A"); return null; });
    es.execute(() -> System.out.print("B"));
} finally {
    System.out.print("C");
}
System.out.println("D");
```

- **A.** `BCDA`
- **B.** `CDBA`
- **C.** `BACD`
- **D.** `ABCD`
- **E.** The code does not compile.

**Answer:** C

**Explanation:** Since Java 19 every `ExecutorService` is `AutoCloseable`, not just the virtual-thread executor. Closing it waits for submitted tasks to finish.

The first task is a `Callable` (it returns `null`, which is why `Thread.sleep` — a checked exception — is allowed without a try/catch). The second task prints `B` immediately on the second pool thread. After 200 ms the first task prints `A`. Only then does the implicit `close()` return, after which the `finally` block prints `C` and the next statement prints `D`: `BACD`.

Option A would be the answer if `close()` did not wait.

## gap-vthread-009

What is the output of the following code fragment, when run from a standard `main` method?

```java
Thread p1 = Thread.ofPlatform().unstarted(() -> {});
Thread p2 = Thread.ofPlatform().daemon().priority(7).unstarted(() -> {});
Thread v  = Thread.ofVirtual().unstarted(() -> {});
System.out.println(p1.isDaemon() + " " + p2.isDaemon() + " " + p2.getPriority()
                   + " " + v.isDaemon() + " " + v.getPriority());
```

- **A.** `true true 7 true 5`
- **B.** `false true 7 true 5`
- **C.** `false true 7 false 5`
- **D.** `false true 5 true 7`
- **E.** `false false 7 true 5`

**Answer:** B

**Explanation:** A platform thread created by a builder **inherits** daemon status from the creating thread unless `daemon(...)` is called. The `main` thread is not a daemon, so `p1.isDaemon()` is `false`. `daemon()` (no argument) is equivalent to `daemon(true)`, so `p2` is a daemon with priority 7.

Virtual threads are always daemon threads with priority `NORM_PRIORITY` (5).

## gap-vthread-010

What is the output of the following code fragment? (Assume `java.time.*` is imported; the enclosing method declares `throws InterruptedException`.)

```java
Thread t = Thread.ofVirtual().start(() -> {
    try { Thread.sleep(Duration.ofSeconds(2)); }
    catch (InterruptedException e) { System.out.print("interrupted "); }
});
boolean done = t.join(Duration.ofMillis(100));
System.out.print(done + " ");
t.interrupt();
System.out.println(t.join(Duration.ofSeconds(5)));
```

- **A.** `true interrupted true`
- **B.** `false interrupted false`
- **C.** `false true`
- **D.** `false interrupted true`
- **E.** The code does not compile because `join()` returns `void`.

**Answer:** D

**Explanation:** 🆕 Java 19 added `Thread.sleep(Duration)` and `Thread.join(Duration)`. Unlike the old `join(long millis)`, which returns `void`, **`join(Duration)` returns a `boolean`**: `true` if the thread terminated within the time, `false` if the wait timed out.

After 100 ms the virtual thread is still sleeping, so `done` is `false`. Interrupting a sleeping virtual thread works like it does for a platform thread: `sleep` throws `InterruptedException`, the catch block prints `interrupted `, and the thread ends. The second `join` therefore returns `true`.

## gap-vthread-011

What is the output of the following code fragment? (Assume `java.util.concurrent.*` is imported; the enclosing method declares `throws Exception`.)

```java
ThreadFactory tf = Thread.ofVirtual().name("vt-", 0).factory();
try (ExecutorService es = Executors.newThreadPerTaskExecutor(tf)) {
    Future<String> f = es.submit(() -> Thread.currentThread().getName());
    System.out.println(f.get());
}
```

- **A.** `vt-1`
- **B.** `pool-1-thread-1`
- **C.** An empty line
- **D.** The code does not compile.
- **E.** `vt-0`

**Answer:** E

**Explanation:** `Thread.Builder.factory()` returns a `ThreadFactory` that creates threads with the builder's settings. `Executors.newThreadPerTaskExecutor(ThreadFactory)` (🆕 Java 21) creates one new thread per task using that factory. The first thread created gets the counter's start value, so the name is `vt-0`.

`newVirtualThreadPerTaskExecutor()` is simply the same executor with an unnamed virtual-thread factory, which is why its threads have empty names.

## gap-vthread-012

Which statements about virtual threads in Java 25 are true? (Choose two.)

- **A.** A virtual thread is always a daemon thread.
- **B.** A virtual thread created with `Thread.ofVirtual().unstarted(r)` is named `VirtualThread-0`.
- **C.** `Thread.currentThread().isVirtual()` returns `false` when called directly in a standard `main` method.
- **D.** `setPriority(Thread.MAX_PRIORITY)` on a virtual thread raises its priority to 10.
- **E.** `Executors.newVirtualThreadPerTaskExecutor()` reuses a fixed pool of virtual threads.
- **F.** `new Thread(r)` creates a virtual thread when the program runs on Java 21 or later.

**Answer:** A, C

**Explanation:** A is true: virtual threads are always daemons, and `setDaemon(false)` throws `IllegalArgumentException`. C is true: the launcher runs `main` on a platform thread.

B is false: virtual threads have an empty name unless one is set. D is false: the priority of a virtual thread is fixed at 5; `setPriority` is ignored. E is false: virtual threads are cheap and are **not pooled**; that executor creates a new virtual thread per task. F is false: the `Thread` constructors always create platform threads; virtual threads are created only via `Thread.ofVirtual()`, `Thread.startVirtualThread()` or a virtual-thread factory/executor.
