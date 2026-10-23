package io.github.squoirelle.material3fx.color;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * Picks theme seed colors out of an image (wallpaper, logo, album art...).
 * <p>
 * A lighter take on Material's quantize-and-score pipeline: pixels are bucketed by HCT hue,
 * buckets are weighted by population and chroma, and the strongest distinct hues win.
 */
public final class SeedExtractor {

    private static final int HUE_BUCKETS = 36;
    private static final double MIN_CHROMA = 15.0;
    private static final int MAX_SAMPLES = 128 * 128;

    private SeedExtractor() {
    }

    /** The single best seed color, or Material's baseline purple if the image is greyscale. */
    public static Color extract(Image image) {
        List<Color> seeds = extract(image, 1);
        return seeds.isEmpty() ? Color.web("#6750A4") : seeds.getFirst();
    }

    /** Up to {@code count} seed colors, best first, at least 30° of hue apart. */
    public static List<Color> extract(Image image, int count) {
        PixelReader reader = image.getPixelReader();
        if (reader == null) {
            throw new IllegalArgumentException("Image has no readable pixels (not loaded yet?)");
        }
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        int step = Math.max(1, (int) Math.sqrt((double) width * height / MAX_SAMPLES));

        double[] weight = new double[HUE_BUCKETS];
        double[] chromaSum = new double[HUE_BUCKETS];
        double[] toneSum = new double[HUE_BUCKETS];
        double[] sin = new double[HUE_BUCKETS];
        double[] cos = new double[HUE_BUCKETS];

        for (int y = 0; y < height; y += step) {
            for (int x = 0; x < width; x += step) {
                int argb = reader.getArgb(x, y);
                if (ColorUtils.alphaFromArgb(argb) < 255) {
                    continue;
                }
                Hct hct = Hct.fromInt(argb);
                if (hct.getChroma() < MIN_CHROMA) {
                    continue;
                }
                int bucket = (int) (hct.getHue() / (360.0 / HUE_BUCKETS)) % HUE_BUCKETS;
                double w = hct.getChroma();
                weight[bucket] += w;
                chromaSum[bucket] += hct.getChroma() * w;
                toneSum[bucket] += hct.getTone() * w;
                sin[bucket] += Math.sin(Math.toRadians(hct.getHue())) * w;
                cos[bucket] += Math.cos(Math.toRadians(hct.getHue())) * w;
            }
        }

        // Smooth over neighbouring buckets so a hue spanning a bucket boundary is not split.
        double[] score = new double[HUE_BUCKETS];
        for (int i = 0; i < HUE_BUCKETS; i++) {
            score[i] = weight[i] + 0.5 * (weight[(i + 1) % HUE_BUCKETS] + weight[(i + HUE_BUCKETS - 1) % HUE_BUCKETS]);
        }

        List<Color> result = new ArrayList<>();
        List<Double> chosenHues = new ArrayList<>();
        boolean[] used = new boolean[HUE_BUCKETS];
        while (result.size() < count) {
            int best = -1;
            for (int i = 0; i < HUE_BUCKETS; i++) {
                if (!used[i] && weight[i] > 0 && (best < 0 || score[i] > score[best])) {
                    best = i;
                }
            }
            if (best < 0) {
                break;
            }
            used[best] = true;
            double hue = MathUtils.sanitizeDegreesDouble(Math.toDegrees(Math.atan2(sin[best], cos[best])));
            if (chosenHues.stream().anyMatch(h -> MathUtils.differenceDegrees(h, hue) < 30)) {
                continue;
            }
            chosenHues.add(hue);
            double chroma = chromaSum[best] / weight[best];
            double tone = MathUtils.clampDouble(30, 70, toneSum[best] / weight[best]);
            result.add(Hct.from(hue, Math.max(chroma, 48), tone).toColor());
        }
        return result;
    }
}
