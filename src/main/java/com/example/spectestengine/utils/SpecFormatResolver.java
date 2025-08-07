package com.example.spectestengine.utils;

import com.example.spectestengine.model.SpecFormat;

public class SpecFormatResolver {
    private SpecFormatResolver() {
        throw new IllegalStateException("Utility class");
    }

    public static SpecFormat resolve(String rawSpec) {
        return switch (rawSpec.stripLeading().charAt(0)) {
            case '{', '[' -> SpecFormat.JSON;
            default -> SpecFormat.YAML;
        };
    }
}
