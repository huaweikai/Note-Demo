@file:Suppress("RedundantVisibilityModifier", "unused")
@file:JvmName("ReflectionFragmentViewBindings")

package com.example.note.util.viewbindingdelegate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

/**
 * 通过bind的方式创建viewBinding，局限于fragment继承父类时，调用有参构造函数，否则无效
 */
fun <V : ViewBinding> Fragment.viewBinding(binder: (View) -> V): LifecycleAwareViewBinding<Fragment, V> {
    return LifecycleAwareViewBinding { binder.invoke(it.requireView()) }
}

/**
 * 通过反射的创建viewBinding，不要在oncCreate前,和onDestroyView后使用
 */
inline fun <reified V : ViewBinding> Fragment.viewBinding(): LifecycleAwareViewBinding<Fragment, V> {
    val method = V::class.java.getMethod("inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.java)
    return LifecycleAwareViewBinding { method.invoke(null, layoutInflater, null, false) as V }
}
