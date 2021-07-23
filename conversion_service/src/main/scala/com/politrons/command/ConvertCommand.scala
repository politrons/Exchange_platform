package com.politrons.command

case class ConvertCommand(fromCurrency: String,
                          toCurrency: String,
                          amount: Double) {

}

