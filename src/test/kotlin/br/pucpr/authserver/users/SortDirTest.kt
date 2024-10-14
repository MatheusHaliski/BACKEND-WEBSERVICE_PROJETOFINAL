package br.pucpr.authserver.users

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SortDirTest {

    @Test
    fun `getByName should return ASC if name is 'asc'`() {
        val result = SortDir.getByName("asc")
        assertEquals(SortDir.ASC, result, "Expected ASC for input 'asc'")
    }

    @Test
    fun `getByName should return DESC if name is 'desc'`() {
        val result = SortDir.getByName("desc")
        assertEquals(SortDir.DESC, result, "Expected DESC for input 'desc'")
    }

    @Test
    fun `getByName should return null if name is invalid`() {
        val result = SortDir.getByName("invalid")
        assertEquals(null, result, "Expected null for invalid input")
    }
    @Test
    fun `getByName should ignore case`() {
        val result = SortDir.getByName("asc")
        assertEquals(null, result, "Expected null for invalid input")
    }
}
