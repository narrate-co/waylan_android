package com.wordsdict.android.data.spell

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.android.UI
import kotlinx.coroutines.launch
import java.io.FileNotFoundException

class SymSpellStore(context: Context) {

    //TODO optimize for mobile
    //        symSpell = new SymSpell(-1, maxEditDistanceLookup, -1, countThreshold);//, (byte)18);
    //        symSpell = new SymSpell(-1, maxEditDistanceLookup, -1, .5, SymSpell.RangeShift.Middle);
    private var symSpell: SymSpell = SymSpell(-1, 3, -1, SymConfig.defaultTargetCount, Long.MAX_VALUE)

    init {
        launch {
            val inputStream = context.assets.open(SymConfig.defaultCorpusPath)
            if (!symSpell.loadDictionary(inputStream, SymConfig.defaultTermIndex, SymConfig.defaultCountIndex)) throw FileNotFoundException("${SymConfig.defaultCorpusPath} not found")
        }
    }

    fun lookup(input: String): List<SuggestItem> {
        return symSpell.lookup(input, SymConfig.defaultVerbosity, SymConfig.defaultMaxEditDistanceLookup)
    }


    fun lookupLive(input: String): LiveData<List<SuggestItem>> {
        val liveData = MutableLiveData<List<SuggestItem>>()
        launch(UI) {
            val results = lookup(input)
            liveData.value = results
        }
        return liveData
    }

    fun lookupCompount(input: String): SuggestItem {
        return symSpell.lookupCompound(input, SymConfig.defaultMaxEditDistanceLookup)[0]
    }

}