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

    private View splashOverlay;
    private LinearLayout layoutRoleSelection, layoutCustomerDashboard, containerCustomerWorkers;
    private View layoutWorkerDashboard;
    private TextView tvRoleSubtitle;

    private EditText etName, etPhone, etArea, etWage, etUpiId;
    private Spinner spinnerWorkerJob, spinnerFilterJob;
    private Button btnRegisterWorker, btnBackFromCustomer, btnBackFromWorker;

    public static class WorkerProfile {
        String name;
        String phone;
        String job;
        String area;
        int wage;
        String upiId;

        public WorkerProfile(String name, String phone, String job, String area, int wage, String upiId) {
            this.name = name;
            this.phone = phone;
            this.job = job;
            this.area = area;
            this.wage = wage;
            this.upiId = upiId;
        }
    }

    // No hardcoded contacts. Only workers who self-register will appear.
    private List<WorkerProfile> registeredWorkers = new ArrayList<>();

    private final String[] allJobs = {
            "എല്ലാ തൊഴിലും (All Jobs)",
            "തെങ്ങ് കയറ്റം (Coconut Climber)",
            "ഡ്രൈവർ (Driver - Auto/Car/Jeep)",
            "ഇലക്ട്രീഷ്യൻ (Electrician)",
            "പ്ലംബർ (Plumber)",
            "പെയിന്റിംഗ് (Painter)",
            "മേസ്തിരി / നിർമ്മാണം (Mason/Civil)",
            "കൃഷിപ്പണി / കാർഷിക കൂലി (Farm Labor)",
            "തടിപ്പണി (Carpenter)",
            "വീട്ടുജോലി / ശുചീകരണം (Domestic Help)",
            "മരപ്പണി & വെട്ട് (Tree Cutting)",
            "വെൽഡിങ് (Welder)"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupSpinners();
        setupRoleRouting();
        playKeralaTone();
        dismissSplash();
    }

    private void initViews() {
        splashOverlay = findViewById(R.id.splashOverlay);
        layoutRoleSelection = findViewById(R.id.layoutRoleSelection);
        layoutCustomerDashboard = findViewById(R.id.layoutCustomerDashboard);
        layoutWorkerDashboard = findViewById(R.id.layoutWorkerDashboard);
        containerCustomerWorkers = findViewById(R.id.containerCustomerWorkers);
        tvRoleSubtitle = findViewById(R.id.tvRoleSubtitle);

        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etArea = findViewById(R.id.etArea);
        etWage = findViewById(R.id.etWage);
        etUpiId = findViewById(R.id.etUpiId);

        spinnerWorkerJob = findViewById(R.id.spinnerWorkerJob);
        spinnerFilterJob = findViewById(R.id.spinnerFilterJob);

        btnRegisterWorker = findViewById(R.id.btnRegisterWorker);
        btnBackFromCustomer = findViewById(R.id.btnBackFromCustomer);
        btnBackFromWorker = findViewById(R.id.btnBackFromWorker);
    }

    private void setupSpinners() {
        // Dropdown for Customer Filter
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, allJobs);
        spinnerFilterJob.setAdapter(filterAdapter);

        // Dropdown for Worker Registration (skip "All Jobs")
        String[] workerJobs = new String[allJobs.length - 1];
        System.arraycopy(allJobs, 1, workerJobs, 0, allJobs.length - 1);
        ArrayAdapter<String> registerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, workerJobs);
        spinnerWorkerJob.setAdapter(registerAdapter);

        spinnerFilterJob.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                renderCustomerWorkerList(allJobs[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupRoleRouting() {
        findViewById(R.id.cardSelectCustomer).setOnClickListener(v -> {
            layoutRoleSelection.setVisibility(View.GONE);
            layoutCustomerDashboard.setVisibility(View.VISIBLE);
            layoutWorkerDashboard.setVisibility(View.GONE);
            tvRoleSubtitle.setText("സേവനം ആവശ്യക്കാർക്ക് (Customer Portal)");
            renderCustomerWorkerList(spinnerFilterJob.getSelectedItem().toString());
        });

        findViewById(R.id.cardSelectWorker).setOnClickListener(v -> {
            layoutRoleSelection.setVisibility(View.GONE);
            layoutCustomerDashboard.setVisibility(View.GONE);
            layoutWorkerDashboard.setVisibility(View.VISIBLE);
            tvRoleSubtitle.setText("തൊഴിലാളി രജിസ്ട്രേഷൻ &amp; ഡാഷ്‌ബോർഡ്");
        });

        btnBackFromCustomer.setOnClickListener(v -> showRoleSelection());
        btnBackFromWorker.setOnClickListener(v -> showRoleSelection());

        btnRegisterWorker.setOnClickListener(v -> handleWorkerRegistration());
    }

    private void showRoleSelection() {
        layoutRoleSelection.setVisibility(View.VISIBLE);
        layoutCustomerDashboard.setVisibility(View.GONE);
        layoutWorkerDashboard.setVisibility(View.GONE);
        tvRoleSubtitle.setText("കോട്ടായി ഗ്രാമപഞ്ചായത്ത് സർവീസ് നെറ്റ്‌വർക്ക്");
    }

    private void handleWorkerRegistration() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String area = etArea.getText().toString().trim();
        String wageStr = etWage.getText().toString().trim();
        String upi = etUpiId.getText().toString().trim();
        String selectedJob = spinnerWorkerJob.getSelectedItem().toString();

        if (name.isEmpty() || phone.isEmpty() || area.isEmpty() || wageStr.isEmpty()) {
            Toast.makeText(this, "ദയവായി പ്രധാന വിവരങ്ങൾ പൂരിപ്പിക്കുക", Toast.LENGTH_SHORT).show();
            return;
        }

        int wage = Integer.parseInt(wageStr);
        registeredWorkers.add(0, new WorkerProfile(name, phone, selectedJob, area, wage, upi));

        etName.setText("");
        etPhone.setText("");
        etArea.setText("");
        etWage.setText("");
        etUpiId.setText("");

        Toast.makeText(this, "നിങ്ങളുടെ വിവരങ്ങൾ വിജയകരമായി ചേർത്തു!", Toast.LENGTH_LONG).show();
        showRoleSelection();
    }

    private void renderCustomerWorkerList(String filter) {
        containerCustomerWorkers.removeAllViews();

        List<WorkerProfile> matched = new ArrayList<>();
        for (WorkerProfile w : registeredWorkers) {
            if (filter.equals(allJobs[0]) || w.job.equalsIgnoreCase(filter)) {
                matched.add(w);
            }
        }

        if (matched.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("ഈ വിഭാഗത്തിൽ നിലവിൽ രജിസ്റ്റർ ചെയ്ത തൊഴിലാളികൾ ആരും ഇല്ല. തൊഴിലാളികൾ സ്വയം രജിസ്റ്റർ ചെയ്യുമ്പോൾ ഇവിടെ കാണാം.");
            tvEmpty.setTextColor(Color.GRAY);
            tvEmpty.setPadding(10, 40, 10, 10);
            tvEmpty.setTextSize(14f);
            containerCustomerWorkers.addView(tvEmpty);
            return;
        }

        for (WorkerProfile worker : matched) {
            CardView card = new CardView(this);
            card.setRadius(16f);
            card.setCardElevation(4f);
            card.setUseCompatPadding(true);

            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(24, 20, 24, 20);

            TextView tvName = new TextView(this);
            tvName.setText(worker.name + " (" + worker.area + ")");
            tvName.setTextSize(16f);
            tvName.setTextColor(Color.parseColor("#1B5E20"));
            tvName.setTypeface(null, android.graphics.Typeface.BOLD);

            TextView tvJob = new TextView(this);
            tvJob.setText("തൊഴിൽ: " + worker.job);
            tvJob.setTextSize(13f);
            tvJob.setTextColor(Color.DKGRAY);

            TextView tvWage = new TextView(this);
            tvWage.setText("കൂലി നിരക്ക്: ₹ " + worker.wage);
            tvWage.setTextSize(14f);
            tvWage.setTextColor(Color.parseColor("#D84315"));
            tvWage.setTypeface(null, android.graphics.Typeface.BOLD);

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setWeightSum(2);
            row.setPadding(0, 14, 0, 0);

            Button btnCall = new Button(this);
            btnCall.setText("വിളിക്കുക 📞");
            btnCall.setBackgroundColor(Color.parseColor("#2E7D32"));
            btnCall.setTextColor(Color.WHITE);
            LinearLayout.LayoutParams p1 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            p1.rightMargin = 8;
            btnCall.setLayoutParams(p1);
            btnCall.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + worker.phone));
                startActivity(intent);
            });

            Button btnPay = new Button(this);
            btnPay.setText("കൂലി നൽകുക (UPI) ₹");
            btnPay.setBackgroundColor(Color.parseColor("#E65100"));
            btnPay.setTextColor(Color.WHITE);
            LinearLayout.LayoutParams p2 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            btnPay.setLayoutParams(p2);
            btnPay.setOnClickListener(v -> {
                String targetUpi = worker.upiId.isEmpty() ? "kottayiworker@upi" : worker.upiId;
                Uri uri = Uri.parse("upi://pay").buildUpon()
                        .appendQueryParameter("pa", targetUpi)
                        .appendQueryParameter("pn", worker.name)
                        .appendQueryParameter("tn", "Kottayi Coolie Service")
                        .appendQueryParameter("am", String.valueOf(worker.wage))
                        .appendQueryParameter("cu", "INR")
                        .build();
                Intent upiIntent = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    startActivity(Intent.createChooser(upiIntent, "Pay via UPI"));
                } catch (Exception e) {
                    Toast.makeText(this, "UPI ആപ്പുകൾ ലഭ്യമല്ല", Toast.LENGTH_SHORT).show();
                }
            });

            row.addView(btnCall);
            row.addView(btnPay);

            layout.addView(tvName);
            layout.addView(tvJob);
            layout.addView(tvWage);
            layout.addView(row);

            card.addView(layout);
            containerCustomerWorkers.addView(card);
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
