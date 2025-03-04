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
package com.slack.circuit.star.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.End
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.slack.circuit.CircuitConfig
import com.slack.circuit.CircuitContent
import com.slack.circuit.CircuitUiEvent
import com.slack.circuit.CircuitUiState
import com.slack.circuit.LocalOverlayHost
import com.slack.circuit.NavEvent
import com.slack.circuit.Navigator
import com.slack.circuit.OverlayHost
import com.slack.circuit.Presenter
import com.slack.circuit.Screen
import com.slack.circuit.ScreenUi
import com.slack.circuit.Ui
import com.slack.circuit.onNavEvent
import com.slack.circuit.star.di.AppScope
import com.slack.circuit.star.home.HomeScreen.Event.ChildNav
import com.slack.circuit.star.home.HomeScreen.Event.HomeEvent
import com.slack.circuit.star.home.HomeScreen.Event.PetListFilterEvent
import com.slack.circuit.star.overlay.BottomSheetOverlay
import com.slack.circuit.star.petlist.Filters
import com.slack.circuit.star.petlist.Gender
import com.slack.circuit.star.petlist.PetListFilterPresenter
import com.slack.circuit.star.petlist.PetListFilterScreen
import com.slack.circuit.star.petlist.Size
import com.slack.circuit.star.ui.StarTheme
import com.slack.circuit.ui
import com.squareup.anvil.annotations.ContributesMultibinding
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import javax.inject.Inject
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

@Parcelize
object HomeScreen : Screen {
  data class State(
    val homeNavState: HomeNavScreen.State,
    val petListFilterState: PetListFilterScreen.State,
    val eventSink: (Event) -> Unit,
  ) : CircuitUiState

  sealed interface Event : CircuitUiEvent {
    class HomeEvent(val event: HomeNavScreen.Event) : Event
    class PetListFilterEvent(val event: PetListFilterScreen.Event) : Event
    class ChildNav(val navEvent: NavEvent) : Event
  }
}

@ContributesMultibinding(AppScope::class)
class HomeScreenPresenterFactory
@Inject
constructor(private val homePresenterFactory: HomePresenter.Factory) : Presenter.Factory {
  override fun create(
    screen: Screen,
    navigator: Navigator,
    circuitConfig: CircuitConfig
  ): Presenter<*>? {
    if (screen is HomeScreen) return homePresenterFactory.create(navigator)
    return null
  }
}

class HomePresenter
@AssistedInject
constructor(
  @Assisted private val navigator: Navigator,
  petListFilterPresenterFactory: PetListFilterPresenter.Factory,
) : Presenter<HomeScreen.State> {
  private val petListFilterPresenter = petListFilterPresenterFactory.create()

  @Composable
  override fun present(): HomeScreen.State {
    val homeNavState = HomeNavPresenter()
    val petListFilterState = petListFilterPresenter.present()
    return HomeScreen.State(homeNavState, petListFilterState) { event ->
      when (event) {
        is HomeEvent -> homeNavState.eventSink(event.event)
        is PetListFilterEvent -> petListFilterState.eventSink(event.event)
        is ChildNav -> navigator.onNavEvent(event.navEvent)
      }
    }
  }

  @AssistedFactory
  interface Factory {
    fun create(navigator: Navigator): HomePresenter
  }
}

@ContributesMultibinding(AppScope::class)
class HomeUiFactory @Inject constructor() : Ui.Factory {
  override fun create(screen: Screen, circuitConfig: CircuitConfig): ScreenUi? {
    if (screen is HomeScreen) {
      return ScreenUi(homeUi())
    }
    return null
  }
}

private fun homeUi() = ui<HomeScreen.State> { state -> HomeContent(state) }

