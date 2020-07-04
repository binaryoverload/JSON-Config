/*
* MIT License
*
* Copyright (c) 2020 William Oldham
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
package com.github.binaryoverload

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.stream.JsonReader
import java.awt.PageAttributes
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.Arrays
import java.util.LinkedHashMap
import java.util.LinkedHashSet
import java.util.Objects
import java.util.Optional
import java.util.OptionalDouble
import java.util.OptionalInt
import java.util.OptionalLong
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock
import java.util.regex.Pattern
import java.util.stream.Collectors


/**
 * Main config class used to represent a json file
 * accessible by "paths"
 *
 * @author BinaryOverload
 * @since 1.0
 */
open class JSONConfig {

    private var mode: MediaType? = null
    private var file: Any? = null
    private var pathSeparator = '.'
    private var allowedSpecialCharacters = charArrayOf('-', '+', '_', '$')
    private val configLock: ReadWriteLock = ReentrantReadWriteLock()
    private val readLock = configLock.readLock()
    private val writeLock = configLock.writeLock()
    private var pathPattern = generatePathPattern(pathSeparator, allowedSpecialCharacters)
    private var splitPattern = Pattern.compile(Pattern.quote(pathSeparator.toString()))

    /**
     * Recommended constructor for most file based applications
     * <br></br>
     * *Uses the default path separator*
     *
     * @param file The file must exist and not be a directory
     * @throws FileNotFoundException if the file does not exist,
     * is a directory rather than a regular file,
     * or for some other reason cannot be opened for
     * reading.
     * @throws NullPointerException  if the passed variable is null
     * @see FileReader
     *
     * @see File
     *
     * @since 1.0
     */
    constructor(file: File) : this() {
        Objects.requireNonNull(file)
        this.internalObject = GSON.fromJson(JsonReader(FileReader(file)), JsonObject::class.java)
        Objects.requireNonNull(this.internalObject, "Input is empty!")
        mode = MediaType.FILE
        this.file = file
    }

    /**
     * Recommended constructor for most file based applications
     * <br></br>
     * *Uses the default path separator*
     *
     * @param fileName The file to get the config from
     * @throws FileNotFoundException    if the file does not exist,
     * is a directory rather than a regular file,
     * or for some other reason cannot be opened for
     * reading.
     * @throws NullPointerException     if the passed variable is null
     * @throws IllegalArgumentException if the file name is empty
     * @see FileReader
     *
     * @since 1.0
     */
    constructor(fileName: String) : this() {
        Objects.requireNonNull(fileName)
        require(fileName.isNotEmpty())
        mode = MediaType.FILE_NAME
        this.internalObject = GSON.fromJson(JsonReader(FileReader(fileName)), JsonObject::class.java)
        Objects.requireNonNull(this.internalObject, "Input is empty!")
        file = fileName
    }

    /**
     * Constructor for use with file-based applications and specification
     * of a custom path separator
     *
     * @param file                The file must exist and not be a directory
     * @param pathSeparator       The separator to use for this config *This cannot be null, empty or
     * any length other than 1*
     * @param allowedSpecialChars The allowed special characters which can be used on the path. This cannot conflict
     * with the path separator.
     * @throws FileNotFoundException    if the file does not exist,
     * is a directory rather than a regular file,
     * or for some other reason cannot be opened for
     * reading.
     * @throws NullPointerException     if any of the passed variables are null
     * @throws IllegalArgumentException if the path separator is not empty or if it is any length
     * other than 1
     * @see FileInputStream
     *
     * @see File
     *
     * @since 1.0
     */
    constructor(file: File, pathSeparator: Char, allowedSpecialChars: CharArray) : this(allowedSpecialChars) {
        Objects.requireNonNull(file)
        this.internalObject = GSON.fromJson(JsonReader(FileReader(file)), JsonObject::class.java)
        Objects.requireNonNull(this.internalObject, "Input is empty!")
        setPathSeparator(pathSeparator)
        mode = MediaType.FILE
        this.file = file
    }

