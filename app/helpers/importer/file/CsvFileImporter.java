package helpers.importer.file;

import play.Logger;
import play.db.jpa.JPA;

import javax.persistence.Query;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;

/**
 * @author jtremeaux
 */
public class CsvFileImporter {
    private File f;

    private InputStream is;

    private String template;

    private Consumer<List<Object>> onNewLineCallback;

    private Query query;

    private LineMapper lineMapper = new LineMapper();

    private String separator;

    protected CsvFileImporter() {
        this.separator = ";";
    }

    private boolean log = true;

    /**
     * Skip the first line (typically a header)
     */
    private boolean skipFirstLine = true;

    public CsvFileImporter(File f) throws IOException {
        this(new FileInputStream(f));
        this.f = f;
    }

    public CsvFileImporter(InputStream is) throws IOException {
        this();
        this.is = is;
    }

    public CsvFileImporter setTemplate(String template) {
        this.template = template;
        return this;
    }

    public CsvFileImporter setOnNewLineCallback(Consumer<List<Object>> onNewLineCallback) {
        this.onNewLineCallback = onNewLineCallback;
        return this;
    }

    public CsvFileImporter setLog(boolean log) {
        this.log = log;
        return this;
    }

    public CsvFileImporter setSkipFirstLine(boolean skipFirstLine) {
        this.skipFirstLine = skipFirstLine;
        return this;
    }

    public void process() {
        List<List<String>> lines = parseCsvFile();
        int totalCount = 0;
        int importedCount = 0;
        if (template != null) {
            query = JPA.em().createNativeQuery(template);
        }
        for (List<String> line : lines) {
            List<Object> mappedLine = lineMapper.mapLine(line);
            try {
                importLine(mappedLine);
                importedCount++;
            } catch (Exception e) {
                Logger.warn(e, "Error importing CSV file");
            }
            totalCount++;
        }
        if (log) {
            Logger.info("Imported %d / %d lines in " + (f != null ? f.getName() : "CSV file"), importedCount, totalCount);
        }
    }

    private void importLine(List<Object> mappedLine) {
        if (query != null) {
            for (int i = 0; i < mappedLine.size(); i++) {
                query.setParameter(i + 1, mappedLine.get(i));
            }
            query.executeUpdate();
        } else if (onNewLineCallback != null) {
            onNewLineCallback.accept(mappedLine);
        } else {
            throw new RuntimeException("No callback or template defined");
        }
    }

    private List<List<String>> parseCsvFile() {
        List<List<String>> lines = new ArrayList<>();
        Scanner scanner = new Scanner(new InputStreamReader(is));
        if (skipFirstLine) {
            scanner.nextLine();
        }
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] split = line.split(separator);
            List<String> values = new ArrayList<>();
            for (String cell : split) {
                values.add(cell.trim());
            }
            lines.add(values);
        }
        return lines;
    }

    public CsvFileImporter setSeparator(String separator) {
        this.separator = separator;
        return this;
    }

    public LineMapper getLineMapper() {
        return lineMapper;
    }
}
