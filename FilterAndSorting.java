import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;

public class FilterAndSorting {
    private static final int ASCENDING = 0;
    private static final int DESCENDING = 1;
    private static final int ORIGINAL = 2;

    private static Map<Integer, Integer> sortStates = new HashMap<>();
    private static ArrayList<Object[]> dataInitial;

    public static void SortSelected(JTable table) {
        initializeTable(table);
        table.getTableHeader().addMouseListener(new SortMouseListener(table));
    }

    private static void initializeTable(JTable table) {
        DefaultTableModel m = (DefaultTableModel) table.getModel();
        dataInitial = new ArrayList<>();
        for (int z = 0; z < m.getRowCount(); z++) {
            Object[] currentRow = new Object[m.getColumnCount()];
            for (int x = 0; x < m.getColumnCount(); x++) {
                currentRow[x] = m.getValueAt(z, x);
            }
            dataInitial.add(currentRow);
        }
    }

    static class SortMouseListener extends MouseAdapter {
        private final JTable table;

        SortMouseListener(JTable table) {
            this.table = table;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            int columnIndex = table.columnAtPoint(e.getPoint());
            if (columnIndex != -1) { // Check if a valid column was clicked
                int currentState = sortStates.getOrDefault(columnIndex, ORIGINAL);
                currentState = (currentState + 1) % 3; // Cycle through ASCENDING, DESCENDING, ORIGINAL

                // Update sort state for the column
                sortStates.put(columnIndex, currentState);

                DefaultTableModel currentModel = (DefaultTableModel) table.getModel();
                ArrayList<Object[]> data = new ArrayList<>(dataInitial);

                // Apply sorting based on all columns' sort states
                data.sort(new MultiColumnComparator(sortStates));

                // Update the table with sorted data
                currentModel.setRowCount(0);
                for (Object[] r : data) {
                    currentModel.addRow(r);
                }
            }
        }
    }

    static class MultiColumnComparator implements Comparator<Object[]> {
        private final Map<Integer, Integer> sortStates;

        MultiColumnComparator(Map<Integer, Integer> sortStates) {
            this.sortStates = sortStates;
        }

        @Override
        public int compare(Object[] o1, Object[] o2) {
            for (Map.Entry<Integer, Integer> entry : sortStates.entrySet()) {
                int columnIndex = entry.getKey();
                int sortOrder = entry.getValue();

                @SuppressWarnings("unchecked")
                Comparable<Object> value1 = (Comparable<Object>) o1[columnIndex];
                @SuppressWarnings("unchecked")
                Comparable<Object> value2 = (Comparable<Object>) o2[columnIndex];

                int result = value1.compareTo(value2);
                if (result != 0) {
                    return sortOrder == ASCENDING ? result : sortOrder == DESCENDING ? -result : 0;
                }
            }
            return 0; // Default: no difference
        }
    }
}