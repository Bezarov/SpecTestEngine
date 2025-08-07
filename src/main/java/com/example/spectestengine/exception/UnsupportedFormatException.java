package com.example.spectestengine.exception;

public class UnsupportedFormatException extends RuntimeException {
    public UnsupportedFormatException(String format) {
        super("Unsupported format: " + format + ". Supported formats: JSON, YAML");
    }
}
