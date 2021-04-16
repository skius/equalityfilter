package com.nielssaurer.equalityfilter;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            // Set System L&F
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        UI ui = new UI();
        ui.start();
    }
}
