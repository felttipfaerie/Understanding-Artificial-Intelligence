import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

public class TextExtraction {

  static class TopWordCategoryClassifier {
    private static final Map<String, List<String>> THEME_KEYWORDS = Map.of(
        "practice", Arrays.asList("practice", "learn", "learnt", "lesson", "tutorial", "youve"),
        "parameters", Arrays.asList("parameter", "parametric", "base", "point", "chain", "constraint", "dimension", "associative"),
        "stretch", Arrays.asList("stretch", "rotate", "move", "scale", "flip", "array", "visibility", "bath", "control")
    );

    public String predict(Path pdfFile, List<String> words) {
      Map<String, Integer> scores = new HashMap<>();
      String filename = pdfFile.getFileName().toString().toLowerCase();
      List<String> filenameTokens = Stream.of(filename.split("[^a-z0-9]+"))
          .filter(token -> !token.isBlank())
          .collect(Collectors.toList());

      for (String theme : THEME_KEYWORDS.keySet()) {
        int score = 0;
        for (String keyword : THEME_KEYWORDS.get(theme)) {
          score += countMatches(words, keyword) * 2;
          score += countMatches(filenameTokens, keyword) * 3;
        }
        scores.put(theme, score);
      }

      return scores.entrySet().stream()
          .max(Map.Entry.comparingByValue())
          .map(Map.Entry::getKey)
          .orElse("stretch");
    }

    public String displayName(String label) {
      return switch (label) {
        case "practice" -> "Practice & Learning";
        case "parameters" -> "Parameters & Constraints";
        default -> "Stretch & Motion";
      };
    }

    public List<String> topWords(List<String> words, List<List<String>> allDocuments) {
      Map<String, Long> globalCounts = new HashMap<>();
      for (List<String> doc : allDocuments) {
        for (String term : new LinkedHashSet<>(doc)) {
          globalCounts.merge(term.toLowerCase(), 1L, Long::sum);
        }
      }

      Map<String, Long> counts = words.stream()
          .filter(word -> !word.isBlank())
          .collect(Collectors.groupingBy(word -> word.toLowerCase(), Collectors.counting()));

      double commonThreshold = allDocuments.size() * 0.60;

      return counts.entrySet().stream()
          .filter(entry -> globalCounts.getOrDefault(entry.getKey(), 0L) < commonThreshold)
          .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
          .limit(8)
          .map(Map.Entry::getKey)
          .collect(Collectors.toList());
    }

    private int countMatches(List<String> tokens, String keyword) {
      return (int) tokens.stream().filter(token -> token.contains(keyword)).count();
    }
  }

  public String toPlainText(String filename) {
    BodyContentHandler handler = new BodyContentHandler(-1);
    AutoDetectParser parser = new AutoDetectParser();
    Metadata metadata = new Metadata();

    try (InputStream stream = new BufferedInputStream(new FileInputStream(filename))) {
      parser.parse(stream, handler, metadata);
      return handler.toString();
    } catch (IOException | TikaException | SAXException e) {
      e.printStackTrace();
      return "";
    }
  }

  public static String convertToLowerCase(String in) {
    return in.toLowerCase();
  }

  public static String removePunctuation(String in) {
    Pattern pattern = Pattern.compile("[\\p{L}\\p{N}]+", Pattern.UNICODE_CHARACTER_CLASS);
    Matcher matcher = pattern.matcher(convertToLowerCase(in));
    StringBuilder sb = new StringBuilder();

    while (matcher.find()) {
      sb.append(matcher.group()).append(' ');
    }

    return sb.toString().trim();
  }

  public String removeAll(String rawtext) throws IOException {
    List<String> stopwords = Files.readAllLines(Path.of("stopwords.txt")).stream()
        .flatMap(line -> Arrays.stream(line.trim().split("\\s+")))
        .filter(word -> !word.isBlank())
        .collect(Collectors.toList());

    List<String> importtext = Stream.of(rawtext.trim().split("\\s+"))
        .filter(word -> !word.isBlank())
        .collect(Collectors.toCollection(ArrayList::new));

    importtext.removeAll(stopwords);
    return String.join(" ", importtext);
  }

  public List<String> loadDocToStrings(String filepath) throws IOException {
    List<String> words = new ArrayList<>();
    List<String> lines = Files.readAllLines(Path.of(filepath));

    for (String line : lines) {
      String[] ws = line.trim().split("\\s+");
      for (String word : ws) {
        if (!word.isBlank()) {
          words.add(word);
        }
      }
    }

    return words;
  }

  public double getTermFrequency(List<String> doc, String term) {
    if (doc.isEmpty()) {
      return 0.0;
    }

    long matches = doc.stream()
        .filter(word -> term.equalsIgnoreCase(word))
        .count();

    return matches / (double) doc.size();
  }

