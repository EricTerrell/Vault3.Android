/*
  Vault 3
  (C) Copyright 2023, Eric Bergman-Terrell

  This file is part of Vault 3.

  Vault 3 is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  Vault 3 is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with Vault 3.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.ericbt.vault3base;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ericbt.vault3base.async.workers.ChangePassword;
import com.ericbt.vault3base.async.UpdateStatus;

public class ChangePasswordProcessingActivity extends AsyncTaskActivity {
    public static final int RESULT_EXCEPTION = RESULT_FIRST_USER;

    private ProgressBar progressBar;

    private TextView percentText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.change_password_processing_dialog);

        setTitle(getString(R.string.change_password_processing_title));

        progressBar = findViewById(R.id.progressBar);
        percentText = findViewById(R.id.percentText);

        final String newPassword = getIntent().getStringExtra(StringLiterals.NewPassword);

        new ChangePassword().changePassword(newPassword, this);
    }

    @Override
    public void enable(boolean enabled) {
    }

    public void progressUpdate(UpdateStatus updateStatus) {
        progressBar.setProgress((int) updateStatus.getPercent());

        percentText.setText(String.format("%.2f %%", updateStatus.getPercent()));
    }

    public void finish(Throwable ex) {
        final String message = ex == null ? "Password changed." : "Cannot change password.";
        final int result = ex == null ? RESULT_OK : RESULT_EXCEPTION;

        new AlertDialog.Builder(this)
                .setTitle("Change Password")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();

                    setResult(result);
                    finish();
                })
                .setCancelable(false)
                .create()
                .show();
    }

    @Override
    public void onBackPressed() {
        // Do not allow user to hit back while the password is being changed.
    }
}
