package io.github.binaryoverload;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.util.Arrays;
import java.util.Objects;
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
     * @see FileInputStream
     * @see File
     * @since 1.0
     */
    public JSONConfig(File file) throws FileNotFoundException, NullPointerException {
        Objects.requireNonNull(file);
        this.object = GSON.fromJson(new JsonReader(new FileReader(file)), JsonObject.class);
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
    public String getPathSeparator() {
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
    public void setPathSeparator(String pathSeparator) {
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
    public JsonObject getObject() {
        return object;
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
        this.object = object;
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
     * @return The element at the specified path <i>Returns null if the element doesn't exist</i>
     * @throws NullPointerException if the object specified is null
     * @throws IllegalArgumentException if the path is malformed
     * @since 2.0
     */
    public JsonElement getElement(JsonObject json, String path) {
        Objects.requireNonNull(json);
        if (path.isEmpty()) {
            return json;
        } else if (!path.matches("([A-z]+(\\.[A-z]+)*)+")) {
            throw new IllegalArgumentException("Malformed path");
        }
        String[] subpaths = path.split("\\.");
        for (int i = 0; i < subpaths.length; i++) {
            String subpath = subpaths[i];
            if (json.get(subpath) == null || json.get(subpath).isJsonNull()) {
                return null;
            } else if (json.get(subpath).isJsonObject()) {
                if (subpaths.length == 1 && subpaths[0].isEmpty()) {
                    return json;
                }
                return getElement(json.get(subpath).getAsJsonObject(),
                        Arrays.stream(subpaths).skip(i + 1).collect(Collectors.joining(".")));
            } else {
                return json.get(subpath);
            }
        }
        return null;
    }

    /**
     * The recommended method to get an element from the config
     *
     * @param path The path to get the element from. If this is blank,
     *             it returns the entire object. If the path is malformed,
     *             then it throws an {@link IllegalArgumentException}
     * @return The element at the specified path <i>Returns null if the element doesn't exist</i>
     * @throws IllegalArgumentException if the path is malformed
     */
    public JsonElement getElement(String path) {
        return getElement(this.object, path);
    }

    /**
     * Sets an object at a specified path. Creates sub paths if they don't exist.
     *
     * @param path The path to get the element from. If this is blank,
     *             it sets the root object. If the path is malformed,
     *             then it throws an {@link IllegalArgumentException}
     * @param object The object to set at the specified path
     * @throws IllegalArgumentException if the path is malformed
     * @throws NullPointerException if the path is null
     */
    public void set(String path, Object object) {

        JsonObject json = this.object;
        JsonObject root = json;
        if (path.isEmpty()) {
            root = GSON.toJsonTree(object).getAsJsonObject();
        } else if (!path.matches("([A-z]+(\\.[A-z]+)*)+")) {
            throw new IllegalArgumentException("Malformed path");
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

}
