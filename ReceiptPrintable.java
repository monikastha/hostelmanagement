package Admin;
import java.awt.*;
import java.awt.print.*;
import java.util.ArrayList;
import java.util.List;

class ReceiptPrintable implements Printable {

    private List<String> lines;
    private final int FONT_SIZE = 12;

    public ReceiptPrintable(String receiptText) {
        this.lines = splitTextIntoLines(receiptText);
    }

    private List<String> splitTextIntoLines(String text) {
        List<String> lines = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (c == '\n') {
                lines.add(currentLine.toString());
                currentLine.setLength(0); // Clear the StringBuilder
            } else {
                currentLine.append(c);
            }
        }
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString()); // Add the last line
        }
        return lines;
    }

    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if (pageIndex > 0) {
            return NO_SUCH_PAGE; // Only one page
        }

        Graphics2D g2d = (Graphics2D) graphics;
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

        // Set font
        Font font = new Font("Monospaced", Font.PLAIN, FONT_SIZE);
        g2d.setFont(font);
        FontMetrics metrics = g2d.getFontMetrics(font);
        int lineHeight = metrics.getHeight();

        int x = 0;
        int y = lineHeight; // Start at the first line

        for (String line : lines) {
            g2d.drawString(line, x, y);
            y += lineHeight;
        }

        return PAGE_EXISTS;
    }
}