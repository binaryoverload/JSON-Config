package io.github.binaryoverload;

import com.google.gson.JsonObject;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class JSONConfigTest {

    public static JSONConfig config = new JSONConfig(JSONConfig.class.getClassLoader().getResourceAsStream("test.json"));

    @Test
    public void testSet() {
        config.set("glossary.title", "Hi there");
        assertTrue((config.getElement("glossary.title").getAsString()).equalsIgnoreCase("Hi there"));
    }

    @Test
    public void testGet() {
        assertTrue((config.getElement("glossary.GlossDiv.title").getAsString()).equalsIgnoreCase("S"));
    }

    @Test
    public void testGetEmpty() {
        assertTrue(config.getElement("").equals(config.getObject()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetMalformedPath() {
        config.getElement("glossary.title...");
    }

    @Test
    public void testGetString() {
        assertTrue(config.getString("glossary.title").isPresent());
        assertTrue(!config.getString("glossary.GlossDiv").isPresent());
    }

}
