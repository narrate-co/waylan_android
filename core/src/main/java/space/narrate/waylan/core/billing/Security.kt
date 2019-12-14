package space.narrate.waylan.core.billing

import android.util.Base64
import com.android.billingclient.util.BillingHelper
import java.io.IOException
import java.security.InvalidKeyException
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.PublicKey
import java.security.Signature
import java.security.SignatureException
import java.security.spec.X509EncodedKeySpec

object Security {

    private const val TAG = "IABUtil/Security"
    private const val KEY_FACTORY_ALGORITHM = "RSA"
    private const val SIGNATURE_ALGORITHM = "SHA1withRSA"

    @Throws(IOException::class)
    fun verifyPurchase(base64PublicKey: String, signedData: String, signature: String): Boolean {
        if (signedData.isEmpty() || base64PublicKey.isEmpty() || signature.isEmpty()) {
            return false
        }

        val key = generatePublicKey(base64PublicKey)
        return verify(key, signedData, signature)
    }

    @Throws(IOException::class)
    private fun generatePublicKey(encodedPublicKey: String): PublicKey {
        try {
            val decodedKey = Base64.decode(encodedPublicKey, Base64.DEFAULT)
            val keyFactory = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM)
            return keyFactory.generatePublic(X509EncodedKeySpec(decodedKey))
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        } catch (e: InvalidKeyException) {
            val msg = "Invalid key specification : $e"
            BillingHelper.logWarn(TAG, msg)
            throw IOException(msg)
        }
    }

    private fun verify(publicKey: PublicKey, signedData: String, signature: String): Boolean {
        val signatureBytes = try {
            Base64.decode(signature, Base64.DEFAULT)
        } catch (e: IllegalArgumentException) {
            BillingHelper.logWarn(TAG, "Base64 decoding failed.")
            return false
        }

        try {
            val signatureAlgorithm = Signature.getInstance(SIGNATURE_ALGORITHM)
            signatureAlgorithm.initVerify(publicKey)
            signatureAlgorithm.update(signedData.toByteArray())
            if (!signatureAlgorithm.verify(signatureBytes)) {
                BillingHelper.logWarn(TAG, "Signature verification failed.")
                return false
            }
            return true
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        } catch (e: InvalidKeyException) {
            BillingHelper.logWarn(TAG, "Invalid key specification")
        } catch (e: SignatureException) {
            BillingHelper.logWarn(TAG, "Signature exception")
        }

        return false
    }
}