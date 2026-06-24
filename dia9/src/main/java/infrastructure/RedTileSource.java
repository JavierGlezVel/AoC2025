package infrastructure;

import java.io.IOException;
import java.util.List;

public interface RedTileSource {
    List<String> getLines() throws IOException;
}
