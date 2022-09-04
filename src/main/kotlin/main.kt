import kotlin.math.abs

class Pawn(val isItWhite : Boolean = true,
           initPosition : String = "a2") {
    private var position = arrayOf(0, 1)
    var enPassant = false

    init {
        val x: Int = when (initPosition[0]) {
            'a' -> 0
            'b' -> 1
            'c' -> 2
            'd' -> 3
            'e' -> 4
            'f' -> 5
            'g' -> 6
            else -> 7
        }
        this.position[0] = x
        position[1] = initPosition[1].toString().toInt() - 1
    }
    fun itIsMoveOrBeat(newPosition: Array<Int>): Boolean {
        val delta = deltaMove(newPosition)

        return abs(delta[0]) == 0
    }

    fun checkByBeat(newPosition: Array<Int>): Boolean {
        val delta = deltaMove(newPosition)
        if (delta[0] !in listOf(-1, 1)) return false

        if (isItWhite) {
            if (delta[1] != 1) return false
        } else {
            if (delta[1] != -1) return false
        }
        return true
    }

    private fun deltaMove(newPosition: Array<Int>): Array<Int> {
        return arrayOf(newPosition[0] - position[0], newPosition[1] - position[1])
    }

    fun checkByMove(newPosition: Array<Int>): Boolean {
        val delta = deltaMove(newPosition)
        if (delta[0] != 0) return false
        val deltaY = if (isItWhite) {
            if (position[1] == 1) 2 else 1
        } else {
            if (position[1] == 6) -2 else -1
        }
        if (abs(delta[1]) > abs(deltaY)) return false
        if (isItWhite) {
            if (delta[1] < 1) return false
            if (delta[1] == 2) enPassant = true
        } else {
            if (delta[1] > -1) return false
            if (delta[1] == -2) enPassant = true
        }

        return true
    }

    fun getPosition(): Array<Int> {
        return position
    }

    fun confirmTheNewPosition(newPosition: Array<Int>) {
        position[0] = newPosition[0]
        position[1] = newPosition[1]
    }
}

class Field {
    private var pawns : MutableList<Pawn> = mutableListOf()
    private var allFigures = listOf(pawns)
    private val listOfTheField = Array(18) { "  +---+---+---+---+---+---+---+---+"}
    init {
        listOfTheField[listOfTheField.lastIndex] = "    a   b   c   d   e   f   g   h"
    }

    fun takeAllPawn(listOfPawn: MutableList<Pawn>) {
        pawns = listOfPawn
        allFigures = listOf(pawns)
    }

    fun checkTheMove(turn: String, colorIsWhite: Boolean): Pair<Boolean, String> {
        if (turn == "exit") return Pair(true, "")
        val regex = Regex("[a-h][1-8][a-h][1-8]")
        if(!turn.matches(regex)) return Pair(false, "Invalid Input")
        val positionFrom = takePosition(turn.substring(0, 2))
        val positionTo = takePosition(turn.substring(2, 4))
        val position = arrayOf(positionFrom[0],  positionFrom[1], positionTo[0], positionTo[1])
        val color = if (colorIsWhite) "white" else "black"
        if (!isThereAFigure(positionFrom, colorIsWhite)) return Pair(false, "No $color pawn at ${turn[0]}${turn[1]}")
        val figure = whatFigureIsIt(positionFrom, colorIsWhite)

        if (figure.itIsMoveOrBeat(positionTo)) {
            // it is a move
            if (!figure.checkByMove(positionTo)) return Pair(false, "Invalid Input")
            if (isThereAFigure(positionTo, !colorIsWhite)) return Pair(false, "Invalid Input")
            if (isThereAFigure(positionTo, colorIsWhite)) return Pair(false, "Invalid Input")
        } else {
            // he takes a figure
            if (!figure.checkByBeat(positionTo)) return Pair(false, "Invalid Input")
            val normalBeat = (!isThereAFigure(positionTo, colorIsWhite)) &&
                    (isThereAFigure(positionTo, !colorIsWhite))
            var killPosition = positionTo
            // en passant check
            var enPassant = false
            val enPassantPosition = arrayOf(position[2], position[1])
            if (isThereAFigure(enPassantPosition, !colorIsWhite)) {
                val figureEnPassant = whatFigureIsIt(enPassantPosition, !colorIsWhite)
                if (figureEnPassant.enPassant) {
                    enPassant = true
                    killPosition = enPassantPosition
                }
            }

            if (!normalBeat && !enPassant) return Pair(false, "Invalid Input")
            killTheFigure(killPosition, !colorIsWhite)
        }
        figure.confirmTheNewPosition(positionTo)
        return Pair(true, "")
    }

