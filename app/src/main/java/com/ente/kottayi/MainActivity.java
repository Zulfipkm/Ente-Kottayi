    private void dismissSplash() {
        View splashLogo = findViewById(R.id.splashLogo);
        View tvSplashTitle = findViewById(R.id.tvSplashTitle);
        View tvDeveloperCredit = findViewById(R.id.tvDeveloperCredit);

        // Initial state for animation
        if (tvDeveloperCredit != null) {
            tvDeveloperCredit.setAlpha(0f);
            tvDeveloperCredit.setScaleX(0.7f);
            tvDeveloperCredit.setScaleY(0.7f);

            // Premium animated entrance for developer credit
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                tvDeveloperCredit.animate()
                        .alpha(1.0f)
                        .scaleX(1.05f)
                        .scaleY(1.05f)
                        .setDuration(700)
                        .withEndAction(() -> {
                            tvDeveloperCredit.animate()
                                    .scaleX(1.0f)
                                    .scaleY(1.0f)
                                    .setDuration(250)
                                    .start();
                        })
                        .start();
            }, 500);
        }

        // Smooth fade out of entire splash screen
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (splashOverlay != null) {
                splashOverlay.animate()
                        .alpha(0.0f)
                        .setDuration(600)
                        .withEndAction(() -> splashOverlay.setVisibility(View.GONE))
                        .start();
            }
        }, 2500);
    }
