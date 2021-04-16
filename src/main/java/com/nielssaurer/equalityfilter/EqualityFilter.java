package com.nielssaurer.equalityfilter;

import java.util.*;
import java.util.stream.Collectors;

public class EqualityFilter {
    private Map<String, Integer> questionToId;
    private Map<String, Integer> questionToGroup;
    private Map<String, Collection<Integer>> questionNotEqualGroup;
    private int[][] notEqualAdjMatrix;
    private List<String> allQuestions;

    public EqualityFilter(String[] questions) {
        notEqualAdjMatrix = new int[questions.length][questions.length];
        questionToGroup = new HashMap<String, Integer>();
        questionToId = new HashMap<String, Integer>();
        questionNotEqualGroup = new HashMap<String, Collection<Integer>>();
        for (int i = 0; i < questions.length; i++) {
            // We know for sure questions are equal to themselves
            questionToGroup.put(questions[i], i);
            // Id of a question is its index
            questionToId.put(questions[i], i);
            // We don't know for sure which other question a question is not equal to
            questionNotEqualGroup.put(questions[i], new HashSet<Integer>());
        }
        allQuestions = Arrays.asList(questions);
    }

    public Map<Integer, Collection<String>> getGroups() {
        Map<Integer, Collection<String>> groups = new HashMap<>();
        for (var entry : questionToGroup.entrySet()) {
            int group = entry.getValue();
            String question = entry.getKey();
            if (!groups.containsKey(group)) {
                groups.put(group, new HashSet<String>());
            }
            groups.get(group).add(question);
        }
        return groups;
    }

    public boolean isEqual(String question1, String question2) {
        return questionToGroup.get(question1) == questionToGroup.get(question2);
    }

    public boolean isNotEqual(String question1, String question2) {
        if (notEqualAdjMatrix[questionToId.get(question1)][questionToId.get(question2)] == 1) {
            // There is a not-equal edge between the two questions, they can't be equal
            return true;
        }
        // Check if any of question1's group is not equal to any of question2's group. Then memoize that result
        boolean areNotEqual = false;
        Outer:
        for (String equal1 : getEquals(question1)) {
            int id1 = questionToId.get(equal1);
            for (String equal2 : getEquals(question2)) {
                int id2 = questionToId.get(equal2);
                if (notEqualAdjMatrix[id1][id2] == 1) {
                    areNotEqual = true;
                    break Outer;
                }
            }
        }
        if (areNotEqual) {
            // Memoize result
            for (String equal1 : getEquals(question1)) {
                setNotEqual(equal1, getEquals(question2));
            }
        }
        return areNotEqual;
    }

    public List<String> getNotEquals(String question) {
        return allQuestions.stream().filter(q -> isNotEqual(question, q)).collect(Collectors.toList());
    }

    public List<String> getPossiblyEqual(String question) {
        // questions for which we don't know yet whether they're equal or not
        ArrayList<String> possiblyEquals = new ArrayList<>();
        possiblyEquals.addAll(allQuestions);
        // Remove questions that are for certain equal
        possiblyEquals.removeAll(getEquals(question));
        // Remove questions that are for certain not equal
        possiblyEquals.removeAll(getNotEquals(question));

        return possiblyEquals;
    }

    public void setNotEqual(String question, String[] notEquals) {
        setNotEqual(question, Arrays.asList(notEquals));
    }

    public void setNotEqual(String question, String notEqual) {
        setNotEqual(question, Collections.singletonList(notEqual));
    }

    public void setNotEqual(String question, List<String> notEquals) {
        setNotEqual(question, notEquals, new HashSet<>());
    }

    public void setNotEqual(String question, List<String> notEquals, Collection<String> ignores) {
        int id1 = questionToId.get(question);
        for (String notEqual : notEquals) {
            if (ignores.contains(notEqual)) {
                // We are already handling this in an ancestor of our call tree
                continue;
            }
            // For all questions that are equal to notEqual, set those not equal to question
            List<String> notEqualEquals = getEqualsStrict(notEqual);
            // ignore notEqual in subsequent call, because we've already set it to be not equal
            ignores.add(notEqual);
            setNotEqual(question, notEqualEquals, ignores);
            int id2 = questionToId.get(notEqual);
            notEqualAdjMatrix[id1][id2] = 1;
            notEqualAdjMatrix[id2][id1] = 1;
        }
    }

    public void setEqual(String question, String[] equals) {
        setEqual(question, Arrays.asList(equals));
    }

    public void setEqual(String question, List<String> equals) {
        int group = questionToGroup.get(question);
        for (String equal : equals) {
            // question is obviously equal to itself and already has the same group
            if (question.equals(equal))
                continue;

            // equal is already set to be equal, hence also all of its equals
            if (group == questionToGroup.get(equal))
                continue;

            // Recursively set all children of equal to question's group
            List<String> equalEquals = getEqualsStrict(equal);
            // Order is important here, to avoid ping-ponging we need to change equal's group before recursive call
            questionToGroup.put(equal, group);
            setEqual(question, equalEquals);
        }
    }

    public List<String> getEquals(String question) {
        List<String> strict = getEqualsStrict(question);
        // question is also equal to itself
        strict.add(question);
        return strict;
    }

    public List<String> getEqualsStrict(String question) {
        int wantedGroup = questionToGroup.get(question);
        List<String> equals = new ArrayList<>();
        for (var entry : questionToGroup.entrySet()) {
            if (entry.getKey().equals(question)) {
                // Don't add question itself to equals
                continue;
            }
            if (entry.getValue() == wantedGroup) {
                equals.add(entry.getKey());
            }
        }
        return equals;
    }
}
