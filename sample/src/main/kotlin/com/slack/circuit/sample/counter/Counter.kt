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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.slack.circuit.CircuitUiEvent
import com.slack.circuit.CircuitUiState
import com.slack.circuit.EventCollector
import com.slack.circuit.Screen
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.sample.counter.CounterScreen.CounterEvent
import com.slack.circuit.sample.counter.CounterScreen.CounterState
import kotlinx.coroutines.flow.Flow
import kotlinx.parcelize.Parcelize

@Parcelize
object CounterScreen : Screen {
  data class CounterState(val count: Int) : CircuitUiState
  interface CounterEvent : CircuitUiEvent {
    object Increment : CounterEvent
    object Decrement : CounterEvent
  }
}

@CircuitInject<CounterScreen>
@Composable
fun CounterPresenter(events: Flow<CounterEvent>): CounterState {
  var state by rememberRetained { mutableStateOf(CounterState(0)) }

  EventCollector(events) { event ->
    when (event) {
      is CounterEvent.Increment -> state = state.copy(count = state.count + 1)
      is CounterEvent.Decrement -> state = state.copy(count = state.count - 1)
    }
  }

  return state
}

@CircuitInject<CounterScreen>
@Composable
fun Counter(state: CounterState, eventSink: (CounterEvent) -> Unit) {
  Box(Modifier.fillMaxSize()) {
    Column(Modifier.align(Alignment.Center)) {
      Text(
        modifier = Modifier.align(CenterHorizontally),
        text = "Count: ${state.count}",
        style = MaterialTheme.typography.displayLarge
      )
      Spacer(modifier = Modifier.height(16.dp))
      Button(
        modifier = Modifier.align(CenterHorizontally),
        onClick = { eventSink(CounterEvent.Increment) }
      ) {
        Text(text = "Increment +")
      }
      Button(
        modifier = Modifier.align(CenterHorizontally),
        onClick = { eventSink(CounterEvent.Decrement) }
      ) {
        Text(text = "Decrement -")
      }
    }
  }
}
