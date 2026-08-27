@file:Suppress("ReturnCount")

package com.rafaelfelipeac.hermes.features.challenges.domain.model

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.min

object ChallengeQuantity {
    const val SCALE = 1_000L

    fun parseLocalized(
        raw: String,
        locale: Locale,
    ): Long? {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return null

        val symbols = DecimalFormatSymbols.getInstance(locale)
        val normalized =
            buildString(trimmed.length) {
                trimmed.forEach { character ->
                    when (character) {
                        symbols.groupingSeparator,
                        '\u00A0',
                        ' ',
                        -> Unit

                        symbols.decimalSeparator -> append('.')
                        else -> append(character)
                    }
                }
            }

        val value = normalized.toBigDecimalOrNull() ?: return null
        if (value <= BigDecimal.ZERO) return null
        if (value.scale() > 3) return null

        return value
            .movePointRight(3)
            .setScale(0, RoundingMode.UNNECESSARY)
            .longValueExact()
    }

    fun format(
        scaledValue: Long,
        locale: Locale,
    ): String {
        val formatter = NumberFormat.getNumberInstance(locale) as DecimalFormat
        formatter.isGroupingUsed = false
        formatter.maximumFractionDigits = 3
        formatter.minimumFractionDigits = 0
        formatter.roundingMode = RoundingMode.HALF_UP

        val decimal = BigDecimal.valueOf(scaledValue, 3).stripTrailingZeros()
        return formatter.format(decimal)
    }

    fun quickAddValues(baseValue: Long): List<Long> {
        val candidates =
            listOf(0.25, 0.5, 1.0)
                .map { fraction ->
                    BigDecimal.valueOf(baseValue)
                        .multiply(BigDecimal.valueOf(fraction))
                        .setScale(0, RoundingMode.HALF_UP)
                        .longValueExact()
                }

        return candidates.filter { it > 0 }.distinct()
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
        return min(1.0, completed.toDouble() / target.toDouble())
    }
}
