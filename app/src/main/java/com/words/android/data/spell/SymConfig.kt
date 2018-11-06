package com.words.android.data.spell

object SymConfig {
    const val defaultMaxEditDistance = 2
    const val defaultDictionaryEditDistance = 3
    const val defaultPrefixLength = 7
    const val defaultMinCountThreshold = 1L
    const val defaultMaxCountThreshold = Long.MAX_VALUE
    const val defaultInitialCapacity = 16
    const val defaultCompactLevel = 5
    const val defaultTargetCount = 943997L
    val defaultEditDistanceAlgorithm = EditDistance.DistanceAlgorithm.Damerau

    const val defaultTermIndex = 0
    const val defaultCountIndex = 1
    const val defaultCorpusPath = "corpus/frequency_dictionary_en_82_765.txt"
    val defaultVerbosity = SymSpell.Verbosity.All
    const val defaultMaxEditDistanceLookup = 3

    const val metadataId = 5555
}