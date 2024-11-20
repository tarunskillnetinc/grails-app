cd ..
CALL ./gradlew.bat clean
CALL grails clean
CALL grails -Dgrails.env=clientuat1 assemble
echo BUILD COMPLETE - CHECK FOR ERRORS