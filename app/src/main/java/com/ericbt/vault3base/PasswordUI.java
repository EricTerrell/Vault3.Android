/*
  Vault 3
  (C) Copyright 2024, Eric Bergman-Terrell

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

import android.text.InputFilter;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.widget.EditText;

public class PasswordUI {
    public static InputFilter createPasswordInputFilter() {
        // http://stackoverflow.com/questions/3349121/how-do-i-use-inputfilter-to-limit-characters-in-an-edittext-in-android
        return (source, start, end, dest, dstart, dend) -> {
            if (source instanceof SpannableStringBuilder) {
                SpannableStringBuilder sourceAsSpannableBuilder = (SpannableStringBuilder) source;

                for (int i = end - 1; i >= start; i--) {
                    char currentChar = source.charAt(i);

                    if (!isValid(currentChar)) {
                        sourceAsSpannableBuilder.delete(i, i + 1);
                    }
                }

                return source;
            } else {
                StringBuilder filteredStringBuilder = new StringBuilder();

                for (int i = start; i < end; i++) {
                    char currentChar = source.charAt(i);

                    if (isValid(currentChar)) {
                        filteredStringBuilder.append(currentChar);
                    }
                }

                return filteredStringBuilder.toString();
            }
        };
    }

    private static boolean isValid(char ch) {
        return !Character.isWhitespace(ch);
    }

    public static void updatePasswordInputType(EditText editText, boolean forceUppercase, boolean showPassword) {
        int inputType = InputType.TYPE_CLASS_TEXT |
                (showPassword ? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD : InputType.TYPE_TEXT_VARIATION_PASSWORD);

        if (forceUppercase) {
            inputType |= InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS;
        }

        editText.setInputType(inputType);
    }
}
