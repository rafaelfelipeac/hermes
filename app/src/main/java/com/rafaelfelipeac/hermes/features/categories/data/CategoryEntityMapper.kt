package com.rafaelfelipeac.hermes.features.categories.data

import com.rafaelfelipeac.hermes.features.categories.data.local.CategoryEntity
import com.rafaelfelipeac.hermes.features.categories.domain.model.Category

internal fun CategoryEntity.toDomain(): Category {
    return Category(
        id = id,
        name = name,
        colorId = colorId,
        sortOrder = sortOrder,
        isHidden = isHidden,
        isSystem = isSystem,
    )
}

internal fun Category.toEntity(): CategoryEntity {
    return CategoryEntity(
        id = id,
        name = name,
        colorId = colorId,
        sortOrder = sortOrder,
        isHidden = isHidden,
        isSystem = isSystem,
    )
}