    /**
     * More advanced constructor allowing users to specify their own input stream
     *
     * @param stream The stream to be used for the JSON Object *This cannot be null*
     * @throws NullPointerException if the stream is null
     * @see InputStream
     *
     * @since 1.0
     */
    constructor(stream: InputStream) : this() {
        Objects.requireNonNull(stream)
        this.internalObject = GSON.fromJson(JsonReader(InputStreamReader(stream)), JsonObject::class.java)
        Objects.requireNonNull(this.internalObject, "Input is empty!")
        mode = MediaType.INPUT_STREAM
        file = stream
    }

    /**
     * More advanced constructor allowing users to specify their own input stream
     *
     * @param stream              The stream to be used for the JSON Object *This cannot be null*
     * @param pathSeparator       The custom path separator to use for this config *This cannot be
     * null, empty or any other lenth than 1*
     * @param allowedSpecialChars The allowed special characters which can be used on the path. This cannot conflict
     * with the path separator.
     * @throws NullPointerException     if any of the passed arguments are null
     * @throws IllegalArgumentException if the path separator is empty or not a length of 1
     * @throws IOException              if the stream is invalid or malformatted
     * @see InputStream
     *
     * @since 1.0
     */
    constructor(stream: InputStream, pathSeparator: Char, allowedSpecialChars: CharArray) : this(allowedSpecialChars) {
        Objects.requireNonNull(stream)
        setPathSeparator(pathSeparator)
        val br = BufferedReader(InputStreamReader(stream))
        this.internalObject = GSON.fromJson(br.lines().collect(Collectors.joining()), JsonObject::class.java)
        br.close()
        Objects.requireNonNull(this.internalObject, "Input is empty!")
        mode = MediaType.INPUT_STREAM
        file = stream
    }

    /**
     * Basic constructor that directly sets the JSONObject
     *
     * @param internalObject The object to assign to the config *Cannot be null*
     * @throws NullPointerException if the object is null
     * @see JsonObject
     *
     * @since 1.0
     */
    constructor(internalObject: JsonObject) : this() {
        this.internalObject = internalObject
        mode = MediaType.JSON_OBJECT
    }

    /**
     * Basic Constructor that sets a JSONObject as well as the path separator
     *
     * @param internalObject              The object to assign to the config *Cannot be null*
     * @param pathSeparator       The path separator to be set *Cannot be null, empty or any length
     * other than 1*
     * @param allowedSpecialChars The allowed special characters which can be used on the path. This cannot conflict
     * with the path separator.
     * @throws NullPointerException     if either of the passed arguments are null
     * @throws IllegalArgumentException if the path separator is empty or not a length of 1
     * @since 1.0
     */
    constructor(internalObject: JsonObject, pathSeparator: Char, allowedSpecialChars: CharArray) : this(allowedSpecialChars) {
        this.internalObject = internalObject
        setPathSeparator(pathSeparator)
        mode = MediaType.JSON_OBJECT
    }

    private constructor(allowedSpecialCharacters: CharArray) {
        this.allowedSpecialCharacters = allowedSpecialCharacters
    }

    private constructor() {}

    /**
     * Gets the path separator for this config
     *
     * @return The path separator
     * @since 1.0
     */
    fun getPathSeparator(): Char {
        return pathSeparator
    }

    /**
     * Sets the path separator for this config
     *
     * @param pathSeparator The path separator to be set *Cannot be null, empty or any length
     * other than 1*
     * @throws NullPointerException     if the path separator provided is null
     * @throws IllegalArgumentException if the path separator is empty of any length other than 1
     * @since 1.0
     */
    fun setPathSeparator(pathSeparator: Char) {
        writeLock.lock()
        try {
            this.pathSeparator = pathSeparator
            for (allowedChar in allowedSpecialCharacters) {
                require(allowedChar != pathSeparator) { "Cannot set path separator to an allowed special character!" }
            }

            // Recompile the pattern on a new path separator.
            pathPattern = generatePathPattern(pathSeparator, allowedSpecialCharacters)
            splitPattern = Pattern.compile(Pattern.quote(pathSeparator.toString()))
        } finally {
            writeLock.unlock()
        }
    }

