
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
package com.github.binaryoverload

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileWriter
import java.io.IOException
import java.io.InputStreamReader
import java.io.UnsupportedEncodingException
import java.net.URISyntaxException
import java.nio.charset.StandardCharsets
import java.util.stream.Collectors

class JSONConfigTest {

    companion object {
        private var config = JSONConfig(JSONConfig::class.java.classLoader.getResourceAsStream("test.json")!!)
    }

    @Test
    fun testSubConfigPositive() {
        assertNotNull(config.getSubConfig("items"))
        assertEquals("Product", config.getSubConfig("items")!!.getString("title"))
    }

    @Test(expected = IllegalStateException::class)
    fun testSubConfigNegative() {
        assertNull(config.getSubConfig("blad.djh.dsjh"))
        config.getSubConfig("title")
    }

    @Test
    fun testSetPositive() {
        config["title"] = "Hi there"
        assertNotNull(config.getElement("title"))
        assertTrue(config.getElement("title")!!.asString.equals("Hi there", ignoreCase = true))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testSetNegative() {
        config["title..."] = "Hi"
    }

    @Test
    fun testGetPositive() {
        assertNotNull(config.getElement("type"))
        assertTrue(config.getElement("type")!!.asJsonPrimitive.isString)
        assertTrue(config.getElement("type")!!.asString.equals("array", ignoreCase = true))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testGetNegative() {
        assertNull(config.getElement("blah"))
        config.getElement("blah...")
    }

    @Test
    fun testGetEmpty() {
        assertNotNull(config.getElement(""))
        assertEquals(config.getElement(""), config.getInternalObject())
    }

    @Test(expected = IllegalArgumentException::class)
    fun testGetMalformedPath() {
        config.getElement("title...")
    }

    @Test
    fun testGetStringPositive() {
        assertNotNull(config.getString("items.title"))
        assertEquals("Product", config.getString("items.title"))
    }

    @Test(expected = IllegalStateException::class)
    fun testGetStringNegative() {
        assertNull(config.getString("items.blah"))
        config.getString("items")
    }

    @Test
    fun testGetIntegerPositive() {
        assertNotNull(config.getInteger("date"))
        assertEquals(10247893, config.getInteger("date"))
    }

    @Test(expected = IllegalStateException::class)
    fun testGetIntegerNegative() {
        assertNull(config.getInteger("blah"))
        config.getInteger("title")
    }

    @Test
    fun testGetDoublePositive() {
        assertNotNull(config.getDouble("date"))
        assertEquals(10247893.0, config.getDouble("date")!!, 0.0)
    }

    @Test(expected = IllegalStateException::class)
    fun testGetDoubleNegative() {
        assertNull(config.getDouble("blah"))
        config.getDouble("title")
    }

    @Test
    fun testGetLongPositive() {
        assertNotNull(config.getLong("date"))
        assertEquals(10247893L, config.getLong("date")!!.toLong())
    }

    @Test(expected = IllegalStateException::class)
    fun testGetLongNegative() {
        assertNull(config.getLong("blah"))
        config.getLong("title")
    }

    @Test
    fun testGetBooleanPositive() {
        assertNotNull(config.getBoolean("items.properties.price.exclusiveMinimum"))
        assertTrue(config.getBoolean("items.properties.price.exclusiveMinimum")!!)
    }

    @Test(expected = IllegalStateException::class)
    fun testGetBooleanNegative() {
        assertNull(config.getBoolean("blah"))
        config.getBoolean("date")
    }

    @Test
    fun testGetArrayPositive() {
        assertNotNull(config.getArray("items.required"))
        assertTrue(config.getArray("items.required")!![0].asString.equals("id", ignoreCase = true))
    }

    @Test(expected = IllegalStateException::class)
    fun testGetArrayNegative() {
        assertNull(config.getArray("items.properties.blah"))
        config.getArray("items.properties")
    }

    @Test
    fun testGetKeysNotDeep() {
        val set = config.getKeys(false)
        val string = BufferedReader(InputStreamReader(this.javaClass
                .classLoader
                .getResourceAsStream("key-notdeep.txt")!!))
                .lines()
                .collect(Collectors.joining("\n"))
        val compare = set.stream().collect(Collectors.joining("\n"))
        assertTrue(string == compare)
    }

    @Test
    fun testGetKeysDeep() {
        val set = config.getKeys(true)
        val string = BufferedReader(InputStreamReader(this.javaClass
                .classLoader
                .getResourceAsStream("key-deep.txt")!!))
                .lines()
                .collect(Collectors.joining("\n"))
        val compare = set.stream().collect(Collectors.joining("\n"))
        assertTrue(string == compare)
    }

    @Test(expected = IllegalStateException::class)
    fun testRemoveNegative() {
        config.remove("items.properties.items")
    }

    @Test
    @Throws(UnsupportedEncodingException::class)
    fun testRemovePositive() {
        val `object` = config.getInternalObject().toString()
        config.remove("items.properties.id")
        assertNull(config.getElement("items.properties.id"))
        config = JSONConfig(ByteArrayInputStream(`object`.toByteArray(StandardCharsets.UTF_8)))
    }

    @Test(expected = UnsupportedOperationException::class)
    @Throws(FileNotFoundException::class)
    fun testReloadUnsupported() {
        val config = JSONConfig()
        config.reload()
    }

    @Test
    @Throws(IOException::class, URISyntaxException::class)
    fun testReloadFile() {
        val file = File(JSONConfig::class.java.classLoader.getResource("testfile.json")!!.toURI())
        val config = JSONConfig(file)
        assertEquals(config.getString("title"), "Product set")
        FileWriter(file).use { writer -> writer.write("{\"title\":\"Product jet\",\"type\":\"array\",\"date\":10247893,\"items\":{\"title\":\"Product\",\"type\":\"object\"}}") }
        config.reload()
        assertEquals(config.getString("title"), "Product jet")
        FileWriter(file).use { writer -> writer.write("{\"title\":\"Product set\",\"type\":\"array\",\"date\":10247893,\"items\":{\"title\":\"Product\",\"type\":\"object\"}}") }
    }
}