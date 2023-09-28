package com.gradle.superscalarsim.builders;

import com.gradle.superscalarsim.enums.RegisterReadinessEnum;
import com.gradle.superscalarsim.models.RegisterModel;

public class RegisterModelBuilder {
    private String name;
    private boolean isConstant;
    private double value;
    private RegisterReadinessEnum readiness;

    public RegisterModelBuilder() {
        this.name = "";
        this.isConstant = false;
        this.value = 0;
    }

    public RegisterModelBuilder hasName(String name) {
        this.name = name;
        return this;
    }

    public RegisterModelBuilder IsConstant(boolean isConstant) {
        this.isConstant = isConstant;
        return this;
    }

    public RegisterModelBuilder HasValue(double value) {
        this.value = value;
        return this;
    }

    public RegisterModelBuilder HasReadiness(RegisterReadinessEnum readiness) {
        this.readiness = readiness;
        return this;
    }

    public RegisterModel build() {
        return new RegisterModel(this.name, this.isConstant, this.value, this.readiness);
    }
}
