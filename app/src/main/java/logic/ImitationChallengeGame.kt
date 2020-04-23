package logic

import com.example.handsight.Constants

class ImitationChallengeGame : Game<Char>(10) {
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