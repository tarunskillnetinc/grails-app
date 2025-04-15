package uk.co.wonderlane.wlpos

import com.google.gson.reflect.TypeToken
import grails.gorm.transactions.Transactional
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials
import uk.co.wonderlane.wlpos.dataaccess.MySqlDal
import uk.co.wonderlane.wlpos.entities.EnableHours
import uk.co.wonderlane.wlpos.entities.OpeningHours
import uk.co.wonderlane.wlpos.entities.OpeningTime
import uk.co.wonderlane.wlpos.entities.OpeningTimeOverride
import uk.co.wonderlane.wlpos.entities.StoreAdditionalDetail
import uk.co.wonderlane.wlpos.entities.StoreLicencing
import uk.co.wonderlane.wlpos.entities.StoreOtherRestrictions
import uk.co.wonderlane.wlpos.entities.StoreRestrictedHours

import java.lang.reflect.Type
import java.sql.CallableStatement
import java.sql.Connection
import java.sql.Types
import java.util.stream.Collectors

@Transactional
class StoreService extends MySqlDal {

    def springSecurityService
    def gsonProvider

    StoreService(DatabaseCredentials databaseCredentials) {
        super(databaseCredentials)
    }

    def getStore(int retailerId, int storeId) {
        return Store.findByRetailerIdAndId(retailerId, storeId)
    }

    def getStores(Collection<Integer> storeIds) {
        return Store.findAll("FROM Store s WHERE s.retailerId = :retailerId AND id IN (:storeIds) ORDER BY s.id DESC", [retailerId: springSecurityService.principal.retailerId, storeIds: storeIds])
    }

    def getStoreByStoreNumber(int retailerId, Integer storeNumber) {
        return Store.find("FROM Store s WHERE s.retailerId = :retailerId AND (JSON_EXTRACT(config, '\$.storeNumber') = :storeNumber OR (:storeNumber IS NULL AND JSON_EXTRACT(config, '\$.storeType') = 'HEAD_OFFICE')) ORDER BY s.id DESC", [retailerId: retailerId, storeNumber: storeNumber])
    }

    def getStoreIdByStoreNumber(Integer storeNumber) {
        def store = getStoreByStoreNumber(springSecurityService.principal.retailerId, storeNumber)
        return store?.id
    }

    def getStores(int retailerId) {
        return Store.findAll("FROM Store s WHERE s.retailerId = :retailerId AND JSON_EXTRACT(config, '\$.storeType') != 'HEAD_OFFICE' ORDER BY s.id DESC", [retailerId: retailerId])
    }

    def getActiveStores(int retailerId) {
        return Store.findAll("FROM Store s WHERE s.retailerId = :retailerId AND s.deleted = 0 AND JSON_EXTRACT(config, '\$.storeType') != 'HEAD_OFFICE' ORDER BY s.id DESC", [retailerId: retailerId])
    }

    def getStoresByType(int retailerId, StoreType storeType) {
        return Store.findAll("FROM Store s WHERE s.retailerId = :retailerId AND JSON_EXTRACT(config, '\$.storeType') = :storeType ORDER BY s.id DESC", [retailerId: retailerId, storeType: storeType.name()])
    }

    def getStoresByRange(int retailerId, Range range) {
        return Store.findAll("FROM Store s WHERE s.retailerId = :retailerId AND s.range = :range AND JSON_EXTRACT(config, '\$.storeType') != 'HEAD_OFFICE' ORDER BY s.id DESC", [retailerId: retailerId, range: range])
    }

    def getStoresByPriceBand(int retailerId, PriceBand priceBand) {
        return Store.findAll("FROM Store s WHERE s.retailerId = :retailerId AND s.priceBand = :priceBand AND JSON_EXTRACT(config, '\$.storeType') != 'HEAD_OFFICE' ORDER BY s.id DESC", [retailerId: retailerId, priceBand: priceBand])
    }

