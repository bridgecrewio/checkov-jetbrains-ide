<<<<<<< HEAD:src/main/kotlin/com/github/bridgecrewio/checkov/MyBundle.kt
package com.github.bridgecrewio.checkov
=======
package com.bridgecrew
>>>>>>> 5069401bc9c3f0dba82fdbef6c97312569d32725:src/main/kotlin/com/bridgecrew/MyBundle.kt

import com.intellij.AbstractBundle
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey

@NonNls
private const val BUNDLE = "messages.MyBundle"

object MyBundle : AbstractBundle(BUNDLE) {

    @Suppress("SpreadOperator")
    @JvmStatic
    fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any) =
            getMessage(key, *params)

    @Suppress("SpreadOperator")
    @JvmStatic
    fun messagePointer(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any) =
            getLazyMessage(key, *params)
}
