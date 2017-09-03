package io.github.binaryoverload;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Miscellaneous utilities for the library
 *
 * @author BinaryOverload
 * @since 1.1
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
        if (string.length() != length) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Verifies a path with a specific path separator
     *
     * @param path The path to verify
     * @param pathSeparator The path separator to use for verification <i>Cannot be null, empty
     *                      or any length other than 1</i>
     * @throws NullPointerException if either of the variables are null
     * @throws IllegalArgumentException if the path separator is empty or any length other than 1
     * @throws IllegalArgumentException if the path supplied is malformed
     * @since 2.1
     */
    public static void verifyPath(String path, String pathSeparator) {
        Objects.requireNonNull(path);
        Objects.requireNonNull(pathSeparator);
        Matcher matcher = Pattern.compile("([A-z0-9-?!£$%^&*()_]\\+(" + pathSeparator + "[A-z0-9-?!£$%^&*()_]\\+)*)+").matcher(path);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Malformed path");
        }
    }



}
