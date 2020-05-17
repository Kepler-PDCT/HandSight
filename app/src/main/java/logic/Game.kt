package logic

import android.util.Log
import kotlin.math.roundToInt

class Question<T>(answer: T, alternatives: List<T>?) {
    val correctAnswer = answer
    val alternatives = alternatives
}

abstract class Game<T> (numberOfQuestions : Int) {
    var wordPosition : Int = 0
    var elapsedTime : Long  = 0

    var previousCorrectAnswers : MutableList<Char> = mutableListOf()

    val numberOfQuestions = numberOfQuestions

    var score = 0
        protected set

    var currentQuestionIndex = 1
        private set

    var finished = false
        private set



    private var questions: List<Question<T>> = (0..numberOfQuestions-1).map { nextQuestion() }

    fun getQuestion(): Question<T> {
        return questions[currentQuestionIndex - 1]
    }

    abstract protected fun nextQuestion(): Question<T>

    abstract fun makeGuess(guess: T): Boolean


    protected fun updateScore(incrementalScore: Int) {
        score += incrementalScore
    }

    fun updateCounter() {
        currentQuestionIndex++;
        if (currentQuestionIndex > numberOfQuestions) {
            finished = true
        }
    }

    fun reset() {
        previousCorrectAnswers.clear()
        currentQuestionIndex = 1
        finished = false
        score = 0
        questions = (0..numberOfQuestions-1).map { nextQuestion() }
    }
}