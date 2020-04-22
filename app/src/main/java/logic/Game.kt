package logic

import android.util.Log
import com.example.handsight.Constants
import kotlin.math.roundToInt

class Question<T>(answer: T, alternatives: List<T>?) {
    val correctAnswer = answer
    val alternatives = alternatives
}

abstract class Game<T> (numberOfQuestions : Int) {
    var wordPosition : Int = 0
    var timerLength : Long = 20000
    var elapsedTime : Long  = 0

    val numberOfQuestions = numberOfQuestions

    var score = 0
        protected set

    var count = 1
        private set

    var finished = false
        private set

    var performanceScore = 0

    private val questions: List<Question<T>> = (0..numberOfQuestions-1).map { nextQuestion() }

    fun getQuestion(): Question<T> {
        return questions[count - 1]
    }

    fun updatePerformanceScore(topKPredictions: Array<String?>, topKScores: FloatArray){
        val pos = topKPredictions.indexOf(getQuestion().correctAnswer.toString())
        if (pos == -1){
            performanceScore = 0
        }
        else{
            var confBonus = topKScores[pos].roundToInt() * 2
            if (confBonus > 20){
                confBonus = 20
            }
            performanceScore = 100 - (pos+1)*20 + confBonus
        }
        Log.d("perf", performanceScore.toString())
    }

    abstract protected fun nextQuestion(): Question<T>

    abstract fun makeGuess(guess: T): Boolean


    protected fun updateScore(incrementalScore: Int) {
        score += incrementalScore
    }

    protected fun updateCounter() {
        count++;
        if (count > numberOfQuestions) {
            finished = true
        }
    }

    fun reset() {
        count = 1
        finished = false
        score = 0
    }
}