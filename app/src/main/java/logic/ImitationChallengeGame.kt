package logic

import android.util.Log
import com.example.handsight.Constants
import kotlin.math.roundToInt

class ImitationChallengeGame : Game<Char>(10) {

    val timerLength : Long = 20000
    var performanceScore = 0

    override fun nextQuestion(): Question<Char> {
        var correctAnswer = Constants.IMAGENET_CLASSES[(0..Constants.IMAGENET_CLASSES.size - 1).random()].single()
        while (correctAnswer in previousCorrectAnswers) {
            correctAnswer = Constants.IMAGENET_CLASSES[(0..Constants.IMAGENET_CLASSES.size - 1).random()].single()
        }
        previousCorrectAnswers.add(correctAnswer)
        return Question(correctAnswer, null)
    }

    fun isCorrect(Guess: Char): Boolean {
        val isCorrect = getQuestion().correctAnswer == Guess
        return isCorrect
    }

    fun setScoreAccordingToPosition (guessPosition: Int) : Boolean {
        var answerPresent=false
        if (guessPosition == 0) {
            updateScore(3)
            answerPresent = true
        } else if (guessPosition == 1) {
            updateScore(2)
            answerPresent = true
        } else if (guessPosition == 2) {
            updateScore(1)
            answerPresent = true
        }
        updateCounter()
        return answerPresent
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
        }
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