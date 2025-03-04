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
package com.slack.circuit.star.di

import androidx.lifecycle.ViewModel
import com.slack.circuit.CircuitConfig
import com.slack.circuit.Presenter
import com.slack.circuit.Ui
import com.slack.circuit.backstack.BackStackRecordLocalProviderViewModel
import com.slack.circuit.retained.Continuity
import com.squareup.anvil.annotations.ContributesTo
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import dagger.multibindings.Multibinds

@ContributesTo(AppScope::class)
@Module
interface CircuitModule {
  @Multibinds fun presenterFactories(): Set<Presenter.Factory>

  @Multibinds fun viewFactories(): Set<Ui.Factory>

  @ViewModelKey(BackStackRecordLocalProviderViewModel::class)
  @IntoMap
  @Binds
  fun BackStackRecordLocalProviderViewModel.bindBackStackRecordLocalProviderViewModel(): ViewModel

  @ViewModelKey(Continuity::class) @IntoMap @Binds fun Continuity.bindContinuity(): ViewModel

  companion object {
    @Provides
    fun provideBackStackRecordLocalProviderViewModel(): BackStackRecordLocalProviderViewModel {
      return BackStackRecordLocalProviderViewModel()
    }

    @Provides
    fun provideContinuity(): Continuity {
      return Continuity()
    }

    @Provides
    fun provideCircuit(
      presenterFactories: @JvmSuppressWildcards Set<Presenter.Factory>,
      uiFactories: @JvmSuppressWildcards Set<Ui.Factory>,
    ): CircuitConfig {
      return CircuitConfig.Builder()
        .apply {
          for (factory in presenterFactories) {
            addPresenterFactory(factory)
          }
          for (factory in uiFactories) {
            addUiFactory(factory)
          }
        }
        .build()
    }
  }
}
