package logic

class GuessingGame: Game<Char>() {
    override fun nextQuestion(): Question<Char> {
        var questionArray = (0..3).map { ((0..25).random() + 65).toChar() }.toList()
        val right = questionArray[(0..3).random()]
        return Question(right, questionArray)
    }
}