package logic

import com.example.handsight.Constants

class GuessingGame : Game<Char>(10) {
    override fun nextQuestion(): Question<Char> {
        var questionArray =
            Constants.IMAGENET_CLASSES.slice(0..Constants.IMAGENET_CLASSES.size - 2).toList()
                .shuffled().take(4).map { it.single() }.toList()
        var correctAnswer = questionArray[(0..3).random()]
        while (correctAnswer in previousCorrectAnswers) {
            questionArray =
                Constants.IMAGENET_CLASSES.slice(0..Constants.IMAGENET_CLASSES.size - 2).toList()
                    .shuffled().take(4).map { it.single() }.toList()
            correctAnswer = questionArray[(0..3).random()]
        }
        previousCorrectAnswers.add(correctAnswer)
        return Question(correctAnswer, questionArray)
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