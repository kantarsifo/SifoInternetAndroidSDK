package se.kantarsifo.mobileanalytics.sampleapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import se.kantarsifo.mobileanalytics.framework.TSMobileAnalytics;

public class ContentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        ListView listView = (ListView) findViewById(android.R.id.list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                sendTag((String) parent.getItemAtPosition(position), "", "");
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

    /**
     * Use this to send tag: TSMobileAnalytics.getInstance().sendTag(categoryName, contentId, contentName);
     * @param content (category)    The current category or name of the page that the user is browsing.
     *                              This value tells us what the user is doing right now.
     *                              If he is reading an article about sports in your application, the value might be “sports”.
     *                              If he is browsing a set of football scores, the value might be “sports/scores/football”.
     *                              max 255 character
     * @param contentId             The value of the current article, resource or content within the category that is being browsed.
     *                              If the current category does not provide different content, this value is not needed.
     *                              For example, if the user is browsing a news article in your news application,
     *                              the value should correspond to the identifier of the article used on your content server.
     *                              If the user is reading article 123456 of an online newspaper,
     *                              the value of category could be “News/Article” and the value of Content ID would then be “123456”.
     *                              This information will be included in the attribute called “id” in the tag.
     *                              max 255 character
     * @param contentName           The name of the current article, resource or content within the category that is being browsed.
     *                              max 255 character
     */
    private void sendTag(String content, String contentId, String contentName) {
        if (TSMobileAnalytics.getInstance() != null) {
            ApplicationImpl.tagInfo().setContentId(content);
            TSMobileAnalytics.getInstance().sendTag(
                    ApplicationImpl.tagInfo().getCategories(),
                    "",
                    ApplicationImpl.tagInfo().getContentId());
            Toast.makeText(this, getString(R.string.toast_sent_tag, content), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, NameActivity.class));
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
        }
    }
}
