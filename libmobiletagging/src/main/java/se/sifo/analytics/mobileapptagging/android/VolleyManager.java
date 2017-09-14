package se.sifo.analytics.mobileapptagging.android;

import android.content.Context;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by ahmetcengiz on 14/09/2017.
 */

class VolleyManager {

    private static VolleyManager sInstance;

    private RequestQueue mRequestQueue;

    public static synchronized VolleyManager getInstance() {
        if (!hasInstance()) {
            sInstance = new VolleyManager();
        }
        return sInstance;

    }

    RequestQueue getRequestQueue(Context context) {
        if (!hasRequestQueue()) {
            mRequestQueue = Volley.newRequestQueue(context);
        }
        return mRequestQueue;
    }

    RequestQueue getRequestQueue(){
        return mRequestQueue;
    }


    private static boolean hasInstance() {
        return sInstance != null;
    }

    private boolean hasRequestQueue() {
        return mRequestQueue != null;
    }
}
