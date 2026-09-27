package com.eve.booking.entity;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class CentreTestId implements Serializable {
    private Long centreId;
    private Long testId;

    public CentreTestId() {}

    public CentreTestId(Long centreId, Long testId) {
        this.centreId = centreId;
        this.testId = testId;
    }

    public Long getCentreId() { return centreId; }
    public Long getTestId() { return testId; }

    public void setCentreId(Long centreId) { this.centreId = centreId; }
    public void setTestId(Long testId) { this.testId = testId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CentreTestId that = (CentreTestId) o;
        return Objects.equals(centreId, that.centreId) && Objects.equals(testId, that.testId);
    }

    @Override
    public int hashCode() { return Objects.hash(centreId, testId); }
}
