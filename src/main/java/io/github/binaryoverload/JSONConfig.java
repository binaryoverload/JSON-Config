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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Main config class used to represent a json file
 * accessible by "paths"
 *
 * @author BinaryOverload
 * @since 1.0
 */
public class JSONConfig {

    private MediaType mode; // 0 = File, 1 = String, 2 = Input Stream, -1 = JsonObject
    private Object file;
    private JsonObject object;
    private char pathSeparator = '.';
    private static Gson GSON = new GsonBuilder().serializeNulls().create();
    private char[] allowedSpecialCharacters = new char[]{'-', '+', '_', '$'};

    private ReadWriteLock configLock = new ReentrantReadWriteLock();
    private Lock readLock = configLock.readLock();
    private Lock writeLock = configLock.writeLock();

    private Pattern pathPattern = GeneralUtils.generatePathPattern(pathSeparator, allowedSpecialCharacters);
    private Pattern splitPattern = Pattern.compile(Pattern.quote(String.valueOf(pathSeparator)));

    /**
     * Recommended constructor for most file based applications
     * <br>
     * <i>Uses the default path separator</i>
     *
     * @param file The file must exist and not be a directory
     * @throws FileNotFoundException if the file does not exist,
     *                               is a directory rather than a regular file,
     *                               or for some other reason cannot be opened for
     *                               reading.
     * @throws NullPointerException  if the passed variable is null
     * @see FileReader
     * @see File
     * @since 1.0
     */
    public JSONConfig(File file) throws FileNotFoundException, NullPointerException {
        this();
        Objects.requireNonNull(file);
        this.object = GSON.fromJson(new JsonReader(new FileReader(file)), JsonObject.class);
        Objects.requireNonNull(this.getObject(), "Input is empty!");
        this.mode = MediaType.FILE;
        this.file = file;
    }

    /**
     * Recommended constructor for most file based applications
     * <br>
     * <i>Uses the default path separator</i>
     *
     * @param fileName The file to get the config from
     * @throws FileNotFoundException    if the file does not exist,
     *                                  is a directory rather than a regular file,
     *                                  or for some other reason cannot be opened for
     *                                  reading.
     * @throws NullPointerException     if the passed variable is null
     * @throws IllegalArgumentException if the file name is empty
     * @see FileReader
     * @since 1.0
     */
    public JSONConfig(String fileName) throws FileNotFoundException, NullPointerException {
        this();
        Objects.requireNonNull(fileName);
        if (fileName.isEmpty()) {
            throw new IllegalArgumentException();
        }
        this.mode = MediaType.FILE_NAME;
        this.object = GSON.fromJson(new JsonReader(new FileReader(fileName)), JsonObject.class);
        Objects.requireNonNull(this.getObject(), "Input is empty!");
        this.file = fileName;
    }

    /**
     * Constructor for use with file-based applications and specification
     * of a custom path separator
     *
     * @param file                The file must exist and not be a directory
     * @param pathSeparator       The separator to use for this config <i>This cannot be null, empty or
     *                            any length other than 1</i>
     * @param allowedSpecialChars The allowed special characters which can be used on the path. This cannot conflict
     *                            with the path separator.
     * @throws FileNotFoundException    if the file does not exist,
     *                                  is a directory rather than a regular file,
     *                                  or for some other reason cannot be opened for
     *                                  reading.
     * @throws NullPointerException     if any of the passed variables are null
     * @throws IllegalArgumentException if the path separator is not empty or if it is any length
     *                                  other than 1
     * @see FileInputStream
     * @see File
     * @since 1.0
     */
    public JSONConfig(File file, char pathSeparator, char[] allowedSpecialChars) throws FileNotFoundException {
        this(allowedSpecialChars);
        Objects.requireNonNull(file);
        this.object = GSON.fromJson(new JsonReader(new FileReader(file)), JsonObject.class);
        Objects.requireNonNull(this.getObject(), "Input is empty!");
        setPathSeparator(pathSeparator);
        this.mode = MediaType.FILE;
        this.file = file;
    }