    /**
     * Gets the JSON object associated with this config
     *
     * @return The JSON Object associated with this config
     * @since 1.0
     */
    /**
     * Sets the JSON object for this config
     *
     * @param object The object to be set *Cannot be null*
     * @throws NullPointerException if the object is null
     * @since 1.0
     */
    var internalObject: JsonObject
        get() {
            readLock.lock()
            return try {
                field.deepCopy()
            } finally {
                readLock.unlock()
            }
        }
        set(obj) {
            writeLock.lock()
            try {
                field = obj
            } finally {
                writeLock.unlock()
            }
        }

    /**
     * Returns a new config with its root object set to the object
     * retrieved from the specified path
     *
     * @param path The path to get the new config from *Cannot be null*
     * @return The JSONConfig wrapped in an [Optional]. The optional is empty if the element
     * at the path is non-existent
     * @throws NullPointerException  if the path is null
     * @throws IllegalStateException if the element at the path is not a JSON object
     * @since 2.3
     */
    fun getSubConfig(path: String): JSONConfig? {
        Objects.requireNonNull(path)
        readLock.lock()
        return try {
            val element = getElement(path)
            if (element == null) {
                null
            } else if (!element.isJsonObject) {
                throw IllegalStateException("The element at the specified path is not a JSON object")
            } else {
                JSONConfig(element.asJsonObject, pathSeparator, allowedSpecialCharacters)
            }
        } finally {
            readLock.unlock()
        }
    }

    /**
     * Gets the list of chars that are allowed in a path
     *
     * @return List of allowed special characters
     */
    fun getAllowedSpecialCharacters(): CharArray {
        readLock.lock()
        return try {
            allowedSpecialCharacters.clone()
        } finally {
            readLock.unlock()
        }
    }

    /**
     * Sets the list of special characters allowed
     *
     * @param allowedSpecialCharacters The list of special characters to be allowed
     */
    fun setAllowedSpecialCharacters(allowedSpecialCharacters: CharArray) {
        writeLock.lock()
        try {
            this.allowedSpecialCharacters = allowedSpecialCharacters.clone()
            pathPattern = generatePathPattern(pathSeparator, allowedSpecialCharacters)
        } finally {
            writeLock.unlock()
        }
    }

    /**
     * Adds a character to the allowed special characters list
     *
     * @param charToAdd The character to add to the allowed special characters list
     */
    fun addAllowedSpecialCharacter(charToAdd: Char) {
        writeLock.lock()
        try {
            for (c in allowedSpecialCharacters) {
                if (c == charToAdd) return
            }
            val newArray = allowedSpecialCharacters.copyOf(allowedSpecialCharacters.size + 1)
            newArray[newArray.size - 1] = charToAdd
            allowedSpecialCharacters = newArray
        } finally {
            writeLock.unlock()
        }
    }

    /**
     * Method to get a JSON Element from a specified path
     *
     *
     * **It is not recommended to use this method! Use
     * [com.github.binaryoverload.JSONConfig.getElement] instead!**
     *
     * @param json      The object to search in
     * @param path      The path to get the element from. If this is blank,
     * it returns the entire object. If the path is malformed,
     * then it throws an [IllegalArgumentException]
     * @param allowNull Whether to allow null
     * @return The element at the specified path *Returns an empty optional if the element
     * doesn't exist*
     * @throws NullPointerException     if the object specified is null
     * @throws IllegalArgumentException if the path is malformed
     * @since 2.0
     */
    fun getElement(json: JsonObject, path: String, allowNull: Boolean): JsonElement? {
        readLock.lock()
        try {
            if (path.isEmpty()) {
                return json
            } else {
                verifyPath(path, pathPattern)
            }
            val subpaths = splitPattern.split(path)
            val subpath = subpaths[0]
            return if (json[subpath] == null || json[subpath].isJsonNull) {
                if (allowNull) {
                    JsonNull.INSTANCE
                } else {
                    null
                }
            } else if (json[subpath].isJsonObject) {
                if (subpaths.size == 1 && subpaths[0].isEmpty()) {
                    json
                } else getElement(json[subpath].asJsonObject, Arrays.stream(subpaths).skip(1).collect(Collectors.joining(pathSeparator.toString())))
            } else {
                json[subpath]
            }
        } finally {
            readLock.unlock()
        }
    }

