cd ..
CALL ./gradlew.bat clean
CALL grails clean
CALL grails -Dgrails.env=zagreus assemble
echo BUILD COMPLETE - CHECK FOR ERRORS