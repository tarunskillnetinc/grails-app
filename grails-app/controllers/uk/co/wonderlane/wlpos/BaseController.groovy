package uk.co.wonderlane.wlpos

import grails.util.Pair
import groovy.json.JsonSlurper
import uk.co.wonderlane.wlpos.reporting.ReportColumn
import uk.co.wonderlane.wlpos.reporting.ReportColumns
import uk.co.wonderlane.wlpos.reporting.ReportType

abstract class BaseController {
    protected enum SearchType {
        PRODUCT,
        CATEGORY
    }

    def productService
    def categoryService

    abstract getColumns()

    def ajaxSaveColumns() {
        try {
            if (params.reportColumns && params.reportType) {
                def userReportColumns = new JsonSlurper().parseText(params.reportColumns)
                def reportType = ReportType.valueOf(params.reportType)

                def reportColumns = getColumns()

                if (!reportColumns) {
                    reportColumns = new ReportColumns(userId: springSecurityService.principal.id, reportType: reportType)
                }

                userReportColumns?.each { userReportColumn ->
                    if (reportColumns?.columns?.find { it.column == userReportColumn.key }) {
                        reportColumns?.columns?.find { it.column == userReportColumn.key }?.enabled = userReportColumn.value
                    } else {
                        reportColumns.addToColumns(new ReportColumn(column: userReportColumn.key, enabled: userReportColumn.value))
                    }
                }

                if (reportType == ReportType.PRODUCT_SEARCH) {
                    productService.saveColumns(reportColumns)
                } else if (reportType == ReportType.CATEGORY_SEARCH) {
                    categoryService.saveColumns(reportColumns)
                }

                render(status: 200)
            }
        } catch (Exception e) {
            e.printStackTrace()
            render(status: 500, text: "An error occurred saving your report column preferences.")
        }
    }

    protected Pair<List<Category>, List<Integer>> baseSearchCategories(String searchTerm) {
        def topLevelCategories = []
        def productCategoryList = []
        boolean isSearch = searchTerm?.length() > 0

        // If no search term is provided then we should reset this back to default (i.e. just the top level departments).
        if (isSearch) {
            def categories = categoryService.searchCategories(searchTerm)
            productCategoryList.addAll(categories?.collect { it.id })
            categories?.each {
                addCategoriesHierarchy(topLevelCategories, productCategoryList, it)
            }
        } else {
            topLevelCategories = categoryService.getTopLevelCategories()
        }
        return new Pair<List<Category>, List<Integer>>(topLevelCategories, productCategoryList)
    }

    protected Pair<List<Category>, List<Integer>> baseSearchForCategories(String searchTerm, String categoryCode) {
        def topLevelCategories = []
        def productCategoryList = []
        boolean isSearch = searchTerm?.length() > 0 || categoryCode?.length() > 0

        // If no search term is provided then we should reset this back to default (i.e. just the top level departments).
        if (isSearch) {
            def categories = categoryService.searchForCategories(searchTerm, categoryCode)
            productCategoryList.addAll(categories?.collect { it.id })
            categories?.each {
                addCategoriesHierarchy(topLevelCategories, productCategoryList, it, [])
            }
        } else {
            topLevelCategories = categoryService.getTopLevelCategories()
        }
        return new Pair<List<Category>, List<Integer>>(topLevelCategories, productCategoryList)
    }

    protected void addCategoriesHierarchy(List topCategories, List productCategoryList, Category category, List<Integer> processedCategories) {
        if (processedCategories.contains(category.id)) {
            return
        }

        processedCategories.add(category.id)

        if (category.parentCategory) {
            if (category.parentCategory.id == category.id) {
                return
            }
            productCategoryList.add(category.parentCategory.id)
            addCategoriesHierarchy(topCategories, productCategoryList, category.parentCategory, processedCategories)
        } else {
            topCategories.add(category)
        }
        
        category.childCategories?.each { childCategory ->
            productCategoryList.add(childCategory.id)
            addCategoriesHierarchy(topCategories, productCategoryList, childCategory, processedCategories)
        }
    }
}