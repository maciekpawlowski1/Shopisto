package com.pawlowski.shopisto.filters;

import android.text.InputFilter;
import android.text.Spanned;

public class MyFilters {


    public static InputFilter getTittleInputFilter()
    {
        return new InputFilter() {
            public CharSequence filter(CharSequence source, int start,
                                       int end, Spanned dest, int dstart, int dend) {

                for (int i = start;i < end;i++) {
                    if (!Character.isLetterOrDigit(source.charAt(i)) &&
                            (source.charAt(i) != ' ') &&
                            (source.charAt(i) != '_') &&
                            (source.charAt(i) != '(') &&
                            (source.charAt(i) != ':') &&
                            (source.charAt(i) != '!') &&
                            (source.charAt(i) != '-') &&
                            (source.charAt(i) != ')'))
                    {
                        return "";
                    }
                }
                return null;
            }
        };
    }
}
