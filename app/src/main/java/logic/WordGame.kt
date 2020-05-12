package logic

import android.util.Log
import com.example.handsight.Constants
import kotlin.math.roundToInt

class WordGame : Game<String>(3) {

    val timerLength : Long = 20000
    var performanceScore = 0

    override fun nextQuestion(): Question<String> {
        wordPosition = 0

        val x = timerLength + elapsedTime

        val randomWord = Constants.WORDS.random().toUpperCase()
        return Question(randomWord, null)
    }

    override fun makeGuess(guess: String): Boolean {
        return true
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
            performanceScore = (100 - (pos+1)*33 + confBonus).roundToInt()
            Log.d("perf", topKScores[pos].toString())
        }
        Log.d("perf", performanceScore.toString())

    }

    fun checkPredictions (predictions:List<Char>) : Boolean {
        if(getQuestion().correctAnswer[wordPosition] in predictions.subList(0,2)) {
            //In top 2
            updateScore((timerLength- elapsedTime).toInt()/1000 + 5)
            return true
        }
        return false
    }

    fun advanceWord () {
        wordPosition++
    }

    fun advanceGame() {
        wordPosition = 0
        updateCounter()
    }

}