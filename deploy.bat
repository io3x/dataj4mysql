@echo off
call mvn clean
call mvn package -Dmaven.test.skip=true
rd /s /q "%cd%\deploy\"

md "%cd%\deploy"
md %cd%\deploy\templates
md %cd%\deploy\static
copy "%cd%\target\application.yml" "%cd%\deploy"
copy "%cd%\target\dataj4mysql.jar" "%cd%\deploy"
xcopy /y /c /e %cd%\target\templates\*.* %cd%\deploy\templates
xcopy /y /c /e %cd%\target\static\*.* %cd%\deploy\static
(
echo @echo off
echo java -jar dataj4mysql.jar
)>"%cd%\deploy\start-dataj4mysql.bat"