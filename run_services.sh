#!/bin/bash

echo "Running Conversion Server"
echo "--------------------------"
java -cp ./conversion_service/target/scala-2.12/conversion_service-assembly-0.1.0-SNAPSHOT.jar  com.politrons.app.ConversionServer &
echo "Running Currency Exchange Server"
echo "-----------------------------------"
java -cp ./currency_exchange_service/target/scala-2.12/currency_exchange_service-assembly-0.1.0-SNAPSHOT.jar com.politrons.app.CurrencyExchangeServer
