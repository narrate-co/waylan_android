package com.words.android.data.mw

import com.words.android.data.disk.mw.Word
import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

/**
 * <entry id="hypocrite">
 *     <ew>hypocrite</ew>
 *     <hw>hyp*o*crite</hw>
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

@Root(name = "entry")
class MwApiWord {

    @field:Attribute
    var id: String = ""

    @field:Element(name = "ew")
    var word: String = ""

    @field:Element(name = "hw")
    var phonetic: String = ""

    @field:Element(name = "sound")
    var sound: Sound = Sound()

    @field:Element(name = "pr")
    var pronunciation: String = ""

    @field:Element(name = "fl")
    var partOfSpeech: String = ""

    @field:Element(name = "et")
    var etymology: String = ""

    @field:Element(name = "def")
    var def: Definition = Definition()

    @field:Element(name = "uro")
    var uro: Uro = Uro()
}

class Sound {
    @field:Element
    var wav: String = ""
}

class Definition {
    @field:Element(name = "date")
    var date: String = ""

    @field:ElementList(inline = true, name = "dt")
    var dts: List<String> = emptyList()

}

class Uro {

    @field:Element(name = "ure")
    var ure: String = ""

    @field:Element(name = "fl")
    var fl: String = ""
}


val MwApiWord.toDbMwWord: Word
    get()  = Word(
            this.id,
            this.phonetic,
            this.sound.wav,
            this.pronunciation,
            this.partOfSpeech,
            this.etymology,
            com.words.android.data.disk.mw.Uro(this.uro.ure, this.uro.fl))

val MwApiWord.toDbMwDefinitions: List<com.words.android.data.disk.mw.Definition>
    get() {
        return listOf(
                com.words.android.data.disk.mw.Definition(
                        this.word,
                        this.def.date,
                        this.def.dts
                )
        )
    }


