package com.mobees.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mobees.app.MobeesApp
import com.mobees.app.di.AppContainer

@Composable
fun appContainer(): AppContainer = (LocalContext.current.applicationContext as MobeesApp).container

/** Creates a ViewModel with access to the [AppContainer] without a DI framework. */
@Composable
inline fun <reified VM : ViewModel> containerViewModel(
    key: String? = null,
    crossinline create: (AppContainer) -> VM,
): VM {
    val container = appContainer()
    return viewModel(
        key = key,
        factory = viewModelFactory { initializer { create(container) } },
    )
}
