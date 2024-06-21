
package com.haidev.kanibis.ui.searchList.view.is24location;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.util.AttributeSet;
import android.view.KeyEvent;

import androidx.appcompat.widget.AppCompatAutoCompleteTextView;

public class ISAutoComplete extends AppCompatAutoCompleteTextView {
    Activity _activity;
    public ISAutoComplete(Context context) {
        super(context);
        _activity = scanForActivity(context);
    }

    public ISAutoComplete(Context context, AttributeSet attrs) {
        super(context, attrs);
        _activity = scanForActivity(context);
    }

    public ISAutoComplete(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        _activity = scanForActivity(context);
    }

    private static Activity scanForActivity(Context cont) {
        if (cont == null)
            return null;
        else if (cont instanceof Activity)
            return (Activity)cont;
        else if (cont instanceof ContextWrapper)
            return scanForActivity(((ContextWrapper)cont).getBaseContext());

        return null;
    }

    @Override
    public boolean enoughToFilter() {
        return true;
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_UP) {
            _activity.onBackPressed();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }
}