    def searchStores(int retailerId, Integer storeNumber, String storeName, boolean showDeleted, params) {
        String sortColumn = "JSON_EXTRACT(s.config, '\$.storeNumber')"

        if (params.sort == "storeName") {
            sortColumn = "JSON_EXTRACT(s.config, '\$.storeName')"
        } else if (params.sort == "storeType") {
            sortColumn = "JSON_EXTRACT(s.config, '\$.storeType')"
        }

        def queryParams = [retailerId: retailerId]
        def sortParams = [max: params.max, offset: params.offset, sort: sortColumn, order: params.order]

        if (storeNumber) {
            queryParams.storeNumber = storeNumber
        }
        if (storeName) {
            queryParams.storeName = "%$storeName%"
        }

        String queryString = """FROM Store s 
                                WHERE s.retailerId = :retailerId
                                AND JSON_EXTRACT(s.config, '\$.storeType') != 'HEAD_OFFICE' 
                                ${(showDeleted ? "" : " AND s.deleted = false")}
                                ${(storeNumber ? " AND JSON_EXTRACT(s.config, '\$.storeNumber') = :storeNumber" : "")} 
                                ${(storeName ? " AND JSON_EXTRACT(s.config, '\$.storeName') LIKE :storeName" : "")}
                                ORDER BY ${sortParams.sort} ${sortParams.order}"""

        def stores = Store.findAll(queryString, queryParams, sortParams)
        def storeCount = Store.executeQuery("SELECT COUNT(s) " +queryString, queryParams)

        return [stores, storeCount.first()]
    }

    def saveStore(StoreCommand store, String configString, String storeAdditionalDetail, String openingHoursString, String storeLicensing, String storeRestrictions) {
        return doSaveStore(store, configString, storeAdditionalDetail, openingHoursString, storeLicensing, storeRestrictions)
    }

    // Needs to not be transactional otherwise Hibernate tries to save the store object rather than allowing the stored procedure to do it (well, it does both).
    @Transactional (readOnly = true)
    def saveStore(Store store) {
        return doSaveStore(store, store.configString, store.additionalDetails, store.openingHoursString, store.licencingString, store.storeRestrictions)
    }

    private void doSaveStore(def store, String configString, String storeAdditionalDetail, String openingHours, String storeLicensing, String storeRestrictions) {
        Connection conn = getConnection()
        CallableStatement cstmt = conn.prepareCall("{ call saveStore(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) }")

        try {
            if (store.id && store.id > 0) {
                cstmt.setInt(1, store.id)
            } else {
                cstmt.setNull(1, Types.INTEGER)
            }

            cstmt.setInt(2, springSecurityService.principal.retailerId)
            if (store.parentStoreId) {
                cstmt.setInt(3, store.parentStoreId)
            } else {
                cstmt.setNull(3, Types.INTEGER)
            }
            cstmt.setInt(4, springSecurityService.principal.id)
            cstmt.setInt(5, store.priceBand.id)
            cstmt.setInt(6, store.range.id)
            cstmt.setString(7, store.retailerStoreId)
            cstmt.setBoolean(8, store.deleted)
            cstmt.setString(9, configString)
            cstmt.setString(10, storeAdditionalDetail)
            cstmt.setString(11, openingHours)
            cstmt.setString(12, storeLicensing)
            cstmt.setString(13, storeRestrictions)
            cstmt.executeUpdate()

            def resultSet = cstmt.getResultSet()
            if (resultSet.next()) {
                store.id = resultSet.getInt("storeId")
            }
        } finally {
            cstmt.close()
            conn.close()
        }
    }

    String getAdditionalDetailsJsonString(List<StoreAdditionalDetailCommand> storeAdditionalDetails){
        List<StoreAdditionalDetail> storeAdditionalDetailList  = convertToStoreAdditionalDetailList(storeAdditionalDetails)
        Type listType = new TypeToken<List<StoreAdditionalDetail>>(){}.getType()
        return gsonProvider.gson.toJson(storeAdditionalDetailList, listType)
    }

     def sortAdditionalDetails(def details) {
        if (details == null || details.isEmpty()) {
            return
        }

        return details.sort { a, b ->
            (a?.description ?: "").compareToIgnoreCase(b?.description ?: "")
        }
    }

