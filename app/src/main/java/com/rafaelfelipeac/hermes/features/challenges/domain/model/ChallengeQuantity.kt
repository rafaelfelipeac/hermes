package com.rafaelfelipeac.hermes.features.challenges.domain.model

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.text.ParsePosition
import java.util.Locale

object ChallengeQuantity {
    @Suppress("ReturnCount")
    fun parseLocalized(
        raw: String,
        locale: Locale,
    ): Long? {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return null

        val formatter = NumberFormat.getNumberInstance(locale)
        val position = ParsePosition(0)
        val parsed = formatter.parse(trimmed, position) ?: return null
        if (position.index != trimmed.length) return null

        val decimal = parsed.toString().toBigDecimalOrNull() ?: return null
        if (decimal.signum() <= 0 || decimal.stripTrailingZeros().scale() > 0) return null
        return try {
            decimal.longValueExact()
        } catch (_: ArithmeticException) {
            null
        }
    }

    fun format(
        value: Long,
        locale: Locale,
    ): String {
        val formatter = NumberFormat.getIntegerInstance(locale)
        formatter.isGroupingUsed = false
        return formatter.format(value)
    }

    fun quickAddValues(baseValue: Long): List<ChallengeQuickAddValue> {
        var minimumQuantity = 1L

        return listOf(25 to BigDecimal("0.25"), 50 to BigDecimal("0.50"), 100 to BigDecimal("1.00"))
            .map { (percentage, fraction) ->
                val roundedQuantity =
                    BigDecimal.valueOf(baseValue)
                        .multiply(fraction)
                        .setScale(0, RoundingMode.HALF_UP)
                        .longValueExact()
                        .coerceAtLeast(1L)
                val quantity = roundedQuantity.coerceAtLeast(minimumQuantity)
                minimumQuantity = Math.addExact(quantity, 1L)
                ChallengeQuickAddValue(percentage = percentage, quantity = quantity)
            }
    }

    fun add(
        left: Long,
        right: Long,
    ): Long {
        return Math.addExact(left, right)
    }

    fun subtract(
        left: Long,
        right: Long,
    ): Long {
        return Math.subtractExact(left, right)
    }

    fun multiply(
        value: Long,
        multiplier: Long,
    ): Long {
        return Math.multiplyExact(value, multiplier)
    }

    fun multiplyAndDivideFloor(
        numerator: Long,
        multiplier: Long,
        denominator: Long,
    ): Long {
        require(denominator > 0)
        return Math.multiplyExact(numerator, multiplier) / denominator
    }

    fun ceilDiv(
        numerator: Long,
        denominator: Long,
    ): Long {
        require(denominator > 0)
        if (numerator <= 0) return 0L
        return ((numerator - 1) / denominator) + 1
    }

    fun cappedProgress(
        completed: Long,
        target: Long,
    ): Double {
        if (target <= 0) return 0.0
        return minOf(1.0, completed.toDouble() / target.toDouble())
    }
}
