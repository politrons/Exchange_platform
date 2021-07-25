test:
	sbt test
build:
	sbt assembly

run-conversion:
	java -cp ./conversion_service/target/scala-2.12/conversion_service-assembly-0.1.0-SNAPSHOT.jar  com.politrons.app.ConversionServer
run-currency-exchange:
	java -cp ./currency_exchange_service/target/scala-2.12/currency_exchange_service-assembly-0.1.0-SNAPSHOT.jar com.politrons.app.CurrencyExchangeServer

run-services:
	java -cp ./conversion_service/target/scala-2.12/conversion_service-assembly-0.1.0-SNAPSHOT.jar  com.politrons.app.ConversionServer &
	java -cp ./currency_exchange_service/target/scala-2.12/currency_exchange_service-assembly-0.1.0-SNAPSHOT.jar com.politrons.app.CurrencyExchangeServer

test-request:
	curl --header "Content-Type: application/json" \
      --request POST \
      --data '{"fromCurrency": "GBR", "toCurrency" : "EUR", "amount" : 102.6}' \
      http://localhost:9994/api/convert/
clean:
	sbt clean

