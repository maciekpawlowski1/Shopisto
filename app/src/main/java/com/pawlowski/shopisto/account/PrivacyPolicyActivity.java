package com.pawlowski.shopisto.account;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.widget.TextView;

import com.pawlowski.shopisto.R;

public class PrivacyPolicyActivity extends AppCompatActivity {

    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);

        textView = findViewById(R.id.text_view_privacy_activity);

        String text = "<html>\n" +
                "<body>\n" +
                "<h2>Privacy Policy</h2>\n" +
                "<p>Maciej Pawłowski built the Shopisto app as a ad-supported app. This SERVICE is provided by Maciej Pawłowski at no cost and is intended\n" +
                "    for use as is.</p>\n" +
                "<p>This page is used to inform website visitors regarding my policies with the collection, use, and\n" +
                "    disclosure of Personal Information if anyone decided to use my Service.</p>\n" +
                "<p>If you choose to use my Service, then you agree to the collection and use of information in\n" +
                "    relation with this policy. The Personal Information that I collect are used for providing and\n" +
                "    improving the Service. I will not use or share your information with anyone except as described\n" +
                "    in this Privacy Policy.</p>\n" +
                "<p>The terms used in this Privacy Policy have the same meanings as in our Terms and Conditions,\n" +
                "    which is accessible at Shopisto, unless otherwise defined in this Privacy Policy.</p>\n" +
                "\n" +
                "<p><strong>Information Collection and Use</strong></p>\n" +
                "<p>For a better experience while using our Service, I may require you to provide us with certain\n" +
                "    personally identifiable information, including but not limited to e-mail adresses. \n" +
                "\tThe information that I request is will be retained by us and used as described in this privacy policy.</p>\n" +
                "<p>The app does use third party services that may collect information used to identify you: Google Firebase Authentication, Google Firebase Realtime Database.\n" +
                "\n" +
                "<p><strong>Log Data</strong></p>\n" +
                "<p>I want to inform you that whenever you use my Service, in case of an error in the app I collect\n" +
                "    data and information (through third party products) on your phone called Log Data. This Log Data\n" +
                "    may include information such as your devices’s Internet Protocol (“IP”) address, device name,\n" +
                "    operating system version, configuration of the app when utilising my Service, the time and date\n" +
                "    of your use of the Service, and other statistics.</p>\n" +
                "\n" +
                "<p><strong>Cookies</strong></p>\n" +
                "<p>Cookies are files with small amount of data that is commonly used an anonymous unique identifier.\n" +
                "    These are sent to your browser from the website that you visit and are stored on your devices’s\n" +
                "    internal memory.</p>\n" +
                "<p>This Services does not uses these “cookies” explicitly. However, the app may use third party code\n" +
                "    and libraries that use “cookies” to collection information and to improve their services. You\n" +
                "    have the option to either accept or refuse these cookies, and know when a cookie is being sent\n" +
                "    to your device. If you choose to refuse our cookies, you may not be able to use some portions of\n" +
                "    this Service.</p>\n" +
                "\n" +
                "<p><strong>Service Providers</strong></p>\n" +
                "<p>I may employ third-party companies and individuals due to the following reasons:</p>\n" +
                "<ul>\n" +
                "    <li>To facilitate our Service;</li>\n" +
                "    <li>To provide the Service on our behalf;</li>\n" +
                "    <li>To perform Service-related services; or</li>\n" +
                "    <li>To assist us in analyzing how our Service is used.</li>\n" +
                "</ul>\n" +
                "<p>I want to inform users of this Service that these third parties have access to your Personal\n" +
                "    Information. The reason is to perform the tasks assigned to them on our behalf. However, they\n" +
                "    are obligated not to disclose or use the information for any other purpose.</p>\n" +
                "\n" +
                "<p><strong>Security</strong></p>\n" +
                "<p>I value your trust in providing us your Personal Information, thus we are striving to use\n" +
                "    commercially acceptable means of protecting it. But remember that no method of transmission over\n" +
                "    the internet, or method of electronic storage is 100% secure and reliable, and I cannot\n" +
                "    guarantee its absolute security.</p>\n" +
                "\n" +
                "<p><strong>Links to Other Sites</strong></p>\n" +
                "<p>This Service may contain links to other sites. If you click on a third-party link, you will be\n" +
                "    directed to that site. Note that these external sites are not operated by me. Therefore, I\n" +
                "    strongly advise you to review the Privacy Policy of these websites. I have no control over, and\n" +
                "    assume no responsibility for the content, privacy policies, or practices of any third-party\n" +
                "    sites or services.</p>\n" +
                "\n" +
                "<p><strong>Children’s Privacy</strong></p>\n" +
                "<p>This Services do not address anyone under the age of 13. I do not knowingly collect personal\n" +
                "    identifiable information from children under 13. In the case I discover that a child under 13\n" +
                "    has provided me with personal information, I immediately delete this from our servers. If you\n" +
                "    are a parent or guardian and you are aware that your child has provided us with personal\n" +
                "    information, please contact me so that I will be able to do necessary actions.</p>\n" +
                "\n" +
                "<p><strong>Changes to This Privacy Policy</strong></p>\n" +
                "<p>I may update our Privacy Policy from time to time. Thus, you are advised to review this page\n" +
                "    periodically for any changes. I will notify you of any changes by posting the new Privacy Policy\n" +
                "    on this page. These changes are effective immediately, after they are posted on this page.</p>\n" +
                "\n" +
                "<p><strong>Contact Us</strong></p>\n" +
                "<p>If you have any questions or suggestions about my Privacy Policy, do not hesitate to contact\n" +
                "    me.</p>\n" +
                "<p>This Privacy Policy page was created at <a href=\"https://privacypolicytemplate.net\"\n" +
                "                                              target=\"_blank\">privacypolicytemplate.net</a>.</p>\n" +
                "</body>\n" +
                "</html>";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            textView.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT));
        } else {
            textView.setText(Html.fromHtml(text));
        };



    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home)
        {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}