  public double getInverseDocumentFrequency(List<List<String>> allDocuments, String term) {
    long wordOccurrences = 0;

    for (List<String> document : allDocuments) {
      boolean found = document.stream().anyMatch(word -> term.equalsIgnoreCase(word));
      if (found) {
        wordOccurrences++;
      }
    }

    if (wordOccurrences == 0) {
      return 0.0;
    }

    return Math.log(allDocuments.size() / (double) wordOccurrences);
  }

  public double computeTfIdf(List<String> doc, List<List<String>> docs, String term) {
    return getTermFrequency(doc, term) * getInverseDocumentFrequency(docs, term);
  }

  private static List<Path> findPdfFiles(Path root) throws IOException {
    try (Stream<Path> paths = Files.walk(root)) {
      return paths
          .filter(Files::isRegularFile)
          .filter(path -> path.getFileName().toString().toLowerCase().endsWith(".pdf"))
          .sorted()
          .collect(Collectors.toList());
    }
  }

  public static void main(String[] args) throws IOException {
    Path root = (args.length > 0)
        ? Path.of(args[0])
        : Path.of("C:\\Users\\elmak\\Desktop\\Actions-course -Novideo");
    Path outputFile = Path.of("C:\\Users\\elmak\\Documents\\GitHub\\Understanding-Artificial-Intelligence\\python-coding-practice\\classification_output.txt");

    TextExtraction t = new TextExtraction();
    StringBuilder output = new StringBuilder();
    List<Path> pdfFiles = findPdfFiles(root);

    if (pdfFiles.isEmpty()) {
      output.append("No PDF files found under: ").append(root).append("\n");
      writeOutput(outputFile, output.toString());
      System.out.print(output);
      return;
    }

    List<List<String>> allDocuments = new ArrayList<>();
    for (Path pdfFile : pdfFiles) {
      String text = t.toPlainText(pdfFile.toString());
      String cleaned = removePunctuation(text);
      String filtered = t.removeAll(cleaned);
      List<String> words = Stream.of(filtered.split("\\s+"))
          .filter(word -> !word.isBlank())
          .collect(Collectors.toList());
      allDocuments.add(words);
    }

    List<String> firstDoc = allDocuments.get(0);
    List<String> uniqueTerms = new ArrayList<>(new LinkedHashSet<>(firstDoc));
    Map<String, Double> scores = new LinkedHashMap<>();

    for (String term : uniqueTerms) {
      scores.put(term, t.computeTfIdf(firstDoc, allDocuments, term));
    }

    List<Map.Entry<String, Double>> ranked = new ArrayList<>(scores.entrySet());
    ranked.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

    output.append("Scanned ").append(pdfFiles.size()).append(" PDF file(s) under: ").append(root).append("\n");

    TopWordCategoryClassifier classifier = new TopWordCategoryClassifier();

    Map<String, Integer> labelCounts = new HashMap<>();
    Map<String, List<String>> groupedWords = new LinkedHashMap<>();
    groupedWords.put("stretch", new ArrayList<>());
    groupedWords.put("practice", new ArrayList<>());
    groupedWords.put("parameters", new ArrayList<>());

    for (int i = 0; i < pdfFiles.size(); i++) {
      Path pdfFile = pdfFiles.get(i);
      String label = classifier.predict(pdfFile, allDocuments.get(i));
      labelCounts.put(label, labelCounts.getOrDefault(label, 0) + 1);
      groupedWords.get(label).addAll(allDocuments.get(i));
      output.append("PDF: ").append(pdfFile.getFileName()).append(" -> ").append(classifier.displayName(label)).append("\n");
    }

    output.append("\nDetected similarity groups (top terms):\n");
    labelCounts.forEach((label, count) -> {
      List<String> words = classifier.topWords(groupedWords.getOrDefault(label, List.of()), allDocuments);
      output.append("  ").append(classifier.displayName(label)).append(" (").append(count).append(" docs)\n");
      if (!words.isEmpty()) {
        output.append("    top words: ").append(String.join(", ", words)).append("\n");
      }
    });

    output.append("\nTop 25 TF-IDF terms in the first document:\n");
    int limit = Math.min(25, ranked.size());
    double maxScore = ranked.get(0).getValue();

    for (int i = 0; i < limit; i++) {
      Map.Entry<String, Double> entry = ranked.get(i);
      int bars = (int) Math.round((entry.getValue() / maxScore) * 40.0);
      String bar = "#".repeat(Math.max(1, bars));
      output.append(String.format("%2d. %-18s %8.5f  %s%n", i + 1, entry.getKey(), entry.getValue(), bar));
    }

    writeOutput(outputFile, output.toString());
    System.out.print(output);
  }

  private static int countTerms(List<String> words) {
    return (int) words.stream().filter(word -> !word.isBlank()).count();
  }

  private static void writeOutput(Path outputFile, String content) throws IOException {
    Files.createDirectories(outputFile.getParent());
    try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(outputFile))) {
      writer.print(content);
    }
  }
}

