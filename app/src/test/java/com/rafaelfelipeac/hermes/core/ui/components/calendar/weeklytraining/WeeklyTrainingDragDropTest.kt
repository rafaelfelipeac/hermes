package com.rafaelfelipeac.hermes.core.ui.components.calendar.weeklytraining

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutUi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.TUESDAY

class WeeklyTrainingDragDropTest {
    @Test
    fun computeDropPreview_slotModeMorning_returnsExpectedOrder() {
        val dragged = workout(id = 3, day = MONDAY, order = 2, slot = TimeSlot.MORNING)
        val morningA = workout(id = 1, day = MONDAY, order = 0, slot = TimeSlot.MORNING)
        val morningB = workout(id = 2, day = MONDAY, order = 1, slot = TimeSlot.MORNING)
        val daySection = SectionKey.Day(MONDAY)
        val context =
            dropContext(
                workouts = listOf(morningA, morningB, dragged),
                section = daySection,
                sectionRect = Rect(left = 0f, top = 100f, right = 300f, bottom = 400f),
                itemBounds =
                    mapOf(
                        morningA.id to Rect(left = 0f, top = 120f, right = 280f, bottom = 160f),
                        morningB.id to Rect(left = 0f, top = 220f, right = 280f, bottom = 260f),
                    ),
                usesSlots = true,
            )

        val preview =
            computeDropPreview(
                draggedWorkoutId = dragged.id,
                dragPosition = Offset(x = 20f, y = 180f),
                context = context,
            )

        assertEquals(daySection, preview?.targetSection)
        assertEquals(TimeSlot.MORNING, preview?.targetTimeSlot)
        assertEquals(1, preview?.targetOrder)
    }

    @Test
    fun computeDropPreview_slotModeAfternoon_assignsAfternoonSlot() {
        val dragged = workout(id = 9, day = MONDAY, order = 0, slot = TimeSlot.MORNING)
        val daySection = SectionKey.Day(MONDAY)
        val context =
            dropContext(
                workouts = listOf(dragged),
                section = daySection,
                sectionRect = Rect(left = 0f, top = 0f, right = 320f, bottom = 300f),
                itemBounds = emptyMap(),
                usesSlots = true,
            )

        val preview =
            computeDropPreview(
                draggedWorkoutId = dragged.id,
                dragPosition = Offset(x = 16f, y = 150f),
                context = context,
            )

        assertEquals(TimeSlot.AFTERNOON, preview?.targetTimeSlot)
    }

    @Test
    fun computeDropPreview_slotModeUsesActualSlotBounds_whenCardsHaveUnevenHeights() {
        val dragged = workout(id = 20, day = MONDAY, order = 0, slot = TimeSlot.MORNING)
        val daySection = SectionKey.Day(MONDAY)
        val context =
            dropContext(
                workouts = listOf(dragged),
                section = daySection,
                sectionRect = Rect(left = 0f, top = 0f, right = 320f, bottom = 500f),
                slotBounds =
                    mapOf(
                        SlotSectionKey(daySection, TimeSlot.MORNING) to
                            Rect(left = 0f, top = 60f, right = 320f, bottom = 320f),
                        SlotSectionKey(daySection, TimeSlot.AFTERNOON) to
                            Rect(left = 0f, top = 330f, right = 320f, bottom = 380f),
                        SlotSectionKey(daySection, TimeSlot.NIGHT) to
                            Rect(left = 0f, top = 390f, right = 320f, bottom = 440f),
                    ),
                itemBounds = emptyMap(),
                usesSlots = true,
            )

        val preview =
            computeDropPreview(
                draggedWorkoutId = dragged.id,
                dragPosition = Offset(x = 16f, y = 350f),
                context = context,
            )

        assertEquals(TimeSlot.AFTERNOON, preview?.targetTimeSlot)
    }

