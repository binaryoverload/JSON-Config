package com.github.binaryoverload

import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

class UtilsTest {

    @Rule
    @JvmField
    var thrown: ExpectedException = ExpectedException.none()

    @Test
    fun checkStringLengthPositive() {
        checkStringLength("", 0, false)
        checkStringLength("s", 1)
        checkStringLength("ss", 2)
    }

    @Test
    fun checkStringLengthNegative() {
        thrown.expect(IllegalArgumentException::class.java)
        checkStringLength("", 0, true)
        checkStringLength("sss", 1)
        checkStringLength("sss", 2)
    }

    @Test
    fun verifyPathPositive() {
        verifyPath("cat.cat.cat", '.', setOf())
        verifyPath("ca-t.cat.cat", '.', setOf('-'))
        verifyPath("cat_cat.cat", '.', setOf('_'))
        verifyPath("cat-cat-cat", '-', setOf())
    }

    @Test(expected = IllegalArgumentException::class)
    fun verifyPathNegative() {
        verifyPath("cat.cat.cat..", '.', setOf())
        verifyPath("cat-cat-cat-", '-', setOf())
    }

    @Test(expected = IllegalArgumentException::class)
    fun verifySpecialCharacters() {
        verifyPath("cat.cat", '.', setOf('-', '.', '_'))
        verifyPath("cat-cat.cat", '-', setOf('-', '.', '_'))
    }
}