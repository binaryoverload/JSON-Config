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
import java.util.concurrent.locks.ReentrantReadWriteLock
import java.util.regex.Pattern
import java.util.stream.Collectors
import kotlin.concurrent.read
import kotlin.concurrent.write


/**
 * Main config class used to represent a json file
 * accessible by "paths"
 *
 * @author BinaryOverload
 * @since 1.0
 */
open class JSONConfig private constructor(val mode: MediaType) {

    @Transient
    private val configLock: ReentrantReadWriteLock = ReentrantReadWriteLock()

    var pathSeparator = '.'
        get() = configLock.read { field }
        set(sep) {
            configLock.write {
                for (allowedChar in allowedSpecialCharacters) {
                    require(allowedChar != pathSeparator) { "Cannot set path separator to an allowed special character!" }
                }
                field = sep

                // Recompile the pattern on a new path separator.
                pathPattern = generatePathPattern(pathSeparator, allowedSpecialCharacters)
                splitPattern = Pattern.compile(Pattern.quote(pathSeparator.toString()))
            }
        }

    private var allowedSpecialCharacters = mutableSetOf('-', '+', '_', '$')
        private set
    private var file: Any? = null
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
     * @see FileReader
     *
     * @see File
     *
     * @since 1.0
     */
    constructor(file: File) : this(MediaType.FILE) {
        Objects.requireNonNull(file)
        this._internalObject = GSON.fromJson(JsonReader(FileReader(file)), JsonObject::class.java)
        Objects.requireNonNull(this._internalObject, "Input is empty!")
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
     * @throws IllegalArgumentException if the file name is empty.
     * @see FileReader
     *
     * @since 1.0
     */
    constructor(fileName: String) : this(MediaType.FILE_NAME) {
        Objects.requireNonNull(fileName)
        require(fileName.isNotEmpty())
        this._internalObject = GSON.fromJson(JsonReader(FileReader(fileName)), JsonObject::class.java)
        Objects.requireNonNull(this._internalObject, "Input is empty!")
        file = fileName
    }

    /**
     * Constructor for use with file-based applications and specification
     * of a custom path separator.
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
     * @throws IllegalArgumentException if the path separator is not empty or if it is any length
     * other than 1.
     * @see FileInputStream
     *
     * @see File
     *
     * @since 1.0
     */
    constructor(file: File, pathSeparator: Char, allowedSpecialChars: MutableSet<Char>) : this(MediaType.FILE) {
        Objects.requireNonNull(file)
        this._internalObject = GSON.fromJson(JsonReader(FileReader(file)), JsonObject::class.java)
        Objects.requireNonNull(this._internalObject, "Input is empty!")
        this.pathSeparator = pathSeparator
        this.file = file
    }

    /**
     * More advanced constructor allowing users to specify their own input stream
     *
     * @param stream The stream to be used for the JSON Object *This cannot be null*
     * @see InputStream
     *
     * @since 1.0
     */
    constructor(stream: InputStream) : this(MediaType.INPUT_STREAM) {
        Objects.requireNonNull(stream)
        this._internalObject = GSON.fromJson(JsonReader(InputStreamReader(stream)), JsonObject::class.java)
        Objects.requireNonNull(this._internalObject, "Input is empty!")
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
     * @throws IllegalArgumentException if the path separator is empty or not a length of 1.
     * @throws IOException              if the stream is invalid or malformatted.
     * @see InputStream
     *
     * @since 1.0
     */
    constructor(stream: InputStream, pathSeparator: Char, allowedSpecialChars: MutableSet<Char>) : this(MediaType.INPUT_STREAM) {
        Objects.requireNonNull(stream)
        this.pathSeparator = pathSeparator
        BufferedReader(InputStreamReader(stream)).useLines {
            this._internalObject = GSON.fromJson(it.joinToString(), JsonObject::class.java)
        }
        Objects.requireNonNull(this._internalObject, "Input is empty!")
        file = stream
    }

    /**
     * Basic Constructor that sets a JSONObject as well as the path separator
     *
     * @param internalObject              The object to assign to the config *Cannot be null*
     * @param pathSeparator       The path separator to be set *Cannot be null, empty or any length
     * other than 1*
     * @param allowedSpecialChars The allowed special characters which can be used on the path. This cannot conflict
     * with the path separator.
     * @throws IllegalArgumentException if the path separator is empty or not a length of 1.
     * @since 1.0
     */
    constructor(internalObject: JsonObject, pathSeparator: Char, allowedSpecialChars: MutableSet<Char>) : this(MediaType.JSON_OBJECT) {
        this._internalObject = internalObject
        this.pathSeparator = pathSeparator
        this.allowedSpecialCharacters = this.allowedSpecialCharacters.union(allowedSpecialChars).toMutableSet()
    }

    constructor() : this(MediaType.JSON_OBJECT)

    private var _internalObject: JsonObject = JsonObject()
        get() {
            return configLock.read {
                field
            }
        }
        set(obj) {
            configLock.write {
                field = obj
            }
        }

    /**
     * Returns a copy of the internal JsonObject for this configuration.
     *
     * @return The deep-copy of the internal JsonObject.
     * @since 3.0
     */
    fun getInternalObject(): JsonObject = configLock.read { _internalObject.deepCopy() }

    /**
     * Returns a new config with its root object set to the object
     * retrieved from the specified path
     *
     * @param path The path to get the new config from *Cannot be null*
     * @return The JSONConfig at the path. Returns null if there is not an element at the specified path.
     * @throws IllegalStateException if the element at the path is not a JSON object.
     * @since 2.3
     */
    fun getSubConfig(path: String): JSONConfig? {
        return configLock.read {
            val element = getElement(path)
            if (element == null) {
                null
            } else if (!element.isJsonObject) {
                throw IllegalStateException("The element at the specified path is not a JSON object")
            } else {
                JSONConfig(element.asJsonObject, pathSeparator, allowedSpecialCharacters)
            }
        }
    }

    /**
     * Adds a character to the allowed special characters set.
     *
     * @param charToAdd The character to add.
     * @return true if the character was added. false if the character was already present in the set.
     * @see Set
     * @since 3.0
     */
    fun addAllowedSpecialCharacter(charToAdd: Char): Boolean {
        return configLock.write {
            allowedSpecialCharacters.add(charToAdd)
        }
    }

    /**
     * Removes a character from the allowed specific characters set.
     *
     * @param charToRemove The character to remove.
     * @return true if the character was removed. false if the character did not exist in the set.
     * @since 3.0
     * @see Set
     */
    fun removeAllowedSpecialCharacter(charToRemove: Char): Boolean {
        return configLock.write {
            allowedSpecialCharacters.remove(charToRemove)
        }
    }

    /**
     * Method to get a JSON Element from a specified path
     *
     * @param json      The object to search in
     * @param path      The path to get the element from. If this is blank,
     * it returns the entire object. If the path is malformed,
     * then it throws an [IllegalArgumentException]
     * @param allowNull Whether to allow null
     * @return The element at the specified path. Returns null if the element doesn't exist.
     * @throws IllegalArgumentException if the path is malformed
     * @since 2.0
     */
    private fun getElement(json: JsonObject, path: String, allowNull: Boolean): JsonElement? {
        configLock.read {
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
     * @return The element at the specified path. Returns null if the element doesn't exist.
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
     * @return The element at the specified path. Returns null if the element doesn't exist.
     * @throws IllegalArgumentException if the path is malformed
     * @since 2.0
     */
    fun getElement(path: String): JsonElement? {
        return getElement(this._internalObject, path)
    }

    /**
     * Sets an object at a specified path. Creates sub paths if they don't exist.
     *
     * @param path   The path to get the element from. If this is blank,
     * it sets the root object. If the path is malformed,
     * then it throws an [IllegalArgumentException]
     * @param `object` The object to set at the specified path
     * @throws IllegalArgumentException if the path is malformed
     * @since 2.0
     */
    operator fun set(path: String, obj: Any?) {
        configLock.write {
            val json: JsonObject = this._internalObject
            var root = json
            if (path.isEmpty()) {
                root = GSON.toJsonTree(obj).asJsonObject
            } else {
                verifyPath(path, pathPattern)
            }
            val subpaths = splitPattern.split(path)
            for (j in subpaths.indices) {
                if (root[subpaths[j]] == null || root[subpaths[j]].isJsonNull) {
                    root.add(subpaths[j], JsonObject())
                    if (j == subpaths.size - 1) {
                        root.add(subpaths[j], GSON.toJsonTree(obj))
                    } else {
                        root = root[subpaths[j]].asJsonObject
                    }
                } else {
                    if (j == subpaths.size - 1) {
                        root.add(subpaths[j], GSON.toJsonTree(obj))
                    } else {
                        root = root[subpaths[j]].asJsonObject
                    }
                }
                if (j == subpaths.size - 1) {
                    root = json
                }
            }
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
        configLock.write {
            val paths = splitPattern.split(path)
            check(getElement(path) != null) { "Element not present!" }
            if (paths.size == 1) {
                _internalObject.remove(path)
            } else {
                val subConfig = getSubConfig(path.substring(0, path.lastIndexOf(pathSeparator)))
                if (subConfig != null) {
                    subConfig.remove(path.substring(path.lastIndexOf(pathSeparator) + 1, path.length))
                } else {
                    throw IllegalStateException("Parent of path specified is not an object!")
                }
            }
        }
    }

    /**
     * Gets a string at the specified path
     *
     * @param path The path to get the string at.
     * @param forceConversion Whether to forcefully convert the element to a string or not. If this is false, this
     * method will return [IllegalStateException] for any element that isn't a JSON String.
     * @return The string value. If the value at the path does not exist then this returns null.
     * @throws IllegalArgumentException if the path is malformed.
     * @throws IllegalStateException    if the element at the path is not a string.
     * @see JSONConfig.getElement
     * @since 3.0
     */
    @JvmOverloads
    fun getString(path: String, default: String? = null, forceConversion: Boolean = false): String? {
        verifyPath(path, pathPattern)
        val element = getElement(path)
        return if (element == null) {
            default
        } else if (element.isJsonPrimitive && element.asJsonPrimitive.isString) {
            element.asString
        } else if (forceConversion) {
            element.toString()
        } else {
            throw IllegalStateException("The element at the path is not a string")
        }
    }

    /**
     * Gets a integer at the specified path
     *
     * @param path The path to get the integer at.
     * @return The integer value at this path. If the value at the path does not exist
     * then null is returned.
     * @throws IllegalArgumentException if the path is malformed.
     * @throws IllegalStateException    if the element at the path is not a number.
     * @see JSONConfig.getElement
     * @since 2.1
     */
    fun getInteger(path: String, default: Int? = null): Int? {
        verifyPath(path, pathPattern)
        val element = getElement(path)
        return if (element == null) {
            default
        } else if (element.isJsonPrimitive && element.asJsonPrimitive.isNumber) {
            element.asNumber.toInt()
        } else {
            throw IllegalStateException("The element at the path is not a number")
        }
    }

    /**
     * Gets a double at the specified path
     *
     * @param path The path to get the double at.
     * @return The double value at this path. If the value at the path does not exist
     * then null is returned.
     * @throws IllegalArgumentException if the path is malformed.
     * @throws IllegalStateException    if the element at the path is not a number.
     * @see JSONConfig.getElement
     * @since 2.2
     */
    fun getDouble(path: String, default: Double? = null): Double? {
        verifyPath(path, pathPattern)
        val element = getElement(path)
        return if (element == null) {
            default
        } else if (element.isJsonPrimitive && element.asJsonPrimitive.isNumber) {
            element.asNumber.toDouble()
        } else {
            throw IllegalStateException("The element at the path is not a number")
        }
    }

    /**
     * Gets a long at the specified path
     *
     * @param path The path to get the long at.
     * @return The long value at this path. If the value at the path does not exist
     * then null is returned.
     * @throws IllegalArgumentException if the path is malformed.
     * @throws IllegalStateException    if the element at the path is not a number.
     * @see JSONConfig.getElement
     * @since 2.2
     */
    fun getLong(path: String, default: Long? = null): Long? {
        verifyPath(path, pathPattern)
        val element = getElement(path)
        return if (element == null) {
            default
        } else if (element.isJsonPrimitive && element.asJsonPrimitive.isNumber) {
            element.asNumber.toLong()
        } else {
            throw IllegalStateException("The element at the path is not a number")
        }
    }

    /**
     * Gets a boolean at the specified path
     *
     * @param path The path to get the boolean at.
     * @return The boolean value at this path. If the value at the path does not exist
     * then null is returned.
     * @throws IllegalArgumentException if the path is malformed.
     * @throws IllegalStateException    if the element at the path is not a boolean.
     * @see JSONConfig.getElement
     * @since 2.2
     */
    fun getBoolean(path: String, default: Boolean? = null): Boolean? {
        verifyPath(path, pathPattern)
        val element = getElement(path)
        return if (element == null) {
            default
        } else if (element.isJsonPrimitive && element.asJsonPrimitive.isBoolean) {
            element.asBoolean
        } else {
            throw IllegalStateException("The element at the path is not a boolean")
        }
    }

    /**
     * Gets a JSON array at the specified path
     *
     * @param path The path to get the array at.
     * @return The array at this path. If the value at the path doesn't exist
     * then null is returned.
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
        configLock.read {
            for ((key, value) in this._internalObject.entrySet()) {
                output.add(key)
                if (value.isJsonObject && deep) {
                    mapChildrenKeys(key, output, value.asJsonObject, deep)
                }
            }
        }
    }

    protected fun mapChildrenKeys(prefix: String, output: MutableSet<String>, jsonObject: JsonObject, deep: Boolean) {
        configLock.read {
            for ((key, value) in jsonObject.entrySet()) {
                output.add(prefix + pathSeparator + key)
                if (value.isJsonObject && deep) {
                    mapChildrenKeys(prefix + pathSeparator + key, output, value.asJsonObject, deep)
                }
            }
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
        configLock.read {
            for ((key, value) in this._internalObject.entrySet()) {
                output[key] = value
                if (value.isJsonObject && deep) {
                    mapChildrenValues(key, output, value.asJsonObject, deep)
                }
            }
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

    override fun toString(): String = configLock.read { _internalObject.toString() }

    operator fun plus(other: JSONConfig): JSONConfig {
        return JSONConfig().apply {
            other._internalObject.entrySet().forEach { this[it.key] = it.value }
            this._internalObject.entrySet().forEach { this[it.key] = it.value }
        }
    }

    operator fun contains(key: String): Boolean = containsKey(key)

    fun containsKey(key: String): Boolean {
        return _internalObject.has(key)
    }

    fun containsValue(element: JsonElement): Boolean {
        return _internalObject.entrySet().find { it.value == element } != null
    }

    /**
     * Reloads the config.
     *
     * @throws UnsupportedOperationException If trying to reload from JsonObject
     * @throws FileNotFoundException         If the config file was moved/removed
     */
    @Throws(FileNotFoundException::class)
    fun reload() {
        configLock.write {
            when (mode) {
                MediaType.FILE -> {
                    val file = file as File
                    this._internalObject = GSON.fromJson(JsonReader(FileReader(file)), JsonObject::class.java)
                }

                MediaType.FILE_NAME -> {
                    val fileName = file as String
                    this._internalObject = GSON.fromJson(JsonReader(FileReader(fileName)), JsonObject::class.java)
                }

                MediaType.INPUT_STREAM -> {
                    val stream = file as InputStream
                    this._internalObject = GSON.fromJson(JsonReader(InputStreamReader(stream)), JsonObject::class.java)
                }

                MediaType.JSON_OBJECT -> throw UnsupportedOperationException("Cannot reload from a JsonObject")
            }
        }
    }

    companion object {
        private val GSON = GsonBuilder().serializeNulls().create()
    }
}