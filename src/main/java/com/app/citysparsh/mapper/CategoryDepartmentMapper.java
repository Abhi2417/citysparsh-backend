package com.app.citysparsh.mapper;

import com.app.citysparsh.model.ComplaintCategory;
import com.app.citysparsh.model.OfficerDepartment;

import java.util.Map;

public class CategoryDepartmentMapper {

    // Each category maps to exactly one department
    private static final Map<ComplaintCategory, OfficerDepartment> CATEGORY_TO_DEPARTMENT = Map.of(
            ComplaintCategory.ROADS_AND_POTHOLES,  OfficerDepartment.ROADS_AND_TRANSPORTATION,
            ComplaintCategory.WATER_AND_DRAINAGE,  OfficerDepartment.WATER_SUPPLY_AND_DRAINAGE,
            ComplaintCategory.STREET_LIGHTING,     OfficerDepartment.ELECTRICAL_SERVICES,
            ComplaintCategory.WASTE_MANAGEMENT,    OfficerDepartment.SANITATION_AND_WASTE_MANAGEMENT,
            ComplaintCategory.PARKS_AND_GREENERY,  OfficerDepartment.PARKS_AND_HORTICULTURE,
            ComplaintCategory.PUBLIC_BUILDINGS,    OfficerDepartment.BUILDINGS_AND_MAINTENANCE,
            ComplaintCategory.OTHER,               OfficerDepartment.GENERAL
    );

    // Get the required department for a given category
    public static OfficerDepartment getDepartmentFor(ComplaintCategory category) {
        return CATEGORY_TO_DEPARTMENT.getOrDefault(category, OfficerDepartment.GENERAL);
    }

    // Check if an officer's department can handle this category
    public static boolean canHandle(OfficerDepartment department, ComplaintCategory category) {
        OfficerDepartment required = getDepartmentFor(category);
        // GENERAL department can handle anything
        return department == required || department == OfficerDepartment.GENERAL;
    }
}
