package com.ericbt.vault3base;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;

public class MoveItemHereActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (Globals.getApplication().getVaultDocument() == null) {
			setResult(RESULT_CANCELED);
			finish();
			return;
		}

		getActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.move_item_here);
		
		setTitle("Move Item Here");
		
		final RadioButton placeAboveSelectedItem = (RadioButton) findViewById(R.id.PlaceAboveSelectedItem);

		Button okButton = (Button) findViewById(R.id.OKButton);
		
		okButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent returnData = new Intent();
				returnData.putExtra(StringLiterals.Above, placeAboveSelectedItem.isChecked());
				returnData.putExtra(StringLiterals.SelectedOutlineItemId, getIntent().getExtras().getInt(StringLiterals.SelectedOutlineItemId));
				returnData.putExtra(StringLiterals.SelectedOutlineItemSortOrder, getIntent().getExtras().getInt(StringLiterals.SelectedOutlineItemSortOrder));
				returnData.putExtra(StringLiterals.SelectedOutlineItemParentId, getIntent().getExtras().getInt(StringLiterals.SelectedOutlineItemParentId));
				setResult(RESULT_OK, returnData);
				finish();
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean result = false;
		
		if (item.getItemId() == android.R.id.home) {
			finish();
			
			result = true;
		}
		
		return result;
	}
}
