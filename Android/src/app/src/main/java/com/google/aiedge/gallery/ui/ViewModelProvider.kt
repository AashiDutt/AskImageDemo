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

package com.google.aiedge.gallery.ui

import android.app.Application
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.aiedge.gallery.GalleryApplication
import com.google.aiedge.gallery.ui.imageclassification.ImageClassificationViewModel
import com.google.aiedge.gallery.ui.imagegeneration.ImageGenerationViewModel
import com.google.aiedge.gallery.ui.llmchat.LlmChatViewModel
import com.google.aiedge.gallery.ui.llmchat.LlmImageToTextViewModel
import com.google.aiedge.gallery.ui.llmsingleturn.LlmSingleTurnViewModel
import com.google.aiedge.gallery.ui.modelmanager.ModelManagerViewModel
import com.google.aiedge.gallery.ui.textclassification.TextClassificationViewModel

object ViewModelProvider {
  val Factory = viewModelFactory {
    // Initializer for ModelManagerViewModel.
    initializer {
      val downloadRepository = galleryApplication().container.downloadRepository
      val dataStoreRepository = galleryApplication().container.dataStoreRepository
      ModelManagerViewModel(
        downloadRepository = downloadRepository,
        dataStoreRepository = dataStoreRepository,
        context = galleryApplication().container.context,
      )
    }

    // Initializer for TextClassificationViewModel
    initializer {
      TextClassificationViewModel()
    }

    // Initializer for ImageClassificationViewModel
    initializer {
      ImageClassificationViewModel()
    }

    // Initializer for LlmChatViewModel.
    initializer {
      LlmChatViewModel()
    }

    // Initializer for LlmSingleTurnViewModel..
    initializer {
      LlmSingleTurnViewModel()
    }

    // Initializer for LlmImageToTextViewModel.
    initializer {
      LlmImageToTextViewModel()
    }

    // Initializer for ImageGenerationViewModel.
    initializer {
      ImageGenerationViewModel()
    }
  }
}

/**
 * Extension function to queries for [Application] object and returns an instance of
 * [GalleryApplication].
 */
fun CreationExtras.galleryApplication(): GalleryApplication =
  (this[AndroidViewModelFactory.APPLICATION_KEY] as GalleryApplication)
