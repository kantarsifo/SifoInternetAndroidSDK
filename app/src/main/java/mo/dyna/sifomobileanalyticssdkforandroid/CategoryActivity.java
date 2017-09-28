package mo.dyna.sifomobileanalyticssdkforandroid;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import se.sifo.analytics.mobileapptagging.android.MobileTaggingFramework;

/**
 * Created by Peter on 2015-04-17.
 */
public class CategoryActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        ListView listView = (ListView) findViewById(android.R.id.list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                sendTag((String) parent.getItemAtPosition(position));
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    private void sendTag(String category) {
        if (MobileTaggingFramework.getInstance() != null) {
            ApplicationImpl.tagInfo().setCategory(1, category);
            MobileTaggingFramework.getInstance().sendTag(ApplicationImpl.tagInfo().getCategories(), "", "");
            Toast.makeText(this, getString(R.string.toast_sent_tag, category), Toast.LENGTH_SHORT).show();

            startActivity(new Intent(this, ContentActivity.class));
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
        }
    }


}
