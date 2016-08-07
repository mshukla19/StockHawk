package com.sam_chordas.android.stockhawk.rest;

/**
 * Created by manas on 8/7/16.
 */
public class InvalidStockException extends Exception {
    @Override
    public String getMessage() {
        return "Invalid Stock Symbol";
    }
}
