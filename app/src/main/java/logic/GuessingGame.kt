package logic

class GuessingGame : Game<Char>() {
    override fun nextQuestion(): Question<Char> {
        var questionArray = (0..3).map { ((0..25).random() + 65).toChar() }.toList()
        val right = questionArray[(0..3).random()]
        return Question(right, questionArray)
    }

    override fun makeGuess(guess: Char): Boolean {
        assert(!finished) // Should not be called if game is finished.
        val isCorrect = getQuestion().correctAnswer == guess
        if (isCorrect) {
            updateScore(1)
        }
        updateCounter()
        return isCorrect
    }
}