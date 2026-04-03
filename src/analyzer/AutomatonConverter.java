package analyzer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * NFA->DFA 与 DFA->MFA 转换工具。
 */
public class AutomatonConverter {

    public Automaton nfaToDfa(Automaton nfa) {
        Automaton dfa = new Automaton();
        List<String> alphabet = new ArrayList<>(nfa.getAlphabet());
        alphabet.sort(Comparator.naturalOrder());

        Set<Integer> startClosure = nfa.epsilonClosure(Set.of(nfa.getStartState()));
        Map<Set<Integer>, Integer> stateIdMap = new LinkedHashMap<>();
        List<Set<Integer>> queue = new ArrayList<>();

        int startId = 0;
        stateIdMap.put(new LinkedHashSet<>(startClosure), startId);
        queue.add(new LinkedHashSet<>(startClosure));
        dfa.setStartState(startId);
        if (containsAccept(startClosure, nfa.getAcceptStates())) {
            dfa.addAcceptState(startId);
        }

        int index = 0;
        while (index < queue.size()) {
            Set<Integer> current = queue.get(index++);
            int fromId = stateIdMap.get(current);

            for (String symbol : alphabet) {
                Set<Integer> moved = nfa.move(current, symbol);
                if (moved.isEmpty()) {
                    continue;
                }
                Set<Integer> closure = nfa.epsilonClosure(moved);
                Set<Integer> key = new LinkedHashSet<>(closure);

                if (!stateIdMap.containsKey(key)) {
                    int id = stateIdMap.size();
                    stateIdMap.put(key, id);
                    queue.add(key);
                    if (containsAccept(key, nfa.getAcceptStates())) {
                        dfa.addAcceptState(id);
                    }
                }

                int toId = stateIdMap.get(key);
                dfa.addTransition(fromId, symbol, toId);
            }
        }

        return dfa;
    }

    public Automaton dfaToMfa(Automaton dfa) {
        List<Integer> states = new ArrayList<>(dfa.getStates());
        states.sort(Comparator.naturalOrder());
        List<String> alphabet = new ArrayList<>(dfa.getAlphabet());
        alphabet.sort(Comparator.naturalOrder());

        Set<Integer> accepting = new LinkedHashSet<>(dfa.getAcceptStates());
        Set<Integer> nonAccepting = new LinkedHashSet<>(states);
        nonAccepting.removeAll(accepting);

        List<Set<Integer>> partitions = new ArrayList<>();
        if (!nonAccepting.isEmpty()) {
            partitions.add(nonAccepting);
        }
        if (!accepting.isEmpty()) {
            partitions.add(accepting);
        }

        boolean changed;
        do {
            changed = false;
            List<Set<Integer>> newPartitions = new ArrayList<>();

            for (Set<Integer> part : partitions) {
                Map<String, Set<Integer>> grouped = new HashMap<>();
                for (Integer state : part) {
                    String signature = buildSignature(state, partitions, dfa, alphabet);
                    grouped.computeIfAbsent(signature, k -> new LinkedHashSet<>()).add(state);
                }
                newPartitions.addAll(grouped.values());
                if (grouped.size() > 1) {
                    changed = true;
                }
            }
            partitions = newPartitions;
        } while (changed);

        return buildMinimizedDfa(partitions, dfa, alphabet);
    }

    private String buildSignature(int state,
                                  List<Set<Integer>> partitions,
                                  Automaton dfa,
                                  List<String> alphabet) {
        StringBuilder sig = new StringBuilder();
        for (String symbol : alphabet) {
            Integer target = firstOrNull(dfa.getTransitions()
                    .getOrDefault(state, Map.of())
                    .getOrDefault(symbol, Set.of()));
            int group = findPartitionIndex(target, partitions);
            sig.append(symbol).append(':').append(group).append(';');
        }
        return sig.toString();
    }

    private int findPartitionIndex(Integer target, List<Set<Integer>> partitions) {
        if (target == null) {
            return -1;
        }
        for (int i = 0; i < partitions.size(); i++) {
            if (partitions.get(i).contains(target)) {
                return i;
            }
        }
        return -1;
    }

    private Integer firstOrNull(Set<Integer> set) {
        return set.stream().findFirst().orElse(null);
    }

    private Automaton buildMinimizedDfa(List<Set<Integer>> partitions, Automaton dfa, List<String> alphabet) {
        Automaton mfa = new Automaton();

        Map<Integer, Integer> oldToNew = new HashMap<>();
        for (int i = 0; i < partitions.size(); i++) {
            for (Integer oldState : partitions.get(i)) {
                oldToNew.put(oldState, i);
            }
        }

        mfa.setStartState(oldToNew.get(dfa.getStartState()));
        for (Integer accept : dfa.getAcceptStates()) {
            mfa.addAcceptState(oldToNew.get(accept));
        }

        for (int i = 0; i < partitions.size(); i++) {
            Integer representative = partitions.get(i).iterator().next();
            for (String symbol : alphabet) {
                Integer target = firstOrNull(dfa.getTransitions()
                        .getOrDefault(representative, Map.of())
                        .getOrDefault(symbol, Set.of()));
                if (target != null) {
                    mfa.addTransition(i, symbol, oldToNew.get(target));
                }
            }
        }

        return mfa;
    }

    private boolean containsAccept(Set<Integer> states, Set<Integer> acceptStates) {
        for (Integer s : states) {
            if (acceptStates.contains(s)) {
                return true;
            }
        }
        return false;
    }
}
