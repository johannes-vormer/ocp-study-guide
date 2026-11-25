---
title: "D8 — Concurrency"
topic: d08-concurrency
order: 8
---

# D8 — Concurrency

## What the exam tests

- Creating threads: `Thread`, `Runnable`, `Callable`, and 🆕 virtual threads and `Thread.Builder`.
- The thread life cycle (`Thread.State`), and `sleep`, `join`, `interrupt`, daemon threads.
- The Concurrency API: `ExecutorService`, `submit`/`execute`/`invokeAll`/`invokeAny`, shutdown semantics, `Future`, `ScheduledExecutorService`, and 🆕 try-with-resources on an executor.
- Thread safety: `synchronized`, `Lock`/`ReentrantLock`/`ReadWriteLock`, atomic classes, `volatile`, `CyclicBarrier`.
- Concurrent collections versus `ConcurrentModificationException`.
- Liveness problems (deadlock, livelock, starvation) and race conditions.
- Parallel streams: ordering, `reduce` pitfalls, side effects.
- 🆕 Scoped values (Java 25).

Output shown below comes from runs on JDK 25. Where the output depends on timing, the page says so.

---

## 1. Creating threads

```java
Thread t1 = new Thread(() -> System.out.println("Runnable lambda"));
Thread t2 = new Thread() { public void run() { System.out.println("subclass"); } };
t1.start();          // new call stack; run() executes in the new thread
t1.run();            // just a normal method call on the CURRENT thread; nothing starts
```

| | `Runnable` | `Callable<V>` |
|---|---|---|
| Package | `java.lang` | `java.util.concurrent` |
| Method | `void run()` | `V call() throws Exception` |
| Returns a value | no | yes |
| Can throw checked exceptions | **no**: `() -> { Thread.sleep(1); }` as a `Runnable` gives *unreported exception InterruptedException* | **yes** |
| Accepted by `new Thread(...)` | yes | **no** |
| Accepted by `ExecutorService.submit` | yes (`Future<?>`, `get()` returns `null`) | yes (`Future<V>`) |

Rules:

- `start()` may be called **once**. A second call throws `IllegalThreadStateException`, even after the thread has terminated.
- If you subclass `Thread`, override `run()` and also pass a `Runnable`, the **overridden `run()` wins**.
- Default names are `Thread-0`, `Thread-1`, and so on. The default priority is 5 (`Thread.NORM_PRIORITY`). Priorities run from 1 to 10 and are only hints.
- A lambda `() -> { Thread.sleep(10); return null; }` passed to `submit` is a `Callable`, because it returns a value and throws a checked exception.

---

## 2. Thread life cycle

`Thread.State` values: **`NEW`, `RUNNABLE`, `BLOCKED`, `WAITING`, `TIMED_WAITING`, `TERMINATED`**. There is no `RUNNING` or `READY` state.

| State | Typical cause (verified) |
|---|---|
| `NEW` | created, `start()` not called yet (also after calling only `run()`) |
| `RUNNABLE` | started and running, or ready to run |
| `BLOCKED` | waiting to enter a `synchronized` block whose monitor another thread holds |
| `WAITING` | `Object.wait()`, `join()` with no timeout, `LockSupport.park()` |
| `TIMED_WAITING` | `Thread.sleep(ms)`, `wait(ms)`, `join(ms)` |
| `TERMINATED` | `run()` has finished |

### `sleep`, `join`, `interrupt`

- `Thread.sleep(ms)` is **static** and always affects the *current* thread. It throws the checked exception `InterruptedException`. 🆕 Java 19 added `Thread.sleep(Duration)`, `join(Duration)` (returns `boolean`) and `threadId()`.
- `t.join()` makes the current thread wait until `t` terminates.
- `t.interrupt()` sets `t`'s interrupt flag. If `t` is in `sleep`, `wait` or `join`, it wakes up with `InterruptedException` and **the flag is cleared** (inside the `catch`, `isInterrupted()` returns `false`).
- If the flag is already set, the next `sleep` throws immediately.
- `Thread.interrupted()` is **static**: it tests **and clears** the flag of the current thread (`true` then `false`). `t.isInterrupted()` is an instance method that does **not** clear the flag.
- `wait()`, `notify()` and `notifyAll()` must be called while holding that object's monitor. Otherwise they throw `IllegalMonitorStateException`.

