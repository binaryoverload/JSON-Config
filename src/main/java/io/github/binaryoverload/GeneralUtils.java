package io.github.binaryoverload;

import java.util.Objects;

/**
 * Miscellaneous utilities for the library
 *
 * @author BinaryOverload
 * @version 1.0
 * @since 1.0.1
 */
public class GeneralUtils {

    /**
     * Helper method to check string length
     * <i>Checks if the string is empty by default</i>
     *
     * @param string The string to check
     * @param length The length to check the string by
     * @throws NullPointerException if either argument is null
     * @throws IllegalArgumentException if the string doesn't comply with the conditions
     *
     * @since 1.1
     */
    public static void checkStringLength(String string, int length) {
        checkStringLength(string, length, true);
    }

    /**
     * Helper method to check string length
     *
     * @param string The string to check
     * @param length The length to check the string by
     * @param emptyCheck Whether to check if the string is empty or not
     * @throws NullPointerException if either argument is null
     * @throws IllegalArgumentException if the string doesn't comply with the conditions
     *
     * @since 1.1
     */
    public static void checkStringLength(String string, int length, boolean emptyCheck) {
        Objects.requireNonNull(string);
        Objects.requireNonNull(length);
        Objects.requireNonNull(emptyCheck);
        if (string.isEmpty() && emptyCheck) {
            throw new IllegalArgumentException();
        }
        if (!(string.length() == length)) {
            throw new IllegalArgumentException();
        }
    }

}
