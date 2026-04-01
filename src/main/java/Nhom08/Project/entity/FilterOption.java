package Nhom08.Project.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "filter_options")
public class FilterOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private FilterGroup group;

    @Column(nullable = false, length = 100)
    private String value;

    @Column(nullable = false, length = 150)
    private String label;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column
    private Boolean active = true;

    public FilterOption() {}

    public FilterOption(FilterGroup group, String value, String label, int sortOrder) {
        this.group = group;
        this.value = value;
        this.label = label;
        this.sortOrder = sortOrder;
        this.active = true;
    }

    public Long getId() { return id; }
    public FilterGroup getGroup() { return group; }
    public void setGroup(FilterGroup group) { this.group = group; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
