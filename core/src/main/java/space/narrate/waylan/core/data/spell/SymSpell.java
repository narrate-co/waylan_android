package space.narrate.waylan.core.data.spell;
//        MIT License
//
//        Copyright (c) 2018 Hampus Londögård
//
//        Permission is hereby granted, free of charge, to any person obtaining a copy
//        of this software and associated documentation files (the "Software"), to deal
//        in the Software without restriction, including without limitation the rights
//        to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//        copies of the Software, and to permit persons to whom the Software is
//        furnished to do so, subject to the following conditions:
//
//        The above copyright notice and this permission notice shall be included in all
//        copies or substantial portions of the Software.
//
//        THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//        IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//        FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//        AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//        LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//        OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
//        SOFTWARE.

import java.io.*;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SymSpell {
    public enum Verbosity{
        Top,
        Closest,
        All
    }
    public enum RangeShift {
        Start,
        Middle,
        End
    }

    private int initialCapacity;
    public int maxDictionaryEditDistance;
    private long minDictionaryCountThreshold = Long.MAX_VALUE; // a simple variable to hold the min count entered
    private long maxDictionaryCountThreshold = Long.MIN_VALUE; // a simple variable to hold the max count entered
    public int prefixLength; //prefix length  5..7
    private long minCountThreshold; //a threshold might be specified, when a term occurs so frequently in the corpus that it is considered a valid word for spelling correction
    private long maxCountThreshold;
    public int compactMask;
    public int maxLength; // a simple variable to hold the max length of a word entered

    // Map of the hash of a delete and it's corresponding, valid dictionary word matches
    private Map<Integer, String[]> deletes;

    // Dictionary of unique correct spelling words, and the frequency count for each word.
    private Map<String, Long> words;

    // Map of unique words that are below the count threshold for being considered correct spellings.
    private Map<String, Long> belowThresholdWords = new HashMap<>();

    // Map of words above the maxCountThreshold
    private Map<String, Long> aboveThresholdWords = new HashMap<>();

    public SymSpell(int initialCapacity, int maxDictionaryEditDistance, int prefixLength, double countThresholdCapturePercentage, RangeShift shift) {
        if (countThresholdCapturePercentage < 0.0 || countThresholdCapturePercentage > 1.0) {
            throw new IllegalArgumentException("countThresholdCapturePercentage must be between 0.0 and 1.0");
        }

        long min = 12714L;
        long max = 23135851162L;
        long range = Math.round(countThresholdCapturePercentage * (max - min));

        long minCount = min;
        long maxCount = max;
        switch (shift){
            case Start:
                maxCount = min + range;
                break;
            case Middle:
                minCount = (max / 2) - (range / 2) - min;
                maxCount = (max / 2) + (range / 2) - min;
                break;
            case End:
                minCount = max - range;
                break;
        }

        init(initialCapacity, maxDictionaryEditDistance, prefixLength, minCount, maxCount);
    }


    public SymSpell(int initialCapacity, int maxDictionaryEditDistance, int prefixLength, long minCountThreshold) {
        init(initialCapacity, maxDictionaryEditDistance, prefixLength, minCountThreshold, Long.MAX_VALUE);
    }

    /// <summary>Create a new instanc of SymSpell.SymSpell.</summary>
    /// <remarks>Specifying ann accurate initialCapacity is not essential,
    /// but it can help speed up processing by aleviating the need for
    /// data restructuring as the size grows.</remarks>
    /// <param name="initialCapacity">The expected number of words in dictionary.</param>
    /// <param name="maxDictionaryEditDistance">Maximum edit distance for doing lookups.</param>
    /// <param name="prefixLength">The length of word prefixes used for spell checking..</param>
    /// <param name="minCountThreshold">The minimum frequency count for dictionary words to be considered correct spellings.</param>
    /// <param name="compactLevel">Degree of favoring lower memory use over speed (0=fastest,most memory, 16=slowest,least memory).</param>
    public SymSpell(int initialCapacity, int maxDictionaryEditDistance, int prefixLength, long minCountThreshold, long maxCountThreshhold)//,
                    //byte compactLevel)
    {
        init(initialCapacity, maxDictionaryEditDistance, prefixLength, minCountThreshold, maxCountThreshhold);
    }

    private void init(int initialCapacity, int maxDictionaryEditDistance, int prefixLength, long minCountThreshold, long maxCountThreshold) {
        if (initialCapacity < 0) initialCapacity = SymConfig.defaultInitialCapacity;
        if (maxDictionaryEditDistance < 0) maxDictionaryEditDistance = SymConfig.defaultDictionaryEditDistance;
        if (prefixLength < 1 || prefixLength <= maxDictionaryEditDistance) prefixLength = SymConfig.defaultPrefixLength;

        if (minCountThreshold > maxCountThreshold) {
            throw new IllegalArgumentException("minCountThreshold cannot be greater than maxCountThreshold");
        }
        if (minCountThreshold < 0) minCountThreshold = SymConfig.defaultMinCountThreshold;
        if (maxCountThreshold < 0) maxCountThreshold = SymConfig.defaultMaxCountThreshold;
//        compactLevel = (byte) defaultCompactLevel;   //TODO might be faulty...

        this.initialCapacity = initialCapacity;
        this.words = new HashMap<>(initialCapacity);
        this.maxDictionaryEditDistance = maxDictionaryEditDistance;
        this.prefixLength = prefixLength;
        this.minCountThreshold = minCountThreshold;
        this.maxCountThreshold = maxCountThreshold;
//        if (compactLevel > 16) compactLevel = 16;
        this.compactMask = (0xffffffff >> (3 + SymConfig.defaultCompactLevel)) << 2;
    }

    /// <summary>Create/Update an entry in the dictionary.</summary>
    /// <remarks>For every word there are deletes with an edit distance of 1..maxEditDistance created and added to the
    /// dictionary. Every delete entry has a suggestions list, which points to the original term(s) it was created from.
    /// The dictionary may be dynamically updated (word frequency and new words) at any time by calling createDictionaryEntry</remarks>
    /// <param name="key">The word to add to dictionary.</param>
    /// <param name="count">The frequency count for word.</param>
    /// <param name="staging">Optional staging object to speed up adding many entry by staging them to a temporary structure.</param>
    /// <returns>True if the word was added as a new correctly spelled word,
    /// or false if the word is added as a below threshold word, or updates an
    /// existing correctly spelled word.</returns>
    public boolean createDictionaryEntry(String key, long count, SuggestionStage staging) {
        if (count <= 0) {
            count = 0;
        }

        if (count <= 0 && this.minCountThreshold > 0) return false; // no point doing anything if count is zero, as it can't change anything



        long countPrevious;

        // look first in below threshold words, update count, and allow promotion to correct spelling word if count reaches threshold
        // threshold must be >1 for there to be the possibility of low threshold words
        if (aboveThresholdWords.containsKey(key)) {
            countPrevious = aboveThresholdWords.get(key);
            count = (Long.MAX_VALUE - countPrevious > count) ? countPrevious + count : Long.MAX_VALUE;
            aboveThresholdWords.put(key, count);
            return false;
        }
        else if (minCountThreshold > 1 && belowThresholdWords.containsKey(key)) {
            countPrevious = belowThresholdWords.get(key);
            // calculate new count for below threshold word
            count = (Long.MAX_VALUE - countPrevious > count) ? countPrevious + count : Long.MAX_VALUE;
            // has reached threshold - remove from below threshold collection (it will be added to correct words below)

            if (count >= minCountThreshold) {
                belowThresholdWords.remove(key);
                if (count >= maxCountThreshold) {
                    aboveThresholdWords.put(key, count);
                    return false;
                }
            }
            else {
                belowThresholdWords.put(key, count); // = count;
                return false;
            }
        }
        else if (words.containsKey(key)) {
            countPrevious = words.get(key);
            // just update count if it's an already added above threshold word
            count = (Long.MAX_VALUE - countPrevious > count) ? countPrevious + count : Long.MAX_VALUE;
            if (count >= maxCountThreshold) {
                words.remove(key);
                aboveThresholdWords.put(key, count);
                return false;
            } else {
                words.put(key, count);
                return false;
            }

        } else if (count < minCountThreshold) {
            // new or existing below threshold word
            belowThresholdWords.put(key, count);
            return false;
        } else if (count > maxCountThreshold) {
            aboveThresholdWords.put(key, count);
            return false;
        }

        if (count < minDictionaryCountThreshold) minDictionaryCountThreshold = count;
        if (count > maxDictionaryCountThreshold) maxDictionaryCountThreshold = count;

        // what we have at this point is a new, above threshold word
        words.put(key, count);

        //edits/suggestions are created only once, no matter how often word occurs
        //edits/suggestions are created only as soon as the word occurs in the corpus,
        //even if the same term existed before in the dictionary as an edit from another word
        if (key.length() > maxLength) maxLength = key.length();

        //TODO avoid creating deletes until all word are processed
        //TODO keep the deletes Map as slim as possible

//        return createDeletes(key, staging);
        return true;
    }

    private boolean createDeletes(String key, SuggestionStage staging) {

        //create deletes
        HashSet<String> edits = editsPrefix(key);

        // if not staging suggestions, put directly into main data structure
        if (staging != null){
            for (String delete : edits) {
                staging.add(getStringHash(delete, compactMask), key);
            }
        } else {
            if (deletes == null) this.deletes = new HashMap<>(initialCapacity); //initialisierung

            for (String delete : edits) {
                int deleteHash = getStringHash(delete, compactMask);
                String[] suggestions;
                if (deletes.containsKey(deleteHash)){
                    suggestions = deletes.get(deleteHash);
                    String[] newSuggestions = Arrays.copyOf(suggestions, suggestions.length + 1);
                    deletes.put(deleteHash, newSuggestions);
                    suggestions = newSuggestions;
                } else {
                    suggestions = new String[1];
                    deletes.put(deleteHash, suggestions);
                }
                suggestions[suggestions.length - 1] = key;
            }
        }
        return true;
    }

    /// <summary>Load multiple dictionary entry from a file of word/frequency count pairs</summary>
    /// <remarks>Merges with any dictionary data already loaded.</remarks>
    /// <param name="corpus">The path+filename of the file.</param>
    /// <param name="termIndex">The column position of the word.</param>
    /// <param name="countIndex">The column position of the frequency count.</param>
    /// <returns>True if file loaded, or false if file not found.</returns>
    public boolean loadDictionary(String corpus, int termIndex, int countIndex) {
        File file = new File(corpus);
        if (!file.exists()) return false;

        BufferedReader br = null;
        try {
            CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
            br = new BufferedReader(new InputStreamReader(new FileInputStream(corpus), decoder));
//            br = Files.newBufferedReader(Paths.get(corpus), StandardCharsets.UTF_8);
        }catch (IOException ex){
            ex.printStackTrace();
        }
        if (br == null) { return false; }
        return loadDictionary(br, termIndex, countIndex);
    }

    /// <summary>Load multiple dictionary entry from an input stream of word/frequency count pairs</summary>
    /// <remarks>Merges with any dictionary data already loaded.</remarks>
    /// <remarks>This is useful for loading the dictionary data from an asset file in Android.</remarks>
    /// <param name="corpus">An input stream to dictionary data.</param>
    /// <param name="termIndex">The column position of the word.</param>
    /// <param name="countIndex">The column position of the frequency count.</param>
    /// <returns>True if file loaded, or false if file not found.</returns>
    public boolean loadDictionary(InputStream corpus, int termIndex, int countIndex) {
        if (corpus == null) return false;
        BufferedReader br = new BufferedReader(new InputStreamReader(corpus, StandardCharsets.UTF_8));
        return loadDictionary(br, termIndex, countIndex);
    }

    /// <summary>Load multiple dictionary entry from an buffered reader of word/frequency count pairs</summary>
    /// <remarks>Merges with any dictionary data already loaded.</remarks>
    /// <param name="corpus">An buffered reader to dictionary data.</param>
    /// <param name="termIndex">The column position of the word.</param>
    /// <param name="countIndex">The column position of the frequency count.</param>
    /// <returns>True if file loaded, or false if file not found.</returns>
    public boolean loadDictionary(BufferedReader br, int termIndex, int countIndex) {
        if (br == null) return false;
        
        SuggestionStage staging = new SuggestionStage(16384);
        try {
            for(String line; (line = br.readLine()) != null;){
                String[] lineParts = line.split("\\s");
                if (lineParts.length >= 2) {
                    String key = lineParts[termIndex];
                    long count;
                    try{
                        count = Long.parseLong(lineParts[countIndex]); 
                        //count = Long.parseUnsignedLong(lineParts[countIndex]);
                        createDictionaryEntry(key, count, staging);
                    }catch (NumberFormatException ex){
                        System.out.println(ex.getMessage());
                    }
                }
            }

            for (Map.Entry<String, Long> word: words.entrySet()) {
                createDeletes(word.getKey(), staging);
            }
        }catch (IOException ex){
            ex.printStackTrace();
            System.out.println(ex.getMessage());
        }
        if (this.deletes == null) this.deletes = new HashMap<>(staging.deleteCount());
        commitStaged(staging);

        purgeBelowThresholdWords();
        purgeAboveThresholdWords();
        //TODO in real application, clear words if we already have a dictionary
        return true;
    }

    //create a frequency dictionary from a corpus (merges with any dictionary data already loaded)
    /// <summary>Load multiple dictionary words from a file containing plain textRes.</summary>
    /// <param name="corpus">The path+filename of the file.</param>
    /// <returns>True if file loaded, or false if file not found.</returns>
    public boolean createDictionary(String corpus) {
        File file = new File(corpus);
        if (!file.exists()) return false;

        SuggestionStage staging = new SuggestionStage(16384);
//        try (BufferedReader br = Files.newBufferedReader(Paths.get(corpus))) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(corpus)));
            for (String line; (line = br.readLine()) != null; ) {
                String[] lines = parseWords(line);
                for (String key : lines) {
                    createDictionaryEntry(key ,1, staging);
                }
//                Arrays.stream(parseWords(line)).forEach(key -> createDictionaryEntry(key, 1, staging));
            }
        }catch (IOException ex){
            System.out.println(ex.getMessage());
        }

        if (this.deletes == null) this.deletes = new HashMap<>(staging.deleteCount());
        commitStaged(staging);
        return true;
    }

    public void purgeBelowThresholdWords() {
        belowThresholdWords = new HashMap<>();
    }

    public void purgeAboveThresholdWords() {
        aboveThresholdWords = new HashMap<>();
    }

    /// <summary>Commit staged dictionary additions.</summary>
    /// <remarks>Used when you write your own process to load multiple words into the
    /// dictionary, and as part of that process, you first created a SuggestionsStage
    /// object, and passed that to createDictionaryEntry calls.</remarks>
    /// <param name="staging">The SymSpell.SuggestionStage object storing the staged data.</param>
    public void commitStaged(SuggestionStage staging) {
        staging.commitTo(deletes);
        staging.clear();
    }

    /// <summary>Find suggested spellings for a given input word, using the maximum
    /// edit distance specified during construction of the SymSpell.SymSpell dictionary.</summary>
    /// <param name="input">The word being spell checked.</param>
    /// <param name="verbosity">The value controlling the quantity/closeness of the retuned suggestions.</param>
    /// <returns>A List of SymSpell.SuggestItem object representing suggested correct spellings for the input word,
    /// sorted by edit distance, and secondarily by count frequency.</returns>
    public List<SuggestItem> lookup(String input, Verbosity verbosity) {
        return lookup(input, verbosity, maxDictionaryEditDistance);
    }
    /// <summary>Find suggested spellings for a given input word.</summary>
    /// <param name="input">The word being spell checked.</param>
    /// <param name="verbosity">The value controlling the quantity/closeness of the retuned suggestions.</param>
    /// <param name="maxEditDistance">The maximum edit distance between input and suggested words.</param>
    /// <returns>A List of SymSpell.SuggestItem object representing suggested correct spellings for the input word,
    /// sorted by edit distance, and secondarily by count frequency.</returns>
    public List<SuggestItem> lookup(String input, Verbosity verbosity, int maxEditDistance) {
        //verbosity=Top: the suggestion with the highest term frequency of the suggestions of smallest edit distance found
        //verbosity=Closest: all suggestions of smallest edit distance found, the suggestions are ordered by term frequency
        //verbosity=All: all suggestions <= maxEditDistance, the suggestions are ordered by edit distance, then by term frequency (slower, no early termination)

        // maxEditDistance used in lookup can't be bigger than the maxDictionaryEditDistance
        // used to construct the underlying dictionary structure.
        if (maxEditDistance > maxDictionaryEditDistance) throw new IllegalArgumentException("Dist to big: " + maxEditDistance);

        List<SuggestItem> suggestions = new ArrayList<>();

        if (deletes == null) return suggestions;

        int inputLen = input.length();

        // early exit - word is too big to possibly match any words
        if (inputLen - maxEditDistance > maxLength) return suggestions;

        // deletes we've considered already
        HashSet<String> consideredDeletes = new HashSet<>();
        // suggestions we've considered already
        HashSet<String> consideredSuggestions = new HashSet<>();
        long suggestionCount;

        // quick look for exact match
        if (words.containsKey(input)) {
            suggestionCount = words.get(input);
            suggestions.add(new SuggestItem(input, 0, suggestionCount));
            // early exit - return exact match, unless caller wants all matches
            if (verbosity != Verbosity.All) return suggestions;
        }
        consideredSuggestions.add(input); // input considered in above.

        int maxEditDistance2 = maxEditDistance;
        int candidatePointer = 0;
        List<String> candidates = new ArrayList<>();

        //add original prefix
        int inputPrefixLen = inputLen;
        if (inputPrefixLen > prefixLength) {
            inputPrefixLen = prefixLength;
            candidates.add(input.substring(0, inputPrefixLen));
        } else {
            candidates.add(input);
        }

        EditDistance distanceComparer = new EditDistance(input, SymConfig.INSTANCE.getDefaultEditDistanceAlgorithm());
        while (candidatePointer < candidates.size()) {
            String candidate = candidates.get(candidatePointer++);
            int candidateLen = candidate.length();
            int lengthDiff = inputPrefixLen - candidateLen;

            //early termination if distance higher than suggestion distance
            if (lengthDiff > maxEditDistance2) {
                // skip to next candidate if Verbosity.All, look no further if Verbosity.Top or Closest
                // (candidates are ordered by delete distance, so none are closer than current)
                if (verbosity == Verbosity.All) continue;
                break;
            }

            //read candidate entry from dictionary
            if (deletes.containsKey(getStringHash(candidate, compactMask))) {
                String[] dictSuggestions = deletes.get(getStringHash(candidate, compactMask));
                //iterate through suggestions (to other correct dictionary items) of delete item and add them to suggestion list
                for (String suggestion : dictSuggestions) {
                    if (suggestion.equals(input)) continue;
                    int suggestionLen = suggestion.length();

                    if ((Math.abs(suggestionLen - inputLen) > maxEditDistance2) // input/suggestion diff > allowed/current best distance
                            || (suggestionLen < candidateLen) // sugg must be for a different delete string, in same bin only because of hash collision
                            || (suggestionLen == candidateLen && !suggestion.equals(candidate))) // if sugg len = delete len, then it either equals delete or is in same bin only because of hash collision
                        continue;

                    int suggPrefixLen = Math.min(suggestionLen, prefixLength);
                    if (suggPrefixLen > inputPrefixLen && (suggPrefixLen - candidateLen) > maxEditDistance2) continue;

                    //True Damerau-Levenshtein Edit Distance: adjust distance, if both distances > 0
                    //We allow simultaneous edits (deletes) of maxEditDistance on on both the dictionary and the input term.
                    //For replaces and adjacent transposes the resulting edit distance stays <= maxEditDistance.
                    //For inserts and deletes the resulting edit distance might exceed maxEditDistance.
                    //To prevent suggestions of a higher edit distance, we need to calculate the resulting edit distance, if there are simultaneous edits on both sides.
                    //Example: (bank==bnak and bank==bink, but bank!=kanb and bank!=xban and bank!=baxn for maxEditDistance=1)
                    //Two deletes on each side of a pair makes them all equal, but the first two pairs have edit distance=1, the others edit distance=2.
                    int distance;
                    int min = 0;
                    if (candidateLen == 0) {
                        //suggestions which have no common chars with input (inputLen<=maxEditDistance && suggestionLen<=maxEditDistance)
                        distance = Math.max(inputLen, suggestionLen);
                        if (distance > maxEditDistance2 || !consideredSuggestions.add(suggestion)) continue;
                    } else if (suggestionLen == 1) {
                        if (input.indexOf(suggestion.charAt(0)) < 0) distance = inputLen;
                        else distance = inputLen - 1;
                        if (distance > maxEditDistance2 || !consideredSuggestions.add(suggestion)) continue;
                    } else
                        //number of edits in prefix == maxeditdistance  && no identic suffix
                        //, then editdistance > maxEditDistance and no need for Levenshtein calculation
                        //      (inputLen >= prefixLength) && (suggestionLen >= prefixLength)
                        if ((prefixLength - maxEditDistance == candidateLen)
                                && (((min = Math.min(inputLen, suggestionLen) - prefixLength) > 1)
                                && !(input.substring(inputLen + 1 - min).equals(suggestion.substring(suggestionLen + 1 - min))))
                                || ((min > 0) && (input.charAt(inputLen - min) != suggestion.charAt(suggestionLen - min))
                                && ((input.charAt(inputLen - min - 1) != suggestion.charAt(suggestionLen - min))
                                || (input.charAt(inputLen - min) != suggestion.charAt(suggestionLen - min - 1))))) {
                            continue;
                        } else {
                            // deleteInSuggestionPrefix is somewhat expensive, and only pays off when verbosity is Top or Closest.
                            if ((verbosity != Verbosity.All && !deleteInSuggestionPrefix(candidate, candidateLen, suggestion, suggestionLen, prefixLength))
                                    || !consideredSuggestions.add(suggestion)) continue;
                            distance = distanceComparer.compare(suggestion, maxEditDistance2);
                            if (distance < 0) continue;
                        }

                    //save some time
                    //do not process higher distances than those already found, if verbosity<All (note: maxEditDistance2 will always equal maxEditDistance when Verbosity.All)
                    if (distance <= maxEditDistance2) {
                        suggestionCount = words.get(suggestion);
                        SuggestItem si = new SuggestItem(suggestion, distance, suggestionCount);
                        if (suggestions.size() > 0) {
                            switch (verbosity) {
                                case Closest:
                                    //we will calculate DamLev distance only to the smallest found distance so far
                                    if (distance < maxEditDistance2) suggestions.clear();
                                    break;
                                case Top:
                                    if (distance < maxEditDistance2 || suggestionCount > suggestions.get(0).count) {
                                        maxEditDistance2 = distance;
                                        suggestions.set(0, si);
                                    }
                                    continue;
                            }
                        }
                        if (verbosity != Verbosity.All) maxEditDistance2 = distance;
                        suggestions.add(si);
                    }
                }
            }

            //add edits
            //derive edits (deletes) from candidate (input) and add them to candidates list
            //this is a recursive process until the maximum edit distance has been reached
            if ((lengthDiff < maxEditDistance) && (candidateLen <= prefixLength))
            {
                //save some time
                //do not create edits with edit distance smaller than suggestions already found
                if (verbosity != Verbosity.All && lengthDiff >= maxEditDistance2) continue;

                for (int i = 0; i < candidateLen; i++)
                {
                    StringBuilder sb = new StringBuilder(candidate);
                    sb.deleteCharAt(i);
                    String delete = sb.toString();

                    if (consideredDeletes.add(delete)) { candidates.add(delete); }
                }
            }
        }

        //sort by ascending edit distance, then by descending word frequency
        if (suggestions.size() > 1) Collections.sort(suggestions);
        return suggestions;
    }

    public List<SuggestItem> lookupCompound(String input, int maxEditDistance) {
        //parse input string into single terms
        if(maxEditDistance > maxDictionaryEditDistance) throw new IllegalArgumentException("Dist to big " + maxEditDistance);
        String[] termList1 = parseWords(input);

        List<SuggestItem> suggestions; //suggestions for a single term
        List<SuggestItem> suggestionParts = new ArrayList<>(); // 1 line with separate parts
        List<SuggestItem> suggestionsCombi;
        EditDistance editDistance;

        //translate every term to its best suggestion, otherwise it remains unchanged
        boolean lastCombi = false;
        for (int i = 0; i < termList1.length; i++){      // For each term do loop
            suggestions = lookup(termList1[i], Verbosity.Top, maxEditDistance); // Get the normal suggestions,
            //combi check, always before split. i > 0 because we can't split on zero obviously.
            if((i > 0) && !lastCombi) {
                suggestionsCombi = lookup(termList1[i - 1] + termList1[i], Verbosity.Top, maxEditDistance);

                if (!suggestionsCombi.isEmpty()) {
                    SuggestItem best1 = suggestionParts.get(suggestionParts.size() - 1);    // Grabbing the currently last part of sentence (i-1)
                    SuggestItem best2;
                    if (!suggestions.isEmpty()) best2 = suggestions.get(0);                 // Getting the best suggestion of term (i)
                    else best2 = new SuggestItem(termList1[i], maxEditDistance + 1, 0); // No suggestion -> it might be correct? (i)

                    editDistance = new EditDistance(termList1[i - 1] + " " + termList1[i], EditDistance.DistanceAlgorithm.Damerau);
                    if (suggestionsCombi.get(0).distance + 1 < editDistance.DamerauLevenshteinDistance(best1.term + " " + best2.term, maxEditDistance)) {
                        suggestionsCombi.get(0).distance++;
                        suggestionParts.set(suggestionParts.size() - 1, suggestionsCombi.get(0));   // Replacing value.
                        lastCombi = true;
                        continue;
                    }
                }
            }

            lastCombi = false;

            //always split terms without suggestion / never split terms with suggestion ed=0 / never split single char terms
            if (!suggestions.isEmpty() && ((suggestions.get(0).distance==0) || (termList1[i].length() == 1))) {
                //choose best suggestion
                suggestionParts.add(suggestions.get(0));
            } else {
                //if no perfect suggestion, split word into pairs
                List<SuggestItem> suggestionsSplit = new ArrayList<>();

                //add original term
                if (!suggestions.isEmpty()) {
                    suggestionsSplit.add(suggestions.get(0));
                }

                if (termList1[i].length() > 1) {
                    for (int j=1; j < termList1[i].length(); j++) {      // Begin splitting! j=1 -> last. Shouldnt it be j.size - 1?
                        String part1 = termList1[i].substring(0,j);
                        String part2 = termList1[i].substring(j);
                        SuggestItem suggestionSplit;
                        List<SuggestItem> suggestions1 = lookup(part1, Verbosity.Top, maxEditDistance);

                        if (!suggestions1.isEmpty()) {
                            if (!suggestions.isEmpty() && (suggestions.get(0).equals(suggestions1.get(0)))) continue; // suggestion top = split_1 suggestion top
                            List<SuggestItem> suggestions2 = lookup(part2, Verbosity.Top, maxEditDistance);

                            if(!suggestions2.isEmpty()) {
                                if (!suggestions.isEmpty() && (suggestions.get(0).equals(suggestions2.get(0)))) continue; //suggestion top = split_2 suggestion top

                                //select best suggestion for split pair
                                String split = suggestions1.get(0).term + " " + suggestions2.get(0).term;
                                editDistance = new EditDistance(termList1[i], EditDistance.DistanceAlgorithm.Damerau);
                                suggestionSplit = new SuggestItem(split,
                                        editDistance.DamerauLevenshteinDistance(split, maxEditDistance),
                                        Math.min(suggestions1.get(0).count, suggestions2.get(0).count));
                                if(suggestionSplit.distance >= 0) suggestionsSplit.add(suggestionSplit);

                                //early termination of split
                                if(suggestionSplit.distance == 1) break;
                            }
                        }
                    }

                    if(!suggestionsSplit.isEmpty()) {
                        //select best suggestion for split pair
                        Collections.sort(suggestionsSplit);
                        suggestionParts.add(suggestionsSplit.get(0));
                    } else {
                        SuggestItem si = new SuggestItem(termList1[i], 0, maxEditDistance + 1);
                        suggestionParts.add(si);
                    }
                } else {
                    SuggestItem si = new SuggestItem(termList1[i], 0, maxEditDistance + 1);
                    suggestionParts.add(si);
                }
            }
        }

        SuggestItem suggestion = new SuggestItem("", Integer.MAX_VALUE, Long.MAX_VALUE);

        StringBuilder s = new StringBuilder();

        for(SuggestItem si : suggestionParts){
            s.append(si.term).append(" ");
            suggestion.count = Math.min(suggestion.count, si.count);
        }

        suggestion.term = s.toString().replaceAll("\\s+$", "");
        editDistance = new EditDistance(suggestion.term, EditDistance.DistanceAlgorithm.Damerau);
        suggestion.distance = editDistance.DamerauLevenshteinDistance(input, maxDictionaryEditDistance);

        List<SuggestItem> suggestionsLine = new ArrayList<>();
        suggestionsLine.add(suggestion);
        return suggestionsLine;
    }

    //public bool enableCompoundCheck = true;
    //false: assumes input string as single term, no compound splitting / decompounding
    //true:  supports compound splitting / decompounding with three cases:
    //1. mistakenly inserted space into a correct word led to two incorrect terms
    //2. mistakenly omitted space between two correct words led to one incorrect combined term
    //3. multiple independent input terms with/without spelling errors

    private long getAverageCount() {
        long totalCount = 0L;
        for (Map.Entry<String, Long> entry : words.entrySet()) {
            totalCount += entry.getValue();
        }
        return totalCount / words.size();
    }

    public Map<Integer, String[]> getDeletes() {
        return deletes;
    }

    public Map<String, Long> getWords() {
        return words;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public List<SuggestItem> lookupCompound(String input)
    {
        return lookupCompound(input, this.maxDictionaryEditDistance);
    }

    public static boolean deleteInSuggestionPrefix(String delete, int deleteLen, String suggestion, int suggestionLen, int prefixLength) {
        if (deleteLen == 0) return true;
        if (prefixLength < suggestionLen) suggestionLen = prefixLength;
        int j = 0;
        for (int i = 0; i < deleteLen; i++)
        {
            char delChar = delete.charAt(i);
            while (j < suggestionLen && delChar != suggestion.charAt(j)) j++;
            if (j == suggestionLen) return false;
        }
        return true;
    }

    private String[] parseWords(String text) {
        // \p{L} UTF-8 characters, plus "_", does not split words at apostrophes.
        Pattern pattern = Pattern.compile("['’\\p{L}-[_]]+");
        Matcher match = pattern.matcher(text.toLowerCase());
        List<String> matches = new ArrayList<>();
        while(match.find()){
            matches.add(match.group());
        }
        String[] toreturn = new String[matches.size()];
        matches.toArray(toreturn);
        return toreturn;
    }

    private HashSet<String> edits(String word, int editDistance, HashSet<String> deleteWords) {
        editDistance++;
        if (word.length() > 1) {
            for (int i = 0; i < word.length(); i++) {
                StringBuilder sb = new StringBuilder(word);     //  word.Remove(i, 1);
                sb.deleteCharAt(i);
                String delete = sb.toString();
                if (deleteWords.add(delete)) {
                    //recursion, if maximum edit distance not yet reached
                    if (editDistance < maxDictionaryEditDistance) edits(delete, editDistance, deleteWords);
                }
            }
        }
        return deleteWords;
    }

    private HashSet<String> editsPrefix(String key) {
        HashSet<String> hashSet = new HashSet<>();
        if (key.length() <= maxDictionaryEditDistance) hashSet.add("");
        if (key.length() > prefixLength) key = key.substring(0, prefixLength);
        hashSet.add(key);
        return edits(key, 0, hashSet);
    }

    @SuppressWarnings("unchecked")
    public static int getStringHash(String s, int compactMask) {
        int len = s.length();
        int lenMask = len;
        if (lenMask > 3) lenMask = 3;

        long hash = 2166136261L;
        for (int i = 0; i < len; i++) {
                hash ^= s.charAt(i);
                hash *= 16777619;
        }

        hash &= compactMask;
        hash |= (long)lenMask;
        return (int)hash;
    }

    public long getMaxDictionaryCountThreshold() {
        return maxDictionaryCountThreshold;
    }

    public long getMinDictionaryCountThreshold() {
        return minDictionaryCountThreshold;
    }
}

