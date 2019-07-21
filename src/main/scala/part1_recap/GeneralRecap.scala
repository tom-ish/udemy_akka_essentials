package part1_recap

import scala.util.Try

/**
  * Created by Tomohiro on 21 juillet 2019.
  */

object GeneralRecap extends App {

  val aCondition : Boolean = false

  var aVariable = 42
  aVariable += 1 // aVariable = 43

  // expressions
  val aCondtionedVal = if(aCondition) 42 else 65

  // code block
  val aCodeBlock = {
    if(aCondition) 74
    56
  }

  // types
  // Unit
  val theUnit = println("Hello Scala")

  def aFunction(x: Int): Int = x + 1

  // recursion - TAIL recursion
  def factorial(n: Int, acc: Int) : Int =
    if(n == 0) acc
    else factorial(n-1, acc * n)




  // Object Oriented Progamming

  class Animal
  class Dog extends Animal
  val aDog : Animal = new Dog

  trait Carnivore {
    def eat(a: Animal): Unit
  }

  class Crocodile extends Animal with Carnivore {
    override def eat(a: Animal): Unit = println("Crunch!")
  }

  // method notation
  val aCroc = new Crocodile
  aCroc.eat(aDog)
  aCroc eat aDog

  // anonymous classes
  val aCarnivore = new Carnivore {
    override def eat(a: Animal): Unit = println("ROAR!")
  }

  aCarnivore eat aDog

  // Generics
  abstract class MyList[+A]
  // Companion objects
  object MyList

  // Case classes
  case class Person(name: String, age: Int) // USED A LOT IN THIS COURSE!!!

  // Exceptions
  val aPotentialFailure = try {
    throw new RuntimeException("I'm innocent !") // Nothing
  } catch {
    case e : Exception => "I caught an exception !"
  } finally {
    // side effects
    println("some logs")
  }



  // Functional Programming

  val incrementer = new Function[Int, Int] {
    override def apply(v1: Int): Int = v1 + 1
  }
  val incremented = incrementer(42) // 43
  // incrementer.apply(42)

  val anonymousIncrementer = (x: Int) => x + 1
  // Int => Int ===== Function1[Int, Int]

  // Functional Programming is all about working with functions as first class
  List(1,2,3).map(incrementer)
  // map = Higher Order Function

  // for comprehensions
  val pairs = for {
    num <- List(1,2,3,4)
    char <- List('a', 'b', 'c', 'd')
  } yield num + "-" + char

  // List(1,2,3,4).flatMap(num => List('a','b','c','d').map(char => num + "-" + char))

  // Seq, Array, List, Vector, Map, Tuples, Sets

  // "collections"
  // Option and Try
  val anOption = Some(2)
  val aTry = Try {
    throw new RuntimeException
  }

  // Pattern Matching
  val unknown = 2
  val order = unknown match {
    case 1 => "first"
    case 2 => "second"
    case _ => "unknown"
  }

  val bob = Person("Bob", 22)
  val greeting = bob match {
    case Person(n, _) => s"Hi, my name is $n"
    case _ => "I don't know my name"
  }

  // ALL THE PATTERNS
}
