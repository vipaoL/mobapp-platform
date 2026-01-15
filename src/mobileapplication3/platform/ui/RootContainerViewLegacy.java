// SPDX-License-Identifier: LGPL-2.1-only

package mobileapplication3.platform.ui;

import android.content.Context;

public class RootContainerViewLegacy extends RootContainerView {
    public RootContainerViewLegacy(Context context) {
        super(context);
        // idk how to set a visibility listener in old androids
        onShow();
    }
}
