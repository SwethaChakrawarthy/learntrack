package com.learntrack.course.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ProgressRequest(@Min(0) @Max(100) int progressPercent) {}
