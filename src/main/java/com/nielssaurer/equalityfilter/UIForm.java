package com.nielssaurer.equalityfilter;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UIForm {
    private JLabel currentQuestionLbl;
    private JList<String> notEqualList;
    private JList<String> equalList;
    public JPanel mainPanel;
    private JButton moveToEqualBtn;
    private JButton moveToNotEqualBtn;
    private JTextArea newQuestionsText;
    private JButton addQuestionsBtn;
    private JButton nextQuestionBtn;
    private JSplitPane splitPane;

    private DefaultListModel<String> equalListModel;
    private DefaultListModel<String> notEqualListModel;

    private EqualityFilter<String> filter;
    private List<String> questionsToProcess;
    private String currQuestion;

    private boolean firstResize = true;

    public UIForm() {
        equalListModel = new DefaultListModel<>();
        notEqualListModel = new DefaultListModel<>();
        equalList.setModel(equalListModel);
        notEqualList.setModel(notEqualListModel);

        Border inputBorder = BorderFactory.createLineBorder(Color.BLACK, 1);
        newQuestionsText.setBorder(inputBorder);

        addQuestionsBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = newQuestionsText.getText();
                String[] questions = text.split("\n");

                // Need modifiable list
                questionsToProcess = new ArrayList<>(Arrays.asList(questions));
                filter = new EqualityFilter<>(questions);
                setNewQuestion(questions[0]);
            }
        });

        nextQuestionBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filter.setEqual(currQuestion, Collections.list(equalListModel.elements()));
                filter.setNotEqual(currQuestion, Collections.list(notEqualListModel.elements()));

                notEqualListModel.clear();
                equalListModel.clear();

                // Loop until we find a question with a non-empty possibly-equal set
                while (questionsToProcess.size() > 0 && notEqualListModel.isEmpty()) {
                    questionsToProcess.remove(0);

                    if (questionsToProcess.size() > 0) {
                        setNewQuestion(questionsToProcess.get(0));
                    }
                }
                if (questionsToProcess.isEmpty()) {
                    System.out.println("Finished! Got the following information:");
                    System.out.println(filter.getGroups());
                    System.exit(1);
                }
            }
        });

        moveToEqualBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<String> toMove = notEqualList.getSelectedValuesList();
                toMove.forEach(q -> notEqualListModel.removeElement(q));
                equalListModel.addAll(toMove);
            }
        });

        moveToNotEqualBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<String> toMove = equalList.getSelectedValuesList();
                toMove.forEach(q -> equalListModel.removeElement(q));
                notEqualListModel.addAll(toMove);
            }
        });
        mainPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                if (firstResize) {
                    splitPane.setDividerLocation(0.5);
                }
                firstResize = false;
            }
        });
    }

    public void setNewQuestion(String question) {
        currQuestion = question;
        currentQuestionLbl.setText(question);
        updateLists();
    }

    public void updateLists() {
        equalListModel.clear();
        notEqualListModel.clear();
        List<String> possiblyEqual = filter.getPossiblyEqual(currQuestion);
        System.out.println(possiblyEqual);
        notEqualListModel.addAll(possiblyEqual);
        System.out.println(notEqualList.getModel().getSize());
    }
}