### Daemon threads

- The JVM exits when only daemon threads are left. A daemon thread that is sleeping when `main` ends never prints anything.
- `setDaemon(true)` must be called **before** `start()`. Calling it afterwards throws `IllegalThreadStateException`.
- A new thread inherits daemon status from the thread that creates it (`main` is a non-daemon thread).

---

## 3. 🆕 Virtual threads (Java 21, JEP 444)

Virtual threads are lightweight threads managed by the JVM. Many virtual threads are mounted on a few **carrier** (platform) threads, which come from a `ForkJoinPool`. `toString()` shows this: `VirtualThread[#29,v1]/runnable@ForkJoinPool-1-worker-1`.

```java
Thread v  = Thread.ofVirtual().name("v1").start(() -> work());   // builder
Thread v2 = Thread.startVirtualThread(() -> work());             // static shortcut, name ""
Thread u  = Thread.ofVirtual().unstarted(() -> work());          // NEW; call u.start() later

try (ExecutorService es = Executors.newVirtualThreadPerTaskExecutor()) {
    // one NEW virtual thread per task; 10_000 tasks that each sleep 100 ms finish in well under a second
    Future<Integer> f = es.submit(() -> 42);
}
```

Facts (verified):

| Property | Virtual thread |
|---|---|
| `isVirtual()` | `true` |
| `isDaemon()` | **always `true`**. `setDaemon(false)` throws `IllegalArgumentException`; `setDaemon(true)` is allowed. |
| Priority | always `NORM_PRIORITY` (5). `setPriority(1)` is silently ignored. |
| Default name | empty string `""` |
| Created with `new Thread(...)` | **no**. There is no public constructor for virtual threads; use the builder, `startVirtualThread`, or a factory or executor. |
| `Thread.ofVirtual().daemon(...)` / `.priority(...)` | **do not compile**, because `Thread.Builder.OfVirtual` has no such methods |
| `ThreadLocal` | works, but each virtual thread has its own copy |
| `main` thread | a platform thread (`Thread.currentThread().isVirtual()` returns `false`) |

Guidelines:

- **Use them for blocking, I/O-bound tasks.** They give more throughput for many concurrent blocking tasks, not faster CPU-bound code.
- **Don't pool them.** They are cheap, so create one per task (`newVirtualThreadPerTaskExecutor`). Limit concurrency with a `Semaphore`, not a pool size.
- **Pinning:** while a virtual thread runs native code (or, before Java 24, holds a `synchronized` monitor), it cannot unmount from its carrier during a blocking operation. That blocks the carrier. JEP 491 (Java 24) removed pinning for `synchronized`. For the exam, remember that pinning reduces scalability; it does not cause wrong results.

### `Thread.Builder` (🆕 Java 21)

`Thread.Builder` is a sealed interface with two subtypes, `Thread.Builder.OfPlatform` and `Thread.Builder.OfVirtual`.

```java
Thread p = Thread.ofPlatform().name("plat").daemon(true).priority(7).start(task);
Thread.Builder b = Thread.ofVirtual().name("worker-", 0);    // name prefix plus counter
b.start(task).getName();   // worker-0
b.start(task).getName();   // worker-1
ThreadFactory f = Thread.ofVirtual().factory();
ExecutorService es = Executors.newThreadPerTaskExecutor(f);
```

