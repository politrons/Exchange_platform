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


## How to build



## How to run

java -cp conversion_service-assembly-0.1.0-SNAPSHOT.jar  com.politrons.app.ConversionServer
java -cp currency_exchange_service-assembly-0.1.0-SNAPSHOT.jar com.politrons.app.CurrencyExchangeServer

## Stack

![My image](img/ZIO.png) ![My image](img/finagle.png)