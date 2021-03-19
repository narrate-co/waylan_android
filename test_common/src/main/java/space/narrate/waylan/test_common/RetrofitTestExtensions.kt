package space.narrate.waylan.test_common

import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

fun <T> T.toSuccessfulCall(): Call<T> {
    val obj = this
    return object : Call<T> {
        override fun enqueue(callback: Callback<T>) {
            callback.onResponse(this, execute())
        }

        override fun isExecuted(): Boolean {
            return false
        }

        override fun clone(): Call<T> {
            return this
        }

        override fun isCanceled(): Boolean {
            return false
        }

        override fun cancel() {
            // do nothing
        }

        override fun execute(): Response<T> {
            return Response.success(obj)
        }

        override fun request(): Request {
            return Request.Builder().build()
        }

        override fun timeout(): Timeout {
          return Timeout()
        }
    }
}

