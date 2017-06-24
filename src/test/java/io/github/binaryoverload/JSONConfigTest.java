package io.github.binaryoverload;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JSONConfigTest {

    private static JSONConfig config = new JSONConfig(JSONConfig.class.getClassLoader().getResourceAsStream("test.json"));

    @Test
    public void testSubConfigPositive() {
        assertTrue(config.getSubConfig("items").isPresent());
        assertTrue(config.getSubConfig("items").get().getString("title").get().equals("Product"));
    }

    @Test(expected = IllegalStateException.class)
    public void testSubConfigNegative() {
        assertFalse(config.getSubConfig("blad.djh.dsjh").isPresent());
        config.getSubConfig("title");
    }

    @Test
    public void testSetPositive() {
        config.set("title", "Hi there");
        assertTrue(config.getElement("title").isPresent());
        assertTrue(config.getElement("title").get().getAsString().equalsIgnoreCase("Hi there"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetNegative() {
        config.set("title...", "Hi");
    }

    @Test
    public void testGetPositive() {
        assertTrue(config.getElement("type").isPresent());
        assertTrue(config.getElement("type").get().getAsJsonPrimitive().isString());
        assertTrue(config.getElement("type").get().getAsString().equalsIgnoreCase("array"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetNegative() {
        assertFalse(config.getElement("blah").isPresent());
        config.getElement("blah...");
    }

    @Test
    public void testGetEmpty() {
        assertTrue(config.getElement("").isPresent());
        assertTrue(config.getElement("").get().equals(config.getObject()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetMalformedPath() {
        config.getElement("title...");
    }

    @Test
    public void testGetStringPositive() {
        assertTrue(config.getString("items.title").isPresent());
        assertTrue(config.getString("items.title").get().equals("Product"));
    }

    @Test(expected = IllegalStateException.class)
    public void testGetStringNegative() {
        assertFalse(config.getString("items.blah").isPresent());
        config.getString("items");
    }

    @Test
    public void testGetIntegerPositive() {
        assertTrue(config.getInteger("date").isPresent());
        assertTrue(config.getInteger("date").getAsInt() == 10247893);
    }

    @Test(expected = IllegalStateException.class)
    public void testGetIntegerNegative() {
        assertFalse(config.getInteger("blah").isPresent());
        config.getInteger("title");
    }


    @Test
    public void testGetDoublePositive() {
        assertTrue(config.getDouble("date").isPresent());
        assertTrue(config.getDouble("date").getAsDouble() == 10247893D);
    }

    @Test(expected = IllegalStateException.class)
    public void testGetDoubleNegative() {
        assertFalse(config.getDouble("blah").isPresent());
        config.getDouble("title");
    }

    @Test
    public void testGetLongPositive() {
        assertTrue(config.getLong("date").isPresent());
        assertTrue(config.getLong("date").getAsLong() == 10247893L);
    }

    @Test(expected = IllegalStateException.class)
    public void testGetLongNegative() {
        assertFalse(config.getLong("blah").isPresent());
        config.getLong("title");
    }

    @Test
    public void testGetBooleanPositive() {
        assertTrue(config.getBoolean("items.properties.price.exclusiveMinimum").isPresent());
        assertTrue(config.getBoolean("items.properties.price.exclusiveMinimum").get());
    }

    @Test(expected = IllegalStateException.class)
    public void testGetBooleanNegative() {
        assertFalse(config.getBoolean("blah").isPresent());
        config.getBoolean("date");
    }

    @Test
    public void testGetArrayPositive() {
        assertTrue(config.getArray("items.required").isPresent());
        assertTrue(config.getArray("items.required").get().get(0).getAsString().equalsIgnoreCase("id"));
    }

    @Test(expected = IllegalStateException.class)
    public void testGetArrayNegative() {
        assertFalse(config.getArray("items.properties.blah").isPresent());
        config.getArray("items.properties");
    }

}
