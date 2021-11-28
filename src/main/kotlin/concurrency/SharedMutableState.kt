package concurrency

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.measureTimeMillis

suspend fun massiveRun(action: suspend () -> Unit) {
    val numberOfCoroutines = 100
    val times = 1000
    val time = measureTimeMillis {
       coroutineScope {
           repeat(numberOfCoroutines) {
               launch {
                   repeat(times){
                       action()
                   }
               }
           }
       }
    }
    println("massiveRun took $time")
}

//@Volatile // This code works slower, but we still don't get "Counter = 100000" at the end, because volatile variables guarantee linearizable (this is a technical term for "atomic") reads and writes to the corresponding variable, but do not provide atomicity of larger actions (increment in our case).
//var counter = 0


// The general solution that works both for threads and for coroutines is to use a thread-safe (aka synchronized, linearizable, or atomic) data structure that provides all the necessary synchronization for the corresponding operations that needs to be performed on a shared state. In the case of a simple counter we can use AtomicInteger class which has atomic incrementAndGet operations:
// This is the fastest solution for this particular problem. It works for plain counters, collections, queues and other standard data structures and basic operations on them. However, it does not easily scale to complex state or to complex operations that do not have ready-to-use thread-safe implementations.
//val counter = AtomicInteger()

// Thread confinement fine-grained
// Thread confinement is an approach to the problem of shared mutable state where all access to the particular shared state is confined to a single thread. It is typically used in UI applications, where all UI state is confined to the single event-dispatch/application thread. It is easy to apply with coroutines by using a
// This code works very slowly, because it does fine-grained thread-confinement. Each individual increment switches from multi-threaded Dispatchers.Default context to the single-threaded context using withContext(counterContext) block.
val counterContext = newSingleThreadContext("CounterContext")
var counter = 0

// Thread confinement coarse-grained
// In practice, thread confinement is performed in large chunks, e.g. big pieces of state-updating business logic are confined to the single thread. The following example does it like that, running each coroutine in the single-threaded context to start with.
// This now works much faster and produces correct result.

// Mutual Exclusion
// solution to the problem is to protect all modifications of the shared state with a critical section that is never executed concurrently. In a blocking world you'd typically use synchronized or ReentrantLock for that. Coroutine's alternative is called Mutex. It has lock and unlock functions to delimit a critical section. The key difference is that Mutex.lock() is a suspending function. It does not block a thread.
//There is also withLock extension function that conveniently represents mutex.lock(); try { ... } finally { mutex.unlock() } pattern:

val mutex = Mutex()

fun main() =
    runBlocking {

//        mutex
        withContext(Dispatchers.Default){
            massiveRun {
               mutex.withLock {
                   counter++
               }
            }
        }

//        // confine everything to a single-threaded context
//        withContext(counterContext) {
//            massiveRun {
//                counter++
//            }
//        }


//        withContext(Dispatchers.Default){
//            massiveRun {
//                withContext(counterContext){
//                    counter++
//                }
////                counter.incrementAndGet()
////                counter++
//            }
//        }
//        What does it print at the end? It is highly unlikely to ever print "Counter = 100000", because a hundred coroutines increment the counter concurrently from multiple threads without any synchronization.
        println("resulting counter => $counter")
    }