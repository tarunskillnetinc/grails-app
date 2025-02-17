package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import grails.orm.HibernateCriteriaBuilder
import org.hibernate.Criteria
import org.hibernate.criterion.ProjectionList
import org.hibernate.criterion.Projections
import org.hibernate.sql.JoinType
import org.hibernate.transform.ResultTransformer
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials
import uk.co.wonderlane.wlpos.dataaccess.MySqlPoolDal
import uk.co.wonderlane.wlpos.entities.StoreConfig
import uk.co.wonderlane.wlpos.enums.wlim.ProductListType
import uk.co.wonderlane.wlpos.reporting.ReportColumns
import uk.co.wonderlane.wlpos.reporting.ReportType
import org.hibernate.criterion.Restrictions as HibernateRestrictions

@Transactional
class AmendableOrderService extends MySqlPoolDal {
    def springSecurityService
    def sessionFactory
    def gsonProvider

    AmendableOrderService(DatabaseCredentials databaseCredentials) {
        super(databaseCredentials)
    }

    @Transactional("reporting")
    def getColumns() {
        return ReportColumns.findByUserIdAndReportType(springSecurityService.principal.id, ReportType.AMENDABLE_ORDER_SEARCH)
    }

    @Transactional("reporting")
    def saveColumns(ReportColumns reportColumns) {
        reportColumns.save()
    }

    def search(categorySearchTerm, storeId) {
        def criteria = getCommonSearchCriteria()

        if (storeId) {
            criteria.add(HibernateRestrictions.eq("pl.store", Store.load((Integer) storeId)))
        }

        if (categorySearchTerm) {
            // If parent category is not null then match on parent category's description
            // if parent category is null (top level category) then match on child category's description
            def parentCategoryRestriction = HibernateRestrictions.and(HibernateRestrictions.isNotNull("pc.id"), HibernateRestrictions.like("pc.description", "%" + (String)categorySearchTerm + "%"))
            def childCategoryRestriction = HibernateRestrictions.and(HibernateRestrictions.isNull("pc.id"), HibernateRestrictions.like("c.description", "%" + (String)categorySearchTerm + "%"))
            criteria.add(HibernateRestrictions.or(parentCategoryRestriction, childCategoryRestriction))
        }

        criteria.setProjection(getOrderSearchProjections())
        criteria.setResultTransformer(getOrderSearchResultTransformer())

        return criteria.list()
    }

    def getOrdersForCategory(categoryId, sku, productDescription, deliveryDate) {
        def criteria = getCommonSearchCriteria()

        addCategoryOrderSearchCriteria(categoryId, criteria, sku, productDescription, deliveryDate)
        criteria.createAlias("packLines", "packl", JoinType.LEFT_OUTER_JOIN)
        criteria.createAlias("packl.pack", "pack", JoinType.LEFT_OUTER_JOIN)

        criteria.setProjection(getCategoryViewProjections())
        criteria.setResultTransformer(getCategoryResultsTransform())

        return criteria.list()
    }

    private void addCategoryOrderSearchCriteria(categoryId, Criteria criteria, sku, productDescription, deliveryDate) {
        // If category for the product has a parent category then match on that otherwise it's a top
        // level category and we should match on that id
        def parentCategoryRestriction = HibernateRestrictions.and(HibernateRestrictions.isNotNull("pc.id"),
                HibernateRestrictions.eq("pc.id", categoryId))
        def childCategoryRestriction = HibernateRestrictions.and(HibernateRestrictions.isNull("pc.id"),
                HibernateRestrictions.eq("c.id", categoryId))

        criteria.add(HibernateRestrictions.or(parentCategoryRestriction, childCategoryRestriction))

        if (sku) {
            HibernateRestrictions.like("pv.sku", "%" + (String) sku + "%")
        }

        if (productDescription) {
            HibernateRestrictions.like("p.description", "%" + (String) productDescription + "%")
        }

        if (deliveryDate) {
            criteria.add(HibernateRestrictions.eq("pl.endDate", DateTime.now(DateTimeZone.UTC)))
        }
    }

    private ProjectionList getOrderSearchProjections() {
        Projections.projectionList()
                .add(Projections.groupProperty("c.id"))
                .add(Projections.groupProperty("c.description"))
                .add(Projections.groupProperty("pc.id"))
                .add(Projections.groupProperty("pc.description"))
                .add(Projections.groupProperty("s.config"))
                .add(Projections.groupProperty("effectiveDate"))
    }

    private ProjectionList getCategoryViewProjections() {
        Projections.projectionList()
                .add(Projections.property("id"))
                .add(Projections.property("pv.sku"))
                .add(Projections.property("p.id"))
                .add(Projections.property("p.description"))
                .add(Projections.property("pv.price"))
                .add(Projections.property("pack.quantity"))
                .add(Projections.property("pl.endDate"))
                .add(Projections.property("quantity"))
                .add(Projections.property("pl.storeId"))

    }

    private ResultTransformer getCategoryResultsTransform() {
        new ResultTransformer() {
            @Override
            Object transformTuple(
                    Object[] tuple,
                    String[] aliases) {
                ProductStock productStock = ProductStock.findBySkuAndStoreId(tuple[1], tuple[9])
                return new AmendableOrderController.AmendOrderCommand(
                        productListItemId: tuple[0],
                        sku: tuple[1],
                        demand: CurrentSalesForecast.findByProduct(Product.load(tuple[2]))?.currentForecast,
                        available: productStock == null ? BigDecimal.ZERO : productStock.quantityDelivered.add(productStock.quantityInStock),
                        productDescription: tuple[3],
                        price: tuple[4],
                        packQuantity: tuple[5],
                        deliveryDate: tuple[6],
                        originalOrderQuantity: tuple[7],
                        amendedOrderQuantity: tuple[8]
                )
            }

            @Override
            List transformList(List tuples) {
                return tuples
            }
        }
    }

    private ResultTransformer getOrderSearchResultTransformer() {
        new ResultTransformer() {
            @Override
            Object transformTuple(
                    Object[] tuple,
                    String[] aliases) {
                def categoryId = tuple[2] == null ? tuple[0] : tuple[2]
                def categoryDescription = tuple[3] == null ? tuple[1] : tuple[3]
                return new AmendableOrderSearch(
                        categoryId: categoryId,
                        categoryDescription: categoryDescription,
                        storeNumber: gsonProvider.gson.fromJson(tuple[4], StoreConfig.class).storeNumber,
                        amendableDate: tuple[5]
                );
            }

            @Override
            List transformList(List tuples) {
                return tuples
            }
        }
    }

    private Criteria getCommonSearchCriteria() {
        def criteria = new HibernateCriteriaBuilder(ProductListItem.class, sessionFactory).buildCriteria {}

        criteria = criteria.createAlias("productList", "pl")
                .createAlias("pl.store", "s")
                .createAlias("productVariant", "pv")
                .createAlias("pv.product", "p")
                .createAlias("p.category", "c")
                .createAlias("c.parentCategory", "pc", JoinType.LEFT_OUTER_JOIN)

        // Check that productlistitem is amendable today
        criteria.add(HibernateRestrictions.eq("effectiveDate", new DateTime().withTimeAtStartOfDay()))

        // Check that productlist is amendable and delivery date is in future
        criteria.add(HibernateRestrictions.eq("pl.type", ProductListType.AMENDABLE_ORDER))
        criteria.add(HibernateRestrictions.ge("pl.endDate", DateTime.now(DateTimeZone.UTC)))

        return criteria
    }

    class AmendableOrderSearch {
        Integer categoryId
        String categoryDescription
        String storeNumber
        DateTime amendableDate
    }
}
