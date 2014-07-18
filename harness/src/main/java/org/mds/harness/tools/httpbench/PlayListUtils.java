package org.mds.harness.tools.httpbench;

import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Pattern;

import org.mds.video.hls.model.*;
import org.mds.video.hls.model.tags.*;
import org.mds.video.hls.model.tags.composite.RecordMarker;
import org.mds.video.hls.utils.StringUtils;

public class PlayListUtils {

    private static final String PREFIX = "../";
    private static final String PREFIX_DOT = "./";

    public static void remapResourceUris(M3uPlayList playList, final String originalPrefix, final String targetPrefix) throws URISyntaxException {
        remapResourceUris(playList, originalPrefix, targetPrefix, null, null);
    }

    public static void remapResourceUris(M3uPlayList playList, final String originalPrefix, final String targetPrefix, List<Integer> excludeIndexes) throws URISyntaxException {
        remapResourceUris(playList, originalPrefix, targetPrefix, excludeIndexes, null);
    }

    public static void remapResourceUris(M3uPlayList playList, final String originalPrefix, final String targetPrefix, Class excludeTag) throws URISyntaxException {
        remapResourceUris(playList, originalPrefix, targetPrefix, null, excludeTag);
    }

    public static void remapResourceUris(M3uPlayList playList, final String originalPrefix, final String targetPrefix, List<Integer> excludeIndexes, Class excludeTag) throws URISyntaxException {
        if (playList == null) return;
        String prefixUrlBase = targetPrefix;
        String originalUrlBase = originalPrefix;

        if (prefixUrlBase != null) {
            // ensure that the base urls end with a separator (backward compatible)
            if (prefixUrlBase.charAt(prefixUrlBase.length() - 1) != '/') {
                prefixUrlBase = StringUtils.join(prefixUrlBase, "/");
            }

            if (originalUrlBase != null && originalUrlBase.charAt(originalUrlBase.length() - 1) != '/') {
                originalUrlBase = StringUtils.join(originalUrlBase, "/");
            }

            if (playList instanceof VariantPlaylist) {
                VariantPlaylist variantPlaylist = (VariantPlaylist) playList;
                // replace variant stream uris
                if (variantPlaylist.getVariantStreams() != null) {
                    int index = 0;
                    for (ExtStreamInfTag stream : variantPlaylist.getVariantStreams()) {
                        if ((excludeIndexes != null && excludeIndexes.contains(index++))
                                || (excludeTag != null && excludeTag.isInstance(stream))) {
                            continue;
                        }
                        stream.setUri(rewriteUri(stream.getUri(), prefixUrlBase, originalUrlBase));
                    }
                }
                if (variantPlaylist.getHeader() != null) {
                    ExtFaxCmTag extFaxCmTag = variantPlaylist.getHeader().getFaxCmTag();
                    if (extFaxCmTag == null || (excludeTag != null && excludeTag.isInstance(extFaxCmTag))) {
                        return;
                    } else {
                        extFaxCmTag.setUri(rewriteUri(extFaxCmTag.getUri(), prefixUrlBase, originalUrlBase));
                    }
                }
                return;
            }

            if (playList instanceof BitratePlaylist) {
                BitratePlaylist bitratePlaylist = (BitratePlaylist) playList;
                // replace record marker uris and key locations
                if (bitratePlaylist.getRecordMarkers() != null) {
                    int index = 0;
                    for (RecordMarker marker : bitratePlaylist.getRecordMarkers()) {
                        if (excludeIndexes != null && excludeIndexes.contains(index++)) {
                            continue;
                        }
                        marker.getInfTag().setUri(rewriteUri(marker.getInfTag().getUri(),
                                prefixUrlBase, originalUrlBase));
                        if (marker.getKeyTag() != null) {
                            marker.getKeyTag().setUri(
                                    rewriteUri(marker.getKeyTag().getUri(),
                                            prefixUrlBase, originalUrlBase));
                        }
                    }
                }
                return;
            }
        }
    }