    /**
     * More advanced constructor allowing users to specify their own input stream
     *
     * @param stream The stream to be used for the JSON Object <i>This cannot be null</i>
     * @throws NullPointerException if the stream is null
     * @see InputStream
     * @since 1.0
     */
    public JSONConfig(InputStream stream) {
        this();
        Objects.requireNonNull(stream);
        this.object = GSON.fromJson(new JsonReader(new InputStreamReader(stream)), JsonObject.class);
        Objects.requireNonNull(this.getObject(), "Input is empty!");
        this.mode = MediaType.INPUT_STREAM;
        this.file = stream;
    }

    /**
     * More advanced constructor allowing users to specify their own input stream
     *
     * @param stream              The stream to be used for the JSON Object <i>This cannot be null</i>
     * @param pathSeparator       The custom path separator to use for this config <i>This cannot be
     *                            null, empty or any other lenth than 1</i>
     * @param allowedSpecialChars The allowed special characters which can be used on the path. This cannot conflict
     *                            with the path separator.
     * @throws NullPointerException     if any of the passed arguments are null
     * @throws IllegalArgumentException if the path separator is empty or not a length of 1
     * @throws IOException              if the stream is invalid or malformatted
     * @see InputStream
     * @since 1.0
     */
    public JSONConfig(InputStream stream, char pathSeparator, char[] allowedSpecialChars) throws IOException {
        this(allowedSpecialChars);
        Objects.requireNonNull(stream);
        setPathSeparator(pathSeparator);
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        this.object = GSON.fromJson(br.lines().collect(Collectors.joining()), JsonObject.class);
        br.close();
        Objects.requireNonNull(this.getObject(), "Input is empty!");
        this.mode = MediaType.INPUT_STREAM;
        this.file = stream;
    }

    /**
     * Basic constructor that directly sets the JSONObject
     *
     * @param object The object to assign to the config <i>Cannot be null</i>
     * @throws NullPointerException if the object is null
     * @see JsonObject
     * @since 1.0
     */
    public JSONConfig(JsonObject object) {
        this();
        Objects.requireNonNull(object, "Input is empty!");
        this.object = object;
        mode = MediaType.JSON_OBJECT;
    }

    /**
     * Basic Constructor that sets a JSONObject as well as the path separator
     *
     * @param object              The object to assign to the config <i>Cannot be null</i>
     * @param pathSeparator       The path separator to be set <i>Cannot be null, empty or any length
     *                            other than 1</i>
     * @param allowedSpecialChars The allowed special characters which can be used on the path. This cannot conflict
     *                            with the path separator.
     * @throws NullPointerException     if either of the passed arguments are null
     * @throws IllegalArgumentException if the path separator is empty or not a length of 1
     * @since 1.0
     */
    public JSONConfig(JsonObject object, char pathSeparator, char[] allowedSpecialChars) {
        this(allowedSpecialChars);
        Objects.requireNonNull(object, "Input is empty!");
        this.object = object;
        setPathSeparator(pathSeparator);
        this.mode = MediaType.JSON_OBJECT;
    }

    private JSONConfig(char[] allowedSpecialCharacters) {
        this.allowedSpecialCharacters = allowedSpecialCharacters;
    }

    private JSONConfig() {}

    /**
     * Gets the path separator for this config
     *
     * @return The path separator
     * @since 1.0
     */
    public char getPathSeparator() {
        return pathSeparator;
    }