Builder methods: `name(String)`, `name(String prefix, long start)`, `start(Runnable)`, `unstarted(Runnable)`, `factory()`, `inheritInheritableThreadLocals(boolean)`, `uncaughtExceptionHandler(...)`. `OfPlatform` adds `daemon`, `priority`, `group` and `stackSize`.

---

## 4. `ExecutorService`

### Factory methods (`Executors`)

| Method | Pool |
|---|---|
| `newSingleThreadExecutor()` | 1 thread; tasks run sequentially, in submission order |
| `newFixedThreadPool(n)` | n threads, unbounded queue |
| `newCachedThreadPool()` | grows as needed and reuses idle threads |
| `newSingleThreadScheduledExecutor()` / `newScheduledThreadPool(n)` | `ScheduledExecutorService` |
| `newWorkStealingPool()` | `ForkJoinPool` |
| 🆕 `newVirtualThreadPerTaskExecutor()` | new virtual thread per task |
| 🆕 `newThreadPerTaskExecutor(ThreadFactory)` | new thread (from the factory) per task |

### Submitting tasks

| Method | Accepts | Returns |
|---|---|---|
| `execute(Runnable)` (from `Executor`) | `Runnable` only | `void` |
| `submit(Runnable)` | `Runnable` | `Future<?>`, whose `get()` returns `null` |
| `submit(Callable<T>)` | `Callable` | `Future<T>` |
| `invokeAll(Collection<Callable<T>>)` | callables | `List<Future<T>>`; **blocks until all are done** |
| `invokeAny(Collection<Callable<T>>)` | callables | `T`, the result of **one** task that completed successfully; blocks, and the remaining tasks are cancelled |

### Shutting down

| Method | Effect |
|---|---|
| `shutdown()` | Rejects new tasks and lets already submitted tasks finish. **Does not block.** `isShutdown()` becomes `true`; `isTerminated()` becomes `true` only when all tasks are done. |
| `shutdownNow()` | Attempts to stop: **interrupts running tasks** and returns `List<Runnable>` of queued tasks that never ran. |
| `awaitTermination(timeout, unit)` | Blocks until terminated or timed out. Returns `boolean`. Throws `InterruptedException`. |
| `submit`/`execute` after shutdown | `RejectedExecutionException` |
| 🆕 `close()` (Java 19, `ExecutorService extends AutoCloseable`) | Calls `shutdown()` and **waits** for all tasks to finish |

```java
try (ExecutorService es = Executors.newFixedThreadPool(2)) {
    es.submit(() -> { sleep(500); System.out.println("done"); });
}   // close() waits here; "done" is printed before the next line
System.out.println("after");
```

If you forget `shutdown()` or `close()` on a pool of **non-daemon** platform threads, the JVM never exits.

### `Future<V>`

| Method | Notes |
|---|---|
| `get()` | Blocks. Throws `InterruptedException` and `ExecutionException` (task exception **wrapped**; `getCause()` gives the original) and `CancellationException` if cancelled. |
| `get(timeout, unit)` | Also throws `TimeoutException`. **The task keeps running.** |
| `isDone()` | `true` when completed normally, failed or cancelled |
| `cancel(boolean mayInterruptIfRunning)` | Returns `true` if cancelled. After that, `isCancelled()` and `isDone()` are both `true`. |
| 🆕 `state()`, `resultNow()`, `exceptionNow()` (Java 19) | `Future.State` is one of `RUNNING`, `SUCCESS`, `FAILED`, `CANCELLED` |

```java
Future<String> f = es.submit(() -> { throw new IOException("boom"); });
try { f.get(); } catch (ExecutionException e) { System.out.println(e.getCause()); } // java.io.IOException: boom
f.state();   // FAILED
```

### `ScheduledExecutorService`

| Method | Task type | Meaning |
|---|---|---|
| `schedule(Callable<V>, delay, unit)` | `Callable` or `Runnable` | runs once after the delay; returns `ScheduledFuture<V>` |
| `scheduleAtFixedRate(Runnable, initialDelay, period, unit)` | **Runnable only** | starts every `period`, measured **start to start** |
| `scheduleWithFixedDelay(Runnable, initialDelay, delay, unit)` | **Runnable only** | waits `delay` **after each run ends** |

