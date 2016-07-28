IV Tool
=======

Download
--------

https://github.com/Tsunamii/IVTool/releases

Syntax
------

```
usage: ivtool [-ptc] -user <user> -pass <1234> [-r] [-f] [-out <file>]
 -d,--debug			show the debug messages
 -f,--force			forces the rename for pokemon that already have a nickname
 -h,--help			print this message
 -o,--out <arg>		create an output file
	--pass <arg>	your ptc/google password
	--ptc			use your ptc account for login instead of google
 -r,--rename		rename every pokemon without a nickname
	--sleep <arg>	sleep time after each action in ms (default = 2000ms)
	--user <arg>	your ptc username / google email
```

Examples
--------

1) Base command (Login with google and print to command line)
```
java -jar ivtool.jar -user=admin -pass=1234
```
2) You want to use your ptc instead (-ptc): 
```
java -jar ivtool.jar -ptc -user=admin -pass=1234
```
3) You want to rename (-r) all your pokemon and export the list as output.csv (-out=output.csv)
```
java -jar ivtool.jar -ptc -user=admin -pass=1234 -r -out=output.csv
```

Example output.csv
--------------

![output.csv](https://raw.githubusercontent.com/Tsunamii/IVTool/master/example_output.jpg)

Changelog
---------

0.1.1

* fixed atk, def, sta calculation
* changed damage per cp to use current cp instead of max cp

0.1.0

* added more infos for the pokemon (atk, def, sta, maxCP, dps, dps per cp, ...)
* updated to the latest pogo-api dev branch version
* google login now uses email+passwort (no more tokens!!!)
* added option to export the list
* added option to print debug information from the pogo-api

0.0.2

* added sorting (Nr asc -> IV desc -> CP desc)
* added sleep after each action (default: 2000ms)
* added option to force renaming for all pokemon

0.0.1

* initial release

TODO
--------

* star pokemon with iv >= x% (implemented but currently not working)
* transfer duplicate pokemon with iv <= x%

Credits
-------

* Java API (https://github.com/Grover-c13/PokeGOAPI-Java)

/!\ WARNING /!\
===============

This tool uses inofficial API calls to gather the needed information, so there is a possibility to be banned for using it. 
I'm not responsible for any damage to your account. Use the tool at your own risk.