    /**
     * Sets the path separator for this config
     *
     * @param pathSeparator The path separator to be set <i>Cannot be null, empty or any length
     *                      other than 1</i>
     * @throws NullPointerException     if the path separator provided is null
     * @throws IllegalArgumentException if the path separator is empty of any length other than 1
     * @since 1.0
     */
    public void setPathSeparator(char pathSeparator) {
        writeLock.lock();
        try {
            this.pathSeparator = pathSeparator;
            for (char allowedChar : allowedSpecialCharacters) {
                if (allowedChar == pathSeparator) {
                    throw new IllegalArgumentException("Cannot set path separator to an allowed special character!");
                }
            }

            // Recompile the pattern on a new path separator.
            this.pathPattern = GeneralUtils.generatePathPattern(pathSeparator, allowedSpecialCharacters);
            this.splitPattern = Pattern.compile(Pattern.quote(String.valueOf(pathSeparator)));
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Gets the JSON object associated with this config
     *
     * @return The JSON Object associated with this config
     * @since 1.0
     */
    public JsonObject getObject() {
        readLock.lock();
        try {
            return object.deepCopy();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Sets the JSON object for this config
     *
     * @param object The object to be set <i>Cannot be null</i>
     * @throws NullPointerException if the object is null
     * @since 1.0
     */
    public void setObject(JsonObject object) {
        Objects.requireNonNull(object);
        writeLock.lock();
        try {
            this.object = object;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Returns a new config with its root object set to the object
     * retrieved from the specified path
     *
     * @param path The path to get the new config from <i>Cannot be null</i>
     * @return The JSONConfig wrapped in an {@link Optional}. The optional is empty if the element
     * at the path is non-existent
     * @throws NullPointerException  if the path is null
     * @throws IllegalStateException if the element at the path is not a JSON object
     * @since 2.3
     */
    public Optional<JSONConfig> getSubConfig(String path) {
        Objects.requireNonNull(path);
        readLock.lock();
        try {
            Optional<JsonElement> element = getElement(path);
            if (!element.isPresent()) {
                return Optional.empty();
            } else if (!element.get().isJsonObject()) {
                throw new IllegalStateException("The element at the specified path is not a JSON object");
            } else {
                return Optional.of(new JSONConfig(element.get().getAsJsonObject(), this.pathSeparator, this.allowedSpecialCharacters));
            }
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Gets the list of chars that are allowed in a path
     *
     * @return List of allowed special characters
     */
    public char[] getAllowedSpecialCharacters() {
        readLock.lock();
        try {
            return this.allowedSpecialCharacters.clone();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Sets the list of special characters allowed
     *
     * @param allowedSpecialCharacters The list of special characters to be allowed
     */
    public void setAllowedSpecialCharacters(char[] allowedSpecialCharacters) {
        writeLock.lock();
        try {
            this.allowedSpecialCharacters = allowedSpecialCharacters.clone();
            this.pathPattern = GeneralUtils.generatePathPattern(pathSeparator, allowedSpecialCharacters);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Adds a character to the allowed special characters list
     *
     * @param charToAdd The character to add to the allowed special characters list
     */
    public void addAllowedSpecialCharacter(char charToAdd) {
        writeLock.lock();
        try {
            for (char c : allowedSpecialCharacters) {
                if (c == charToAdd) return;
            }
            char[] newArray = Arrays.copyOf(allowedSpecialCharacters, allowedSpecialCharacters.length + 1);
            newArray[newArray.length - 1] = charToAdd;
            this.allowedSpecialCharacters = newArray;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Method to get a JSON Element from a specified path
     * <p>
     * <strong>It is not recommended to use this method! Use
     * {@link io.github.binaryoverload.JSONConfig#getElement(String)} instead!</strong>
     *
     * @param json      The object to search in
     * @param path      The path to get the element from. If this is blank,
     *                  it returns the entire object. If the path is malformed,
     *                  then it throws an {@link IllegalArgumentException}
     * @param allowNull Whether to allow null
     * @return The element at the specified path <i>Returns an empty optional if the element
     * doesn't exist</i>
     * @throws NullPointerException     if the object specified is null
     * @throws IllegalArgumentException if the path is malformed
     * @since 2.0
     */
    public Optional<JsonElement> getElement(JsonObject json, String path, boolean allowNull) {
        readLock.lock();
        try {
            Objects.requireNonNull(json);
            if (path.isEmpty()) {
                return Optional.of(json);
            } else {
                GeneralUtils.verifyPath(path, pathPattern);
            }
            String[] subpaths = splitPattern.split(path);
            String subpath = subpaths[0];
            if (json.get(subpath) == null || json.get(subpath).isJsonNull()) {
                if (allowNull) {
                    return Optional.of(JsonNull.INSTANCE);
                }
                return Optional.empty();
            } else if (json.get(subpath).isJsonObject()) {
                if (subpaths.length == 1 && subpaths[0].isEmpty()) {
                    return Optional.of(json);
                }
                return getElement(json.get(subpath).getAsJsonObject(), Arrays.stream(subpaths).skip(1).collect(Collectors.joining(String.valueOf(pathSeparator))));
            } else {
                return Optional.of(json.get(subpath));
            }
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Method to get a JSON Element from a specified path
     * <p>
     * <strong>It is not recommended to use this method! Use
     * {@link io.github.binaryoverload.JSONConfig#getElement(String)} instead!</strong>
     *
     * @param json The object to search in
     * @param path The path to get the element from. If this is blank,
     *             it returns the entire object. If the path is malformed,
     *             then it throws an {@link IllegalArgumentException}
     * @return The element at the specified path <i>Returns an empty optional if the element
     * doesn't exist</i>
     * @throws NullPointerException     if the object specified is null
     * @throws IllegalArgumentException if the path is malformed
     * @since 2.0
     */
    public Optional<JsonElement> getElement(JsonObject json, String path) {
        return getElement(json, path, false);
    }

    /**
     * The recommended method to get an element from the config
     *
     * @param path The path to get the element from. If this is blank,
     *             it returns the entire object. If the path is malformed,
     *             then it throws an {@link IllegalArgumentException}
     * @return The element at the specified path <i>Returns an empty optional if the element
     * doesn't exist</i>
     * @throws IllegalArgumentException if the path is malformed
     * @since 2.0
     */
    public Optional<JsonElement> getElement(String path) {
        return getElement(this.object, path);
    }

    /**
     * Sets an object at a specified path. Creates sub paths if they don't exist.
     *
     * @param path   The path to get the element from. If this is blank,
     *               it sets the root object. If the path is malformed,
     *               then it throws an {@link IllegalArgumentException}
     * @param object The object to set at the specified path
     * @throws IllegalArgumentException if the path is malformed
     * @throws NullPointerException     if the path is null
     * @since 2.0
     */
    public void set(String path, Object object) {
        writeLock.lock();
        try {
            JsonObject json = this.object;
            JsonObject root = json;
            if (path.isEmpty()) {
                root = GSON.toJsonTree(object).getAsJsonObject();
            } else {
                GeneralUtils.verifyPath(path, pathPattern);
            }
            String[] subpaths = splitPattern.split(path);
            for (int j = 0; j < subpaths.length; j++) {
                if (root.get(subpaths[j]) == null || root.get(subpaths[j]).isJsonNull()) {
                    root.add(subpaths[j], new JsonObject());
                    if (j == subpaths.length - 1) {
                        root.add(subpaths[j], GSON.toJsonTree(object));
                    } else {
                        root = root.get(subpaths[j]).getAsJsonObject();
                    }
                } else {
                    if (j == subpaths.length - 1) {
                        root.add(subpaths[j], GSON.toJsonTree(object));
                    } else {
                        root = root.get(subpaths[j]).getAsJsonObject();
                    }
                }
                if (j == subpaths.length - 1) {
                    root = json;
                }
            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Removes an element from the path
     *
     * @param path The path to remove
     * @throws IllegalStateException if the element at the path is not present
     * @throws IllegalStateException if the parent of the specified path is not an object
     */
    public void remove(String path) {
        writeLock.lock();
        try {
            String[] paths = splitPattern.split(path);
            if (!getElement(path).isPresent()) {
                throw new IllegalStateException("Element not present!");
            }
            if (paths.length == 1) {
                object.remove(path);
            } else {
                Optional<JSONConfig> subConfig = getSubConfig(path.substring(0, path.lastIndexOf(pathSeparator)));
                if (subConfig.isPresent()) {
                    subConfig.get().remove(path.substring(path.lastIndexOf(pathSeparator) + 1, path.length()));
                    subConfig = subConfig;
                } else {
                    throw new IllegalStateException("Parent of path specified is not an object!");
                }
            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Gets a string at the specified path
     *
     * @param path The path to get the string at <i>Must not be null</i>
     * @return An optional containing the string value. If the value at the path does not exist
     * optional is empty
     * @throws NullPointerException     if the path is null
     * @throws IllegalArgumentException if the path is malformed
     * @throws IllegalStateException    if the element at the path is not a string
     * @see JSONConfig#getElement(String)
     * @since 2.1
     */
    public Optional<String> getString(String path) {
        GeneralUtils.verifyPath(path, pathPattern);
        if (!getElement(path).isPresent()) {
            return Optional.empty();
        } else if (getElement(path).get().isJsonPrimitive() && getElement(path).get().getAsJsonPrimitive().isString()) {
            return Optional.of(getElement(path).get().getAsString());
        } else {
            throw new IllegalStateException("The element at the path is not a string");
        }
    }

    /**
     * Gets a integer at the specified path
     *
     * @param path The path to get the integer at <i>Must not be null</i>
     * @return An optional containing the integer value. If the value at the path does not exist
     * then the optional is empty
     * @throws NullPointerException     if the path is null
     * @throws IllegalArgumentException if the path is malformed
     * @throws IllegalStateException    if the element at the path is not a number
     * @see JSONConfig#getElement(String)
     * @since 2.1
     */
    public OptionalInt getInteger(String path) {
        GeneralUtils.verifyPath(path, pathPattern);
        if (!getElement(path).isPresent()) {
            return OptionalInt.empty();
        } else if (getElement(path).get().isJsonPrimitive() && getElement(path).get().getAsJsonPrimitive().isNumber()) {
            return OptionalInt.of(getElement(path).get().getAsNumber().intValue());
        } else {
            throw new IllegalStateException("The element at the path is not a number");
        }
    }

    /**
     * Gets a double at the specified path
     *
     * @param path The path to get the double at <i>Must not</i>
     * @return An optional containing the double value. If the value at the path does not exist
     * then the optional is empty
     * @throws NullPointerException     if the path is null
     * @throws IllegalArgumentException if the path is malformed
     * @throws IllegalStateException    if the element at the path is not a number
     * @see JSONConfig#getElement(String)
     * @since 2.2
     */
    public OptionalDouble getDouble(String path) {
        GeneralUtils.verifyPath(path, pathPattern);
        if (!getElement(path).isPresent()) {
            return OptionalDouble.empty();
        } else if (getElement(path).get().isJsonPrimitive() && getElement(path).get().getAsJsonPrimitive().isNumber()) {
            return OptionalDouble.of(getElement(path).get().getAsNumber().doubleValue());
        } else {
            throw new IllegalStateException("The element at the path is not a number");
        }
    }

    /**
     * Gets a long at the specified path
     *
     * @param path The path to get the long at <i>Must not be null</i>
     * @return An optional containing the long value. If the value at the path does not exist
     * then the optional is empty
     * @throws NullPointerException     if the path is null
     * @throws IllegalArgumentException if the path is malformed
     * @throws IllegalStateException    if the element at the path is not a number
     * @see JSONConfig#getElement(String)
     * @since 2.2
     */
    public OptionalLong getLong(String path) {
        GeneralUtils.verifyPath(path, pathPattern);
        if (!getElement(path).isPresent()) {
            return OptionalLong.empty();
        } else if (getElement(path).get().isJsonPrimitive() && getElement(path).get().getAsJsonPrimitive().isNumber()) {
            return OptionalLong.of(getElement(path).get().getAsNumber().longValue());
        } else {
            throw new IllegalStateException("The element at the path is not a number");
        }
    }

    /**
     * Gets a boolean at the specified path
     *
     * @param path The path to get the boolean at <i>Must not be null</i>
     * @return An optional containing the double value. If the value at the path does not exist
     * then the optional is empty
     * @throws NullPointerException     if the path is null
     * @throws IllegalArgumentException if the path is malformed
     * @throws IllegalStateException    if the element at the path is not a boolean
     * @see JSONConfig#getElement(String)
     * @since 2.2
     */
    public Optional<Boolean> getBoolean(String path) {
        GeneralUtils.verifyPath(path, pathPattern);
        if (!getElement(path).isPresent()) {
            return Optional.empty();
        } else if (getElement(path).get().isJsonPrimitive() && getElement(path).get().getAsJsonPrimitive().isBoolean()) {
            return Optional.of(getElement(path).get().getAsBoolean());
        } else {
            throw new IllegalStateException("The element at the path is not a boolean");
        }
    }

    /**
     * Gets a JSON array at the specified path
     *
     * @param path The path to get the array at <i>Must not be null</i>
     * @return An optional containing the array. If the value at the path doesn't exist
     * than the optional is empty
     * @throws NullPointerException     if the path is null
     * @throws IllegalArgumentException if the path is malformed
     * @throws IllegalStateException    if the element at the path is not an array
     * @see JSONConfig#getElement(String)
     * @since 2.5
     */
    public Optional<JsonArray> getArray(String path) {
        GeneralUtils.verifyPath(path, pathPattern);
        if (!getElement(path).isPresent()) {
            return Optional.empty();
        } else if (getElement(path).get().isJsonArray()) {
            return Optional.of(getElement(path).get().getAsJsonArray());
        } else {
            throw new IllegalStateException("The element at the path is not an array");
        }
    }

    /**
     * Gets a list of keys in this config
     * If {@code deep} is true then it will descend into all subpaths
     * using the path to the keys as the key itself
     *
     * @param deep Whether to transverse the entire config tree or not
     * @return A {@link LinkedHashSet} containing all the keys. If the config is empty
     * then so will this list be
     */
    public Set<String> getKeys(boolean deep) {
        Set<String> set = new LinkedHashSet<>();
        mapChildrenKeys(set, deep);
        return set;
    }

    protected void mapChildrenKeys(Set<String> output, boolean deep) {
        readLock.lock();
        try {
            for (Map.Entry<String, JsonElement> entry : this.object.entrySet()) {
                output.add(entry.getKey());
                if (entry.getValue().isJsonObject() && deep) {
                    mapChildrenKeys(entry.getKey(), output, entry.getValue().getAsJsonObject(), deep);
                }
            }
        } finally {
            readLock.unlock();
        }
    }

    protected void mapChildrenKeys(String prefix, Set<String> output, JsonObject jsonObject, boolean deep) {
        readLock.lock();
        try {
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                output.add(prefix + pathSeparator + entry.getKey());
                if (entry.getValue().isJsonObject() && deep) {
                    mapChildrenKeys(prefix + pathSeparator + entry.getKey(), output, entry.getValue().getAsJsonObject(), deep);
                }
            }
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Gets a map of keys and values in this config
     * If {@code deep} is true then it will descend into all subpaths
     * using the path to the keys as the key itself
     *
     * @param deep Whether to transverse the entire config tree or not
     * @return A {@link LinkedHashMap} containing all the keys and values. If the config is empty
     * then so will this list be
     */
    public Map<String, Object> getValues(boolean deep) {
        Map<String, Object> map = new LinkedHashMap<>();
        mapChildrenValues(map, deep);
        return map;
    }

    protected void mapChildrenValues(Map<String, Object> output, boolean deep) {
        readLock.lock();
        try {
            for (Map.Entry<String, JsonElement> entry : this.object.entrySet()) {
                output.put(entry.getKey(), entry.getValue());
                if (entry.getValue().isJsonObject() && deep) {
                    mapChildrenValues(entry.getKey(), output, entry.getValue().getAsJsonObject(), deep);
                }
            }
        } finally {
            readLock.unlock();
        }
    }

    protected void mapChildrenValues(String prefix, Map<String, Object> output, JsonObject jsonObject, boolean deep) {
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            output.put(prefix + pathSeparator + entry.getKey(), entry.getValue());
            if (entry.getValue().isJsonObject() && deep) {
                mapChildrenValues(prefix + pathSeparator + entry.getKey(), output, entry.getValue().getAsJsonObject(), deep);
            }
        }
    }

    @Override
    public String toString() {
        readLock.lock();
        try {
            return object.toString();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Reloads the config.
     *
     * @throws UnsupportedOperationException If trying to reload from JsonObject
     * @throws IllegalStateException         If somehow the mode is invalid
     * @throws FileNotFoundException         If the config file was moved/removed
     */
    public void reload() throws FileNotFoundException {
        writeLock.lock();
        try {
            switch (mode) {
                case FILE:
                    File file = (File) this.file;
                    this.object = GSON.fromJson(new JsonReader(new FileReader(file)), JsonObject.class);
                    break;
                case FILE_NAME:
                    String fileName = (String) this.file;
                    this.object = GSON.fromJson(new JsonReader(new FileReader(fileName)), JsonObject.class);
                    break;
                case INPUT_STREAM:
                    InputStream stream = (InputStream) this.file;
                    this.object = GSON.fromJson(new JsonReader(new InputStreamReader(stream)), JsonObject.class);
                    break;
                case JSON_OBJECT:
                    throw new UnsupportedOperationException("Cannot reload from a JsonObject");
                default:
                    throw new IllegalStateException("Invalid mode");
            }
        } finally {
            writeLock.unlock();
        }
    }

}