    /**
     * Method to get a JSON Element from a specified path
     *
     *
     * **It is not recommended to use this method! Use
     * [com.github.binaryoverload.JSONConfig.getElement] instead!**
     *
     * @param json The object to search in
     * @param path The path to get the element from. If this is blank,
     * it returns the entire object. If the path is malformed,
     * then it throws an [IllegalArgumentException]
     * @return The element at the specified path *Returns an empty optional if the element
     * doesn't exist*
     * @throws NullPointerException     if the object specified is null
     * @throws IllegalArgumentException if the path is malformed
     * @since 2.0
     */
    fun getElement(json: JsonObject, path: String): JsonElement? {
        return getElement(json, path, false)
    }

    /**
     * The recommended method to get an element from the config
     *
     * @param path The path to get the element from. If this is blank,
     * it returns the entire object. If the path is malformed,
     * then it throws an [IllegalArgumentException]
     * @return The element at the specified path *Returns an empty optional if the element
     * doesn't exist*
     * @throws IllegalArgumentException if the path is malformed
     * @since 2.0
     */
    fun getElement(path: String): JsonElement? {
        return getElement(this.internalObject, path)
    }

    /**
     * Sets an object at a specified path. Creates sub paths if they don't exist.
     *
     * @param path   The path to get the element from. If this is blank,
     * it sets the root object. If the path is malformed,
     * then it throws an [IllegalArgumentException]
     * @param object The object to set at the specified path
     * @throws IllegalArgumentException if the path is malformed
     * @throws NullPointerException     if the path is null
     * @since 2.0
     */
    operator fun set(path: String, `object`: Any?) {
        writeLock.lock()
        try {
            val json: JsonObject = this.internalObject
            var root = json
            if (path.isEmpty()) {
                root = GSON.toJsonTree(`object`).asJsonObject
            } else {
                verifyPath(path, pathPattern)
            }
            val subpaths = splitPattern.split(path)
            for (j in subpaths.indices) {
                if (root[subpaths[j]] == null || root[subpaths[j]].isJsonNull) {
                    root.add(subpaths[j], JsonObject())
                    if (j == subpaths.size - 1) {
                        root.add(subpaths[j], JSONConfig.Companion.GSON.toJsonTree(`object`))
                    } else {
                        root = root[subpaths[j]].asJsonObject
                    }
                } else {
                    if (j == subpaths.size - 1) {
                        root.add(subpaths[j], JSONConfig.Companion.GSON.toJsonTree(`object`))
                    } else {
                        root = root[subpaths[j]].asJsonObject
                    }
                }
                if (j == subpaths.size - 1) {
                    root = json
                }
            }
        } finally {
            writeLock.unlock()
        }
    }

    /**
     * Removes an element from the path
     *
     * @param path The path to remove
     * @throws IllegalStateException if the element at the path is not present
     * @throws IllegalStateException if the parent of the specified path is not an object
     */
    fun remove(path: String) {
        writeLock.lock()
        try {
            val paths = splitPattern.split(path)
            check(getElement(path) != null) { "Element not present!" }
            if (paths.size == 1) {
                internalObject.remove(path)
            } else {
                val subConfig = getSubConfig(path.substring(0, path.lastIndexOf(pathSeparator)))
                if (subConfig != null) {
                    subConfig.remove(path.substring(path.lastIndexOf(pathSeparator) + 1, path.length))
                } else {
                    throw IllegalStateException("Parent of path specified is not an object!")
                }
            }
        } finally {
            writeLock.unlock()
        }
    }

    /**
     * Gets a string at the specified path
     *
     * @param path The path to get the string at *Must not be null*
     * @return An optional containing the string value. If the value at the path does not exist
     * optional is empty
     * @throws NullPointerException     if the path is null
     * @throws IllegalArgumentException if the path is malformed
     * @throws IllegalStateException    if the element at the path is not a string
     * @see JSONConfig.getElement
     * @since 2.1
     */
    fun getString(path: String): String? {
        verifyPath(path, pathPattern)
        val element = getElement(path)
        return if (element == null) {
            null
        } else if (element.isJsonPrimitive && element.asJsonPrimitive.isString) {
            element.asString
        } else {
            throw IllegalStateException("The element at the path is not a string")
        }
    }

