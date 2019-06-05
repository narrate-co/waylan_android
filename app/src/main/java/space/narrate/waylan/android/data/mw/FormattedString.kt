package space.narrate.waylan.android.data.mw

import org.simpleframework.xml.Root
import org.simpleframework.xml.convert.Convert
import org.simpleframework.xml.convert.Converter
import org.simpleframework.xml.stream.InputNode
import org.simpleframework.xml.stream.OutputNode


/**
 * A convenience class used in Merriam-Webster [Entry] object fields to easily
 * signal the need to convert a Node and a Node's children using [FormattedStringConverter].
 */
@Root
@Convert(FormattedStringConverter::class)
data class FormattedString(var value: String = "")


/**
 * A class to convert XML formatted Strings into HTML formatted Strings.
 *
 * The Merriam-Webster API returns many fields that contain XML markup for things like
 * links, all caps, italics, etc. These are not officially documented by Merriam-Webster. Below
 * is the best mapping understood so far:
 *
 * d_link -> link to another word
 * sx -> all caps link to another word
 * vi -> a separator (•)
 * it -> italics
 * sxn -> sn reference to another word
 * fn -> link to another word
 * dx -> start reference to extra material (illustrations)
 * dxt -> dx reference name
 * dxn -> dx reference listType (illustration)
 * ag -> attribution (author, name, etc.)
 *
 */
class FormattedStringConverter: Converter<FormattedString> {

    override fun write(node: OutputNode?, value: FormattedString?) {
        //Do nothing. We're not writing XML
    }

    override fun read(node: InputNode?): FormattedString {
        val builder = StringBuilder()

        builder.append(node?.value ?: "")
        flatten(node, node?.next, builder)

        val formattedString = FormattedString(builder.toString())
        return formattedString
    }

    private fun flatten(parent: InputNode?, child: InputNode?, sb: StringBuilder) {
        if (parent != null) {

            //append floating textRes before child element
            appendValue(parent.value, sb)

            var c = child
            while (c != null) {

                //TODO keep adding conversions for more xml->html tags
                when (c.name) {
                    "d_link", "sx", "fn" -> {
                        appendValueInsideTag(
                                if (c.name == "d_link") c.value else c.value.toUpperCase(),
                                "u",
                                sb
                        )
                    }
                    "vi" -> appendValue("• ", sb)
                    "it" -> appendValueInsideTag(c.value, "i", sb)
                    "dx", "dxt", "dxn" -> appendValue("", sb) //do nothing. remove
                    "ag" -> {
                        appendValue(" - ${c.value}", sb)
                    }
                    else -> appendValueInsideTag(c.value, c.name, sb)
                }

                //append floating textRes after child element
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