- Passing a `Callable` (for example `() -> 5`) to `scheduleAtFixedRate` **does not compile**.
- In my test the period was 100 ms and each task took 150 ms. At fixed rate, runs started at about 0/150/300/450 ms: they never overlap, and the next run starts as soon as the previous one finishes. With fixed delay, starts were about 250 ms apart (150 ms of work plus 100 ms delay).
- Repeating tasks run until cancelled or until the executor shuts down.

---

## 5. Thread safety

### Race condition demo (verified: 8 threads, 100 000 increments)

```text
plain=98477  volatile=98256  sync=100000  atomic=100000
```

`count++` is read-modify-write and not atomic. **`volatile` fixes visibility, not atomicity.** Only `synchronized`, locks or atomic classes give the correct result. The exact lost-update counts differ from run to run.

### `synchronized`

```java
synchronized void inc() { count++; }                 // locks on this
static synchronized void sInc() { sCount++; }        // locks on MyClass.class
void inc2() { synchronized (this) { count++; } }     // same monitor as inc()
```

- Any object can be the monitor; a primitive cannot.
- A static synchronized method and an instance synchronized method use **different** monitors, so they do not exclude each other.
- Monitors are **reentrant**: a thread that holds the monitor can enter it again.

### `Lock` / `ReentrantLock`

```java
Lock lock = new ReentrantLock();     // new ReentrantLock(true) creates a fair lock; the default is unfair
lock.lock();
try { /* critical section */ } finally { lock.unlock(); }

if (lock.tryLock()) { try { ... } finally { lock.unlock(); } }            // returns immediately
if (lock.tryLock(200, TimeUnit.MILLISECONDS)) { ... }                     // waits; throws InterruptedException
```

- `tryLock()` returns `boolean`. If it returns `false` you **must not** call `unlock()`.
- A lock is reentrant. Locking twice requires unlocking twice (`getHoldCount()` was 2 in my test).
- `unlock()` by a thread that does not hold the lock throws `IllegalMonitorStateException`.
- Other methods: `lockInterruptibly()` and `newCondition()` (`await`/`signal`).

`ReadWriteLock rw = new ReentrantReadWriteLock();` gives `rw.readLock()` and `rw.writeLock()`. Several readers may hold the read lock at once, but a writer is excluded while any read lock is held (verified: a second reader's `tryLock()` returned `true`, the writer's returned `false`).

### Atomic classes (`java.util.concurrent.atomic`)

`AtomicInteger`, `AtomicLong`, `AtomicBoolean` and `AtomicReference<V>` (also `LongAdder` and the array variants).

```java
AtomicInteger ai = new AtomicInteger(5);
ai.getAndIncrement();   // returns 5, value 6
ai.incrementAndGet();   // returns 7
ai.getAndAdd(10);       // returns 7, value 17
ai.addAndGet(1);        // returns 18
ai.compareAndSet(18, 0);// true, value 0
ai.updateAndGet(x -> x + 3); // 3
ai.getAndSet(9);        // returns 3, value 9
ai.accumulateAndGet(2, Math::max); // 9
```

`getAndX` returns the **old** value and `xAndGet` returns the **new** value. There is no `++` operator on atomics. Each individual call is atomic, but a **sequence** of calls is not.

### `CyclicBarrier` and `CountDownLatch`

```java
CyclicBarrier cb = new CyclicBarrier(3, () -> System.out.println("all arrived"));
// each of 3 tasks calls cb.await(); the barrier action runs once, by the last thread to arrive
```

