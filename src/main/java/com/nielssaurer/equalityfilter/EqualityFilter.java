package com.nielssaurer.equalityfilter;

import java.util.*;
import java.util.stream.Collectors;

public class EqualityFilter<V> {
    private Map<V, Integer> questionToId;
    private Map<V, Integer> questionToGroup;
    private Map<V, Collection<Integer>> questionNotEqualGroup;
    private int[][] notEqualAdjMatrix;
    private List<V> allQuestions;

    public EqualityFilter(V[] questions) {
        notEqualAdjMatrix = new int[questions.length][questions.length];
        questionToGroup = new HashMap<V, Integer>();
        questionToId = new HashMap<V, Integer>();
        questionNotEqualGroup = new HashMap<V, Collection<Integer>>();
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

    public Map<Integer, Collection<V>> getGroups() {
        Map<Integer, Collection<V>> groups = new HashMap<>();
        for (var entry : questionToGroup.entrySet()) {
            int group = entry.getValue();
            V question = entry.getKey();
            if (!groups.containsKey(group)) {
                groups.put(group, new HashSet<V>());
            }
            groups.get(group).add(question);
        }
        return groups;
    }

    public boolean isEqual(V question1, V question2) {
        return questionToGroup.get(question1).equals(questionToGroup.get(question2));
    }

    public boolean isNotEqual(V question1, V question2) {
        if (notEqualAdjMatrix[questionToId.get(question1)][questionToId.get(question2)] == 1) {
            // There is a not-equal edge between the two questions, they can't be equal
            return true;
        }
        // Check if any of question1's group is not equal to any of question2's group. Then memoize that result
        boolean areNotEqual = false;
        Outer:
        for (V equal1 : getEquals(question1)) {
            int id1 = questionToId.get(equal1);
            for (V equal2 : getEquals(question2)) {
                int id2 = questionToId.get(equal2);
                if (notEqualAdjMatrix[id1][id2] == 1) {
                    areNotEqual = true;
                    break Outer;
                }
            }
        }
        if (areNotEqual) {
            // Memoize result
            for (V equal1 : getEquals(question1)) {
                setNotEqual(equal1, getEquals(question2));
            }
        }
        return areNotEqual;
    }

    public List<V> getNotEquals(V question) {
        return allQuestions.stream().filter(q -> isNotEqual(question, q)).collect(Collectors.toList());
    }

    public List<V> getPossiblyEqual(V question) {
        // questions for which we don't know yet whether they're equal or not
        ArrayList<V> possiblyEquals = new ArrayList<>();
        possiblyEquals.addAll(allQuestions);
        // Remove questions that are for certain equal
        possiblyEquals.removeAll(getEquals(question));
        // Remove questions that are for certain not equal
        possiblyEquals.removeAll(getNotEquals(question));

        return possiblyEquals;
    }

    public void setNotEqual(V question, V[] notEquals) {
        setNotEqual(question, Arrays.asList(notEquals));
    }

    public void setNotEqual(V question, V notEqual) {
        setNotEqual(question, Collections.singletonList(notEqual));
    }

    public void setNotEqual(V question, List<V> notEquals) {
        setNotEqual(question, notEquals, new HashSet<>());
    }

    public void setNotEqual(V question, List<V> notEquals, Collection<V> ignores) {
        int id1 = questionToId.get(question);
        for (V notEqual : notEquals) {
            if (ignores.contains(notEqual)) {
                // We are already handling this in an ancestor of our call tree
                continue;
            }
            // For all questions that are equal to notEqual, set those not equal to question
            List<V> notEqualEquals = getEqualsStrict(notEqual);
            // ignore notEqual in subsequent call, because we've already set it to be not equal
            ignores.add(notEqual);
            setNotEqual(question, notEqualEquals, ignores);
            int id2 = questionToId.get(notEqual);
            notEqualAdjMatrix[id1][id2] = 1;
            notEqualAdjMatrix[id2][id1] = 1;
        }
    }

    public void setEqual(V question, V[] equals) {
        setEqual(question, Arrays.asList(equals));
    }

    public void setEqual(V question, List<V> equals) {
        int group = questionToGroup.get(question);
        for (V equal : equals) {
            // question is obviously equal to itself and already has the same group
            if (question.equals(equal))
                continue;

            // equal is already set to be equal, hence also all of its equals
            if (group == questionToGroup.get(equal))
                continue;

            // Recursively set all children of equal to question's group
            List<V> equalEquals = getEqualsStrict(equal);
            // Order is important here, to avoid ping-ponging we need to change equal's group before recursive call
            questionToGroup.put(equal, group);
            setEqual(question, equalEquals);
        }
    }

    public List<V> getEquals(V question) {
        List<V> strict = getEqualsStrict(question);
        // question is also equal to itself
        strict.add(question);
        return strict;
    }

    public List<V> getEqualsStrict(V question) {
        int wantedGroup = questionToGroup.get(question);
        List<V> equals = new ArrayList<>();
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
