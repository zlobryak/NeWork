package ru.netology.nework.utils

import android.os.Bundle
import ru.netology.nework.data.dto.PostItem
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

object PostItemArg: ReadWriteProperty<Bundle, PostItem?> {
    override fun getValue(
        thisRef: Bundle,
        property: KProperty<*>
    ): PostItem? {
        return thisRef.getParcelable(property.name)
    }

    override fun setValue(
        thisRef: Bundle,
        property: KProperty<*>,
        value: PostItem?
    ) {
        thisRef.putParcelable(property.name, value)
    }
}