package logic

class Question<T>(answer: T, alternatives: List<T>?) {
    val correctAnswer = answer
    val alternatives = alternatives
}

abstract class Game<T> {
    val numberOfQuestions = 10

    var score = 0
        protected set

    var count = 1
        private set

    var finished = false
        private set

    private val questions: List<Question<T>> = (0..numberOfQuestions).map { nextQuestion() }

    fun getQuestion(): Question<T> {
        return questions[count - 1]
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

    protected fun reset() {
        count = 1
        finished = false
        score = 0
    }
}