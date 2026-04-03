package analyzer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * 基于 Thompson 构造：正则式 -> NFA。
 * 支持字符、括号、并集 |、连接、闭包 *。
 */
public class RegexToNfaConverter {
    private int stateCounter;

    public boolean isValidRegex(String regex) {
        if (regex == null || regex.isBlank()) {
            return false;
        }
        int balance = 0;
        for (char c : regex.toCharArray()) {
            if (c == '(') {
                balance++;
            } else if (c == ')') {
                balance--;
                if (balance < 0) {
                    return false;
                }
            }
        }
        return balance == 0;
    }

    public Automaton convert(String regex) {
        if (!isValidRegex(regex)) {
            throw new IllegalArgumentException("正则式不合法");
        }
        stateCounter = 0;
        String concatRegex = addConcatOperator(regex.replaceAll("\\s+", ""));
        String postfix = toPostfix(concatRegex);
        return buildNfaFromPostfix(postfix);
    }

    private String addConcatOperator(String regex) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < regex.length(); i++) {
            char c1 = regex.charAt(i);
            sb.append(c1);
            if (i == regex.length() - 1) {
                continue;
            }
            char c2 = regex.charAt(i + 1);
            if (needsConcat(c1, c2)) {
                sb.append('.');
            }
        }
        return sb.toString();
    }

    private boolean needsConcat(char c1, char c2) {
        boolean left = isSymbol(c1) || c1 == ')' || c1 == '*';
        boolean right = isSymbol(c2) || c2 == '(';
        return left && right;
    }

    private boolean isSymbol(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    private int precedence(char op) {
        return switch (op) {
            case '*' -> 3;
            case '.' -> 2;
            case '|' -> 1;
            default -> 0;
        };
    }

    private String toPostfix(String regex) {
        StringBuilder output = new StringBuilder();
        Deque<Character> ops = new ArrayDeque<>();

        for (char c : regex.toCharArray()) {
            if (isSymbol(c)) {
                output.append(c);
            } else if (c == '(') {
                ops.push(c);
            } else if (c == ')') {
                while (!ops.isEmpty() && ops.peek() != '(') {
                    output.append(ops.pop());
                }
                if (!ops.isEmpty() && ops.peek() == '(') {
                    ops.pop();
                }
            } else {
                while (!ops.isEmpty() && precedence(ops.peek()) >= precedence(c)) {
                    output.append(ops.pop());
                }
                ops.push(c);
            }
        }

        while (!ops.isEmpty()) {
            output.append(ops.pop());
        }
        return output.toString();
    }

    private Automaton buildNfaFromPostfix(String postfix) {
        Deque<Fragment> stack = new ArrayDeque<>();

        for (char token : postfix.toCharArray()) {
            if (isSymbol(token)) {
                int start = newState();
                int end = newState();
                Automaton nfa = new Automaton();
                nfa.setStartState(start);
                nfa.addAcceptState(end);
                nfa.addTransition(start, String.valueOf(token), end);
                stack.push(new Fragment(nfa, start, end));
            } else if (token == '.') {
                Fragment right = stack.pop();
                Fragment left = stack.pop();
                Automaton merged = mergeAutomata(left.nfa, right.nfa);
                merged.addTransition(left.end, Automaton.EPSILON, right.start);
                merged.getAcceptStates().clear();
                merged.addAcceptState(right.end);
                stack.push(new Fragment(merged, left.start, right.end));
            } else if (token == '|') {
                Fragment right = stack.pop();
                Fragment left = stack.pop();
                Automaton merged = mergeAutomata(left.nfa, right.nfa);
                int newStart = newState();
                int newEnd = newState();
                merged.setStartState(newStart);
                merged.getAcceptStates().clear();
                merged.addAcceptState(newEnd);
                merged.addTransition(newStart, Automaton.EPSILON, left.start);
                merged.addTransition(newStart, Automaton.EPSILON, right.start);
                merged.addTransition(left.end, Automaton.EPSILON, newEnd);
                merged.addTransition(right.end, Automaton.EPSILON, newEnd);
                stack.push(new Fragment(merged, newStart, newEnd));
            } else if (token == '*') {
                Fragment item = stack.pop();
                Automaton nfa = item.nfa;
                int newStart = newState();
                int newEnd = newState();
                nfa.setStartState(newStart);
                nfa.getAcceptStates().clear();
                nfa.addAcceptState(newEnd);
                nfa.addTransition(newStart, Automaton.EPSILON, item.start);
                nfa.addTransition(newStart, Automaton.EPSILON, newEnd);
                nfa.addTransition(item.end, Automaton.EPSILON, item.start);
                nfa.addTransition(item.end, Automaton.EPSILON, newEnd);
                stack.push(new Fragment(nfa, newStart, newEnd));
            }
        }

        if (stack.size() != 1) {
            throw new IllegalArgumentException("正则式无法构建 NFA");
        }
        return stack.pop().nfa;
    }

    private Automaton mergeAutomata(Automaton a, Automaton b) {
        Automaton merged = new Automaton();
        copyInto(a, merged);
        copyInto(b, merged);
        return merged;
    }

    private void copyInto(Automaton source, Automaton target) {
        for (String[] row : source.toTransitionRows()) {
            target.addTransition(Integer.parseInt(row[0]), row[1], Integer.parseInt(row[2]));
        }
        if (target.getStartState() < 0) {
            target.setStartState(source.getStartState());
        }
        for (Integer accept : source.getAcceptStates()) {
            target.addAcceptState(accept);
        }
    }

    private int newState() {
        return stateCounter++;
    }

    private record Fragment(Automaton nfa, int start, int end) {
    }
}
