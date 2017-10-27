package se.kantarsifo.mobileanalytics.sampleapp;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import se.sifo.analytics.mobileapptagging.android.TSMobileAnalytics;


/**
 * Created by ahmetcengiz on 26/09/2017.
 */

public class InitializeFragment extends Fragment implements CompoundButton.OnCheckedChangeListener {
    private EditText mAppNameET, mCpIdET;
    private CheckBox panelistOnly, useHttps, logEnabled;
    private ViewPagerListener mListener;
    private Button initButton;
    private TextView failTV, successTV;

    public InitializeFragment() {

    }

    public InitializeFragment(ViewPagerListener listener) {
        mListener = listener;
    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.initialize_fragment, container, false);
        mAppNameET = (EditText) v.findViewById(R.id.appNameET);
        mCpIdET = (EditText) v.findViewById(R.id.cpIdET);
        panelistOnly = (CheckBox) v.findViewById(R.id.panelistOnly);
        useHttps = (CheckBox) v.findViewById(R.id.useHttps);
        logEnabled = (CheckBox) v.findViewById(R.id.logEnabled);
        successTV = (TextView) v.findViewById(R.id.success_request);
        failTV = (TextView) v.findViewById(R.id.fail_request);

        //set current framework analytic
        if (TSMobileAnalytics.getInstance() != null) {
            successTV.setText("success request: " + TSMobileAnalytics.getInstance().getNbrOfSuccessfulRequests());
            failTV.setText("fail request: " + TSMobileAnalytics.getInstance().getNbrOfFailedRequests());
            Log.v("requestqueue", "queue :" + TSMobileAnalytics.getInstance().getRequestQueue().size());
        }

        //set starter data
        mAppNameET.setText("MyAppName");
        mCpIdET.setText(Contants.CODIGO_CPID);

        //get settings of last initialized framework
        getPreferenceSetup();

