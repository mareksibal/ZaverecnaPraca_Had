package kotlin_game

class Snake(
        private val x: Int,
        private val y: Int,
        private val length: Int,
        private var direction: Direction
) {

    private val body: MutableList<Pair> = ArrayList()
    var eat: Boolean = false

    init {
        for(i in 0 .. length) {
            body.add( when(direction) {
                        Direction.DOWN -> Pair(x, y - i)
                        Direction.UP -> Pair(x, y + i)
                        Direction.RIGHT -> Pair(x - i, y)
                        Direction.LEFT -> Pair(x + i, y)
            })
        }
    }

    fun makeStep(direction: Direction) {
        this.direction = direction

        val newBodyPart = if(eat) {
            getTail().copy()
        } else {
            null
        }

        for(i in body.size - 1 downTo 1) {
            body[i] = body[i - 1].copy()
        }

        when(direction) {
            Direction.LEFT -> getHead().x = getHead().x - 1
            Direction.RIGHT -> getHead().x = getHead().x + 1
            Direction.UP -> getHead().y = getHead().y - 1
            Direction.DOWN -> getHead().y = getHead().y + 1
        }

        newBodyPart?.let {
            body.add(it)
            eat = false
        }
    }

    fun makeStep() {
        makeStep(direction)
    }

    fun getBody() = body
    fun getDirection() = direction
    fun getHead() = body.first()
    fun getTail() = body.last()
}