    private fun killTheFigure(position: Array<Int>, colorIsWhite: Boolean) {
        for (setOfFigure in allFigures) {
            for (figure in setOfFigure) {
                val figurePosition = figure.getPosition()
                if (figure.isItWhite == colorIsWhite &&
                    position[0] == figurePosition[0] && position[1] == figurePosition[1]) {
                    pawns.remove(figure)
                    allFigures = listOf(pawns)
                    return
                }
            }
        }
    }

    private fun isThereAFigure(position: Array<Int>, colorIsWhite: Boolean): Boolean {
        for (setOfFigure in allFigures) {
            for (figure in setOfFigure) {
                val figurePosition = figure.getPosition()
                if (figure.isItWhite == colorIsWhite &&
                    position[0] == figurePosition[0] && position[1] == figurePosition[1]) {
                    return true
                }
            }
        }
        return false
    }

    private fun whatFigureIsIt(position: Array<Int>, colorIsWhite: Boolean): Pawn {
        for (setOfFigure in allFigures) {
            for (figure in setOfFigure) {
                val figurePosition = figure.getPosition()
                if (figure.isItWhite == colorIsWhite &&
                    position[0] == figurePosition[0] && position[1] == figurePosition[1]) {
                    return figure
                }
            }
        }
        return Pawn(true, "a2")
    }

    private fun takePosition(turn: String): Array<Int> {
        val position = Array(2) {0}
        val x: Int = when (turn[0]) {
            'a' -> 0
            'b' -> 1
            'c' -> 2
            'd' -> 3
            'e' -> 4
            'f' -> 5
            'g' -> 6
            else -> 7
        }
        position[0] = x
        position[1] = turn[1].toString().toInt() - 1
        return position
    }

    private fun clearTheField() {
        for (line in 0..7) {
            listOfTheField[1 + line*2] = "${8 - line} |   |   |   |   |   |   |   |   |"
        }
    }

    fun drawTheField() {
        clearTheField()
        addElementsToTheField()
        drawIt()
    }

    private fun addElementsToTheField() {
        for (figureSet in allFigures) {
            for (figure in figureSet) {
                when (figure) {
                    is Pawn -> { addAPawn(figure) }
                    else -> { }
                }
            }
        }
    }

    private fun drawIt() {
        for (line in listOfTheField) {
            println(line)
        }
        println()
    }

    private fun addAPawn(pawn: Pawn) {
        val char = if (pawn.isItWhite) "W" else "B"
        val coordinates = pawn.getPosition()
        var newLine = listOfTheField[15 - 2*coordinates[1]]
        newLine = newLine.substring(0, 4 + 4*coordinates[0]) + char + newLine.substring(5 + 4*coordinates[0], newLine.length)
        listOfTheField[15 - 2*coordinates[1]] = newLine
        allFigures = listOf(pawns)
    }

    fun enPassantResetAll(isColorWhite: Boolean) {
        for (pawn in pawns) {
            if (pawn.isItWhite == isColorWhite) {
                pawn.enPassant = false
            }
        }
    }

    fun checkWin(isColorWhite: Boolean): Boolean {
        val lastLine = if (isColorWhite) 7 else 0
        for (pawn in pawns) {
            if (pawn.getPosition()[1] == lastLine) {
                return true
            }
        }
        val allPawn = getWhiteAndBlack()
        val opponentPawn = if (isColorWhite) allPawn.second else allPawn.first
        if (opponentPawn.size == 0) return true
        return false
    }

