//package io.github.sayaka04.androidremoteclient.api;
//import java.nio.charset.StandardCharsets;
//import java.util.ArrayList;
//import java.util.List;
//
//public class MultipartBodyBuilder {
//
//    private final String boundary = "Boundary-" + System.currentTimeMillis();
//    private final List<byte[]> parts = new ArrayList<>();
//
//    public String getBoundary() {
//        return boundary;
//    }
//
//    public MultipartBodyBuilder addField(String name, String value) {
//        String part =
//                "--" + boundary + "\r\n" +
//                        "Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n" +
//                        value + "\r\n";
//
//        parts.add(part.getBytes(StandardCharsets.UTF_8));
//        return this;
//    }
//
//    public MultipartBodyBuilder addFile(String name, String filename, byte[] fileBytes, String mimeType) {
//        String header =
//                "--" + boundary + "\r\n" +
//                        "Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + filename + "\"\r\n" +
//                        "Content-Type: " + mimeType + "\r\n\r\n";
//
//        parts.add(header.getBytes(StandardCharsets.UTF_8));
//        parts.add(fileBytes);
//        parts.add("\r\n".getBytes(StandardCharsets.UTF_8));
//
//        return this;
//    }
//
//    public byte[] build() {
//        String end = "--" + boundary + "--";
//        parts.add(end.getBytes(StandardCharsets.UTF_8));
//
//        int size = parts.stream().mapToInt(p -> p.length).sum();
//        byte[] result = new byte[size];
//
//        int pos = 0;
//        for (byte[] part : parts) {
//            System.arraycopy(part, 0, result, pos, part.length);
//            pos += part.length;
//        }
//
//        return result;
//    }
//}