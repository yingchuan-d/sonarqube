/*
 * SonarQube
 * Copyright (C) 2009-2020 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.server.security;

import org.sonar.server.measure.Rating;

import static org.sonar.server.measure.Rating.A;
import static org.sonar.server.measure.Rating.B;
import static org.sonar.server.measure.Rating.C;
import static org.sonar.server.measure.Rating.D;
import static org.sonar.server.measure.Rating.E;

public class SecurityReviewRating {

  private SecurityReviewRating() {
    // Only static method
  }

  /**
   * This code will be removed when updating computation of Security Review Rating for portfolios
   */
  @Deprecated
  public static Rating computeForPortfolios(int ncloc, int securityHotspots) {
    if (ncloc == 0) {
      return A;
    }
    double ratio = (double) securityHotspots * 1000d / (double) ncloc;
    if (ratio <= 3d) {
      return A;
    } else if (ratio <= 10) {
      return B;
    } else if (ratio <= 15) {
      return C;
    } else if (ratio <= 25) {
      return D;
    } else {
      return E;
    }
  }

  public static double computePercent(long hotspotsToReview, long hotspotsReviewed) {
    long total = hotspotsToReview + hotspotsReviewed;
    if (total == 0) {
      return 100.0;
    }
    return hotspotsReviewed * 100.0 / total;
  }

  public static Rating computeRating(Double percent) {
    if (percent >= 80.0) {
      return A;
    } else if (percent >= 70.0) {
      return B;
    } else if (percent >= 50.0) {
      return C;
    } else if (percent >= 30.0) {
      return D;
    }
    return E;
  }
}
