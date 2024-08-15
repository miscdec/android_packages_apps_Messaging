package com.android.messaging2.ui.components.itemselector

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.toIntRect
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Preview
@Composable
fun ItemGrid() {

    val items by rememberSaveable { mutableStateOf(List(20) { it }) }
    val selectedIds = rememberSaveable { mutableStateOf(emptySet<Int>()) } // NEW
    val inSelectionMode by remember { derivedStateOf { selectedIds.value.isNotEmpty() } } // NEW

    val state = rememberLazyGridState() // NEW

    // How fast the grid should be scrolling at any given time. The closer the
    // user moves their pointer to the bottom of the screen, the faster the scroll.
    val autoScrollSpeed = remember { mutableFloatStateOf(0f) }
    // Executing the scroll
    LaunchedEffect(autoScrollSpeed.floatValue) {
        if (autoScrollSpeed.floatValue != 0f) {
            while (isActive) {
                state.scrollBy(autoScrollSpeed.floatValue)
                delay(10)
            }
        }
    }


    LazyVerticalGrid(
        state = state,
        columns = GridCells.Fixed(1),
        modifier = Modifier.photoGridDragHandler(
            lazyGridState = state,
            selectedIds = selectedIds,
            autoScrollSpeed = autoScrollSpeed, // NEW
            autoScrollThreshold = with(LocalDensity.current) { 40.dp.toPx() }
        ),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        item {
            Text(text = "${selectedIds.value}")
        }
        items(count = items.count(), key = { items.indexOf(it) }) { id: Int ->
            val selected = selectedIds.value.contains(id) // NEW
            SelectorItem(
                selected = selected,
                inSelectionMode = inSelectionMode,
                modifier = Modifier
                    .semantics {
                        if (!inSelectionMode) {
                            onLongClick("Select") {
                                selectedIds.value += id
                                true
                            }
                        }
                    }
                    .then(if (inSelectionMode) {
                        Modifier.toggleable(
                            value = selected,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null, // do not show a ripple
                            onValueChange = {
                                if (it) {
                                    selectedIds.value += id
                                } else {
                                    selectedIds.value -= id
                                }
                            }
                        )
                    } else Modifier)
                    .clickable { // NEW
                        selectedIds.value = if (selected) {
                            selectedIds.value.minus(id)
                        } else {
                            selectedIds.value.plus(id)
                        }
                    })
        }

    }

}

fun Modifier.photoGridDragHandler(
    lazyGridState: LazyGridState,
    selectedIds: MutableState<Set<Int>>,
    autoScrollSpeed: MutableState<Float>,
    autoScrollThreshold: Float
) = composed(
    inspectorInfo = debugInspectorInfo {
        name = "photoGridDragHandler"
        properties["lazyGridState"] = lazyGridState
        properties["selectedIds"] = selectedIds
        properties["autoScrollSpeed"] = autoScrollSpeed
        properties["autoScrollThreshold"] = autoScrollThreshold
    }
) {
    this.pointerInput(Unit) {
        var initialKey: Int? = null
        var currentKey: Int? = null

        detectDragGesturesAfterLongPress(
            onDragStart = { offset ->
                lazyGridState.gridItemKeyAtPosition(offset)?.let { key -> // #1
                    if (!selectedIds.value.contains(key)) { // #2
                        initialKey = key
                        currentKey = key
                        selectedIds.value += key // #3
                    }
                }
            },
            onDragCancel = {
                initialKey = null
                autoScrollSpeed.value = 0f
            },
            onDragEnd = {
                initialKey = null
                autoScrollSpeed.value = 0f
            },
            onDrag = { change, _ ->
                if (initialKey != null) {
                    // NEW
                    // If dragging near the vertical edges of the grid, start scrolling
                    val distFromBottom =
                        lazyGridState.layoutInfo.viewportSize.height - change.position.y
                    val distFromTop = change.position.y
                    autoScrollSpeed.value = when {
                        distFromBottom < autoScrollThreshold -> autoScrollThreshold - distFromBottom
                        distFromTop < autoScrollThreshold -> -(autoScrollThreshold - distFromTop)
                        else -> 0f
                    }

                    // Add or remove photos from selection based on drag position
                    lazyGridState.gridItemKeyAtPosition(change.position)?.let { key ->
                        if (currentKey != key) {
                            selectedIds.value = selectedIds.value
                                .minus(initialKey!!..currentKey!!)
                                .minus(currentKey!!..initialKey!!)
                                .plus(initialKey!!..key)
                                .plus(key..initialKey!!)
                            currentKey = key
                        }
                    }
                }
            }
        )
    }
}


// The key of the photo underneath the pointer. Null if no photo is hit by the pointer.
fun LazyGridState.gridItemKeyAtPosition(hitPoint: Offset): Int? =
    layoutInfo.visibleItemsInfo.find { itemInfo ->
        itemInfo.size.toIntRect().contains(hitPoint.round() - itemInfo.offset)
    }?.key as? Int

@Composable
fun SelectorItem(modifier: Modifier, selected: Boolean, inSelectionMode: Boolean) {
//    Surface(
////        tonalElevation = 3.dp,
//        contentColor = MaterialTheme.colorScheme.primary,
//    ) {
    Row(
        modifier
            .fillMaxWidth()
            .safeContentPadding()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier, verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null)
            Column(modifier) {
                Text(text = "title")
                Text(text = "Text")
            }
        }
        if (inSelectionMode) {
            if (selected) {
                Icon(Icons.Default.CheckCircle, null)
            } else {
                Icon(Icons.Default.RadioButtonUnchecked, null)
            }
        }
    }

//    }
}