    private static String rewriteUri(String uri, String prefixUrl, String replaceUrlPrefix) throws URISyntaxException {

        if (!uri.startsWith("http")) {
            uri = normalize(uri);
//            while (uri.startsWith(PREFIX)) {
//                UrlPair urlPair = replaceRelativePath(uri, prefixUrl);
//                uri = urlPair.resource;
//                prefixUrl = urlPair.prefixUrl;
//            }
//
//            uri = StringUtils.substringAfterLast(uri, PREFIX_DOT);

            return StringUtils.join(prefixUrl, uri);
        }

        if (replaceUrlPrefix != null && uri.startsWith(replaceUrlPrefix)) {
            return StringUtils.join(prefixUrl, StringUtils.substring(uri, replaceUrlPrefix.length()));
        }
        return uri;
    }

    private static UrlPair replaceRelativePath(String uri, String prefixUrl) {
        UrlPair urlPair = new UrlPair();
        urlPair.resource = StringUtils.substringAfter(uri, PREFIX);
        if (prefixUrl.charAt(prefixUrl.length() - 1) == '/') {
            prefixUrl = StringUtils.substringBeforeLast(prefixUrl, '/');
        }
        urlPair.prefixUrl = StringUtils.substringBeforeLast(prefixUrl, '/');
        return urlPair;
    }

    public static void appendParametersToVariantPlaylist(VariantPlaylist playlist, String parameters) {
        if (playlist == null || playlist.getVariantStreams() == null || StringUtils.isEmpty(parameters)) {
            return;
        }
        for (ExtStreamInfTag streamInfTag : playlist.getVariantStreams()) {
            String bitrateManifestUri = streamInfTag.getUri();
            String originalUri = StringUtils.substringBefore(bitrateManifestUri, "?");
            String originalParameters = StringUtils.substringAfter(bitrateManifestUri, "?");
            String targetParameters;
            if (StringUtils.isEmpty(originalParameters)) {
                targetParameters = parameters;
            } else {
                targetParameters = StringUtils.join(originalParameters, "&", parameters);
            }
            String newBitrateManifestUri = StringUtils.join(originalUri, "?", targetParameters);
            streamInfTag.setUri(newBitrateManifestUri);
        }
    }

    private static class UrlPair {
        private String prefixUrl;
        private String resource;
    }

    private static Pattern pattern = Pattern.compile("((/|^))\\./");

    protected static String cleanUrl(String url) {
        return pattern.matcher(url).replaceAll("$1");
    }

    private static int needsNormalization(String path) {
        boolean normal = true;
        int ns = 0;                     // Number of segments
        int end = path.length() - 1;    // Index of last char in path
        int p = 0;                      // Index of next char in path

        // Skip initial slashes
        while (p <= end) {
            if (path.charAt(p) != '/') break;
            p++;
        }
        if (p > 1) normal = false;

        // Scan segments
        while (p <= end) {

            // Looking at "." or ".." ?
            if ((path.charAt(p) == '.')
                    && ((p == end)
                    || ((path.charAt(p + 1) == '/')
                    || ((path.charAt(p + 1) == '.')
                    && ((p + 1 == end)
                    || (path.charAt(p + 2) == '/')))))) {
                normal = false;
            }
            ns++;

            // Find beginning of next segment
            while (p <= end) {
                if (path.charAt(p++) != '/')
                    continue;

                // Skip redundant slashes
                while (p <= end) {
                    if (path.charAt(p) != '/') break;
                    normal = false;
                    p++;
                }

                break;
            }
        }

        return normal ? -1 : ns;
    }

    public static String normalize(String ps) {

        // Does this path need normalization?
        int ns = needsNormalization(ps);        // Number of segments
        if (ns < 0)
            // Nope -- just return it
            return ps;

        char[] path = ps.toCharArray();         // Path in char-array form

        // Split path into segments
        int[] segs = new int[ns];               // Segment-index array
        split(path, segs);

        // Remove dots
        removeDots(path, segs);

        // Prevent scheme-name confusion
        maybeAddLeadingDot(path, segs);

        // Join the remaining segments and return the result
        String s = new String(path, 0, join(path, segs));
        if (s.equals(ps)) {
            // string was already normalized
            return ps;
        }
        return s;
    }

