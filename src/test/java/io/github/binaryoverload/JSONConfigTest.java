package io.github.binaryoverload;

import com.google.gson.JsonObject;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class JSONConfigTest {

    private static JSONConfig config = new JSONConfig(JSONConfig.class.getClassLoader().getResourceAsStream("test.json"));

    @Test
    public void testSet() {
        config.set("title", "Hi there");
        assertTrue((config.getElement("title").getAsString()).equalsIgnoreCase("Hi there"));
    }

    @Test
    public void testGet() {
        assertTrue(config.getString("type").isPresent());
        assertTrue(config.getString("type").get().equalsIgnoreCase("array"));
    }

    @Test
    public void testGetEmpty() {
        assertTrue(config.getElement("").equals(config.getObject()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetMalformedPath() {
        config.getElement("title...");
    }

    @Test
    public void testGetString() {
        assertTrue(config.getString("items.title").isPresent());
        assertTrue(!config.getString("items.properties").isPresent());
        assertTrue(!config.getString("items.required").isPresent());
    }

    @Test
    public void testGetInteger() {
        assertTrue(config.getInteger("date").isPresent());
        assertTrue(!config.getInteger("title").isPresent());
        assertTrue(!config.getInteger("items").isPresent());
    }


    @Test
    public void testGetDouble() {
        assertTrue(config.getDouble("items.properties.price.minimum").isPresent());
        assertTrue(config.getDouble("date").isPresent());
        assertTrue(!config.getDouble("items.title").isPresent());
    }

    @Test
    public void testGetLong() {
        assertTrue(config.getLong("items.properties.price.minimum").isPresent());
        assertTrue(config.getLong("date").isPresent());
        assertTrue(!config.getLong("items.title").isPresent());
    }

    @Test
    public void testGetBoolean() {
        assertTrue(config.getBoolean("items.properties.price.exclusiveMinimum").isPresent());
        assertTrue(!config.getBoolean("date").isPresent());
        assertTrue(!config.getBoolean("items.title").isPresent());
    }

}
