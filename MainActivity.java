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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private View splashOverlay;
    private ImageView splashLogo;
    private LinearLayout workerListContainer;
    private EditText etWorkerName, etWorkerPhone, etWorkerSkill, etWorkerWage;
    private Button btnAddWorker;

    private static class Worker {
        String name;
        String skill;
        String phone;
        int dailyWage;

        Worker(String name, String skill, String phone, int dailyWage) {
            this.name = name;
            this.skill = skill;
            this.phone = phone;
            this.dailyWage = dailyWage;
        }
    }

    private List<Worker> workers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        loadInitialKottayiWorkers();
        renderWorkerCards(workers);
        setupListeners();

        // Traditional Kerala Audio Tone & Splash Dismiss
        playKeralaTraditionalTone();
        handleSplashDismiss();
    }

    private void initViews() {
        splashOverlay = findViewById(R.id.splashOverlay);
        splashLogo = findViewById(R.id.splashLogo);
        workerListContainer = findViewById(R.id.workerListContainer);

        etWorkerName = findViewById(R.id.etWorkerName);
        etWorkerPhone = findViewById(R.id.etWorkerPhone);
        etWorkerSkill = findViewById(R.id.etWorkerSkill);
        etWorkerWage = findViewById(R.id.etWorkerWage);
        btnAddWorker = findViewById(R.id.btnAddWorker);
    }

    private void loadInitialKottayiWorkers() {
        workers.add(new Worker("രാജൻ കെ. (Rajan)", "തെങ്ങ് കയറ്റം (Coconut Climber)", "9876543210", 600));
        workers.add(new Worker("മണികണ്ഠൻ (Mani)", "ഡ്രൈവർ (Car/Auto Driver)", "9876543211", 750));
        workers.add(new Worker("സുരേഷ് ബാബു (Suresh)", "ഇലക്ട്രീഷ്യൻ (Electrician)", "9876543212", 800));
    }

    private void renderWorkerCards(List<Worker> listToRender) {
        workerListContainer.removeAllViews();

        for (Worker worker : listToRender) {
            CardView card = new CardView(this);
            card.setRadius(18f);
            card.setCardElevation(4f);
            card.setUseCompatPadding(true);

            LinearLayout itemLayout = new LinearLayout(this);
            itemLayout.setOrientation(LinearLayout.VERTICAL);
            itemLayout.setPadding(24, 20, 24, 20);

            TextView tvName = new TextView(this);
            tvName.setText(worker.name);
            tvName.setTextSize(16f);
            tvName.setTextColor(Color.parseColor("#1B5E20"));
            tvName.setTypeface(null, android.graphics.Typeface.BOLD);

            TextView tvSkill = new TextView(this);
            tvSkill.setText("തൊഴിൽ: " + worker.skill);
            tvSkill.setTextSize(13f);
            tvSkill.setTextColor(Color.DKGRAY);

            TextView tvWage = new TextView(this);
            tvWage.setText("കൂലി: ₹ " + worker.dailyWage + " / ദിവസം");
            tvWage.setTextSize(14f);
            tvWage.setTextColor(Color.parseColor("#E65100"));
            tvWage.setTypeface(null, android.graphics.Typeface.BOLD);

            LinearLayout actionRow = new LinearLayout(this);
            actionRow.setOrientation(LinearLayout.HORIZONTAL);
            actionRow.setWeightSum(2);
            actionRow.setPadding(0, 14, 0, 0);

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
            btnPay.setBackgroundColor(Color.parseColor("#F57C00"));
            btnPay.setTextColor(Color.WHITE);
            LinearLayout.LayoutParams p2 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            btnPay.setLayoutParams(p2);
            btnPay.setOnClickListener(v -> initiateUpiPayment(worker.name, worker.dailyWage));

            actionRow.addView(btnCall);
            actionRow.addView(btnPay);

            itemLayout.addView(tvName);
            itemLayout.addView(tvSkill);
            itemLayout.addView(tvWage);
            itemLayout.addView(actionRow);

            card.addView(itemLayout);
            workerListContainer.addView(card);
        }
    }

    private void initiateUpiPayment(String name, int amount) {
        Uri uri = Uri.parse("upi://pay").buildUpon()
                .appendQueryParameter("pa", "kottayiworker@upi")
                .appendQueryParameter("pn", name)
                .appendQueryParameter("tn", "Kottayi Coolie Payment")
                .appendQueryParameter("am", String.valueOf(amount))
                .appendQueryParameter("cu", "INR")
                .build();

        Intent upiIntent = new Intent(Intent.ACTION_VIEW, uri);
        Intent chooser = Intent.createChooser(upiIntent, "Pay with UPI (GPay / PhonePe / Paytm)");
        try {
            startActivity(chooser);
        } catch (Exception e) {
            Toast.makeText(this, "ഫോണിൽ UPI ആപ്പുകൾ കണ്ടെത്തിയില്ല", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupListeners() {
        btnAddWorker.setOnClickListener(v -> {
            String name = etWorkerName.getText().toString().trim();
            String phone = etWorkerPhone.getText().toString().trim();
            String skill = etWorkerSkill.getText().toString().trim();
            String wageStr = etWorkerWage.getText().toString().trim();

            if (name.isEmpty() || phone.isEmpty() || skill.isEmpty() || wageStr.isEmpty()) {
                Toast.makeText(this, "ദയവായി എല്ലാ വിവരങ്ങളും പൂരിപ്പിക്കുക", Toast.LENGTH_SHORT).show();
                return;
            }

            int wage = Integer.parseInt(wageStr);
            workers.add(0, new Worker(name, skill, phone, wage));
            renderWorkerCards(workers);

            etWorkerName.setText("");
            etWorkerPhone.setText("");
            etWorkerSkill.setText("");
            etWorkerWage.setText("");

            Toast.makeText(this, "തൊഴിലാളിയെ വിജയകരമായി ചേർത്തു!", Toast.LENGTH_LONG).show();
        });

        findViewById(R.id.btnCategoryClimber).setOnClickListener(v -> filterWorkers("തെങ്ങ്"));
        findViewById(R.id.btnCategoryDriver).setOnClickListener(v -> filterWorkers("ഡ്രൈവർ"));
        findViewById(R.id.btnCategoryElectrician).setOnClickListener(v -> filterWorkers("ഇലക്ട്രീഷ്യൻ"));
    }

    private void filterWorkers(String query) {
        List<Worker> filtered = new ArrayList<>();
        for (Worker w : workers) {
            if (w.skill.contains(query)) {
                filtered.add(w);
            }
        }
        renderWorkerCards(filtered.isEmpty() ? workers : filtered);
    }

    // Kerala Traditional Musical Beat / Chenda Rhythm
    private void playKeralaTraditionalTone() {
        new Thread(() -> {
            try {
                int sampleRate = 44100;
                int durationMs = 1500;
                int count = (sampleRate * durationMs) / 1000;
                short[] samples = new short[count];

                for (int i = 0; i < count; i++) {
                    double progress = (double) i / count;
                    // Traditional acoustic frequency modulations
                    double baseFreq = 140.0;
                    double rhythmicBounce = Math.sin(2.0 * Math.PI * 6.0 * progress);
                    double freq = baseFreq + (rhythmicBounce * 30.0);

                    double angle = 2.0 * Math.PI * i / (sampleRate / freq);
                    double harmonic = 0.4 * Math.sin(angle * 2.0);
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

    private void handleSplashDismiss() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (splashOverlay != null) {
                splashOverlay.animate()
                        .alpha(0.0f)
                        .setDuration(500)
                        .withEndAction(() -> splashOverlay.setVisibility(View.GONE))
                        .start();
            }
        }, 1800);
    }
}
