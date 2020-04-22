package logic

class WordGame : Game<String>(3) {


    override fun nextQuestion(): Question<String> {
        wordPosition = 0

        val x = timerLength + elapsedTime

        val randomWord = WORDS.random().toUpperCase()
        return Question(randomWord, null)
    }

    override fun makeGuess(guess: String): Boolean {
        return true
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