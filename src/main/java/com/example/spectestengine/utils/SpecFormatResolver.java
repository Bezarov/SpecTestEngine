package com.example.spectestengine.utils;

import com.example.spectestengine.exception.InvalidSpecException;
import com.example.spectestengine.model.SpecFormat;

public class SpecFormatResolver {
    private SpecFormatResolver() {
        throw new IllegalStateException("Utility class");
    }

    public static SpecFormat resolve(String rawSpec) {
        return switch (rawSpec) {
            case String spec when isJson(spec) -> SpecFormat.JSON;
            case String spec when isYaml(spec) -> SpecFormat.YAML;
            case String spec when isXml(spec) -> SpecFormat.XML;
            default -> throw new InvalidSpecException("Unsupported specification format");
        };
    }

    private static boolean isJson(String rawSpec) {
        return rawSpec.stripLeading().charAt(0) == '{' || rawSpec.stripLeading().charAt(0) == '[';
    }

    private static boolean isYaml(String rawSpec) {
        return rawSpec.contains(":") &&
                !rawSpec.contains("{") &&
                !rawSpec.contains("[") &&
                !rawSpec.contains("<");
    }

    private static boolean isXml(String rawSpec) {
        return rawSpec.stripLeading().charAt(0) == '<';
    }
}
