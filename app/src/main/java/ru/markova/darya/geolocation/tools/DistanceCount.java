package ru.markova.darya.geolocation.tools;

/**
 * Created by Vitaliy Kholuenko on 11.06.2017.
 */
public class DistanceCount {

    /**
     * Counts distance between two geo points with known coordinates
     * @param lat1 - first point's latitude (in degrees, e.g. 51.1005001)
     * @param lon1 - first point's longitude (in degrees, e.g. 39.1005001)
     * @param lat2 - second point's latitude (in degrees, e.g. 51.1005001)
     * @param lon2 - second point's longitude (in degrees, e.g. 39.1005001)
     * @return distance between geo points (in meters)
     */
    public static double distance(double lat1, double lon1, double lat2, double lon2) {
        final double pi = Math.PI;

        // Convert to radians
        lat1 *= pi / 180;
        lon1 *= pi / 180;
        lat2 *= pi / 180;
        lon2 *= pi / 180;

        // Earth radiuses
        double r1 = earthRadius(lat1);
        double r2 = earthRadius(lat2);

        /**
         * Convert from spherical to Descartes coordinates and count difference between points for each axis
         * the curvature of the arc is neglected
         */
        double dx = r2 * Math.cos(lon2) * Math.cos(lat2) - r1 * Math.cos(lon1) * Math.cos(lat1);
        double dy = r2 * Math.sin(lon2) * Math.cos(lat2) - r1 * Math.sin(lon1) * Math.cos(lat1);
        double dz = r2 * Math.sin(lat2) - r1 * Math.sin(lat1);

        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * Counts Earth radius in a place with known latitude
     * @param lat - latitude (in radians)
     * @return Earth radius (in meters)
     */
    private static double earthRadius(double lat) {
        final double POLE_RADIUS = 6356752.3;
        final double EQUATOR_RADIUS = 6378137;

        double assist1 = EQUATOR_RADIUS * EQUATOR_RADIUS * Math.cos(lat);
        double assist2 = POLE_RADIUS * POLE_RADIUS * Math.sin(lat);
        double assist3 = EQUATOR_RADIUS * Math.cos(lat);
        double assist4 = POLE_RADIUS * Math.sin(lat);

        return Math.sqrt( (assist1 * assist1 + assist2 * assist2) /
                (assist3 * assist3 + assist4 * assist4));
    }
}
