package com.nerflix.clone.util;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Stream;

public class FileHandlerUtil {

    private static final Set<String> ALLOWED_MATERIAL_EXTENSIONS = Set.of(
            ".pdf", ".doc", ".docx", ".ppt", ".pptx",
            ".xls", ".xlsx", ".txt", ".md", ".csv",
            ".zip", ".rar", ".7z", ".json",
            ".png", ".jpg", ".jpeg", ".webp"
    );

    private FileHandlerUtil() {
    }

    public static String extractFileExtension(String originalFilename) {
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }
        return fileExtension;
    }

    public static boolean isAllowedMaterialExtension(String originalFilename) {
        String ext = extractFileExtension(originalFilename);
        return !ext.isEmpty() && ALLOWED_MATERIAL_EXTENSIONS.contains(ext);
    }

    public static Path findFileByUuid(Path directory, String uuid) throws IOException {
        try (Stream<Path> stream = Files.list(directory)) {
            return stream
                    .filter(path -> path.getFileName().toString().startsWith(uuid))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("File not found for UUID: " + uuid));
        }
    }

    public static String detectVideoContentType(String filename) {
        if (filename == null) return "video/mp4";
        String lower = filename.toLowerCase();

        if (lower.endsWith(".webm")) return "video/webm";
        if (lower.endsWith(".ogg")) return "video/ogg";
        if (lower.endsWith(".mkv")) return "video/x-matroska";
        if (lower.endsWith(".avi")) return "video/x-msvideo";
        if (lower.endsWith(".mov")) return "video/quicktime";
        if (lower.endsWith(".flv")) return "video/x-flv";
        if (lower.endsWith(".wmv")) return "video/x-ms-wmv";
        if (lower.endsWith(".m4v")) return "video/x-m4v";
        if (lower.endsWith(".3gp")) return "video/3gpp";
        if (lower.endsWith(".mpg") || lower.endsWith(".mpeg")) return "video/mpeg";

        return "video/mp4";
    }

    public static String detectImageContentType(String filename) {
        if (filename == null) return "image/jpeg";
        String lower = filename.toLowerCase();

        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";

        return "image/jpeg";
    }

    public static String detectMaterialContentType(String filename) {
        if (filename == null) return "application/octet-stream";
        String lower = filename.toLowerCase();

        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".txt") || lower.endsWith(".md")) return "text/plain";
        if (lower.endsWith(".csv")) return "text/csv";
        if (lower.endsWith(".json")) return "application/json";
        if (lower.endsWith(".doc")) return "application/msword";
        if (lower.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (lower.endsWith(".ppt")) return "application/vnd.ms-powerpoint";
        if (lower.endsWith(".pptx")) return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
        if (lower.endsWith(".xls")) return "application/vnd.ms-excel";
        if (lower.endsWith(".xlsx")) return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        if (lower.endsWith(".zip")) return "application/zip";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".webp")) return "image/webp";

        return "application/octet-stream";
    }

    public static long[] parseRangeHeader(String rangeHeader, long fileLength) {
        String[] ranges = rangeHeader.replace("bytes=", "").split("-");
        long rangeStart = Long.parseLong(ranges[0]);
        long rangeEnd = ranges.length > 1 &&
                !ranges[1].isEmpty() ? Long.parseLong(ranges[1]) : fileLength - 1;
        return new long[]{rangeStart, rangeEnd};
    }

    public static Resource createRangeResource(Path filePath, long rangeStart, long rangeLength) throws IOException {
        RandomAccessFile fileReader = new RandomAccessFile(filePath.toFile(), "r");
        fileReader.seek(rangeStart);

        InputStream partialContentStream = new InputStream() {
            private long totalBytesRead = 0;

            @Override
            public int read() throws IOException {
                if (totalBytesRead >= rangeLength) {
                    fileReader.close();
                    return -1;
                }
                totalBytesRead++;
                return fileReader.read();
            }

            @Override
            public int read(byte[] buffer, int offset, int length) throws IOException {
                if (totalBytesRead >= rangeLength) {
                    fileReader.close();
                    return -1;
                }
                long remainingBytes = rangeLength - totalBytesRead;
                int bytesToRead = (int) Math.min(length, remainingBytes);
                int bytesActuallyRead = fileReader.read(buffer, offset, bytesToRead);
                if (bytesActuallyRead > 0) {
                    totalBytesRead += bytesActuallyRead;
                }
                if (totalBytesRead >= rangeLength) {
                    fileReader.close();
                }
                return bytesActuallyRead;
            }

            @Override
            public void close() throws IOException {
                fileReader.close();
            }
        };

        return new InputStreamResource(partialContentStream) {
            @Override
            public long contentLength() {
                return rangeLength;
            }
        };
    }

    public static Resource createFullResource(Path filePath) throws IOException {
        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            throw new IOException("File not found or not readable: " + filePath);
        }
        return resource;
    }
}
