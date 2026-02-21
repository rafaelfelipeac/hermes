package com.rafaelfelipeac.hermes.features.categories.presentation

import com.rafaelfelipeac.hermes.features.categories.domain.model.Category
import com.rafaelfelipeac.hermes.features.categories.presentation.model.CategoryUi

fun Category.toUi(): CategoryUi {
    return CategoryUi(
        id = id,
        name = name,
        colorId = colorId,
        sortOrder = sortOrder,
        isHidden = isHidden,
        isSystem = isSystem,
    )
}
