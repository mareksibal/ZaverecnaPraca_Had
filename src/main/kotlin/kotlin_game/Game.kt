package kotlin_game

import com.googlecode.lanterna.TerminalFacade
import com.googlecode.lanterna.input.Key
import com.googlecode.lanterna.screen.Screen
import com.googlecode.lanterna.screen.ScreenWriter
import com.googlecode.lanterna.terminal.Terminal
import java.util.*

class Game {
    companion object {
        private const val WORLD_WIDTH = 80
        private const val WORLD_HEIGHT = 24

        private const val GAME_SPEED = 150

        private val CONTROL_KEYS = arrayOf(
                (Key(Key.Kind.ArrowUp)),
                (Key(Key.Kind.ArrowDown)),
                (Key(Key.Kind.ArrowLeft)),
                (Key(Key.Kind.ArrowRight))
        )
        private val TERMINATE_KEY = Key(Key.Kind.Escape)
    }

    private var terminal: Terminal = TerminalFacade.createTerminal()
    private var screen: Screen
    private var writer: ScreenWriter

    private var snake: Snake
    private var directionsQueue: ArrayList<Direction>
    private lateinit var state: State
    private lateinit var apple: Pair

    init {
        screen = Screen(terminal).apply {
            cursorPosition = null
            startScreen()
            refresh()
        }

        writer = ScreenWriter(screen)

        snake = Snake(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 3, Direction.UP)
        directionsQueue = ArrayList()

        generateApple()
    }

    fun start() {
        drawScreen()
        var stepStartMillis = 0L
        state = State.RUNNING

        while (true) {
            handleInput()

            val currentTimeMillis = System.currentTimeMillis()
            if(currentTimeMillis - stepStartMillis > GAME_SPEED) {
                stepStartMillis = currentTimeMillis

                if(directionsQueue.isNotEmpty()){
                    snake.makeStep(directionsQueue.removeAt(0))
                } else {
                    snake.makeStep()
                }

                checkWin()
                checkCrash()

                if(state != State.RUNNING){
                    break
                }

                if(snake.getHead() == apple) {
                    snake.eat = true
                    generateApple()
                }

                drawScreen()
            }
        }

        when(state) {
            State.EXIT -> {
                screen.stopScreen()
                return
            }
            State.CRASH -> writer.drawString(24, 12, "####     NABURAL SI :(     ####")
            State.WIN -> writer.drawString(24, 12, "####      VYHRAL SI :)     ####")
        }

        writer.drawString(24, 11, "###############################")
        writer.drawString(24, 13, "####         [ESC]         ####")
        writer.drawString(24, 14, "###############################")
        screen.refresh()

        while (true) {
            val key = terminal.readInput()
            if (key != null) {
                if (key == TERMINATE_KEY) {
                    break
                }
            }
        }

        screen.stopScreen()
    }

    private fun generateApple() {
        val random = Random()

        var newApple: Pair

        do {
            newApple = Pair(random.nextInt(WORLD_WIDTH - 2) + 1, random.nextInt(WORLD_HEIGHT - 2) + 1)
        } while (isOccupied(newApple))

        apple = newApple
    }

    private fun handleInput() {
        val key = terminal.readInput()

        key?.let {
            val direction = getDirectionFromKey(it)

            if (direction != null) {
                if (directionsQueue.isEmpty()) {
                    if (snake.getDirection() != getOppositeDirection(direction)) {
                        directionsQueue.add(direction)
                    }
                } else if (getLastDirection() != getOppositeDirection(direction)) {
                    directionsQueue.add(direction)
                }
            } else if (key == TERMINATE_KEY) {
                state = State.EXIT
            }
        }
    }

    private fun checkCrash() {
        val snakeHead = snake.getHead()

        if(snakeHead.x == 0 || snakeHead.y == 0 || snakeHead.x == WORLD_WIDTH - 1 || snakeHead.y == WORLD_HEIGHT - 1) {
            state = State.CRASH
        }

        for(i in 1 until snake.getBody().size) {
            if(snakeHead == snake.getBody()[i]) {
                state = State.CRASH
                break
            }
        }
    }

    private fun checkWin() {
        if(snake.getBody().size == ((WORLD_HEIGHT - 2) * (WORLD_WIDTH - 2))) {
            state = State.WIN
        }
    }

    private fun drawScreen() {
        screen.clear()

        val wall = StringBuilder()
        for(i in 0 .. WORLD_WIDTH) {
            wall.append("■")
        }

        writer.drawString(0, 0, wall.toString())
        for(i in 1 until  WORLD_HEIGHT - 1) {
            writer.drawString(0, i, "■")
            writer.drawString(WORLD_WIDTH - 1, i, "■")
        }
        writer.drawString(0, WORLD_HEIGHT - 1, wall.toString())

        for(i in 1 until snake.getBody().size) {
            val bodyPart = snake.getBody()[i]
            writer.drawString(bodyPart.x, bodyPart.y, "*")
        }

        val headPart = snake.getHead()
        writer.drawString(headPart.x, headPart.y, "O")

        writer.drawString(apple.x, apple.y, "ò")

        screen.refresh()
    }

    private fun getDirectionFromKey(key: Key): Direction? {
        for(i in 0 until CONTROL_KEYS.size) {
            if(CONTROL_KEYS[i] == key) {
                return Direction.values()[i]
            }
        }

        return null
    }

    private fun getOppositeDirection(direction: Direction) = when(direction) {
        Direction.UP -> Direction.DOWN
        Direction.DOWN -> Direction.UP
        Direction.RIGHT -> Direction.LEFT
        Direction.LEFT -> Direction.RIGHT
    }

    private fun isOccupied(pair: Pair) = snake.getBody().contains(pair)

    private fun getLastDirection() = directionsQueue.last()

    private enum class State {
        RUNNING,
        EXIT,
        CRASH,
        WIN
    }
}