package space.narrate.waylan.about.ui.about

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import space.narrate.waylan.about.data.AboutRepository
import space.narrate.waylan.about.data.ThirdPartyLibraryStore

class AboutRepositoryTest {

    // Dependencies
    private val thirdPartyLibraryStore = ThirdPartyLibraryStore

    // Subject under test
    private lateinit var aboutRepository: AboutRepository

    @Before
    fun setUp() {
        aboutRepository = AboutRepository(thirdPartyLibraryStore)
    }

    @Test
    fun allThirdPartyLibs_returnsNonEmptyList() {
        assertThat(aboutRepository.allThirdPartyLibraries).isNotEmpty()
    }
}