package logic

class WordGame : Game<List<Char>>(3) {


    override fun nextQuestion(): Question<List<Char>> {
        wordPosition = 0

        val x = timerLength + elapsedTime

        val randomWord = WORDS.random().toUpperCase()
        return Question(randomWord.toList(), null)
    }

    override fun makeGuess(guess: List<Char>): Boolean {
        return true
    }

    fun checkPredictions (predictions:List<Char>) : Boolean {
        if(getQuestion().correctAnswer[wordPosition] in predictions.subList(0,2)) {
            //In top 2
            wordPosition++
            updateScore((timerLength- elapsedTime).toInt()/1000 + 5)
            if (wordPosition == getQuestion().correctAnswer.count()) {
                advanceGame()
            }
            return true
        }
        return false
    }

    fun advanceWord () {
        wordPosition++
            if (wordPosition == getQuestion().correctAnswer.count()) {
                advanceGame()
            }
    }

    fun advanceGame() {
        wordPosition = 0
        updateCounter()
    }

}