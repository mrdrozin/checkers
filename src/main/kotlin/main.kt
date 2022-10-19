import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.swing.Swing
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Rect
import org.jetbrains.skia.makeFromFile
import org.jetbrains.skija.Typeface
import org.jetbrains.skiko.GenericSkikoView
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkikoView
import java.awt.Dimension
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.JFrame
import javax.swing.SwingUtilities
import javax.swing.WindowConstants
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


fun main() {
    startPos()
    createWindow("Checkers")

}

const val SIZE: Int = 8
const val LEFT = 100f
const val TOP = 100f
const val LENGTH = 600f
const val SQUARE = 75f
val cords = listOf("A", "B", "C", "D", "E", "F", "G", "H")
val board = (100f..100f + LENGTH)
val empty = Field(false, null, null, null)

data class Field(
    var notEmpty: Boolean,
    var color: Color?,
    var Queen: Boolean?,
    var canBeEaten: Boolean?
) {

}

var turn = Color.WHITE
var fields = mutableListOf<MutableList<Field>>()
var isActive = false
var Moves = mutableListOf<Pair<Int, Int>>()
var Eaten = mutableListOf<Pair<Int, Int>>()
fun startPos() {
    repeat(SIZE) { it ->
        fields.add(mutableListOf())
        repeat(SIZE) { jt ->
            fields[it].add(empty)
        }
    }


    repeat(SIZE / 2) {
        fields[0][2 * it] = Field(
            true, Color.WHITE, false, canBeEaten = true
        )
    }
    repeat(SIZE / 2) {
        fields[1][2 * it + 1] = Field(
            true, Color.WHITE, false, canBeEaten = true
        )
    }
    repeat(SIZE / 2) {
        fields[2][2 * it] = Field(
            true, Color.WHITE, false, canBeEaten = true
        )
    }
    repeat(SIZE / 2) {
        fields[3][2 * it + 1] = Field(
            false, null, null, canBeEaten = true
        )
    }
    repeat(SIZE / 2) {
        fields[4][2 * it] = Field(
            false, null, null, canBeEaten = true
        )
    }
    repeat(SIZE / 2) {
        fields[5][2 * it + 1] = Field(
            true, Color.BLACK, false, canBeEaten = true
        )
    }
    repeat(SIZE / 2) {
        fields[6][2 * it] = Field(
            true, Color.BLACK, false, canBeEaten = true
        )
    }
    repeat(SIZE / 2) {
        fields[7][2 * it + 1] = Field(
            true, Color.BLACK, false, canBeEaten = true
        )
    }


}

enum class Condition {
    ATTACK, MOVE, PASSIVE
}

data class Ancestor(var x: Int?, var y: Int?, var condition: Condition)

var ancestor = Ancestor(null, null, Condition.PASSIVE)

enum class Color {
    WHITE, BLACK
}


fun whiteCh(x: Int, y: Int, canvas: Canvas) {
    canvas.drawCircle(LEFT + SQUARE / 2 + SQUARE * x, TOP + LENGTH - SQUARE / 2 - SQUARE * y,
        32.5f, org.jetbrains.skia.Paint().apply {
            color = 0xFFC8B560.toInt()
        })
    canvas.drawCircle(LEFT + SQUARE / 2 + SQUARE * x, TOP + LENGTH - SQUARE / 2 - SQUARE * y,
        25f, org.jetbrains.skia.Paint().apply {
            color = 0xFFE8D96F.toInt()
        })
}

fun whiteQueen(x: Int, y: Int, canvas: Canvas) {
    canvas.drawCircle(LEFT + SQUARE / 2 + SQUARE * x, TOP + LENGTH - SQUARE / 2 - SQUARE * y,
        32.5f, org.jetbrains.skia.Paint().apply {
            color = 0xFFC8B560.toInt()
        })
    canvas.drawCircle(LEFT + SQUARE / 2 + SQUARE * x, TOP + LENGTH - SQUARE / 2 - SQUARE * y,
        25f, org.jetbrains.skia.Paint().apply {
            color = 0xFFE8D96F.toInt()
        })
    canvas.drawCircle(LEFT + SQUARE / 2 + SQUARE * x, TOP + LENGTH - SQUARE / 2 - SQUARE * y,
        5f, org.jetbrains.skia.Paint().apply {
            color = 0xFFFF0000.toInt()
        })
}

