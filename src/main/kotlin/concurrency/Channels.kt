package concurrency

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.selects.whileSelect

suspend fun playWithChannels() = coroutineScope {
    val channel = Channel<Int>()
    launch {
        for (x in 1..5) channel.send(x * x)
    }
    repeat(5) {
        println("received => ${channel.receive()}")
        println("hello $it times")
    }
    println("done playing with channels")
}

suspend fun playWithChannels2() = coroutineScope {
    val channel = Channel<Int>()
    launch {
        for (x in 1..5) channel.send(x)
        channel.close()
    }
    for (y in channel) println("received => $y")
    println("done playing with channels")
}

// producer consumer pattern
fun CoroutineScope.produceSquares() : ReceiveChannel<Int> = produce {
    for (x in 1..5) send(x * x)
}

// pipelines
// A pipeline is a pattern where one coroutine is producing, possibly infinite, stream of values:
// And another coroutine or coroutines are consuming that stream, doing some processing, and producing some other results. In the example below, the numbers are just squared:
fun CoroutineScope.produceNumbers() = produce<Int> {
    var x = 1
    while (true) {
        send(x++)
    }
}

fun CoroutineScope.squareNumbers(numbers: ReceiveChannel<Int>) : ReceiveChannel<Int> = produce {
    for (x in numbers) send(x * x)
}

// fan out
// multiple coroutines may receive from same channel, distributing work between themselves
fun CoroutineScope.produceNumbers2() = produce<Int> {
    var x = 1
    while (true) {
        send(x++)
        delay(100)
    }
}

// Then we can have several processor coroutines
fun CoroutineScope.launchProcessor(id: Int, channel: ReceiveChannel<Int>) = launch {
    for (msg in channel) {
        println("id => $id and msg => $msg")
    }
}

// fan in
// Multiple coroutines may send to the same channel
suspend fun sendString(channel: SendChannel<String>, s: String, gap: Long) {
    while (true) {
        delay(gap)
        channel.send(s)
    }
}

// buffered channels
// The channels shown so far had no buffer. Unbuffered channels transfer elements when sender and receiver meet each other (aka rendezvous). If send is invoked first, then it is suspended until receive is invoked, if receive is invoked first, it is suspended until send is invoked.
//Both Channel() factory function and produce builder take an optional capacity parameter to specify buffer size. Buffer allows senders to send multiple elements before suspending, similar to the BlockingQueue with a specified capacity, which blocks when buffer is full.
suspend fun bufferedChannel() = coroutineScope {
    val channel = Channel<Int>(5)
    val job = launch {
        repeat(10) {
            println("sending $it")
            channel.send(it)
//            The first four elements are added to the buffer and the sender suspends when trying to send the fifth one.
            println("sent $it")
        }
    }
    delay(1000)
    job.cancel()
}

fun main() = runBlocking {

    bufferedChannel()

//    val channel = Channel<String>()
////    launching in the main thread
//    launch { sendString(channel, "hello", 100) }
//    launch { sendString(channel, "worlds", 200) }
//    repeat(6){
//        println("received in channel => ${channel.receive()}")
//    }
////    above launched routines will now be cancelled
//    coroutineContext.cancelChildren()

//    val producer = produceNumbers2()
//    repeat(5) {
//        launchProcessor(it, producer)
//        delay(1000)
//    }
//
////  Note that cancelling a producer coroutine closes its channel, thus eventually terminating iteration over the channel that processor coroutines are doing.
////  Also, pay attention to how we explicitly iterate over channel with for loop to perform fan-out in launchProcessor code. Unlike consumeEach, this for loop pattern is perfectly safe to use from multiple coroutines. If one of the processor coroutines fails, then others would still be processing the channel, while a processor that is written via consumeEach always consumes (cancels) the underlying channel on its normal or abnormal completion.
//    producer.cancel()

//    val numbers = produceNumbers()
//    val squares = squareNumbers(numbers)
//    repeat(7){
//        println(squares.receive())
//    }
//    coroutineContext.cancelChildren()

//    val squares = produceSquares()
//    squares.consumeEach { println("$it") }

//    playWithChannels2()
}