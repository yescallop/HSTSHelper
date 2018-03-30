# HSTSHelper

HSTSHelper uses [LittleProxy](https://github.com/adamfisk/LittleProxy) to create a HTTP proxy server which redirects specific HTTP requests (filtered by regex rules) to HTTPS

## ATTENTION
This program runs only on Windows operating system

## Usage
1. Compile it yourself into a jar or directly download the jar
2. Create a file called "rules.txt"
3. Put your regex rules in the file ([example](https://github.com/yescallop/HSTSHelper/blob/master/rules.txt))
4. Execute `java -jar HSTSHelper.jar` or anything else
5. The proxy will be automatically set to system default
6. Press enter to stop the program