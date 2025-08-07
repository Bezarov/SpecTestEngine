package com.example.spectestengine.model;

import com.example.spectestengine.exception.UnsupportedFormatException;
import lombok.Getter;
import org.springframework.http.MediaType;

@Getter
public enum SpecFormat {
    JSON(MediaType.APPLICATION_JSON),
    YAML(MediaType.APPLICATION_YAML);

    private final MediaType mediaType;

    SpecFormat(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public static MediaType getMediaType(String format) {
        for (SpecFormat supportedFormat : values()) {
            if (supportedFormat.name().equalsIgnoreCase(format)) {
                return supportedFormat.getMediaType();
            }
        }
        throw new UnsupportedFormatException(format);
    }
}