    private fun hasNoTurn(opponentPawn: MutableList<Pawn>): Boolean {
        for (pawn in opponentPawn) {
            if (canItGo(pawn)) {
                return false
            }
        }
        return true
    }

    private fun canItGo(pawn: Pawn): Boolean {
        val isColorWhite = pawn.isItWhite
        val direction = if (isColorWhite) 1 else -1
        val position = pawn.getPosition()
        val positionAboveTheWhitePawn = arrayOf(pawn.getPosition()[0], pawn.getPosition()[1] + direction)
        if (!isThereAFigure(position = positionAboveTheWhitePawn, colorIsWhite = !isColorWhite) &&
            !isThereAFigure(position = positionAboveTheWhitePawn, colorIsWhite = isColorWhite)) {
            return true
        }
        if (position[0] > 0) {
            val positionLeft = arrayOf(position[0] - 1, position[1] + direction)
            if (isThereAFigure(position = positionLeft, colorIsWhite = !isColorWhite)) {
                return true
            }
        }
        if (position[0] < 7) {
            val positionRight = arrayOf(position[0] + 1, position[1] + direction)
            if (isThereAFigure(position = positionRight, colorIsWhite = !isColorWhite)) {
                return true
            }
        }
        return false
    }

    private fun getWhiteAndBlack() : Pair<MutableList<Pawn>, MutableList<Pawn>> {
        val whitePawns: MutableList<Pawn> = mutableListOf()
        val blackPawns: MutableList<Pawn> = mutableListOf()
        for (pawn in pawns) {
            if (pawn.isItWhite) {
                whitePawns.add(pawn)
            } else {
                blackPawns.add(pawn)
            }
        }
        return Pair(whitePawns, blackPawns)
    }

    fun checkDraw(isColorWhite: Boolean): Boolean {
        val allPawn = getWhiteAndBlack()
        val whitePawns = allPawn.first
        val blackPawns = allPawn.second
        val opponentPawn: MutableList<Pawn> = if (isColorWhite) {
            blackPawns
        } else {
            whitePawns
        }
        if (hasNoTurn(opponentPawn)) return true
        return false
    }
}

fun main() {
    val listOfPawn = mutableListOf<Pawn>()
    for (i in listOf("a", "b", "c", "d", "e", "f", "g", "h")) {
        listOfPawn.add(Pawn(isItWhite = true, initPosition = i + "2"))
        listOfPawn.add(Pawn(isItWhite = false, initPosition = i + "7"))
    }
    println("Pawns-Only Chess")
    println("First Player's name:")
    val firstPlayer = readln()
    println("Second Player's name:")
    val secondPlayer = readln()
    val field = Field()
    field.takeAllPawn(listOfPawn)
    field.drawTheField()

    var play = true
    var isColorWhite: Boolean
    while (play) {
        for (playersName in listOf(firstPlayer, secondPlayer)) {
            field.enPassantResetAll(playersName == firstPlayer)
            println("${playersName}'s turn:")
            isColorWhite = playersName == firstPlayer
            var turn = readln()
            var checkIt = field.checkTheMove(turn = turn, colorIsWhite = isColorWhite)
            while(!checkIt.first) {
                println(checkIt.second)
                println("${playersName}'s turn:")
                turn = readln()
                checkIt = field.checkTheMove(turn = turn, colorIsWhite = isColorWhite)
            }
            if (turn == "exit") {
                println("Bye!")
                play = false
                break
            } else {
                field.drawTheField()
                if (field.checkWin(isColorWhite = isColorWhite)) {
                    println(if (isColorWhite) "White Wins!" else "Black Wins!")
                    println("Bye!")
                    play = false
                    break
                }
                if (field.checkDraw(isColorWhite = isColorWhite)) {
                    println("Stalemate!")
                    println("Bye!")
                    play = false
                    break
                }
            }
        }
    }
}