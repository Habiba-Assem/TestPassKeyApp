package com.test.passkeyapp.utilities

import android.content.ContextWrapper
import android.os.SystemClock
import android.view.View
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager2.widget.ViewPager2


fun <T> LifecycleOwner.observe(liveData: LiveData<T>, action: (t: T) -> Unit) {
    liveData.observe(this) { t -> action(t) }
}

fun <T> LifecycleOwner.observeEvent(liveData: LiveData<Event<T>>, action: (t: T) -> Unit) {
    liveData.observe(this, EventObserver { action(it) })
}

// Fragment
fun <T> Fragment.observe(liveData: LiveData<T>, action: (t: T) -> Unit) =
    viewLifecycleOwner.observe(liveData, action)

fun <T> Fragment.observeEvent(liveData: LiveData<Event<T>>, action: (t: T) -> Unit) =
    viewLifecycleOwner.observeEvent(liveData, action)

// https://developer.android.com/guide/navigation/use-graph/programmatic#returning_a_result
fun <T> Fragment.observeNavigationResultOnce(
    key: String, action: (t: T) -> Unit
) {
    val navController = findNavController()
    val navBackStackEntry = navController.currentBackStackEntry
    // We use a String here, but any type that can be put in a Bundle is supported
    navBackStackEntry?.savedStateHandle?.getLiveData<T>(key)?.observe(
        viewLifecycleOwner
    ) {
        action(it)
        navBackStackEntry.savedStateHandle.remove<T>(key)
    }
}

// https://developer.android.com/guide/navigation/use-graph/programmatic#additional_considerations
fun <T> Fragment.observeNavigationResultFromDialogOnce(
    @IdRes currentDestinationId: Int, key: String, action: (t: T) -> Unit
) {
    val navController = findNavController()
    // After a configuration change or process death, the currentBackStackEntry
    // points to the dialog destination, so you must use getBackStackEntry()
    // with the specific ID of your destination to ensure we always
    // get the right NavBackStackEntry
    val navBackStackEntry = navController.getBackStackEntry(currentDestinationId)

    // Create our observer and add it to the NavBackStackEntry's lifecycle
    val observer = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_RESUME && navBackStackEntry.savedStateHandle.contains(key)) {
            val result = navBackStackEntry.savedStateHandle.get<T>(key)
            result?.let {
                action(it)
                navBackStackEntry.savedStateHandle.remove<T>(key)
            }
        }
    }
    navBackStackEntry.getLifecycle().addObserver(observer)

    // As addObserver() does not automatically remove the observer, we
    // call removeObserver() manually when the view lifecycle is destroyed
    viewLifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_DESTROY) {
            navBackStackEntry.getLifecycle().removeObserver(observer)
        }
    })
}

fun LifecycleOwner.observe(view: View, action: () -> Unit) {
    view.setOnClickListener { action() }
}

fun LifecycleOwner.observeWithDebounce(view: View, debounceTime: Long = 600L, action: () -> Unit) {
    view.setOnClickListener { action() }
    view.setOnClickListener(object : View.OnClickListener {
        private var lastClickTime: Long = 0

        override fun onClick(v: View) {
            if (SystemClock.elapsedRealtime() - lastClickTime < debounceTime) return
            else action()

            lastClickTime = SystemClock.elapsedRealtime()
        }
    })
}


fun LifecycleOwner.observeTextChange(view: TextView, action: () -> Unit) {
    view.addTextChangedListener { action() }
}

fun LifecycleOwner.observeChangeEditTextFocus(view: TextView, action: () -> Unit) {
    view.setOnFocusChangeListener { _, hasFocus ->
        if (hasFocus.not()) action()
    }
}

fun LifecycleOwner.observeTouch(view: Spinner, action: (view: View) -> Boolean) {
    view.setOnTouchListener { _, _ -> return@setOnTouchListener action(view) }
}

fun LifecycleOwner.observeItemSelected(view: Spinner, action: (position: Int) -> Unit) {
    view.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(p0: AdapterView<*>?) {}
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            if (position > 0) action(position)
        }
    }
}

fun LifecycleOwner.observeSlide(view: SeekBar, action: (progress: Int) -> Unit) {
    view.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(p0: SeekBar?, progress: Int, p2: Boolean) {
            action(progress)
        }

        override fun onStartTrackingTouch(p0: SeekBar?) {}
        override fun onStopTrackingTouch(p0: SeekBar?) {
            action(p0?.progress ?: 0)
        }
    })
}

fun LifecycleOwner.observeMultipleViews(vararg views: View, action: () -> Unit) {
    views.forEach { view ->
        view.setOnClickListener { action() }
    }
}

fun LifecycleOwner.observeSwipeToRefreshView(view: SwipeRefreshLayout, action: () -> Unit) {
    view.setOnRefreshListener { action() }
}

fun LifecycleOwner.observeViewPager(viewPager: ViewPager2,action: (position: Int) -> Unit){
    viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            action(position)
        }
    })
}

fun LinearLayout.observe(view: View, action: () -> Unit) {
    view.setOnClickListener { action() }
}

fun <T> LinearLayout.observe(liveData: LiveData<T>, action: (t: T) -> Unit) {
    liveData.observe(lifecycleOwner) { t -> action(t) }
}
val LinearLayout.lifecycleOwner: LifecycleOwner
    get() {
        var context = this.context
        while (context !is LifecycleOwner && context is ContextWrapper) {
            context = context.baseContext
        }
        return context as LifecycleOwner
    }