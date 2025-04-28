/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.aiedge.gallery.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.aiedge.gallery.data.Model
import com.google.aiedge.gallery.data.ModelDownloadStatusType
import com.google.aiedge.gallery.data.Task
import com.google.aiedge.gallery.ui.common.chat.ConfigDialog
import com.google.aiedge.gallery.ui.modelmanager.ModelManagerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelPageAppBar(
  task: Task,
  model: Model,
  modelManagerViewModel: ModelManagerViewModel,
  onBackClicked: () -> Unit,
  onModelSelected: (Model) -> Unit,
  modifier: Modifier = Modifier,
  onConfigChanged: (oldConfigValues: Map<String, Any>, newConfigValues: Map<String, Any>) -> Unit = { _, _ -> },
) {
  var showConfigDialog by remember { mutableStateOf(false) }
  val modelManagerUiState by modelManagerViewModel.uiState.collectAsState()
  val context = LocalContext.current
  val curDownloadStatus = modelManagerUiState.modelDownloadStatus[model.name]

  CenterAlignedTopAppBar(title = {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      // Task type.
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Icon(
          task.icon ?: ImageVector.vectorResource(task.iconVectorResourceId!!),
          tint = getTaskIconColor(task = task),
          modifier = Modifier.size(16.dp),
          contentDescription = "",
        )
        Text(
          task.type.label,
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
          color = getTaskIconColor(task = task)
        )
      }

      // Model chips pager.
      ModelPickerChipsPager(
        task = task,
        initialModel = model,
        modelManagerViewModel = modelManagerViewModel,
        onModelSelected = onModelSelected,
      )
    }
  }, modifier = modifier,
    // The back button.
    navigationIcon = {
      IconButton(onClick = onBackClicked) {
        Icon(
          imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
          contentDescription = "",
        )
      }
    },
    // The config button for the model (if existed).
    actions = {
      val showConfigButton =
        model.configs.isNotEmpty() && curDownloadStatus?.status == ModelDownloadStatusType.SUCCEEDED
      IconButton(
        onClick = {
          showConfigDialog = true
        },
        enabled = showConfigButton,
        modifier = Modifier.alpha(if (showConfigButton) 1f else 0f)
      ) {
        Icon(
          imageVector = Icons.Rounded.Settings,
          contentDescription = "",
          tint = MaterialTheme.colorScheme.primary
        )
      }
    })

  // Config dialog.
  if (showConfigDialog) {
    ConfigDialog(
      title = "Model configs",
      configs = model.configs,
      initialValues = model.configValues,
      onDismissed = { showConfigDialog = false },
      onOk = { curConfigValues ->
        // Hide config dialog.
        showConfigDialog = false

        // Check if the configs are changed or not. Also check if the model needs to be
        // re-initialized.
        var same = true
        var needReinitialization = false
        for (config in model.configs) {
          val key = config.key.label
          val oldValue = convertValueToTargetType(
            value = model.configValues.getValue(key), valueType = config.valueType
          )
          val newValue = convertValueToTargetType(
            value = curConfigValues.getValue(key), valueType = config.valueType
          )
          if (oldValue != newValue) {
            same = false
            if (config.needReinitialization) {
              needReinitialization = true
            }
            break
          }
        }
        if (same) {
          return@ConfigDialog
        }

        // Save the config values to Model.
        val oldConfigValues = model.configValues
        model.configValues = curConfigValues

        // Force to re-initialize the model with the new configs.
        if (needReinitialization) {
          modelManagerViewModel.initializeModel(
            context = context, task = task, model = model, force = true
          )
        }

        // Notify.
        onConfigChanged(oldConfigValues, model.configValues)
      },
    )
  }
}