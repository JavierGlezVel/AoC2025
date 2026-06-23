package infrastructure;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileRangeSource implements RangeSource {
    private final String path;

    public FileRangeSource(String path) {
        this.path = path;
    }

    @Override
    public String getContent() throws IOException {
        return Files.readString(Path.of(path));
    }
}
