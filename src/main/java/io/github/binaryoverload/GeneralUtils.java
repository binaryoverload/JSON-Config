/*
 * MIT License
 *
 * Copyright (c) 2018 William Oldham
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
     * @throws NullPointerException     if either argument is null
     * @throws IllegalArgumentException if the string doesn't comply with the conditions
     * @since 1.1
     */
    public static void checkStringLength(String string, int length) {
        checkStringLength(string, length, true);
    }

    /**
     * Helper method to check string length
     *
     * @param string     The string to check
     * @param length     The length to check the string by
     * @param emptyCheck Whether to check if the string is empty or not
     * @throws NullPointerException     if either argument is null
     * @throws IllegalArgumentException if the string doesn't comply with the conditions
     * @since 1.1
     */
    public static void checkStringLength(String string, int length, boolean emptyCheck) {
        Objects.requireNonNull(string);
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
     * @param ps   The path separator to use for verification <i>Cannot be null, empty
     *             or any length other than 1</i>
     * @throws NullPointerException     if either of the variables are null
     * @throws IllegalArgumentException if the path separator is empty or any length other than 1
     * @throws IllegalArgumentException if the path supplied is malformed
     * @since 2.1
     */
    public static void verifyPath(String path, char ps) {
        Objects.requireNonNull(path);
        Matcher matcher = Pattern.compile("([^" + ps + "]+(" + ps + "[^" + ps + "])*)+").matcher(path);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Malformed path, could not match '" + path + "' with seperator '" + ps + "'");
        }
    }
}
