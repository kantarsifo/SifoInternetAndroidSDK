package mo.dyna.sifomobileanalyticssdkforandroid;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
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

    public InitializeFragment() {

    }

    public InitializeFragment(ViewPagerListener listener) {
        mListener = listener;
    }

    private String title;
    private int page;

    // newInstance constructor for creating fragment with arguments
    public static InitializeFragment newInstance(int page, String title) {
        InitializeFragment fragmentFirst = new InitializeFragment();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        fragmentFirst.setArguments(args);
        return fragmentFirst;
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
        TSMobileAnalytics.destroyFramework();
        mAppNameET = v.findViewById(R.id.appNameET);
        mCpIdET = v.findViewById(R.id.cpIdET);
        panelistOnly = v.findViewById(R.id.panelistOnly);
        useHttps = v.findViewById(R.id.useHttps);
        logEnabled = v.findViewById(R.id.logEnabled);

        mAppNameET.setText("MyAppName");
        mCpIdET.setText(Contants.CODIGO_CPID);

        if (PublicSharedPreferences.getDefaults(Contants.CPID_PREFERENCE, getContext()) != null) {
            mCpIdET.setText(PublicSharedPreferences.getDefaults(Contants.CPID_PREFERENCE, getContext()));
        }

        if (PublicSharedPreferences.getDefaults(Contants.APP_NAME_PREFERENCE, getContext()) != null) {
            mAppNameET.setText(PublicSharedPreferences.getDefaults(Contants.APP_NAME_PREFERENCE, getContext()));
        }


        Button webViewBtn = (Button) v.findViewById(R.id.btn_webview);
        webViewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TSMobileAnalytics.getInstance() != null) {
                    mListener.sendPageNumber(2);
                } else {
                    Toast.makeText(getContext(), "Framework must be initialized", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button nativeViewBtn = v.findViewById(R.id.btn_native);
        nativeViewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TSMobileAnalytics.getInstance() != null) {
                    mListener.sendPageNumber(1);
                } else {
                    Toast.makeText(getContext(), "Framework must be initialized", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button destroyButton = v.findViewById(R.id.destroy_button);
        destroyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TSMobileAnalytics.destroyFramework();
                initButton.getBackground().clearColorFilter();
            }
        });


        panelistOnly.setOnCheckedChangeListener(this);
        useHttps.setOnCheckedChangeListener(this);
        logEnabled.setOnCheckedChangeListener(this);

        initButton = v.findViewById(R.id.initialize_button);
        initButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (mCpIdET.getText().length() > 0 && mAppNameET.getText().length() > 0 && mAppNameET.getText().length() > 0) {
                    if (mCpIdET.getText().length() <= 6 || mCpIdET.getText().length() == 32) {
                        TSMobileAnalytics.createInstance(new TSMobileAnalytics.Builder(getActivity().getApplicationContext())
                                .setCpId(mCpIdET.getText().toString())
                                .setApplicationName(mAppNameET.getText().toString())
                                .panelistTrackingOnly(panelistOnly.isChecked())
                                .setLogPrintsActivated(logEnabled.isChecked())
                                .useHttps(useHttps.isChecked())
                                .build());

                        PublicSharedPreferences.setDefaults(Contants.CPID_PREFERENCE, mCpIdET.getText().toString(), getContext());
                        PublicSharedPreferences.setDefaults(Contants.APP_NAME_PREFERENCE, mAppNameET.getText().toString(), getContext());

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
                        Toast.makeText(getContext(), "cpId must be 6 or 32 character", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getContext(), "cpId or application name can not be empty", Toast.LENGTH_SHORT).show();
                }

            }
        });

        if (TSMobileAnalytics.getInstance() != null) {
            initButton.getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
        }

        return v;
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        initButton.getBackground().clearColorFilter();
        TSMobileAnalytics.destroyFramework();
    }
}
