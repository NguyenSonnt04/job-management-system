package Nhom08.Project.dto;

public record CareerPathSkillView(
    Long id,
    Integer skillOrder,
    String name,
    String category,
    Boolean isRequired,
    String proficiencyLevel,
    String description
) {}