    def getOpeningHoursAsObject(StoreOpeningHoursCommand storeOpeningHoursCommand) {
        OpeningHours openingHours = new OpeningHours()
        if (storeOpeningHoursCommand != null) {
            storeOpeningHoursCommand.getRegularHours()?.forEach {regHours -> {
                switch (regHours.day) {
                    case "Monday":
                        openingHours.monday = regularHoursMap(regHours)
                        break
                    case "Tuesday":
                        openingHours.tuesday = regularHoursMap(regHours)
                        break
                    case "Wednesday":
                        openingHours.wednesday = regularHoursMap(regHours)
                        break
                    case "Thursday":
                        openingHours.thursday = regularHoursMap(regHours)
                        break
                    case "Friday":
                        openingHours.friday = regularHoursMap(regHours)
                        break
                    case "Saturday":
                        openingHours.saturday = regularHoursMap(regHours)
                        break
                    case "Sunday":
                        openingHours.sunday = regularHoursMap(regHours)
                        break
                }
            }}

            openingHours.specialOpeningHours = new ArrayList<>()
            storeOpeningHoursCommand.getSpecialOpeningHours()?.forEach {specialHours ->{
                if (specialHours != null) {
                    openingHours.specialOpeningHours.add(openingHoursOverrideMap(specialHours))
                }
            }}
        }
        return openingHours
    }

    def getAlcoholLicensingCommandAsObject(AlcoholLicensingCommand alcoholLicensingCommand) {
        StoreLicencing storeLicencing = new StoreLicencing()
        if (alcoholLicensingCommand != null) {
            storeLicencing.setAlcoholLicensingHours(getOpeningHoursAsObject(alcoholLicensingCommand))
            storeLicencing.setLicensedToSellAlcohol(alcoholLicensingCommand.licensedToSellAlcohol)
        }
        return storeLicencing
    }

    def getStoreRestrictionsCommandAsObject(StoreRestrictionsCommand storeRestrictionsCommand) {
        StoreRestrictedHours storeRestrictedHours = new StoreRestrictedHours()
        if (storeRestrictionsCommand != null) {
            storeRestrictionsCommand.getRegularHours()?.forEach { enableHours ->
                {
                    switch (enableHours.day) {
                        case "Monday":
                            storeRestrictedHours.monday = getEnableHoursAsObject(enableHours)
                            break
                        case "Tuesday":
                            storeRestrictedHours.tuesday = getEnableHoursAsObject(enableHours)
                            break
                        case "Wednesday":
                            storeRestrictedHours.wednesday = getEnableHoursAsObject(enableHours)
                            break
                        case "Thursday":
                            storeRestrictedHours.thursday = getEnableHoursAsObject(enableHours)
                            break
                        case "Friday":
                            storeRestrictedHours.friday = getEnableHoursAsObject(enableHours)
                            break
                        case "Saturday":
                            storeRestrictedHours.saturday = getEnableHoursAsObject(enableHours)
                            break
                        case "Sunday":
                            storeRestrictedHours.sunday = getEnableHoursAsObject(enableHours)
                            break
                    }
                }
            }
            storeRestrictedHours.storeOtherRestrictions = new ArrayList<>()
            storeRestrictionsCommand.getOtherRestrictions()?.forEach { otherRestrictions ->
                {
                    if (otherRestrictions != null) {
                        storeRestrictedHours.storeOtherRestrictions.add(getStoreOtherRestrictionAsObject(otherRestrictions))
                    }
                }
            }
            return storeRestrictedHours
        }
    }


