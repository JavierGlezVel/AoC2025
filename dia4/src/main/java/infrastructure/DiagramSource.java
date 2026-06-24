package infrastructure;

import java.io.IOException;
import java.util.List;

public interface DiagramSource {
    List<String> getLines() throws IOException;
}
