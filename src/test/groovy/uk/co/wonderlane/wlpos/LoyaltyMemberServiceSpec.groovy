package uk.co.wonderlane.wlpos

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import org.grails.datastore.mapping.query.api.BuildableCriteria
import spock.lang.Specification
import uk.co.wonderlane.wlpos.enums.MemberStatus
import uk.co.wonderlane.wlpos.helpers.HibernateTestMockCriteria
import uk.co.wonderlane.wlpos.loyalty.Member

class LoyaltyMemberServiceSpec extends Specification implements ServiceUnitTest<LoyaltyMemberService>, DataTest {
    Class<?>[] getDomainClassesToMock() {
        return [Member] as Class[]
    }

    def 'Should successfully find a member by email address'() {
        given:
        String emailAddress = "tester@example.com"
        Member member = generateDummyMember("1234567890123456", emailAddress)

        Member.metaClass.static.findAllByEmail = { String email ->
            if (email == member.email) {
                return [member]
            } else {
                return null
            }
        }

        when: 'Get member by card number action is executed'
        def memberReturned = service.findByEmail(emailAddress)

        then: 'successfully return the transaction'
        memberReturned != null
        memberReturned instanceof Map
        memberReturned.totalResults == 1

        cleanup:
        Member.metaClass = null
    }

    def 'Should successfully find a member by card number'() {
        given:
        String cardNumber = "1234567890123456"
        Member member = generateDummyMember(cardNumber, "john.doe@example.com")

        Member.metaClass.static.findByCardNumber = { String number ->
            if (number == member.cardNumber) {
                return member
            } else {
                return null
            }
        }

        when: 'Get member by card number action is executed'
        def memberReturned = service.findByCardNumber(cardNumber)

        then: 'successfully return the transaction'
        memberReturned != null
        memberReturned instanceof Member

        cleanup:
        Member.metaClass = null
    }

    def 'Should successfully find a member by card number wildcard'() {
        given:
        String cardNumber = "123456"
        Member member = generateDummyMember(cardNumber, "john.doe@example.com")

        HibernateTestMockCriteria mockCriteria = new HibernateTestMockCriteria()
        mockCriteria.getResponses().add(member)
        BuildableCriteria defaultCriteria = Member.createCriteria()
        Member.metaClass.static.createCriteria = { return mockCriteria }

        Member.metaClass.static.findAllByCardNumberLike = { String number ->
            return [member]
        }

        when: 'Get member by card number action is executed'
        def memberReturned = service.findByCardNumber(cardNumber, 20, 0, "", "asc")

        then: 'successfully return the transaction'
        memberReturned != null
        memberReturned instanceof Map
        memberReturned.totalResults == 1

        cleanup:
        Member.metaClass = null
        Member.metaClass.static.createCriteria = { return defaultCriteria }
    }

    def generateDummyMember(String cardNumber, String email) {
        def member = new Member(
            id: 1,
            retailerId: 123,
            cardType: 1,
            firstName: "John",
            lastName: "Doe",
            email: email,
            mobile_no: "1234567890",
            password: "password123",
            resetToken: "reset123",
            resetTokenExpiry: new Date(),
            cardNumber: cardNumber,
            postcode: "12345",
            dateOfBirth: new Date(),
            lastTransaction: new Date(),
            offersAvailable: 5,
            currentPoints: 100,
            currentSpend: 100.0,
            currentSavings: 50.0,
            currentStamps: 10,
            maxStamps: 20,
            status: MemberStatus.ACTIVE,
            dateCreated: new Date(),
            dateUpdated: new Date()
        )

        member
    }
}