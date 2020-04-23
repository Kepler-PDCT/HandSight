package logic

import android.util.Log
import com.example.handsight.Constants
import kotlin.math.roundToInt

class ImitationChallengeGame : Game<Char>(10) {

    val timerLength : Long = 20000
    var performanceScore = 0

    override fun nextQuestion(): Question<Char> {
        val right = Constants.IMAGENET_CLASSES[(0..Constants.IMAGENET_CLASSES.size - 2).random()].single()
        return Question(right, null)
    }

    fun isCorrect(Guess: Char): Boolean {
        val isCorrect = getQuestion().correctAnswer == Guess
        return isCorrect
    }

    fun setScoreAccordingToPosition (guessPosition: Int) {
        if (guessPosition == 0) {
            updateScore(3)
        } else if (guessPosition == 1) {
            updateScore(2)
        } else if (guessPosition == 2) {
            updateScore(1)
        }
    }

    fun updatePerformanceScore(topKPredictions: Array<String?>, topKScores: FloatArray){
        val pos = topKPredictions.indexOf(getQuestion().correctAnswer.toString())
        if (pos == -1){
            performanceScore = 0
        }
        else{
            var confBonus = topKScores[pos] * 200
            if (confBonus > 20){
                confBonus = 20f
            }
            performanceScore = (100 - (pos+1)*20 + confBonus).roundToInt()
            Log.d("perf", topKScores[pos].toString())
        }
        Log.d("perf", performanceScore.toString())

    }

    fun advanceGame () {
        updateCounter()
    }

    override fun makeGuess(guess: Char): Boolean {
        assert(!finished) // Should not be called if game is finished.
        val isCorrect = getQuestion().correctAnswer == guess
        //Right answer is at top for 2 seconds
        if (isCorrect) {
            updateScore(5)
        }
        updateCounter()
        return isCorrect
    }
}