- `await()` throws `InterruptedException` and `BrokenBarrierException`, so it can be used in a `Callable` but not directly in a `Runnable` lambda.
- The barrier **resets** after it trips, so it can be reused ("cyclic").
- If the pool has **fewer threads than parties**, every thread waits at the barrier and the program **hangs**.
- `CountDownLatch(n)`: threads call `countDown()`, and `await()` blocks until the count reaches 0. It cannot be reset.

---

## 6. Concurrent collections

| Use | Concurrent class |
|---|---|
| `Map` | `ConcurrentHashMap`, `ConcurrentSkipListMap` (sorted) |
| `Set` | `ConcurrentSkipListSet` (sorted), `CopyOnWriteArraySet` |
| `List` | `CopyOnWriteArrayList` |
| `Queue` / `Deque` | `ConcurrentLinkedQueue`, `ConcurrentLinkedDeque`, `LinkedBlockingQueue`, `ArrayBlockingQueue`, `LinkedBlockingDeque` |

Verified behaviour:

| Code | Result |
|---|---|
| `ArrayList` / `HashMap`: remove or add inside a for-each over itself | `ConcurrentModificationException` (this happens **with a single thread**) |
| `Collections.synchronizedList(...)`: add while iterating | **still a `ConcurrentModificationException`**; synchronized wrappers do not make iteration safe |
| `ConcurrentHashMap`: remove every key inside a for-each over `keySet()` | fine, `{}` |
| `CopyOnWriteArrayList [1,2,3]`: `add(i*10)` inside a for-each | `[1, 2, 3, 10, 20, 30]`. The iterator is a snapshot, so the loop runs only 3 times. |
| `ConcurrentHashMap.put(null, 1)` or `put("k", null)` | `NullPointerException` (no null keys or values) |

`BlockingQueue` (bounded `LinkedBlockingQueue<>(1)`):

| Action | Full or empty behaviour |
|---|---|
| `offer(e)` / `poll()` | returns `false` / `null` |
| `offer(e, timeout, unit)` / `poll(timeout, unit)` | waits, then returns `false` / `null`; throws `InterruptedException` |
| `put(e)` / `take()` | **blocks** indefinitely |
| `add(e)` | `IllegalStateException` when full |

`BlockingDeque` adds `offerFirst`/`offerLast`, `putFirst`/`putLast`, `takeFirst`/`takeLast` and `pollFirst`/`pollLast` (with timeouts).

---

## 7. Liveness problems

| Problem | Description |
|---|---|
| **Deadlock** | Two or more threads each wait forever for a lock another one holds. Classic case: T1 locks A then B, while T2 locks B then A. In my test both threads were `BLOCKED` and `ThreadMXBean.findDeadlockedThreads()` reported 2. **Fix:** acquire locks in the same order, or use `tryLock` with a timeout. |
| **Starvation** | A thread can never get a resource because other threads keep taking it (for example, unfair locks or long-running greedy threads). |
| **Livelock** | Threads are **active** (not blocked) but keep reacting to each other and make no progress, like two people repeatedly stepping aside in a corridor. It is a special case of starvation. |
| **Race condition** | The result depends on thread timing (lost updates, two users registering the same name). |

---

## 8. Parallel streams

```java
List.of(1,2,3).parallelStream();          // from a Collection
Stream.of(1,2,3).parallel();              // intermediate op; isParallel() == true
s.sequential();                           // the last call wins for the whole pipeline
```

Verified results:

| Code | Result |
|---|---|
| `Stream.of(1..8).parallel().map(String::valueOf).collect(joining())` | `12345678`. Collectors keep **encounter order**. |
| `.parallel().forEach(...)` | **any order** |
| `.parallel().forEachOrdered(...)` | `12345678`, but parallelism is lost for that step |
| `findAny()` on a parallel stream | any element; `findFirst()` respects order |
| `Stream.of(1,2,3).parallel().reduce(10, Integer::sum)` | **36** (sequential: 16). The identity 10 is applied once per chunk, so the identity must be a true identity. |
| `IntStream.rangeClosed(1,8).parallel().reduce(0, (x,y)->x-y)` | varied (sequential: −36). The accumulator must be **associative**. |
| `IntStream.range(0,10000).parallel().forEach(arrayList::add)` | wrong size (5231) or an exception. Use `collect` instead of side effects. |
| `.parallel().collect(Collectors.groupingByConcurrent(...))` | `ConcurrentHashMap` |

