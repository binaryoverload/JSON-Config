package io.github.binaryoverload;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by william on 23/06/17.
 */
public class JSONConfigTest {

    @Test
    public void testSet() {
        JSONConfig config = new JSONConfig(this.getClass().getClassLoader().getResourceAsStream("test.json"));
        config.set("glossary.title", "Hi there");
        assertTrue(((String) config.getObject("glossary.title")).equalsIgnoreCase("Hi there"));
    }
}