        //show webview to test
        Button webViewBtn = (Button) v.findViewById(R.id.btn_webview);
        webViewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TSMobileAnalytics.getInstance() != null) {
                    mListener.sendPageNumber(2);
                } else {
                    showToastMessage("Framework must be initialized");
                }
            }
        });

        //show list view to test
        Button nativeViewBtn = (Button) v.findViewById(R.id.btn_native);
        nativeViewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TSMobileAnalytics.getInstance() != null) {
                    mListener.sendPageNumber(1);
                } else {
                    showToastMessage("Framework must be initialized");
                }
            }
        });

        Button destroyButton = (Button) v.findViewById(R.id.destroy_button);
        destroyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                destroyCurrentFramework();
            }
        });

        //to detect cpId or application name is changed
        setAddTextChangeListeners();

        //to detect framework's params is changed(useHttp, panelistTrackingOnly, logEnabled)
        setOnCheckedChangeListeners();


        initButton = (Button) v.findViewById(R.id.initialize_button);
        initButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (mCpIdET.getText().length() > 0 && mAppNameET.getText().length() > 0 && mAppNameET.getText().length() > 0) {
                    if (mCpIdET.getText().length() == 4 || mCpIdET.getText().length() == 32) {

                        //initialize framework with builder
                        initializeFrameworkWithBuilder();

                        //or use this one
                        //initializeFrameworkWithCreateInstance();

                        //save setting of last initialized framework
                        setPreferenceSetup();

                        //print setting of current framework
                        printParams();

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (TSMobileAnalytics.getInstance() != null) {
                                    view.getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
                                }
                            }
                        }, 500);
                    } else {
                        showToastMessage("cpId must be 4 or 32 character");
                    }
                } else {
                    showToastMessage("cpId or application name can not be empty");
                }
            }
        });

        if (TSMobileAnalytics.getInstance() != null) {
            initButton.getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
        }

        return v;
    }

    /**
     * Initialize method of framework.
     */
    private void initializeFrameworkWithCreateInstance() {
        TSMobileAnalytics.setLogPrintsActivated(logEnabled.isChecked());
        TSMobileAnalytics.useHttps(useHttps.isChecked());
        TSMobileAnalytics.createInstance(getActivity().getApplicationContext(), mCpIdET.getText().toString(), mAppNameET.getText().toString(), panelistOnly.isChecked());
    }

    /**
     * Initialize method of framework with builder.
     * In your app, you can use it onCreate method.
     */
    private void initializeFrameworkWithBuilder() {
        TSMobileAnalytics.createInstance(new TSMobileAnalytics.Builder(getActivity().getApplicationContext())
                .setCpId(mCpIdET.getText().toString())
                .setApplicationName(mAppNameET.getText().toString())
                .setPanelistTrackingOnly(panelistOnly.isChecked())
                .setLogPrintsActivated(logEnabled.isChecked())
                .useHttps(useHttps.isChecked())
                .build());
    }

    private void setOnCheckedChangeListeners() {
        panelistOnly.setOnCheckedChangeListener(this);
        useHttps.setOnCheckedChangeListener(this);
        logEnabled.setOnCheckedChangeListener(this);
    }

    private void setAddTextChangeListeners() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                destroyCurrentFramework();
            }
        };

        mCpIdET.addTextChangedListener(textWatcher);
        mAppNameET.addTextChangedListener(textWatcher);
    }


    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        destroyCurrentFramework();
    }

    private void showToastMessage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();

    }

    private void setPreferenceSetup() {
        PublicSharedPreferences.setDefaults(Contants.CPID_PREFERENCE, mCpIdET.getText().toString(), getContext());
        PublicSharedPreferences.setDefaults(Contants.APP_NAME_PREFERENCE, mAppNameET.getText().toString(), getContext());
        PublicSharedPreferences.setBool(Contants.LOG_ENABLED_PREFERENCE, logEnabled.isChecked(), getContext());
        PublicSharedPreferences.setBool(Contants.USE_HTTPS_PREFERENCE, useHttps.isChecked(), getContext());
        PublicSharedPreferences.setBool(Contants.PANELIST_TRACKING_ONLY_PREFERENCE, panelistOnly.isChecked(), getContext());
    }

    private void getPreferenceSetup() {
        if (PublicSharedPreferences.getDefaults(Contants.CPID_PREFERENCE, getContext()) != null) {
            mCpIdET.setText(PublicSharedPreferences.getDefaults(Contants.CPID_PREFERENCE, getContext()));
        }

        if (PublicSharedPreferences.getDefaults(Contants.APP_NAME_PREFERENCE, getContext()) != null) {
            mAppNameET.setText(PublicSharedPreferences.getDefaults(Contants.APP_NAME_PREFERENCE, getContext()));
        }

        useHttps.setChecked(PublicSharedPreferences.getBoolean(Contants.USE_HTTPS_PREFERENCE, getContext()));
        panelistOnly.setChecked(PublicSharedPreferences.getBoolean(Contants.PANELIST_TRACKING_ONLY_PREFERENCE, getContext()));
        logEnabled.setChecked(PublicSharedPreferences.getBoolean(Contants.LOG_ENABLED_PREFERENCE, getContext()));
    }

    /**
     * Destroy current framework
     * In your app, you DON'T have to pay attention to use this method.
     */
    private void destroyCurrentFramework() {
        successTV.setText("success request: " + 0);
        failTV.setText("success request: " + 0);
        initButton.getBackground().clearColorFilter();
        TSMobileAnalytics.destroyFramework();
    }

    private void printParams() {
        Log.v("printParams", "cpId :" + mCpIdET.getText());
        Log.v("printParams", "appName :" + mAppNameET.getText());
        Log.v("printParams", "useHttps :" + useHttps.isChecked());
        Log.v("printParams", "logEnabled :" + logEnabled.isChecked());
        Log.v("printParams", "panelistTrackingOnly :" + panelistOnly.isChecked());
    }


}