    static private void split(char[] path, int[] segs) {
        int end = path.length - 1;      // Index of last char in path
        int p = 0;                      // Index of next char in path
        int i = 0;                      // Index of current segment

        // Skip initial slashes
        while (p <= end) {
            if (path[p] != '/') break;
            path[p] = '\0';
            p++;
        }

        while (p <= end) {

            // Note start of segment
            segs[i++] = p++;

            // Find beginning of next segment
            while (p <= end) {
                if (path[p++] != '/')
                    continue;
                path[p - 1] = '\0';

                // Skip redundant slashes
                while (p <= end) {
                    if (path[p] != '/') break;
                    path[p++] = '\0';
                }
                break;
            }
        }

        if (i != segs.length)
            throw new InternalError();  // ASSERT
    }

    private static void removeDots(char[] path, int[] segs) {
        int ns = segs.length;
        int end = path.length - 1;

        for (int i = 0; i < ns; i++) {
            int dots = 0;               // Number of dots found (0, 1, or 2)

            // Find next occurrence of "." or ".."
            do {
                int p = segs[i];
                if (path[p] == '.') {
                    if (p == end) {
                        dots = 1;
                        break;
                    } else if (path[p + 1] == '\0') {
                        dots = 1;
                        break;
                    } else if ((path[p + 1] == '.')
                            && ((p + 1 == end)
                            || (path[p + 2] == '\0'))) {
                        dots = 2;
                        break;
                    }
                }
                i++;
            } while (i < ns);
            if ((i > ns) || (dots == 0))
                break;

            if (dots == 1) {
                // Remove this occurrence of "."
                segs[i] = -1;
            } else {
                // If there is a preceding non-".." segment, remove both that
                // segment and this occurrence of ".."; otherwise, leave this
                // ".." segment as-is.
                int j;
                for (j = i - 1; j >= 0; j--) {
                    if (segs[j] != -1) break;
                }
                if (j >= 0) {
                    int q = segs[j];
                    if (!((path[q] == '.')
                            && (path[q + 1] == '.')
                            && (path[q + 2] == '\0'))) {
                        segs[i] = -1;
                        segs[j] = -1;
                    }
                }
            }
        }
    }

    private static int join(char[] path, int[] segs) {
        int ns = segs.length;           // Number of segments
        int end = path.length - 1;      // Index of last char in path
        int p = 0;                      // Index of next path char to write

        if (path[p] == '\0') {
            // Restore initial slash for absolute paths
            path[p++] = '/';
        }

        for (int i = 0; i < ns; i++) {
            int q = segs[i];            // Current segment
            if (q == -1)
                // Ignore this segment
                continue;

            if (p == q) {
                // We're already at this segment, so just skip to its end
                while ((p <= end) && (path[p] != '\0'))
                    p++;
                if (p <= end) {
                    // Preserve trailing slash
                    path[p++] = '/';
                }
            } else if (p < q) {
                // Copy q down to p
                while ((q <= end) && (path[q] != '\0'))
                    path[p++] = path[q++];
                if (q <= end) {
                    // Preserve trailing slash
                    path[p++] = '/';
                }
            } else
                throw new InternalError(); // ASSERT false
        }

        return p;
    }

    private static void maybeAddLeadingDot(char[] path, int[] segs) {

        if (path[0] == '\0')
            // The path is absolute
            return;

        int ns = segs.length;
        int f = 0;                      // Index of first segment
        while (f < ns) {
            if (segs[f] >= 0)
                break;
            f++;
        }
        if ((f >= ns) || (f == 0))
            // The path is empty, or else the original first segment survived,
            // in which case we already know that no leading "." is needed
            return;

        int p = segs[f];
        while ((p < path.length) && (path[p] != ':') && (path[p] != '\0')) p++;
        if (p >= path.length || path[p] == '\0')
            // No colon in first segment, so no "." needed
            return;

        // At this point we know that the first segment is unused,
        // hence we can insert a "." segment at that position
        path[0] = '.';
        path[1] = '\0';
        segs[0] = 0;
    }
}
