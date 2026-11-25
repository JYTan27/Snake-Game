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

/** Stores ordered run history in a versioned local XML file. */
public final class XmlLeaderboardRepository implements LeaderboardRepository {

    /** XML schema version accepted by this repository. */
    private static final String VERSION = "1";
    /** Absolute path to the saved leaderboard. */
    private final Path file;

    /**
     * Creates a repository for the given file, resolving its absolute path.
     *
     * @param file the XML file used for run history
     * @throws NullPointerException if {@code file} is null
     */
    public XmlLeaderboardRepository(Path file) {
        this.file = Objects.requireNonNull(file).toAbsolutePath();
    }

    /**
     * Resolves the default file under {@code LOCALAPPDATA} when available,
     * with the user's home directory as a fallback.
     *
     * @return the default leaderboard XML path
     */
    public static Path defaultPath() {
        // Use the application's local-data directory when Windows provides it.
        String local = System.getenv("LOCALAPPDATA");
        return local != null && !local.isBlank()
                ? Path.of(local, "AdvancedSnake", "leaderboard.xml")
                : Path.of(System.getProperty("user.home"), ".advanced-snake", "leaderboard.xml");
    }

    /** {@inheritDoc} An unsupported XML version or duplicate result ID causes a read failure. */
    @Override
    public List<RunResult> load() throws IOException {
        if (Files.notExists(file)) return List.of();
        // Read the file with an XML parser configured to block external content.
        try (InputStream input = Files.newInputStream(file)) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            // The parser identifies this feature by its exact URI without fetching it.
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            // Check the root element and version before interpreting any rows.
            Element root = factory.newDocumentBuilder().parse(input).getDocumentElement();
            if (!root.getTagName().equals("leaderboard") || !VERSION.equals(root.getAttribute("version"))) {
                throw new IOException("Unsupported leaderboard format or version");
            }
            // Collect results in document order and reject repeated IDs while reading rows.
            List<RunResult> results = new ArrayList<>();
            HashSet<UUID> ids = new HashSet<>();
            var children = root.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                // Read only run elements from the document's child nodes.
                if (!(children.item(i) instanceof Element row) || !row.getTagName().equals("run")) continue;
                // Decode the persisted fields into a validated run result.
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

    /** {@inheritDoc} Writes to a temporary file in the same directory before replacing the saved file. */
    @Override
    public void save(List<RunResult> results) throws IOException {
        Files.createDirectories(file.getParent());
        // Use a temporary file beside the destination for the final replacement.
        Path temporary = Files.createTempFile(file.getParent(), "leaderboard-", ".tmp");
        try {
            // Write the complete versioned document before replacing the saved file.
            try (OutputStream output = Files.newOutputStream(temporary)) {
                var writer = XMLOutputFactory.newFactory().createXMLStreamWriter(output, "UTF-8");
                try {
                    writer.writeStartDocument("UTF-8", "1.0");
                    writer.writeStartElement("leaderboard");
                    writer.writeAttribute("version", VERSION);
                    // Write rows in recording order so tie-breaking survives reload.
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
