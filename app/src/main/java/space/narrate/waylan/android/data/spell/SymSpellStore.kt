package space.narrate.waylan.android.data.spell

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.FileNotFoundException
import kotlin.coroutines.CoroutineContext

/**
 * A top-level store to access an in-memory instance of JavaSymSpell, a Java port of SymSpell.
 *
 * For details on SymSpell, see <a>https://github.com/wolfgarbe/SymSpell</a>
 * For details on JavaSymSpell, see <a>https://github.com/Lundez/JavaSymSpell</a>
 *
 * TODO port JavaSymSpell to Kotlin
 *
 * TODO further optimize memory use and corpus data for Words-specific use case.
 * TODO this could include the ability to learn the most common word frequency range a user
 * TODO typically searches for and initialize in-memory SymSpell instances targeting those
 * TODO ranges.
 *
 * TODO check available memory and don't init SymSpell if there is not enough available
 */
class SymSpellStore(context: Context) : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    private var symSpell: SymSpell = SymSpell(
        -1,
        3,
        -1,
        SymConfig.defaultTargetCount,
        Long.MAX_VALUE
    )

    init {
        launch {
            val inputStream = context.assets.open(SymConfig.defaultCorpusPath)
            if (!symSpell.loadDictionary(
                    inputStream,
                    SymConfig.defaultTermIndex,
                    SymConfig.defaultCountIndex
                )) throw FileNotFoundException("${SymConfig.defaultCorpusPath} not found")
        }
    }

    /**
     * Find all possible correct spellings/alternatives for the given [input]
     */
    fun lookup(input: String): List<SuggestItem> {
        return symSpell.lookup(
            input,
            SymConfig.defaultVerbosity,
            SymConfig.defaultMaxEditDistanceLookup
        )
    }


    /**
     * Find all possible correct spellings/alterations for the given [input].
     *
     * @return A LiveData object containing a List of [SuggestItem]s
     */
    fun lookupLive(input: String): LiveData<List<SuggestItem>> {
        val liveData = MutableLiveData<List<SuggestItem>>()
        launch(Dispatchers.Main) {
            val results = lookup(input)
            liveData.value = results
        }
        return liveData
    }

}