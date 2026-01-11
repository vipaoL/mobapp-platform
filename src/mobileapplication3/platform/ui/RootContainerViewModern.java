// SPDX-License-Identifier: LGPL-2.1-only

package mobileapplication3.platform.ui;

import android.content.Context;
import android.util.Log;
import android.view.View;

public class RootContainerViewModern extends RootContainerView {
    public RootContainerViewModern(Context context) {
        super(context);
    }

    // Added in API level 8. Causes VerifyError on older versions
    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        Log.d("Visibility changed", changedView.getClass().getSimpleName() + " " + visibility);
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == VISIBLE) {
            onShow();
        }
        if (visibility == INVISIBLE) {
            onHide();
        }
    }
}
