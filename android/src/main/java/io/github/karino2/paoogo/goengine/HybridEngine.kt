package io.github.karino2.paoogo.goengine


class Policy(val threshold : Int) {
    fun isFirst(te: Int) : Boolean {
        return te < threshold
    }
}

class HybridEngine(val first: GoEngine, val second: GoEngine, val policy: Policy) : GoEngine {
    var te = 0
    override fun genMoveInternal(isBlack: Boolean): Int {
        te += 1
        val (main, sub) =  if(policy.isFirst(te)) {
            Pair(first, second)
        } else {
            Pair(second, first)
        }
        val res = main.genMoveInternal(isBlack)
        val pos = GoEngine.internalToPos(res)
        if (pos.pass)
            sub.doPass(isBlack)
        else
            sub.doMove(pos.x, pos.y, isBlack)
        return res
    }

    override fun debugInfo(): String? {
        return "te=${te}, isFirst=${policy.isFirst(te)}"
    }

    override fun setKomi(komi: Float) {
        first.setKomi(komi)
        second.setKomi(komi)
    }

    override fun clearBoard() {
        first.clearBoard()
        second.clearBoard()
        te = 0
    }

    override fun setBoardSize(size: Int) {
        first.setBoardSize(size)
        second.setBoardSize(size)
    }

    override fun doMove(x: Int, y: Int, isBlack: Boolean): Boolean {
        te += 1
        first.doMove(x, y, isBlack)
        return second.doMove(x, y, isBlack)
    }

    override fun doPass(isBlack: Boolean) {
        te += 1
        first.doPass(isBlack)
        second.doPass(isBlack)
    }

}