    /**
     * Gets a integer at the specified path
     *
     * @param path The path to get the integer at *Must not be null*
     * @return An optional containing the integer value. If the value at the path does not exist
     * then the optional is empty
     * @throws NullPointerException     if the path is null
     * @throws IllegalArgumentException if the path is malformed
     * @throws IllegalStateException    if the element at the path is not a number
     * @see JSONConfig.getElement
     * @since 2.1
     */
    fun getInteger(path: String): Int? {
        verifyPath(path, pathPattern)
        val element = getElement(path)
        return if (element == null) {
            null
        } else if (element.isJsonPrimitive && element.asJsonPrimitive.isNumber) {
            element.asNumber.toInt()
        } else {
            throw IllegalStateException("The element at the path is not a number")
        }
    }

    /**
     * Gets a double at the specified path
     *
     * @param path The path to get the double at *Must not*
     * @return An optional containing the double value. If the value at the path does not exist
     * then the optional is empty
     * @throws NullPointerException     if the path is null
     * @throws IllegalArgumentException if the path is malformed
     * @throws IllegalStateException    if the element at the path is not a number
     * @see JSONConfig.getElement
     * @since 2.2
     */
    fun getDouble(path: String): Double? {
        verifyPath(path, pathPattern)
        val element = getElement(path)
        return if (element == null) {
            null
        } else if (element.isJsonPrimitive && element.asJsonPrimitive.isNumber) {
            element.asNumber.toDouble()
        } else {
            throw IllegalStateException("The element at the path is not a number")
        }
    }

    /**
     * Gets a long at the specified path
     *
     * @param path The path to get the long at *Must not be null*
     * @return An optional containing the long value. If the value at the path does not exist
     * then the optional is empty
     * @throws NullPointerException     if the path is null
     * @throws IllegalArgumentException if the path is malformed
     * @throws IllegalStateException    if the element at the path is not a number
     * @see JSONConfig.getElement
     * @since 2.2
     */
    fun getLong(path: String): Long? {
        verifyPath(path, pathPattern)
        val element = getElement(path)
        return if (element == null) {
            null
        } else if (element.isJsonPrimitive && element.asJsonPrimitive.isNumber) {
            element.asNumber.toLong()
        } else {
            throw IllegalStateException("The element at the path is not a number")
        }
    }

    /**
     * Gets a boolean at the specified path
     *
     * @param path The path to get the boolean at *Must not be null*
     * @return An optional containing the double value. If the value at the path does not exist
     * then the optional is empty
     * @throws NullPointerException     if the path is null
     * @throws IllegalArgumentException if the path is malformed
     * @throws IllegalStateException    if the element at the path is not a boolean
     * @see JSONConfig.getElement
     * @since 2.2
     */
    fun getBoolean(path: String): Boolean? {
        verifyPath(path, pathPattern)
        val element = getElement(path)
        return if (element == null) {
            null
        } else if (element.isJsonPrimitive && element.asJsonPrimitive.isBoolean) {
            element.asBoolean
        } else {
            throw IllegalStateException("The element at the path is not a boolean")
        }
    }

    /**
     * Gets a JSON array at the specified path
     *
     * @param path The path to get the array at *Must not be null*
     * @return An optional containing the array. If the value at the path doesn't exist
     * than the optional is empty
     * @throws NullPointerException     if the path is null
     * @throws IllegalArgumentException if the path is malformed
     * @throws IllegalStateException    if the element at the path is not an array
     * @see JSONConfig.getElement
     * @since 2.5
     */
    fun getArray(path: String): JsonArray? {
        verifyPath(path, pathPattern)
        val element = getElement(path)
        return when {
            element == null -> null
            element.isJsonArray -> element.asJsonArray
            else -> throw IllegalStateException("The element at the path is not an array")
        }
    }

