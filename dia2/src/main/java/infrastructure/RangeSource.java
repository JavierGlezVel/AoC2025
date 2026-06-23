package infrastructure;

import java.io.IOException;

public interface RangeSource {
    String getContent() throws IOException;
}
