/* TODO: have this file generated */

package org.harctoolbox.irp;

/**
 * This class contains version and license information and constants.
 */
public final class Version {
    /** Verbal description of the license of the current work. */
    public final static String licenseString = "Copyright (C) 2016 Bengt Martensson.\n\nThis program is free software: you can redistribute it and/or modify it under the termsof the GNU General Public License as published by the Free Software Foundation;either version 3 of the License, or (at your option) any later version.\n\nThis program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the GNU General Public License for more details.\n\nYou should have received a copy of the GNU General Public Licensealong with this program. If not, see http://www.gnu.org/licenses/.";

    /** Verbal description of licenses of third-party components. */
    public final static String thirdPartyString = "";

    public final static String appName = "IrpTransmogrifier";
    public final static String version = "0.0.1";
    public final static String versionString = appName + " version " + version;

    /** Project home page. */
    public final static String homepageUrl = "http://www.harctoolbox.org";

    /** URL containing current official version. */
    public final static String currentVersionUrl = homepageUrl + "/downloads/" + appName + ".version";

    private Version() {
    }

    public static void main(String[] args) {
        System.out.println(versionString);
    }
}
