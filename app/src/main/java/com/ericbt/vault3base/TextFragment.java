/*
  Vault 3
  (C) Copyright 2022, Eric Bergman-Terrell

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

import android.app.Activity;
import android.app.Fragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ericbt.vault3base.async_tasks.update_font.UpdateFontTask;
import com.ericbt.vault3base.async_tasks.update_font.UpdateFontTaskParameters;
import com.ericbt.vault3base.async_tasks.update_outline_item.UpdateOutlineItemTask;
import com.ericbt.vault3base.async_tasks.update_outline_item.UpdateOutlineItemTaskParameters;

import fonts.AndroidFont;
import fonts.FontList;

public class TextFragment extends Fragment implements TextDisplayUpdate {
    private String titleText, textText, fontName;
    private float fontSize;
    private int red;
    private int green;
    private int blue;
    private int fontStyle;
    private int outlineItemId;
    private int outlineItemParentId;
    private TextView title, text;
    private ScrollView scrollView;
    private Button edit, sendEmail, setFont;
    private View divider;
    private enum MenuItemEnum { CopyTitle, CopyText, CopyTitleAndText }

    private static final int SET_FONT = 1;
    private static final int EDIT_ITEM = 2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.text_fragment, container, false);

        if (savedInstanceState != null) {
            outlineItemId = savedInstanceState.getInt(StringLiterals.OutlineItemId);
            outlineItemParentId = savedInstanceState.getInt(StringLiterals.OutlineItemParentId);
        }
        else if (hasExtras()) {
            outlineItemId = getActivity().getIntent().getExtras().getInt(StringLiterals.OutlineItemId);
            outlineItemParentId = getActivity().getIntent().getExtras().getInt(StringLiterals.OutlineItemParentId);
        }

        if (savedInstanceState != null) {
            titleText = savedInstanceState.getString(StringLiterals.Title);
            textText = savedInstanceState.getString(StringLiterals.Text);
        }
        else if (hasExtras()) {
            titleText = getActivity().getIntent().getExtras().getString(StringLiterals.Title);
            textText = getActivity().getIntent().getExtras().getString(StringLiterals.Text);
        }

        divider = view.findViewById(R.id.Divider);

        title = view.findViewById(R.id.Title);
        title.setText(titleText);

        text = view.findViewById(R.id.Text);
        text.setText(textText);

        if (savedInstanceState != null) {
            red = savedInstanceState.getInt(StringLiterals.Red);
            green = savedInstanceState.getInt(StringLiterals.Green);
            blue = savedInstanceState.getInt(StringLiterals.Blue);
        }
        else if (hasExtras()) {
            red = getActivity().getIntent().getExtras().getInt(StringLiterals.Red);
            green = getActivity().getIntent().getExtras().getInt(StringLiterals.Green);
            blue = getActivity().getIntent().getExtras().getInt(StringLiterals.Blue);
        }

        int color = Color.argb(255, red, green, blue);

        title.setTextColor(color);
        text.setTextColor(color);

        if (savedInstanceState != null) {
            fontSize = savedInstanceState.getFloat(StringLiterals.FontSizeInPoints);
        }
        else if (hasExtras()) {
            fontSize = getActivity().getIntent().getExtras().getFloat(StringLiterals.FontSizeInPoints);
        }

        if (fontSize > 0.0) {
            text.setTextSize(TypedValue.COMPLEX_UNIT_PX, FontUtils.pointsToPixels(fontSize));
        }

        if (savedInstanceState != null) {
            fontName = savedInstanceState.getString(StringLiterals.FontName);
            fontStyle = savedInstanceState.getInt(StringLiterals.FontStyle);
        }
        else if (hasExtras()) {
            fontName = getActivity().getIntent().getExtras().getString(StringLiterals.FontName);
            fontStyle = getActivity().getIntent().getExtras().getInt(StringLiterals.FontStyle);
        }

        if (fontName != null) {
            Typeface typeface = Typeface.create(fontName, fontStyle);

            title.setTypeface(typeface);
            text.setTypeface(typeface);
        }

        scrollView = view.findViewById(R.id.ScrollView);

        RGBColor backgroundColor = VaultPreferenceActivity.getTextBackgroundColor();

        if (backgroundColor != null) {
            int bgColor = Color.argb(255, backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue());

            scrollView.setBackgroundColor(bgColor);
            title.setBackgroundColor(bgColor);
            text.setBackgroundColor(bgColor);
        }

        registerForContextMenu(title);
        registerForContextMenu(text);

        String linkToMap = getResources().getString(R.string.link_to_map);

        // Disable address linking for platforms that don't support it.
        if (linkToMap.equals("false")) {
            text.setAutoLinkMask(text.getAutoLinkMask() & ~Linkify.MAP_ADDRESSES);
        }

        edit = view.findViewById(R.id.Edit);

        edit.setOnClickListener(v -> {
            Intent intent = new Intent(TextFragment.this.getActivity(), EditItemActivity.class);
            intent.putExtra(StringLiterals.Title, titleText);
            intent.putExtra(StringLiterals.Text, textText);
            intent.putExtra(StringLiterals.OutlineItemId, TextFragment.this.getActivity().getIntent().getExtras().getInt(StringLiterals.OutlineItemId));
            intent.putExtra(StringLiterals.OutlineItemParentId, TextFragment.this.getActivity().getIntent().getExtras().getInt(StringLiterals.OutlineItemParentId));
            startActivityForResult(intent, EDIT_ITEM);
        });

        sendEmail = view.findViewById(R.id.SendEmail);

        sendEmail.setOnClickListener(v -> {
            // Create the Intent
            final Intent emailIntent = new Intent(Intent.ACTION_SEND);

            // Fill it with Data
            emailIntent.setType("plain/text");
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, TextFragment.this.getActivity().getIntent().getExtras().getString(StringLiterals.Title));

            String paidVersionURL = getResources().getString(R.string.email_download_url);

            String sentByVault3 = String.format("This email was sent to you by Vault 3. Download your copy from %s", paidVersionURL);
            String text = String.format("%s\n\n%s", TextFragment.this.getActivity().getIntent().getExtras().getString(StringLiterals.Text), sentByVault3);

            emailIntent.putExtra(Intent.EXTRA_TEXT, text);

            // Send it to the Activity-Chooser
            startActivity(Intent.createChooser(emailIntent, "Send Email..."));
        });

        setFont = view.findViewById(R.id.SetFont);

        setFont.setOnClickListener(v -> {
            Intent intent = new Intent(TextFragment.this.getActivity(), SetFontActivity.class);

            intent.putExtra(StringLiterals.FontName, fontName);
            intent.putExtra(StringLiterals.FontSizeInPoints, fontSize);
            intent.putExtra(StringLiterals.FontStyle, fontStyle);

            intent.putExtra(StringLiterals.Red, red);
            intent.putExtra(StringLiterals.Green, green);
            intent.putExtra(StringLiterals.Blue, blue);

            startActivityForResult(intent, SET_FONT);
        });

        return view;
    }

    private boolean hasExtras() {
        return getActivity().getIntent().getExtras() != null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Log.i(StringLiterals.LogTag, "TextFragment.onSaveInstanceState");

        outState.putString(StringLiterals.Title, titleText);
        outState.putString(StringLiterals.Text, textText);

        outState.putInt(StringLiterals.Red, red);
        outState.putInt(StringLiterals.Green, green);
        outState.putInt(StringLiterals.Blue, blue);

        outState.putString(StringLiterals.FontName, fontName);
        outState.putInt(StringLiterals.FontStyle, fontStyle);
        outState.putFloat(StringLiterals.FontSizeInPoints, fontSize);

        outState.putInt(StringLiterals.OutlineItemId, outlineItemId);
        outState.putInt(StringLiterals.OutlineItemParentId, outlineItemParentId);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        menu.setHeaderTitle(getActivity().getIntent().getExtras().getString(StringLiterals.Title));
        menu.add(0, MenuItemEnum.CopyTitle.ordinal(), 0, "Copy Title");
        menu.add(0, MenuItemEnum.CopyText.ordinal(), 1, "Copy Text");
        menu.add(0, MenuItemEnum.CopyTitleAndText.ordinal(), 2, "Copy Title & Text");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);

        if (item.getItemId() == MenuItemEnum.CopyTitle.ordinal()) {
            clipboard.setPrimaryClip(ClipData.newPlainText("Outline Item Title", getActivity().getIntent().getExtras().getString(StringLiterals.Title)));
        }
        else if (item.getItemId() == MenuItemEnum.CopyText.ordinal()) {
            clipboard.setPrimaryClip(ClipData.newPlainText("Outline Item Text", getActivity().getIntent().getExtras().getString(StringLiterals.Text)));
        }
        else if (item.getItemId() == MenuItemEnum.CopyTitleAndText.ordinal()) {
            clipboard.setPrimaryClip(ClipData.newPlainText("Outline Item Title and Text",
                    String.format("%s\r\n\r\n%s", getActivity().getIntent().getExtras().getString(StringLiterals.Title), getActivity().getIntent().getExtras().getString(StringLiterals.Text))));
        }

        return true;
    }

    public void enable(boolean enabled) {
        final boolean buttonsEnabled = enabled && outlineItemId != OutlineItem.ROOT_ID;
        final int visibility = buttonsEnabled ? View.VISIBLE : View.GONE;

        View[] views = new View[] { divider, title, text, scrollView, edit, setFont, sendEmail };

        for (View view : views) {
            view.setEnabled(buttonsEnabled);
            view.setVisibility(visibility);
        }

        if (getActivity() instanceof Vault3) {
            title.setVisibility(View.GONE);
            divider.setVisibility(View.GONE);
        }
    }

    public void update(boolean enable, OutlineItem outlineItem) {
        outlineItemId = outlineItem.getId();

        enable(enable);

        outlineItemId = outlineItem.getId();
        outlineItemParentId = outlineItem.getParentId();

        titleText = outlineItem.getTitle();
        textText = !outlineItem.isRoot() ? outlineItem.getText() : "";

        title.setText(titleText);
        text.setText(textText);

        final RGBColor rgbColor = outlineItem.getColor();

        if (rgbColor != null) {
            red = outlineItem.getColor().getRed();
            green = outlineItem.getColor().getGreen();
            blue = outlineItem.getColor().getBlue();

            final int color = Color.argb(255, red, green, blue);
            title.setTextColor(color);
            text.setTextColor(color);
        }

        final AndroidFont font = outlineItem.getFont();

        if (font != null) {
            text.setTextSize(TypedValue.COMPLEX_UNIT_PX, FontUtils.pointsToPixels(font.getSizeInPoints()));

            Typeface typeFace = Typeface.create(font.getName(), font.getStyle());

            title.setTypeface(typeFace);
            text.setTypeface(typeFace);

            fontName = font.getName();
            fontStyle = font.getStyle();
            fontSize = font.getSizeInPoints();
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        getAsyncTaskActivity().setEnabled(enabled);
    }

    public void update(OutlineItem outlineItem) {
        update(true, outlineItem);
    }

    @Override
    public AsyncTaskActivity getAsyncTaskActivity() {
        return (AsyncTaskActivity) getActivity();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case SET_FONT: {
                if (resultCode == Activity.RESULT_OK) {
                    AndroidFont newFont = new AndroidFont(data.getExtras().getString(StringLiterals.NewFontName),
                            data.getExtras().getFloat(StringLiterals.NewFontSizeInPoints),
                            data.getExtras().getInt(StringLiterals.NewFontStyle));

                    OutlineItem outlineItem = new OutlineItem();
                    outlineItem.setId(outlineItemId);
                    outlineItem.setColor(new RGBColor(data.getExtras().getInt(StringLiterals.Red), data.getExtras().getInt(StringLiterals.Green), data.getExtras().getInt(StringLiterals.Blue)));

                    outlineItem.setTitle(titleText);
                    outlineItem.setText(textText);

                    FontList fontList = FontList.deserialize(data.getExtras().getString(StringLiterals.FontList));
                    outlineItem.setFontList(fontList);

                    int newColor = data.getExtras().getInt(StringLiterals.NewFontColor);

                    enable(false);

                    new UpdateFontTask().execute(
                            new UpdateFontTaskParameters(
                                    newFont,
                                    outlineItem,
                                    newColor,
                                    this,
                                    TextFragment.this.getActivity()));
                }
            }
            break;

            case EDIT_ITEM: {
                if (resultCode == Activity.RESULT_OK) {
                    OutlineItem outlineItem = new OutlineItem();
                    outlineItem.setId(data.getExtras().getInt(StringLiterals.OutlineItemId));

                    enable(false);

                    new UpdateOutlineItemTask().execute(
                            new UpdateOutlineItemTaskParameters(
                                    outlineItem,
                                    data.getExtras().getString(StringLiterals.Title),
                                    data.getExtras().getString(StringLiterals.Text),
                                    this,
                                    TextFragment.this.getActivity()));
                }
            }
            break;
        }
    }

}
