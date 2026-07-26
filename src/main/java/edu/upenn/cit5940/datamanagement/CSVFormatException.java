package edu.upenn.cit5940.datamanagement;
/**
 * @author nanzheng
 */

public class CSVFormatException extends Exception {
    private final int line;
    private final int column;
    private final int row;
    private final int field;

    /**
     * Constructs a new exception with no message or extra values.
     */
    public CSVFormatException() {
        super();
        this.line = -1;
        this.column = -1;
        this.row = -1;
        this.field = -1;
    }

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param  message the detail message (which is saved for later retrieval
     *         by the {@link #getMessage()} method).
     * @param  line the line number in the input file with the syntax error.
     * @param  column the character within the current line in the input file
     *         with the syntax error.
     * @param  row the index current row being processed.
     * @param  field the index of the current field within the current row.
     */
    public CSVFormatException(String message, int line, int column, int row, int field) {
        super((message == null ? "" : message + ": ") + "error at " + line + ", " + column + ", " + row + ", " + field);
        this.line = line;
        this.column = column;
        this.row = row;
        this.field = field;
    }

    /**
     * Constructs a new exception with a default message.
     *
     * @param  line the line number in the input file with the syntax error.
     * @param  column the character within the current line in the input file
     *         with the syntax error.
     * @param  row the index current row being processed.
     * @param  field the index of the current field within the current row.
     */
    public CSVFormatException(int line, int column, int row, int field) {
        this(null, line, column, row, field);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof CSVFormatException;
    }

    public int hashCode() {
        return java.util.Objects.hash(line, column, row, field);
    }
}
