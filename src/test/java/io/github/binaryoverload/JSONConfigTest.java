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

package io.github.binaryoverload;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.*;
import java.net.URISyntaxException;
import org.junit.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
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
        String object = config.getObject().toString();
        config.remove("items.properties.id");
        assertTrue(!config.getElement("items.properties.id").isPresent());
        config = new JSONConfig(new ByteArrayInputStream(object.getBytes("UTF-8")));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testReloadUnsupported() throws FileNotFoundException {
        JSONConfig config = new JSONConfig(new JsonObject());
        config.reload();
    }

    @Test
    public void testReloadFile() throws IOException, URISyntaxException {
        File file = new File(JSONConfig.class.getClassLoader().getResource("testfile.json").toURI());
        JSONConfig config = new JSONConfig(file);
        assertEquals(config.getString("title").get(), "Product set");

        try (FileWriter writer = new FileWriter(file)) {
            writer.write("{\"title\":\"Product jet\",\"type\":\"array\",\"date\":10247893,\"items\":{\"title\":\"Product\",\"type\":\"object\"}}");
        }

        config.reload();
        assertEquals(config.getString("title").get(), "Product jet");

        try (FileWriter writer = new FileWriter(file)) {
            writer.write("{\"title\":\"Product set\",\"type\":\"array\",\"date\":10247893,\"items\":{\"title\":\"Product\",\"type\":\"object\"}}");
        }

    }

}