fun blackCh(x: Int, y: Int, canvas: Canvas) {
    canvas.drawCircle(LEFT + SQUARE / 2 + SQUARE * x, TOP + LENGTH - SQUARE / 2 - SQUARE * y,
        32.5f, org.jetbrains.skia.Paint().apply {
            color = 0xFF000000.toInt()
        })
    canvas.drawCircle(LEFT + SQUARE / 2 + SQUARE * x, TOP + LENGTH - SQUARE / 2 - SQUARE * y,
        25f, org.jetbrains.skia.Paint().apply {
            color = 0xFF524B4B.toInt()
        })
}

fun blackQueen(x: Int, y: Int, canvas: Canvas) {
    canvas.drawCircle(LEFT + SQUARE / 2 + SQUARE * x, TOP + LENGTH - SQUARE / 2 - SQUARE * y,
        32.5f, org.jetbrains.skia.Paint().apply {
            color = 0xFF000000.toInt()
        })
    canvas.drawCircle(LEFT + SQUARE / 2 + SQUARE * x, TOP + LENGTH - SQUARE / 2 - SQUARE * y,
        25f, org.jetbrains.skia.Paint().apply {
            color = 0xFF524B4B.toInt()
        })
    canvas.drawCircle(LEFT + SQUARE / 2 + SQUARE * x, TOP + LENGTH - SQUARE / 2 - SQUARE * y,
        5f, org.jetbrains.skia.Paint().apply {
            color = 0xFFFF0000.toInt()
        })
}

fun availableMove(x: Int, y: Int, canvas: Canvas) {
    canvas.drawRect(Rect.makeXYWH(
        LEFT + SQUARE * x, TOP + LENGTH - SQUARE - SQUARE * y,
        SQUARE, SQUARE
    ), org.jetbrains.skia.Paint().apply {
        color = 0x70000000.toInt()
    })
}

fun boardRender(skiaLayer: SkiaLayer) {
    skiaLayer.skikoView = GenericSkikoView(skiaLayer, object : SkikoView {
        override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {

            val paintW = org.jetbrains.skia.Paint().apply {
                color = 0xFFE8E4C9.toInt()
            }
            val paintB = org.jetbrains.skia.Paint().apply {
                color = 0xFF357EC7.toInt()
            }
            Typeface.makeFromFile("fonts/JetBrainsMono-Regular.ttf")
            val font = org.jetbrains.skia.Font(
                org.jetbrains.skia.Typeface.makeFromFile("fonts/JetBrainsMono-Regular.ttf"),
                20f
            )
            canvas.drawPaint(paint = org.jetbrains.skia.Paint().apply {
                color = 0xFF36454F.toInt()
            })
            canvas.drawRect(Rect.makeXYWH(LEFT, TOP, LENGTH, LENGTH), paintW)
            val x = LEFT
            val y = TOP
            repeat(SIZE / 2) { it ->
                repeat(SIZE / 2) { jt ->
                    canvas.drawRect(
                        Rect.Companion.makeXYWH(
                            LEFT + jt * 2 * SQUARE, TOP + SQUARE + it * 2 * SQUARE,
                            SQUARE, SQUARE
                        ), paintB
                    )
                    canvas.drawRect(
                        Rect.Companion.makeXYWH(
                            LEFT + SQUARE + jt * 2 * SQUARE, y + it * 2 * SQUARE,
                            SQUARE, SQUARE
                        ), paintB
                    )

                }
            }
            repeat(8) {
                canvas.drawString(cords[it], LEFT + 30f + it * SQUARE, TOP + LENGTH + 30f, font, paintW)
                canvas.drawString((it + 1).toString(), LEFT - 22, TOP + LENGTH - 34 - SQUARE * it, font, paintW)
            }
            var counter = 0
            fields.forEach {
                it.forEach { jt ->
                    when (jt.color) {
                        Color.BLACK -> when (jt.Queen) {
                            false -> blackCh(counter % SIZE, counter / SIZE, canvas)
                            true -> blackQueen(counter % SIZE, counter / SIZE, canvas)
                        }

                        Color.WHITE -> when (jt.Queen) {
                            false -> whiteCh(counter % SIZE, counter / SIZE, canvas)
                            true -> whiteQueen(counter % SIZE, counter / SIZE, canvas)
                        }
                    }
                    counter += 1
                }

            }
            Moves.forEach { availableMove(it.first, it.second, canvas) }
            when (turn) {
                Color.WHITE -> canvas.drawString("White's move", LEFT + LENGTH / 2 - 60, TOP - 40, font, paintW)
                Color.BLACK -> canvas.drawString("Black's move", LEFT + LENGTH / 2 - 60, TOP - 40, font, paintW)
            }
        }
    })
}

