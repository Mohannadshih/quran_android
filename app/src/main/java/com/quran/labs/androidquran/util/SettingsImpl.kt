package com.quran.labs.androidquran.util

import android.content.SharedPreferences
import com.quran.data.dao.Settings
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class SettingsImpl @Inject constructor(private val quranSettings: QuranSettings) : Settings {
  private val preferencesFlow: Flow<String> by lazy {
    callbackFlow {
      val callback =
        SharedPreferences.OnSharedPreferenceChangeListener { _, pref ->
          if (pref != null) {
            trySendBlocking(pref)
              .onFailure {}
          }
        }
      quranSettings.registerPreferencesListener(callback)

      awaitClose { quranSettings.unregisterPreferencesListener(callback) }
    }
  }

  override suspend fun setVersion(version: Int) {
    quranSettings.version = version
  }

  override suspend fun removeDidDownloadPages() {
    quranSettings.removeDidDownloadPages()
  }

  override suspend fun setShouldOverlayPageInfo(shouldOverlay: Boolean) {
    quranSettings.setShouldOverlayPageInfo(shouldOverlay)
  }

  override suspend fun lastPage(): Int {
    return quranSettings.lastPage
  }

  override suspend fun isNightMode(): Boolean {
    return quranSettings.isNightMode
  }

  override suspend fun nightModeTextBrightness(): Int {
    return quranSettings.nightModeTextBrightness
  }

  override suspend fun nightModeBackgroundBrightness(): Int {
    return quranSettings.nightModeBackgroundBrightness
  }

  override suspend fun shouldShowHeaderFooter(): Boolean {
    return quranSettings.shouldOverlayPageInfo()
  }

  override suspend fun shouldShowBookmarks(): Boolean {
    return quranSettings.shouldHighlightBookmarks()
  }

  override suspend fun pageType(): String {
    return quranSettings.pageType
  }

  override suspend fun showSidelines(): Boolean {
    return quranSettings.isSidelines
  }

  override suspend fun setShowSidelines(show: Boolean) {
    quranSettings.isSidelines = show
  }

  override suspend fun showLineDividers(): Boolean {
    return quranSettings.isShowLineDividers
  }

  override suspend fun setShouldShowLineDividers(show: Boolean) {
    quranSettings.isShowLineDividers = show
  }

  override fun preferencesFlow(): Flow<String> = preferencesFlow
}
