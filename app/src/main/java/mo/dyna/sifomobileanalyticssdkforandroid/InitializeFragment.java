package mo.dyna.sifomobileanalyticssdkforandroid;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

import se.sifo.analytics.mobileapptagging.android.MobileTaggingFramework;



/**
 * Created by ahmetcengiz on 26/09/2017.
 */

public class InitializeFragment extends Fragment implements CompoundButton.OnCheckedChangeListener{
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
        MobileTaggingFramework.destroyFramework();
        mAppNameET = v.findViewById(R.id.appNameET);
        mCpIdET = v.findViewById(R.id.cpIdET);
        panelistOnly = v.findViewById(R.id.panelistOnly);
        useHttps = v.findViewById(R.id.useHttps);
        logEnabled = v.findViewById(R.id.logEnabled);

        mAppNameET.setText("MyAppName");
        mCpIdET.setText(Contants.CODIGO_CPID);

        Button webViewBtn = (Button) v.findViewById(R.id.btn_webview);
        webViewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MobileTaggingFramework.getInstance() != null) {
                    mListener.sendPageNumber(2);
                } else {
                    Toast.makeText(getContext(), "please initialize framework", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button nativeViewBtn = v.findViewById(R.id.btn_native);
        nativeViewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MobileTaggingFramework.getInstance() != null) {
                    mListener.sendPageNumber(1);
                } else {
                    Toast.makeText(getContext(), "please initialize framework", Toast.LENGTH_SHORT).show();
                }
            }
        });


        panelistOnly.setOnCheckedChangeListener(this);
        useHttps.setOnCheckedChangeListener(this);
        logEnabled.setOnCheckedChangeListener(this);

        initButton = v.findViewById(R.id.initialize_button);
        initButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCpIdET.getText().length() > 0 && mAppNameET.getText().length() > 0)
                    MobileTaggingFramework.createInstance(new MobileTaggingFramework.Builder(getActivity().getApplicationContext())
                            .setCpId(mCpIdET.getText().toString())
                            .setApplicationName(mAppNameET.getText().toString())
                            .panelistTrackingOnly(panelistOnly.isChecked())
                            .setLogPrintsActivated(logEnabled.isChecked())
                            .useHttps(useHttps.isChecked())
                            .build());

                view.getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);

            }
        });

        if(MobileTaggingFramework.getInstance() != null){
            initButton.getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
        }

        return v;
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        initButton.getBackground().clearColorFilter();
        MobileTaggingFramework.destroyFramework();

    }
}
