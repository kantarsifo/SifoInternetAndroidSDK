package mo.dyna.sifomobileanalyticssdkforandroid;

import android.app.Application;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.util.Log;

import mo.dyna.sifomobileanalyticssdkforandroid.BuildConfig;
import mo.dyna.sifomobileanalyticssdkforandroid.Contants;
import se.sifo.analytics.mobileapptagging.android.MobileTaggingFramework;
import se.sifo.analytics.mobileapptagging.android.TagDataRequest;
import se.sifo.analytics.mobileapptagging.android.TagDataRequestCallbackListener;

/**
 * Created by Peter on 2015-04-17.
 */
public class ApplicationImpl extends Application implements TagDataRequestCallbackListener {

    private static final TagInfo sTagInfo = new TagInfo();

    @Override
    public void onCreate() {
        super.onCreate();

//        ComponentName webComponent = new ComponentName(this, WebActivity.class);
//        getPackageManager().setComponentEnabledSetting(webComponent,
//                BuildConfig.USE_WEB_ACTIVITY ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
//                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
//                0);
    }


    @Override
    public void onDataRequestComplete(TagDataRequest request) {
        Log.d(Contants.LOG_TAG, request.getURL());
        Log.d(Contants.LOG_TAG, "Data request completed with success:" +
                "\nCode: " + request.getHttpStatusCode() +
                "\nRequest ID: " + request.getRequestID() +
                "\nCat: " + request.getCat() +
                "\nName: " + request.getName() +
                "\nId: " + request.getID());

        Log.d(Contants.LOG_TAG, "Number of successful requests: "
                + MobileTaggingFramework.getInstance().getNbrOfSuccessfulRequests());
        Log.d(Contants.LOG_TAG, "***********************************");
        Log.d(Contants.LOG_TAG, "Request queue size: " + MobileTaggingFramework.getInstance().getRequestQueue().size());
    }

    @Override
    public void onDataRequestFailed(TagDataRequest request) {
        Log.w(Contants.LOG_TAG, request.getURL());
        Log.w(Contants.LOG_TAG, "Data request completed with failure:" +
                "\nCode: " + request.getHttpStatusCode() +
                "\nRequest ID: " + request.getRequestID() +
                "\nCat: " + request.getCat() +
                "\nName: " + request.getName() +
                "\nId: " + request.getID());

        Log.w(Contants.LOG_TAG, "Number of successful requests: "
                + MobileTaggingFramework.getInstance().getNbrOfSuccessfulRequests());
        Log.w(Contants.LOG_TAG, "Number of failed requests: "
                + MobileTaggingFramework.getInstance().getNbrOfFailedRequests());
        Log.w(Contants.LOG_TAG, "***********************************");
        Log.w(Contants.LOG_TAG, "Request queue size: " + MobileTaggingFramework.getInstance().getRequestQueue().size());
    }


    public static TagInfo tagInfo() {
        return sTagInfo;
    }

    public static class TagInfo {
        private String[] categories = new String[2];
        private String name;
        private String contentId;

        public void setCategory(int index, String category) {
            categories[index] = category;
        }

        public String getCategory(int index) {
            return categories[index];
        }

        public String[] getCategories() {
            return categories;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getContentId() {
            return contentId;
        }

        public void setContentId(String contentId) {
            this.contentId = contentId;
        }


    }
}
