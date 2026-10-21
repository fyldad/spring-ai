package ru.anblazhnov.springai.rag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.postretrieval.document.DocumentPostProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Supplies source text and useful provenance to the RAG prompt. */
@Component
public class SourceDocumentContext implements DocumentPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(SourceDocumentContext.class);
    private static final int MAX_SNAPSHOT_BYTES = 128 * 1024;
    private static final List<String> CONTEXT_FIELDS = List.of(
            "repository_id", "module_id", "relative_path", "start_line", "end_line",
            "type_fqn", "signature", "chunk_kind", "part_index", "part_count",
            "inheritance", "annotations", "overview");

    private final Path snapshotDirectory;

    public SourceDocumentContext(@Value("${app.rag.snapshot-directory:}") String snapshotDirectory) {
        this.snapshotDirectory = StringUtils.hasText(snapshotDirectory)
                ? Path.of(snapshotDirectory).toAbsolutePath().normalize() : null;
    }

    @Override
    public List<Document> process(Query query, List<Document> documents) {
        List<Document> result = new ArrayList<>();
        Set<Path> restoredSnapshots = new HashSet<>();
        for (Document original : documents) {
            Document document = original;
            // Qdrant normally maps doc_content to text before this processor runs.
            if (!StringUtils.hasText(document.getText())
                    && document.getMetadata().get("doc_content") instanceof String content
                    && StringUtils.hasText(content)) {
                var metadata = new HashMap<>(document.getMetadata());
                metadata.remove("doc_content");
                document = document.mutate().media(null).text(content).metadata(metadata).build();
            }
            if (isFragment(document)) {
                Path snapshot = findSnapshot(document);
                if (snapshot != null && restoredSnapshots.contains(snapshot)) {
                    continue;
                }
                String content = readSnapshot(snapshot);
                if (StringUtils.hasText(content)) {
                    restoredSnapshots.add(snapshot);
                    document = document.mutate().media(null).text(content)
                            .metadata(new HashMap<>(document.getMetadata()))
                            .metadata("restored_from_snapshot", snapshotDirectory.relativize(snapshot).toString())
                            .build();
                }
                else {
                    log.warn("Source chunk {} in {} contains missing or very short source text. "
                                    + "Re-ingest complete semantic chunks or configure app.rag.snapshot-directory.",
                            document.getId(), document.getMetadata().get("relative_path"));
                }
            }
            result.add(document);
        }
        return result;
    }

    public String format(List<Document> documents) {
        return documents.stream().map(this::formatDocument).collect(Collectors.joining("\n\n---\n\n"));
    }

    private String formatDocument(Document document) {
        StringBuilder context = new StringBuilder("Document: ").append(document.getId()).append('\n');
        for (String field : CONTEXT_FIELDS) {
            Object value = document.getMetadata().get(field);
            if (value != null) {
                context.append(field).append(": ").append(value).append('\n');
            }
        }
        if (document.getMetadata().containsKey("restored_from_snapshot")) {
            context.append("Source scope: full snapshot; chunk line numbers above describe the original fragment.\n");
        }
        if (isFragment(document)) {
            context.append("Source status: incomplete fragment; method names do not establish process step order.\n");
        }
        return context.append("\nSource content:\n")
                .append(document.getText() == null ? "" : document.getText()).toString();
    }

    private boolean isFragment(Document document) {
        String text = document.getText();
        if (!StringUtils.hasText(text)) {
            return true;
        }
        // Legacy ingestion repeated a long header before a few characters of source.
        String normalized = text.replace("\r\n", "\n");
        int headerEnd = normalized.startsWith("Repository: ") ? normalized.indexOf("\n\n") : -1;
        String source = headerEnd >= 0 ? normalized.substring(headerEnd + 2) : normalized;
        return source.strip().length() < 32;
    }

    private Path findSnapshot(Document document) {
        if (snapshotDirectory == null) {
            return null;
        }
        // A parent snapshot contains the surrounding semantic unit (e.g. a whole class).
        for (String key : List.of("parent_snapshot", "source_snapshot")) {
            Object value = document.getMetadata().get(key);
            if (value instanceof String reference && StringUtils.hasText(reference)) {
                try {
                    Path relative = Path.of(reference);
                    Path resolved = snapshotDirectory.resolve(relative).normalize();
                    if (!relative.isAbsolute() && resolved.startsWith(snapshotDirectory) && Files.isRegularFile(resolved)) {
                        Path realPath = resolved.toRealPath();
                        if (realPath.startsWith(snapshotDirectory.toRealPath())) {
                            return resolved;
                        }
                    }
                }
                catch (IOException | InvalidPathException e) {
                    log.debug("Cannot resolve snapshot {}", reference, e);
                }
            }
        }
        return null;
    }

    private String readSnapshot(Path snapshot) {
        if (snapshot == null) {
            return null;
        }
        try (var input = Files.newInputStream(snapshot)) {
            byte[] bytes = input.readNBytes(MAX_SNAPSHOT_BYTES + 1);
            if (bytes.length > MAX_SNAPSHOT_BYTES) {
                log.warn("Snapshot {} exceeds {} bytes; re-ingest smaller semantic chunks", snapshot, MAX_SNAPSHOT_BYTES);
                return null;
            }
            return new String(bytes, StandardCharsets.UTF_8);
        }
        catch (IOException e) {
            log.warn("Cannot read source snapshot {}", snapshot, e);
            return null;
        }
    }
}
