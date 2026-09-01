package com.r2h_widget.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FavoritesRepositoryTest {
    private val repository = InMemoryFavoritesRepository(setOf("clock-01", "clock-02"))

    @Test
    fun addAndRemoveFavorite_updatesFlow() = runBlocking {
        repository.setFavorite("clock-01", true)
        assertEquals(setOf("clock-01"), repository.favoriteIds.first())

        repository.setFavorite("clock-01", false)
        assertTrue(repository.favoriteIds.first().isEmpty())
    }

    @Test
    fun duplicateFavorite_doesNotCreateDuplicateIds() = runBlocking {
        repository.setFavorite("clock-01", true)
        repository.setFavorite("clock-01", true)

        assertEquals(setOf("clock-01"), repository.favoriteIds.first())
    }

    @Test
    fun unknownFavorite_isIgnored() = runBlocking {
        repository.setFavorite("missing", true)

        assertTrue(repository.favoriteIds.first().isEmpty())
    }
}
