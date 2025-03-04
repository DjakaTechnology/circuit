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
package com.slack.circuit.star.petdetail

import com.google.common.truth.Truth.assertThat
import com.slack.circuit.star.petlist.PetListPresenterTest
import com.slack.circuit.star.petlist.TestRepository
import com.slack.circuit.star.test
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PetDetailPresenterTest {
  @Test
  fun `present - emit loading state then no animal state`() = runTest {
    val repository = TestRepository(emptyList())
    val screen = PetDetailScreen(123L, "key")
    val presenter = PetDetailPresenter(screen, repository)

    presenter.test {
      assertThat(awaitItem()).isEqualTo(PetDetailScreen.State.Loading)
      assertThat(awaitItem()).isEqualTo(PetDetailScreen.State.UnknownAnimal)
    }
  }

  @Test
  fun `present - emit loading state then success state`() = runTest {
    val animal = PetListPresenterTest.animal
    val repository = TestRepository(listOf(animal))
    val screen = PetDetailScreen(animal.id, animal.photos.first().small)
    val presenter = PetDetailPresenter(screen, repository)

    presenter.test {
      assertThat(PetDetailScreen.State.Loading).isEqualTo(awaitItem())

      val success = animal.toPetDetailState(animal.photos.first().small)
      assertThat(awaitItem()).isEqualTo(success)
    }
  }
}