fun canEat(x: Int, y: Int): Boolean {
    Moves.clear()
    var flag = false
    if (fields[y][x].color == turn) {
        when (fields[y][x].Queen) {
            false -> when {
                (y == SIZE - 2) or (y == SIZE - 1) -> when {
                    (x == 0) or (x == 1) -> if ((fields[y - 1][x + 1].color != turn && fields[y - 1][x + 1].color != null
                                && !fields[y - 2][x + 2].notEmpty && fields[y - 1][x + 1].canBeEaten == true)
                    ) {
                        Moves.add(Pair(x + 2, y - 2)); flag = true
                    }

                    (x == SIZE - 2) or (x == SIZE - 1) -> if (fields[y - 1][x - 1].color != turn && fields[y - 1][x - 1].color != null &&
                        !fields[y - 2][x - 2].notEmpty && fields[y - 1][x - 1].canBeEaten == true
                    ) {
                        Moves.add(Pair(x - 2, y - 2)); flag = true
                    }

                    else -> {
                        if ((fields[y - 1][x + 1].color != turn && fields[y - 1][x + 1].color != null &&
                                    !fields[y - 2][x + 2].notEmpty) && fields[y - 1][x + 1].canBeEaten == true
                        ) {
                            Moves.add(Pair(x + 2, y - 2)); flag = true
                        }
                        if ((fields[y - 1][x - 1].color != turn && fields[y - 1][x - 1].color != null &&
                                    !fields[y - 2][x - 2].notEmpty) && fields[y - 1][x - 1].canBeEaten == true
                        ) {
                            Moves.add(Pair(x - 2, y - 2)); flag = true
                        }
                    }
                }

                (y == 0) or (y == 1) -> when {
                    (x == 0) or (x == 1) -> if ((fields[y + 1][x + 1].color != turn && fields[y + 1][x + 1].color != null &&
                                !fields[y + 2][x + 2].notEmpty) && fields[y + 1][x + 1].canBeEaten == true
                    ) {
                        Moves.add(Pair(x + 2, y + 2)); flag = true
                    }

                    (x == SIZE - 2) or (x == SIZE - 1) -> if (fields[y + 1][x - 1].color != turn && fields[y + 1][x - 1].color != null
                        && !fields[y + 2][x - 2].notEmpty && fields[y + 1][x - 1].canBeEaten == true
                    ) {
                        Moves.add(Pair(x - 2, y + 2)); flag = true
                    }

                    else -> {
                        if ((fields[y + 1][x + 1].color != turn && fields[y + 1][x + 1].color != null
                                    && !fields[y + 2][x + 2].notEmpty) && fields[y + 1][x + 1].canBeEaten == true
                        ) {
                            Moves.add(Pair(x + 2, y + 2)); flag = true
                        }
                        if ((fields[y + 1][x - 1].color != turn && fields[y + 1][x - 1].color != null
                                    && !fields[y + 2][x - 2].notEmpty) && fields[y + 1][x - 1].canBeEaten == true
                        ) {
                            Moves.add(Pair(x - 2, y + 2)); flag = true
                        }
                    }
                }

                else -> {
                    when {
                        (x == 0) or (x == 1) -> {
                            if (fields[y + 1][x + 1].color != turn && fields[y + 1][x + 1].color != null
                                && !fields[y + 2][x + 2].notEmpty && fields[y + 1][x + 1].canBeEaten == true
                            ) {
                                Moves.add(Pair(x + 2, y + 2)); flag = true
                            }
                            if (fields[y - 1][x + 1].color != turn && fields[y - 1][x + 1].color != null
                                && !fields[y - 2][x + 2].notEmpty && fields[y - 1][x + 1].canBeEaten == true
                            ) {
                                Moves.add(Pair(x + 2, y - 2)); flag = true
                            }
                        }

                        (x == SIZE - 2) or (x == SIZE - 1) -> {
                            if (fields[y + 1][x - 1].color != turn && fields[y + 1][x - 1].color != null
                                && !fields[y + 2][x - 2].notEmpty && fields[y + 1][x - 1].canBeEaten == true
                            ) {
                                Moves.add(Pair(x - 2, y + 2)); flag = true
                            }
                            if (fields[y - 1][x - 1].color != turn && fields[y - 1][x - 1].color != null
                                && !fields[y - 2][x - 2].notEmpty && fields[y - 1][x - 1].canBeEaten == true
                            ) {
                                Moves.add(Pair(x - 2, y - 2)); flag = true
                            }
                        }

                        else -> {
                            if (fields[y + 1][x + 1].color != turn && fields[y + 1][x + 1].color != null
                                && !fields[y + 2][x + 2].notEmpty && fields[y + 1][x + 1].canBeEaten == true
                            ) {
                                Moves.add(Pair(x + 2, y + 2)); flag = true
                            }
                            if (fields[y + 1][x - 1].color != turn && fields[y + 1][x - 1].color != null
                                && !fields[y + 2][x - 2].notEmpty && fields[y + 1][x - 1].canBeEaten == true
                            ) {
                                Moves.add(Pair(x - 2, y + 2)); flag = true
                            }
                            if (fields[y - 1][x + 1].color != turn && fields[y - 1][x + 1].color != null
                                && !fields[y - 2][x + 2].notEmpty && fields[y - 1][x + 1].canBeEaten == true
                            ) {
                                Moves.add(Pair(x + 2, y - 2)); flag = true
                            }
                            if (fields[y - 1][x - 1].color != turn && fields[y - 1][x - 1].color != null
                                && !fields[y - 2][x - 2].notEmpty && fields[y - 1][x - 1].canBeEaten == true
                            ) {
                                Moves.add(Pair(x - 2, y - 2)); flag = true
                            }
                        }
                    }
                }
            }

            true -> {
                var flag1 = false
                var flag2 = false
                var flag3 = false
                var flag4 = false
                val down = y
                val up = SIZE - 1 - y
                val left = x
                val right = SIZE - 1 - x
                for (i in (1..minOf(up, right))) {
                    when {
                        fields[y + i][x + i].color == turn -> break
                        (fields[y + i][x + i].canBeEaten == false && fields[y + i][x + i].notEmpty) -> {
                            break
                        }

                        (fields[y + i][x + i].color != turn) && (fields[y + i][x + i].color != null) && (max(
                            x,
                            y
                        ) + i + 1 <= SIZE - 1)
                                && (fields[y + i][x + i].canBeEaten == true) -> {
                            if (flag1) {
                                break
                            }

                            for (j in i + 1..minOf(up, right)) {
                                when (fields[y + j][x + j].notEmpty) {
                                    false -> {
                                        flag = true;flag1 = true; Moves.add(Pair(x + j, y + j))
                                    }

                                    else -> break
                                }
                            }
                        }
                    }
                }
                for (i in (1..minOf(down, right))) {
                    when {
                        (fields[y - i][x + i].canBeEaten == false && fields[y - i][x + i].notEmpty) -> {
                            break
                        }

                        fields[y - i][x + i].color == turn -> break
                        (fields[y - i][x + i].color != turn) && (fields[y - i][x + i].color != null) && (x + i + 1 <= SIZE - 1) && (y - i - 1 >= 0)
                                && (fields[y - i][x + i].canBeEaten == true) -> {
                            if (flag2) {
                                break
                            }

                            for (j in i + 1..minOf(down, right)) {
                                when (fields[y - j][x + j].notEmpty) {
                                    false -> {
                                        flag = true;flag2 = true; Moves.add(Pair(x + j, y - j))
                                    }

                                    else -> break
                                }
                            }
                        }
                    }
                }
                for (i in (1..minOf(up, left))) {
                    when {
                        fields[y + i][x - i].color == turn -> break
                        (fields[y + i][x - i].canBeEaten == false && fields[y + i][x - i].notEmpty) -> {
                            break
                        }

                        (fields[y + i][x - i].color != turn) && (fields[y + i][x - i].color != null) && (x - i - 1 >= 0) && (y + i + 1 <= SIZE - 1)
                                && (fields[y + i][x - i].canBeEaten == true) -> {
                            if (flag3) {
                                break
                            }

                            for (j in i + 1..minOf(up, left)) {
                                when (fields[y + j][x - j].notEmpty) {
                                    false -> {
                                        flag = true;flag3 = true; Moves.add(Pair(x - j, y + j))
                                    }

                                    else -> break
                                }
                            }
                        }
                    }
                }
                for (i in (1..minOf(down, left))) {
                    when {
                        fields[y - i][x - i].color == turn -> break
                        (fields[y - i][x - i].canBeEaten == false && fields[y - i][x - i].notEmpty) -> {
                            break
                        }

                        (fields[y - i][x - i].color != turn) && (fields[y - i][x - i].color != null) && (min(
                            x,
                            y
                        ) - i - 1 >= 0)
                                && (fields[y - i][x - i].canBeEaten == true) -> {

                            if (flag4) {
                                break
                            }
                            for (j in i + 1..minOf(down, left)) {
                                when (fields[y - j][x - j].notEmpty) {
                                    false -> {
                                        flag = true; flag4 = true; Moves.add(Pair(x - j, y - j))
                                    }

                                    else -> break
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    return flag
}

fun canMove(x: Int, y: Int): Boolean {
    var flag = false
    Moves.clear()
    when (fields[y][x].Queen) {
        false -> {
            when (turn) {
                Color.WHITE -> {
                    when (x) {
                        0 -> {
                            if (!fields[y + 1][x + 1].notEmpty) {
                                Moves.add(Pair(x + 1, y + 1)); flag = true
                            }
                        }

                        SIZE - 1 -> {
                            if (!fields[y + 1][x - 1].notEmpty) {
                                Moves.add(Pair(x - 1, y + 1)); flag = true
                            }
                        }

                        else -> {
                            if (!fields[y + 1][x + 1].notEmpty) {
                                Moves.add(Pair(x + 1, y + 1)); flag = true
                            }
                            if (!fields[y + 1][x - 1].notEmpty) {
                                Moves.add(Pair(x - 1, y + 1)); flag = true
                            }
                        }
                    }
                }

                Color.BLACK -> {
                    when (x) {
                        0 -> {
                            if (!fields[y - 1][x + 1].notEmpty) {
                                Moves.add(Pair(x + 1, y - 1)); flag = true
                            }
                        }

                        SIZE - 1 -> {
                            if (!fields[y - 1][x - 1].notEmpty) {
                                Moves.add(Pair(x - 1, y - 1)); flag = true
                            }
                        }

                        else -> {
                            if (!fields[y - 1][x + 1].notEmpty) {
                                Moves.add(Pair(x + 1, y - 1)); flag = true
                            }
                            if (!fields[y - 1][x - 1].notEmpty) {
                                Moves.add(Pair(x - 1, y - 1)); flag = true
                            }
                        }
                    }
                }
            }
        }

        true -> {
            val down = y
            val up = SIZE - 1 - y
            val left = x
            val right = SIZE - 1 - x
            for (i in (1..minOf(up, right))) {
                when (fields[y + i][x + i].notEmpty) {
                    true -> break
                    false -> {
                        Moves.add(Pair(x + i, y + i)); flag = true
                    }
                }
            }
            for (i in (1..minOf(down, right))) {
                when (fields[y - i][x + i].notEmpty) {
                    true -> break
                    false -> {
                        Moves.add(Pair(x + i, y - i)); flag = true
                    }
                }
            }
            for (i in (1..minOf(up, left))) {
                when (fields[y + i][x - i].notEmpty) {
                    true -> break
                    false -> {
                        Moves.add(Pair(x - i, y + i)); flag = true
                    }
                }
            }
            for (i in (1..minOf(down, left))) {
                when (fields[y - i][x - i].notEmpty) {
                    true -> break
                    false -> {
                        Moves.add(Pair(x - i, y - i)); flag = true
                    }
                }
            }
        }
    }
    return flag
}


fun createWindow(title: String) = runBlocking(Dispatchers.Swing) {
    val skiaLayer = SkiaLayer()
    SwingUtilities.invokeLater {
        val window = JFrame("Checkers game").apply {
            defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
            preferredSize = Dimension(830, 820)
        }

        val l: MouseListener = object : MouseListener {
            override fun mouseClicked(e: MouseEvent) {
                mouseInteract(e.x, e.y, skiaLayer)
            }

            override fun mouseExited(e: MouseEvent) {}
            override fun mousePressed(e: MouseEvent) {
                mouseInteract(e.x, e.y, skiaLayer)
            }

            override fun mouseEntered(e: MouseEvent) {
            }

            override fun mouseReleased(e: MouseEvent) {}
        }
        skiaLayer.addMouseListener(l)
        boardRender(skiaLayer)
        skiaLayer.attachTo(window.contentPane)
        skiaLayer.needRedraw()
        window.pack()
        window.isVisible = true
        window.isResizable = false
    }
}

fun mouseInteract(X: Int, Y: Int, skiaLayer: SkiaLayer) {
    var anyEat = false
    val moveForThis = mutableListOf<Pair<Int, Int>>()
    Moves.forEach { moveForThis.add(it) }
    repeat(64) {
        if (canEat(it % 8, it / 8)) {
            anyEat = true
        }
    }
    if (X.toFloat() in board && Y.toFloat() in board) {
        val x = ((X - LEFT) / SQUARE).toInt()
        val y = 7 - ((Y - TOP) / SQUARE).toInt()
        val field = fields[y][x]
        if (Pair(x, y) !in moveForThis) {
            isActive = false
            if (!field.notEmpty) {
                Moves.clear();moveForThis.clear(); boardRender(skiaLayer)
            }
            if (field.color == turn) {
                if (anyEat) {
                    if (canEat(x, y)) {
                        boardRender(skiaLayer)
                        isActive = true
                        ancestor.condition = Condition.ATTACK
                        ancestor.x = x
                        ancestor.y = y
                    }
                } else {
                    if (canMove(x, y)) {
                        boardRender(skiaLayer)
                        isActive = true
                        ancestor.condition = Condition.MOVE
                        ancestor.x = x
                        ancestor.y = y
                    }
                }
            }
        } else {
            when (ancestor.condition) {
                Condition.ATTACK -> {
                    var attacked: Pair<Int?, Int?> = Pair(null, null)
                    var eatX: Int
                    var eatY: Int
                    fields[y][x] = fields[ancestor.y!!][ancestor.x!!]
                    fields[ancestor.y!!][ancestor.x!!] = empty
                    val signX = (-x + ancestor.x!!) / abs(x - ancestor.x!!)
                    val signY = (-y + ancestor.y!!) / abs(y - ancestor.y!!)
                    repeat(abs(x - ancestor.x!!) - 1) {
                        if (fields[y + signY * (it + 1)][x + signX * (it + 1)].notEmpty) {
                            attacked = Pair(x + signX * (it + 1), y + signY * (it + 1))
                        }
                    }
                    fields[attacked.second!!][attacked.first!!].canBeEaten = false
                    Eaten.add(attacked as Pair<Int, Int>)
                    when {
                        (turn == Color.BLACK) and (y == 0) -> fields[y][x].Queen = true
                        (turn == Color.WHITE) and (y == SIZE - 1) -> fields[y][x].Queen = true
                    }
                    if (!canEat(x, y)) {
                        Eaten.forEach { fields[it.second][it.first] = empty }
                        Eaten.clear()

                        turn = when (turn) {
                            Color.BLACK -> Color.WHITE
                            Color.WHITE -> Color.BLACK
                        }
                        boardRender(skiaLayer)
                        ancestor.x = null
                        ancestor.y = null
                        ancestor.condition = Condition.PASSIVE

                    } else {
                        ancestor.x = x
                        ancestor.y = y
                        when {
                            (turn == Color.BLACK) and (y == 0) -> {
                                fields[y][x].Queen = true
                            }

                            (turn == Color.WHITE) and (y == SIZE - 1) -> {
                                fields[y][x].Queen = true
                            }
                        }
                        mouseInteract(x, y, skiaLayer)
                    }
                }

                Condition.MOVE -> {
                    Moves.clear()

                    isActive = false
                    ancestor.condition = Condition.PASSIVE
                    fields[y][x] = fields[ancestor.y!!][ancestor.x!!]
                    fields[ancestor.y!!][ancestor.x!!] = empty
                    when {
                        (turn == Color.BLACK) and (y == 0) -> fields[y][x].Queen = true
                        (turn == Color.WHITE) and (y == SIZE - 1) -> fields[y][x].Queen = true
                    }
                    turn = when (turn) {
                        Color.BLACK -> Color.WHITE
                        Color.WHITE -> Color.BLACK
                    }
                    boardRender(skiaLayer)
                    ancestor.x = null
                    ancestor.y = null
                }
            }
        }
    } else {
        Moves.clear();
        boardRender(skiaLayer)
    }
}







