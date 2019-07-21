package part1_recap

import scala.concurrent.Future

/**
  * Created by Tomohiro on 21 juillet 2019.
  */

object ThreadModelLimitations extends App {

  /*
    Daniel's rants
   */

  /**
    * #1 : OOP encapsulation is only valid in the SINGLE THREADED MODEL.
    */

  class BankAccount(private var amount: Int) {
    override def toString: String = "" + amount

    def withdraw(money: Int) = this.synchronized {
      this.amount -= money
    }
    def deposit(money: Int) = this.synchronized {
      this.amount += money
    }
    def getAmount = amount
  }

  //  val account = new BankAccount(2000)
  //  for(_ <- 1 to 1000) {
  //    new Thread(() => account.withdraw(1)).start()
  //  }
  //  for(_ <- 1 to 1000) {
  //    new Thread(() => account.deposit(1)).start()
  //  }
  //
  //  println(account.getAmount)

  // OOP encapsulation is broken in a multithreaded environment
  // synchronization ! Locks to the rescue

  // deadlocks, livelocks


  /**
    * #2 : delegating something to a thread is a PAIN
    */

  // we have a running thread
  // we want to pass a runnable to that thread
  var task: Runnable = null

  var runningThread: Thread = new Thread(() => {
    while(true) {
      while(task == null) {
        runningThread.synchronized {
          println("[Background] waiting for a task...")
          runningThread.wait()
        }
      }

      task.synchronized {
        println("[Background] I have a task !")
        task.run()
        task = null
      }
    }
  })

  def delegateToBackgroundThread(r: Runnable) = {
    if(task == null) task = r

    runningThread.synchronized {
      runningThread.notify()
    }
  }

  runningThread.start()
  Thread.sleep(500)
  delegateToBackgroundThread(() => println(42))
  Thread.sleep(1000)
  delegateToBackgroundThread(() => println("This should run in the background"))


  /**
    * #3 : tracing and dealing with errors in a multithreaded environment is a PAIN IN THE NECK
    */
  // 1M numbers between 10 threads
  import scala.concurrent.ExecutionContext.Implicits.global

  val futures = (0 to 9)
    .map(i => 100000 * i until 100000 * (i + 1)) // 0 - 99999, 100000 - 199999, 200000 - 199999 etc
    .map(range => Future {
    if(range.contains(546735)) throw new RuntimeException("invalid number")
    range.sum
  })

  val sumFuture = Future.reduceLeft(futures)(_ + _) // Future with the sum of all the numbers
  sumFuture.onComplete(println)
}
