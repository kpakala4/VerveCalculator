package com.example.vervecalculator

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.vervecalculator.databinding.ActivityMainBinding
import net.pubnative.lite.sdk.HyBidError
import net.pubnative.lite.sdk.api.HyBidInterstitialAd
import net.pubnative.lite.sdk.interstitial.HyBidInterstitialAdListener
import net.pubnative.lite.sdk.utils.Logger
import net.pubnative.lite.sdk.views.HyBidAdViewListener

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var interstitialAd: HyBidInterstitialAd
    private var interstitialReady = false
    private val expressionBuilder = StringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupCalculatorButtons()
        initBanner()
        initInterstitial()
    }

    private fun setupCalculatorButtons() {
        val numericButtons = setOf(
            binding.button0,
            binding.button1,
            binding.button2,
            binding.button3,
            binding.button4,
            binding.button5,
            binding.button6,
            binding.button7,
            binding.button8,
            binding.button9
        )

        numericButtons.forEach { button ->
            button.setOnClickListener {
                appendToExpression(button.text.toString())
            }
        }

        binding.buttonDecimal.setOnClickListener { appendToExpression(".") }
        binding.buttonPlus.setOnClickListener { appendOperator("+") }
        binding.buttonMinus.setOnClickListener { appendOperator("-") }
        binding.buttonMultiply.setOnClickListener { appendOperator("×") }
        binding.buttonDivide.setOnClickListener { appendOperator("/") }

        binding.buttonClear.setOnClickListener {
            expressionBuilder.clear()
            updateExpressionDisplay()
        }

        binding.buttonDelete.setOnClickListener {
            if (expressionBuilder.isNotEmpty()) {
                expressionBuilder.deleteCharAt(expressionBuilder.lastIndex)
                updateExpressionDisplay()
            }
        }

        binding.buttonEquals.setOnClickListener {
            val expression = expressionBuilder.toString()
            val result = evaluateExpression(expression)
            if (result != null) {
                val display = if (result % 1.0 == 0.0) {
                    result.toLong().toString()
                } else {
                    "%.4f".format(result).trimEnd('0').trimEnd('.')
                }
                expressionBuilder.clear().append(display)
                updateExpressionDisplay()
                showInterstitial()
            }
        }
    }

    private fun appendToExpression(token: String) {
        if (token == ".") {
            val lastNumber = expressionBuilder.substringAfterLastAnyOf(setOf("+", "-", "×", "/"))
            if (lastNumber.contains('.')) {
                return
            }
            if (lastNumber.isEmpty()) {
                expressionBuilder.append('0')
            }
        }
        expressionBuilder.append(token)
        updateExpressionDisplay()
    }

    private fun StringBuilder.substringAfterLastAnyOf(delimiters: Set<String>): String {
        if (isEmpty()) return ""
        val lastIndex = delimiters.map { lastIndexOf(it) }.maxOrNull() ?: -1
        return if (lastIndex == -1) toString() else substring(lastIndex + 1)
    }

    private fun appendOperator(operator: String) {
        if (expressionBuilder.isEmpty()) return
        val lastChar = expressionBuilder.last()
        if (lastChar in charArrayOf('+', '-', '×', '/')) {
            expressionBuilder[expressionBuilder.lastIndex] = operator.first()
        } else {
            expressionBuilder.append(operator)
        }
        updateExpressionDisplay()
    }

    private fun updateExpressionDisplay() {
        binding.resultView.text = if (expressionBuilder.isEmpty()) "0" else expressionBuilder.toString()
    }

    private fun evaluateExpression(expression: String): Double? {
        if (expression.isBlank()) return null
        return try {
            val tokens = tokenize(expression)
            val rpn = shuntingYard(tokens)
            calculateRpn(rpn)
        } catch (ex: IllegalArgumentException) {
            Logger.e("Calculator", "Evaluation error", ex)
            null
        }
    }

    private fun tokenize(expression: String): List<String> {
        val tokens = mutableListOf<String>()
        var index = 0
        while (index < expression.length) {
            val char = expression[index]
            when {
                char.isDigit() || char == '.' -> {
                    val start = index
                    index++
                    while (index < expression.length && (expression[index].isDigit() || expression[index] == '.')) {
                        index++
                    }
                    tokens += expression.substring(start, index)
                }
                char in setOf('+', '-', '×', '/') -> {
                    tokens += char.toString()
                    index++
                }
                char.isWhitespace() -> index++
                else -> throw IllegalArgumentException("Invalid character: $char")
            }
        }
        return tokens
    }

    private fun shuntingYard(tokens: List<String>): List<String> {
        val output = mutableListOf<String>()
        val operators = ArrayDeque<String>()
        val precedence = mapOf("+" to 1, "-" to 1, "×" to 2, "/" to 2)
        tokens.forEach { token ->
            when {
                token.toDoubleOrNull() != null -> output += token
                precedence.containsKey(token) -> {
                    while (operators.isNotEmpty() && precedence.getValue(operators.last()) >= precedence.getValue(token)) {
                        output += operators.removeLast()
                    }
                    operators.addLast(token)
                }
                else -> throw IllegalArgumentException("Unsupported token $token")
            }
        }
        while (operators.isNotEmpty()) {
            output += operators.removeLast()
        }
        return output
    }

    private fun calculateRpn(tokens: List<String>): Double {
        val stack = ArrayDeque<Double>()
        tokens.forEach { token ->
            val number = token.toDoubleOrNull()
            if (number != null) {
                stack.addLast(number)
            } else {
                if (stack.size < 2) throw IllegalArgumentException("Bad expression")
                val right = stack.removeLast()
                val left = stack.removeLast()
                val result = when (token) {
                    "+" -> left + right
                    "-" -> left - right
                    "×" -> left * right
                    "/" -> left / right
                    else -> throw IllegalArgumentException("Unknown operator")
                }
                stack.addLast(result)
            }
        }
        if (stack.size != 1) throw IllegalArgumentException("Bad expression")
        return stack.last()
    }

    private fun initBanner() {
        binding.bannerStatus.text = getString(R.string.banner_status_loading)
        binding.bannerView.load(HybridApp.ZONE_ID, object : HyBidAdViewListener {
            override fun onAdLoaded() {
                binding.bannerStatus.text = getString(R.string.banner_status_loaded)
            }

            override fun onAdLoadFailed(error: HyBidError?) {
                binding.bannerStatus.text = getString(R.string.banner_status_failed)
            }

            override fun onAdImpression() {
                // No-op
            }

            override fun onAdClick() {
                // No-op
            }
        })
    }

    private fun initInterstitial() {
        interstitialAd = HyBidInterstitialAd(this, object : HyBidInterstitialAdListener {
            override fun onInterstitialLoaded() {
                interstitialReady = true
                binding.bannerStatus.text = getString(R.string.interstitial_ready)
            }

            override fun onInterstitialLoadFailed(error: HyBidError?) {
                interstitialReady = false
                binding.bannerStatus.text = getString(R.string.interstitial_failed)
            }

            override fun onInterstitialImpression() {
                interstitialReady = false
                binding.bannerStatus.text = getString(R.string.interstitial_loading)
            }

            override fun onInterstitialClick() {
                // No-op
            }

            override fun onInterstitialDismissed() {
                prepareInterstitial()
            }
        })
        prepareInterstitial()
    }

    private fun prepareInterstitial() {
        interstitialReady = false
        binding.bannerStatus.text = getString(R.string.interstitial_loading)
        interstitialAd.load(HybridApp.ZONE_ID)
    }

    private fun showInterstitial() {
        if (interstitialReady && interstitialAd.isReady) {
            interstitialReady = false
            interstitialAd.show()
        } else {
            prepareInterstitial()
        }
    }

    override fun onDestroy() {
        binding.bannerView.destroy()
        interstitialAd.destroy()
        super.onDestroy()
    }
}
