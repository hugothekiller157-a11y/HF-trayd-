package com.hftrader.app

import android.graphics.*
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.sin
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var chart: ChartView
    private lateinit var priceText: TextView
    private lateinit var positionText: TextView
    private lateinit var pnlText: TextView
    private lateinit var statusText: TextView
    private var price = 132450.0
    private var position = 0
    private var entry = 0.0
    private var pnl = 0.0
    private var qty = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 18, 24, 12)
            setBackgroundColor(Color.rgb(11,15,20))
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val title = TextView(this).apply {
            text = "HF TRADER"
            textSize = 22f
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
        }
        statusText = TextView(this).apply {
            text = "  ● SIMULADOR"
            textSize = 13f
            setTextColor(Color.rgb(0,200,83))
        }
        header.addView(title, LinearLayout.LayoutParams(0, 50, 1f))
        header.addView(statusText)
        root.addView(header)

        val asset = TextView(this).apply {
            text = "WINFUT   •   Mini índice"
            textSize = 15f
            setTextColor(Color.LTGRAY)
        }
        root.addView(asset)

        priceText = TextView(this).apply {
            text = formatPrice()
            textSize = 28f
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
        }
        root.addView(priceText)

        chart = ChartView()
        root.addView(chart, LinearLayout.LayoutParams(-1, 0, 1f))

        val info = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        positionText = TextView(this).apply {
            text = "Posição: FLAT"
            textSize = 14f
            setTextColor(Color.LTGRAY)
        }
        pnlText = TextView(this).apply {
            text = "P&L: R$ 0,00"
            textSize = 14f
            setTextColor(Color.WHITE)
        }
        info.addView(positionText, LinearLayout.LayoutParams(0, 48, 1f))
        info.addView(pnlText, LinearLayout.LayoutParams(0, 48, 1f))
        root.addView(info)

        val qtyRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val qtyLabel = TextView(this).apply {
            text = "Quantidade"
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER_VERTICAL
        }
        val qtyEdit = EditText(this).apply {
            setText("1")
            inputType = 2
            setTextColor(Color.WHITE)
            setSingleLine()
        }
        qtyRow.addView(qtyLabel, LinearLayout.LayoutParams(0, 55, 1f))
        qtyRow.addView(qtyEdit, LinearLayout.LayoutParams(100, 55))
        root.addView(qtyRow)

        val buttons = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        val buy = Button(this).apply {
            text = "COMPRAR"
            setOnClickListener {
                qty = qtyEdit.text.toString().toIntOrNull()?.coerceAtLeast(1) ?: 1
                if (position <= 0) {
                    entry = price
                    position = qty
                }
                updateUI()
            }
        }
        val sell = Button(this).apply {
            text = "VENDER"
            setOnClickListener {
                qty = qtyEdit.text.toString().toIntOrNull()?.coerceAtLeast(1) ?: 1
                if (position >= 0) {
                    entry = price
                    position = -qty
                }
                updateUI()
            }
        }
        val close = Button(this).apply {
            text = "ZERAR"
            setOnClickListener {
                if (position != 0) {
                    pnl += (price - entry) * position
                }
                position = 0
                entry = 0.0
                updateUI()
            }
        }
        buttons.addView(buy, LinearLayout.LayoutParams(0, 60, 1f))
        buttons.addView(sell, LinearLayout.LayoutParams(0, 60, 1f))
        buttons.addView(close, LinearLayout.LayoutParams(0, 60, 1f))
        root.addView(buttons)

        val note = TextView(this).apply {
            text = "V1 • Modo simulação • Sem envio de ordens reais"
            textSize = 11f
            gravity = Gravity.CENTER
            setTextColor(Color.GRAY)
        }
        root.addView(note, LinearLayout.LayoutParams(-1, 34))

        setContentView(root)
        chart.postDelayed(object : Runnable {
            override fun run() {
                price += Random.nextDouble(-35.0, 35.0)
                updateUI()
                chart.invalidate()
                chart.postDelayed(this, 1200)
            }
        }, 1200)
    }

    private fun formatPrice() = "R$ %,.2f".format(price).replace(",", "X").replace(".", ",").replace("X", ".")

    private fun updateUI() {
        priceText.text = formatPrice()
        val openPnl = if (position != 0) (price - entry) * position else 0.0
        val total = pnl + openPnl
        positionText.text = if (position == 0) "Posição: FLAT" else "Posição: ${if (position > 0) "LONG" else "SHORT"} x${kotlin.math.abs(position)}"
        pnlText.text = "P&L: R$ %,.2f".format(total).replace(",", "X").replace(".", ",").replace("X", ".")
    }

    inner class ChartView : View(this@MainActivity) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        override fun onDraw(c: Canvas) {
            super.onDraw(c)
            c.drawColor(Color.rgb(14,19,26))
            paint.strokeWidth = 1f
            paint.color = Color.rgb(40,48,58)
            for (i in 1..5) {
                val y = height * i / 6f
                c.drawLine(0f, y, width.toFloat(), y, paint)
            }
            val path = Path()
            val n = 60
            for (i in 0 until n) {
                val x = i * width.toFloat() / (n - 1)
                val y = height * 0.5f - sin(i * 0.42) * height * 0.12f -
                        sin(i * 0.11) * height * 0.08f - (price % 1000) / 1000f * 25f
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            paint.color = Color.rgb(0, 200, 83)
            paint.strokeWidth = 4f
            paint.style = Paint.Style.STROKE
            c.drawPath(path, paint)
            paint.style = Paint.Style.FILL
            paint.color = Color.GRAY
            paint.textSize = 24f
            c.drawText("SIMULAÇÃO", 20f, 32f, paint)
        }
    }
}
