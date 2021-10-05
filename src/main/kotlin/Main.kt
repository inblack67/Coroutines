import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.math.floor

fun main() = runBlocking {
    greetFromCoroutine()
    repeat(100) {
//        launch 100 coroutines (100_000 => 100k)
        launch {
            delay(5000L)
            println(floor(Math.random() * 100))
        }
    }
}

suspend fun greetFromCoroutine() = coroutineScope {
    launch {
        delay(1000L)
        println("coroutine => hello")
    }
    val job = launch {
        delay(2000L)
        println("coroutine 2 => hello")
    }
    job.join() // wait until child coroutine completes
    println("coroutine 2 done")
    println("I will be first")
}

//In addition to the coroutine scope provided by different builders, it is possible to declare your own scope using the coroutineScope builder. It creates a coroutine scope and does not complete until all launched children complete.
//runBlocking and coroutineScope builders may look similar because they both wait for their body and all its children to complete. The main difference is that the runBlocking method blocks the current thread for waiting, while coroutineScope just suspends, releasing the underlying thread for other usages. Because of that difference, runBlocking is a regular function and coroutineScope is a suspending function.

