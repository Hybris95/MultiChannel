@echo off
@javac -Xlint -cp "./class;./jars/craftbukkit.jar;./jars/Permissions.jar" -d "./class" ./src/com/hybris/bukkit/multichannel/*.java
@pause