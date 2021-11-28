package concurrency
import kotlinx.coroutines.*

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

//Run it to see that it continues to print "I'm sleeping" even after cancellation until the job completes by itself after five iterations.
suspend fun longComputationJob() = coroutineScope {
    val startTime = System.currentTimeMillis()
    val job = launch (Dispatchers.Default) {
        var nextPrintTime = startTime
        var i = 0
        while (i < 5) { // computation loop, just wastes CPU
            // print a message twice a second
            if (System.currentTimeMillis() >= nextPrintTime) {
                println("job: I'm sleeping ${i++} ...")
                nextPrintTime += 500L
            }
        }
    }
    job.cancelAndJoin()
    println("I am done waiting")
}

suspend fun longComputationJob2() = coroutineScope {
    val startTime = System.currentTimeMillis()
    val job = launch (Dispatchers.Default) {
        var nextPrintTime = startTime
        var i = 0
//        isActive => Returns true when the current Job is still active (has not completed and was not cancelled yet).
//        so when job is cancelled isActive becomes false and the loop will break
        while (isActive) { // cancellable computation loop
            if (System.currentTimeMillis() >= nextPrintTime) {
                println("job: I'm sleeping ${i++} ...")
                nextPrintTime += 500L
            }
        }
    }
    delay(1300L) // to give some time for computing
    job.cancelAndJoin() // can't wait no more
    println("I am done waiting")
}

//Cancellable suspending functions throw CancellationException on cancellation which can be handled in the usual way. For example, try {...} finally {...} expression and Kotlin use function execute their finalization actions normally when a coroutine is cancelled:
suspend fun closingResourcesWithFinally() = coroutineScope {
    val job = launch {
        println("${currentCoroutineContext()} launched")
        try {
            repeat(10){
                println("I am delaying you")
                delay(500)
            }
        } finally {
            println("${currentCoroutineContext()} was cancelled, closing resources...")
        }
    }
    delay(10)
    job.cancelAndJoin()
}

suspend fun nonCancellableTask() = coroutineScope {
    val job = launch () {
        println("${currentCoroutineContext()} launched")
        try {
            repeat(10){
                println("I am delaying you")
                delay(500)
            }
        }finally {
            // finally runs when coroutine is cancelled so no suspending functions can be used
            withContext(NonCancellable){
//            following code uses suspending function (delay) and can be used
//            A non-cancelable job that is always active. It is designed for withContext function to prevent cancellation of code blocks that need to be executed without cancellation.
//            WARNING: This object is not designed to be used with launch, async, and other coroutine builders. if you write launch(NonCancellable) { ... } then not only the newly launched job will not be cancelled when the parent is cancelled, the whole parent-child relation between parent and child is severed. The parent will not wait for the child's completion, nor will be cancelled when the child crashed.
                delay(1000)
                println("${currentCoroutineContext()} non cancellable finally block")
            }
        }
    }
    delay(1000)
    job.cancelAndJoin()
}

// throws exception
//The timeout event in withTimeout is asynchronous with respect to the code running in its block and may happen at any time, even right before the return from inside of the timeout block. Keep this in mind if you open or acquire some resource inside the block that needs closing or release outside of the block.
suspend fun longJobWithTimeout() : String {
    return withTimeout<String>(1000){
        repeat(10) {
            println("repeated $it times")
            delay(500)
        }
        "Done"
    }
}

//does not crash but result null as result if timed out
suspend fun longJobWithTimeoutOrNull() : String? {
    return withTimeoutOrNull(1000) {
        repeat(10){
            println("${currentCoroutineContext()} running longJobWithTimeoutOrNull $it times")
            delay(500)
        }
        "done"
    }
}

fun main() = runBlocking {

    println("longJobWithTimeoutOrNull res => ${longJobWithTimeoutOrNull()}")

//    val res = longJobWithTimeout()
//    println("longJobWithTimeout res => $res")

//    nonCancellableTask()
//    closingResourcesWithFinally()
//    longComputationJob2()
//    heavyTask()
}