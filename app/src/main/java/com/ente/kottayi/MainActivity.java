package com.ente.kottayi;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private View splashOverlay, layoutAuth, layoutMainApp, viewSearch, viewRegister, viewProfile, weatherEffectView, rootContainer;
    private LinearLayout containerWorkersList, layoutHeader;
    private LinearLayout layoutDriverSpecific, layoutCookSpecific, layoutClimberSpecific, layoutGeneralWage;
    private TextView tvHeaderSubtitle, tvDeveloperCredit, tvWeatherBadge, tvUserEmail;
    private Button btnLogin, btnSignUp, btnLogout, btnDeleteAccount;
    private Button navSearch, navRegister, navProfile, btnSaveProfile;
    private Spinner spinnerFilterJob, spinnerWorkerJob;
    private EditText etAuthEmail, etAuthPassword, etName, etPhone, etArea, etWage, etUpiId;

    // Custom Category Specific Inputs
    private EditText etVehicleType, etRatePerKm, etMinCharge;
    private EditText etFoodItems, etRatePerPlate;
    private EditText etRatePerTree;

    public static class WorkerProfile {
        String id, userId, name, phone, job, area, upiId;
        long wage;
        String extraDetails;

        public WorkerProfile() {}

        public WorkerProfile(String id, String userId, String name, String phone, String job, String area, long wage, String upiId, String extraDetails) {
            this.id = id;
            this.userId = userId;
            this.name = name;
            this.phone = phone;
            this.job = job;
            this.area = area;
            this.wage = wage;
            this.upiId = upiId;
            this.extraDetails = extraDetails;
        }
    }

    private List<WorkerProfile> cloudWorkerList = new ArrayList<>();

    private final String[] jobCategories = {
            "എല്ലാ തൊഴിലും (All Works)",
            "🌴 തെങ്ങ് കയറ്റം (Coconut Climber)",
            "🚗 ഡ്രൈവർ (Driver - Rate per Km)",
            "🍳 പാചകം & കാറ്ററിംഗ് (Cook/Food Items)",
            "💡 ഇലക്ട്രീഷ്യൻ (Electrician)",
            "🔧 പ്ലംബർ (Plumber)",
            "🎨 പെയിന്റിംഗ് (Painter)",
            "🧱 മേസ്തിരി / കോൺക്രീറ്റ് (Mason / Civil)",
            "🌾 കാർഷിക കൂലിപ്പണി (Agricultural / Farm)",
            "🪵 ആശാരി / തടിപ്പണി (Carpenter)",
            "🪓 മരം വെട്ട് / വാഴ വെട്ട് (Tree Cutting)",
            "🌿 പുല്ലുവെട്ട് & തോട്ടപ്പണി (Gardening)",
            "🧹 വീട്ടുജോലി / ശുചീകരണം (Cleaning)",
            "⚡ വെൽഡിങ് & ഗ്രിൽ വർക്ക് (Welder)",
            "📦 ചുമട്ടുതൊഴിലാളി / കൂലിപ്പണി (Porter)"
    };

    // Atmosphere Effects for Glass Background
    private static class Cloud { float x, y, radius, speed; }
    private static class Star { float x, y, radius, alphaSpeed; float alpha; }
    private static class RainDrop { float x, y, length, speed; }
    private List<Cloud> clouds = new ArrayList<>();
    private List<Star> stars = new ArrayList<>();
    private List<RainDrop> rainDrops = new ArrayList<>();
    private Paint cloudPaint = new Paint(), starPaint = new Paint(), moonPaint = new Paint(), rainPaint = new Paint();
    private boolean isNight = false, isRainy = false, hasClouds = true, isAnimating = true;
    private Handler animationHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        initAtmosphere();
        setupAuthListeners();
        setupJobDropdowns();
        setupBottomNavigation();
        playKeralaTone();
        dismissSplash();

        checkUserSession();
        fetchKottayiWeather();
    }

    private void initViews() {
        rootContainer = findViewById(R.id.rootContainer);
        splashOverlay = findViewById(R.id.splashOverlay);
        layoutAuth = findViewById(R.id.layoutAuth);
        layoutMainApp = findViewById(R.id.layoutMainApp);
        viewSearch = findViewById(R.id.viewSearch);
        viewRegister = findViewById(R.id.viewRegister);
        viewProfile = findViewById(R.id.viewProfile);
        weatherEffectView = findViewById(R.id.weatherEffectView);
        containerWorkersList = findViewById(R.id.containerWorkersList);
        layoutHeader = findViewById(R.id.layoutHeader);

        tvHeaderSubtitle = findViewById(R.id.tvHeaderSubtitle);
        tvDeveloperCredit = findViewById(R.id.tvDeveloperCredit);
        tvWeatherBadge = findViewById(R.id.tvWeatherBadge);
        tvUserEmail = findViewById(R.id.tvUserEmail);

        etAuthEmail = findViewById(R.id.etAuthEmail);
        etAuthPassword = findViewById(R.id.etAuthPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnLogout = findViewById(R.id.btnLogout);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);

        navSearch = findViewById(R.id.navSearch);
        navRegister = findViewById(R.id.navRegister);
        navProfile = findViewById(R.id.navProfile);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        spinnerFilterJob = findViewById(R.id.spinnerFilterJob);
        spinnerWorkerJob = findViewById(R.id.spinnerWorkerJob);

        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etArea = findViewById(R.id.etArea);
        etWage = findViewById(R.id.etWage);
        etUpiId = findViewById(R.id.etUpiId);

        // Dynamic category fields
        layoutDriverSpecific = findViewById(R.id.layoutDriverSpecific);
        etVehicleType = findViewById(R.id.etVehicleType);
        etRatePerKm = findViewById(R.id.etRatePerKm);
        etMinCharge = findViewById(R.id.etMinCharge);

        layoutCookSpecific = findViewById(R.id.layoutCookSpecific);
        etFoodItems = findViewById(R.id.etFoodItems);
        etRatePerPlate = findViewById(R.id.etRatePerPlate);

        layoutClimberSpecific = findViewById(R.id.layoutClimberSpecific);
        etRatePerTree = findViewById(R.id.etRatePerTree);

        layoutGeneralWage = findViewById(R.id.layoutGeneralWage);
    }

    private void checkUserSession() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            layoutAuth.setVisibility(View.GONE);
            layoutMainApp.setVisibility(View.VISIBLE);
            tvUserEmail.setText("Account: " + currentUser.getEmail());
            listenToCloudWorkers();
        } else {
            layoutAuth.setVisibility(View.VISIBLE);
            layoutMainApp.setVisibility(View.GONE);
        }
    }

    private void setupAuthListeners() {
        btnLogin.setOnClickListener(v -> {
            String email = etAuthEmail.getText().toString().trim();
            String pass = etAuthPassword.getText().toString().trim();
            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Email-ഉം Password-ഉം നൽകുക", Toast.LENGTH_SHORT).show();
                return;
            }
            mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    checkUserSession();
                } else {
                    Toast.makeText(this, "Login Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnSignUp.setOnClickListener(v -> {
            String email = etAuthEmail.getText().toString().trim();
            String pass = etAuthPassword.getText().toString().trim();
            if (email.isEmpty() || pass.length() < 6) {
                Toast.makeText(this, "സാധുവായ Email-ഉം കുറഞ്ഞത് 6 അക്ഷരമുള്ള Password-ഉം നൽകുക", Toast.LENGTH_SHORT).show();
                return;
            }
            mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "അക്കൗണ്ട് നിർമ്മിച്ചു!", Toast.LENGTH_SHORT).show();
                    checkUserSession();
                } else {
                    Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            checkUserSession();
        });

        btnDeleteAccount.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("ഡാറ്റ ഇല്ലാതാക്കുക (Delete Data)")
                    .setMessage("നിങ്ങളുടെ അക്കൗണ്ടും രജിസ്റ്റർ ചെയ്ത എല്ലാ തൊഴിൽ വിവരങ്ങളും എന്നെന്നേക്കുമായി ഇല്ലാതാക്കണോ?")
                    .setPositiveButton("ഡിലീറ്റ് ചെയ്യുക", (dialog, which) -> deleteUserCloudData())
                    .setNegativeButton("റദ്ദാക്കുക", null)
                    .show();
        });
    }

    private void deleteUserCloudData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;
        String uid = user.getUid();

        db.collection("workers").whereEqualTo("userId", uid).get().addOnSuccessListener(snapshot -> {
            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                doc.getReference().delete();
            }
            user.delete().addOnCompleteListener(task -> {
                Toast.makeText(this, "നിങ്ങളുടെ അക്കൗണ്ടും വിവരങ്ങളും പൂർണ്ണമായി ഡിലീറ്റ് ചെയ്തു!", Toast.LENGTH_LONG).show();
                checkUserSession();
            });
        });
    }

    private void setupJobDropdowns() {
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, jobCategories);
        spinnerFilterJob.setAdapter(filterAdapter);

        String[] registerList = new String[jobCategories.length - 1];
        System.arraycopy(jobCategories, 1, registerList, 0, jobCategories.length - 1);
        ArrayAdapter<String> regAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, registerList);
        spinnerWorkerJob.setAdapter(regAdapter);

        spinnerWorkerJob.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = registerList[position];
                layoutDriverSpecific.setVisibility(View.GONE);
                layoutCookSpecific.setVisibility(View.GONE);
                layoutClimberSpecific.setVisibility(View.GONE);
                layoutGeneralWage.setVisibility(View.VISIBLE);

                if (selected.contains("ഡ്രൈവർ")) {
                    layoutDriverSpecific.setVisibility(View.VISIBLE);
                    layoutGeneralWage.setVisibility(View.GONE);
                } else if (selected.contains("പാചകം")) {
                    layoutCookSpecific.setVisibility(View.VISIBLE);
                    layoutGeneralWage.setVisibility(View.GONE);
                } else if (selected.contains("തെങ്ങ് കയറ്റം")) {
                    layoutClimberSpecific.setVisibility(View.VISIBLE);
                    layoutGeneralWage.setVisibility(View.GONE);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerFilterJob.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                renderWorkerCards(jobCategories[position]);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnSaveProfile.setOnClickListener(v -> saveWorkerToFirestore());
    }

    private void setupBottomNavigation() {
        navSearch.setOnClickListener(v -> {
            viewSearch.setVisibility(View.VISIBLE);
            viewRegister.setVisibility(View.GONE);
            viewProfile.setVisibility(View.GONE);
            navSearch.setTextColor(Color.parseColor("#81C784"));
            navRegister.setTextColor(Color.parseColor("#B0BEC5"));
            navProfile.setTextColor(Color.parseColor("#B0BEC5"));
            renderWorkerCards(spinnerFilterJob.getSelectedItem().toString());
        });

        navRegister.setOnClickListener(v -> {
            viewSearch.setVisibility(View.GONE);
            viewRegister.setVisibility(View.VISIBLE);
            viewProfile.setVisibility(View.GONE);
            navRegister.setTextColor(Color.parseColor("#81C784"));
            navSearch.setTextColor(Color.parseColor("#B0BEC5"));
            navProfile.setTextColor(Color.parseColor("#B0BEC5"));
        });

        navProfile.setOnClickListener(v -> {
            viewSearch.setVisibility(View.GONE);
            viewRegister.setVisibility(View.GONE);
            viewProfile.setVisibility(View.VISIBLE);
            navProfile.setTextColor(Color.parseColor("#81C784"));
            navSearch.setTextColor(Color.parseColor("#B0BEC5"));
            navRegister.setTextColor(Color.parseColor("#B0BEC5"));
        });
    }

    private void listenToCloudWorkers() {
        db.collection("workers").addSnapshotListener((value, error) -> {
            if (error != null || value == null) return;
            cloudWorkerList.clear();
            for (DocumentSnapshot doc : value.getDocuments()) {
                WorkerProfile p = new WorkerProfile(
                        doc.getId(),
                        doc.getString("userId"),
                        doc.getString("name"),
                        doc.getString("phone"),
                        doc.getString("job"),
                        doc.getString("area"),
                        doc.getLong("wage") != null ? doc.getLong("wage") : 0,
                        doc.getString("upiId"),
                        doc.getString("extraDetails")
                );
                cloudWorkerList.add(p);
            }
            renderWorkerCards(spinnerFilterJob.getSelectedItem() != null ? spinnerFilterJob.getSelectedItem().toString() : jobCategories[0]);
        });
    }

    private void saveWorkerToFirestore() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String area = etArea.getText().toString().trim();
        String upi = etUpiId.getText().toString().trim();
        String selectedJob = spinnerWorkerJob.getSelectedItem().toString();

        long wage = 0;
        StringBuilder extra = new StringBuilder();

        if (name.isEmpty() || phone.isEmpty() || area.isEmpty()) {
            Toast.makeText(this, "ദയവായി പേരും ഫോൺ നമ്പറും സ്ഥലവും നൽകുക", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedJob.contains("ഡ്രൈവർ")) {
            String vType = etVehicleType.getText().toString().trim();
            String perKm = etRatePerKm.getText().toString().trim();
            String minCh = etMinCharge.getText().toString().trim();
            if (perKm.isEmpty()) {
                Toast.makeText(this, "1 Km നിരക്ക് നൽകുക", Toast.LENGTH_SHORT).show();
                return;
            }
            wage = Long.parseLong(perKm);
            extra.append("വാഹനം: ").append(vType.isEmpty() ? "Auto/Car" : vType)
                    .append(" | 1 Km നിരക്ക്: ₹").append(perKm);
            if (!minCh.isEmpty()) extra.append(" (മിനിമം: ₹").append(minCh).append(")");
        } else if (selectedJob.contains("പാചകം")) {
            String food = etFoodItems.getText().toString().trim();
            String plate = etRatePerPlate.getText().toString().trim();
            if (plate.isEmpty()) {
                Toast.makeText(this, "പ്ലേറ്റ് നിരക്ക് നൽകുക", Toast.LENGTH_SHORT).show();
                return;
            }
            wage = Long.parseLong(plate);
            extra.append("വിഭവങ്ങൾ: ").append(food.isEmpty() ? "സദ്യ/ബിരിയാണി" : food)
                    .append(" | നിരക്ക്: ₹").append(plate).append(" / പ്ലേറ്റ്");
        } else if (selectedJob.contains("തെങ്ങ് കയറ്റം")) {
            String perTree = etRatePerTree.getText().toString().trim();
            if (perTree.isEmpty()) {
                Toast.makeText(this, "1 തെങ്ങിന് നിരക്ക് നൽകുക", Toast.LENGTH_SHORT).show();
                return;
            }
            wage = Long.parseLong(perTree);
            extra.append("1 തെങ്ങിന് നിരക്ക്: ₹").append(perTree);
        } else {
            String wageStr = etWage.getText().toString().trim();
            if (wageStr.isEmpty()) {
                Toast.makeText(this, "കൂലി നിരക്ക് നൽകുക", Toast.LENGTH_SHORT).show();
                return;
            }
            wage = Long.parseLong(wageStr);
            extra.append("ദിവസ വേതനം: ₹").append(wage);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getUid());
        data.put("name", name);
        data.put("phone", phone);
        data.put("job", selectedJob);
        data.put("area", area);
        data.put("wage", wage);
        data.put("upiId", upi);
        data.put("extraDetails", extra.toString());

        db.collection("workers").add(data).addOnSuccessListener(doc -> {
            Toast.makeText(this, "വിവരങ്ങൾ വിജയകരമായി പബ്ലിഷ് ചെയ്തു!", Toast.LENGTH_LONG).show();
            etName.setText("");
            etPhone.setText("");
            etArea.setText("");
            etWage.setText("");
            etUpiId.setText("");
            etVehicleType.setText("");
            etRatePerKm.setText("");
            etMinCharge.setText("");
            etFoodItems.setText("");
            etRatePerPlate.setText("");
            etRatePerTree.setText("");
            navSearch.performClick();
        });
    }

    private void renderWorkerCards(String selectedCategory) {
        containerWorkersList.removeAllViews();

        List<WorkerProfile> filtered = new ArrayList<>();
        for (WorkerProfile profile : cloudWorkerList) {
            if (selectedCategory.equals(jobCategories[0]) || (profile.job != null && profile.job.equalsIgnoreCase(selectedCategory))) {
                filtered.add(profile);
            }
        }

        if (filtered.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("ഈ വിഭാഗത്തിൽ നിലവിൽ ആരും രജിസ്റ്റർ ചെയ്തിട്ടില്ല.\n'എന്റെ തൊഴിൽ' വഴി രജിസ്റ്റർ ചെയ്താൽ എല്ലാവർക്കും കാണാം.");
            tvEmpty.setTextColor(Color.parseColor("#B0BEC5"));
            tvEmpty.setPadding(20, 40, 20, 20);
            tvEmpty.setTextSize(13f);
            containerWorkersList.addView(tvEmpty);
            return;
        }

        for (WorkerProfile worker : filtered) {
            // Liquid Glass Card Styling
            LinearLayout glassCard = new LinearLayout(this);
            glassCard.setOrientation(LinearLayout.VERTICAL);
            glassCard.setBackgroundResource(R.drawable.glass_card_bg);
            glassCard.setPadding(20, 18, 20, 18);

            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(0, 0, 0, 16);
            glassCard.setLayoutParams(cardParams);

            TextView tvName = new TextView(this);
            tvName.setText(worker.name + " (" + worker.area + ")");
            tvName.setTextSize(16f);
            tvName.setTextColor(Color.parseColor("#FFFFFF"));
            tvName.setTypeface(null, android.graphics.Typeface.BOLD);

            TextView tvJob = new TextView(this);
            tvJob.setText(worker.job);
            tvJob.setTextSize(13f);
            tvJob.setPadding(0, 4, 0, 2);
            tvJob.setTextColor(Color.parseColor("#81C784"));

            TextView tvDetails = new TextView(this);
            tvDetails.setText(worker.extraDetails != null && !worker.extraDetails.isEmpty() ? worker.extraDetails : ("വേതനം: ₹ " + worker.wage));
            tvDetails.setTextSize(14f);
            tvDetails.setTextColor(Color.parseColor("#FFD54F"));
            tvDetails.setTypeface(null, android.graphics.Typeface.BOLD);
            tvDetails.setPadding(0, 2, 0, 6);

            LinearLayout btnRow = new LinearLayout(this);
            btnRow.setOrientation(LinearLayout.HORIZONTAL);
            btnRow.setWeightSum(3);
            btnRow.setPadding(0, 10, 0, 0);

            // Frosted Glass Buttons
            Button btnCall = new Button(this);
            btnCall.setText("വിളിക്കുക 📞");
            btnCall.setBackgroundColor(Color.parseColor("#332E7D32"));
            btnCall.setTextColor(Color.WHITE);
            LinearLayout.LayoutParams p1 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            p1.rightMargin = 4;
            btnCall.setLayoutParams(p1);
            btnCall.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + worker.phone))));

            Button btnChat = new Button(this);
            btnChat.setText("ചാറ്റ് 💬");
            btnChat.setBackgroundColor(Color.parseColor("#330288D1"));
            btnChat.setTextColor(Color.WHITE);
            LinearLayout.LayoutParams p2 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            p2.rightMargin = 4;
            btnChat.setLayoutParams(p2);
            btnChat.setOnClickListener(v -> openDirectChat(worker.name, worker.phone));

            Button btnPay = new Button(this);
            btnPay.setText("കൂലി ₹");
            btnPay.setBackgroundColor(Color.parseColor("#33EF6C00"));
            btnPay.setTextColor(Color.WHITE);
            LinearLayout.LayoutParams p3 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            btnPay.setLayoutParams(p3);
            btnPay.setOnClickListener(v -> {
                String targetUpi = (worker.upiId == null || worker.upiId.isEmpty()) ? "kottayi@upi" : worker.upiId;
                Uri upiUri = Uri.parse("upi://pay").buildUpon()
                        .appendQueryParameter("pa", targetUpi)
                        .appendQueryParameter("pn", worker.name)
                        .appendQueryParameter("tn", "Kottayi Coolie Service")
                        .appendQueryParameter("am", String.valueOf(worker.wage))
                        .appendQueryParameter("cu", "INR")
                        .build();
                try {
                    startActivity(Intent.createChooser(new Intent(Intent.ACTION_VIEW, upiUri), "UPI വഴി നൽകുക"));
                } catch (Exception e) {
                    Toast.makeText(this, "UPI ആപ്പ് കണ്ടെത്തിയില്ല", Toast.LENGTH_SHORT).show();
                }
            });

            btnRow.addView(btnCall);
            btnRow.addView(btnChat);
            btnRow.addView(btnPay);

            glassCard.addView(tvName);
            glassCard.addView(tvJob);
            glassCard.addView(tvDetails);
            glassCard.addView(btnRow);

            containerWorkersList.addView(glassCard);
        }
    }

    private void openDirectChat(String name, String phone) {
        try {
            String cleanPhone = phone.replaceAll("[^0-9]", "");
            if (cleanPhone.length() == 10) cleanPhone = "91" + cleanPhone;
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://api.whatsapp.com/send?phone=" + cleanPhone + "&text=" + Uri.encode("നമസ്കാരം " + name + ", എന്റെ കോട്ടായി ആപ്പ് വഴിയാണ് മെസ്സേജ് അയക്കുന്നത്.")));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, name + "-മായി ചാറ്റ് ചെയ്യാൻ WhatsApp ലഭ്യമല്ല", Toast.LENGTH_SHORT).show();
        }
    }

    private void initAtmosphere() {
        cloudPaint.setColor(Color.parseColor("#26FFFFFF"));
        cloudPaint.setStyle(Paint.Style.FILL);
        cloudPaint.setAntiAlias(true);

        starPaint.setColor(Color.WHITE);
        starPaint.setStyle(Paint.Style.FILL);
        starPaint.setAntiAlias(true);

        moonPaint.setColor(Color.parseColor("#FFF59D"));
        moonPaint.setStyle(Paint.Style.FILL);
        moonPaint.setAntiAlias(true);

        rainPaint.setColor(Color.parseColor("#8090A4AE"));
        rainPaint.setStrokeWidth(3f);
        rainPaint.setAntiAlias(true);

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        isNight = (hour >= 18 || hour < 6);

        Random rnd = new Random();
        for (int i = 0; i < 6; i++) {
            Cloud c = new Cloud();
            c.x = rnd.nextInt(1000);
            c.y = 120 + rnd.nextInt(600);
            c.radius = 70 + rnd.nextInt(60);
            c.speed = 0.5f + (rnd.nextFloat() * 0.8f);
            clouds.add(c);
        }
        for (int i = 0; i < 35; i++) {
            Star s = new Star();
            s.x = rnd.nextInt(1080);
            s.y = rnd.nextInt(900);
            s.radius = 2f + rnd.nextFloat() * 3f;
            s.alpha = rnd.nextInt(255);
            s.alphaSpeed = 2f + rnd.nextFloat() * 3f;
            stars.add(s);
        }
        for (int i = 0; i < 40; i++) {
            RainDrop d = new RainDrop();
            d.x = rnd.nextInt(1080);
            d.y = rnd.nextInt(1920);
            d.length = 24 + rnd.nextInt(20);
            d.speed = 18 + rnd.nextInt(10);
            rainDrops.add(d);
        }

        weatherEffectView.setBackground(new android.graphics.drawable.Drawable() {
            @Override
            public void draw(Canvas canvas) {
                int width = canvas.getWidth() > 0 ? canvas.getWidth() : 1080;
                if (isNight) {
                    canvas.drawCircle(width - 150, 180, 36, moonPaint);
                    Paint shadow = new Paint();
                    shadow.setColor(Color.parseColor("#061A24"));
                    canvas.drawCircle(width - 138, 172, 32, shadow);
                    for (Star s : stars) {
                        s.alpha += s.alphaSpeed;
                        if (s.alpha > 255 || s.alpha < 40) s.alphaSpeed = -s.alphaSpeed;
                        starPaint.setAlpha((int) Math.max(40, Math.min(255, s.alpha)));
                        canvas.drawCircle(s.x, s.y, s.radius, starPaint);
                    }
                }
                if (hasClouds) {
                    for (Cloud c : clouds) {
                        c.x += c.speed;
                        if (c.x - c.radius > width) {
                            c.x = -c.radius * 2;
                        }
                        canvas.drawCircle(c.x, c.y, c.radius, cloudPaint);
                        canvas.drawCircle(c.x + (c.radius * 0.7f), c.y - (c.radius * 0.2f), c.radius * 0.8f, cloudPaint);
                        canvas.drawCircle(c.x - (c.radius * 0.6f), c.y + (c.radius * 0.1f), c.radius * 0.7f, cloudPaint);
                    }
                }
                if (isRainy) {
                    for (RainDrop d : rainDrops) {
                        d.y += d.speed;
                        if (d.y > canvas.getHeight() && canvas.getHeight() > 0) {
                            d.y = -d.length;
                        }
                        canvas.drawLine(d.x, d.y, d.x - 4, d.y + d.length, rainPaint);
                    }
                }
            }
            @Override public void setAlpha(int alpha) {}
            @Override public void setColorFilter(android.graphics.ColorFilter colorFilter) {}
            @Override public int getOpacity() { return android.graphics.PixelFormat.TRANSLUCENT; }
        });

        Runnable animator = new Runnable() {
            @Override
            public void run() {
                if (!isAnimating) return;
                weatherEffectView.invalidate();
                animationHandler.postDelayed(this, 35);
            }
        };
        animationHandler.post(animator);
    }

    private void fetchKottayiWeather() {
        new Thread(() -> {
            try {
                String apiUrl = "https://api.open-meteo.com/v1/forecast?latitude=10.7511&longitude=76.5292&current_weather=true";
                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();

                JSONObject json = new JSONObject(response.toString());
                JSONObject current = json.getJSONObject("current_weather");
                int weatherCode = current.getInt("weathercode");
                double temp = current.getDouble("temperature");

                runOnUiThread(() -> {
                    isRainy = ((weatherCode >= 51 && weatherCode <= 67) || (weatherCode >= 80 && weatherCode <= 82) || weatherCode >= 95);
                    if (isNight) {
                        tvWeatherBadge.setText("🌙 രാത്രി (" + (int)temp + "°C)");
                    } else if (isRainy) {
                        tvWeatherBadge.setText("🌧️ മഴ (" + (int)temp + "°C)");
                    } else {
                        tvWeatherBadge.setText("⛅ മേഘാവൃതം (" + (int)temp + "°C)");
                    }
                });
            } catch (Exception ignored) {}
        }).start();
    }

    private void playKeralaTone() {
        new Thread(() -> {
            try {
                int sampleRate = 44100;
                int count = (sampleRate * 1400) / 1000;
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
                        .setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
                        .setAudioFormat(new AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(sampleRate).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
                        .setBufferSizeInBytes(samples.length * 2)
                        .setTransferMode(AudioTrack.MODE_STATIC).build();
                track.write(samples, 0, samples.length);
                track.play();
            } catch (Exception ignored) {}
        }).start();
    }

    private void dismissSplash() {
        if (tvDeveloperCredit != null) {
            tvDeveloperCredit.setAlpha(0f);
            tvDeveloperCredit.setScaleX(0.7f);
            tvDeveloperCredit.setScaleY(0.7f);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                tvDeveloperCredit.animate().alpha(1.0f).scaleX(1.0f).scaleY(1.0f).setDuration(600).start();
            }, 500);
        }
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (splashOverlay != null) {
                splashOverlay.animate().alpha(0.0f).setDuration(500).withEndAction(() -> splashOverlay.setVisibility(View.GONE)).start();
            }
        }, 2200);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isAnimating = false;
        animationHandler.removeCallbacksAndMessages(null);
    }
}
