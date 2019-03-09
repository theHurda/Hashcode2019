package zz.hashcode;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import zz.hashcode.solver.Christofides;

import java.util.*;
import java.util.stream.Collectors;

public class HashCode2019 {
  public static final int RUN_SIZE = 2000;
  public static final int LOOKBACK_DEPTH = 200;

  public static Map<String, Integer> scoreCache = new HashMap<>();

  public static List<Slide> createSlides(Input input) {
    List<Slide> toReturn = new ArrayList<>(input.images.size());
    List<Input.Image> verticalImages = new ArrayList<>();
    for (Input.Image image : input.images) {
      if (image.multi) {
        verticalImages.add(image);
      } else {
        toReturn.add(new Slide(image));
      }
    }
    Collections.sort(verticalImages, Comparator.comparing(Input.Image::getTagsSize));
    while (!verticalImages.isEmpty()) {
      Input.Image a = verticalImages.get(0);
      Input.Image best = null;
      int union = Integer.MAX_VALUE;
      for (int i = verticalImages.size() - 1; i > Math.max(0, verticalImages.size() - LOOKBACK_DEPTH); i--) {
        Input.Image b = verticalImages.get(i);
        int size = Sets.intersection(a.tags, b.tags).size();
        if (size < union) {
          best = b;
          union = size;
        }
      }
      toReturn.add(new Slide(a, best));
      verticalImages.remove(a);
      verticalImages.remove(best);
    }
    //        for (int i = 0; i < verticalImages.size() / 2; i++) {
    //            toReturn.add(new Slide(verticalImages.get(i), verticalImages.get(verticalImages.size() - i - 1)));
    //        }
    return toReturn;
  }

  static class Solution {
    List<Slide> slides = new ArrayList<>();

    @Override
    public String toString() {
      return slides.size() + "\n" + Joiner.on("\n").join(slides);
    }
  }

  static class Slide {
    List<Input.Image> images = new ArrayList<>();

    public Slide(Input.Image... image) {
      for (Input.Image i : image) {
        images.add(i);
      }
    }

    public String toString() {
      return Joiner.on(" ").join(images.stream().map(Input.Image::getId).sorted().collect(Collectors.toList()));
    }

    public Set<String> getTags() {
      Set<String> allTags = new HashSet<>();
      for (Input.Image image : images) {
        allTags.addAll(image.tags);
      }
      return allTags;
    }

    int getTagSize() {
      return getTags().size();
    }
  }

  static class Input {
    static class Image {
      int id;
      boolean multi;
      Set<String> tags;

      int getId() {
        return id;
      }

      Image(int id, boolean multi, Set<String> tags) {
        this.id = id;
        this.multi = multi;
        this.tags = tags;
      }

      int getTagsSize() {
        return tags.size();
      }
    }

    List<Image> images;
  }

  private static double[][] computeInterestsDouble(List<Slide> slides) {
    int N = slides.size();
    double[][] distances = new double[N][N];
    for (int i = 0; i < slides.size(); i++) {
      for (int j = 0; j < slides.size(); j++) {
        if (i == j) {
          distances[i][j] = Double.POSITIVE_INFINITY;
        }
        Set<String> s1 = slides.get(i).getTags();
        Set<String> s2 = slides.get(j).getTags();
        distances[i][j] = 100.0 - Math.min(Math.min(Sets.difference(s1, s2).size(), Sets.difference(s2, s1).size()),
            Sets.intersection(s1, s2).size());
      }
    }
    return distances;
  }

  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    Input input = new Input();
    int imageCount = scanner.nextInt();
    input.images = new ArrayList<>();
    for (int i = 0; i < imageCount; i++) {
      String orientation = scanner.next();
      Set<String> tags = new HashSet<>();
      int tagCount = scanner.nextInt();
      for (int j = 0; j < tagCount; j++) {
        String tag = scanner.next();
        tags.add(tag);
      }
      input.images.add(new Input.Image(i, "V".equals(orientation), tags));
    }

//    this part needed if assuming pre-created slides
//    List<Slide> slides = createSlides(input);
//    Collections.sort(slides, Comparator.comparing(Slide::getTagSize).reversed());

