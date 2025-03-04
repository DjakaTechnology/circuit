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
package com.slack.circuit.retained

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

public class Continuity : ViewModel(), RetainedStateRegistry {
  private val delegate = RetainedStateRegistryImpl()

  override fun consumeValue(key: String): Any? {
    return delegate.consumeValue(key)
  }

  override fun registerValue(key: String, value: Any?): RetainedStateRegistry.Entry {
    return delegate.registerValue(key, value)
  }

  override fun onCleared() {
    delegate.retained.clear()
  }
}

@Composable
public fun continuityRetainedStateRegistry(): RetainedStateRegistry {
  return viewModel<Continuity>()
}
