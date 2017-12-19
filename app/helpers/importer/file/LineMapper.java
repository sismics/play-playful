package helpers.importer.file;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jtremeaux
 */
public class LineMapper {
    private List<Class> columnMapper;

    public List<Object> mapLine(List<String> line) {
        // Map each column
        List<Object> mapped = new ArrayList<>();
        for (int i = 0; i < line.size(); i++) {
            String value = line.get(i);
            if (value != null) {
                value = value.trim();
            }
            mapped.add(mapField(i, value));
        }

        // Add missing columns
//        if (columnMapper != null) {
//            for (int i = 0; i < columnMapper.size() - line.size(); i++) {
//                mapped.add(mapField(i, ""));
//            }
//        }

        return mapped;
    }

    public Object mapField(int pos, String value) {
        if (columnMapper == null || pos >= columnMapper.size()) {
            return value;
        }
        Class clazz = columnMapper.get(pos);
        if (clazz == Double.class) {
            if (value.isEmpty()) {
                return null;
            }
            return Double.valueOf(value);
        } else if (clazz == Integer.class) {
            if (value.isEmpty()) {
                return null;
            }
            return Double.valueOf(value).intValue();
        } else if (clazz == Boolean.class) {
            if (value.isEmpty()) {
                return false;
            }
            if ("yes".equalsIgnoreCase(value)) {
                return true;
            }
            try {
                return Double.valueOf(value).intValue() == 1;
            } catch (Exception e) {
                // NOP
            }
            return false;
        }
        return value;
    }

    public static String unquote(String s) {
        return s.substring(1, s.length() - 1);
    }

    public void setColumnMapper(List<Class> columnMapper) {
        this.columnMapper = columnMapper;
    }
}
