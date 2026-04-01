package Nhom08.Project.controller;

import Nhom08.Project.entity.FilterGroup;
import Nhom08.Project.entity.FilterOption;
import Nhom08.Project.repository.FilterGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/filters")
public class FilterController {

    @Autowired
    private FilterGroupRepository filterGroupRepository;

    /**
     * Trả về tất cả filter groups kèm options — public, không cần auth.
     * Response:
     * [
     *   { "code": "salary", "name": "Mức lương", "options": [ { "value": "...", "label": "..." }, ... ] },
     *   ...
     * ]
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getFilters() {
        List<FilterGroup> groups = filterGroupRepository.findByActiveTrueOrderBySortOrderAsc();

        List<Map<String, Object>> result = groups.stream().map(g -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("code", g.getCode());
            map.put("name", g.getName());

            List<Map<String, String>> opts = g.getOptions() == null ? List.of() :
                g.getOptions().stream()
                    .filter(o -> Boolean.TRUE.equals(o.getActive()))
                    .map(o -> Map.of("value", o.getValue(), "label", o.getLabel()))
                    .collect(Collectors.toList());
            map.put("options", opts);
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }
}
