// SPDX-License-Identifier: LGPL-2.1-only

package mobileapplication3.platform.ui;

import android.content.Context;
import android.util.Log;
import android.view.View;

public class RootContainerViewLegacy extends RootContainerView {
    public RootContainerViewLegacy(Context context) {
        super(context);
        // idk how to set a visibility listener in old androids
        onShow();
    }
}
