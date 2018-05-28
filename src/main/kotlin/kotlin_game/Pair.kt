package kotlin_game

data class Pair(var x: Int, var y: Int) {

    override fun equals(other: Any?): Boolean {
        if(this === other) return true
        if(other == null || javaClass != other.javaClass) return false

        val pair = other as Pair
        if(x != pair.x || y != pair.y){
            return false
        }

        return true
    }

    override fun hashCode() = 31 * x + y

}