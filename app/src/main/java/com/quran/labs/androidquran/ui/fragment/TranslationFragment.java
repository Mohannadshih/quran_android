package com.quran.labs.androidquran.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.quran.data.core.QuranInfo;
import com.quran.data.model.SuraAyah;
import com.quran.data.model.selection.AyahSelection;
import com.quran.labs.androidquran.common.LocalTranslation;
import com.quran.labs.androidquran.common.QuranAyahInfo;
import com.quran.labs.androidquran.data.QuranDisplayData;
import com.quran.labs.androidquran.presenter.quran.ayahtracker.AyahTrackerItem;
import com.quran.labs.androidquran.presenter.quran.ayahtracker.AyahTrackerPresenter;
import com.quran.labs.androidquran.presenter.quran.ayahtracker.AyahTranslationTrackerItem;
import com.quran.labs.androidquran.presenter.translation.TranslationPresenter;
import com.quran.labs.androidquran.ui.PagerActivity;
import com.quran.labs.androidquran.ui.helpers.AyahSelectedListener;
import com.quran.labs.androidquran.ui.helpers.AyahTracker;
import com.quran.labs.androidquran.ui.helpers.QuranPage;
import com.quran.labs.androidquran.ui.translation.TranslationView;
import com.quran.labs.androidquran.ui.util.PageController;
import com.quran.labs.androidquran.util.QuranSettings;
import com.quran.labs.androidquran.view.QuranTranslationPageLayout;
import com.quran.reading.common.ReadingEventPresenter;

import java.util.List;

import javax.inject.Inject;

public class TranslationFragment extends Fragment implements
    AyahTrackerPresenter.AyahInteractionHandler, QuranPage,
    TranslationPresenter.TranslationScreen, PageController {
  private static final String PAGE_NUMBER_EXTRA = "pageNumber";

  private static final String SI_SCROLL_POSITION = "SI_SCROLL_POSITION";

  private int pageNumber;
  private int scrollPosition;

  private TranslationView translationView;
  private QuranTranslationPageLayout mainView;
  private AyahTrackerItem[] ayahTrackerItems;

  @Inject QuranInfo quranInfo;
  @Inject QuranDisplayData quranDisplayData;
  @Inject QuranSettings quranSettings;
  @Inject TranslationPresenter presenter;
  @Inject AyahTrackerPresenter ayahTrackerPresenter;
  @Inject AyahSelectedListener ayahSelectedListener;
  @Inject ReadingEventPresenter readingEventPresenter;

  public static TranslationFragment newInstance(int page) {
    final TranslationFragment f = new TranslationFragment();
    final Bundle args = new Bundle();
    args.putInt(PAGE_NUMBER_EXTRA, page);
    f.setArguments(args);
    return f;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (savedInstanceState != null) {
      scrollPosition = savedInstanceState.getInt(SI_SCROLL_POSITION);
    }
    setHasOptionsMenu(true);
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater,
                           ViewGroup container, Bundle savedInstanceState) {
    Context context = getActivity();
    mainView = new QuranTranslationPageLayout(context);
    mainView.setPageController(this, pageNumber, quranInfo.getSkip());

    translationView = mainView.getTranslationView();
    translationView.setTranslationClickedListener(v -> {
      final Activity activity = getActivity();
      if (activity instanceof PagerActivity) {
        ((PagerActivity) getActivity()).toggleActionBar();
      }
    });

    return mainView;
  }

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);

    pageNumber = getArguments() != null ? getArguments().getInt(PAGE_NUMBER_EXTRA) : -1;
    final int[] pages = { pageNumber };
    ((PagerActivity) getActivity()).getPagerActivityComponent()
        .quranPageComponentFactory()
        .generate(pages)
        .inject(this);
  }

  @Override
  public void updateView() {
    if (isAdded()) {
      mainView.updateView(quranSettings);
      refresh();
    }
  }

  @NonNull
  @Override
  public AyahTracker getAyahTracker() {
    return ayahTrackerPresenter;
  }

  @NonNull
  @Override
  public AyahTrackerItem[] getAyahTrackerItems() {
    if (ayahTrackerItems == null) {
      ayahTrackerItems = new AyahTrackerItem[] {
          new AyahTranslationTrackerItem(pageNumber, quranInfo, translationView) };
    }
    return ayahTrackerItems;
  }

  @Override
  public void onResume() {
    super.onResume();
    ayahTrackerPresenter.bind(this);
    presenter.bind(this);
    updateView();
  }

  @Override
  public void onPause() {
    ayahTrackerPresenter.unbind(this);
    presenter.unbind(this);
    super.onPause();
  }

  @Override
  public void setVerses(int page,
                        @NonNull LocalTranslation[] translations,
                        @NonNull List<QuranAyahInfo> verses) {
    translationView.setVerses(quranDisplayData, translations, verses);
  }

  @Override
  public void updateScrollPosition() {
    translationView.setScrollPosition(scrollPosition);
  }

  public void refresh() {
    presenter.refresh();
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle outState) {
    scrollPosition = translationView.findFirstCompletelyVisibleItemPosition();
    outState.putInt(SI_SCROLL_POSITION, scrollPosition);
    super.onSaveInstanceState(outState);
  }

  @Override
  public boolean handleTouchEvent(@NonNull MotionEvent event,
                                  @NonNull AyahSelectedListener.EventType eventType,
                                  int page) {
    return false;
  }

  @Override
  public void handleRetryClicked() {
  }

  @Override
  public void onScrollChanged(float y) {
    if (isVisible()) {
      final AyahSelection ayahSelection = readingEventPresenter.currentAyahSelection();
      if (ayahSelection instanceof AyahSelection.Ayah) {
        final AyahSelection.Ayah currentAyahSelection = ((AyahSelection.Ayah) ayahSelection);
        final SuraAyah suraAyah = currentAyahSelection.getSuraAyah();

        readingEventPresenter.onAyahSelection(
            new AyahSelection.Ayah(suraAyah,
                translationView.getToolbarPosition(suraAyah.sura, suraAyah.ayah))
        );
      }
    }
  }

  @Override
  public void handleLongPress(@NonNull SuraAyah suraAyah) {
    if (isVisible()) {
      readingEventPresenter.onAyahSelection(
          new AyahSelection.Ayah(suraAyah, translationView.getToolbarPosition(suraAyah.sura, suraAyah.ayah))
      );
    }
  }

  @Override
  public void endAyahMode() {
    if (isVisible()) {
      ayahTrackerPresenter.endAyahMode();
    }
  }
}