    Solution s = solve(input.images);
    int score = evaluate(s);
    System.err.println("Score: " + score);
    System.out.println(s);
  }

  private static int evaluate(Solution s) {
    Slide last = s.slides.get(0);
    int score = 0;
    for (int i = 1; i < s.slides.size(); i++) {
      Slide now = s.slides.get(i);
      score += getScore(last, now);
      last = now;
    }
    return score;
  }

  private static Solution solve(List<Input.Image> input) {
    return solveGreedyByImage(new HashSet<>(input));
  }

  private static int getScore(Slide last, Slide now) {
    int same = Sets.intersection(last.getTags(), now.getTags()).size();
    return Math.min(Math.min(last.getTags().size() - same, now.getTags().size() - same), same);
  }

  private static int getScoreCached(Slide last, Slide now) {
    String key = makeCacheKey(last, now);

    if (scoreCache.containsKey(key)) {
      return scoreCache.get(key);
    }

    int score = getScore(last, now);
    scoreCache.put(key, score);
    return score;
  }

  private static Solution solveGreedy(List<Slide> input) {
    Solution toReturn = new Solution();

    Slide last = input.remove(0);
    toReturn.slides.add(last);

    while (!input.isEmpty()) {
      Slide nextBest = null;
      int bestScore = 0;

      for (Slide slide : input) {
        if (slide.getTagSize() / 2 < bestScore) {
          break;
        }
        int s = getScore(last, slide);
        // last slide = smallest tag set that yields that score
        if (s >= bestScore) {
          bestScore = s;
          nextBest = slide;
        }
      }

      last = nextBest;
      toReturn.slides.add(last);
      input.remove(last);
    }

    return toReturn;
  }

  private static String makeCacheKey(Slide s1, Slide s2) {
    List<Slide> slides = new ArrayList<>();
    slides.add(s1);
    slides.add(s2);

    return Joiner.on("-").join(slides.stream().map(Slide::toString).sorted().collect(Collectors.toList()));
  }

  private static Solution solveGreedyByImage(Set<Input.Image> images) {
    Solution toReturn = new Solution();

    Slide current;
    Optional<Input.Image> firstImage = images.stream().filter(i -> !i.multi).findFirst();
    if (firstImage.isPresent()) {
      current = new Slide(firstImage.get());
    } else {
      List<Input.Image> imageList = images.stream().limit(2).collect(Collectors.toList());
      current = new Slide(imageList.get(0), imageList.get(1));
    }
    toReturn.slides.add(current);
    images.removeAll(current.images);

    Set<Input.Image> hImages = images.stream().filter(i -> !i.multi).collect(Collectors.toSet());
    Set<Input.Image> vImages = images.stream().filter(i -> i.multi).collect(Collectors.toSet());

    while (!hImages.isEmpty() || !vImages.isEmpty()) {
      // see progress
      System.err.println("remaining: h " + hImages.size() + ", v " + vImages.size());

      int hScore = -1;
      Slide hSlide = null;

      // best neighbor from horizontal slides
      for (Input.Image image : hImages) {
        Slide slide = new Slide(image);
        int score = getScoreCached(current, slide);
        if (score > hScore) {
          hScore = score;
          hSlide = slide;
        }
      }

      int vScore = -1;
      Slide vSlide = null;

      // best neighbor from vertical slides
      for (Input.Image v1 : vImages) {
        for (Input.Image v2 : vImages) {
          if (v1.id != v2.id) {
            Slide slide = new Slide(v1, v2);
            int score = getScoreCached(current, slide);
            if (score > vScore) {
              vScore = score;
              vSlide = slide;
            }
          }
        }
      }

      // pick better of candidates
      if (hScore > vScore) {
        current = hSlide;
        toReturn.slides.add(current);
        hImages.removeAll(hSlide.images);
      } else if (vScore >= 0) {
        current = vSlide;
        toReturn.slides.add(current);
        vImages.removeAll(vSlide.images);
      }
    }

    return toReturn;
  }

  private static Solution solveChristofides(List<Slide> input) {
    Solution toReturn = new Solution();

    int co = 0;
    int loop = 0;
    for (List<Slide> slides : Lists.partition(input, RUN_SIZE)) {
      System.err.println(co++ + "/" + input.size() / RUN_SIZE);
      int[] output = new Christofides().solve(computeInterestsDouble(slides));
      for (int i : output) {
        toReturn.slides.add(input.get((loop * RUN_SIZE) + i));
      }
      loop++;
    }

    return toReturn;
  }

  private static Solution solveStupid(Input input) {
    Solution toReturn = new Solution();
    for (Input.Image image : input.images) {
      toReturn.slides.add(new Slide(image));
    }
    return toReturn;
  }
}