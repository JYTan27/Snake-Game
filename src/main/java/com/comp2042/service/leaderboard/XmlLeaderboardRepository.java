package com.comp2042.service.leaderboard;

import com.comp2042.model.stats.RunOutcome;
import com.comp2042.model.stats.RunResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import org.w3c.dom.Element;

/** Versioned local storage. Failed reads never modify the original file. */
public final class XmlLeaderboardRepository implements LeaderboardRepository {
    private static final String VERSION = "1";
    private final Path file;

    public XmlLeaderboardRepository(Path file) {
        this.file = Objects.requireNonNull(file).toAbsolutePath();
    }

    public static Path defaultPath() {
        String local = System.getenv("LOCALAPPDATA");
        return local != null && !local.isBlank()
                ? Path.of(local, "AdvancedSnake", "leaderboard.xml")
                : Path.of(System.getProperty("user.home"), ".advanced-snake", "leaderboard.xml");
    }

    @Override
    public List<RunResult> load() throws IOException {
        if (Files.notExists(file)) return List.of();
        try (InputStream input = Files.newInputStream(file)) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            Element root = factory.newDocumentBuilder().parse(input).getDocumentElement();
            if (!root.getTagName().equals("leaderboard") || !VERSION.equals(root.getAttribute("version"))) {
                throw new IOException("Unsupported leaderboard format or version");
            }
            List<RunResult> results = new ArrayList<>();
            HashSet<UUID> ids = new HashSet<>();
            var children = root.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                if (!(children.item(i) instanceof Element row) || !row.getTagName().equals("run")) continue;
                RunResult result = new RunResult(UUID.fromString(row.getAttribute("id")),
                        row.getAttribute("mode"), Integer.parseInt(row.getAttribute("score")),
                        RunOutcome.valueOf(row.getAttribute("outcome")),
                        Long.parseLong(row.getAttribute("activeTimeMillis")),
                        Integer.parseInt(row.getAttribute("snakeLength")),
                        Instant.parse(row.getAttribute("recordedAt")));
                if (!ids.add(result.id())) throw new IOException("Duplicate leaderboard result ID");
                results.add(result);
            }
            return List.copyOf(results);
        } catch (IOException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IOException("Cannot read leaderboard", exception);
        }
    }

    @Override
    public void save(List<RunResult> results) throws IOException {
        Files.createDirectories(file.getParent());
        Path temporary = Files.createTempFile(file.getParent(), "leaderboard-", ".tmp");
        try {
            try (OutputStream output = Files.newOutputStream(temporary)) {
                var writer = XMLOutputFactory.newFactory().createXMLStreamWriter(output, "UTF-8");
                try {
                    writer.writeStartDocument("UTF-8", "1.0");
                    writer.writeStartElement("leaderboard");
                    writer.writeAttribute("version", VERSION);
                    for (RunResult result : results) {
                        writer.writeEmptyElement("run");
                        writer.writeAttribute("id", result.id().toString());
                        writer.writeAttribute("mode", result.modeId());
                        writer.writeAttribute("score", Integer.toString(result.score()));
                        writer.writeAttribute("outcome", result.outcome().name());
                        writer.writeAttribute("activeTimeMillis", Long.toString(result.activeTimeMillis()));
                        writer.writeAttribute("snakeLength", Integer.toString(result.snakeLength()));
                        writer.writeAttribute("recordedAt", result.recordedAt().toString());
                    }
                    writer.writeEndElement();
                    writer.writeEndDocument();
                } finally {
                    writer.close();
                }
            }
            try {
                Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (XMLStreamException exception) {
            throw new IOException("Cannot write leaderboard", exception);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}
