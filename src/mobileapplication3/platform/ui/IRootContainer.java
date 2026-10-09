package mobileapplication3.platform.ui;

import mobileapplication3.platform.KeyboardHelper;
import mobileapplication3.ui.IContainer;
import mobileapplication3.ui.IPopupFeedback;
import mobileapplication3.ui.IUIComponent;
import mobileapplication3.ui.UISettings;

public interface IRootContainer extends IContainer, IPopupFeedback, KeyboardHelper.IKeyboardListener{
    int getHeight();
    IUIComponent getRootUIComponent();
    int getWidth();
    void init();
    void onHide();
    void onShow();
    void setFullscreenMode(boolean value);
    void setRootUIComponent(IUIComponent rootUIComponent);
    void setUISettings(UISettings uiSettings);
}
