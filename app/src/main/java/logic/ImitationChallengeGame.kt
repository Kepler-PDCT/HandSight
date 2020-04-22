package logic

class ImitationChallengeGame : Game<Char>(10) {
    override fun nextQuestion(): Question<Char> {
        var questionArray = (0..3).map { ((0..25).random() + 65).toChar() }.toList()
        val right = questionArray[(0..3).random()]
        return Question(right, questionArray)
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
        return isCorrect
    }
}