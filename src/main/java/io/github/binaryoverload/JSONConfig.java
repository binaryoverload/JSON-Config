package io.github.binaryoverload;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Main config class used to represent a json file
 * accessible by "paths"
 *
 * @author BinaryOverload
 * @since 1.0
 */
public class JSONConfig {

    private JsonObject object;
    private String pathSeparator = ".";
    private static Gson GSON = new GsonBuilder().serializeNulls().create();


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
        Objects.requireNonNull(file);
        this.object = GSON.fromJson(new JsonReader(new FileReader(file)), JsonObject.class);
    }

    /**
     * Recommended constructor for most file based applications
     * <br>
     * <i>Uses the default path separator</i>
     *
     * @param fileName The file to get the config from
     * @throws FileNotFoundException if the file does not exist,
     *                               is a directory rather than a regular file,
     *                               or for some other reason cannot be opened for
     *                               reading.
     * @throws NullPointerException  if the passed variable is null
     * @throws IllegalArgumentException if the file name is empty
     * @see FileReader
     * @since 1.0
     */
    public JSONConfig(String fileName) throws FileNotFoundException, NullPointerException {
        Objects.requireNonNull(fileName);
        if (fileName.isEmpty()) {
            throw new IllegalArgumentException();
        }
        this.object = GSON.fromJson(new JsonReader(new FileReader(fileName)), JsonObject.class);
    }

    /**
     * Constructor for use with file-based applications and specification
     * of a custom path separator
     *
     * @param file          The file must exist and not be a directory
     * @param pathSeparator The separator to use for this config <i>This cannot be null, empty or
     *                      any length other than 1</i>
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
    public JSONConfig(File file, String pathSeparator) throws FileNotFoundException {
        Objects.requireNonNull(file);
        Objects.requireNonNull(pathSeparator);
        GeneralUtils.checkStringLength(pathSeparator, 1);
        this.object = GSON.fromJson(new JsonReader(new FileReader(file)), JsonObject.class);
        setPathSeparator(pathSeparator);
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
        Objects.requireNonNull(stream);
        this.object = GSON.fromJson(new JsonReader(new InputStreamReader(stream)),
                JsonObject.class);
    }

    /**
     * More advanced constructor allowing users to specify their own input stream
     *
     * @param stream        The stream to be used for the JSON Object <i>This cannot be null</i>
     * @param pathSeparator The custom path separator to use for this config <i>This cannot be
     *                      null, empty or any other lenth than 1</i>
     * @throws NullPointerException     if any of the passed arguments are null
     * @throws IllegalArgumentException if the path separator is empty or not a length of 1
     * @see InputStream
     * @since 1.0
     */
    public JSONConfig(InputStream stream, String pathSeparator) {
        Objects.requireNonNull(stream);
        Objects.requireNonNull(pathSeparator);
        GeneralUtils.checkStringLength(pathSeparator, 1);
        setPathSeparator(pathSeparator);
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
        Objects.requireNonNull(object);
        this.object = object;
    }

    /**
     * Basic Constructor that sets a JSONObject as well as the path separator
     *
     * @param object        The object to assign to the config <i>Cannot be null</i>
     * @param pathSeparator The path separator to be set <i>Cannot be null, empty or any length
     *                      other than 1</i>
     * @throws NullPointerException     if either of the passed arguments are null
     * @throws IllegalArgumentException if the path separator is empty or not a length of 1
     * @since 1.0
     */
    public JSONConfig(JsonObject object, String pathSeparator) {
        Objects.requireNonNull(object);
        this.object = object;
        GeneralUtils.checkStringLength(pathSeparator, 1);
        setPathSeparator(pathSeparator);
    }

    /**
     * Gets the path separator for this config
     *
     * @return The path separator
     * @since 1.0
     */
    public synchronized String getPathSeparator() {
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
    public synchronized void setPathSeparator(String pathSeparator) {
        Objects.requireNonNull(pathSeparator);
        GeneralUtils.checkStringLength(pathSeparator, 1);
        this.pathSeparator = pathSeparator;
    }

    /**
     * Gets the JSON object associated with this config
     *
     * @return The JSON Object associated with this config
     * @since 1.0
     */
    public synchronized JsonObject getObject() {
        return object;
    }

    /**
     * Sets the JSON object for this config
     *
     * @param object The object to be set <i>Cannot be null</i>
     * @throws NullPointerException if the object is null
     * @since 1.0
     */
    public synchronized void setObject(JsonObject object) {
        Objects.requireNonNull(object);
        this.object = object;
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
    public synchronized Optional<JSONConfig> getSubConfig(String path) {
        Objects.requireNonNull(path);
        GeneralUtils.verifyPath(path, pathSeparator);
        Optional<JsonElement> element = getElement(path);
        if (!element.isPresent()) {
            return Optional.empty();
        } else if (!element.get().isJsonObject()) {
            throw new IllegalStateException("The element at the specified path is not a JSON object");
        } else {
            return Optional.of(new JSONConfig(element.get().getAsJsonObject()));
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
    public synchronized Optional<JsonElement> getElement(JsonObject json, String path) {
        Objects.requireNonNull(json);
        if (path.isEmpty()) {
            return Optional.of(json);
        } else {
            GeneralUtils.verifyPath(path, this.pathSeparator);
        }
        String[] subpaths = path.split("\\.");
        for (int i = 0; i < subpaths.length; i++) {
            String subpath = subpaths[i];
            if (json.get(subpath) == null || json.get(subpath).isJsonNull()) {
                return Optional.empty();
            } else if (json.get(subpath).isJsonObject()) {
                if (subpaths.length == 1 && subpaths[0].isEmpty()) {
                    return Optional.of(json);
                }
                return getElement(json.get(subpath).getAsJsonObject(),
                        Arrays.stream(subpaths).skip(i + 1).collect(Collectors.joining(".")));
            } else {
                return Optional.of(json.get(subpath));
            }
        }
        return Optional.empty();
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
    public synchronized Optional<JsonElement> getElement(String path) {
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
    public synchronized void set(String path, Object object) {

        JsonObject json = this.object;
        JsonObject root = json;
        if (path.isEmpty()) {
            root = GSON.toJsonTree(object).getAsJsonObject();
        } else {
            GeneralUtils.verifyPath(path, this.pathSeparator);
        }
        String[] subpaths = path.split("\\.");
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
    public synchronized Optional<String> getString(String path) {
        GeneralUtils.verifyPath(path, pathSeparator);
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
    public synchronized OptionalInt getInteger(String path) {
        GeneralUtils.verifyPath(path, pathSeparator);
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
    public synchronized OptionalDouble getDouble(String path) {
        GeneralUtils.verifyPath(path, pathSeparator);
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
    public synchronized OptionalLong getLong(String path) {
        GeneralUtils.verifyPath(path, pathSeparator);
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
    public synchronized Optional<Boolean> getBoolean(String path) {
        GeneralUtils.verifyPath(path, pathSeparator);
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
    public synchronized Optional<JsonArray> getArray(String path) {
        GeneralUtils.verifyPath(path, pathSeparator);
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
    public synchronized Set<String> getKeys(boolean deep) {
        Set<String> set = new LinkedHashSet<>();
        mapChildrenKeys(set, this.object, deep);
        return set;
    }

    protected synchronized void mapChildrenKeys(Set<String> output, JsonObject object, boolean deep) {
        for (Map.Entry<String, JsonElement> entry : this.object.entrySet()) {
            output.add(entry.getKey());
            if (entry.getValue().isJsonObject() && deep) {
                mapChildrenKeys(entry.getKey(), output, entry.getValue().getAsJsonObject(), deep);
            }
        }
    }

    protected synchronized void mapChildrenKeys(String prefix, Set<String> output,
                                                JsonObject jsonObject, boolean deep) {
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            output.add(prefix + pathSeparator + entry.getKey());
            if (entry.getValue().isJsonObject() && deep) {
                mapChildrenKeys(prefix + pathSeparator + entry.getKey(), output, entry.getValue().getAsJsonObject(), deep);
            }
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
    public synchronized Map<String, Object> getValues(boolean deep) {
        Map<String, Object> map = new LinkedHashMap<>();
        mapChildrenValues(map, this.object, deep);
        return map;
    }

    protected synchronized void mapChildrenValues(Map<String, Object> output, JsonObject object, boolean deep) {
        for (Map.Entry<String, JsonElement> entry : this.object.entrySet()) {
            output.put(entry.getKey(), entry.getValue());
            if (entry.getValue().isJsonObject() && deep) {
                mapChildrenValues(entry.getKey(), output, entry.getValue().getAsJsonObject(), deep);
            }
        }
    }

    protected synchronized void mapChildrenValues(String prefix, Map<String, Object> output,
                                                  JsonObject jsonObject, boolean deep) {
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            output.put(prefix + pathSeparator + entry.getKey(), entry.getValue());
            if (entry.getValue().isJsonObject() && deep) {
                mapChildrenValues(prefix + pathSeparator + entry.getKey(), output, entry.getValue().getAsJsonObject(), deep);
            }
        }
    }

}
