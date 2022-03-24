package com.pawlowski.shopisto.base;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.IdRes;

public abstract class BaseViewMvc {
    protected View rootView;

    protected <T extends View> T findViewById(@IdRes int id)
    {
        return rootView.findViewById(id);
    }

    public View getRootView()
    {
        return rootView;
    }

    public void hideKeyboard() {
        Context context = getRootView().getContext();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
    }
}
