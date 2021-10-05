import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main () = runBlocking {
    launch {
        greetFromCoroutine()
    }
    println("main => hello")
}

suspend fun greetFromCoroutine() {
    delay(1000L)
    println("coroutine => hello")
}