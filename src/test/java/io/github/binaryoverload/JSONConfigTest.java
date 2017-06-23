package io.github.binaryoverload;

import com.google.gson.JsonObject;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class JSONConfigTest {

    @Test
    public void testSet() {
        JSONConfig config = new JSONConfig(this.getClass().getClassLoader().getResourceAsStream("test.json"));
        config.set("glossary.title", "Hi there");
        assertTrue((config.getElement("glossary.title").getAsString()).equalsIgnoreCase("Hi there"));
    }

    @Test
    public void testGet() {
        JSONConfig config = new JSONConfig(this.getClass().getClassLoader().getResourceAsStream("test.json"));
        assertTrue((config.getElement("glossary.GlossDiv.title").getAsString()).equalsIgnoreCase("S"));
    }

    @Test
    public void testGetEmpty() {
        JSONConfig config = new JSONConfig(this.getClass().getClassLoader().getResourceAsStream("test.json"));
        assertTrue(config.getElement("").equals(config.getObject()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetMalformedPath() {
        JSONConfig config = new JSONConfig(this.getClass().getClassLoader().getResourceAsStream("test.json"));
        config.getElement("glossary.title...");
    }

}
