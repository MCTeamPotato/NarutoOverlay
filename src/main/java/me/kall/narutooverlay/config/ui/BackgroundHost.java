package me.kall.narutooverlay.config.ui;

public interface BackgroundHost {
    void chooseBackgroundImage();

    void clearBackgroundImage();

    void chooseBackgroundFolder();

    void clearBackgroundFolder();

    void setBackgroundInterval(double seconds);

    void setBackgroundFade(double seconds);

    void setBackgroundDim(double dim);
}
