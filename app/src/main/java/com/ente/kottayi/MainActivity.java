package com.ente.kottayi;

import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private View splashOverlay, viewSearch, viewRegister;
    private LinearLayout containerWorkersList;
    private TextView tvHeaderSubtitle;
    private Button navSearch, navRegister, btnSaveProfile;
    private Spinner spinnerFilterJob, spinnerWorkerJob;
    private EditText etName, etPhone, etArea, etWage, etUpiId, etCustomJob;

    public static class WorkerProfile {
        String name, phone, job, area, upiId;
        int wage;

        public WorkerProfile(String name, String phone, String job, String area, int wage, String upiId) {
            this.name = name;
            this.phone = phone;
            this.job = job;
            this.area = area;
            this.wage = wage;
            this.upiId = upiId;
        }
    }

    private List<WorkerProfile> workerList = new ArrayList<>();

    // കോട്ടായിയിലെ സമഗ്രമായ എല്ലാ പണികളുടെയും ലിസ്റ്റ്
    private final String[] jobCategories = {
            "എല്ലാ തൊഴിലും (All Works)",
            "🌴 തെങ്ങ് കയറ്റം (Coconut Climber)",
            "🚗 ഡ്രൈവർ (Car / Auto / Goods / Tractor)",
            "💡 ഇലക്ട്രീഷ്യൻ (Electrician)",
            "🔧 പ്ലംബർ (Plumber)",
            "🎨 പെയിന്റിംഗ് (Painter)",
            "🧱 മേസ്തിരി / കോൺക്രീറ്റ് (Mason / Civil)",
            "🌾 കാർഷിക കൂലിപ്പണി (Agricultural / Paddy Field)",
            "🪵 ആശാരി / തടിപ്പണി (Carpenter)",
            "🪓 മരം വെട്ട് / വാഴ വെട്ട് (Tree Cutting)",
            "🌿 പുല്ലുവെട്ട് & തോട്ടപ്പണി (Grass Cutting / Gardening)",
            "🧹 വീട്ടുജോലി / ക്ലീനിംഗ് (Domestic Help / Cleaning)",
            "🕳️ കിണർ പണി / ശുചീകരണം (Well Digging & Cleaning)",
            "🍳 പാചകം / കാറ്ററിംഗ് സഹായി (Catering / Cook)",
            "⚡ വെൽഡിങ് & ഗ്രിൽ വർക്ക് (Welder / Iron Work)",
            "📦 കൂലിപ്പണി / ചുമട്ടുതൊഴിലാളി (Daily Wage Loader / Porter)",
            "✨ മറ്റു തൊഴിലുകൾ (Other Works)"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupJobDropdowns();
        setupBottomNavigation();
        playKeralaTone();
        dismissSplash();
    }

    private void initViews() {
        splashOverlay = findViewById(R.id.splashOverlay);
        viewSearch = findViewById(R.id.viewSearch);
        viewRegister = findViewById(R.id.viewRegister);
        containerWorkersList = findViewById(R.id.containerWorkersList);
        tvHeaderSubtitle = findViewById(R.id.tvHeaderSubtitle);

        navSearch = findViewById(R.id.navSearch);
        navRegister = findViewById(R.id.navRegister);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        spinnerFilterJob = findViewById(R.id.spinnerFilterJob);
        spinnerWorkerJob = findViewById(R.id.spinnerWorkerJob);

        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etArea = findViewById(R.id.etArea);
        etWage = findViewById(R.id.etWage);
        etUpiId = findViewById(R.id.etUpiId);
        etCustomJob = findViewById(R.id.etCustomJob);
    }

    private void setupJobDropdowns() {
        // Customer Filter Dropdown
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, jobCategories);
        spinnerFilterJob.setAdapter(filterAdapter);

        // Worker Register Dropdown
        String[] registerList = new String[jobCategories.length - 1];
        System.arraycopy(jobCategories, 1, registerList, 0, jobCategories.length - 1);
        ArrayAdapter<String> regAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, registerList);
        spinnerWorkerJob.setAdapter(regAdapter);

        spinnerWorkerJob.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (registerList[position].contains("മറ്റു തൊഴിലുകൾ")) {
                    etCustomJob.setVisibility(View.VISIBLE);
                } else {
                    etCustomJob.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerFilterJob.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                renderWorkerCards(jobCategories[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnSaveProfile.setOnClickListener(v -> saveWorkerData());
    }

    private void setupBottomNavigation() {
        navSearch.setOnClickListener(v -> {
            viewSearch.setVisibility(View.VISIBLE);
            viewRegister.setVisibility(View.GONE);
            navSearch.setTextColor(Color.parseColor("#1B5E20"));
            navRegister.setTextColor(Color.parseColor("#757575"));
            tvHeaderSubtitle.setText("കോട്ടായിയിലെ തൊഴിലാളികളെ തിരയുക");
            renderWorkerCards(spinnerFilterJob.getSelectedItem().toString());
        });

        navRegister.setOnClickListener(v -> {
            viewSearch.setVisibility(View.GONE);
            viewRegister.setVisibility(View.VISIBLE);
            navRegister.setTextColor(Color.parseColor("#1B5E20"));
            navSearch.setTextColor(Color.parseColor("#757575"));
            tvHeaderSubtitle.setText("നിങ്ങളുടെ തൊഴിലും കൂലിയും ചേർക്കൂ");
        });
    }

    private void saveWorkerData() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String area = etArea.getText().toString().trim();
        String wageStr = etWage.getText().toString().trim();
        String upi = etUpiId.getText().toString().trim();

        String selectedJob = spinnerWorkerJob.getSelectedItem().toString();
        if (selectedJob.contains("മറ്റു തൊഴിലുകൾ") && !etCustomJob.getText().toString().trim().isEmpty()) {
            selectedJob = "🛠️ " + etCustomJob.getText().toString().trim();
        }

        if (name.isEmpty() || phone.isEmpty() || area.isEmpty() || wageStr.isEmpty()) {
            Toast.makeText(this, "ദയവായി പേരും ഫോൺ നമ്പറും കൂലിയും നൽകുക", Toast.LENGTH_SHORT).show();
            return;
        }

        int wage = Integer.parseInt(wageStr);
        workerList.add(0, new WorkerProfile(name, phone, selectedJob, area, wage, upi));

        etName.setText("");
        etPhone.setText("");
        etArea.setText("");
        etWage.setText("");
        etUpiId.setText("");
        etCustomJob.setText("");

        Toast.makeText(this, "വിവരങ്ങൾ വിജയകരമായി ചേർത്തു!", Toast.LENGTH_LONG).show();
        navSearch.performClick();
    }

    private void renderWorkerCards(String selectedCategory) {
        containerWorkersList.removeAllViews();

        List<WorkerProfile> filtered = new ArrayList<>();
        for (WorkerProfile profile : workerList) {
            if (selectedCategory.equals(jobCategories[0]) || profile.job.equalsIgnoreCase(selectedCategory)) {
                filtered.add(profile);
            }
        }

        if (filtered.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("ഈ വിഭാഗത്തിൽ നിലവിൽ പണിക്കാർ രജിസ്റ്റർ ചെയ്തിട്ടില്ല.\nതാഴെയുള്ള 'എന്റെ തൊഴിൽ ചേർക്കുക' വഴി ആർക്കും സൗജന്യമായി രജിസ്റ്റർ ചെയ്യാം.");
            tvEmpty.setTextColor(Color.GRAY);
            tvEmpty.setLineSpacing(6f, 1f);
            tvEmpty.setPadding(20, 50, 20, 20);
            tvEmpty.setTextSize(14f);
            containerWorkersList.addView(tvEmpty);
            return;
        }

        for (WorkerProfile worker : filtered) {
            CardView card = new CardView(this);
            card.setRadius(14f);
            card.setCardElevation(3f);
            card.setUseCompatPadding(true);

            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(20, 18, 20, 18);

            TextView tvName = new TextView(this);
            tvName.setText(worker.name + " (" + worker.area + ")");
            tvName.setTextSize(16f);
            tvName.setTextColor(Color.parseColor("#1B5E20"));
            tvName.setTypeface(null, android.graphics.Typeface.BOLD);

            TextView tvJob = new TextView(this);
            tvJob.setText(worker.job);
            tvJob.setTextSize(13f);
            tvJob.setPadding(0, 4, 0, 4);
            tvJob.setTextColor(Color.parseColor("#37474F"));

            TextView tvWage = new TextView(this);
            tvWage.setText("വേതനം: ₹ " + worker.wage);
            tvWage.setTextSize(15f);
            tvWage.setTextColor(Color.parseColor("#C62828"));
            tvWage.setTypeface(null, android.graphics.Typeface.BOLD);

            LinearLayout btnRow = new LinearLayout(this);
            btnRow.setOrientation(LinearLayout.HORIZONTAL);
            btnRow.setWeightSum(2);
            btnRow.setPadding(0, 12, 0, 0);

            Button btnCall = new Button(this);
            btnCall.setText("വിളിക്കുക 📞");
            btnCall.setBackgroundColor(Color.parseColor("#2E7D32"));
            btnCall.setTextColor(Color.WHITE);
            LinearLayout.LayoutParams p1 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            p1.rightMargin = 6;
            btnCall.setLayoutParams(p1);
            btnCall.setOnClickListener(v -> {
                Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + worker.phone));
                startActivity(callIntent);
            });

            Button btnPay = new Button(this);
            btnPay.setText("കൂലി നൽകുക (UPI) ₹");
            btnPay.setBackgroundColor(Color.parseColor("#EF6C00"));
            btnPay.setTextColor(Color.WHITE);
            LinearLayout.LayoutParams p2 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            btnPay.setLayoutParams(p2);
            btnPay.setOnClickListener(v -> {
                String targetUpi = worker.upiId.isEmpty() ? "kottayiworker@upi" : worker.upiId;
                Uri upiUri = Uri.parse("upi://pay").buildUpon()
                        .appendQueryParameter("pa", targetUpi)
                        .appendQueryParameter("pn", worker.name)
                        .appendQueryParameter("tn", "Kottayi Coolie Service")
                        .appendQueryParameter("am", String.valueOf(worker.wage))
                        .appendQueryParameter("cu", "INR")
                        .build();
                try {
                    startActivity(Intent.createChooser(new Intent(Intent.ACTION_VIEW, upiUri), "UPI വഴി കൂലി നൽകുക"));
                } catch (Exception e) {
                    Toast.makeText(this, "ഫോണിൽ UPI ആപ്പുകൾ കണ്ടെത്തിയില്ല", Toast.LENGTH_SHORT).show();
                }
            });

            btnRow.addView(btnCall);
            btnRow.addView(btnPay);

            layout.addView(tvName);
            layout.addView(tvJob);
            layout.addView(tvWage);
            layout.addView(btnRow);

            card.addView(layout);
            containerWorkersList.addView(card);
        }
    }

    private void playKeralaTone() {
        new Thread(() -> {
            try {
                int sampleRate = 44100;
                int durationMs = 1400;
                int count = (sampleRate * durationMs) / 1000;
                short[] samples = new short[count];

                for (int i = 0; i < count; i++) {
                    double progress = (double) i / count;
                    double freq = 145.0 + (Math.sin(2.0 * Math.PI * 6.0 * progress) * 28.0);
                    double angle = 2.0 * Math.PI * i / (sampleRate / freq);
                    double harmonic = 0.35 * Math.sin(angle * 2.0);
                    double envelope = Math.sin(Math.PI * progress);
                    samples[i] = (short) ((Math.sin(angle) + harmonic) * envelope * 24000);
                }

                AudioTrack track = new AudioTrack.Builder()
                        .setAudioAttributes(new AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build())
                        .setAudioFormat(new AudioFormat.Builder()
                                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                .setSampleRate(sampleRate)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                .build())
                        .setBufferSizeInBytes(samples.length * 2)
                        .setTransferMode(AudioTrack.MODE_STATIC)
                        .build();

                track.write(samples, 0, samples.length);
                track.play();
            } catch (Exception ignored) {}
        }).start();
    }

    private void dismissSplash() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (splashOverlay != null) {
                splashOverlay.animate()
                        .alpha(0.0f)
                        .setDuration(500)
                        .withEndAction(() -> splashOverlay.setVisibility(View.GONE))
                        .start();
            }
        }, 1600);
    }
}
