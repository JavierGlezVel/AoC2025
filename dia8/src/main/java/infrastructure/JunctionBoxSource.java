package infrastructure;

import java.io.IOException;
import java.util.List;

public interface JunctionBoxSource {
    List<String> getLines() throws IOException;
}
