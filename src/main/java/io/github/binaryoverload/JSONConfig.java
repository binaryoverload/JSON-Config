package io.github.binaryoverload;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Main config class used to represent a json file
 * accessible by "paths"
 *
 * @author BinaryOverload
 * @version 1.0
 *
 */
public class JSONConfig {

    private JSONObject object;
    private String pathSeparator = ".";


    /**
     * Recommended constructor for most file based applications
     * <br>
     * <i>Uses the default path separator</i>
     *
     * @param file The file must exist and not be a directory
     * @throws FileNotFoundException if the file does not exist,
     *                   is a directory rather than a regular file,
     *                   or for some other reason cannot be opened for
     *                   reading.
     * @throws NullPointerException if the passed variable is null
     *
     * @see FileInputStream
     * @see File
     * @since 1.0
     */
    public JSONConfig(File file) throws FileNotFoundException, NullPointerException {
        Objects.requireNonNull(file);
        this.object = new JSONObject(new JSONTokener(new FileInputStream(file)));
    }

    /**
     * Constructor for use with file-based applications and specification
     * of a custom path separator
     *
     * @param file The file must exist and not be a directory
     * @param pathSeparator The separator to use for this config <i>This cannot be null, empty or any length other than 1</i>
     * @throws FileNotFoundException if the file does not exist,
     *                   is a directory rather than a regular file,
     *                   or for some other reason cannot be opened for
     *                   reading.
     * @throws NullPointerException if any of the passed variables are null
     * @throws IllegalArgumentException if the path separator is not empty or if it is any length other than 1
     *
     * @see FileInputStream
     * @see File
     * @since 1.0
     */
    public JSONConfig(File file, String pathSeparator) throws FileNotFoundException {
        Objects.requireNonNull(file);
        Objects.requireNonNull(pathSeparator);
        if (pathSeparator.isEmpty() || pathSeparator.length() != 1) {
            throw new IllegalArgumentException();
        }
        this.object = new JSONObject(new JSONTokener(new FileInputStream(file)));
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
        this.object = new JSONObject(new JSONTokener(stream));
    }

    /**
     * More advanced constructor allowing users to specify their own input stream
     *
     * @param stream The stream to be used for the JSON Object <i>This cannot be null</i>
     * @param pathSeparator The custom path separator to use for this config <i>This cannot be null, empty or any other lenth than 1</i>
     * @throws NullPointerException if any of the passed arguments are null
     * @throws IllegalArgumentException if the path separator is empty or not a length of 1
     * @see InputStream
     * @since 1.0
     */
    public JSONConfig(InputStream stream, String pathSeparator) {
        Objects.requireNonNull(stream);
        Objects.requireNonNull(pathSeparator);
        if (pathSeparator.isEmpty() || pathSeparator.length() != 1) {
            throw new IllegalArgumentException();
        }
        setPathSeparator(pathSeparator);
    }

    /**
     * Basic constructor that directly sets the JSONObject
     *
     * @param object The object to assign to the config <i>Cannot be null</i>
     * @throws NullPointerException if the object is null
     * @since 1.0
     * @see JSONObject
     */
    public JSONConfig(JSONObject object) {
        Objects.requireNonNull(object);
        this.object = object;
    }

    /**
     * Basic Constructor that sets a JSONObject as well as the path separator
     *
     * @param object The object to assign to the config <i>Cannot be null</i>
     * @param pathSeparator The object to assign to the config <i>Cannot be null, empty or any other length other than 1</i>
     * @throws NullPointerException if either of the passed arguments are null
     * @throws IllegalArgumentException if the path separator is empty or not a length of 1
     * @since 1.0
     */
    public JSONConfig(JSONObject object, String pathSeparator) {
        Objects.requireNonNull(object);
        this.object = object;
        if (pathSeparator.isEmpty() || pathSeparator.length() != 1) {
            throw new IllegalArgumentException();
        }
        setPathSeparator(pathSeparator);
    }

    public String getPathSeparator() {
        return pathSeparator;
    }

    public void setPathSeparator(String pathSeparator) {
        this.pathSeparator = pathSeparator;
    }

    public JSONObject getObject() {
        return object;
    }

    public void setObject(JSONObject object) {
        this.object = object;
    }
}
