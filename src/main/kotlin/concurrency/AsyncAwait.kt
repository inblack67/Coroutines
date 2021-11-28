package concurrency

import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

suspend fun getNum1() : Int {
    delay(1000)
    return 1
}

suspend fun getNum2() : Int {
    delay(2000)
    return 2
}

// structured concurrency with async
suspend fun concurrentSum(): Int = coroutineScope {
    val one = async { getNum1() }
    val two = async { getNum2() }
    one.await() + two.await()
}

// if something goes wrong inside the code of the concurrentSum function, and it throws an exception, all the coroutines that were launched in its scope will be cancelled.
suspend fun failedConcurrentSum() = coroutineScope {
    val one = async<Int> {
        try {
            delay(5000)
            getNum1()
        } finally {
            println("one got cancelled")
        }
    }
    val two = async {
        throw ArithmeticException()
    }
}

fun main() = runBlocking {

    try {
        failedConcurrentSum()
    } catch (e: ArithmeticException){
        println(e)
    }

    val time4  = measureTimeMillis {
        println("sum => ${concurrentSum()}")
    }

    println("time4 => $time4")

//    Lazily started async
//    Optionally, async can be made lazy by setting its start parameter to CoroutineStart.LAZY. In this mode it only starts the coroutine when its result is required by await, or if its Job 's start function is invoked. Run the following example:
    val time3 = measureTimeMillis {
        val one = async(start = CoroutineStart.LAZY) { getNum1() }
        val two = async(start = CoroutineStart.LAZY) { getNum2() }
        two.start() // this one is started first
//        one.start()
//        So, here the two coroutines are defined but not executed as in the previous example, but the control is given to the programmer on when exactly to start the execution by calling start. We first start one, then start two, and then await for the individual coroutines to finish.
//        Note that if we just call await in println without first calling start on individual coroutines, this will lead to sequential behavior, since await starts the coroutine execution and waits for its finish, which is not the intended use-case for laziness. The use-case for async(start = CoroutineStart.LAZY) is a replacement for the standard lazy function in cases when computation of the value involves suspending functions.
        println("sum ${one.await() + two.await()}")
    }
    println("time3 => $time3")

    val time = measureTimeMillis {

//        This is twice as fast, because the two coroutines execute concurrently. Note that concurrency with coroutines is always explicit.
        val one = async { getNum1() }
        val two = async { getNum2() }
        val res = one.await() + two.await()
        println("sum => $res")
    }

    println("time spent => $time") // 2041 => concurrent => getNum1 & getNum2 => executed concurrently so bottleneck => max(delay1, delay2)


    val time2 = measureTimeMillis {
//      sequential execution => even in coroutine
        val num1 = getNum1()
        val num2 = getNum2()
        println("sum => ${num1+num2}")
    }

    println("time2 spent $time2") // 3018 => 1000 + 2000 milliseconds of delay => bottleneck => delay1 + delay2
}