    StoreOpeningHoursCommand convertToStoreOpeningHoursCommand(OpeningHours openingHours) {
        StoreOpeningHoursCommand command = new StoreOpeningHoursCommand()

        // Convert regular hours
        if (openingHours != null) {
            List<OpeningTimeCommand> regularHours = new ArrayList<>();
            addOpeningTimeToCmd(regularHours, "Monday", openingHours.getMonday())
            addOpeningTimeToCmd(regularHours, "Tuesday", openingHours.getTuesday())
            addOpeningTimeToCmd(regularHours, "Wednesday", openingHours.getWednesday())
            addOpeningTimeToCmd(regularHours, "Thursday", openingHours.getThursday())
            addOpeningTimeToCmd(regularHours, "Friday", openingHours.getFriday())
            addOpeningTimeToCmd(regularHours, "Saturday", openingHours.getSaturday())
            addOpeningTimeToCmd(regularHours, "Sunday", openingHours.getSunday())
            command.setRegularHours(regularHours)

            // Convert special opening hours
            List<OpeningTimeOverrideCommand> specialHours = new ArrayList<>();
            if (openingHours.getSpecialOpeningHours() != null) {
                for (OpeningTimeOverride override : openingHours.getSpecialOpeningHours()) {
                    OpeningTimeOverrideCommand overrideCommand = new OpeningTimeOverrideCommand()
                    overrideCommand.description = override.description
                    if (override.getDate() != null) {
                        overrideCommand.setDate(override.getDate().toString("yyyy-MM-dd"))
                    }
                    overrideCommand.setStartTime(override.getStartTime() != null ? override.getStartTime().toString("HH:mm") : null)
                    overrideCommand.setEndTime(override.getEndTime() != null ? override.getEndTime().toString("HH:mm") : null)
                    overrideCommand.setClosed(override.isClosed())
                    specialHours.add(overrideCommand)
                }
            }
            command.setSpecialOpeningHours(specialHours)
        } else {
            command.setRegularHours([
                    new OpeningTimeCommand(day: 'Monday', startTime: '', endTime: '', closed: false),
                    new OpeningTimeCommand(day: 'Tuesday', startTime: '', endTime: '', closed: false),
                    new OpeningTimeCommand(day: 'Wednesday', startTime: '', endTime: '', closed: false),
                    new OpeningTimeCommand(day: 'Thursday', startTime: '', endTime: '', closed: false),
                    new OpeningTimeCommand(day: 'Friday', startTime: '', endTime: '', closed: false),
                    new OpeningTimeCommand(day: 'Saturday', startTime: '', endTime: '', closed: false),
                    new OpeningTimeCommand(day: 'Sunday', startTime: '', endTime: '', closed: false)
            ])
        }

        return command
    }

    AlcoholLicensingCommand convertToAlcoholLicensingCommand(StoreLicencing storeLicencing) {
        StoreOpeningHoursCommand storeOpeningHoursCommand = convertToStoreOpeningHoursCommand(storeLicencing?.alcoholLicensingHours);
        return new AlcoholLicensingCommand(
                licensedToSellAlcohol: storeLicencing?.licensedToSellAlcohol,
                regularHours: storeOpeningHoursCommand?.regularHours,
                specialOpeningHours: storeOpeningHoursCommand?.specialOpeningHours
        )
    }

