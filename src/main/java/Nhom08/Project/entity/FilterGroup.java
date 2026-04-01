package Nhom08.Project.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "filter_groups")
public class FilterGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String code; // salary, level, posted_within, employment_type, experience, job_rank

    @Column(nullable = false, length = 100)
    private String name; // Mức lương, Cấp bậc, ...

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column
    private Boolean active = true;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC")
    private List<FilterOption> options;

    public FilterGroup() {}

    public FilterGroup(String code, String name, int sortOrder) {
        this.code = code;
        this.name = name;
        this.sortOrder = sortOrder;
        this.active = true;
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public List<FilterOption> getOptions() { return options; }
    public void setOptions(List<FilterOption> options) { this.options = options; }
}
