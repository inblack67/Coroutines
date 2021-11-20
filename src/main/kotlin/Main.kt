import kotlinx.coroutines.*

fun main() = runBlocking {
   heavyTask()
}

suspend fun heavyTask() = coroutineScope {
    var count = 0
    val job = launch {
        repeat(100) {
            println("heavy task ${++count}% done")
            delay(5000)
        }
    }
    delay(1000)
    println("heavy task took too long, cancelling...")
    job.cancel()
    job.join() // wait for job's completion/cancellation
    println("heavy task function done executing")
}

suspend fun launchMany() = coroutineScope {
    var count = 0
    repeat(10) {
        launch {
            println("${currentCoroutineContext()} => count => ${++count}")
        }
    }
}

suspend fun multiTask() = coroutineScope {
    val job1 = launch {
        println("task 1...")
    }
    val job2 = launch {
        println("task 2...")
    }
    job1.join()
    job2.join()
    println("multi task finished") // executed when the above two jobs are completed
}

suspend fun greet(){
    delay(1000)
    println("hello world")
}