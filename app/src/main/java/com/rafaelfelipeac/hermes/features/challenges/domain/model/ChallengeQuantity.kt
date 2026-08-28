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
        val byQuantity = linkedMapOf<Long, Int>()

        listOf(25 to BigDecimal("0.25"), 50 to BigDecimal("0.50"), 100 to BigDecimal("1.00"))
            .forEach { (percentage, fraction) ->
                val quantity =
                    BigDecimal.valueOf(baseValue)
                        .multiply(fraction)
                        .setScale(0, RoundingMode.HALF_UP)
                        .longValueExact()

                if (quantity > 0L) {
                    val existingPercentage = byQuantity[quantity]
                    if (existingPercentage == null || percentage > existingPercentage) {
                        byQuantity[quantity] = percentage
                    }
                }
            }

        return byQuantity.entries
            .sortedBy { it.value }
            .map { (quantity, percentage) ->
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
