package analyzer;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 自动机文件读写：
 * START:x
 * ACCEPT:a,b,c
 * TRANS:
 * from,symbol,to
 */
public final class AutomatonFileUtil {
    private AutomatonFileUtil() {
    }

    public static void save(Automaton automaton, File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("START:").append(automaton.getStartState()).append('\n');
        sb.append("ACCEPT:");
        boolean first = true;
        for (Integer accept : automaton.getAcceptStates()) {
            if (!first) {
                sb.append(',');
            }
            sb.append(accept);
            first = false;
        }
        sb.append('\n');
        sb.append("TRANS:\n");
        for (String[] row : automaton.toTransitionRows()) {
            sb.append(row[0]).append(',').append(row[1]).append(',').append(row[2]).append('\n');
        }
        FileUtil.writeText(file, sb.toString());
    }

    public static Automaton load(File file) throws IOException {
        List<String> lines = FileUtil.readText(file).lines().toList();
        Automaton automaton = new Automaton();

        boolean inTrans = false;
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (trimmed.startsWith("START:")) {
                automaton.setStartState(Integer.parseInt(trimmed.substring("START:".length()).trim()));
            } else if (trimmed.startsWith("ACCEPT:")) {
                String part = trimmed.substring("ACCEPT:".length()).trim();
                if (!part.isEmpty()) {
                    for (String s : part.split(",")) {
                        automaton.addAcceptState(Integer.parseInt(s.trim()));
                    }
                }
            } else if (trimmed.equals("TRANS:")) {
                inTrans = true;
            } else if (inTrans) {
                String[] arr = trimmed.split(",");
                if (arr.length == 3) {
                    automaton.addTransition(
                            Integer.parseInt(arr[0].trim()),
                            arr[1].trim(),
                            Integer.parseInt(arr[2].trim())
                    );
                }
            }
        }

        return automaton;
    }
}
