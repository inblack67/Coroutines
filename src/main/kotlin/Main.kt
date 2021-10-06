import kotlinx.coroutines.*
import kotlin.math.floor

fun main() = runBlocking {
    val job = launch {
        repeat(100) { it ->
            println("coroutine is sleeping in $it ms")
            delay(500L)
        }
    }
    delay(1500L)
    println("main => waiting since forever")
//    job.cancel()
//    job.join()
    job.cancelAndJoin()
    println("main => I can quit now")
}