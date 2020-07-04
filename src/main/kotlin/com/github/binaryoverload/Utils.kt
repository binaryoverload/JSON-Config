package com.github.binaryoverload

import java.util.Objects
import java.util.regex.Pattern

/**
 * Helper method to check string length
 * *Checks if the string is empty by default*
 *
 * @param string The string to check
 * @param length The length to check the string by
 * @throws NullPointerException     if either argument is null
 * @throws IllegalArgumentException if the string doesn't comply with the conditions
 * @since 1.1
 */
fun checkStringLength(string: String, length: Int) {
    checkStringLength(string, length, true)
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
fun checkStringLength(string: String, length: Int, emptyCheck: Boolean) {
    Objects.requireNonNull(string)
    require(!(string.isEmpty() && emptyCheck))
    require(string.length == length)
}

/**
 * Verifies a path with a specific path separator
 *
 * @param path    The path to verify
 * @param pattern The regex pattern which is used to check if the path matches.
 * @throws NullPointerException     if either of the variables are null
 * @throws IllegalArgumentException if the path separator is empty or any length other than 1
 * @throws IllegalArgumentException if the path supplied is malformed
 * @since 2.1
 */
fun verifyPath(path: String, pattern: Pattern) {
    Objects.requireNonNull(path)
    val matcher = pattern.matcher(path)
    require(matcher.matches()) {
        String.format("Malformed path, could not match '%s'. Regex: %s",
                path,
                pattern.toString()
        )
    }
}

/**
 * Verifies a path with a specific path separator
 *
 * @param path                The path to verify.
 * @param ps                  The path separator to use for verification.
 * @param allowedSpecialChars The allowed special characters which can be used on the path. This cannot conflict
 * with the path separator.
 * @throws NullPointerException     if either of the variables are null
 * @throws IllegalArgumentException if the path separator is empty or any length other than 1
 * @throws IllegalArgumentException if the path supplied is malformed
 * @since 2.1
 */
fun verifyPath(path: String, ps: Char, allowedSpecialChars: Set<Char>) {
    Objects.requireNonNull(path)
    require(verifyNoConflict(ps, allowedSpecialChars)) { "The allowedSpecialChars array contains the path separator!" }
    val matcher = generatePathPattern(ps, allowedSpecialChars).matcher(path)
    require(matcher.matches()) {
        String.format("Malformed path, could not match '%s'. Regex: %s",
                path,
                matcher.toString()
        )
    }
}

/**
 * Verify that the char array (in this case special character array) does not contain char
 * (in this case path separator).
 *
 * @param c      The char to check for.
 * @param cList The array to iterate through.
 * @return True if the char is not in the array, false otherwise.
 */
fun verifyNoConflict(c: Char, cList: Set<Char>): Boolean {
    for (cc in cList) if (c == cc) return false
    return true
}

fun escapeRegex(s: String?): String? {
    return if (s == null || s.isEmpty()) "" else s.replace(s.toRegex(), "\\\\$0")
}

/**
 * Generate a Pattern (compiled) from pathSeparator char and allowedSpecialCharacters char array.
 * You should only run this when it is needed and store the return. You should NOT run this multiple times!
 *
 * @param pathSeparator            The path separator char to include in the regex.
 * @param allowedSpecialCharacters The allowed special characters which will be escaped. Keep in mind
 * this will not verify a conflict. See [.verifyNoConflict]
 * for that.
 * @return The compiled Pattern used for checking a valid path with the specified pathSeperator and
 * allowedSpecialCharacters.
 */
fun generatePathPattern(pathSeparator: Char, allowedSpecialCharacters: Set<Char>): Pattern {
    val allowed = allowedSpecialCharacters.joinToString()
    return Pattern.compile(String.format("([\\w%s]+[%s]?)+([\\w%s]+)*", allowed, pathSeparator, allowed))
}