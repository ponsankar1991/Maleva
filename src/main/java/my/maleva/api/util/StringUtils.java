package my.maleva.api.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class StringUtils {

    private StringUtils() {
        // prevent instantiation
    }

    public static List<String> splitInParts(String input, int partLength) {

        Objects.requireNonNull(input, "Input string cannot be null");

        if (partLength <= 0) {
            throw new IllegalArgumentException("Part length must be greater than zero.");
        }

        List<String> parts = new ArrayList<>();

        for (int i = 0; i < input.length(); i += partLength) {
            parts.add(input.substring(
                    i,
                    Math.min(i + partLength, input.length())
            ));
        }

        return parts;
    }
}
