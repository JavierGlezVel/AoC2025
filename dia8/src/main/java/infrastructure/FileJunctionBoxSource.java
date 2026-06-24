package infrastructure;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FileJunctionBoxSource implements JunctionBoxSource {
    private final String path;

    public FileJunctionBoxSource(String path) {
        this.path = path;
    }

    @Override
    public List<String> getLines() throws IOException {
        return Files.readAllLines(Path.of(path));
    }
}