The rules for `reduce(identity, accumulator, combiner)` on parallel streams: the identity must be an identity, and the accumulator and combiner must be associative and stateless.

---

## 9. 🆕 Scoped values (Java 25, JEP 506)

A `ScopedValue<T>` passes an **immutable** value to the code called within a bounded scope (including deeply nested calls), without method parameters. It is a lighter alternative to `ThreadLocal`.

```java
static final ScopedValue<String> USER = ScopedValue.newInstance();

USER.isBound();             // false
USER.orElse("guest");       // "guest"
USER.get();                 // NoSuchElementException (not bound)

ScopedValue.where(USER, "alice").run(() -> {
    USER.get();                                          // "alice"
    ScopedValue.where(USER, "bob").run(() -> USER.get()); // "bob" (nested rebinding)
    USER.get();                                          // "alice" again
});
USER.isBound();             // false again after the scope ends

String r = ScopedValue.where(USER, "carol")
                      .where(LEVEL, 3)                 // chain several bindings
                      .call(() -> USER.get() + ":" + LEVEL.get());   // "carol:3"
```

API (Java 25):

| Member | Notes |
|---|---|
| `static <T> ScopedValue<T> newInstance()` | usually stored in a `static final` field |
| `static <T> Carrier where(ScopedValue<T>, T)` | returns a `ScopedValue.Carrier` |
| `Carrier.where(...)`, `Carrier.run(Runnable)`, `Carrier.call(CallableOp<R,X>)`, `Carrier.get(key)` | `call` returns a value and **propagates checked exceptions** of the lambda's type |
| `get()` | value, or `NoSuchElementException` when unbound |
| `isBound()`, `orElse(T other)`, `orElseThrow(Supplier)` | `orElse(null)` throws `NullPointerException` |

Compared with `ThreadLocal`:

| | `ThreadLocal` | `ScopedValue` |
|---|---|---|
| Mutability | `set()` any time | **no `set()`**. Use a nested `where(...).run(...)` to "change" the value for an inner scope. |
| Lifetime | until `remove()` or the thread dies (leak risk in pools) | only during `run`/`call`; unbound automatically afterwards |
| Inheritance | `InheritableThreadLocal` copies values | Not inherited by plain `new Thread(...)` (verified `isBound()` returns `false`). It is inherited by structured-concurrency subtasks, which are preview in 25. |
| Cost with many virtual threads | each thread holds its own map | cheap and shared read-only |

*Structured concurrency (`StructuredTaskScope`, JEP 505) is still **preview** in Java 25 and is not on the exam.*

---

## 🆕 Since Java 17

| Feature | Version |
|---|---|
| `ExecutorService extends AutoCloseable` (`close()` waits for tasks); `Future.state()`/`resultNow()`/`exceptionNow()`; `Thread.sleep(Duration)`, `join(Duration)`, `threadId()` | Java 19 |
| Virtual threads, `Thread.Builder`, `Thread.ofVirtual()/ofPlatform()`, `Thread.startVirtualThread`, `Executors.newVirtualThreadPerTaskExecutor()`/`newThreadPerTaskExecutor()` | **Java 21**, JEP 444 |
| `synchronized` no longer pins virtual threads | Java 24, JEP 491 |
| Scoped values (`ScopedValue`) | **Java 25**, JEP 506 |
| Structured concurrency | preview in 25, not on the exam |

---

## Exam traps

