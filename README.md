# Setup
## installing Java JRE
- a jdk works as well.
- this has been tested on openJDK17
### Windows
Use this link: https://www.oracle.com/java/technologies/downloads/
### Mac OS
Use this link: https://www.oracle.com/java/technologies/downloads/
### Linux
Debain-based systems:
`sudo apt-get install openjdk17-jre`
## seting up environment
In the server and client directories, make sure to have a data folder so communications can happen **THE JAVA CODE WILL NOT DO THIS FOR YOU**
- the folder must be named "data"
- the client file must be in a folder named "client," and the server code must be in a folder called "server"
# Running
open 2 terminals and navigate to the client folder in one, and the server folder in the other.
run `java server2.java [portnumber]` in your server terminal first, then `java client2.java localhost [portnumber]` in the client terminal
**THE PORT NUMBERS MUST BE THE SAME OR IT WILL SPIT OUT AN ERROR**