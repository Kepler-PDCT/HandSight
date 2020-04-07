package com.example.handsight

import logic.ImitationGame
import logic.GuessingGame
import org.hamcrest.CoreMatchers.hasItem
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

class ImitationGameUnitTest {
    @Test
    fun initialize_values() {
        val game = ImitationGame()
        assertEquals(false, game.finished)
        assertEquals(false, game.finished)
        assertEquals(0, game.score)
        assertEquals(1, game.count)
        assertEquals(10, game.numberOfQuestions)
    }

    @Test
    fun testCorrect() {
        val game = ImitationGame()
        assertEquals(true, game.isCorrect(game.getQuestion().correctAnswer))
    }

    @Test
    fun testAdvancement() {
        val game = ImitationGame()

        for (i in 1..10) {
            val questionObj = game.getQuestion()
            game.makeGuess(questionObj.correctAnswer)
            if(i < 10) {
                assertEquals(false, game.finished)
                assertEquals(5*i, game.score)
                assertEquals(i+1, game.count)
            }
        }

        assertEquals(true, game.finished)
    }

}

class GuessingGameUnitTest {
    @Test
    fun initialize_values() {
        val game = GuessingGame()
        assertEquals(false, game.finished)
        assertEquals(0, game.score)
        assertEquals(1, game.count)
        assertEquals(10, game.numberOfQuestions)
    }

    @Test
    fun answer_exist_in_alternatives(){
        val game = GuessingGame()
        val questionObj = game.getQuestion()

        assertThat(questionObj.alternatives, hasItem(questionObj.correctAnswer))
    }

    @Test
    fun generates_valid_chars(){
        val game = GuessingGame()
        val questionObj = game.getQuestion()

        assertEquals(true, questionObj.correctAnswer.isLetter())

        assertNotEquals(null, questionObj.alternatives)

        val (_, bad)  = questionObj.alternatives!!.partition { it.isLetter() }
        assertEquals(0, bad.size)
    }

    @Test
    fun values_updates_correctly(){
        val game = GuessingGame()
        val questionObj = game.getQuestion()

        game.makeGuess(questionObj.correctAnswer)

        assertEquals(false, game.finished)
        assertEquals(1, game.score)
        assertEquals(2, game.count)
    }

    @Test
    fun finishes_correctly() {
        val game = GuessingGame()

        for (i in 0..9) {
            val questionObj = game.getQuestion()
            game.makeGuess(questionObj.correctAnswer)
        }

        assertEquals(true, game.finished)
        assertEquals(10, game.score)
        assertEquals(11, game.count)
    }
}
