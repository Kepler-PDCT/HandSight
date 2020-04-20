package logic

class Question<T>(answer: T, alternatives: List<T>?) {
    val correctAnswer = answer
    val alternatives = alternatives
}

abstract class Game<T> (numberOfQuestions : Int) {


    var WORDS : Array<String> = arrayOf(
        "seven",
        "world",
        "heart",
        "pizza",
        "water",
        "sixty",
        "board",
        "month",
        "Angel",
        "death",
        "green",
        "music",
        "fifty",
        "three",
        "party",
        "piano",
        "Kelly",
        "mouth",
        "woman",
        "sugar",
        "amber",
        "dream",
        "apple",
        "laugh",
        "tiger",
        "faith",
        "earth",
        "river",
        "money",
        "peace",
        "forty",
        "words",
        "smile",
        "house",
        "watch",
        "lemon",
        "South",
        "erica",
        "anime",
        "Jesus",
        "china",
        "stone",
        "blood",
        "thing",
        "light",
        "David",
        "cough",
        "story",
        "power",
        "India",
        "eagle",
        "human",
        "start",
        "right",
        "molly",
        "guard",
        "witch",
        "dough",
        "think",
        "image",
        "album",
        "catch",
        "sleep",
        "Quran",
        "organ",
        "peter",
        "Cupid",
        "storm",
        "silly",
        "berry",
        "rhyme",
        "carol",
        "olive",
        "leave",
        "whale",
        "James",
        "sally",
        "ology",
        "brave",
        "Asian",
        "Aaron",
        "Holly",
        "arrow",
        "there",
        "Ebola",
        "bacon",
        "local",
        "graph",
        "super",
        "Brown",
        "onion",
        "Simon",
        "globe",
        "alley",
        "stick",
        "Spain",
        "daddy",
        "scare",
        "kylie",
        "quiet"
    )
    var wordPosition : Int = 0
    var timerLength : Long = 20000
    var elapsedTime : Long  = 0

    val numberOfQuestions = numberOfQuestions

    var score = 0
        protected set

    var count = 1
        private set

    var finished = false
        private set

    private val questions: List<Question<T>> = (0..numberOfQuestions-1).map { nextQuestion() }

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

    fun reset() {
        count = 1
        finished = false
        score = 0
    }
}