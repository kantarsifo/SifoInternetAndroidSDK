package se.kantarsifo.mobileanalytics.framework

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.*


interface LaunchIntent {
    fun launchIntent(intent: Intent)
}

class ActivityResultHandler(private val activity: ComponentActivity,intent: Intent, onResult: (ActivityResult?) -> Unit):
        LaunchIntent by ActivityResultImpl(activity.activityResultRegistry,intent, activity, onResult)

class ActivityResultImpl(private val registry: ActivityResultRegistry,
                         private val intent: Intent,
                         lifecycleOwner: LifecycleOwner,
                         private val onResult: (ActivityResult?)->(Unit)): LifecycleObserver, LaunchIntent
 {

    private lateinit var startForResult: ActivityResultLauncher<Intent>

     @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
     fun onCreate() {
         startForResult = registry.register("key",ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
             startForResult.unregister()
             onResult(result)
         }
         try {
             startForResult.launch(intent)
         } catch (e: Exception) {
             onResult(null)
         }

     }

     init {
         lifecycleOwner.lifecycle.removeObserver(this)
         lifecycleOwner.lifecycle.addObserver(this)
    }

     override fun launchIntent(intent: Intent) {
         startForResult.launch(intent)
     }

}
