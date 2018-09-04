package com.words.android.data.mw

import com.words.android.data.disk.mw.OrderedDefinitionItem
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

    @field:Element(name = "sound", required = false)
    var sound: Sound = Sound()

    @field:Element(name = "vr", required = false)
    var vr: Vr = Vr()

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

    @field:Element(name = "art", required = false)
    var art: Art = Art()

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

    @field:Element(name = "bmp", required = false)
    var bmp: String = ""

    @field:Element(name = "cap", required = false)
    @field:Convert(FormattedStringConverter::class)
    var cap: FormattedString = FormattedString()
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
            appendValue(parent.value, sb)

            var c = child
            while (c != null) {

                //TODO use html compliant tags to easily format string in ui
                /**
                 *
                 * d_link -> link to another word
                 * sx -> all caps link to another word
                 * vi -> •
                 * it -> italics
                 * sxn -> sn reference to another word
                 * fn -> link to another word
                 * dx -> start reference to extra material (illustrations)
                 * dxt -> dx reference name
                 * dxn -> dx reference type (illustration)
                 * ag -> attribution (author, name, etc.)
                 *
                 */

                when (c.name) {
                    "d_link", "sx", "fn" -> appendValueInsideTag(if (c.name == "d_link") c.value else c.value.toUpperCase(), "u", sb)
                    "vi" -> appendValue("• ", sb)
                    "it" -> appendValueInsideTag(c.value, "i", sb)
                    "dx", "dxt", "dxn" -> appendValue("", sb) //do nothing. remove
                    "ag" -> {
                        appendValue(" - ${c.value}", sb)
                    }
                    else -> appendValueInsideTag(c.value, c.name, sb)
                }

                //append floating text after child element
                appendValue(parent.value, sb)

                flatten(c, c.next, sb)

                c = parent.next
            }
        }
    }

    private fun appendValue(value: String?, sb: StringBuilder) {
        val v = value?.replace(":", ": ")
        sb.append(v ?: "")
    }

    private fun appendValueInsideTag(value: String?, tagName: String, sb: StringBuilder) {
        sb.append("<$tagName>")
        appendValue(value, sb)
        sb.append("</$tagName>")
    }
}

val Entry.toDbMwWord: Word
    get()  = Word(
            this.id,
            this.word,
            this.subj,
            this.phonetic,
            com.words.android.data.disk.mw.Sound(this.sound.wav.firstOrNull() ?: "", this.sound.wpr.firstOrNull() ?: ""), //TODO restructure db
            this.pronunciation.value,
            this.partOfSpeech,
            this.etymology.value,
            com.words.android.data.disk.mw.Uro(this.uro.firstOrNull()?.ure ?: "", this.uro.firstOrNull()?.fl ?: ""))

val Entry.toDbMwDefinitions: List<com.words.android.data.disk.mw.Definition>
    get() {
        val orderedDefs = this.def.dts.mapIndexed { index, formattedString ->
            val sn = this.def.sn.getOrNull(index) ?: (index + 1).toString()
            println("Entry::creating ordered DefItem: $sn - $formattedString")
            OrderedDefinitionItem(sn, formattedString.value)
        }
        return listOf(
                com.words.android.data.disk.mw.Definition(
                        "${this.id}${orderedDefs.hashCode()}",
                        this.id,
                        this.word,
                        this.def.date,
                        orderedDefs
                )
        )
    }
