package com.example.calc

import android.content.Intent
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.calc.R.*

class MainActivity : AppCompatActivity() {

    private var tvDisplay: TextView? = null
    private var tvHistory: TextView? = null
    private var lastNumeric: Boolean = false
    private var stateError: Boolean = false
    private var lastDot: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_main)

        tvDisplay = findViewById(id.tvDisplay)
        tvHistory = findViewById(id.tvHistory)

        // Enable scrolling for history
        tvHistory?.movementMethod = ScrollingMovementMethod()

        // Button listeners
        findViewById<Button>(id.btnAC).setOnClickListener {
            tvDisplay?.text = ""
            lastNumeric = false
            stateError = false
            lastDot = false
        }

        // DEL Button (Delete last character)
        findViewById<Button>(id.btnDel).setOnClickListener {
            val currentText = tvDisplay?.text.toString()
            if (currentText.isNotEmpty()) {
                tvDisplay?.text = currentText.dropLast(1)
            }
        }

        findViewById<Button>(id.btnEquals).setOnClickListener { onEqual() }

        // Set listeners for Operators
        val opListener = View.OnClickListener { view -> onOperator(view) }
        findViewById<Button>(id.btnDivide).setOnClickListener(opListener)
        findViewById<Button>(id.btnMultiply).setOnClickListener(opListener)
        findViewById<Button>(id.btnMinus).setOnClickListener(opListener)
        findViewById<Button>(id.btnPlus).setOnClickListener(opListener)
    }

    // Linked via android:onClick="onDigitClick" in XML
    fun onDigitClick(view: View) {
        if (stateError) {
            tvDisplay?.text = (view as Button).text
            stateError = false
        } else {
            tvDisplay?.append((view as Button).text)
        }
        lastNumeric = true
    }

    private fun onOperator(view: View) {
        if (lastNumeric && !stateError) {
            tvDisplay?.append((view as Button).text)
            lastNumeric = false
            lastDot = false
        }
    }

    private fun onEqual() {
        val content = tvDisplay?.text.toString()

        // --- SECRET MECHANISM ---
        if (content == BuildConfig.SECRET_CODE) {
            // Clear the display so no one sees the code
            tvDisplay?.text = ""
            // Navigate to Secret Activity
            val intent = Intent(this, SecretNotesActivity::class.java)
            startActivity(intent)
            return
        }
        // ------------------------

        if (lastNumeric && !stateError) {
            val txt = tvDisplay?.text.toString()
            try {
                // Perform Calculation
                val result = calculateResult(txt)

                // Display Result
                tvDisplay?.text = result
                lastDot = true

                // --- HISTORY (Brownie Points) ---
                // Add "Equation = Result" to history, separated by new lines
                val historyEntry = "$txt = $result\n"
                tvHistory?.append(historyEntry)
                // -------------------------------

            } catch (_: Exception) {
                tvDisplay?.text = "Error"
                stateError = true
                lastNumeric = false
            }
        }
    }

    // Manual Calculation Logic (No external libraries)
    private fun calculateResult(input: String): String {
        var strToParse = input
        var prefix = ""

        if (strToParse.startsWith("-")) {
            prefix = "-"
            strToParse = strToParse.substring(1)
        }

        val result: Double

        if (strToParse.contains("+")) {
            val split = strToParse.split("+")
            result = (prefix + split[0]).toDouble() + split[1].toDouble()
        } else if (strToParse.contains("-")) {
            // Note: simple split won't handle multiple minuses perfectly in manual mode
            val split = strToParse.split("-")
            result = (prefix + split[0]).toDouble() - split[1].toDouble()
        } else if (strToParse.contains("*")) {
            val split = strToParse.split("*")
            result = (prefix + split[0]).toDouble() * split[1].toDouble()
        } else if (strToParse.contains("/")) {
            val split = strToParse.split("/")
            result = (prefix + split[0]).toDouble() / split[1].toDouble()
        } else {
            return input // No operator found
        }

        // Format: Remove .0 if it's a whole number
        return if (result % 1.0 == 0.0) {
            result.toInt().toString()
        } else {
            result.toString()
        }
    }
}