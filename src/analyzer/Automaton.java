package analyzer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 自动机基础数据结构。
 */
public class Automaton {
    public static final String EPSILON = "ε";

    private final Set<Integer> states = new HashSet<>();
    private final Set<String> alphabet = new HashSet<>();
    private final Map<Integer, Map<String, Set<Integer>>> transitions = new HashMap<>();
    private int startState = -1;
    private final Set<Integer> acceptStates = new HashSet<>();

    public Set<Integer> getStates() {
        return states;
    }

    public Set<String> getAlphabet() {
        return alphabet;
    }

    public Map<Integer, Map<String, Set<Integer>>> getTransitions() {
        return transitions;
    }

    public int getStartState() {
        return startState;
    }

    public void setStartState(int startState) {
        this.startState = startState;
        states.add(startState);
    }

    public Set<Integer> getAcceptStates() {
        return acceptStates;
    }

    public void addAcceptState(int state) {
        acceptStates.add(state);
        states.add(state);
    }

    public void addTransition(int from, String symbol, int to) {
        states.add(from);
        states.add(to);
        if (!EPSILON.equals(symbol)) {
            alphabet.add(symbol);
        }
        transitions.computeIfAbsent(from, k -> new HashMap<>())
                .computeIfAbsent(symbol, k -> new HashSet<>())
                .add(to);
    }

    public Set<Integer> move(Set<Integer> fromStates, String symbol) {
        Set<Integer> result = new HashSet<>();
        for (Integer s : fromStates) {
            Map<String, Set<Integer>> out = transitions.getOrDefault(s, Map.of());
            result.addAll(out.getOrDefault(symbol, Set.of()));
        }
        return result;
    }

    public Set<Integer> epsilonClosure(Set<Integer> input) {
        Set<Integer> closure = new HashSet<>(input);
        List<Integer> stack = new ArrayList<>(input);
        while (!stack.isEmpty()) {
            int state = stack.remove(stack.size() - 1);
            Set<Integer> epsNext = transitions.getOrDefault(state, Map.of()).getOrDefault(EPSILON, Set.of());
            for (Integer next : epsNext) {
                if (closure.add(next)) {
                    stack.add(next);
                }
            }
        }
        return closure;
    }

    public List<String[]> toTransitionRows() {
        List<String[]> rows = new ArrayList<>();
        List<Integer> sortedStates = new ArrayList<>(states);
        sortedStates.sort(Comparator.naturalOrder());

        for (Integer from : sortedStates) {
            Map<String, Set<Integer>> symbolMap = transitions.getOrDefault(from, Map.of());
            List<String> symbols = new ArrayList<>(symbolMap.keySet());
            symbols.sort(Comparator.naturalOrder());
            for (String symbol : symbols) {
                List<Integer> toList = new ArrayList<>(symbolMap.get(symbol));
                toList.sort(Comparator.naturalOrder());
                for (Integer to : toList) {
                    rows.add(new String[]{String.valueOf(from), symbol, String.valueOf(to)});
                }
            }
        }
        return rows;
    }
}