    StoreRestrictionsCommand convertToStoreRestrictionCommand(StoreRestrictedHours storeRestrictedHours){
        StoreRestrictionsCommand storeRestrictionsCommand = new StoreRestrictionsCommand()
        if (storeRestrictedHours != null) {
            List<EnableHoursCommand> regularHours = new ArrayList<>();
            addEnabledTimeToCmd(regularHours, "Monday", storeRestrictedHours.getMonday())
            addEnabledTimeToCmd(regularHours, "Tuesday", storeRestrictedHours.getTuesday())
            addEnabledTimeToCmd(regularHours, "Wednesday", storeRestrictedHours.getWednesday())
            addEnabledTimeToCmd(regularHours, "Thursday", storeRestrictedHours.getThursday())
            addEnabledTimeToCmd(regularHours, "Friday", storeRestrictedHours.getFriday())
            addEnabledTimeToCmd(regularHours, "Saturday", storeRestrictedHours.getSaturday())
            addEnabledTimeToCmd(regularHours, "Sunday", storeRestrictedHours.getSunday())
            storeRestrictionsCommand.setRegularHours(regularHours)

            List<StoreOtherRestrictionsCommand> otherRestrictions = new ArrayList<>();
            if (storeRestrictedHours.getStoreOtherRestrictions() != null) {
                for (StoreOtherRestrictions override : storeRestrictedHours.getStoreOtherRestrictions()) {
                    StoreOtherRestrictionsCommand overrideCommand = new StoreOtherRestrictionsCommand()
                    overrideCommand.description = override.description
                    if (override.getStartDateTime() != null) {
                        overrideCommand.setStartDateTime(override.getStartDateTime().toString("dd/MM/YYYY HH:mm"))
                    }
                    if (override.getEndDateTime() != null) {
                        overrideCommand.setEndDateTime(override.getEndDateTime().toString("dd/MM/YYYY HH:mm"))
                    }
                    otherRestrictions.add(overrideCommand)
                }
            }
            storeRestrictionsCommand.setOtherRestrictions(otherRestrictions)
        } else {
            storeRestrictionsCommand.setRegularHours([
                    new EnableHoursCommand(day: 'Monday', timeFrom: '', timeTo: '', restrictionEnabled: false),
                    new EnableHoursCommand(day: 'Tuesday', timeFrom: '', timeTo: '', restrictionEnabled: false),
                    new EnableHoursCommand(day: 'Wednesday', timeFrom: '', timeTo: '', restrictionEnabled: false),
                    new EnableHoursCommand(day: 'Thursday', timeFrom: '', timeTo: '', restrictionEnabled: false),
                    new EnableHoursCommand(day: 'Friday', timeFrom: '', timeTo: '', restrictionEnabled: false),
                    new EnableHoursCommand(day: 'Saturday', timeFrom: '', timeTo: '', restrictionEnabled: false),
                    new EnableHoursCommand(day: 'Sunday', timeFrom: '', timeTo: '', restrictionEnabled: false)
            ])
        }
        return storeRestrictionsCommand
    }

    private EnableHoursCommand getEnableHoursCommand(String day, EnableHours enableHours){
        if (enableHours != null) {
            return enableHours
        } else {
            new EnableHoursCommand(day: day, timeFrom: '', timeTo: '', restrictionEnabled: false)
        }
    }

    private void addOpeningTimeToCmd(List<OpeningTimeCommand> regularHours, String day, OpeningTime openingTime) {
        OpeningTimeCommand command = new OpeningTimeCommand()
        command.setDay(day)

        if (openingTime != null) {
            command.setStartTime(openingTime.getStartTime() != null ? openingTime.getStartTime().toString("HH:mm") : null)
            command.setEndTime(openingTime.getEndTime() != null ? openingTime.getEndTime().toString("HH:mm") : null)
            command.setClosed(openingTime.isClosed())
        } else {
            command.setStartTime(null)
            command.setEndTime(null)
            command.setClosed(false)
        }

        regularHours.add(command)
    }

    def sortStoreRestrictions(StoreRestrictionsCommand restrictions) {
        if (restrictions == null) {
            return null
        }

        // Filter out empty items from otherRestrictions
        if (restrictions.otherRestrictions) {
            restrictions.otherRestrictions = restrictions.otherRestrictions.findAll { restriction ->
                restriction != null
            }

            // Sort the filtered list by description (case-insensitive)
            restrictions.otherRestrictions.sort { a, b ->
                (a?.description ?: "").compareToIgnoreCase(b?.description ?: "")
            }
        }

        // Return the complete StoreRestrictionsCommand object
        return restrictions
    }

    private OpeningTime regularHoursMap(OpeningTimeCommand openingTimeCommand) {
        OpeningTime openingTime = new OpeningTime()
        DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm")
        openingTime.startTime = openingTimeCommand.startTime ? formatter.parseLocalTime(openingTimeCommand.startTime) : null
        openingTime.endTime = openingTimeCommand.endTime ? formatter.parseLocalTime(openingTimeCommand.endTime) : null
        openingTime.closed = openingTimeCommand.closed
        return openingTime
    }

