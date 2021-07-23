# Exchange_platform

## Summary

* For the Rest API and Rest connector I use [Finagle](https://twitter.github.io/finagle/) which provide the possibility
  to use the reactive features:
  * **Async request** Running the computation in another thread.
  * **Retry strategy:** To handle error in communications and retry with a specific strategy
  * **Circuit breaker:** A fail fast feature, to control that when a server is down, after apply 
    the retry strategy close the communication until it detects the server is responding again.
  * **Observability:**  We add a monitoring which it subscribe to error in communications, to allow us
    make some strategies in case we need.
  

* To control all possible side effects in our program I use Effect system [ZIO](https://zio.dev), a pure functional programing toolkit
  which provide the features to have Pure functional programs with side effect control,
  lazy evaluation, performance improvements since the program run in Fibers(Green threads) instead in OS Threads, and also DI mechanism with ZLayers.


## Testing

![My image](img/testPyramid.png)

**To go fast you have to go well** this quote of Robert C. Martin express perfectly what TDD and BDD is about. You should think first in all corner cases of your program, and then implement
one by one committing every scenario to have a quick feedback about your program.

In my application I invested around 70% of the time implementing the test framework, the type of testing implemented are described below.

* **Unit**: I used [scalatest](https://www.scalatest.org) together with some local mocks to Mock external resources of your class.
* **Integration**: I create the **test-framework** module with dependencies of both modules to test the end to end of the platform.
 
  Just to be clear, the Integration test are just a proof that our Unit test are well designed and the Mock behaves as I expect. None IT test should ever fail. And if it does,
  you have to reproduce it in Unit test.
  
## How to build



## How to run

java -cp conversion_service-assembly-0.1.0-SNAPSHOT.jar  com.politrons.app.ConversionServer
java -cp currency_exchange_service-assembly-0.1.0-SNAPSHOT.jar com.politrons.app.CurrencyExchangeServer

## Stack

![My image](img/ZIO.png) ![My image](img/finagle.png)