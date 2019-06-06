package javax0.geci.api;

import java.util.List;
import java.util.Set;

/**
 * A {@code Segment} object represents an editor-fold part in the source file that the code generator can
 * write. A {@code Segment} is retrieved by the code generator calling the {@link Source#open(String)} method
 * of the source and it can write into the segment.
 */
public interface Segment extends AutoCloseable {

    /**
     * Get the lines of the segment as it is at the moment. This is the text that was written into the segment by the
     * code generator. There is no way to get the content of a segment that was in the file before the generator
     * started it's work. The generator should not depend on the code that is inside the generated segment prior
     * its work started.
     *
     * @return the textual content of the segment as it is at the moment.
     */
    String getContent();

    /**
     * When the generator does not find a segment based on the starting
     * and ending line it may decide to insert the segment towards the
     * end of the file. For example in case of Java the generator (in
     * class {@link Source} can insert the segment before the last
     * closing '}' character.
     *
     * <p>In that case, however, the segment start and segment end lines
     * should also be inserted to the code, as they are not part of the
     * source yet. When such a segment is created {@code preface} should
     * contain the segment start line and {@code postface} should
     * contain the lines that are the end of the segment.
     *
     * <p>When the code generation runs next time these lines will
     * already be recognized. This is to ease the work of the developer
     * inserting the segment start and end when the code generator runs
     * the first time.
     *
     * @param preface the preface lines
     */
    void setPreface(String ... preface);

    /**
     * See the documentation of {@link #setPreface(String...)}
     * @param postface the postface lines that end a segment
     */
    void setPostface(String ... postface);

    /**
     * Set the content of the segment.
     * <p>
     * The intended use of this method along with the getter is to let a code write a segment that contains
     * macro placeholders and in the last step replace the collected and formatted content with the one that
     * resolves the placeholders.
     *
     * @param content the new content of the segment.
     */
    void setContent(String content);

    /**
     * Write a line to the segment after the last line.
     *
     * @param s          the content of the line. Nothing is printed if {@code null}.
     * @param parameters parameters that are used as actual values in the {@code s} format string
     * @return {@code this}
     */
    Segment write(String s, Object... parameters);

    /**
     * Write all the lines of the segment into this segment.
     *
     * @param segment the other segment that contains lines that were created beforehand
     * @return {@code this}
     */
    Segment write(Segment segment);

    /**
     * Insert a new line into the segment.
     * @return {@code this}
     */
    Segment newline();

    /**
     * Define a parameter that can be used in the write methods.
     * @param keyValuePairs the keys and values that can be used in the first parameter of the write method between {{ and }}
     * @return {@code this}
     */
    Segment param(String ... keyValuePairs);

    Set<String> paramKeySet();


    /**
     * Delete the previously defined parameters.
     */
    void resetParams();

    /**
     * Write a line into the segment after the last line and increase the indenting for the coming lines.
     * Usually you use this method when the line ends with a '{' character.
     *
     * @param s          the content of the line. Nothing is printed if {@code null}.
     * @param parameters parameters that are used as actual values in the {@code s} format string
     * @return {@code this}
     */
    Segment write_r(String s, Object... parameters);

    default Segment _r(String s, Object... parameters) {
        return write_r(s, parameters);
    }

    /**
     * Write a line into the segment after the last line and after decreasing the indenting. Usually you
     * use this method to put the '}' at the end of the code blocks.
     *
     * @param s          the content of the line. Nothing is printed if {@code null}.
     * @param parameters parameters that are used as actual values in the {@code s} format string
     * @return {@code this}
     */
    Segment write_l(String s, Object... parameters);

    default Segment _l(String s, Object... parameters) {
        return write_l(s, parameters);
    }

    /**
     * Usually this method is not implemented separately and is needed for to be auto closeable so that the segment
     * can be used in a try-with-resources block.
     */
    default void close() {
    }
}
