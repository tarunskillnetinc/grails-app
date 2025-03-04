package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import grails.orm.HibernateCriteriaBuilder
import grails.validation.Validateable
import org.hibernate.Criteria
import org.hibernate.criterion.ProjectionList
import org.hibernate.criterion.Projections
import org.hibernate.sql.JoinType
import org.hibernate.transform.ResultTransformer
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials
import uk.co.wonderlane.wlpos.dataaccess.MySqlPoolDal
import uk.co.wonderlane.wlpos.entities.StoreConfig
import uk.co.wonderlane.wlpos.enums.PromotionType
import uk.co.wonderlane.wlpos.enums.wlim.ProductListType
import uk.co.wonderlane.wlpos.reporting.ReportColumns
import uk.co.wonderlane.wlpos.reporting.ReportType
import org.hibernate.criterion.Restrictions as HibernateRestrictions

import java.math.RoundingMode

@Transactional
class AmendableOrderService extends MySqlPoolDal {
    def springSecurityService
    def sessionFactory
    def gsonProvider
    def productListService

    AmendableOrderService(DatabaseCredentials databaseCredentials) {
        super(databaseCredentials)
    }

    @Transactional("reporting")
    def getColumns() {
        return ReportColumns.findByUserIdAndReportType(springSecurityService.principal.id, ReportType.AMENDABLE_ORDER_SEARCH)
    }

    @Transactional("reporting")
    def getCategoryViewColumns() {
        return ReportColumns.findByUserIdAndReportType(springSecurityService.principal.id, ReportType.AMENDABLE_ORDER_VIEW_CATEGORY)
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

    def getOrdersForCategory(categoryId, sku, productDescription, deliveryDate, storeId) {
        def criteria = getCommonSearchCriteria()

        criteria = addCategoryOrderSearchCriteria(categoryId, criteria, sku, productDescription, deliveryDate, storeId)
        criteria = criteria.createAlias("packLines", "packl", JoinType.LEFT_OUTER_JOIN)
        criteria = criteria.createAlias("packl.pack", "pack", JoinType.LEFT_OUTER_JOIN)

        criteria = criteria.setProjection(getCategoryViewProjections())
        criteria = criteria.setResultTransformer(getCategoryResultsTransform())

        return criteria.list()
    }

    def saveAmendedQuantity(AmendedLine amendedLine) {
        def productListItem = ProductListItem.findById(amendedLine.productListItemId)

        // Store quantity before so we'll know whether to send out a new message
        def quantityBefore = productListItem.quantity
        productListItem.quantity = amendedLine.amendedOrderQuantity
        productListItem.save(flush: true, failOnError: true)

        def productList = productListItem.productList.getProductList(null, springSecurityService.principal.storeId)
        def commonProductListItem = productListItem.getProductListItem(null, springSecurityService.principal.storeId)
        commonProductListItem.setOriginalQuantity(amendedLine.originalOrderQuantity)

        if(amendedLine.amendedOrderQuantity != quantityBefore) {
            // We're only planning to send the productListItem that has changed as part of the stock transaction
            productList.productListItems = new ArrayList<>()
            productList.productListItems.add(commonProductListItem)
            productListService.sendProductListExportRequest(productList)
        }
    }

    private static Criteria addCategoryOrderSearchCriteria(categoryId, Criteria criteria, sku, productDescription, deliveryDate, storeId) {
        // If category for the product has a parent category then match on that otherwise it's a top
        // level category and we should match on that id
        def parentCategoryRestriction = HibernateRestrictions.and(HibernateRestrictions.isNotNull("pc.id"),
                HibernateRestrictions.eq("pc.id", categoryId))
        def childCategoryRestriction = HibernateRestrictions.and(HibernateRestrictions.isNull("pc.id"),
                HibernateRestrictions.eq("c.id", categoryId))

        criteria = criteria.add(HibernateRestrictions.or(parentCategoryRestriction, childCategoryRestriction))

        if (sku) {
            criteria = criteria.add(HibernateRestrictions.sqlRestriction("sku like '%" + (String) sku + "%'"))
        }

        if (productDescription) {
            criteria = criteria.add(HibernateRestrictions.like("p.description", "%" + (String) productDescription + "%"))
        }

        if (deliveryDate) {
            DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("dd/MM/YYYY").withZoneUTC()
            criteria = criteria.add(HibernateRestrictions.eq("pl.endDate", dateTimeFormatter.parseDateTime((String)deliveryDate)))
        }

        if (storeId) {
            criteria = criteria.add(HibernateRestrictions.eq("s.id", storeId))
        }

        return criteria
    }

    private ProjectionList getOrderSearchProjections() {
        Projections.projectionList()
                .add(Projections.groupProperty("c.id"))
                .add(Projections.groupProperty("c.description"))
                .add(Projections.groupProperty("pc.id"))
                .add(Projections.groupProperty("pc.description"))
                .add(Projections.groupProperty("s.config"))
                .add(Projections.groupProperty("effectiveDate"))
                .add(Projections.groupProperty("s.id"))
    }

    private ProjectionList getCategoryViewProjections() {
        Projections.projectionList()
                .add(Projections.property("id"))
                .add(Projections.property("pv.sku"))
                .add(Projections.property("p.id"))
                .add(Projections.property("p.description"))
                .add(Projections.property("productVariant"))
                .add(Projections.property("pack.quantity"))
                .add(Projections.property("pl.endDate"))
                .add(Projections.property("quantity"))
                .add(Projections.property("pl.store"))
                .add(Projections.property("fillQuantity"))

    }

    private ResultTransformer getCategoryResultsTransform() {
        new ResultTransformer() {
            @Override
            Object transformTuple(
                    Object[] tuple,
                    String[] aliases) {
                def store = ((Store)tuple[8])
                ProductStock productStock = ProductStock.findBySkuAndStoreId((long)tuple[1], store.id)

                def amendedLine = new AmendedLine(
                        productListItemId: tuple[0],
                        sku: tuple[1],
                        demand: CurrentSalesForecast.findByProduct(Product.load(tuple[2]))?.currentForecast,
                        available: productStock == null ? BigDecimal.ZERO : productStock.quantityDelivered.add(productStock.quantityInStock),
                        productDescription: tuple[3],
                        price: ((ProductVariant)tuple[4]).getCurrentPrice(store.priceBand),
                        packQuantity: tuple[5],
                        deliveryDate: tuple[6],
                        originalOrderQuantity: tuple[9],
                        amendedOrderQuantity: tuple[7]
                )

                amendedLine.convertQuantitiesToPackNumbers()

                return amendedLine
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
                        amendableDate: tuple[5],
                        storeId: tuple[6]
                )
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
        Integer storeId
    }

    class AmendedLine implements Validateable {
        Integer productListItemId
        String sku
        String productDescription
        BigDecimal price
        BigDecimal packQuantity
        DateTime deliveryDate
        BigDecimal originalOrderQuantity
        BigDecimal amendedOrderQuantity
        BigDecimal demand
        BigDecimal available

        void convertQuantitiesToPackNumbers() {
            demand = demand?.divide(packQuantity, 3 , RoundingMode.HALF_UP)
            available = available?.divide(packQuantity, 3, RoundingMode.HALF_UP)
        }

        static constraints = {
            amendedOrderQuantity nullable: false, min: 0.001, max:9999.999, scale: 3
        }
    }
}
