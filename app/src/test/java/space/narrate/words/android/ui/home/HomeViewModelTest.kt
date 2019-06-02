package space.narrate.words.android.ui.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertAbout
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import space.narrate.words.android.R
import org.mockito.Mockito.`when` as whenever
import space.narrate.words.android.data.repository.WordRepository
import space.narrate.words.android.FirestoreTestData
import space.narrate.words.android.LiveDataTestUtils
import space.narrate.words.android.valueBlocking
import space.narrate.words.android.ui.list.ListType

class HomeViewModelTest {

    private lateinit var homeViewModel: HomeViewModel

    private val wordRepository = mock(WordRepository::class.java)

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        homeViewModel = HomeViewModel(wordRepository)
    }

    @Test
    fun shouldGenerateListWithoutPreviews() {
        whenever(wordRepository.getGlobalWordTrending(4L, emptyList())).thenReturn(
            LiveDataTestUtils.of(emptyList())
        )
        whenever(wordRepository.getUserWordRecents(any())).thenReturn(
            LiveDataTestUtils.of(emptyList())
        )
        whenever(wordRepository.getUserWordFavorites(any())).thenReturn(
            LiveDataTestUtils.of(emptyList())
        )

        assertThat(homeViewModel.list.valueBlocking).orderedContentsAreSameAs(
            listOf(
                HomeItemModel.SettingsModel,
                HomeItemModel.DividerModel,
                HomeItemModel.ItemModel(ListType.FAVORITE, R.string.title_favorite),
                HomeItemModel.ItemModel(ListType.RECENT, R.string.title_recent),
                HomeItemModel.ItemModel(ListType.TRENDING, R.string.title_trending)
            )
        )
    }

    @Test
    fun shouldGenerateListWithPreviews() {
        whenever(wordRepository.getGlobalWordTrending(4L, emptyList())).thenReturn(
            LiveDataTestUtils.of(FirestoreTestData.globalWords)
        )
        whenever(wordRepository.getUserWordRecents(any())).thenReturn(
            LiveDataTestUtils.of(FirestoreTestData.user1Words)
        )
        whenever(wordRepository.getUserWordFavorites(any())).thenReturn(
            LiveDataTestUtils.of(FirestoreTestData.user1Words)
        )

        val globalWordsPreview = "wharf, mercurial"
        val userWordsPreview = "ostensibly, impetuous"

        // Wait for all 3 sources to call this live data observer's onChanged callback.
        assertThat(homeViewModel.list.valueBlocking(3)).orderedContentsAreSameAs(
            listOf(
                HomeItemModel.SettingsModel,
                HomeItemModel.DividerModel,
                HomeItemModel.ItemModel(ListType.FAVORITE, R.string.title_favorite, userWordsPreview),
                HomeItemModel.ItemModel(ListType.RECENT, R.string.title_recent, userWordsPreview),
                HomeItemModel.ItemModel(ListType.TRENDING, R.string.title_trending, globalWordsPreview)
            )
        )
    }


    private fun assertThat(list: List<HomeItemModel>?): HomeItemModelListSubject {
        return assertAbout(homeItemModelList()).that(list)
    }
}