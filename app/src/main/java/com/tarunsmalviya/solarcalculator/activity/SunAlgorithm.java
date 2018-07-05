package com.tarunsmalviya.solarcalculator.activity;

public class SunAlgorithm {

    private static final int J1970 = 2440588;
    private static final int J2000 = 2451545;
    private static final long MILLI_IN_A_DAY = 1000 * 60 * 60 * 24;

    // Obliquity of the Earth
    private static double e = (Math.PI / 180) * 23.4397;

    private static double millisToJulian(long millis) {
        return millis / MILLI_IN_A_DAY - 0.5 + J1970;
    }

    private static double millisFromJulian(double julian) {
        return (julian + 0.5 - J1970) * MILLI_IN_A_DAY;
    }

    private static double millisToDays(long millis) {
        return millisToJulian(millis) - J2000;
    }


    private static double rightAscension(double l, double b) {
        return Math.atan2(Math.sin(l) * Math.cos(e) - Math.tan(b) * Math.sin(e), Math.cos(l));
    }

    private static double declination(double l, double b) {
        return Math.asin(Math.sin(b) * Math.cos(e) + Math.cos(b) * Math.sin(e) * Math.sin(l));
    }

    private static double azimuth(double H, double phi, double dec) {
        return Math.atan2(Math.sin(H), Math.cos(H) * Math.sin(phi) - Math.tan(dec) * Math.cos(phi));
    }

    private static double altitude(double H, double phi, double dec) {
        return Math.asin(Math.sin(phi) * Math.sin(dec) + Math.cos(phi) * Math.cos(dec) * Math.cos(H));
    }

    private static double siderealTime(double d, double lw) {
        return (Math.PI / 180) * (280.16 + 360.9856235 * d) - lw;
    }

    private static double astroRefraction(double h) {
        if (h < 0) // the following formula works for positive altitudes only.
            h = 0; // if h = -0.08901179 a div/0 would occur.

        // formula 16.4 of "Astronomical Algorithms" 2nd edition by Jean Meeus (Willmann-Bell, Richmond) 1998.
        // 1.02 / tan(h + 10.26 / (h + 5.10)) h in degrees, result in arc minutes -> converted to rad:
        return 0.0002967 / Math.tan(h + 0.00312536 / (h + 0.08901179));
    }

    private static double solarMeanAnomaly(double d) {
        return (Math.PI / 180) * (357.5291 + 0.98560028 * d);
    }

    private static double eclipticLongitude(double M) {
        // equation of center
        double C = (Math.PI / 180) * (1.9148 * Math.sin(M) + 0.02 * Math.sin(2 * M) + 0.0003 * Math.sin(3 * M));
        // perihelion of the Earth
        double P = (Math.PI / 180) * 102.9372;

        return M + C + P + Math.PI;
    }

    private static double[] sunCoords(double d) {

        double M = solarMeanAnomaly(d), L = eclipticLongitude(M);

        return new double[]{declination(L, 0), rightAscension(L, 0)};
    }

    // Calculations for sun times
    private static final double J0 = 0.0009;


    private static double julianCycle(double d, double lw) {
        return Math.round(d - J0 - lw / (2 * Math.PI));
    }

    private static double approxTransit(double Ht, double lw, double n) {
        return J0 + (Ht + lw) / (2 * Math.PI) + n;
    }

    private static double solarTransitJ(double ds, double M, double L) {
        return J2000 + ds + 0.0053 * Math.sin(M) - 0.0069 * Math.sin(2 * L);
    }

    private static double hourAngle(double h, double phi, double d) {
        return Math.acos((Math.sin(h) - Math.sin(phi) * Math.sin(d)) / (Math.cos(phi) * Math.cos(d)));
    }

    // returns set time for the given sun altitude
    private static double getSetJ(double h, double lw, double phi, double dec, double n, double M, double L) {
        double w = hourAngle(h, phi, dec),
                a = approxTransit(w, lw, n);
        return solarTransitJ(a, M, L);
    }

    // Calculates sun times for a given date and latitude/longitude
    public static double[] calculateTimes(long millis, double lat, double lng) {
        double lw = (Math.PI / 180) * -lng;
        double phi = (Math.PI / 180) * lat;

        double d = millisToDays(millis);
        double n = julianCycle(d, lw);
        double ds = approxTransit(0, lw, n);

        double M = solarMeanAnomaly(ds);
        double L = eclipticLongitude(M);
        double dec = declination(L, 0);

        double Jnoon = solarTransitJ(ds, M, L);

        double Jset = getSetJ(-0.833 * (Math.PI / 180), lw, phi, dec, n, M, L);
        double Jrise = Jnoon - (Jset - Jnoon);

        return new double[]{millisFromJulian(Jrise), millisFromJulian(Jset)};
    }
}