@Composable
fun HomeContent(state: HomeScreen.State) {
  val systemUiController = rememberSystemUiController()
  systemUiController.setStatusBarColor(MaterialTheme.colorScheme.background)
  systemUiController.setNavigationBarColor(MaterialTheme.colorScheme.primaryContainer)

  Scaffold(
    modifier = Modifier.navigationBarsPadding().systemBarsPadding().fillMaxWidth(),
    topBar = {
      CenterAlignedTopAppBar(
        title = {
          Text("Adoptables", fontSize = 22.sp, color = MaterialTheme.colorScheme.onBackground)
        },
        colors =
          TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
          ),
        actions = {
          val scope = rememberCoroutineScope()
          val overlayHost = LocalOverlayHost.current
          IconButton(
            onClick = {
              scope.launch {
                val result = overlayHost.updateFilters(state.petListFilterState)
                state.eventSink(
                  PetListFilterEvent(PetListFilterScreen.Event.UpdatedFilters(result))
                )
              }
            }
          ) {
            Icon(
              imageVector = Icons.Default.FilterList,
              contentDescription = "Filter pet list",
              tint = MaterialTheme.colorScheme.onBackground
            )
          }
        },
      )
    },
    bottomBar = {
      StarTheme(useDarkTheme = true) {
        BottomNavigationBar(selectedIndex = state.homeNavState.index) { index ->
          state.eventSink(HomeEvent(HomeNavScreen.Event.ClickNavItem(index)))
        }
      }
    }
  ) { paddingValues ->
    Box(modifier = Modifier.padding(paddingValues)) {
      val screen =
        state.homeNavState.bottomNavItems[state.homeNavState.index].screenFor(
          state.petListFilterState.filters
        )
      CircuitContent(screen, { event -> state.eventSink(ChildNav(event)) })
    }
  }
}

@Composable
private fun BottomNavigationBar(selectedIndex: Int, onSelectedIndex: (Int) -> Unit) {
  // These are the buttons on the NavBar, they dictate where we navigate too.
  val items = listOf(BottomNavItem.Adoptables, BottomNavItem.About)
  NavigationBar(
    containerColor = MaterialTheme.colorScheme.primaryContainer,
  ) {
    items.forEachIndexed { index, item ->
      NavigationBarItem(
        icon = { Icon(imageVector = item.icon, contentDescription = item.title) },
        label = { Text(text = item.title) },
        alwaysShowLabel = true,
        selected = selectedIndex == index,
        onClick = { onSelectedIndex(index) }
      )
    }
  }
}

private suspend fun OverlayHost.updateFilters(state: PetListFilterScreen.State): Filters {
  return show(
    BottomSheetOverlay(
      model = state.filters,
      onDismiss = { state.filters },
    ) { initialFilters, overlayNavigator ->
      var filters by remember { mutableStateOf(initialFilters) }
      Column(Modifier.fillMaxWidth()) {
        GenderFilterOption(filters.gender) { filters = filters.copy(gender = it) }
        SizeFilterOption(filters.size) { filters = filters.copy(size = it) }

        Row(Modifier.align(End)) {
          Button(onClick = { overlayNavigator.finish(initialFilters) }) { Text("Cancel") }
          Spacer(Modifier.width(16.dp))
          Button(onClick = { overlayNavigator.finish(filters) }) { Text("Save") }
        }
      }
    }
  )
}

@Composable
private fun GenderFilterOption(
  selected: Gender,
  selectedGender: (Gender) -> Unit,
) {
  Box { Text(text = "Gender") }
  Row(modifier = Modifier.selectableGroup(), horizontalArrangement = Arrangement.SpaceEvenly) {
    Gender.values().forEach { gender ->
      Column {
        Text(text = gender.name)
        RadioButton(selected = selected == gender, onClick = { selectedGender(gender) })
      }
    }
  }
}

@Composable
private fun SizeFilterOption(
  selected: Size,
  selectedSize: (Size) -> Unit,
) {
  Box { Text(text = "Size") }
  Row(modifier = Modifier.selectableGroup(), horizontalArrangement = Arrangement.SpaceEvenly) {
    Size.values().forEach { size ->
      Column {
        Text(text = size.name)
        RadioButton(selected = selected == size, onClick = { selectedSize(size) })
      }
    }
  }
}
