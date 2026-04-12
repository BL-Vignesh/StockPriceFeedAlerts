package com.portfolio.stockpricefeed.exception;

public class InvalidTickerException extends RuntimeException {
    public InvalidTickerException(String ticker) {
        super("Invalid stock ticker: '" + ticker + "'. Please select a valid NSE stock.");
    }
}