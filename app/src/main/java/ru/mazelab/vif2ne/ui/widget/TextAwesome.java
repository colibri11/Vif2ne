package ru.mazelab.vif2ne.ui.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.LruCache;
import android.widget.TextView;

import ru.mazelab.vif2ne.R;

/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Created by Colibri  15.05.15 22:19
 * TextAwesome.java
 *
 *
 */

public class TextAwesome extends TextView {

    private static final int[] STATE_ALLOW = new int[]{R.attr.state_allow};
    private static final int[] STATE_ERROR = new int[]{R.attr.state_error};
    private static final int[] STATE_COMPLETE = new int[]{R.attr.state_complete};
    private final static String NAME = "FONTAWESOME";
    private static LruCache<String, Typeface> sTypefaceCache = new LruCache<String, Typeface>(12);
    private boolean sAllow = false;
    private boolean sError = false;
    private boolean sComplete = true;

    public TextAwesome(Context context) {
        super(context);
        init();
    }

    public TextAwesome(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void init() {

        Typeface typeface = sTypefaceCache.get(NAME);

        if (typeface == null) {

            typeface = Typeface.createFromAsset(getContext().getAssets(), "font/fontawesome.ttf");
            sTypefaceCache.put(NAME, typeface);

        }

        setTypeface(typeface);

    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        int[] state = super.onCreateDrawableState(extraSpace + 4);
        if (sAllow) {
            mergeDrawableStates(state, STATE_ALLOW);
        }
        if (sError) {
            mergeDrawableStates(state, STATE_ERROR);
        }
        if (sComplete) {
            mergeDrawableStates(state, STATE_COMPLETE);
        }
        return state;
    }

    public void setsAllow(boolean sAllow) {
        this.sAllow = sAllow;
        if (sAllow) {
            this.sComplete = false;
            this.sError = false;
        }
        refreshDrawableState();
    }

    public void setsComplete(boolean sComplete) {
        this.sComplete = sComplete;
        if (sComplete) {
            this.sAllow = false;
            this.sError = false;
        }
        refreshDrawableState();
    }

    public void setsError(boolean sError) {
        this.sError = sError;
        if (sError) {
            this.sAllow = false;
            this.sComplete = false;
        }

        refreshDrawableState();
    }

}
