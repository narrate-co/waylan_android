package com.words.android.data.mw

import com.words.android.data.disk.mw.Word
import org.simpleframework.xml.*
import org.simpleframework.xml.convert.Convert
import org.simpleframework.xml.convert.Converter
import org.simpleframework.xml.stream.InputNode
import org.simpleframework.xml.stream.OutputNode

/**
 * <entry id="hypocrite">
 *     <word>hypocrite</word>
 *     <phonetic>hyp*o*crite</phonetic>
 *     <sound>
 *         <wav>hypocr02.wav</wav>
 *     </sound>
 *     <pr>ˈhi-pə-ˌkrit</pr>
 *     <fl>noun</fl>
 *     <et>Middle English
 *          <it>ypocrite,</it> from Anglo-French, from Late Latin
 *          <it>hypocrita,</it> from Greek
 *          <it>hypokritēs</it> actor, hypocrite, from
 *          <it>hypokrinesthai</it>
 *     </et>
 *     <def>
 *         <date>13th century</date>
 *         <sn>1</sn>
 *         <dt>:a person who puts on a false appearance of <d_link>virtue</d_link> or religion</dt>
 *         <sn>2</sn>
 *         <dt>:a person who acts in contradiction to his or her stated beliefs or feelings</dt>
 *     </def>
 *     <uro>
 *         <ure>hypocrite</ure>
 *         <fl>adjective</fl>
 *     </uro>
 * </entry>
 */

@Root(name = "entry_list", strict = false)
class EntryList {

    @field:Attribute
    var version: String = ""

    @field:ElementList(entry = "entry", inline = true)
    var entries: MutableList<Entry> = mutableListOf()
}

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

    @field:Element(name = "sound", required = false)
    var sound: Sound = Sound()

    @field:Element(name = "cx", required = false)
    var cx: Cx = Cx()

    @field:Element(name = "pr", required = false)
    @field:Convert(FormattedStringConverter::class)
    var pronunciation: FormattedString = FormattedString()

    @field:Element(name = "fl", required = false)
    var partOfSpeech: String = ""

    @field:ElementList(inline = true, entry = "in", required = false)
    var variants: MutableList<Variants> = mutableListOf()

    @field:Element(name = "et", required = false)
    @field:Convert(FormattedStringConverter::class)
    var etymology: FormattedString = FormattedString()

    @field:Element(name = "def", required = false)
    var def: Definition = Definition()

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
    @field:Element(name = "wav", required = false)
    var wav: String = ""

    @field:Element(name = "wpr", required = false)
    var wpr: String = ""
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

@Root
@Convert(FormattedStringConverter::class)
data class FormattedString(var value: String = "")

class FormattedStringConverter: Converter<FormattedString> {

    override fun write(node: OutputNode?, value: FormattedString?) {
        //Do nothing. We're not writing XML
    }

    override fun read(node: InputNode?): FormattedString {
        val builder = StringBuilder()

        builder.append(node?.value ?: "")
        flatten(node, node?.next, builder)

        val formattedString = FormattedString(builder.toString())
        println("FormattedStringConverter:: returning = ${formattedString.value}")
        return formattedString
    }

    private fun flatten(parent: InputNode?, child: InputNode?, sb: StringBuilder) {
        if (parent != null) {

            //append floating text before child element
            sb.append(parent.value ?: "")


            var c = child
            while (c != null) {
                sb.append("<${c.name}>")
                sb.append(c.value ?: "")
                sb.append("</${c.name}>")

                //append floating text after child element
                sb.append(parent.value ?: "")

                flatten(c, c.next, sb)

                c = parent.next
            }
        }
    }
}

class Definition {

    @field:ElementList(inline = true, entry = "vt", required = false)
    var vt: MutableList<String> = mutableListOf()

    @field:Element(name = "date", required = false)
    var date: String = ""

    @field:ElementList(inline = true, entry = "sn", required = false)
    var sn: MutableList<String> = mutableListOf()

    @field:Element(name = "sd", required = false)
    var sd: String = ""

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
    var pr: String = ""

    @field:Element(name = "vr", required = false)
    var vr: Vr = Vr()
}

class Vr {
    @field:Element(name = "vl", required = false)
    var vl: String = ""

    @field:Element(name = "va", required = false)
    var va: String = ""

    @field:Element(name = "sound", required = false)
    var sound: Sound = Sound()

    @field:Element(name = "pr", required = false)
    var pr: String = ""

}

val Entry.toDbMwWord: Word
    get()  = Word(
            this.word,
            this.phonetic,
            this.sound.wav,
            this.pronunciation.value,
            this.partOfSpeech,
            this.etymology.value,
            com.words.android.data.disk.mw.Uro(this.uro.firstOrNull()?.ure ?: "", this.uro.firstOrNull()?.fl ?: ""))

val Entry.toDbMwDefinitions: List<com.words.android.data.disk.mw.Definition>
    get() {
        return listOf(
                com.words.android.data.disk.mw.Definition(
                        "${this.word}${this.def.dts}",
                        this.word,
                        this.def.date,
                        this.def.sn,
                        this.def.dts.map { it.value }
                )
        )
    }
