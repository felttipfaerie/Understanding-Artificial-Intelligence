@echo off
cd /d "%~dp0"
"C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot\bin\javac.exe" -cp "tika-app-2.9.2.jar" TextExtraction.java
"C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot\bin\java.exe" -cp ".;tika-app-2.9.2.jar" TextExtraction "%USERPROFILE%\Downloads\Rose_Barfield_CV_March 2026 -V2.docx.pdf"
