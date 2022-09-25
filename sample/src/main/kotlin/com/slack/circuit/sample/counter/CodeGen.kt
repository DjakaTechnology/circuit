/*
 * Copyright (C) 2022 Slack Technologies, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.slack.circuit.sample.counter

import com.slack.circuit.Navigator
import com.slack.circuit.Presenter
import com.slack.circuit.Screen
import com.slack.circuit.ScreenUi
import com.slack.circuit.Ui
import com.slack.circuit.presenterOf
import com.slack.circuit.sample.di.AppScope
import com.slack.circuit.ui
import com.squareup.anvil.annotations.ContributesMultibinding
import javax.inject.Inject

annotation class CircuitInject<T : Screen>

@ContributesMultibinding(AppScope::class)
class CounterPresenterFactory @Inject constructor() : Presenter.Factory {
  override fun create(screen: Screen, navigator: Navigator): Presenter<*, *>? {
    return when (screen) {
      is CounterScreen -> presenterOf { events -> CounterPresenter(events) }
      else -> null
    }
  }
}

@ContributesMultibinding(AppScope::class)
class CounterUiFactory @Inject constructor() : Ui.Factory {
  override fun create(screen: Screen): ScreenUi? {
    return when (screen) {
      is CounterScreen ->
        ScreenUi(
          ui<CounterScreen.CounterState, CounterScreen.CounterEvent> { state, eventSink ->
            Counter(state, eventSink)
          }
        )

      else -> null
    }
  }
}