    @Test
    fun handleDrop_usesSameOrderAsPreview() {
        val dragged = workout(id = 30, day = MONDAY, order = 2, slot = TimeSlot.MORNING)
        val morningA = workout(id = 11, day = MONDAY, order = 0, slot = TimeSlot.MORNING)
        val morningB = workout(id = 12, day = MONDAY, order = 1, slot = TimeSlot.MORNING)
        val daySection = SectionKey.Day(MONDAY)
        var moved: MoveCapture? = null
        val context =
            dropContext(
                workouts = listOf(morningA, morningB, dragged),
                section = daySection,
                sectionRect = Rect(left = 0f, top = 100f, right = 300f, bottom = 400f),
                itemBounds =
                    mapOf(
                        morningA.id to Rect(left = 0f, top = 120f, right = 280f, bottom = 160f),
                        morningB.id to Rect(left = 0f, top = 220f, right = 280f, bottom = 260f),
                    ),
                usesSlots = true,
                onMoved = { id, day, slot, order ->
                    moved = MoveCapture(id = id, day = day, slot = slot, order = order)
                },
            )
        val drop = Offset(x = 20f, y = 180f)
        val preview = computeDropPreview(dragged.id, drop, context)

        handleDrop(
            draggedWorkoutId = dragged.id,
            dragPosition = drop,
            context = context,
        )

        assertEquals(preview?.targetSection?.dayOfWeekOrNull(), moved?.day)
        assertEquals(preview?.targetTimeSlot, moved?.slot)
        assertEquals(preview?.targetOrder, moved?.order)
    }

    @Test
    fun handleDrop_whenSectionHasNoSlots_keepsTimeSlotNull() {
        val dragged = workout(id = 100, day = MONDAY, order = 0, slot = TimeSlot.MORNING)
        val target = SectionKey.Day(TUESDAY)
        var moved: MoveCapture? = null
        val context =
            dropContext(
                workouts = listOf(dragged),
                section = target,
                sectionRect = Rect(left = 0f, top = 400f, right = 320f, bottom = 700f),
                itemBounds = emptyMap(),
                usesSlots = false,
                onMoved = { id, day, slot, order ->
                    moved = MoveCapture(id = id, day = day, slot = slot, order = order)
                },
            )

        handleDrop(
            draggedWorkoutId = dragged.id,
            dragPosition = Offset(x = 16f, y = 500f),
            context = context,
            targetSectionOverride = target,
        )

        assertEquals(TUESDAY, moved?.day)
        assertNull(moved?.slot)
    }
}

private data class MoveCapture(
    val id: Long,
    val day: java.time.DayOfWeek?,
    val slot: TimeSlot?,
    val order: Int,
)

private fun dropContext(
    workouts: List<WorkoutUi>,
    section: SectionKey,
    sectionRect: Rect,
    slotBounds: Map<SlotSectionKey, Rect> = emptyMap(),
    itemBounds: Map<Long, Rect>,
    usesSlots: Boolean,
    onMoved: (Long, java.time.DayOfWeek?, TimeSlot?, Int) -> Unit = { _, _, _, _ -> },
): DropContext {
    val workoutsBySection =
        mapOf(
            section to workouts.filter { it.dayOfWeek == section.dayOfWeekOrNull() },
        )
    val dayUsesSlots =
        section.dayOfWeekOrNull()
            ?.let { mapOf(it to usesSlots) }
            .orEmpty()

    return DropContext(
        workouts = workouts,
        workoutsBySection = workoutsBySection,
        sectionBounds = mapOf(section to sectionRect),
        slotBounds = slotBounds,
        dayUsesSlots = dayUsesSlots,
        itemBounds = itemBounds,
        onWorkoutMoved = onMoved,
    )
}

private fun workout(
    id: Long,
    day: java.time.DayOfWeek?,
    order: Int,
    slot: TimeSlot?,
): WorkoutUi {
    return WorkoutUi(
        id = id,
        dayOfWeek = day,
        type = "Type$id",
        description = "",
        isCompleted = false,
        isRestDay = false,
        categoryId = null,
        categoryColorId = null,
        categoryName = null,
        order = order,
        eventType = EventType.WORKOUT,
        timeSlot = slot,
    )
}