    private OpeningTimeOverride openingHoursOverrideMap(OpeningTimeOverrideCommand openingTimeOverrideCommand) {
        if (openingTimeOverrideCommand == null) {
            return null
        }
        DateTimeFormatter formatterDate = DateTimeFormat.forPattern("yyyy-MM-dd");
        DateTimeFormatter formatterTime = DateTimeFormat.forPattern("HH:mm")
        OpeningTimeOverride openingTimeOverride = new OpeningTimeOverride()
        openingTimeOverride.description = openingTimeOverrideCommand?.description
        openingTimeOverride.date = openingTimeOverrideCommand?.date ? formatterDate.parseLocalDate(openingTimeOverrideCommand.date) : null
        openingTimeOverride.startTime = openingTimeOverrideCommand?.startTime ? formatterTime.parseLocalTime(openingTimeOverrideCommand.startTime) : null
        openingTimeOverride.endTime = openingTimeOverrideCommand?.endTime ? formatterTime.parseLocalTime(openingTimeOverrideCommand.endTime) : null
        openingTimeOverride.closed = openingTimeOverrideCommand?.closed
        return openingTimeOverride
    }

    private List<StoreAdditionalDetail> convertToStoreAdditionalDetailList(List<StoreAdditionalDetailCommand> storeAdditionalDetails) {
        return storeAdditionalDetails.stream()
                .map(command -> {
                    if (command != null) {
                        StoreAdditionalDetail detail = new StoreAdditionalDetail();
                        detail.setDescription(command.getDescription());
                        detail.setValue(command.getValue());
                        return detail;
                    }
                })
                .collect(Collectors.toList());
    }

    private void addEnabledTimeToCmd(List<EnableHoursCommand> regularHours, String day, EnableHours enableHours) {
        EnableHoursCommand command = new EnableHoursCommand()
        command.setDay(day)

        if (enableHours != null) {
            DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm")
            LocalTime timeFrom = enableHours.getTimeFrom()
            if (timeFrom != null) {
                command.setTimeFrom(formatter.print(timeFrom))
            } else {
                command.setTimeFrom(null)
            }

            LocalTime timeTo = enableHours.getTimeTo()
            if (timeTo != null) {
                command.setTimeTo(formatter.print(timeTo))
            } else {
                command.setTimeTo(null)
            }
            command.setRestrictionEnabled(enableHours.isRestrictionEnabled())
        } else {
            command.setTimeFrom(null)
            command.setTimeTo(null)
            command.setRestrictionEnabled(false)
        }

        regularHours.add(command)
    }

    private EnableHours getEnableHoursAsObject(EnableHoursCommand enableHoursCommand) {
        EnableHours enableHours = new EnableHours()
        if (enableHoursCommand != null) {
            DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm")
            enableHours.timeFrom = enableHoursCommand.timeFrom ? formatter.parseLocalTime(enableHoursCommand.timeFrom) : null
            enableHours.timeTo = enableHoursCommand.timeTo ? formatter.parseLocalTime(enableHoursCommand.timeTo) : null
            enableHours.restrictionEnabled = enableHoursCommand.restrictionEnabled
        }
        return enableHours
    }

    private StoreOtherRestrictions getStoreOtherRestrictionAsObject(StoreOtherRestrictionsCommand storeOtherRestrictionsCommand) {
        if (storeOtherRestrictionsCommand == null) {
            return null
        }
        DateTimeFormatter formatterDate = DateTimeFormat.forPattern("dd/MM/YYYY HH:mm")
        StoreOtherRestrictions storeOtherRestrictions = new StoreOtherRestrictions()
        storeOtherRestrictions.description = storeOtherRestrictionsCommand?.description
        storeOtherRestrictions.startDateTime = storeOtherRestrictionsCommand?.startDateTime ? formatterDate.parseDateTime(storeOtherRestrictionsCommand?.startDateTime) : null
        storeOtherRestrictions.endDateTime = storeOtherRestrictionsCommand?.endDateTime ? formatterDate.parseDateTime(storeOtherRestrictionsCommand?.endDateTime) : null
        return storeOtherRestrictions
    }
}