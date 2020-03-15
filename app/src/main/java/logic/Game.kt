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

    abstract fun nextQuestion(): Question<T>

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
    }
}