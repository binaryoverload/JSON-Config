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

package com.github.binaryoverload;

import com.google.gson.JsonObject;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class JSONConfigTest {

    private static JSONConfig config = new JSONConfig(JSONConfig.class.getClassLoader().getResourceAsStream("test.json"));

    @Test
    public void testSubConfigPositive() {
        assertNotNull(config.getSubConfig("items"));
        assertEquals("Product", config.getSubConfig("items").getString("title"));
    }

    @Test(expected = IllegalStateException.class)
    public void testSubConfigNegative() {
        assertNull(config.getSubConfig("blad.djh.dsjh"));
        config.getSubConfig("title");
    }

    @Test
    public void testSetPositive() {
        config.set("title", "Hi there");
        assertNotNull(config.getElement("title"));
        assertTrue(config.getElement("title").getAsString().equalsIgnoreCase("Hi there"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetNegative() {
        config.set("title...", "Hi");
    }

    @Test
    public void testGetPositive() {
        assertNotNull(config.getElement("type"));
        assertTrue(config.getElement("type").getAsJsonPrimitive().isString());
        assertTrue(config.getElement("type").getAsString().equalsIgnoreCase("array"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetNegative() {
        assertNull(config.getElement("blah"));
        config.getElement("blah...");
    }

    @Test
    public void testGetEmpty() {
        assertNotNull(config.getElement(""));
        assertEquals(config.getElement(""), config.getInternalObject());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetMalformedPath() {
        config.getElement("title...");
    }

    @Test
    public void testGetStringPositive() {
        assertNotNull(config.getString("items.title"));
        assertEquals("Product", config.getString("items.title"));
    }

    @Test(expected = IllegalStateException.class)
    public void testGetStringNegative() {
        assertNull(config.getString("items.blah"));
        config.getString("items");
    }

    @Test
    public void testGetIntegerPositive() {
        assertNotNull(config.getInteger("date"));
        assertEquals(10247893, (int) config.getInteger("date"));
    }

    @Test(expected = IllegalStateException.class)
    public void testGetIntegerNegative() {
        assertNull(config.getInteger("blah"));
        config.getInteger("title");
    }


    @Test
    public void testGetDoublePositive() {
        assertNotNull(config.getDouble("date"));
        assertEquals(10247893D, config.getDouble("date"), 0.0);
    }

    @Test(expected = IllegalStateException.class)
    public void testGetDoubleNegative() {
        assertNull(config.getDouble("blah"));
        config.getDouble("title");
    }

    @Test
    public void testGetLongPositive() {
        assertNotNull(config.getLong("date"));
        assertEquals(10247893L, config.getLong("date").longValue());
    }

    @Test(expected = IllegalStateException.class)
    public void testGetLongNegative() {
        assertNull(config.getLong("blah"));
        config.getLong("title");
    }

    @Test
    public void testGetBooleanPositive() {
        assertNotNull(config.getBoolean("items.properties.price.exclusiveMinimum"));
        assertTrue(config.getBoolean("items.properties.price.exclusiveMinimum"));
    }

    @Test(expected = IllegalStateException.class)
    public void testGetBooleanNegative() {
        assertNull(config.getBoolean("blah"));
        config.getBoolean("date");
    }

    @Test
    public void testGetArrayPositive() {
        assertNotNull(config.getArray("items.required"));
        assertTrue(config.getArray("items.required").get(0).getAsString().equalsIgnoreCase("id"));
    }

    @Test(expected = IllegalStateException.class)
    public void testGetArrayNegative() {
        assertNull(config.getArray("items.properties.blah"));
        config.getArray("items.properties");
    }

    @Test
    public void testGetKeysNotDeep() {
        Set<String> set = config.getKeys(false);
        String string = new BufferedReader(new InputStreamReader(this.getClass()
                .getClassLoader()
                .getResourceAsStream("key-notdeep.txt")))
                .lines()
                .collect(Collectors.joining("\n"));
        String compare = set.stream().collect(Collectors.joining("\n"));
        assertTrue(string.equals(compare));
    }

    @Test
    public void testGetKeysDeep() {
        Set<String> set = config.getKeys(true);
        String string = new BufferedReader(new InputStreamReader(this.getClass()
                .getClassLoader()
                .getResourceAsStream("key-deep.txt")))
                .lines()
                .collect(Collectors.joining("\n"));
        String compare = set.stream().collect(Collectors.joining("\n"));
        assertTrue(string.equals(compare));
    }

    @Test(expected = IllegalStateException.class)
    public void testRemoveNegative() {
        config.remove("items.properties.items");
    }

    @Test
    public void testRemovePositive() throws UnsupportedEncodingException {
        String object = config.getInternalObject().toString();
        config.remove("items.properties.id");
        assertNull(config.getElement("items.properties.id"));
        config = new JSONConfig(new ByteArrayInputStream(object.getBytes(StandardCharsets.UTF_8)));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testReloadUnsupported() throws FileNotFoundException {
        JSONConfig config = new JSONConfig();
        config.reload();
    }

    @Test
    public void testReloadFile() throws IOException, URISyntaxException {
        File file = new File(JSONConfig.class.getClassLoader().getResource("testfile.json").toURI());
        JSONConfig config = new JSONConfig(file);
        assertEquals(config.getString("title"), "Product set");

        try (FileWriter writer = new FileWriter(file)) {
            writer.write("{\"title\":\"Product jet\",\"type\":\"array\",\"date\":10247893,\"items\":{\"title\":\"Product\",\"type\":\"object\"}}");
        }

        config.reload();
        assertEquals(config.getString("title"), "Product jet");

        try (FileWriter writer = new FileWriter(file)) {
            writer.write("{\"title\":\"Product set\",\"type\":\"array\",\"date\":10247893,\"items\":{\"title\":\"Product\",\"type\":\"object\"}}");
        }

    }

}
