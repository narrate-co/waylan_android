package com.wordsdict.android.data.mw

import org.simpleframework.xml.*
import org.simpleframework.xml.convert.Convert

/**
 * The XML representation of a Merriam-Webster API response.
 *
 * @sample mw_response_quiescent A same Merriam-Webster API response
 *      for GET: https://www.dictionaryapi.com/api/v1/references/collegiate/xml/quiescent?key=<merriam_webster_dev_key>
 *
 */
@Root(name = "entry_list", strict = false)
class EntryList {

    @field:Attribute
    var version: String = ""

    @field:ElementList(entry = "entry", inline = true, required = false)
    var entries: MutableList<Entry> = mutableListOf()

    // A list of possible alternate words (as they appear in the dictionary)
    // typically only returned when entries are empty
    @field:ElementList(entry = "suggestion", inline = true, required = false)
    var suggestions: MutableList<String> = mutableListOf()
}

@Root(name = "entry", strict = false)
class Entry {

    @field:Attribute(name = "id", required = false)
    var id: String = ""

    @field:Element(name = "ew", required = false)
    var word: String = ""

    @field:Element(name = "subj", required = false)
    var subj: String = ""

    @field:ElementList(entry = "grp", inline = true, required = false)
    var grps: MutableList<String> = mutableListOf()

    @field:Element(name = "hw", required = false)
    var phonetic: String = ""

    @field:Element(name = "lb", required = false)
    var lb: String = ""

    @field:ElementList(entry = "sound", inline = true, required = false)
    var sounds: MutableList<Sound> = mutableListOf()

    @field:Element(name = "vr", required = false)
    var vr: Vr = Vr()

    @field:Element(name = "cx", required = false)
    var cx: Cx = Cx()

    @field:ElementList(entry = "pr", inline = true, required = false)
    @field:Convert(FormattedStringConverter::class)
    var pronunciations: List<FormattedString> = mutableListOf()

    @field:Element(name = "fl", required = false)
    var partOfSpeech: String = ""

    @field:ElementList(inline = true, entry = "in", required = false)
    var variants: MutableList<Variants> = mutableListOf()

    @field:Element(name = "et", required = false)
    @field:Convert(FormattedStringConverter::class)
    var etymology: FormattedString = FormattedString()

    @field:Element(name = "def", required = false)
    var def: Definition = Definition()

    @field:ElementList(entry = "art", inline = true, required = false)
    var art: MutableList<Art> = mutableListOf()

    @field:Element(name = "dro", required = false)
    var dro: Dro = Dro()

    @field:ElementList(entry = "uro", inline = true, required = false)
    var uro: MutableList<Uro> = mutableListOf()
}

class Cx {
    @field:Element(name = "cl", required = false)
    var cl: String = ""

    @field:Element(name = "ct", required = false)
    var ct: String = ""
}

class Sound {
    @field:ElementList(entry = "wav", inline = true, required = false)
    var wav: MutableList<String> = mutableListOf()

    @field:ElementList(entry = "wpr", inline = true, required = false)
    var wpr: MutableList<String> = mutableListOf()
}

class Variants {
    @field:ElementList(entry = "if", inline = true, required = false)
    var variant: MutableList<String> = mutableListOf()

    @field:Element(name = "sound", required = false)
    var sound: Sound = Sound()

    @field:Element(name = "pr", required = false)
    var pr: String = ""

    @field:Element(name = "il", required = false)
    var il: String = ""
}

class Art {

    @field:Element(name = "artref", required = false)
    var artRef: ArtRef = ArtRef()

    @field:Element(name = "bmp", required = false)
    var bmp: String = ""

    @field:Element(name = "capt", required = false)
    @field:Convert(FormattedStringConverter::class)
    var capt: FormattedString = FormattedString()

    @field:Element(name = "cap", required = false)
    @field:Convert(FormattedStringConverter::class)
    var cap: FormattedString = FormattedString()

    @field:Element(name = "dim", required = false)
    var dim: String = ""
}

class ArtRef {
    @field:Attribute
    var id: String = ""
}


class Definition {

    @field:ElementList(inline = true, entry = "vt", required = false)
    var vt: MutableList<String> = mutableListOf()

    @field:Element(name = "date", required = false)
    var date: String = ""

    @field:Element(name = "sl", required = false)
    var sl: String = ""

    @field:ElementList(inline = true, entry = "sn", required = false)
    var sn: MutableList<String> = mutableListOf()

    @field:ElementList(entry = "sd", inline = true, required = false)
    var sd: MutableList<String> = mutableListOf()

    @field:ElementList(entry = "ssl", inline = true, required = false)
    var ssl: MutableList<String> = mutableListOf()

    @field:Element(name = "sin", required = false)
    var sin: Sin = Sin()

    @field:Element(name = "svr", required = false)
    @field:Convert(FormattedStringConverter::class)
    var svr: FormattedString = FormattedString()

    @field:Element(name = "ss", required = false)
    var ss: String = ""

    @field:ElementList(inline = true, entry = "dt", type = FormattedString::class)
    @field:Convert(FormattedStringConverter::class)
    var dts: MutableList<FormattedString> = mutableListOf()

    @field:ElementList(entry = "snp", inline = true, required = false)
    var snps: MutableList<String> = mutableListOf()

    @field:ElementList(inline = true, entry = "slb", required = false)
    var slb: MutableList<String> = mutableListOf()


}

class Dro {
    @field:Element(name = "drp", required = false)
    var drp: String = ""

    @field:Element(name = "def", required = false)
    var definition: Definition = Definition()
}

class Sin {
    @field:Element(name = "spl", required = false)
    var spl: String = ""
}

class Uro {

    @field:Element(name = "ure", required = false)
    var ure: String = ""

    @field:Element(name = "fl", required = false)
    var fl: String = ""

    @field:Element(name = "sound", required = false)
    var sound: Sound = Sound()

    @field:Element(name = "pr", required = false)
    @field:Convert(FormattedStringConverter::class)
    var pr: FormattedString = FormattedString()

    @field:Element(name = "vr", required = false)
    var vr: Vr = Vr()
}

class Vr {
    @field:Element(name = "vl", required = false)
    var vl: String = ""

    @field:Element(name = "va", required = false)
    var va: String = ""

    @field:ElementList(entry = "sound", inline = true, required = false)
    var sound: MutableList<Sound> = mutableListOf()

    @field:Element(name = "pr", required = false)
    var pr: String = ""

}
