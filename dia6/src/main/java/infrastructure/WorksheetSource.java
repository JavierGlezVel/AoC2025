package infrastructure;

import java.io.IOException;
import java.util.List;

public interface WorksheetSource {
    List<String> getLines() throws IOException;
}
