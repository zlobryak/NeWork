package ru.netology.nework.utils

import android.os.Bundle
import ru.netology.nework.data.dto.PostItem
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class PostItemArg : ReadWriteProperty<Bundle, PostItem?> {
    override fun getValue(thisRef: Bundle, property: KProperty<*>): PostItem? {
        return thisRef.getParcelable("post_item_arg")
    }

    override fun setValue(thisRef: Bundle, property: KProperty<*>, value: PostItem?) {
        thisRef.putParcelable("post_item_arg", value)
    }
}


