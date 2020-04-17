package com.construction.feature.address.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.math.BigDecimal;
import java.util.regex.Pattern;

@Embeddable
@Getter
@Setter
public class Location {

    @Column(columnDefinition = "text")
    private String locationUrl;

    private BigDecimal latitude;

    private BigDecimal longitude;

    public Location setLocationUrl(String locationUrl) {
        this.locationUrl = locationUrl;
        setLatitude(null);
        setLongitude(null);
        return this;
    }

    public Location setLatitude(BigDecimal latitude) {
        if (latitude == null) {
            this.latitude = getFromCoordinate(this.locationUrl, 0);
        } else if (this.locationUrl == null) {
            this.latitude = latitude;
        }
        return this;
    }

    public Location setLongitude(BigDecimal longitude) {
        if (longitude == null) {
            this.longitude = getFromCoordinate(this.locationUrl, 1);
        } else if (this.locationUrl == null) {
            this.longitude = longitude;
        }
        return this;
    }

    private BigDecimal getFromCoordinate(final String url, final int i) {
        final var pattern = Pattern.compile("@[0-9]+.[0-9]+,[0-9]+.[0-9]+");
        final var matcher = pattern.matcher(url);
        if (matcher.find()) {
            return new BigDecimal(matcher.group().substring(1).split(",")[i]);
        }
        return null;
    }
}
