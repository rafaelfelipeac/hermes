package com.rafaelfelipeac.hermes.core.strings

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

interface StringProvider {
    fun get(id: Int, vararg args: Any): String
}

@Singleton
class AndroidStringProvider
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
    ) : StringProvider {
        override fun get(id: Int, vararg args: Any): String {
            return context.getString(id, *args)
        }
    }
