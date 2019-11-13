package space.narrate.waylan.test_common

import okhttp3.mockwebserver.MockResponse
import java.io.File

/**
 * Load a json file as the body of a MockResponse.
 */
fun MockResponse.setBodyFromJson(path: String): MockResponse {
    val uri = this.javaClass.classLoader!!.getResource(path)
    val file = File(uri.path)
    return setBody(String(file.readBytes()))
}