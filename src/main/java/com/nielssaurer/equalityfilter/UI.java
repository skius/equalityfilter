package com.nielssaurer.equalityfilter;

import javax.swing.*;

public class UI {

    private final JFrame frame;

    public UI() {
        frame = new JFrame("Equality Filter");
        frame.setContentPane(new UIForm().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setSize(500, 600);


    }

    public void start() {
        frame.setVisible(true);
    }
}