- Calling `run()` instead of `start()` runs the code on the current thread. Calling `start()` twice throws `IllegalThreadStateException`.
- `Thread.sleep` is static: `t.sleep(1000)` sleeps the **current** thread, not `t`.
- `Thread.interrupted()` clears the flag; `isInterrupted()` does not. Catching `InterruptedException` also clears it.
- `wait`/`notify` outside `synchronized` throw `IllegalMonitorStateException`. So does `unlock()` on a lock you do not hold.
- A `Runnable` lambda cannot throw checked exceptions; the same lambda with a `return` becomes a `Callable` when passed to `submit`.
- `execute()` returns `void` and accepts only a `Runnable`. `submit(Runnable).get()` returns `null`.
- Task exceptions surface **only** through `Future.get()`, wrapped in `ExecutionException`.
- `get(timeout)` throwing `TimeoutException` does **not** cancel the task.
- `shutdown()` does not wait and does not stop running tasks. `shutdownNow()` interrupts them. Without either (or `close()`), a platform pool keeps the JVM alive.
- `scheduleAtFixedRate` and `scheduleWithFixedDelay` take only a `Runnable`. Only `schedule` accepts a `Callable`.
- `volatile` does not make `count++` safe.
- Static and instance `synchronized` methods use different locks.
- If `tryLock()` returned `false`, a following `unlock()` throws `IllegalMonitorStateException`.
- `CyclicBarrier(n)` needs at least n threads running at once, otherwise it hangs.
- `Collections.synchronizedList` iteration plus modification still throws `ConcurrentModificationException`. Concurrent collections do not.
- `ConcurrentHashMap` rejects `null` keys and values.
- Parallel `forEach` has no order. `reduce` with a non-identity seed or a non-associative accumulator gives different results in parallel.
- Virtual threads are **always daemon**, always priority 5, and cannot be created with `new Thread()`. `Thread.ofVirtual()` has no `daemon()` or `priority()` methods.
- Do not pool virtual threads. Use `newVirtualThreadPerTaskExecutor()`.
- `ScopedValue` has **no `set`**. `get()` outside a binding throws `NoSuchElementException`. The value disappears after `run`/`call` returns. Nested `where` shadows the value only for the inner scope.

---

## Self-check before practice

- [ ] I can list the six `Thread.State` values and the call that leads to each.
- [ ] I know the differences between `start()` and `run()`, and between `interrupted()` and `isInterrupted()`.
- [ ] I can compare `Runnable` and `Callable` (return value, checked exceptions, where each is accepted).
- [ ] I can create a virtual thread in three ways and state its daemon, priority and name rules.
- [ ] I know what `execute`, `submit`, `invokeAll` and `invokeAny` return.
- [ ] I can explain `shutdown`, `shutdownNow`, `awaitTermination` and `close()` on an `ExecutorService`.
- [ ] I know every exception `Future.get` can throw and what `cancel(true)` does.
- [ ] I can explain fixed rate versus fixed delay, and which `schedule*` methods accept a `Callable`.
- [ ] I know which monitor `synchronized` uses for instance and static methods.
- [ ] I can write a correct `lock`/`tryLock` plus `try`/`finally` pattern.
- [ ] I can tell `getAndIncrement` from `incrementAndGet`.
- [ ] I know when a `CyclicBarrier` deadlocks.
- [ ] I can predict CME versus no CME for `ArrayList`, `synchronizedList`, `ConcurrentHashMap` and `CopyOnWriteArrayList`.
- [ ] I can tell deadlock, livelock, starvation and race condition apart.
- [ ] I can predict parallel `forEach`, `forEachOrdered`, `findAny` and `reduce` results.
- [ ] I can use `ScopedValue` (`newInstance`, `where().run/call`, `get`, `isBound`, `orElse`, nested rebinding) and contrast it with `ThreadLocal`.

**Practice:** `practice/ocp17-829` chapter 8 (*Managing Concurrent Code Execution*), then the `java25-gap` set for virtual threads, `Thread.Builder`, `ExecutorService.close()` and `ScopedValue`.
