package com.reserve.app;

public class BottomMenuItem {
    int iconResId;
    String label;

    public BottomMenuItem(int iconResId, String label) {
        this.iconResId = iconResId;
        this.label = label;
    }

    public int getIconResId() {
        return iconResId;
    }

    public String getLabel() {
        return label;
    }
}
