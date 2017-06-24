package io.github.binaryoverload;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
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
        assertFalse(config.getString("items.properties").isPresent());
        assertFalse(config.getString("items.required").isPresent());
    }

    @Test
    public void testGetInteger() {
        assertTrue(config.getInteger("date").isPresent());
        assertFalse(config.getInteger("title").isPresent());
        assertFalse(config.getInteger("items").isPresent());
    }


    @Test
    public void testGetDouble() {
        assertTrue(config.getDouble("items.properties.price.minimum").isPresent());
        assertTrue(config.getDouble("date").isPresent());
        assertFalse(config.getDouble("items.title").isPresent());
    }

    @Test
    public void testGetLong() {
        assertTrue(config.getLong("items.properties.price.minimum").isPresent());
        assertTrue(config.getLong("date").isPresent());
        assertFalse(config.getLong("items.title").isPresent());
    }

    @Test
    public void testGetBoolean() {
        assertTrue(config.getBoolean("items.properties.price.exclusiveMinimum").isPresent());
        assertFalse(config.getBoolean("date").isPresent());
        assertFalse(config.getBoolean("items.title").isPresent());
    }

}