    /**
     * Gets a list of keys in this config
     * If `deep` is true then it will descend into all subpaths
     * using the path to the keys as the key itself
     *
     * @param deep Whether to transverse the entire config tree or not
     * @return A [LinkedHashSet] containing all the keys. If the config is empty
     * then so will this list be
     */
    fun getKeys(deep: Boolean): Set<String> {
        val set: MutableSet<String> = LinkedHashSet()
        mapChildrenKeys(set, deep)
        return set
    }

    protected fun mapChildrenKeys(output: MutableSet<String>, deep: Boolean) {
        readLock.lock()
        try {
            for ((key, value) in this.internalObject.entrySet()) {
                output.add(key)
                if (value.isJsonObject && deep) {
                    mapChildrenKeys(key, output, value.asJsonObject, deep)
                }
            }
        } finally {
            readLock.unlock()
        }
    }

    protected fun mapChildrenKeys(prefix: String, output: MutableSet<String>, jsonObject: JsonObject, deep: Boolean) {
        readLock.lock()
        try {
            for ((key, value) in jsonObject.entrySet()) {
                output.add(prefix + pathSeparator + key)
                if (value.isJsonObject && deep) {
                    mapChildrenKeys(prefix + pathSeparator + key, output, value.asJsonObject, deep)
                }
            }
        } finally {
            readLock.unlock()
        }
    }

    /**
     * Gets a map of keys and values in this config
     * If `deep` is true then it will descend into all subpaths
     * using the path to the keys as the key itself
     *
     * @param deep Whether to transverse the entire config tree or not
     * @return A [LinkedHashMap] containing all the keys and values. If the config is empty
     * then so will this list be
     */
    fun getValues(deep: Boolean): Map<String, Any> {
        val map: MutableMap<String, Any> = LinkedHashMap()
        mapChildrenValues(map, deep)
        return map
    }

    protected fun mapChildrenValues(output: MutableMap<String, Any>, deep: Boolean) {
        readLock.lock()
        try {
            for ((key, value) in this.internalObject.entrySet()) {
                output[key] = value
                if (value.isJsonObject && deep) {
                    mapChildrenValues(key, output, value.asJsonObject, deep)
                }
            }
        } finally {
            readLock.unlock()
        }
    }

    protected fun mapChildrenValues(prefix: String, output: MutableMap<String, Any>, jsonObject: JsonObject, deep: Boolean) {
        for ((key, value) in jsonObject.entrySet()) {
            output[prefix + pathSeparator + key] = value
            if (value.isJsonObject && deep) {
                mapChildrenValues(prefix + pathSeparator + key, output, value.asJsonObject, deep)
            }
        }
    }

    override fun toString(): String {
        readLock.lock()
        return try {
            internalObject.toString()
        } finally {
            readLock.unlock()
        }
    }

    /**
     * Reloads the config.
     *
     * @throws UnsupportedOperationException If trying to reload from JsonObject
     * @throws IllegalStateException         If somehow the mode is invalid
     * @throws FileNotFoundException         If the config file was moved/removed
     */
    @Throws(FileNotFoundException::class)
    fun reload() {
        writeLock.lock()
        try {
            when (mode) {
                MediaType.FILE -> {
                    val file = file as File?
                    this.internalObject = JSONConfig.Companion.GSON.fromJson<JsonObject>(JsonReader(FileReader(file)), JsonObject::class.java)
                }

                MediaType.FILE_NAME -> {
                    val fileName = file as String?
                    this.internalObject = JSONConfig.Companion.GSON.fromJson<JsonObject>(JsonReader(FileReader(fileName)), JsonObject::class.java)
                }

                MediaType.INPUT_STREAM -> {
                    val stream = file as InputStream?
                    this.internalObject = JSONConfig.Companion.GSON.fromJson<JsonObject>(JsonReader(InputStreamReader(stream)), JsonObject::class.java)
                }

                MediaType.JSON_OBJECT -> throw UnsupportedOperationException("Cannot reload from a JsonObject")
                else -> throw IllegalStateException("Invalid mode")
            }
        } finally {
            writeLock.unlock()
        }
    }

    companion object {
        private val GSON = GsonBuilder().serializeNulls().create()
    }
}